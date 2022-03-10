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
  def resolve(): Option[SchedulerConfig] = for {
    snsTopicArn <- Config.envOrNoneAndLog("SNS_TOPIC_ARN")
    stage <- Config.envOrNoneAndLog("STAGE")
    stack <- Config.envOrNoneAndLog("STACK")
  } yield SchedulerConfig(
    snsTopicArn,
    stage,
    stack
  )
}
