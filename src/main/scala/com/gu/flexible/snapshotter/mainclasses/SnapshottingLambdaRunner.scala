package com.gu.flexible.snapshotter.mainclasses

import com.gu.flexible.snapshotter.SnapshottingLambda
import com.gu.flexible.snapshotter.config.SnapshotterConfig
import com.gu.flexible.snapshotter.logic.{FutureUtils, SNSLogic}
import com.gu.flexible.snapshotter.model.{SnapshotMetadata, SnapshotRequest}

object SnapshottingLambdaRunner extends App {
  val bucket:String = "flexible-snapshotter-code"
  val stack:String = ???


  val sl = new SnapshottingLambda()
  val input:Seq[String] = Seq(SNSLogic.serialise(SnapshotRequest("57431375f7d04d8e107ab19e", SnapshotMetadata("Testing"))))
  val config = new SnapshotterConfig(
    bucket = bucket,
    stage = "DEV",
    stack = stack
  )
  val results = sl.snapshot(input,
    config
  )
  val fin = SnapshottingLambda.logResults(results)(sl.cloudWatchClient, config)
  FutureUtils.await(fin)
  sl.shutdown()
}
