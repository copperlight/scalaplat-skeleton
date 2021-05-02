import sbt._

// format: off
object Dependencies {
  object Versions {
    val akka     = "2.6.14"
    val akkaHttp = "10.2.4"
    val scala    = "2.13.4"
  }

  val akkaActorTyped    = "com.typesafe.akka" %% "akka-actor-typed" % Versions.akka
  val akkaSlf4j         = "com.typesafe.akka" %% "akka-slf4j" % Versions.akka
  val akkaStream        = "com.typesafe.akka" %% "akka-stream" % Versions.akka
  val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % Versions.akka
  val akkaTestkit       = "com.typesafe.akka" %% "akka-testkit" % Versions.akka

  val akkaHttp          = "com.typesafe.akka" %% "akka-http" % Versions.akkaHttp
  val akkaHttpCaching   = "com.typesafe.akka" %% "akka-http-caching" % Versions.akkaHttp
  val akkaHttpCore      = "com.typesafe.akka" %% "akka-http-core" % Versions.akkaHttp
  val akkaHttpTestkit   = "com.typesafe.akka" %% "akka-http-testkit" % Versions.akkaHttp

  val logback           = "ch.qos.logback" % "logback-classic" % "1.2.3"

  val munit             = "org.scalameta" %% "munit" % "0.7.25"

  val scalaCompiler     = "org.scala-lang" % "scala-compiler" % Versions.scala
  val scalaLibrary      = "org.scala-lang" % "scala-library" % Versions.scala
  val scalaReflect      = "org.scala-lang" % "scala-reflect" % Versions.scala

  val scalaLogging      = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.3"
}
// format: on
