package com.gu.flexible.snapshotter.resources

import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudwatch.{AmazonCloudWatch, AmazonCloudWatchClientBuilder}
import com.amazonaws.services.lambda.{AWSLambda, AWSLambdaClientBuilder}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.services.sns.{AmazonSNS, AmazonSNSClientBuilder}

object AWSClientFactory {
  def getRegion = Option(System.getenv("AWS_DEFAULT_REGION")).map(Regions.fromName).get

  def createSNSClient(implicit region:Regions): AmazonSNS =
    AmazonSNSClientBuilder.standard().withRegion(region.getName).build()
  def createS3Client(implicit region:Regions): AmazonS3 =
    AmazonS3ClientBuilder.standard().withRegion(region.getName).build()
  def createLambdaClient(implicit region:Regions): AWSLambda =
    AWSLambdaClientBuilder.standard().withRegion(region.getName).build()
  def createCloudwatchClient(implicit region:Regions): AmazonCloudWatch =
    AmazonCloudWatchClientBuilder.standard().withRegion(region.getName).build()
}
