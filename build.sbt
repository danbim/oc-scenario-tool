name := "scenario-tool-playground"

version := "1.0"

lazy val `scenario-tool-playground` = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "org.webjars" %% "webjars-play" % "2.3.0-2",
  "org.webjars" % "bootstrap" % "3.1.1-2",
  "com.google.guava" % "guava" % "18.0"
)

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")