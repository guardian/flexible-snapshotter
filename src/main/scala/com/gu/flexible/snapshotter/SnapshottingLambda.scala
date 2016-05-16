package com.gu.flexible.snapshotter

import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import com.amazonaws.services.lambda.runtime.Context
import com.gu.flexible.snapshotter.config.{Config, SnapshotterConfig}
import com.gu.flexible.snapshotter.logic.{ApiLogic, KinesisLogic, S3Logic}
import com.gu.flexible.snapshotter.model.{Attempt, BatchSnapshotRequest, Snapshot}
import com.gu.flexible.snapshotter.resources.{AWSClientFactory, WSClientFactory}
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global

class SnapshottingLambda {
  import KinesisLogic._
  import ApiLogic._
  import S3Logic._

  implicit val wsClient = WSClientFactory.createClient
  implicit val s3Client = AWSClientFactory.createS3Client
  implicit val lambdaClient = AWSClientFactory.createLambdaClient

  def snapshot(input: KinesisEvent, context: Context) = {
    implicit val config = SnapshotterConfig.resolve(Config.guessStage(context), context)

    val buffers = buffersFromLambdaEvent(input)
    val snapshotRequestAttempts = buffers.map(deserialiseFromByteBuffer[BatchSnapshotRequest])
    for {
      batchSnapshotRequests <- Attempt.successfulAttempts(snapshotRequestAttempts)
      snapshotRequests = batchSnapshotRequests.flatMap(_.asSnapshotRequests)
      apiResults = snapshotRequests.map(contentForSnapshot)
      successfulApiResults <- Attempt.successfulAttempts(apiResults)
    } {
      successfulApiResults.foreach{ case Snapshot(id, reason, json) =>
        uploadToS3Bucket(id, new DateTime(), json)
      }
    }
  }
}