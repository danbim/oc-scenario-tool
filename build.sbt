name := "scenario-tool-playground"

version := "1.0"

lazy val `scenario-tool-playground` = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  cache,
  javaWs,
  javaCore,
  "org.webjars" %% "webjars-play" % "2.3.0-2",
  "org.webjars" % "bootstrap" % "3.1.1-2",
  "com.google.guava" % "guava" % "18.0",
  "com.feth" %% "play-authenticate" % "0.7.0-SNAPSHOT",
  "be.objectify" %% "deadbolt-java" % "2.4.0",
  ("com.clever-age" % "play2-elasticsearch" % "1.1.0")
    .exclude("com.typesafe.play", "play-functional_2.10")
    .exclude("com.typesafe.akka", "akka-actor_2.10")
    .exclude("com.typesafe.play", "play-json_2.10")
    .exclude("com.typesafe.play", "play_2.10")
    .exclude("com.typesafe.play", "play-iteratees_2.10")
    .exclude("com.typesafe.akka", "akka-slf4j_2.10")
    .exclude("org.scala-stm", "scala-stm_2.10")
    .exclude("com.typesafe.play", "play-datacommons_2.10")
    .exclude("com.typesafe.play", "play-java_2.10")
)

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")
