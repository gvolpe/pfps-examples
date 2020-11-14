import sbt._

object Dependencies {

  object Versions {
    val cats         = "2.2.0"
    val catsEffect   = "2.2.0"
    val catsMeowMtl  = "0.4.1"
    val console4cats = "0.8.1"
    val derevo       = "0.11.5"
    val fs2          = "2.4.5"
    val log4cats     = "1.0.0"
    val logback      = "1.2.1"
    val monocle      = "2.1.0"
    val newtype      = "0.4.4"
    val refined      = "0.9.18"

    val betterMonadicFor = "0.3.1"
    val contextApplied   = "0.1.4"
    val kindProjector    = "0.11.0"
  }

  object Libraries {
    val cats         = "org.typelevel"  %% "cats-core"    % Versions.cats
    val catsEffect   = "org.typelevel"  %% "cats-effect"  % Versions.catsEffect
    val console4cats = "dev.profunktor" %% "console4cats" % Versions.console4cats
    val fs2          = "co.fs2"         %% "fs2-core"     % Versions.fs2

    val catsMeowMtlCore    = "com.olegpy" %% "meow-mtl-core"    % Versions.catsMeowMtl
    val catsMeowMtlEffects = "com.olegpy" %% "meow-mtl-effects" % Versions.catsMeowMtl

    val derevoCats    = "org.manatki" %% "derevo-cats"         % Versions.derevo
    val derevoTagless = "org.manatki" %% "derevo-cats-tagless" % Versions.derevo

    val refinedCore = "eu.timepit" %% "refined"      % Versions.refined
    val refinedCats = "eu.timepit" %% "refined-cats" % Versions.refined

    val log4cats = "io.chrisdavenport" %% "log4cats-slf4j" % Versions.log4cats
    val newtype  = "io.estatico"       %% "newtype"        % Versions.newtype

    val monocleCore  = "com.github.julien-truffaut" %% "monocle-core"  % Versions.monocle
    val monocleMacro = "com.github.julien-truffaut" %% "monocle-macro" % Versions.monocle

    // Runtime
    val logback = "ch.qos.logback" % "logback-classic" % Versions.logback
  }

  object CompilerPlugins {
    val betterMonadicFor = compilerPlugin("com.olegpy"     %% "better-monadic-for" % Versions.betterMonadicFor)
    val contextApplied   = compilerPlugin("org.augustjune" %% "context-applied"    % Versions.contextApplied)
    val kindProjector = compilerPlugin(
      "org.typelevel" %% "kind-projector" % Versions.kindProjector cross CrossVersion.full
    )
  }

}
