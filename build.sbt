name := """auth0-play-filter"""

version := "1.0.1"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "2.5.4",
  "com.typesafe" % "config" % "1.3.0",
  "com.auth0" % "java-jwt" % "2.1.0",
  "commons-codec" % "commons-codec" % "1.4",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test")
