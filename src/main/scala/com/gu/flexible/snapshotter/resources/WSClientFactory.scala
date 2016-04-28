package com.gu.flexible.snapshotter.resources

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.ahc.AhcWSClient

object WSClientFactory {
  def createClient = {
    implicit val system = ActorSystem("ws")
    implicit val materializer = ActorMaterializer()
    AhcWSClient()
  }
}
