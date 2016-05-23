package com.gu.flexible.snapshotter.config

import ch.qos.logback.classic.{Logger, LoggerContext}
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.joran.spi.JoranException
import ch.qos.logback.core.util.StatusPrinter
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.{Region, Regions}
import com.gu.flexible.snapshotter.Logging
import com.gu.logback.appender.kinesis.KinesisAppender
import net.logstash.logback.layout.LogstashLayout
import org.slf4j.LoggerFactory

case class KinesisAppenderConfig(
  stream: String,
  credentialsProvider: AWSCredentialsProvider,
  region: Regions,
  bufferSize: Int = 1000
)

object LogStash extends Logging {
  var initialised = false

  def enableLogstashKinesisHandler(config: KinesisAppenderConfig, customFields: (String, String)*) =
    init(config, customFields.toMap)

  def makeCustomFields(customFields: Map[String,String]): String = {
    "{" + (for((k, v) <- customFields) yield(s""""${k}":"${v}"""")).mkString(",") + "}"
  }

  def makeLayout(customFields: String) = {
    val l = new LogstashLayout()
    l.setCustomFields(customFields)
    l
  }

  def makeKinesisAppender(layout: LogstashLayout, context: LoggerContext, appenderConfig: KinesisAppenderConfig) = {
    val a = new KinesisAppender[ILoggingEvent]()
    a.setStreamName(appenderConfig.stream)
    a.setRegion(appenderConfig.region.getName)
    a.setCredentialsProvider(appenderConfig.credentialsProvider)
    a.setBufferSize(appenderConfig.bufferSize)

    a.setContext(context)
    a.setLayout(layout)

    layout.start()
    a.start()
    a
  }

  def init(config: KinesisAppenderConfig, customFields: Map[String, String]) {
    if (!initialised) {
      val context = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
      try {
        val layout = makeLayout(makeCustomFields(customFields))
        val appender = makeKinesisAppender(layout, context, config)
        val rootLogger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[Logger]
        rootLogger.addAppender(appender)
        initialised = true
      } catch {
        case e: JoranException => // ignore, errors will be printed below
      }

      StatusPrinter.printInCaseOfErrorsOrWarnings(context)
      log.info("Logstash appender now initialised")
    } else {
      log.info("Logstash already initialised")
    }
  }
}
