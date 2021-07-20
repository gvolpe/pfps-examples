import Dependencies._

ThisBuild / scalaVersion := "2.13.6"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "dev.profunktor"
ThisBuild / organizationName := "ProfunKtor"

ThisBuild / scalafixDependencies += Libraries.organizeImports

resolvers += Resolver.sonatypeRepo("snapshots")

Compile / run / fork := true

lazy val root = (project in file("."))
  .settings(
    name := "pfps-examples",
    scalacOptions ++= List("-Ymacro-annotations", "-Yrangepos", "-Wconf:cat=unused:info"),
    scalafmtOnCompile := true,
    libraryDependencies ++= Seq(
      CompilerPlugins.betterMonadicFor,
      CompilerPlugins.kindProjector,
      CompilerPlugins.semanticDB,
      Libraries.cats,
      Libraries.catsEffect,
      Libraries.catsMtl,
      Libraries.derevoCats,
      Libraries.derevoCirceMagnolia,
      Libraries.derevoTagless,
      Libraries.fs2,
      Libraries.monocleCore,
      Libraries.monocleMacro,
      Libraries.newtype,
      Libraries.refinedCore,
      Libraries.refinedCats,
      Libraries.tofu
    )
  )

addCommandAlias("runLinter", ";scalafixAll --rules OrganizeImports")
