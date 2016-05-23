package com.gu.flexible.snapshotter.model

import play.api.libs.json.Json

case class SnapshotRequest(contentId:String, metadata:SnapshotMetadata)
object SnapshotRequest {
  implicit val formats = Json.format[SnapshotRequest]
}