package com.gu.flexible.snapshotter

import java.util.{Map => JMap}

import scala.concurrent.ExecutionContext.Implicits.global
import com.amazonaws.services.lambda.runtime.Context
import com.gu.flexible.snapshotter.logic.{ApiLogic, KinesisLogic}
import com.gu.flexible.snapshotter.model.BatchSnapshotRequest
import com.gu.flexible.snapshotter.resources.{AWSClientFactory, WSClientFactory}
import play.api.libs.ws.WSClient

class SchedulingLambda extends Logging {
  import ApiLogic._
  import KinesisLogic._

  implicit val wsClient: WSClient = WSClientFactory.createClient
  implicit val kinesisClient = AWSClientFactory.createKinesisClient
  implicit val lambdaClient = AWSClientFactory.createLambdaClient

  // this is run under a lambda cron
  def run(event: JMap[String, Object], context: Context): Unit = {
    implicit val config = SchedulerConfig.resolve(Config.guessStage(context), context)

    for {
      apiResult <- contentModifiedSince(fiveMinutesAgo)
      contentIds = parseContentIds(apiResult)
      snapshotRequestBatch = BatchSnapshotRequest(contentIds, "Scheduled snapshot")
      serialisedContentIds = serialiseToByteBuffer(snapshotRequestBatch)
    } {
      sendToKinesis(config.kinesisStream, serialisedContentIds)
    }
  }
}
