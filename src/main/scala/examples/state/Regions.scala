package examples.state

import scala.concurrent.duration._

import cats.effect._
import cats.effect.std.{ Semaphore, Supervisor }

object Regions extends IOApp.Simple {

  val randomSleep: IO[Unit] =
    IO(scala.util.Random.nextInt(100)).flatMap { ms =>
      IO.sleep((ms + 700).millis)
    }.void

  def p1(sem: Semaphore[IO]): IO[Unit] =
    sem.permit.surround(IO.println("Running P1")) >> randomSleep

  def p2(sem: Semaphore[IO]): IO[Unit] =
    sem.permit.surround(IO.println("Running P2")) >> randomSleep

  def run: IO[Unit] =
    Supervisor[IO].use { s =>
      Semaphore[IO](1).flatMap { sem =>
        s.supervise(p1(sem).foreverM).void *>
          s.supervise(p2(sem).foreverM).void *>
          IO.sleep(5.seconds).void
      }
    }

}
