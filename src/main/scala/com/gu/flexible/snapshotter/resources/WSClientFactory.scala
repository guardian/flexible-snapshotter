package com.gu.flexible.snapshotter.resources

import java.io.IOException

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.libs.ws.ahc.AhcWSClient

object WSClientFactory {
  def createClient = {
    implicit val system = ActorSystem("ws")
    implicit val materializer = ActorMaterializer()
    val client = AhcWSClient()

    new WSClient {
      def underlying[T]: T = client.underlying
      def url(url: String): WSRequest = client.url(url)

      @scala.throws[IOException]
      def close(): Unit = {
        client.close()
        system.terminate()
      }
    }
  }
}
