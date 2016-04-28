package com.gu.flexible.snapshotter.model

import play.api.libs.json.JsValue

case class Snapshot(id: String, reason: String, json: JsValue)