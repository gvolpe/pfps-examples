package examples.streams

import cats.effect._
import cats.implicits._
import fs2._
import fs2.concurrent.SignallingRef
import java.time.Instant
import scala.concurrent.duration._
import scala.util.Random
import cats.effect.Deferred

object interruption extends IOApp {

  def putStrLn[A](a: A): IO[Unit] =
    IO(Instant.now()).flatMap { now =>
      IO(println(s"[$now] - $a"))
    }

  // interruptAfter example
  val p1 =
    Stream
      .emit[IO, String]("ping")
      .repeat
      .metered(1.second)
      .evalTap(putStrLn(_))
      .interruptAfter(3.seconds)
      .onComplete(Stream.eval(putStrLn("pong")))

  // interruptWhen example
  val p2 =
    Stream.eval(Deferred[IO, Either[Throwable, Unit]]).flatMap { promise =>
      Stream
        .eval(IO(Random.nextInt(5)))
        .repeat
        .metered(1.second)
        .evalTap(putStrLn(_))
        .evalTap {
          case 0 => promise.complete(().asRight)
          case _ => IO.unit
        }
        .interruptWhen(promise)
        .onComplete(Stream.eval(putStrLn("interrupted!")))
    }

  // signalling pause and interruption
  val p3 =
    Stream
      .eval(SignallingRef[IO, Boolean](false))
      .flatMap { signal =>
        val src =
          Stream
            .emit[IO, String]("ping")
            .repeat
            .metered(1.second)
            .evalTap(putStrLn(_))
            .pauseWhen(signal)

        val pause =
          Stream
            .sleep[IO](3.seconds)
            .evalTap(_ => putStrLn(">> Pausing stream <<"))
            .evalTap(_ => signal.set(true))

        val resume =
          Stream
            .sleep[IO](7.seconds)
            .evalTap(_ => putStrLn(">> Resuming stream <<"))
            .evalTap(_ => signal.set(false))

        Stream(src, pause, resume).parJoinUnbounded
      }
      .interruptAfter(10.seconds)
      .onComplete(Stream.eval(putStrLn("pong")))

  val s1 =
    Stream.sleep[IO](5.seconds) >> Stream.eval(putStrLn("done s1"))

  val s2 =
    Stream.random[IO].evalMap(putStrLn(_)).metered(1.second).interruptAfter(10.seconds)

  val p4 =
    Stream(s1, s2).parJoin(2)

  val p5 =
    Stream(s1, s2.concurrently(p1)).parJoin(2)

  def run(args: List[String]): IO[ExitCode] =
    p2.compile.drain.as(ExitCode.Success)

}
