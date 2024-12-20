package com.gu.flexible.snapshotter

import com.amazonaws.regions.Region
import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.s3.model.PutObjectResult
import com.gu.flexible.snapshotter.config.{CommonConfig, SnapshotterConfig}
import com.gu.flexible.snapshotter.logic._
import com.gu.flexible.snapshotter.model.{Attempt, SnapshotRequest}
import com.gu.flexible.snapshotter.resources.{AWSClientFactory, WSClientFactory}
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SnapshottingLambda extends Logging {
  import ApiLogic._
  import S3Logic._
  import SNSLogic._

  implicit val region: Region = AWSClientFactory.getRegion
  implicit val wsClient = WSClientFactory.createClient
  implicit val s3Client = AWSClientFactory.createS3Client
  implicit val cloudWatchClient = AWSClientFactory.createCloudwatchClient

  def run(input: SNSEvent): Unit = {
    implicit val config = SnapshotterConfig.resolve().get

    val requests = fromLambdaEvent(input)
    log.info(s"Processing message IDs: ${requests.map(_.id).mkString(", ")}")

    val results = snapshot(requests.map(_.content), config)
    val fin = SnapshottingLambda.logResults(results)

    FutureUtils.await(fin)
  }

  def snapshot(requests: Seq[String], config: SnapshotterConfig): Attempt[Seq[(Attempt[PutObjectResult], Attempt[PutObjectResult])]] = {
    implicit val implicitConfig = config
    val snapshotRequestAttempts = requests.map(deserialise[SnapshotRequest])
    for {
      snapshotRequests <- Attempt.successfulAttempts(snapshotRequestAttempts)
      apiResults = snapshotRequests.map(contentForSnapshot)
      successfulApiResults <- Attempt.successfulAttempts(apiResults)
    } yield {
      successfulApiResults.map{ snapshot =>
        val snapshotTime = new DateTime()
        val snapshotKey = makeKey(snapshot.id, snapshotTime, "json")
        val snapshotSummaryKey = makeKey(snapshot.id, snapshotTime, "info.json")
        (
          uploadToS3Bucket(snapshotKey, snapshot.data),
          uploadToS3Bucket(snapshotSummaryKey, snapshot.summaryData)
        )
      }
    }
  }

  def shutdown(): Unit = {
    s3Client.shutdown()
    wsClient.close()
  }
}

object SnapshottingLambda extends Logging {
  import MetricName._

  def logResults(results: Attempt[Seq[(Attempt[PutObjectResult],Attempt[PutObjectResult])]])
    (implicit cloudWatchClient:AmazonCloudWatch, config: CommonConfig): Future[Unit] = {
    results.fold(
      { failed =>
        CloudWatchLogic.putMetricData(
          MetricName.contentSnapshotError -> MetricValue(failed.errors.size, MetricValue.Count)
        )
        Future.successful(failed.errors.foreach(_.logTo(log)))
      },
      { succeeded =>
        val data = succeeded.map(_._1)
        val summaries = succeeded.map(_._2)
        logResultsAttempts(data, Some(contentSnapshotSuccess), Some(contentSnapshotError))
        logResultsAttempts(summaries)
      }
    ).flatMap(identity)
  }

  def logResultsAttempts(succeeded: Seq[Attempt[PutObjectResult]], successMetric: Option[MetricName] = None,
    failureMetric: Option[MetricName] = None)(implicit cloudWatchClient:AmazonCloudWatch, config: CommonConfig): Future[Unit] = {
    Future.sequence(succeeded.map(_.asFuture)).map { attempts =>
      attempts.foreach {
        case Left(failures) =>
          failureMetric.map { metric =>
            CloudWatchLogic.putMetricData(metric -> MetricValue(failures.errors.size, MetricValue.Count))
          }
          failures.errors.foreach(_.logTo(log))
        case Right(result) =>
          successMetric.map { metric =>
            CloudWatchLogic.putMetricData(metric -> MetricValue(1.0, MetricValue.Count)
            )
          }
          log.info(s"SUCCESS: $result")
      }
    }
  }
}
