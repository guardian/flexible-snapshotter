package com.gu.flexible.snapshotter.config

import com.amazonaws.services.lambda.AWSLambda
import com.amazonaws.services.lambda.model.GetFunctionConfigurationRequest
import com.amazonaws.services.lambda.runtime.Context
import com.gu.flexible.snapshotter.Logging
import play.api.libs.json.Json

object Config {
  def apiUrl(stage: String, stack: String): String = {
    val subDomain = stack match {
      case "flexible-secondary" => "apiv2" // we're not running a proxy (or mongo) in secondary, go direct to postgres
      case _ => "flexible-api"
    }

    s"http://$subDomain.${stage.toLowerCase}.${stack.toLowerCase}.gudiscovery.:8080"
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
    "contentChangeDetails.published",
    "contentChangeDetails.lastModified",
    "contentChangeDetails.revision"
  ).map(_.split("\\.").toList)
}

object LambdaConfig extends Logging {
  def getDescriptionJson(context: Context)(implicit lambdaClient: AWSLambda) = {
    val functionMetadata = lambdaClient.getFunctionConfiguration(
      new GetFunctionConfigurationRequest()
        .withFunctionName(context.getFunctionName)
    )
    Json.parse(functionMetadata.getDescription)
  }
}

trait CommonConfig {
  def cloudWatchNameSpace: String = "SnapshotterLambdas"
  def cloudWatchDimensions: Seq[(String,String)] = Seq("Stage" -> stage)
  def stage: String
  def stack: String

  def apiUrl: String = Config.apiUrl(stage, stack)

  def contentUri = s"$apiUrl/content"
  def contentRawUri = s"$apiUrl/contentRaw"

  def fieldsToExtract = Config.defaultFieldsToExtract
}
