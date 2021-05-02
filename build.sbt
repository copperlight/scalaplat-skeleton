lazy val commonSettings = Seq(
  version := "0.1.0-SNAPSHOT",
  organization := "io.github.copperlight",
  scalaVersion := Dependencies.Version.scala,
)

lazy val `scalaplat-skeleton` = project.in(file("."))
  .settings(libraryDependencies ++= Seq(
    Dependencies.akkaActorTyped,
    Dependencies.akkaHttp,
    Dependencies.akkaStream,
    Dependencies.logback,
    Dependencies.scalaLogging,
    Dependencies.akkaHttpTestkit % Test,
    Dependencies.akkaStreamTestkit % Test,
    Dependencies.munit % Test,
  ))
  .settings(commonSettings: _*)
  .settings(Seq(
    assembly / assemblyJarName := s"${name.value}_${scalaBinaryVersion.value}_${version.value}.jar",
    assembly / mainClass := Some(s"${organization.value}.skeleton.Main"),
  ))
