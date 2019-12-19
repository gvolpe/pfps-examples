package examples.mtl

import cats.effect._
import cats.effect.Console.implicits._
import cats.effect.concurrent.Ref
import cats.implicits._
import cats.mtl._
import com.olegpy.meow.effects._
import com.olegpy.meow.hierarchy._
import org.manatki.derevo.catsInstances.show
import org.manatki.derevo.derive

object StateDemo extends IOApp {

  @derive(show)
  case class FooState(value: String) extends AnyVal

  import com.olegpy.meow.prelude._ // Use Monad instance from MonadState

  def mtlProgram[F[_]: Console](implicit M: MonadState[F, FooState]): F[Unit] =
    for {
      current <- M.get
      _ <- Console[F].putStrLn(current)
      _ <- M.set(FooState("foo"))
      updated <- M.get
      _ <- Console[F].putStrLn(updated)
    } yield ()

  val program = Ref.of[IO, FooState](FooState("bar")).flatMap { ref =>
    ref.runState { implicit ms =>
      mtlProgram[IO]
    }
  }

  def run(args: List[String]): IO[ExitCode] =
    program.as(ExitCode.Success)

}
