package examples.patterns

import cats.effect._
import cats.effect.std.Semaphore
import cats.implicits._
import scala.concurrent.duration._

object SemExample extends IOApp.Simple {

  def someExpensiveTask: IO[Unit] =
    IO.sleep(1.second) >> IO.println("expensive task") >> someExpensiveTask

  def p1(sem: Semaphore[IO]): IO[Unit] =
    sem.permit.use(_ => someExpensiveTask) >> p1(sem)

  def p2(sem: Semaphore[IO]): IO[Unit] =
    sem.permit.use(_ => someExpensiveTask) >> p2(sem)

  def run: IO[Unit] =
    Semaphore[IO](1).flatMap { sem =>
      p1(sem).start.void *>
        p2(sem).start.void
    } *> IO.never.void

}
