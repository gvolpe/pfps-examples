package examples.state

import scala.concurrent.duration._

import cats.effect._
import cats.effect.std.{ Semaphore, Supervisor }
import cats.effect.unsafe.implicits.global

object LeakyState extends IOApp.Simple {

  // global access
  lazy val sem: Semaphore[IO] =
    Semaphore[IO](1).unsafeRunSync()

  def launchMissiles: IO[Unit] =
    sem.permit.surround {
      IO.println("Launching missiles") >>
        IO.sleep(3.seconds) >>
        IO.println("Missiles launched, releasing lock")
    }

  def randomSleep: IO[Unit] =
    IO(scala.util.Random.nextInt(100)).flatMap { ms =>
      IO.sleep((ms + 700).millis)
    }.void

  def p1: IO[Unit] =
    sem.permit.surround(IO.println("Running P1")) >> randomSleep

  def p2: IO[Unit] =
    sem.permit.surround(IO.println("Running P2")) >> randomSleep

  def run: IO[Unit] =
    Supervisor[IO].use { s =>
      s.supervise(launchMissiles) *>
        s.supervise(p1.foreverM).void *>
        s.supervise(p2.foreverM).void *>
        IO.sleep(5.seconds).void
    }

}
