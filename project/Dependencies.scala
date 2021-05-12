import sbt._

object Dependencies {

  object V {
    val cats       = "2.6.0"
    val catsEffect = "3.1.0"
    val catsMtl    = "1.2.0"
    val derevo     = "0.12.5"
    val fs2        = "3.0.2"
    val monocle    = "3.0.0-M5"
    val newtype    = "0.4.4"
    val refined    = "0.9.24"
    val tofu       = "0.10.2"

    val betterMonadicFor = "0.3.1"
    val kindProjector    = "0.11.3"
    val organizeImports  = "0.5.0"
    val semanticDB       = "4.4.17"
  }

  object Libraries {
    def derevo(artifact: String): ModuleID = "tf.tofu" %% s"derevo-$artifact" % V.derevo

    val cats       = "org.typelevel" %% "cats-core"   % V.cats
    val catsEffect = "org.typelevel" %% "cats-effect" % V.catsEffect
    val catsMtl    = "org.typelevel" %% "cats-mtl"    % V.catsMtl
    val fs2        = "co.fs2"        %% "fs2-core"    % V.fs2

    val derevoCats          = derevo("cats")
    val derevoCirceMagnolia = derevo("circe-magnolia")
    val derevoTagless       = derevo("cats-tagless")

    val tofu = "tf.tofu" %% "tofu-core-higher-kind" % V.tofu

    val refinedCore = "eu.timepit" %% "refined"      % V.refined
    val refinedCats = "eu.timepit" %% "refined-cats" % V.refined

    val newtype  = "io.estatico"       %% "newtype"        % V.newtype

    val monocleCore  = "com.github.julien-truffaut" %% "monocle-core"  % V.monocle
    val monocleMacro = "com.github.julien-truffaut" %% "monocle-macro" % V.monocle

    // Scalafix rules
    val organizeImports = "com.github.liancheng" %% "organize-imports" % V.organizeImports
  }

  object CompilerPlugins {
    val betterMonadicFor = compilerPlugin("com.olegpy" %% "better-monadic-for" % V.betterMonadicFor)
    val kindProjector = compilerPlugin(
      "org.typelevel" %% "kind-projector" % V.kindProjector cross CrossVersion.full
    )
    val semanticDB = compilerPlugin(
      "org.scalameta" % "semanticdb-scalac" % V.semanticDB cross CrossVersion.full
    )
  }

}
