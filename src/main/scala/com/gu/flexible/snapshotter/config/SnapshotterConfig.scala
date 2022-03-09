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
   bucket <- envOrNone("SNAPSHOT_BUCKET")
   stage <- envOrNone("STAGE")
   stack <- envOrNone("STACK")
   kmsKey = envOrNone("KMS_KEY_ARN")
  } yield SnapshotterConfig(
    bucket,
    stage,
    stack,
    kmsKey
  )
}

