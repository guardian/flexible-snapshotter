package com.gu.flexible.snapshotter.model

import play.api.libs.json.Json

case class SnapshotRequestBatch(contentIds:Seq[String], reason:String) {
  def asSnapshotRequests: Seq[SnapshotRequest] = contentIds.map(SnapshotRequest(_, reason))
}

object SnapshotRequestBatch {
  implicit val contentIdsFormats = Json.format[SnapshotRequestBatch]
}