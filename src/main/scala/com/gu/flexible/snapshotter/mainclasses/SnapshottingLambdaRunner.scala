package com.gu.flexible.snapshotter.mainclasses

import java.nio.ByteBuffer

import com.amazonaws.regions.{Region, Regions}
import com.gu.flexible.snapshotter.SnapshottingLambda
import com.gu.flexible.snapshotter.config.{Config, SnapshotterConfig}
import com.gu.flexible.snapshotter.logic.KinesisLogic
import com.gu.flexible.snapshotter.model.{BatchSnapshotRequest, SnapshotMetadata}

object SnapshottingLambdaRunner extends App {
  val bucket:String = ???

  val sl = new SnapshottingLambda()
  val input:Seq[ByteBuffer] = Seq(KinesisLogic.serialiseToByteBuffer(BatchSnapshotRequest(Seq("572dda3af7d0f2a7e4bbfb73"), SnapshotMetadata("Testing"))))
  sl.snapshot(input,
    new SnapshotterConfig(bucket, Config.apiUrl("DEV"), region = Region.getRegion(Regions.EU_WEST_1)),
    new FakeContext()
  )
  sl.shutdown()
}