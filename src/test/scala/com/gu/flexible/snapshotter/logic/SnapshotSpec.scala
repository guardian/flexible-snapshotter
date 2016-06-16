package com.gu.flexible.snapshotter.logic

import com.gu.flexible.snapshotter.model.Snapshot
import org.scalatest.{FlatSpec, ShouldMatchers}
import play.api.libs.json.{JsBoolean, JsObject, JsString, Json}

class SnapshotSpec extends FlatSpec with ShouldMatchers {
  val testJson = Json.obj(
    "test1" -> Json.obj(
      "field1a" -> JsString("bubbles"),
      "field1b" -> JsString("bubbling")
    ),
    "test2" -> Json.obj(
      "field2a" -> JsBoolean(true),
      "field2b" -> JsBoolean(false)
    )
  )

  "soloField" should "extract a single field from a tree" in {
    val result = Snapshot.soloField(testJson, List("test1", "field1a"))
    result should be(Some(Json.obj("test1" -> Json.obj("field1a" -> JsString("bubbles")))))
  }

  it should "return none if part of the path doesn't exist" in {
    val result = Snapshot.soloField(testJson, List("test3", "field1a"))
    result should be(None)
  }

  it should "return a subtree if the path ends with an object" in {
    val result = Snapshot.soloField(testJson, List("test1"))
    result should be(Some(Json.obj(
      "test1" -> Json.obj(
        "field1a" -> JsString("bubbles"),
        "field1b" -> JsString("bubbling")
      )
    )))
  }
}
