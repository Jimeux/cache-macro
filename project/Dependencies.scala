import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.3"

  lazy val macroParadise = "org.scalameta" %% "paradise" % "3.0.0-M10"
  lazy val scalameta = "org.scalameta" %% "scalameta" % "1.8.0"
  lazy val scalametaContrib = "org.scalameta" %% "contrib" % "1.8.0"
}
