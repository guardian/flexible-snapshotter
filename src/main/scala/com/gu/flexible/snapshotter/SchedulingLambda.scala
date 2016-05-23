package com.gu.flexible.snapshotter

import java.util.{Map => JMap}

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.Regions
import com.amazonaws.services.kinesis.model.PutRecordResult
import com.amazonaws.services.lambda.runtime.Context
import com.gu.flexible.snapshotter.config.{Config, KinesisAppenderConfig, LogStash, SchedulerConfig}
import com.gu.flexible.snapshotter.logic.{ApiLogic, FutureUtils, KinesisLogic}
import com.gu.flexible.snapshotter.model.{Attempt, SnapshotMetadata, SnapshotRequest}
import com.gu.flexible.snapshotter.resources.{AWSClientFactory, WSClientFactory}
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

  // this is run under a lambda cron
  def run(event: JMap[String, Object], context: Context): Unit = {
    val stage = Config.guessStage(context)
    val config = SchedulerConfig.resolve(stage, context)
    config.logstashKinesisStream.foreach { stream =>
      val config = KinesisAppenderConfig(stream, new DefaultAWSCredentialsProviderChain(), region)
      LogStash.enableLogstashKinesisHandler(config, "stack" -> "flexible", "stage" -> stage, "app" -> "snapshot-scheduling-lambda")
    }
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
    kinesisClient.shutdown()
    lambdaClient.shutdown()
    wsClient.close()
  }
}

object SchedulingLambda extends Logging {
  def logResult(result: Attempt[Seq[PutRecordResult]]): Future[Unit] = {
    result.fold(
      { errors =>
        errors.errors.foreach(_.logTo(log))
      }, { kinesisResults =>
        log.info(s"SUCCESS: $kinesisResults")
      }
    )
  }
}