package examples.mtl

import cats.data.StateT
import cats.effect._
import cats.effect.Console.implicits._
import cats.effect.concurrent.Ref
import cats.implicits._
import cats.mtl._
import cats.mtl.instances.all._
import com.olegpy.meow.effects._
import com.olegpy.meow.hierarchy._
import org.manatki.derevo.catsInstances.show
import org.manatki.derevo.derive

object StateDemo extends IOApp {

  @derive(show)
  case class FooState(value: String) extends AnyVal

  import com.olegpy.meow.prelude._ // Use Monad instance from MonadState

  def inspection[F[_]](implicit C: Console[F], M: MonadState[F, FooState]): F[Unit] =
    M.inspect(st => s"prefix:$st").flatMap(C.putStrLn(_)) >>
        M.get.flatMap(C.putStrLn(_))

  def program[F[_]](implicit C: Console[F], M: MonadState[F, FooState]): F[Unit] =
    for {
      current <- M.get
      _ <- C.putStrLn(current)
      _ <- M.set(FooState("foo"))
      updated <- M.get
      _ <- C.putStrLn(updated)
    } yield ()

  val p1: IO[Unit] =
    program[StateT[IO, FooState, *]].run(FooState("bar")).void

  val p2: IO[Unit] =
    Ref.of[IO, FooState](FooState("bar")).flatMap { ref =>
      ref.runState { implicit ms =>
        program[IO]
      }
    }

  def run(args: List[String]): IO[ExitCode] =
    (p1 >> p2).as(ExitCode.Success)

}
