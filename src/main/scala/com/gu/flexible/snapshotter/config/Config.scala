package com.gu.flexible.snapshotter.config

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
  def getDescriptionJson(context: Context)(implicit lambdaClient: AWSLambdaClient) = {
    val functionMetadata = lambdaClient.getFunctionConfiguration(
      new GetFunctionConfigurationRequest()
        .withFunctionName(context.getFunctionName)
    )
    Json.parse(functionMetadata.getDescription)
  }
}

trait CommonConfig {
  def region: Region
  def cloudWatchNameSpace: String = "SnapshotterLambdas"
  def cloudWatchDimensions: Seq[(String,String)] = Seq("Stage" -> stage, "Lambda" -> app)
  def stage: String
  def app: String

  def apiUrl: String = Config.apiUrl(stage)

  def contentUri = s"$apiUrl/content"
  def contentRawUri = s"$apiUrl/contentRaw"
}