package com.gu.flexible.snapshotter.model

import play.api.libs.json._

import scala.language.postfixOps

case class Snapshot(id: String, metadata: SnapshotMetadata, data: JsValue) {
  val summaryData: JsObject = Json.obj(
    "reason" -> JsString(metadata.reason)
  ) ++ JsObject(Snapshot.fieldsToExtract.flatMap { field =>
    Snapshot.extractField(data, field).map(field ->)
  })
}

object Snapshot {
  val fieldsToExtract = List(
    "preview.fields.headline",
    "preview.settings.commentable",
    "type",
    "preview.settings.liveBloggingNow",
    "preview.settings.legallySensitive",
    "published",
    "scheduledLaunchDate",
    "preview.settings.embargoedUntil"
  )

  def extractField(json: JsLookup, field: String): Option[JsValue] = {
    field.split("\\.").foldLeft(json) {
      case (js, component) => js \ component
    }.result.toOption
  }
}