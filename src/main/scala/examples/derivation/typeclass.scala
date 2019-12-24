package examples.derivation

import cats._
import cats.implicits._
import cats.tagless.FunctorK
import org.manatki.derevo.catsInstances.{ eq => eqv, show, semigroup }
import org.manatki.derevo.derive
import org.manatki.derevo.tagless.{ flatMap, functor, functorK }

object typeclass {

  @derive(functorK)
  sealed trait Foo[F[_]]
  case object FooOne extends Foo[List]
  case object FooTwo extends Foo[Option]

  FunctorK[Foo]

  @derive(functor, flatMap)
  sealed trait HigherKind[A]
  case object KindOne extends HigherKind[Int]
  case object KindTwo extends HigherKind[String]

  Functor[HigherKind]
  FlatMap[HigherKind]

  @derive(eqv, semigroup, show)
  final case class SingleKind(value: String)

  Eq[SingleKind]
  Semigroup[SingleKind]
  Show[SingleKind]

}
