package com.gu.flexible.snapshotter.resources

import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.amazonaws.services.s3.AmazonS3Client
import com.gu.flexible.snapshotter.Config

object AWSClientFactory {
  def createKinesisClient(implicit config:Config): AmazonKinesisClient = new AmazonKinesisClient().withRegion(config.region)
  def createS3Client(implicit config: Config): AmazonS3Client = new AmazonS3Client().withRegion(config.region)
}
