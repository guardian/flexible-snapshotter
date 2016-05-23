package com.gu.flexible.snapshotter.mainclasses

import com.amazonaws.regions.{Region, Regions}
import com.gu.flexible.snapshotter.SchedulingLambda
import com.gu.flexible.snapshotter.config.{Config, SchedulerConfig}
import com.gu.flexible.snapshotter.logic.FutureUtils

object SchedulingLambdaRunner extends App {
  val kinesisStream:String = ???

  val sl = new SchedulingLambda()
  val result = sl.schedule(
    new SchedulerConfig(
      kinesisStream,
      Config.apiUrl("DEV"),
      region = Region.getRegion(Regions.EU_WEST_1)
    ),
    new FakeContext()
  )
  val fin = SchedulingLambda.logResult(result)
  FutureUtils.await(fin)
  sl.shutdown()
}