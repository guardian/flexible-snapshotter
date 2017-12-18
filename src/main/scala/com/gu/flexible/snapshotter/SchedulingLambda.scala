package com.gu.flexible.snapshotter

import java.util.{Map => JMap}

import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.sns.model.PublishResult
import com.gu.flexible.snapshotter.config.{CommonConfig, Config, SchedulerConfig}
import com.gu.flexible.snapshotter.logic._
import com.gu.flexible.snapshotter.model.{Attempt, SnapshotMetadata, SnapshotRequest}
import com.gu.flexible.snapshotter.resources.{AWSClientFactory, WSClientFactory}
import org.apache.log4j.LogManager
import play.api.libs.ws.StandaloneWSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SchedulingLambda extends Logging {
  import ApiLogic._
  import SNSLogic._

  implicit val region: Regions = AWSClientFactory.getRegion
  implicit val wsClient: StandaloneWSClient  = WSClientFactory.createClient
  implicit val snsClient = AWSClientFactory.createSNSClient
  implicit val lambdaClient = AWSClientFactory.createLambdaClient
  implicit val cloudWatchClient = AWSClientFactory.createCloudwatchClient

  // this is run under a lambda cron
  def run(event: JMap[String, Object], context: Context): Unit = {
    implicit val config = SchedulerConfig.resolve(Config.guessStage(context), context)
    val result = schedule(config, context)
    val fin = SchedulingLambda.logResult(result)

    FutureUtils.await(fin)
  }

  def schedule(config: SchedulerConfig, context: Context): Attempt[Seq[PublishResult]] = {
    implicit val implicitConfig = config
    log.info(s"$config")

    val metadata = SnapshotMetadata("Scheduled snapshot")

    for {
      apiResult <- contentModifiedSince(fiveMinutesAgo)
      contentIds = parseContentIds(apiResult)
      snapshotRequests = contentIds.map(SnapshotRequest(_, metadata))
    } yield {
      snapshotRequests.map { request =>
        val serialisedRequest = serialise(request)
        publish(config.snsTopicArn, serialisedRequest)
      }
    }
  }

  def shutdown() = {
    LogManager.shutdown()
    snsClient.shutdown()
    lambdaClient.shutdown()
    wsClient.close()
  }
}

object SchedulingLambda extends Logging {
  def logResult(result: Attempt[Seq[PublishResult]])
    (implicit cloudWatchClient:AmazonCloudWatch, config: CommonConfig): Future[Unit] = {
    result.fold(
      { errors =>
        errors.errors.foreach(_.logTo(log))
        CloudWatchLogic.putMetricData(
          MetricName.scheduledContentIdsError -> MetricValue(errors.errors.size, MetricValue.Count)
        )
      }, { publishResults =>
        log.info(s"SUCCESS: $publishResults")
        CloudWatchLogic.putMetricData(
          MetricName.scheduledContentIdsSuccess -> MetricValue(publishResults.size, MetricValue.Count)
        )
      }
    )
  }
}