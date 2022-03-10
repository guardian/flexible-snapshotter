package com.gu.flexible.snapshotter.config

import com.gu.flexible.snapshotter.Logging
import play.api.libs.json.Json


object LambdaSchedulerConfig {
  implicit val configReads = Json.reads[LambdaSchedulerConfig]
}
case class LambdaSchedulerConfig(snsTopicArn: String, stack: String)

case class SchedulerConfig(
  snsTopicArn: String,
  stage: String,
  stack: String) extends CommonConfig

object SchedulerConfig extends Logging {
  def resolve(): Option[SchedulerConfig] = {
    val maybeSnsTopicArn = Config.envOrNoneAndLog("SNS_TOPIC_ARN")
    val maybeStage = Config.envOrNoneAndLog("STAGE")
    val maybeStack = Config.envOrNoneAndLog("STACK")

    for {
      snsTopicArn <- maybeSnsTopicArn
      stage <- maybeStage
      stack <- maybeStack
    } yield SchedulerConfig(
      snsTopicArn,
      stage,
      stack
    )
  }
}
