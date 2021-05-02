
lazy val `scalaplat-skeleton` = project.in(file("."))
  .settings(libraryDependencies ++= Seq(
    Dependencies.akkaActorTyped,
    Dependencies.akkaHttp,
    Dependencies.akkaHttpTestkit,
    Dependencies.akkaStream,
    Dependencies.akkaStreamTestkit,
    Dependencies.logback,
    Dependencies.munit,
    Dependencies.scalaLogging
  ))
