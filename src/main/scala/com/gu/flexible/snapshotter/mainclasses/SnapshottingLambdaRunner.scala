package com.gu.flexible.snapshotter.mainclasses

import java.nio.ByteBuffer

import com.amazonaws.regions.{Region, Regions}
import com.gu.flexible.snapshotter.SnapshottingLambda
import com.gu.flexible.snapshotter.config.{Config, SnapshotterConfig}
import com.gu.flexible.snapshotter.logic.{FutureUtils, KinesisLogic}
import com.gu.flexible.snapshotter.model.{SnapshotMetadata, SnapshotRequest}

object SnapshottingLambdaRunner extends App {
  val bucket:String = ???

  val sl = new SnapshottingLambda()
  val input:Seq[ByteBuffer] = Seq(KinesisLogic.serialiseToByteBuffer(SnapshotRequest("572dda3af7d0f2a7e4bbfb73", SnapshotMetadata("Testing"))))
  val config = new SnapshotterConfig(
    bucket = bucket,
    stage = "DEV",
    region = Region.getRegion(Regions.EU_WEST_1)
  )
  val results = sl.snapshot(input,
    config,
    new FakeContext()
  )
  val fin = SnapshottingLambda.logResults(results)(sl.cloudWatchClient, config)
  FutureUtils.await(fin)
  sl.shutdown()
}