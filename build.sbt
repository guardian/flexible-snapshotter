name := "flexible-snapshotter"
organization  := "com.gu"

scalaVersion in ThisBuild := "2.11.8"

description   := "AWS lambdas to snapshot Flexible content to S3"
scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
  "com.amazonaws" % "aws-lambda-java-events" % "1.1.0",
  "com.amazonaws" % "aws-lambda-java-log4j" % "1.0.0",
  "org.slf4j" % "slf4j-log4j12" % "1.7.21",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.10.39",
  "com.typesafe.play" %% "play-json" % "2.5.0",
  "com.typesafe.play" %% "play-ws" % "2.5.0",
  "org.scalatest" %% "scalatest" % "2.2.5" % "test"
)