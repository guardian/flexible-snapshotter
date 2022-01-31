package com.gu.flexible.snapshotter.config

import com.amazonaws.services.lambda.AWSLambda
import com.amazonaws.services.lambda.runtime.Context
import play.api.libs.json.Json

object LambdaSnapshotterConfig {
  implicit val configReads = Json.reads[LambdaSnapshotterConfig]
}
case class LambdaSnapshotterConfig(bucket: String, stack: String, kmsKey: Option[String])

case class SnapshotterConfig(
  bucket: String,
  stage: String,
  stack: String,
  kmsKey: Option[String] = None) extends CommonConfig

object SnapshotterConfig {
  def resolve(stage: String, context: Context)(implicit lambdaClient: AWSLambda): SnapshotterConfig = {
    val lambdaJson = LambdaConfig.getDescriptionJson(context)
    val lambdaConfig = lambdaJson.as[LambdaSnapshotterConfig]
    SnapshotterConfig(
      bucket = lambdaConfig.bucket,
      stage = stage,
      kmsKey = lambdaConfig.kmsKey,
      stack = lambdaConfig.stack
    )
  }
}

