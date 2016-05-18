package com.gu.flexible.snapshotter.logic

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.amazonaws.services.kinesis.model.{PutRecordRequest, PutRecordResult}
import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import com.gu.flexible.snapshotter.Logging
import com.gu.flexible.snapshotter.model.{Attempt, AttemptError, AttemptErrors}
import play.api.libs.json._

import scala.collection.JavaConverters._

object KinesisLogic extends Logging {
  def serialiseToByteBuffer[T](o: T)(implicit writes:Writes[T]): ByteBuffer = {
    val json = Json.toJson(o)
    val bytes = json.toString.getBytes(StandardCharsets.UTF_8)
    ByteBuffer.wrap(bytes)
  }

  def deserialiseFromByteBuffer[T](buffer: ByteBuffer)(implicit reads:Reads[T]): Attempt[T] = {
    val json = Json.parse(buffer.array())
    Json.fromJson[T](json).fold(
      { failures =>
        val attemptErrors = failures.map { case (path, errors) =>
          AttemptError(errors.map(_.messages.mkString(",")).mkString(";"), context = Some(path.toString))
        }
        attemptErrors.foreach(error => log.error(error.logString, error.throwable.orNull))
        Attempt.Left(AttemptErrors(attemptErrors))
      },
      Attempt.Right
    )
  }

  def sendToKinesis(streamName: String, buffer: ByteBuffer)(implicit client:AmazonKinesisClient): PutRecordResult = {
    log.info(s"Sending to Kinesis: ${new String(buffer.array(), StandardCharsets.UTF_8)}")
    val result = client.putRecord(
      new PutRecordRequest().
        withStreamName(streamName).
        withData(buffer).
        withPartitionKey(new String(buffer.array().take(256)))
    )
    log.info(s"Sent to Kinesis: Seq#:${result.getSequenceNumber}")
    result
  }

  def buffersFromLambdaEvent(event: KinesisEvent): Map[String, ByteBuffer] = event.getRecords.asScala.map{ record =>
    record.getKinesis.getSequenceNumber -> record.getKinesis.getData
  }.toMap
}

