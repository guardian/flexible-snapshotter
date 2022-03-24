package com.gu.flexible.snapshotter.config

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
  def resolve(): Option[SnapshotterConfig] = {
    val maybeBucket = Config.envOrNoneAndLog("SNAPSHOT_BUCKET")
    val maybeStage = Config.envOrNoneAndLog("STAGE")
    val maybeStack = Config.envOrNoneAndLog("STACK")
    val maybeKmsKey = Config.envOrNoneAndLog("KMS_KEY_ARN")

    for {
      bucket <- maybeBucket
      stage <- maybeStage
      stack <- maybeStack
      kmsKey = maybeKmsKey
    } yield SnapshotterConfig(
      bucket,
      stage,
      stack,
      kmsKey
    )
  }
}

