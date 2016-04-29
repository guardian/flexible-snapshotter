package com.gu.flexible.snapshotter.model

import play.api.libs.json.Json

case class BatchSnapshotRequest(contentIds:Seq[String], reason:String) {
  def asSnapshotRequests: Seq[SnapshotRequest] = contentIds.map(SnapshotRequest(_, reason))
}

object BatchSnapshotRequest {
  implicit val contentIdsFormats = Json.format[BatchSnapshotRequest]
}