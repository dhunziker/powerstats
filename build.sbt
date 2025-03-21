import Dependencies.*
import sbtassembly.AssemblyKeys.assemblyMergeStrategy

Global / excludeLintKeys := Set(idePackagePrefix)

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / organization := "ai.powerstats"

ThisBuild / scalaVersion := "3.6.3"

ThisBuild / Compile / run / fork := true

lazy val commonSettings = Seq(
  idePackagePrefix := Some(s"${organization.value}.${name.value}"),
  scalacOptions += "-deprecation",
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
    "org.typelevel" %% "cats-effect-testing-scalatest" % catsEffectTestingVersion % Test,
    "org.typelevel" %% "cats-effect" % catsEffectVersion,
    "org.typelevel" %% "log4cats-slf4j" % log4catsVersion
  ),
  assembly / assemblyMergeStrategy := {
    case PathList("META-INF", xs@_*) =>
      xs.map(_.toLowerCase) match {
        case "services" :: xs => MergeStrategy.filterDistinctLines
        case _ => MergeStrategy.discard
      }
    case x => MergeStrategy.first
  }
)

lazy val root = (project in file("."))
  .aggregate(common, backend, api)
  .settings(
    name := "powerstats"
  )

lazy val common = (project in file("common"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-config" % circeConfigVersion,
      "io.circe" %% "circe-generic" % circeGenericVersion,
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari" % doobieVersion
    )
  )

lazy val backend = (project in file("backend"))
  .dependsOn(common)
  .settings(
    commonSettings,
    assembly / mainClass := Some("ai.powerstats.backend.Main"),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion % Runtime,
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-io" % fs2Version
    )
  )

lazy val api = (project in file("api"))
  .dependsOn(common)
  .settings(
    commonSettings,
    assembly / mainClass := Some("ai.powerstats.api.Main"),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion % Runtime,
      "org.flywaydb" % "flyway-core" % flywayVersion,
      "org.flywaydb" % "flyway-database-postgresql" % flywayVersion % Runtime,
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion
    )
  )
