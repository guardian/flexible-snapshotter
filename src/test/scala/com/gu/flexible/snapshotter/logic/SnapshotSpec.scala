package com.gu.flexible.snapshotter.logic

import com.gu.flexible.snapshotter.model.Snapshot
import org.scalatest.{FlatSpec, ShouldMatchers}
import play.api.libs.json.{JsBoolean, JsString, Json}

class SnapshotSpec extends FlatSpec with ShouldMatchers {
  "extractField" should "return a value from a json tree" in {
    val testJson = Json.obj(
      "test" -> Json.obj(
        "field" -> JsString("bubbles")
      )
    )
    val result = Snapshot.extractField(testJson, "test.field")
    result should be(Some(JsString("bubbles")))
  }

  it should "return none when the field is missing" in {
    val testJson = Json.obj(
      "test" -> Json.obj(
        "field" -> JsString("bubbles")
      )
    )
    val result = Snapshot.extractField(testJson, "test.field2")
    result should be(None)
  }

  it should "return none when the object is missing" in {
    val testJson = Json.obj(
      "test" -> Json.obj(
        "field" -> JsString("bubbles")
      )
    )
    val result = Snapshot.extractField(testJson, "test2.field")
    result should be(None)
  }

  it should "return something when the field is a boolean" in {
    val testJson = Json.obj(
      "test" -> Json.obj(
        "field" -> JsBoolean(true)
      )
    )
    val result = Snapshot.extractField(testJson, "test.field")
    result should be(Some(JsBoolean(true)))
  }
}
