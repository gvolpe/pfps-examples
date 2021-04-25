package examples.mtl

import cats.Monad
import cats.data.StateT
import cats.effect._
import cats.effect.std.Console
import cats.implicits._
import cats.mtl.Stateful
import derevo.cats.show
import derevo.derive

object MTLStateful extends IOApp.Simple {

  @derive(show)
  case class FooState(value: String) extends AnyVal

  type HasFoo[F[_]] = Stateful[F, FooState]
  object HasFoo {
    def apply[F[_]: Stateful[*[_], FooState]]: HasFoo[F] = implicitly
  }

  def program[F[_]: Console: HasFoo: Monad]: F[Unit] =
    for {
      a <- HasFoo[F].get
      _ <- Console[F].println(a)
      _ <- HasFoo[F].set(FooState("foo"))
      b <- HasFoo[F].get
      _ <- Console[F].println(b)
    } yield ()

  def inspection[F[_]: Console: HasFoo: Monad]: F[Unit] =
    for {
      a <- HasFoo[F].inspect(st => s"prefix:$st")
      _ <- Console[F].println(a)
      b <- HasFoo[F].get
      _ <- Console[F].println(b)
    } yield ()

  val p1: IO[Unit] =
    program[StateT[IO, FooState, *]].run(FooState("init")).void

  val p2: IO[Unit] =
    StatefulRef
      .of[IO, FooState](FooState("init"))
      .flatMap { implicit st =>
        program[IO]
      }

  def run: IO[Unit] = p1 >> p2

}

object StatefulRef {
  def of[F[_]: Ref.Make: Monad, A](init: A): F[Stateful[F, A]] =
    Ref.of[F, A](init).map { ref =>
      new Stateful[F, A] {
        def monad: Monad[F] = implicitly

        def get: F[A]          = ref.get
        def set(s: A): F[Unit] = ref.set(s)
      }
    }
}
