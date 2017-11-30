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

val awsVersion = "1.11.234"
val playVersion = "1.1.2"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-events" % "1.1.0" intransitive(),
  "com.amazonaws" % "aws-lambda-java-log4j" % "1.0.0",
  "org.slf4j" % "slf4j-log4j12" % "1.7.21",
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