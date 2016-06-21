package com.gu.flexible.snapshotter.logic

import com.gu.flexible.snapshotter.model._
import com.gu.flexible.snapshotter.Logging
import com.gu.flexible.snapshotter.config.CommonConfig
import org.joda.time.DateTime
import play.api.http.Status
import play.api.libs.json.{JsObject, JsValue}
import play.api.libs.ws.{WSClient, WSRequest}

import scala.concurrent.ExecutionContext

object ApiLogic extends Logging {
  def contentForSnapshot(snapshotRequest: SnapshotRequest)(implicit ws:WSClient, config:CommonConfig, context:ExecutionContext): Attempt[Snapshot] = {
    contentForId(snapshotRequest.contentId).map{ json =>
      Snapshot(snapshotRequest.contentId, snapshotRequest.metadata, json, config.fieldsToExtract)
    }
  }

  def contentForId(id: String)(implicit ws:WSClient, config:CommonConfig, context:ExecutionContext): Attempt[JsValue] = {
    val request = ws.url(s"${config.contentRawUri}/$id").withQueryString("includePreview"->"true", "includeLive"->"true")
    log.info(s"Requesting content for ID $id")
    jsonOnOKStatus(request)
  }

  def contentModifiedSince(since: DateTime)(implicit ws:WSClient, config:CommonConfig, context:ExecutionContext): Attempt[JsValue] = {
    val request = ws.url(config.contentUri).withQueryString("since" -> since.getMillis.toString)
    log.info(s"Requesting since $since - converted to ${since.getMillis}")
    jsonOnOKStatus(request)
  }

  def parseContentIds(json:JsValue): Seq[String] = (json \ "data").as[Seq[JsObject]].map(getId)

  def fiveMinutesAgo: DateTime = new DateTime().minusMinutes(5)

  private[snapshotter] def getId(doc: JsObject): String = (doc \ "data" \ "id").as[String]

  private[snapshotter] def jsonOnOKStatus(request: WSRequest)(implicit context:ExecutionContext): Attempt[JsValue] = {
    val attempt = Attempt.Async.Right(request.get())
    attempt.flatMap { response =>
      response.status match {
        case Status.OK => Attempt.Right(response.json)
        case _ =>
          val message = s"${request.toString}: Response status was ${response.status}:${response.statusText}"
          log.warn(message)
          Attempt.Left(AttemptErrors(AttemptError(message)))
      }
    }
  }
}