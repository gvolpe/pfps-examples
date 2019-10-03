package examples

// TODO: Move this into an `examples` project

import cats.effect._
import cats.effect.concurrent.Semaphore
import cats.implicits._
import cats.effect.Console.io._
import scala.concurrent.duration._

object SemExample extends IOApp {

  def someExpensiveTask: IO[Unit] =
    IO.sleep(1.second) >> putStrLn("expensive task") >> someExpensiveTask

  def p1(sem: Semaphore[IO]): IO[Unit] =
    sem.withPermit(someExpensiveTask) >> p1(sem)

  def p2(sem: Semaphore[IO]): IO[Unit] =
    sem.withPermit(someExpensiveTask) >> p2(sem)

  def run(args: List[String]): IO[ExitCode] =
    Semaphore[IO](1).flatMap { sem =>
      p1(sem).start.void *>
        p2(sem).start.void
    } *> IO.never.as(ExitCode.Success)

}
