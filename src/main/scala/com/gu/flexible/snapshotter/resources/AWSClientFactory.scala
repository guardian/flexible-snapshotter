package com.gu.flexible.snapshotter.resources

import com.amazonaws.regions.{Region, RegionUtils}
import com.amazonaws.services.cloudwatch.{AmazonCloudWatch, AmazonCloudWatchClientBuilder}
import com.amazonaws.services.lambda.{AWSLambda, AWSLambdaClientBuilder}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.services.sns.{AmazonSNS, AmazonSNSClientBuilder}

object AWSClientFactory {
  def getRegion: Region = RegionUtils.getRegion(System.getenv("AWS_DEFAULT_REGION"))

  def createSNSClient(implicit region:Region): AmazonSNS =
    AmazonSNSClientBuilder.standard().withRegion(region.getName).build()
  def createS3Client(implicit region:Region): AmazonS3 =
    AmazonS3ClientBuilder.standard().withRegion(region.getName).build()
  def createLambdaClient(implicit region:Region): AWSLambda =
    AWSLambdaClientBuilder.standard().withRegion(region.getName).build()
  def createCloudwatchClient(implicit region:Region): AmazonCloudWatch =
    AmazonCloudWatchClientBuilder.standard().withRegion(region.getName).build()
}
