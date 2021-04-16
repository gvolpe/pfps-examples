package examples.mtl

import cats._
import cats.data.Kleisli
import cats.effect._
import cats.effect.std.Console
import cats.implicits._
import cats.mtl._
import derevo.cats.show
import derevo.derive

object MtlClassyDemo extends IOApp.Simple {
  import reader._

  def simple[F[_]: Ask[*[_], String]: Functor]: F[String] =
    Ask[F, String].ask.map(_ ++ " foo")

  def p1[F[_]: Console: FlatMap: HasCtx]: F[Unit] =
    Ask[F, Ctx].ask.flatMap(Console[F].println)

  def p2[F[_]: Console: FlatMap: HasFoo]: F[Unit] =
    Ask[F, Foo].ask.flatMap(Console[F].println)

  def p3[F[_]: Console: FlatMap: HasBar]: F[Unit] =
    Ask[F, Bar].ask.flatMap(Console[F].println)

  // meow-mtl not yet published for the latest cats-mtl
  def program[F[_]: Console: FlatMap: HasCtx]: F[Unit] =
    p1[F] /*p2[F] >> p3[F] >>*/ >> Console[F].println("Done")

  val ctx = Ctx(Foo("foo"), Bar(123))

  //val effectful: IO[Unit] =
  //Ref.of[IO, Ctx](ctx).flatMap { ref =>
  //ref.runAsk { implicit ioCtxAsk =>
  //program[IO]
  //}
  //}

  val manual: IO[Unit] = {
    implicit val askIO: Ask[IO, Ctx] =
      new Ask[IO, Ctx] {
        override val applicative: Applicative[IO] = implicitly
        override def ask[E2 >: Ctx]: IO[E2]       = IO.pure(ctx)
      }
    program[IO]
  }

  def run: IO[Unit] =
    program[Kleisli[IO, Ctx, *]].run(ctx) >> manual
  //effectful >> manual

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
