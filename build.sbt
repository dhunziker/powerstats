import Dependencies.*

Global / excludeLintKeys := Set(idePackagePrefix)

ThisBuild / organization := "ai.powerstats"

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.6.3"

ThisBuild / Compile / run / fork := true

lazy val commonSettings = Seq(
  idePackagePrefix := Some(s"${organization.value}.${name.value}"),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
    "org.typelevel" %% "cats-effect-testing-scalatest" % catsEffectTestingVersion % Test,
    "org.http4s" %% "http4s-ember-client" % http4sVersion,
    "org.http4s" %% "http4s-ember-server" % http4sVersion,
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.typelevel" %% "log4cats-slf4j" % log4catsVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion
  )
)

lazy val root = (project in file("."))
  .aggregate(common, backend, api)
  .settings(
    name := "powerstats"
  )

lazy val common = (project in file("common"))
  .settings(
    commonSettings
  )

lazy val backend = (project in file("backend"))
  .dependsOn(common)
  .settings(
    commonSettings
  )

lazy val api = (project in file("api"))
  .dependsOn(common)
  .settings(
    commonSettings
  )
