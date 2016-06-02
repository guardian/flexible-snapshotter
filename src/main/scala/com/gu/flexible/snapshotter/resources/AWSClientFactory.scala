package com.gu.flexible.snapshotter.resources

import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.sns.AmazonSNSClient

object AWSClientFactory {
  def getRegion = Option(System.getenv("AWS_DEFAULT_REGION")).map(Regions.fromName).get

  def createSNSClient(implicit region:Regions): AmazonSNSClient =
    new AmazonSNSClient().withRegion(region)
  def createS3Client(implicit region:Regions): AmazonS3Client =
    new AmazonS3Client().withRegion(region)
  def createLambdaClient(implicit region:Regions): AWSLambdaClient =
    new AWSLambdaClient().withRegion(region)
  def createCloudwatchClient(implicit region:Regions): AmazonCloudWatchClient =
    new AmazonCloudWatchClient().withRegion(region)
}
