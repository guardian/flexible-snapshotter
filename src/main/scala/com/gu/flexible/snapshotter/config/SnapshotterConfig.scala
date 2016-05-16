package com.gu.flexible.snapshotter.config

import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.runtime.Context
import play.api.libs.json.Json

object LambdaSnapshotterConfig {
  implicit val configReads = Json.reads[LambdaSnapshotterConfig]
}
case class LambdaSnapshotterConfig(bucket: String, kmsKey: Option[String])

case class SnapshotterConfig(
  bucket: String,
  apiUrl: String,
  kmsKey: Option[String] = None,
  region: Region = Regions.getCurrentRegion) extends CommonConfig

object SnapshotterConfig {
  def resolve(stage: String, context: Context)(implicit lambdaClient: AWSLambdaClient): SnapshotterConfig = {
    val lambdaJson = LambdaConfig.getDescriptionJson(context)
    val lambdaConfig = lambdaJson.as[LambdaSnapshotterConfig]
    SnapshotterConfig(lambdaConfig.bucket, Config.apiUrl(stage), lambdaConfig.kmsKey)
  }
}

