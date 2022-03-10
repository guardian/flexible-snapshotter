package com.gu.flexible.snapshotter.config

import com.gu.flexible.snapshotter.Logging
import scala.util.Properties.envOrNone

object Config extends Logging {
  def envOrNoneAndLog(name: String): Option[String] =
    envOrNone(name).orElse {
      log.warn(s"Could not get environment variable ${name}")
      None
    }

  def apiUrl(stage: String, stack: String): String = {
    val subDomain = stack match {
      case "flexible-secondary" => "apiv2" // we're not running a proxy (or mongo) in secondary, go direct to postgres
      case _ => "flexible-api"
    }

    s"http://$subDomain.${stage.toLowerCase}.${stack.toLowerCase}.gudiscovery.:8080"
  }

  val defaultFieldsToExtract = List(
    "preview.fields.headline",
    "preview.settings.commentable",
    "type",
    "preview.settings.liveBloggingNow",
    "preview.settings.legallySensitive",
    "published",
    "scheduledLaunchDate",
    "preview.settings.embargoedUntil",
    "contentChangeDetails.published",
    "contentChangeDetails.lastModified",
    "contentChangeDetails.revision"
  ).map(_.split("\\.").toList)
}

trait CommonConfig {
  def cloudWatchNameSpace: String = "SnapshotterLambdas"
  def cloudWatchDimensions: Seq[(String,String)] = Seq("Stage" -> stage)
  def stage: String
  def stack: String

  def apiUrl: String = Config.apiUrl(stage, stack)

  def contentUri = s"$apiUrl/content"
  def contentRawUri = s"$apiUrl/contentRaw"

  def fieldsToExtract = Config.defaultFieldsToExtract
}
