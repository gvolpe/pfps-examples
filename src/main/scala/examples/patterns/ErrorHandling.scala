package examples.patterns

import cats._
import cats.effect._
import cats.implicits._
import scala.util.control.NoStackTrace

case class Category(name: String)

trait Random[F[_]] {
  def bool: F[Boolean]
  def int: F[Int]
}

object Random {
  implicit def syncInstance[F[_]: Sync]: Random[F] =
    new Random[F] {
      def bool: F[Boolean] = int.map(_ % 2 === 0)
      def int: F[Int]      = F.delay(scala.util.Random.nextInt(100))
    }
}

sealed trait BusinessError extends NoStackTrace
case object RandomError extends BusinessError
//case object AnotherError extends BusinessError // uncomment to see issue

trait Categories[F[_]] {
  def findAll: F[List[Category]]
  def maybeFindAll: F[Either[BusinessError, List[Category]]]
}

class LiveCategories[
    F[_]: MonadError[*[_], Throwable]: Random
] extends Categories[F] {

  def findAll: F[List[Category]] =
    F.bool.ifM(
      List.empty[Category].pure[F],
      RandomError.raiseError[F, List[Category]]
    )

  def maybeFindAll: F[Either[BusinessError, List[Category]]] =
    F.bool.map {
      case true  => List.empty[Category].asRight[BusinessError]
      case false => RandomError.asLeft[List[Category]]
    }

}

class Program[F[_]: Functor](
    categories: Categories[F]
) {

  def finfAll: F[List[Category]] =
    categories.maybeFindAll.map {
      case Right(c)          => c
      case Left(RandomError) => List.empty[Category]
    }

}

class SameProgram[F[_]: ApplicativeError[*[_], Throwable]](
    categories: Categories[F]
) {

  def findAll: F[List[Category]] =
    categories.findAll.handleError {
      case RandomError => List.empty[Category]
    }

}
