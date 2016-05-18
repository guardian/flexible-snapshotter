package com.gu.flexible.snapshotter

import java.util.{Map => JMap}

import com.amazonaws.regions.Regions

import scala.concurrent.ExecutionContext.Implicits.global
import com.amazonaws.services.lambda.runtime.Context
import com.gu.flexible.snapshotter.config.{Config, SchedulerConfig}
import com.gu.flexible.snapshotter.logic.{ApiLogic, KinesisLogic}
import com.gu.flexible.snapshotter.model.{BatchSnapshotRequest, SnapshotMetadata}
import com.gu.flexible.snapshotter.resources.{AWSClientFactory, WSClientFactory}
import org.apache.log4j.LogManager
import play.api.libs.ws.WSClient

import scala.concurrent.duration._
import scala.concurrent.Await
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
    val config = SchedulerConfig.resolve(Config.guessStage(context), context)
    schedule(config, context)
  }

  def schedule(config: SchedulerConfig, context: Context): Unit = {
    implicit val implicitConfig = config
    log.info(s"$config")

    val results = for {
      apiResult <- contentModifiedSince(fiveMinutesAgo)
      contentIds = parseContentIds(apiResult)
    } yield {
      val snapshotRequestBatchOption = if (contentIds.nonEmpty)
        Some(BatchSnapshotRequest(contentIds, SnapshotMetadata("Scheduled snapshot")))
      else None

      snapshotRequestBatchOption.map { batch =>
        val serialisedContentIds = serialiseToByteBuffer(batch)
        sendToKinesis(config.kinesisStream, serialisedContentIds)
      }
    }

    val fin = results.fold(
      { errors =>
        errors.errors.foreach(_.logTo(log))
      },{kinesisResult =>
        log.info(s"SUCCESS: $kinesisResult")
      })
    Await.ready(fin, 120 seconds)
  }

  def shutdown() = {
    LogManager.shutdown()
    kinesisClient.shutdown()
    lambdaClient.shutdown()
    wsClient.close()
  }
}