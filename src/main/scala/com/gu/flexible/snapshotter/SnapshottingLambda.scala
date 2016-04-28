package com.gu.flexible.snapshotter

import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import com.amazonaws.services.lambda.runtime.Context
import com.gu.flexible.snapshotter.logic.{ApiLogic, KinesisLogic, S3Logic}
import com.gu.flexible.snapshotter.model.{Attempt, ContentIds}
import com.gu.flexible.snapshotter.resources.{AWSClientFactory, WSClientFactory}
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global

class SnapshottingLambda {
  import KinesisLogic._
  import ApiLogic._
  import S3Logic._

  implicit val config = Config()
  implicit val wsClient = WSClientFactory.createClient
  implicit val s3Client = AWSClientFactory.createS3Client

  def snapshot(input: KinesisEvent, context: Context) = {
    val buffers = buffersFromLambdaEvent(input)
    val temps: Seq[Attempt[ContentIds]] = buffers.map(deserialiseFromByteBuffer[ContentIds])
    for {
      contentIdRecords <- Attempt.successfulAttempts(temps)
      ids = contentIdRecords.flatMap(_.contentIds)
      apiResults = ids.map(contentWithId(_))
      successfulApiResults <- Attempt.successfulAttempts(apiResults)
    } {
      successfulApiResults.foreach{ case(id, result) =>
        uploadToS3Bucket(id, new DateTime(), result)
      }
    }
  }
}
