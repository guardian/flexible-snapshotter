package com.gu.flexible.snapshotter.mainclasses

import com.amazonaws.services.lambda.runtime.{ClientContext, CognitoIdentity, Context, LambdaLogger}

class FakeContext extends Context {
  def getIdentity: CognitoIdentity = ???

  def getLogStreamName: String = ???

  def getClientContext: ClientContext = ???

  val logger = new LambdaLogger {
    def log(string: String): Unit = System.out.println(string)
    def log(bytes: Array[Byte]): Unit = System.out.println(bytes)
  }
  def getLogger: LambdaLogger = logger

  def getMemoryLimitInMB: Int = ???

  def getInvokedFunctionArn: String = ???

  def getRemainingTimeInMillis: Int = ???

  def getAwsRequestId: String = ???

  def getFunctionVersion: String = ???

  def getFunctionName: String = "Scheduling-Lambda-DEV"

  def getLogGroupName: String = ???
}
