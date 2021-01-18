package com.gu.flexible.snapshotter.logic

import com.gu.flexible.snapshotter.model._
import com.gu.flexible.snapshotter.Logging
import com.gu.flexible.snapshotter.config.CommonConfig
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{StandaloneWSClient, StandaloneWSRequest}

import scala.concurrent.ExecutionContext

object ApiLogic extends Logging {
  def contentForSnapshot(snapshotRequest: SnapshotRequest)(implicit ws:StandaloneWSClient, config:CommonConfig, context:ExecutionContext): Attempt[Snapshot] = snapshotRequest.content match {
    case Some(contentJson) => {
      log.info(s"Snapshot for ID: ${snapshotRequest.contentId} already has content.")
      Attempt.Right(Snapshot(snapshotRequest.contentId, snapshotRequest.metadata, contentJson, config.fieldsToExtract))
    }
    case None => {
      log.info(s"Fetching content for snapshot with ID: ${snapshotRequest.contentId}")
      contentForId(snapshotRequest.contentId).map { contentJson =>
        Snapshot(snapshotRequest.contentId, snapshotRequest.metadata, contentJson, config.fieldsToExtract)
      }
    }
  }

  def contentForId(id: String)(implicit ws:StandaloneWSClient, config:CommonConfig, context:ExecutionContext): Attempt[JsValue] = {
    val request = ws.url(s"${config.contentRawUri}/$id").withQueryStringParameters(List(("includePreview", "true"), ("includeLive", "true")): _*)
    log.info(s"Requesting content for ID $id in stage ${config.stage} stack ${config.stack}")
    jsonOnOKStatus(request)
  }

  def contentModifiedSince(since: DateTime)(implicit ws:StandaloneWSClient, config:CommonConfig, context:ExecutionContext): Attempt[JsValue] = {
    val request = ws.url(config.contentUri).withQueryStringParameters(("since", since.getMillis.toString))
    log.info(s"Requesting since $since - converted to ${since.getMillis}")
    jsonOnOKStatus(request)
  }

  def parseContentIds(json:JsValue): Seq[String] = (json \ "data").as[Seq[JsObject]].map(getId)

  def fiveMinutesAgo: DateTime = new DateTime().minusMinutes(5)

  private[snapshotter] def getId(doc: JsObject): String = (doc \ "data" \ "id").as[String]

  private[snapshotter] def jsonOnOKStatus(request: StandaloneWSRequest)(implicit context:ExecutionContext): Attempt[JsValue] = {
    val attempt = Attempt.Async.Right(request.get())
    attempt.flatMap { response =>
      response.status match {
        case 200 => Attempt.Right(Json.parse(response.body))
        case _ =>
          val message = s"${request.toString}: Response status was ${response.status}:${response.statusText}"
          log.warn(message)
          Attempt.Left(AttemptErrors(AttemptError(message)))
      }
    }
  }
}