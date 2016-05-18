package com.gu.flexible.snapshotter.mainclasses

import com.amazonaws.regions.{Region, Regions}
import com.gu.flexible.snapshotter.SchedulingLambda
import com.gu.flexible.snapshotter.config.{Config, SchedulerConfig}

object SchedulingLambdaRunner extends App {
  val kinesisStream:String = ???

  val sl = new SchedulingLambda()
  sl.schedule(
    new SchedulerConfig(kinesisStream, Config.apiUrl("DEV"), Region.getRegion(Regions.EU_WEST_1)),
    new FakeContext()
  )
  sl.shutdown()
}