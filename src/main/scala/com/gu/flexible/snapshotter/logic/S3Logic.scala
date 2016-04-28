package com.gu.flexible.snapshotter.logic

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.{AmazonClientException, AmazonServiceException}
import com.amazonaws.services.s3.model.{ObjectMetadata, PutObjectRequest, SSEAwsKeyManagementParams}
import com.gu.flexible.snapshotter.{Config, Logging}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.{JsValue, Json}

object S3Logic extends Logging {
  def uploadToS3Bucket(id: String, date: DateTime, content: JsValue)(implicit s3Client: AmazonS3Client, config: Config): Unit = {
    val jsonBytes = Json.prettyPrint(content).getBytes(StandardCharsets.UTF_8)

    val key = makeKey(id, date)

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
      s3Client.putObject(putObjectRequest)
    } catch {
      case (e @ (_: AmazonClientException | _ : AmazonServiceException)) =>
        log.error(s"Failed to upload document $id to bucket ${config.bucket} with key $key", e)
    }
  }

  private[snapshotter] val isoDateTimeFormatter = ISODateTimeFormat.dateTime()
  private[snapshotter] def makeKey(id: String, date: DateTime) = s"$id/${isoDateTimeFormatter.print(date)}"
}
