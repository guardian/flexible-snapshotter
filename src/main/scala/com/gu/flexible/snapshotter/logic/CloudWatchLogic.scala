package com.gu.flexible.snapshotter.logic

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient
import com.amazonaws.services.cloudwatch.model.{Dimension, MetricDatum, PutMetricDataRequest, StandardUnit}
import com.gu.flexible.snapshotter.Logging
import com.gu.flexible.snapshotter.config.CommonConfig

import scala.util.control.NonFatal

object MetricValue {
  val None = StandardUnit.None.toString
  val Count = StandardUnit.Count.toString
}
case class MetricValue(value: Double, unit: String = MetricValue.None)

object MetricName {
  // these keys are referenced in the cloudformation - don't change them!
  val contentSnapshotError = "contentSnapshotError"
  val contentSnapshotSuccess = "contentSnapshotSuccess"
  val scheduledContentIdsError = "scheduledContentIdsError"
  val scheduledContentIdsSuccess = "scheduledContentIdsSuccess"
}

object CloudWatchLogic extends Logging {
  def awsDimensions(dimensions: (String, String)*) = {
    dimensions.map{ case (name, value) => new Dimension().withName(name).withValue(value) }
  }

  def putMetricData(metrics: (String, MetricValue)*)(implicit cloudWatchClient: AmazonCloudWatchClient, config: CommonConfig) = {
    val dimensions = awsDimensions(config.cloudWatchDimensions:_*)
    val metricData = metrics.map { case (name, value) =>
      new MetricDatum().withMetricName(name).withUnit(value.unit).withValue(value.value).withDimensions(dimensions:_*)
    }
    try {
      cloudWatchClient.putMetricData(new PutMetricDataRequest().withNamespace(config.cloudWatchNameSpace).withMetricData(metricData:_*))
    } catch {
      case NonFatal(e) => log.warn(s"Could't post metrics $metrics", e)
    }
  }
}
