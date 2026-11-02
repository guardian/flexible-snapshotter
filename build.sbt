import com.typesafe.sbt.packager.archetypes.JavaAppPackaging._

name := "snapshotter-lambda"
organization  := "com.gu"

scalaVersion := "2.13.9"

description   := "AWS lambdas to snapshot Flexible content to S3"
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Ywarn-unused:imports")

val awsVersion = "1.12.765"
val playVersion = "2.2.9"
val jacksonVersion = "2.18.11"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.1",
  "com.amazonaws" % "aws-lambda-java-events" % "1.2.1" intransitive(),
  "com.amazonaws" % "aws-lambda-java-log4j2" % "1.6.0",
  "org.slf4j" % "slf4j-simple" % "2.0.13",
  "net.logstash.log4j" % "jsonevent-layout" % "1.7",
  "com.amazonaws" % "aws-java-sdk-s3" % awsVersion,
  "com.amazonaws" % "aws-java-sdk-lambda" % awsVersion,
  "com.amazonaws" % "aws-java-sdk-cloudwatch" % awsVersion,
  "com.amazonaws" % "aws-java-sdk-sns" % awsVersion,
  "com.typesafe.play" %% "play-ws-standalone-json" % playVersion,
  "com.typesafe.play" %% "play-ahc-ws-standalone" % playVersion,
  "org.scalatest" %% "scalatest" % "3.2.19" % "test",
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion
)

// Keep Play and AWS transitive Jackson modules on the same patched release.
dependencyOverrides ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion,
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor" % jacksonVersion,
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % jacksonVersion,
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % jacksonVersion
)

publishMavenStyle := false

enablePlugins(JavaAppPackaging)

Universal / topLevelDirectory := None
Universal / packageName := normalizedName.value