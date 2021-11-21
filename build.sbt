name := "Streaming Data Pipeline"

version := "0.1"

scalaVersion := "2.12.12"

idePackagePrefix := Some("org.cs441.proj")

val AkkaVersion = "2.6.13"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test
)
