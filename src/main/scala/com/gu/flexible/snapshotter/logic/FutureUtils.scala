package com.gu.flexible.snapshotter.logic

import java.util.concurrent.TimeoutException

import com.gu.flexible.snapshotter.Logging

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

object FutureUtils extends Logging {
  def await[T](future: Future[T]) = {
    try {
      Await.ready(future, 120 seconds)
    } catch {
      case e: TimeoutException => log.error("Timed out whilst waiting for futures to complete", e)
      case e: InterruptedException => log.error("Interrupted whilst waiting for future to complete", e)
    }
  }
}
