import Dependencies.*

Global / excludeLintKeys := Set(idePackagePrefix)

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.6.3"

ThisBuild / Compile / run / fork := true

lazy val commonSettings = Seq(
  idePackagePrefix := Some(s"ai.powerstats.${name.value}"),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
    "org.http4s" %% "http4s-ember-client" % http4sVersion,
    "org.http4s" %% "http4s-ember-server" % http4sVersion,
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.typelevel" %% "log4cats-slf4j" % log4catsVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion
  )
)

lazy val root = (project in file("."))
  .aggregate(model, backend, api)
  .settings(
    name := "powerstats"
  )

lazy val model = (project in file("model"))
  .settings(
    commonSettings
  )

lazy val backend = (project in file("backend"))
  .dependsOn(model)
  .settings(
    commonSettings
  )

lazy val api = (project in file("api"))
  .dependsOn(model)
  .settings(
    commonSettings
  )
