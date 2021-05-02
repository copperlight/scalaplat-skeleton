
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
