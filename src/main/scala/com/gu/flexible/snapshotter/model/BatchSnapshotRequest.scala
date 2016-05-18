package com.gu.flexible.snapshotter.model

import play.api.libs.json.Json

case class BatchSnapshotRequest(contentIds:Seq[String], metadata:SnapshotMetadata) {
  def asSnapshotRequests: Seq[SnapshotRequest] = contentIds.map(SnapshotRequest(_, metadata))
}

object BatchSnapshotRequest {
  implicit val contentIdsFormats = Json.format[BatchSnapshotRequest]
}