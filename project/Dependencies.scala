import sbt._

// format: off
object Dependencies {
  object Version {
    val akka     = "2.6.14"
    val akkaHttp = "10.2.4"
    val aws2     = "2.16.52"
    val jackson  = "2.12.2"
    val scala    = "2.13.4"
  }

  val akkaActorTyped    = "com.typesafe.akka" %% "akka-actor-typed" % Version.akka
  val akkaSlf4j         = "com.typesafe.akka" %% "akka-slf4j" % Version.akka
  val akkaStream        = "com.typesafe.akka" %% "akka-stream" % Version.akka
  val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % Version.akka
  val akkaTestkit       = "com.typesafe.akka" %% "akka-testkit" % Version.akka

  val akkaHttp          = "com.typesafe.akka" %% "akka-http" % Version.akkaHttp
  val akkaHttpCaching   = "com.typesafe.akka" %% "akka-http-caching" % Version.akkaHttp
  val akkaHttpCore      = "com.typesafe.akka" %% "akka-http-core" % Version.akkaHttp
  val akkaHttpTestkit   = "com.typesafe.akka" %% "akka-http-testkit" % Version.akkaHttp

  val aws2Core          = "software.amazon.awssdk" % "core" % Version.aws2
  val aws2DynamoDB      = "software.amazon.awssdk" % "dynamodb" % Version.aws2
  val aws2EC2           = "software.amazon.awssdk" % "ec2" % Version.aws2
  val aws2SES           = "software.amazon.awssdk" % "ses" % Version.aws2
  val aws2STS           = "software.amazon.awssdk" % "sts" % Version.aws2

  val caffeine           = "com.github.ben-manes.caffeine" % "caffeine" % "3.0.1"

  val jacksonAnno       = "com.fasterxml.jackson.core" % "jackson-annotations" % Version.jackson
  val jacksonCore       = "com.fasterxml.jackson.core" % "jackson-core" % Version.jackson
  val jacksonJava8      = "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % Version.jackson
  val jacksonJsr310     = "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % Version.jackson
  val jacksonMapper     = "com.fasterxml.jackson.core" % "jackson-databind" % Version.jackson
  val jacksonScala      = "com.fasterxml.jackson.module" %% "jackson-module-scala" % Version.jackson
  val jacksonSmile      = "com.fasterxml.jackson.dataformat" % "jackson-dataformat-smile" % Version.jackson

  val logback           = "ch.qos.logback" % "logback-classic" % "1.2.3"

  val munit             = "org.scalameta" %% "munit" % "0.7.25"

  val scalaCompiler     = "org.scala-lang" % "scala-compiler" % Version.scala
  val scalaLibrary      = "org.scala-lang" % "scala-library" % Version.scala
  val scalaReflect      = "org.scala-lang" % "scala-reflect" % Version.scala

  val scalaLogging      = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.3"

  val typesafeConfig    = "com.typesafe" % "config" % "1.4.1"
}
// format: on
