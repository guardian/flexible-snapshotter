package com.gu.flexible.snapshotter

import java.nio.ByteBuffer

import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import com.amazonaws.services.lambda.runtime.Context
import com.gu.flexible.snapshotter.config.{Config, SnapshotterConfig}
import com.gu.flexible.snapshotter.logic.{ApiLogic, KinesisLogic, S3Logic}
import com.gu.flexible.snapshotter.model.{Attempt, BatchSnapshotRequest}
import com.gu.flexible.snapshotter.resources.{AWSClientFactory, WSClientFactory}
import org.joda.time.DateTime

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

class SnapshottingLambda extends Logging {
  import KinesisLogic._
  import ApiLogic._
  import S3Logic._

  implicit val region: Regions = AWSClientFactory.getRegion
  implicit val wsClient = WSClientFactory.createClient
  implicit val s3Client = AWSClientFactory.createS3Client
  implicit val lambdaClient = AWSClientFactory.createLambdaClient

  def run(input: KinesisEvent, context: Context): Unit = {
    val config = SnapshotterConfig.resolve(Config.guessStage(context), context)
    val buffers = buffersFromLambdaEvent(input)
    log.info(s"Processing sequence numbers: ${buffers.keys.toSeq}")
    snapshot(buffers.values.toSeq,config,context)
  }

  def snapshot(buffers: Seq[ByteBuffer], config: SnapshotterConfig, context: Context): Unit = {
    implicit val implicitConfig = config
    val snapshotRequestAttempts = buffers.map(deserialiseFromByteBuffer[BatchSnapshotRequest])
    val results = for {
      batchSnapshotRequests <- Attempt.successfulAttempts(snapshotRequestAttempts)
      snapshotRequests = batchSnapshotRequests.flatMap(_.asSnapshotRequests)
      apiResults = snapshotRequests.map(contentForSnapshot)
      successfulApiResults <- Attempt.successfulAttempts(apiResults)
    } yield {
      successfulApiResults.map{ snapshot =>
        uploadToS3Bucket(snapshot.id, new DateTime(), snapshot.snapshotDocument)
      }
    }

    val fin = results.flatMap(uploads => Attempt.sequence(uploads)).fold(
      { errors =>
        errors.errors.foreach(_.logTo(log))
      },{kinesisResult =>
        log.info(s"SUCCESS: $kinesisResult")
      })
    Await.ready(fin, 120 seconds)
  }

  def shutdown(): Unit = {
    lambdaClient.shutdown()
    s3Client.shutdown()
    wsClient.close()
  }
}