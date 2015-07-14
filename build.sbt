name := "scenario-tool-playground"

version := "1.0"

lazy val `scenario-tool-playground` = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  cache,
  javaWs,
  javaCore,
  evolutions,
  "org.webjars" %% "webjars-play" % "2.3.0-2",
  "org.webjars" % "bootstrap" % "3.1.1-2",
  "com.google.guava" % "guava" % "18.0",
  "com.feth" %% "play-authenticate" % "0.7.0-SNAPSHOT",
  "be.objectify" %% "deadbolt-java" % "2.4.0"
)

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
routesGenerator := InjectedRoutesGenerator
unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")
