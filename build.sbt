organization := "com.lucho"

name := "rxspray"

version := "0.1"

scalaVersion := "2.10.3"

val sprayVersion = "1.2.0"

val akkaVersion = "2.2.3"

val logbackVersion = "1.0.9"

resolvers ++= Seq(
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
  "spray repo" at "http://repo.spray.io" 
)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

seq(Revolver.settings: _*)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.netflix.rxjava" % "rxjava-core" % "0.15.1",
  "com.netflix.rxjava" % "rxjava-scala" % "0.15.1" exclude("org.scala-lang", "scala-library"),
  "io.spray" % "spray-can" % sprayVersion,
  "io.spray" % "spray-client" % sprayVersion,
  "io.spray" % "spray-http" % sprayVersion,
  "io.spray" % "spray-httpx" % sprayVersion,
  "io.spray" %% "spray-json" % "1.2.5",
  "org.scalatest" %% "scalatest" % "2.0" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.1" % "test",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "ch.qos.logback" % "logback-core" % logbackVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "org.projectreactor" % "reactor-logback" % "1.0.0.RELEASE"
)