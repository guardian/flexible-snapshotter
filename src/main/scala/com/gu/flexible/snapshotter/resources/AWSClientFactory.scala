package com.gu.flexible.snapshotter.resources

import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.s3.AmazonS3Client

object AWSClientFactory {
  def createKinesisClient(implicit region:Regions): AmazonKinesisClient =
    new AmazonKinesisClient().withRegion(region)
  def createS3Client(implicit region:Regions): AmazonS3Client =
    new AmazonS3Client().withRegion(region)
  def createLambdaClient(implicit region:Regions): AWSLambdaClient =
    new AWSLambdaClient().withRegion(region)
}
