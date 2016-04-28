package com.gu.flexible.snapshotter.model

import play.api.libs.json.Json

case class ContentIds(contentIds:Seq[String])

object ContentIds {
  implicit val contentIdsFormats = Json.format[ContentIds]
}