package com.gu.flexible.snapshotter.config

import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model.GetFunctionConfigurationRequest
import com.amazonaws.services.lambda.runtime.Context
import com.gu.flexible.snapshotter.Logging
import play.api.libs.json.Json

object Config {
  def apiUrl(stage: String, stack: String): String = (stage, stack) match {
    case ("PROD", "flexible")  => "http://internal-Flexible-ApiLoadB-1QAHGRQLH03UW-649659201.eu-west-1.elb.amazonaws.com:8080"
    case ("PROD", "flexible-secondary") => "http://internal-Flexible-ApiLoadB-1NYF8NMK5QNMP-231637936.eu-west-1.elb.amazonaws.com:8080"
    case ("CODE", "flexible-secondary") => "http://internal-Flexible-ApiLoadB-1LO2GC5WZ5S09-11639498.eu-west-1.elb.amazonaws.com:8080"
    case _ => "http://internal-Flexible-ApiLoadB-15RTA1C81ZYGU-432053948.eu-west-1.elb.amazonaws.com:8080"
  }

  def guessStage(context: Context): String =
    context.getFunctionName.split(Array('-','_')).toList.filter(_.length > 0).lastOption.getOrElse {
      throw new IllegalArgumentException(s"Couldn't guess stage from function name ${context.getFunctionName}")
    }

  val defaultFieldsToExtract = List(
    "preview.fields.headline",
    "preview.settings.commentable",
    "type",
    "preview.settings.liveBloggingNow",
    "preview.settings.legallySensitive",
    "published",
    "scheduledLaunchDate",
    "preview.settings.embargoedUntil",
    "contentChangeDetails.published"
  ).map(_.split("\\.").toList)
}

object LambdaConfig extends Logging {
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
  def cloudWatchDimensions: Seq[(String,String)] = Seq("Stage" -> stage)
  def stage: String
  def stack: String

  def apiUrl: String = Config.apiUrl(stage, stack)

  def contentUri = s"$apiUrl/content"
  def contentRawUri = s"$apiUrl/contentRaw"

  def fieldsToExtract = Config.defaultFieldsToExtract
}