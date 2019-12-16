package examples.derivation

import cats._
import cats.implicits._
import org.manatki.derevo.catsInstances.show
import org.manatki.derevo.derive
import org.manatki.derevo.tagless.{ flatMap, functor }

object typeclass {

  @derive(functor, flatMap)
  sealed trait HigherKind[A]
  case object KindOne extends HigherKind[Int]
  case object KindTwo extends HigherKind[String]

  @derive(show)
  final case class SingleKind(value: String)

  Functor[HigherKind]
  FlatMap[HigherKind]
  //Monad[HigherKind] // does not compile

}
