let GithubActions =
      https://raw.githubusercontent.com/regadas/github-actions-dhall/master/package.dhall sha256:b42b062af139587666185c6fb72cc2994aa85a30065324174760b7d29a9d81c9

let matrix = toMap { java = [ "8.0.242", "11.0.5" ], scala = [ "2.12.10", "2.13.2" ] }

let setup =
      [ GithubActions.steps.checkout
      , GithubActions.steps.run
          { run =
              ''
              shasum build.sbt \
                project/plugins.sbt \
                project/build.properties \
                project/Dependencies.scala > gha.cache.tmp
              ''
          }
      , GithubActions.steps.cache
          { path = "~/.sbt", key = "sbt", hashFile = "gha.cache.tmp" }
      , GithubActions.steps.cache
          { path = "~/.cache/coursier"
          , key = "coursier"
          , hashFile = "gha.cache.tmp"
          }
      ]

in  GithubActions.Workflow::{
    , name = "Scala"
    , on = GithubActions.On::{
      , push = Some GithubActions.Push::{
          branches = Some [ "master" ]
        }
      , pull_request = Some GithubActions.PullRequest::{=}
      }
    , jobs = toMap
        { build = GithubActions.Job::{
          , name = "Build"
          , needs = None (List Text)
          , strategy = Some GithubActions.Strategy::{ matrix = matrix }
          , runs-on = GithubActions.types.RunsOn.ubuntu-latest
          , steps =
                setup
              # [ GithubActions.steps.java-setup
                    { java-version = "\${{ matrix.java}}" }
                , GithubActions.steps.run
                    { run = "sbt \"++\${{ matrix.scala}} test\"" }
                ]
          }
        }
    }
