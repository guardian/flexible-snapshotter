package com.gu.flexible.snapshotter.mainclasses

import com.amazonaws.regions.{Region, Regions}
import com.gu.flexible.snapshotter.SnapshottingLambda
import com.gu.flexible.snapshotter.config.SnapshotterConfig
import com.gu.flexible.snapshotter.logic.{FutureUtils, SNSLogic}
import com.gu.flexible.snapshotter.model.{SnapshotMetadata, SnapshotRequest}

object SnapshottingLambdaRunner extends App {
  val bucket:String = ???

  val sl = new SnapshottingLambda()
  val input:Seq[String] = Seq(SNSLogic.serialise(SnapshotRequest("572dda3af7d0f2a7e4bbfb73", SnapshotMetadata("Testing"))))
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