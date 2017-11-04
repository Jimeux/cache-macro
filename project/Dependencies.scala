import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.3"

  lazy val macroParadise = "org.scalameta" %% "paradise" % "3.0.0-M10"
  lazy val scalameta = "org.scalameta" %% "scalameta" % "1.8.0"
  lazy val scalametaContrib = "org.scalameta" %% "contrib" % "1.8.0"

  lazy val json4sNative = "org.json4s" %% "json4s-native" % "3.5.3"
  lazy val json4sExt = "org.json4s" %% "json4s-ext" % "3.5.3"
}
