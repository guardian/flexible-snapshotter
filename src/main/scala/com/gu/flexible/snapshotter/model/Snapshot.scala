package com.gu.flexible.snapshotter.model

import play.api.libs.json._

import scala.language.postfixOps

case class Snapshot(id: String, metadata: SnapshotMetadata, data: JsValue, fieldsToExtract: List[List[String]]) {
  val summaryMetadata: JsObject = Json.obj("reason" -> JsString(metadata.reason))
  val summaryFields: JsObject = fieldsToExtract.flatMap(Snapshot.soloField(data, _)).
    foldLeft(Json.obj())(_ deepMerge _)
  val summaryData: JsObject = Json.obj("metadata" -> summaryMetadata, "summary" -> summaryFields)
}

object Snapshot {
  def soloField(json: JsLookup, field: List[String]): Option[JsObject] = {
    field match {
      case head :: Nil => (json \ head).toOption.map(obj => Json.obj(head -> obj))
      case head :: tail => soloField(json \ head, tail).map(obj => Json.obj(head -> obj))
      case _ => None
    }
  }
}