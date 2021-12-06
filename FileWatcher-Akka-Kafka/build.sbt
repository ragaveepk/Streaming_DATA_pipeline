name := "Streaming Data Pipeline"

version := "0.1"

scalaVersion := "2.12.12"

idePackagePrefix := Some("org.cs441.proj")
val scalacticVersion = "3.2.9"

val AkkaVersion = "2.5.17"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
  "org.scalactic" %% "scalactic" % scalacticVersion,
  "org.scalatest" %% "scalatest" % scalacticVersion % Test,
  "org.scalatest" %% "scalatest-featurespec" % scalacticVersion % Test,
)

libraryDependencies += "com.typesafe" % "config" % "1.3.3"
libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % "1.0.1"
