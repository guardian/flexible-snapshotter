package com.gu.flexible.snapshotter.model

import play.api.libs.json.Json

case class SnapshotMetadata(reason:String)
object SnapshotMetadata {
  implicit val formats = Json.format[SnapshotMetadata]
}
