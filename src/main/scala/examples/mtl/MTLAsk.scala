package examples.mtl

import cats._
import cats.data.Kleisli
import cats.effect._
import cats.effect.std.Console
import cats.implicits._
import cats.mtl._
import derevo.cats.show
import derevo.derive

object MTLAsk extends IOApp.Simple {
  import reader._

  def simple[F[_]: Ask[*[_], String]: Functor]: F[String] =
    Ask[F, String].ask.map(_ ++ " foo")

  def program[F[_]: Console: FlatMap: HasCtx]: F[Unit] =
    Ask[F, Ctx].ask.flatMap(Console[F].println)

  val ctx = Ctx(Foo("foo"), Bar(123))

  val effectful: IO[Unit] = {
    implicit val askIO: Ask[IO, Ctx] = ManualAsk.of(ctx)
    program[IO]
  }

  def run: IO[Unit] =
    program[Kleisli[IO, Ctx, *]].run(ctx) >> effectful

}

object ManualAsk {
  def of[F[_]: Applicative, A](ctx: A): Ask[F, A] =
    new Ask[F, A] {
      def applicative: Applicative[F] = implicitly

      def ask[A2 >: A]: F[A2] = ctx.pure[F].widen
    }
}

object reader {
  @derive(show)
  final case class Foo(value: String)

  @derive(show)
  final case class Bar(value: Int)

  @derive(show)
  final case class Ctx(foo: Foo, bar: Bar)

  type HasFoo[F[_]] = Ask[F, Foo]
  type HasBar[F[_]] = Ask[F, Bar]
  type HasCtx[F[_]] = Ask[F, Ctx]
}
