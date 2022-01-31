package com.gu.flexible.snapshotter.config

import com.amazonaws.services.lambda.AWSLambda
import com.amazonaws.services.lambda.runtime.Context
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
  def resolve(stage: String, context: Context)(implicit lambdaClient: AWSLambda): SchedulerConfig = {
    val lambdaJson = LambdaConfig.getDescriptionJson(context)
    val lambdaConfig = lambdaJson.as[LambdaSchedulerConfig]
    SchedulerConfig(
      snsTopicArn = lambdaConfig.snsTopicArn,
      stage = stage,
      stack = lambdaConfig.stack
    )
  }
}