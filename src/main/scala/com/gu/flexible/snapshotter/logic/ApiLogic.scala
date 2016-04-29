package com.gu.flexible.snapshotter.logic

import com.gu.flexible.snapshotter.model.{Attempt, Snapshot, SnapshotRequest, BatchSnapshotRequest}
import com.gu.flexible.snapshotter.{Config, Logging}
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

object ApiLogic extends Logging {
  def contentForSnapshot(snapshotRequest: SnapshotRequest)(implicit ws:WSClient, config:Config, context:ExecutionContext): Attempt[Snapshot] = {
    val request = ws.url(s"${config.contentRawUri}/${snapshotRequest.contentId}")
    val json = request.get().map(response => Snapshot(snapshotRequest.contentId, snapshotRequest.reason, response.json))
    json onFailure { case t => log.warn(s"Error occurred fetching content with ID ${snapshotRequest.contentId} from API", t)}
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