package com.gu.flexible.snapshotter.config

import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.runtime.Context
import play.api.libs.json.{Json, Reads}

object LambdaSchedulerConfig {
  implicit val configReads = Json.reads[LambdaSchedulerConfig]
}
case class LambdaSchedulerConfig(snsTopicArn: String, stack: String)

case class SchedulerConfig(
  snsTopicArn: String,
  stage: String,
  stack: String,
  region: Region = Regions.getCurrentRegion) extends CommonConfig

object SchedulerConfig {
  def resolve(stage: String, context: Context)(implicit lambdaClient: AWSLambdaClient): SchedulerConfig = {
    val lambdaJson = LambdaConfig.getDescriptionJson(context)
    val lambdaConfig = lambdaJson.as[LambdaSchedulerConfig]
    SchedulerConfig(
      snsTopicArn = lambdaConfig.snsTopicArn,
      stage = stage,
      stack = lambdaConfig.stack
    )
  }
}