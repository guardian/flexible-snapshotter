package com.gu.flexible.snapshotter.logic

import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.{PublishRequest, PublishResult}
import com.gu.flexible.snapshotter.Logging
import com.gu.flexible.snapshotter.model.{Attempt, AttemptError, AttemptErrors}
import play.api.libs.json.{Json, Reads, Writes}

import scala.collection.JavaConverters._

case class SNSMessage(id: String, content: String)

object SNSLogic extends Logging {
  def serialise[T](o: T)(implicit writes:Writes[T]): String = Json.stringify(Json.toJson(o))

  def deserialise[T](jsonString: String)(implicit reads:Reads[T]): Attempt[T] = {
    val json = Json.parse(jsonString)
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

  def fromLambdaEvent(event: SNSEvent): Seq[SNSMessage] = {
    event.getRecords.asScala.map{ record =>
      SNSMessage(record.getSNS.getMessageId, record.getSNS.getMessage)
    }.toList
  }

  def publish(topicArn: String, message: String)(implicit client: AmazonSNS): PublishResult = {
    log.info(s"Sending to SNS: $message")
    val result = client.publish(
      new PublishRequest().
        withTopicArn(topicArn).
        withMessage(message)
    )
    log.info(s"Sent to SNS with message ID: ${result.getMessageId}")
    result
  }
}
