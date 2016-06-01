package com.gu.flexible.snapshotter

import java.util.{Map => JMap}

import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient
import com.amazonaws.services.kinesis.model.PutRecordResult
import com.amazonaws.services.lambda.runtime.Context
import com.gu.flexible.snapshotter.config.{CommonConfig, Config, SchedulerConfig}
import com.gu.flexible.snapshotter.logic._
import com.gu.flexible.snapshotter.model.{Attempt, SnapshotMetadata, SnapshotRequest}
import com.gu.flexible.snapshotter.resources.{AWSClientFactory, WSClientFactory}
import org.apache.log4j.LogManager
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

class SchedulingLambda extends Logging {
  import ApiLogic._
  import KinesisLogic._

  implicit val region: Regions = AWSClientFactory.getRegion
  implicit val wsClient: WSClient = WSClientFactory.createClient
  implicit val kinesisClient = AWSClientFactory.createKinesisClient
  implicit val lambdaClient = AWSClientFactory.createLambdaClient
  implicit val cloudWatchClient = AWSClientFactory.createCloudwatchClient

  // this is run under a lambda cron
  def run(event: JMap[String, Object], context: Context): Unit = {
    implicit val config = SchedulerConfig.resolve(Config.guessStage(context), context)
    val result = schedule(config, context)
    val fin = SchedulingLambda.logResult(result)

    FutureUtils.await(fin)
  }

  def schedule(config: SchedulerConfig, context: Context): Attempt[Seq[PutRecordResult]] = {
    implicit val implicitConfig = config
    log.info(s"$config")

    val metadata = SnapshotMetadata("Scheduled snapshot")

    for {
      apiResult <- contentModifiedSince(fiveMinutesAgo)
      contentIds = parseContentIds(apiResult)
      snapshotRequests = contentIds.map(SnapshotRequest(_, metadata))
    } yield {
      snapshotRequests.map { request =>
        val serialisedRequest = serialiseToByteBuffer(request)
        sendToKinesis(config.kinesisStream, serialisedRequest)
      }
    }
  }

  def shutdown() = {
    LogManager.shutdown()
    kinesisClient.shutdown()
    lambdaClient.shutdown()
    wsClient.close()
  }
}

object SchedulingLambda extends Logging {
  def logResult(result: Attempt[Seq[PutRecordResult]])
    (implicit cloudWatchClient:AmazonCloudWatchClient, config: CommonConfig): Future[Unit] = {
    result.fold(
      { errors =>
        errors.errors.foreach(_.logTo(log))
        CloudWatchLogic.putMetricData(
          "scheduledContentIdsError" -> MetricValue(errors.errors.size, MetricValue.Count)
        )
      }, { kinesisResults =>
        log.info(s"SUCCESS: $kinesisResults")
        CloudWatchLogic.putMetricData(
          "scheduledContentIdsSuccess" -> MetricValue(kinesisResults.size, MetricValue.Count)
        )
      }
    )
  }
}