import com.typesafe.sbt.packager.archetypes.JavaAppPackaging._

name := "snapshotter-lambda"
organization  := "com.gu"

scalaVersion := "2.13.8"

description   := "AWS lambdas to snapshot Flexible content to S3"
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Ywarn-unused:imports")

val awsVersion = "1.12.148"
val playVersion = "2.1.3"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.1",
  "com.amazonaws" % "aws-lambda-java-events" % "1.2.1" intransitive(),
  "com.amazonaws" % "aws-lambda-java-log4j2" % "1.5.1",
  "org.slf4j" % "slf4j-simple" % "1.7.32",
  "net.logstash.log4j" % "jsonevent-layout" % "1.7",
  "com.amazonaws" % "aws-java-sdk-s3" % awsVersion,
  "com.amazonaws" % "aws-java-sdk-lambda" % awsVersion,
  "com.amazonaws" % "aws-java-sdk-cloudwatch" % awsVersion,
  "com.amazonaws" % "aws-java-sdk-sns" % awsVersion,
  "com.typesafe.play" %% "play-ws-standalone-json" % playVersion,
  "com.typesafe.play" %% "play-ahc-ws-standalone" % playVersion,
  "org.scalatest" %% "scalatest" % "3.1.2" % "test",
  // This is required to force aws libraries to use the latest version of jackson
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.2"
)

publishMavenStyle := false

enablePlugins(JavaAppPackaging, RiffRaffArtifact)

Universal / topLevelDirectory := None
Universal / packageName := normalizedName.value