package com.gu.flexible.snapshotter

import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.lambda.runtime.Context

object Config {
  def apply() = new ProdConfig()

  def guessStage(context: Context): Option[String] =
    context.getFunctionName.split(Array('-','_')).toList.filter(_.length > 0).lastOption
}

sealed trait Config {
  def apiUrl: String
  def bucket: String
  def kmsKey: Option[String]
  def kinesisStream: String

  def region: Region = Regions.getCurrentRegion

  def contentUri = s"$apiUrl/content"
  def contentRawUri = s"$apiUrl/contentRaw"
}

class DevConfig extends Config {
  override val apiUrl: String = "http://internal-Flexible-ApiLoadB-15RTA1C81ZYGU-432053948.eu-west-1.elb.amazonaws.com:8080"
  override val bucket: String = "flexible-snapshots-dev"
  override val kmsKey: Option[String] = ???
  override val kinesisStream: String = ???

  override val region = Region.getRegion(Regions.EU_WEST_1)
}

class CodeConfig extends Config {
  override val bucket: String = "flexible-snapshots-code"
  override val apiUrl: String = "http://internal-Flexible-ApiLoadB-15RTA1C81ZYGU-432053948.eu-west-1.elb.amazonaws.com:8080"
  override val kmsKey: Option[String] = ???
  override val kinesisStream: String = ???
}

class ProdConfig extends Config {
  override val apiUrl: String = "http://internal-Flexible-ApiLoadB-1QAHGRQLH03UW-649659201.eu-west-1.elb.amazonaws.com:8080"
  override val bucket: String = "flexible-snapshots-prod"
  override val kmsKey: Option[String] = ???
  override val kinesisStream: String = ???
}
