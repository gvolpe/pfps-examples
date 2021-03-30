package examples.streams

import cats.FlatMap
import cats.effect._
import cats.effect.Console.io._
import cats.effect.Console.implicits._
import cats.implicits._
import scala.concurrent.duration._
import cats.effect.Temporal

object BgApp extends IOApp {

  def program[F[_]: Background: Console: FlatMap: Temporal]: F[Unit] =
    F.schedule(F.putStrLn("foo"), 3.seconds) >> F.sleep(1.second) >> program

  /*
   * It keeps on running the spawned fibers even when interrupted
   *
   * Prints out "foo" 3 times and "Done", and then it prints out "foo"
   * twice with the program already finalized.
   */
  val p1: IO[Unit] = {
    implicit val bg = Background.simple[IO]
    program[IO].timeoutTo(5.seconds, putStrLn("Done"))
  }

  /*
   * It properly cancels spawned fibers when interrupted
   *
   * Prints out "foo" 3 times and "Done".
   */
  val p2: IO[Unit] =
    Background
      .resource[IO] // or `manualResource`
      .use { implicit bg =>
        program[IO].timeoutTo(5.seconds, putStrLn("Done"))
      }

  def run(args: List[String]): IO[ExitCode] =
    p1.as(ExitCode.Success)

}
