package com.gu.flexible.snapshotter

import java.util.{Map => JMap}

import com.amazonaws.regions.Regions
import com.amazonaws.services.kinesis.model.PutRecordResult
import com.amazonaws.services.lambda.runtime.Context
import com.gu.flexible.snapshotter.config.{Config, SchedulerConfig}
import com.gu.flexible.snapshotter.logic.{ApiLogic, FutureUtils, KinesisLogic}
import com.gu.flexible.snapshotter.model.{Attempt, BatchSnapshotRequest, SnapshotMetadata}
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

  // this is run under a lambda cron
  def run(event: JMap[String, Object], context: Context): Unit = {
    val config = SchedulerConfig.resolve(Config.guessStage(context), context)
    val result = schedule(config, context)
    val fin = SchedulingLambda.logResult(result)

    FutureUtils.await(fin)
  }

  def schedule(config: SchedulerConfig, context: Context): Attempt[Option[PutRecordResult]] = {
    implicit val implicitConfig = config
    log.info(s"$config")

    for {
      apiResult <- contentModifiedSince(fiveMinutesAgo)
      contentIds = parseContentIds(apiResult)
    } yield {
      if (contentIds.nonEmpty) {
        val batch = BatchSnapshotRequest(contentIds, SnapshotMetadata("Scheduled snapshot"))
        val serialisedContentIds = serialiseToByteBuffer(batch)
        Some(sendToKinesis(config.kinesisStream, serialisedContentIds))
      } else None
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
  def logResult(result: Attempt[Option[PutRecordResult]]): Future[Unit] = {
    result.fold(
      { errors =>
        errors.errors.foreach(_.logTo(log))
      }, { kinesisResult =>
        log.info(s"SUCCESS: $kinesisResult")
      }
    )
  }
}