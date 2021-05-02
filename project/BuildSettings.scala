import sbt._
import sbt.Keys._

object BuildSettings {

  val compilerFlags = Seq(
    "-deprecation",
    "-unchecked",
    "-Xlint:_,-infer-any",
    "-feature"
  )

  lazy val checkLicenseHeaders = taskKey[Unit]("Check the license headers for all source files.")

  lazy val formatLicenseHeaders = taskKey[Unit]("Fix the license headers for all source files.")

  lazy val buildSettings: Seq[Def.Setting[_]] = Seq(
    organization := "io.github.copperlight",
    scalaVersion := Dependencies.Version.scala,
    scalacOptions ++= BuildSettings.compilerFlags,
    crossPaths := false,
    sourcesInBase := false,
    Test / fork := true,
    autoScalaLibrary := false,
    externalResolvers := BuildSettings.resolvers,
    (update / evictionWarningOptions).withRank(KeyRanks.Invisible) := EvictionWarningOptions.empty,
    checkLicenseHeaders := License.checkLicenseHeaders(streams.value.log, sourceDirectory.value),
    formatLicenseHeaders := License.formatLicenseHeaders(streams.value.log, sourceDirectory.value),
    Compile / packageBin / packageOptions += Package.ManifestAttributes(
      "Build-Date" -> java.time.Instant.now().toString,
      "Build-Number" -> sys.env.getOrElse("GITHUB_RUN_ID", "unknown"),
      "Commit" -> sys.env.getOrElse("GITHUB_SHA", "unknown")
    )
  )

  lazy val commonDeps = Seq()

  val resolvers = Seq(
    Resolver.mavenLocal,
    Resolver.mavenCentral
  )
}