package com.gu.flexible.snapshotter

import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.s3.model.PutObjectResult
import com.gu.flexible.snapshotter.config.{CommonConfig, Config, SnapshotterConfig}
import com.gu.flexible.snapshotter.logic._
import com.gu.flexible.snapshotter.model.{Attempt, SnapshotRequest}
import com.gu.flexible.snapshotter.resources.{AWSClientFactory, WSClientFactory}
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

class SnapshottingLambda extends Logging {
  import ApiLogic._
  import S3Logic._
  import SNSLogic._

  implicit val region: Regions = AWSClientFactory.getRegion
  implicit val wsClient = WSClientFactory.createClient
  implicit val s3Client = AWSClientFactory.createS3Client
  implicit val lambdaClient = AWSClientFactory.createLambdaClient
  implicit val cloudWatchClient = AWSClientFactory.createCloudwatchClient

  def run(input: SNSEvent, context: Context): Unit = {
    implicit val config = SnapshotterConfig.resolve(Config.guessStage(context), context)

    val requests = fromLambdaEvent(input)
    log.info(s"Processing sequence numbers: ${requests.map(_.id)}")

    val results = snapshot(requests.map(_.content), config, context)
    val fin = SnapshottingLambda.logResults(results)

    FutureUtils.await(fin)
  }

  def snapshot(requests: Seq[String], config: SnapshotterConfig, context: Context): Attempt[Seq[Attempt[PutObjectResult]]] = {
    implicit val implicitConfig = config
    val snapshotRequestAttempts = requests.map(deserialise[SnapshotRequest])
    for {
      snapshotRequests <- Attempt.successfulAttempts(snapshotRequestAttempts)
      apiResults = snapshotRequests.map(contentForSnapshot)
      successfulApiResults <- Attempt.successfulAttempts(apiResults)
    } yield {
      successfulApiResults.map{ snapshot =>
        uploadToS3Bucket(snapshot.id, new DateTime(), snapshot.snapshotDocument)
      }
    }
  }

  def shutdown(): Unit = {
    lambdaClient.shutdown()
    s3Client.shutdown()
    wsClient.close()
  }
}

object SnapshottingLambda extends Logging {
  def logResults(results: Attempt[Seq[Attempt[PutObjectResult]]])
    (implicit cloudWatchClient:AmazonCloudWatchClient, config: CommonConfig): Future[Unit] = {
    results.fold(
      { failed =>
        CloudWatchLogic.putMetricData(
          // this key is referenced in the cloudformation - don't change it!
          MetricName.contentSnapshotError -> MetricValue(failed.errors.size, MetricValue.Count)
        )
        Future.successful(failed.errors.foreach(_.logTo(log)))
      },
      { succeeded =>
        Future.sequence(succeeded.map(_.asFuture)).map { attempts =>
          attempts.foreach {
            case Left(failures) =>
              CloudWatchLogic.putMetricData(
                // this key is referenced in the cloudformation - don't change it!
                MetricName.contentSnapshotError -> MetricValue(failures.errors.size, MetricValue.Count)
              )
              failures.errors.foreach(_.logTo(log))
            case Right(result) =>
              CloudWatchLogic.putMetricData(
                MetricName.contentSnapshotSuccess -> MetricValue(1.0, MetricValue.Count)
              )
              log.info(s"SUCCESS: $result")
          }
        }
      }
    ).flatMap(identity)
  }
}