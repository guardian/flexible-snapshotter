package com.gu.flexible.snapshotter.logic

import com.gu.flexible.snapshotter.model.{Attempt, Snapshot, SnapshotRequest}
import com.gu.flexible.snapshotter.{Config, Logging}
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

object ApiLogic extends Logging {
  def contentForSnapshot(snapshotRequest: SnapshotRequest)(implicit ws:WSClient, config:Config, context:ExecutionContext): Attempt[Snapshot] = {
    contentForId(snapshotRequest.contentId).map{ json =>
      Snapshot(snapshotRequest.contentId, snapshotRequest.reason, json)
    }
  }

  def contentForId(id: String)(implicit ws:WSClient, config:Config, context:ExecutionContext): Attempt[JsValue] = {
    val request = ws.url(s"${config.contentRawUri}/$id")
    val json = request.get().map(_.json)
    json onFailure { case t => log.warn(s"Error occurred fetching content with ID $id from API", t)}
    Attempt.Async.Right(json)
  }

  def contentModifiedSince(since: DateTime)(implicit ws:WSClient, config:Config, context:ExecutionContext): Attempt[JsValue] = {
    val request = ws.url(config.contentUri).withQueryString("since" -> since.toString)
    val json = request.get().map(_.json)
    json onFailure { case t => log.warn("Error occurred fetching from API", t) }
    Attempt.Async.Right(json)
  }

  def parseContentIds(json:JsValue): Seq[String] = (json \ "data").as[Seq[JsObject]].map(getId)

  def fiveMinutesAgo: DateTime = new DateTime().minusMinutes(5)

  private[snapshotter] def getId(doc: JsObject): String = (doc \ "data" \ "id").as[String]
}