package com.gu.flexible.snapshotter

import org.slf4j.LoggerFactory

trait Logging {
  lazy val log = LoggerFactory.getLogger(this.getClass)
}
