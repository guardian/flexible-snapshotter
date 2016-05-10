name := "flexible-snapshotter"
organization  := "com.gu"

scalaVersion in ThisBuild := "2.11.8"

description   := "AWS lambdas to snapshot Flexible content to S3"
scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

val awsVersion = "1.10.75"
val playVersion = "2.5.0"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
  "com.amazonaws" % "aws-lambda-java-events" % "1.1.0",
  "com.amazonaws" % "aws-lambda-java-log4j" % "1.0.0",
  "org.slf4j" % "slf4j-log4j12" % "1.7.21",
  "com.amazonaws" % "aws-java-sdk-s3" % awsVersion,
  "com.typesafe.play" %% "play-json" % playVersion,
  "com.typesafe.play" %% "play-ws" % playVersion,
  "org.scalatest" %% "scalatest" % "2.2.5" % "test"
)