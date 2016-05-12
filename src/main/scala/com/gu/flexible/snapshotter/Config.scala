package com.gu.flexible.snapshotter

import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model.GetFunctionConfigurationRequest
import com.amazonaws.services.lambda.runtime.Context
import play.api.libs.json.Json

object Config {
  def apiUrl(stage: String): String = stage match {
    case "PROD" => "http://internal-Flexible-ApiLoadB-1QAHGRQLH03UW-649659201.eu-west-1.elb.amazonaws.com:8080"
    case _ => "http://internal-Flexible-ApiLoadB-15RTA1C81ZYGU-432053948.eu-west-1.elb.amazonaws.com:8080"
  }

  def guessStage(context: Context): String =
    context.getFunctionName.split(Array('-','_')).toList.filter(_.length > 0).lastOption.getOrElse {
      throw new IllegalArgumentException(s"Couldn't guess stage from function name ${context.getFunctionName}")
    }
}

object LambdaConfig {
  def getDescriptionJson(context: Context)(implicit lambdaClient:AWSLambdaClient) = {
    val functionMetadata = lambdaClient.getFunctionConfiguration(
      new GetFunctionConfigurationRequest()
        .withFunctionName(context.getFunctionName)
    )
    Json.parse(functionMetadata.getDescription)
  }
}

object LambdaSchedulerConfig {
  implicit val configReads = Json.reads[LambdaSchedulerConfig]
}
case class LambdaSchedulerConfig(kinesisStream: String)

object LambdaSnapshotterConfig {
  implicit val configReads = Json.reads[LambdaSnapshotterConfig]
}
case class LambdaSnapshotterConfig(bucket: String, kmsKey: Option[String])

sealed trait CommonConfig {
  def apiUrl: String
  def region: Region

  def contentUri = s"$apiUrl/content"
  def contentRawUri = s"$apiUrl/contentRaw"
}

case class SnapshotterConfig(
  bucket: String,
  apiUrl: String,
  kmsKey: Option[String] = None,
  region: Region = Regions.getCurrentRegion) extends CommonConfig

object SnapshotterConfig {
  def resolve(stage: String, context: Context)(implicit lambdaClient: AWSLambdaClient): SnapshotterConfig = {
    val lambdaJson = LambdaConfig.getDescriptionJson(context)
    val lambdaConfig = lambdaJson.as[LambdaSnapshotterConfig]
    SnapshotterConfig(lambdaConfig.bucket, Config.apiUrl(stage), lambdaConfig.kmsKey)
  }
}

case class SchedulerConfig(
  kinesisStream: String,
  apiUrl: String,
  region: Region = Regions.getCurrentRegion) extends CommonConfig

object SchedulerConfig {
  def resolve(stage: String, context: Context)(implicit lambdaClient: AWSLambdaClient): SchedulerConfig = {
    val lambdaJson = LambdaConfig.getDescriptionJson(context)
    val lambdaConfig = lambdaJson.as[LambdaSchedulerConfig]
    SchedulerConfig(lambdaConfig.kinesisStream, Config.apiUrl(stage))
  }
}