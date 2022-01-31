import com.typesafe.sbt.packager.archetypes.JavaAppPackaging._

name := "snapshotter-lambda"
organization  := "com.gu"

scalaVersion in ThisBuild := "2.11.11"

description   := "AWS lambdas to snapshot Flexible content to S3"
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Ywarn-unused-import")

val awsVersion = "1.12.148"
val playVersion = "1.1.2"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
  "com.amazonaws" % "aws-lambda-java-events" % "1.1.0" intransitive(),
  "org.slf4j" % "slf4j-simple" % "1.7.32",
  "net.logstash.log4j" % "jsonevent-layout" % "1.7",
  "com.amazonaws" % "aws-java-sdk-s3" % awsVersion,
  "com.amazonaws" % "aws-java-sdk-lambda" % awsVersion,
  "com.amazonaws" % "aws-java-sdk-cloudwatch" % awsVersion,
  "com.amazonaws" % "aws-java-sdk-sns" % awsVersion,
  "com.typesafe.play" %% "play-ws-standalone-json" % playVersion,
  "com.typesafe.play" %% "play-ahc-ws-standalone" % playVersion,
  "org.scalatest" %% "scalatest" % "2.2.5" % "test",
  // This is required to force aws libraries to use the latest version of jackson
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.2"
)

publishMavenStyle := false

enablePlugins(JavaAppPackaging, RiffRaffArtifact)

topLevelDirectory in Universal := None
packageName in Universal := normalizedName.value

riffRaffPackageType := (packageBin in Universal).value
riffRaffUploadArtifactBucket := Option("riffraff-artifact")
riffRaffUploadManifestBucket := Option("riffraff-builds")
riffRaffManifestProjectName :=  s"editorial-tools:flexible:${name.value}"
riffRaffBuildIdentifier :=  Option(System.getenv("BUILD_NUMBER")).getOrElse("dev")
