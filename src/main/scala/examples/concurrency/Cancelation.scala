package examples.concurrency

import scala.concurrent.duration._

import cats.effect.kernel.{ Async, Deferred }
import cats.effect.std.{ Console, Random }
import cats.effect.{ IO, IOApp }
import cats.syntax.all._

object Cancelation extends IOApp.Simple {

  def run: IO[Unit] = simple

  val simple: IO[Unit] =
    Deferred[IO, Unit].flatMap { gate =>
      program(gate).background.surround {
        IO.sleep(500.millis) >> IO.println("Canceling fiber")
      }
    }

  val extended: IO[Unit] =
    (Deferred[IO, Unit], Random.scalaUtilRandom[IO]).tupled.flatMap { case (gate, rand) =>
      program(gate).start.bracket { _ =>
        rand.betweenInt(50, 500).flatMap { n =>
          val p1 = IO.sleep(400.millis) >> gate.complete(()) >> IO.println("Gate completed")
          val p2 = IO.sleep(n.millis) >> IO.println("Canceling fiber")
          IO.race(p1, p2).void
        }
      }(_.cancel)
    }

  def program[F[_]: Async: Console](
      gate: Deferred[F, Unit]
  ): F[Unit] =
    Async[F].uncancelable { poll =>
      Console[F].println("Waiting for gate") >> poll(gate.get)
    }

}
