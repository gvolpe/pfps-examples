package examples.derivation

import cats._
import cats.arrow.FunctionK
import cats.data.Tuple2K
import cats.implicits._
import cats.tagless._
import org.manatki.derevo.catsInstances.{ eq => eqv, _ }
import org.manatki.derevo.derive
import org.manatki.derevo.tagless.{ applyK, flatMap }

object typeclass {

  // --- Single-kinded type `*`
  @derive(eqv, order, semigroup, show)
  final case class SingleKind(value: String)

  Eq[SingleKind]
  Order[SingleKind]
  Semigroup[SingleKind]
  Show[SingleKind]

  val a = SingleKind("a")
  val b = SingleKind("b")

  a === b // from Eq
  a.min(b) // from Order
  a |+| b // from Semigroup
  a.show // from Show

  // --- Higher-kinded type `* -> *`
  @derive(flatMap)
  sealed trait HigherKind[A]
  case object KindOne extends HigherKind[Int]
  case object KindTwo extends HigherKind[String]

  Apply[HigherKind]
  Functor[HigherKind]
  FlatMap[HigherKind]
  Semigroupal[HigherKind]

  KindOne.map(_ * 2) // from Functor
  KindTwo.void // from Functor
  KindTwo >> KindOne // from FlatMap
  (KindOne, KindTwo).tupled // from Semigroupal
  (KindOne, KindTwo).mapN { // from Apply
    case (x, y) => s"$x - $y"
  }

  // --- Higher-order functor `* -> * -> *`
  @derive(applyK)
  trait Alg[F[_]] {
    def foo: F[String]
  }

  case object ListAlg extends Alg[List] {
    def foo: List[String] = List("1", "2")
  }

  case object OptionAlg extends Alg[Option] {
    def foo: Option[String] = "foo".some
  }

  FunctorK[Alg].mapK(ListAlg)(λ[List ~> Option](_.headOption))
  SemigroupalK[Alg].productK(ListAlg, OptionAlg)

  val fk: Tuple2K[List, Option, *] ~> Either[String, *] =
    new FunctionK[Tuple2K[List, Option, *], Either[String, *]] {
      def apply[A](fa: Tuple2K[List, Option, A]): Either[String, A] =
        fa.second.fold(fa.first.mkString(", ").asLeft[A])(_.asRight)
    }

  ApplyK[Alg].map2K(ListAlg, OptionAlg)(fk)

}
