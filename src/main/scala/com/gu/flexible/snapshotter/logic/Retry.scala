package com.gu.flexible.snapshotter.logic

import com.gu.flexible.snapshotter.model.Attempt

import scala.concurrent.ExecutionContext

object Retry {
  def apply[T](f: => Attempt[T], retries: Int)(implicit ec: ExecutionContext): Attempt[T] = {
    if (retries > 0)
      f.recoverWith(_ => apply(f, retries - 1))
    else
      f
  }
}
