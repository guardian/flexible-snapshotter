package com.gu.flexible.snapshotter.mainclasses

import com.gu.flexible.snapshotter.SchedulingLambda
import com.gu.flexible.snapshotter.config.SchedulerConfig
import com.gu.flexible.snapshotter.logic.FutureUtils

object SchedulingLambdaRunner extends App {
  val snsTopicArn:String = ???
  val stack:String = ???

  val sl = new SchedulingLambda()

  implicit val config = new SchedulerConfig(
    snsTopicArn = snsTopicArn,
    stack = stack,
    stage = "DEV"
  )

  val result = sl.schedule(config)
  val fin = SchedulingLambda.logResult(result)(sl.cloudWatchClient, config)
  FutureUtils.await(fin)
  sl.shutdown()
}
