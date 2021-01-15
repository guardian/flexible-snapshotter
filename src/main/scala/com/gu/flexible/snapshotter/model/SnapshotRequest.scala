package com.gu.flexible.snapshotter.model

import play.api.libs.json.{JsValue, Json}

case class SnapshotRequest(contentId:String, metadata:SnapshotMetadata, content: Option[JsValue])
object SnapshotRequest {
  implicit val formats = Json.format[SnapshotRequest]
}