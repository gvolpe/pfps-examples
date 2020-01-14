package examples.streams

import cats.effect._
import cats.effect.concurrent.Deferred
import cats.implicits._
import fs2._
import fs2.concurrent.SignallingRef
import java.time.Instant
import scala.concurrent.duration._
import scala.util.Random

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

  def run(args: List[String]): IO[ExitCode] =
    p3.compile.drain.as(ExitCode.Success)

}
