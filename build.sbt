lazy val assemblySettings = Seq(
  version := "0.1.0-SNAPSHOT",
  organization := "io.github.copperlight",
  scalaVersion := Dependencies.Version.scala,
  assembly / assemblyJarName := s"${name.value}_${scalaBinaryVersion.value}_${version.value}.jar",
  assembly / mainClass := Some(s"${organization.value}.skeleton.Main"),
)

lazy val `scalaplat-skeleton` = project.in(file("."))
  .configure(BuildSettings.profile)
  .settings(libraryDependencies ++= Seq(
    Dependencies.akkaActorTyped,
    Dependencies.akkaHttp,
    Dependencies.akkaStream,
    Dependencies.jacksonScala,
    Dependencies.logback,
    Dependencies.scalaLogging,
    Dependencies.akkaHttpTestkit % Test,
    Dependencies.akkaStreamTestkit % Test,
    Dependencies.munit % Test,
  ))
  .settings(assemblySettings: _*)
