import Dependencies._
import sbt.{CrossVersion, Resolver}

lazy val commonSettings = Seq(
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.3",
  scalacOptions := Seq(
    "-encoding", "UTF-8",
    "-target:jvm-1.8",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Xfatal-warnings",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-language:higherKinds"
  )
)

lazy val macroAnnotationSettings = Seq(
  resolvers += Resolver.sonatypeRepo("releases"),
  resolvers += Resolver.bintrayRepo("scalameta", "maven"),
  addCompilerPlugin(macroParadise cross CrossVersion.full),
  scalacOptions += "-Xplugin-require:macroparadise",
  // macroparadise plugin doesn't work in REPL yet
  scalacOptions in(Compile, console) ~= (_ filterNot (_ contains "paradise"))
)

lazy val macros = (project in file("macros"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      scalameta,
      scalametaContrib,
      json4sNative,
      json4sExt
    )
  )
  .settings(macroAnnotationSettings)

lazy val root = (project in file("."))
  .settings(
    commonSettings,
    name := "cache-macro",
    libraryDependencies ++= Seq(
      json4sNative,
      json4sExt,

      scalaTest % Test
    )
  )
  .settings(macroAnnotationSettings)
  .dependsOn(macros)
