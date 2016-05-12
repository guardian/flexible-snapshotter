package com.gu.flexible.snapshotter.resources

import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.s3.AmazonS3Client

object AWSClientFactory {
  def createKinesisClient(implicit region:Region = Regions.getCurrentRegion): AmazonKinesisClient = new AmazonKinesisClient().withRegion(region)
  def createS3Client(implicit region:Region = Regions.getCurrentRegion): AmazonS3Client = new AmazonS3Client().withRegion(region)
  def createLambdaClient(implicit region:Region = Regions.getCurrentRegion): AWSLambdaClient = new AWSLambdaClient().withRegion(region)
}
