package com.gu.flexible.snapshotter.logic

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.{AmazonClientException, AmazonServiceException}
import com.amazonaws.services.s3.model.{ObjectMetadata, PutObjectRequest, PutObjectResult, SSEAwsKeyManagementParams}
import com.gu.flexible.snapshotter.Logging
import com.gu.flexible.snapshotter.config.SnapshotterConfig
import com.gu.flexible.snapshotter.model.{Attempt, AttemptError, AttemptErrors}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.{JsValue, Json}

object S3Logic extends Logging {
  def uploadToS3Bucket(id: String, date: DateTime, content: JsValue)(implicit s3Client: AmazonS3Client, config: SnapshotterConfig): Attempt[PutObjectResult] = {
    val jsonBytes = Json.prettyPrint(content).getBytes(StandardCharsets.UTF_8)

    val key = makeKey(id, date, extension = "json")

    val objectMetadata = new ObjectMetadata()
    objectMetadata.setContentLength(jsonBytes.length)
    objectMetadata.setContentType("application/json; charset=utf-8")

    val keyManagementParams =
      config.kmsKey.map(new SSEAwsKeyManagementParams(_)).getOrElse(new SSEAwsKeyManagementParams())

    val putObjectRequest = new PutObjectRequest(
      config.bucket,
      key,
      new ByteArrayInputStream(jsonBytes),
      objectMetadata
    ).withSSEAwsKeyManagementParams(keyManagementParams)

    try {
      log.info(s"Saving content with id $id to bucket ${config.bucket} with key $key")
      Attempt.Right(s3Client.putObject(putObjectRequest))
    } catch {
      case (e @ (_: AmazonClientException | _ : AmazonServiceException)) =>
        Attempt.Left(AttemptError(s"Failed to upload document $id to bucket ${config.bucket} with key $key", throwable = Some(e)))
    }
  }

  private[snapshotter] val isoDateTimeFormatter = ISODateTimeFormat.dateTime()
  private[snapshotter] def makeKey(id: String, date: DateTime, extension: String) = s"$id/${isoDateTimeFormatter.print(date)}.$extension"
}
