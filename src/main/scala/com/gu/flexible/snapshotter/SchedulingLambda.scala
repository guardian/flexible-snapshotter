package com.gu.flexible.snapshotter

import java.util.{Map => JMap}

import scala.concurrent.ExecutionContext.Implicits.global
import com.amazonaws.services.lambda.runtime.Context
import com.gu.flexible.snapshotter.logic.{ApiLogic, KinesisLogic}
import com.gu.flexible.snapshotter.resources.{AWSClientFactory, WSClientFactory}
import play.api.libs.ws.WSClient

class SchedulingLambda extends Logging {
  import ApiLogic._
  import KinesisLogic._

  implicit val config = Config()
  implicit val wsClient: WSClient = WSClientFactory.createClient
  implicit val kinesisClient = AWSClientFactory.createKinesisClient

  // this is run under a lambda cron
  def run(event: JMap[String, Object], context: Context): Unit = {
    for {
      apiResult <- contentModifiedSince(fiveMinutesAgo)
      contentIds = parseContentIds(apiResult)
      serialisedContentIds = serialiseToByteBuffer(contentIds)
    } {
      sendToKinesis(config.kinesisStream, serialisedContentIds)
    }
  }
}
