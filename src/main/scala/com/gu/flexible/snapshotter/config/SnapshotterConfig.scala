package com.gu.flexible.snapshotter.config

import play.api.libs.json.Json

import scala.util.Properties.envOrNone

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
  def resolve(): Option[SnapshotterConfig] = for {
   bucket <- Config.envOrNoneAndLog("SNAPSHOT_BUCKET")
   stage <- Config.envOrNoneAndLog("STAGE")
   stack <- Config.envOrNoneAndLog("STACK")
   kmsKey = Config.envOrNoneAndLog("KMS_KEY_ARN")
  } yield SnapshotterConfig(
    bucket,
    stage,
    stack,
    kmsKey
  )
}

