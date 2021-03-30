package examples.mtl

import cats._
import cats.data.Kleisli
import cats.effect._
import cats.effect.Console.implicits._
import cats.implicits._
import cats.mtl._
import cats.mtl.instances.all._
import com.olegpy.meow.effects._
import com.olegpy.meow.hierarchy._
import derevo.cats.show
import derevo.derive
import cats.effect.Ref

object MtlClassyDemo extends IOApp {
  import reader._

  def simple[F[_]: ApplicativeAsk[*[_], String]: Functor]: F[String] =
    ApplicativeAsk[F, String].ask.map(_ ++ " foo")

  def p1[F[_]: Console: FlatMap: HasCtx]: F[Unit] =
    F.ask.flatMap(ctx => F.putStrLn(ctx))

  def p2[F[_]: Console: FlatMap: HasFoo]: F[Unit] =
    F.ask.flatMap(foo => F.putStrLn(foo))

  def p3[F[_]: Console: FlatMap: HasBar]: F[Unit] =
    F.ask.flatMap(bar => F.putStrLn(bar))

  def program[F[_]: Console: FlatMap: HasCtx]: F[Unit] =
    p2[F] >> p3[F] >> F.putStrLn("Done")

  val ctx = Ctx(Foo("foo"), Bar(123))

  val effectful: IO[Unit] =
    Ref.of[IO, Ctx](ctx).flatMap { ref =>
      ref.runAsk { implicit ioCtxAsk =>
        program[IO]
      }
    }

  val manual: IO[Unit] = {
    implicit val askIO: ApplicativeAsk[IO, Ctx] =
      new DefaultApplicativeAsk[IO, Ctx] {
        override val applicative: Applicative[IO]  = implicitly
        override def ask: IO[Ctx]                  = IO.pure(ctx)
        override def reader[A](f: Ctx => A): IO[A] = ask.map(f)
      }
    program[IO]
  }

  def run(args: List[String]): IO[ExitCode] =
    program[Kleisli[IO, Ctx, *]].run(ctx) >>
      effectful >> manual.as(ExitCode.Success)

}

object reader {
  @derive(show)
  final case class Foo(value: String)

  @derive(show)
  final case class Bar(value: Int)

  @derive(show)
  final case class Ctx(foo: Foo, bar: Bar)

  type HasFoo[F[_]] = ApplicativeAsk[F, Foo]
  type HasBar[F[_]] = ApplicativeAsk[F, Bar]
  type HasCtx[F[_]] = ApplicativeAsk[F, Ctx]
}
