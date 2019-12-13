package examples.mtl

import cats._
import cats.effect._
import cats.effect.Console.implicits._
import cats.implicits._
import cats.mtl._
import org.manatki.derevo.catsInstances.show
import org.manatki.derevo.derive

object reader extends IOApp {
  def ask[F[_], A](implicit ev: ApplicativeAsk[F, A]): F[A] = ev.ask

  @derive(show)
  final case class Foo(value: String)

  @derive(show)
  final case class Bar(value: Int)

  @derive(show)
  final case class Ctx(foo: Foo, bar: Bar)

  type HasFoo[F[_]] = ApplicativeAsk[F, Foo]
  type HasBar[F[_]] = ApplicativeAsk[F, Bar]
  type HasCtx[F[_]] = ApplicativeAsk[F, Ctx]

  def program[F[_]: Console: HasCtx: Monad]: F[Unit] =
    ask[F, Ctx].flatMap(ctx => Console[F].putStrLn(ctx))

  val ctx = Ctx(Foo("foo"), Bar(123))

  implicit val askIO: ApplicativeAsk[IO, Ctx] =
    new DefaultApplicativeAsk[IO, Ctx] {
      override val applicative: Applicative[IO]  = implicitly
      override def ask: IO[Ctx]                  = IO.pure(ctx)
      override def reader[A](f: Ctx => A): IO[A] = ask.map(f)
    }

  def run(args: List[String]): IO[ExitCode] =
    program[IO].as(ExitCode.Success)

}
