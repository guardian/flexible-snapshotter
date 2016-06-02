package com.gu.flexible.snapshotter.mainclasses

import com.amazonaws.regions.{Region, Regions}
import com.gu.flexible.snapshotter.SchedulingLambda
import com.gu.flexible.snapshotter.config.{Config, SchedulerConfig}
import com.gu.flexible.snapshotter.logic.FutureUtils

object SchedulingLambdaRunner extends App {
  val kinesisStream:String = ???

  val sl = new SchedulingLambda()

  implicit val config = new SchedulerConfig(
    kinesisStream = kinesisStream,
    stage = "DEV",
    region = Region.getRegion(Regions.EU_WEST_1)
  )

  val result = sl.schedule(config, new FakeContext())
  val fin = SchedulingLambda.logResult(result)(sl.cloudWatchClient, config)
  FutureUtils.await(fin)
  sl.shutdown()
}