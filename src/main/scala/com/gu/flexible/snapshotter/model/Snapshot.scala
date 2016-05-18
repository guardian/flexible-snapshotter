package com.gu.flexible.snapshotter.model

import play.api.libs.json.{JsValue, Json}

case class Snapshot(id: String, snapshotMetadata: SnapshotMetadata, data: JsValue) {
  lazy val snapshotDocument = Json.obj(
    "data" -> data,
    "metadata" -> snapshotMetadata
  )
}