package com.gu.flexible.snapshotter

import java.nio.ByteBuffer

import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import com.amazonaws.services.s3.model.PutObjectResult
import com.gu.flexible.snapshotter.config.{Config, SnapshotterConfig}
import com.gu.flexible.snapshotter.logic.{ApiLogic, FutureUtils, KinesisLogic, S3Logic}
import com.gu.flexible.snapshotter.model.{Attempt, BatchSnapshotRequest}
import com.gu.flexible.snapshotter.resources.{AWSClientFactory, WSClientFactory}
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

class SnapshottingLambda extends Logging {
  import ApiLogic._
  import KinesisLogic._
  import S3Logic._

  implicit val region: Regions = AWSClientFactory.getRegion
  implicit val wsClient = WSClientFactory.createClient
  implicit val s3Client = AWSClientFactory.createS3Client
  implicit val lambdaClient = AWSClientFactory.createLambdaClient

  def run(input: KinesisEvent, context: Context): Unit = {
    val config = SnapshotterConfig.resolve(Config.guessStage(context), context)
    val buffers = buffersFromLambdaEvent(input)
    log.info(s"Processing sequence numbers: ${buffers.keys.toSeq}")

    val results = snapshot(buffers.values.toSeq, config, context)
    val fin = SnapshottingLambda.logResults(results)

    FutureUtils.await(fin)
  }

  def snapshot(buffers: Seq[ByteBuffer], config: SnapshotterConfig, context: Context): Attempt[Seq[Attempt[PutObjectResult]]] = {
    implicit val implicitConfig = config
    val snapshotRequestAttempts = buffers.map(deserialiseFromByteBuffer[BatchSnapshotRequest])
    for {
      batchSnapshotRequests <- Attempt.successfulAttempts(snapshotRequestAttempts)
      snapshotRequests = batchSnapshotRequests.flatMap(_.asSnapshotRequests)
      apiResults = snapshotRequests.map(contentForSnapshot)
      successfulApiResults <- Attempt.successfulAttempts(apiResults)
    } yield {
      successfulApiResults.map{ snapshot =>
        uploadToS3Bucket(snapshot.id, new DateTime(), snapshot.snapshotDocument)
      }
    }
  }

  def shutdown(): Unit = {
    lambdaClient.shutdown()
    s3Client.shutdown()
    wsClient.close()
  }
}

object SnapshottingLambda extends Logging {
  def logResults(results: Attempt[Seq[Attempt[PutObjectResult]]]): Future[Unit] = {
    results.fold(
      { failed => Future.successful(failed.errors.foreach(_.logTo(log))) }, { succeeded =>
        Future.sequence(succeeded.map(_.asFuture)).map { attempts =>
          attempts.foreach {
            case Left(failures) =>
              failures.errors.foreach(_.logTo(log))
            case Right(result) =>
              log.info(s"SUCCESS: $result")
          }
        }
      }
    ).flatMap(identity)
  }
}