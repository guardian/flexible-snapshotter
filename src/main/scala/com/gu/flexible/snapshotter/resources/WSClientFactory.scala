package com.gu.flexible.snapshotter.resources

import java.io.IOException

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.{StandaloneWSClient, StandaloneWSRequest}
import play.api.libs.ws.ahc.StandaloneAhcWSClient

object WSClientFactory {
  def createClient = {
    implicit val system = ActorSystem("ws")
    implicit val materializer = ActorMaterializer()
    val client = StandaloneAhcWSClient()

    new StandaloneWSClient {
      def underlying[T]: T = client.underlying
      def url(url: String): StandaloneWSRequest = client.url(url)

      @scala.throws[IOException]
      def close(): Unit = {
        client.close()
        system.terminate()
      }
    }
  }
}
