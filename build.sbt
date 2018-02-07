name := "test-app"

version := "0.1"

scalaVersion := "2.12.4"
val akkaV = "10.1.0-RC1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaV,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaV,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaV,
  "com.typesafe.akka" %% "akka-stream" % "2.5.8",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test"
)