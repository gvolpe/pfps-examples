package examples.concurrency

import scala.concurrent.duration._

import cats.effect.{ IO, IOApp }
import cats.syntax.all._
import fs2.Stream
import fs2.concurrent.SignallingRef

object Pausing extends IOApp.Simple {

  def run: IO[Unit] =
    Stream
      .eval(SignallingRef[IO, Boolean](false))
      .flatMap { signal =>
        val src =
          Stream
            .repeatEval(IO.println("ping"))
            .pauseWhen(signal)
            .metered(1.second)

        val pause =
          Stream
            .sleep[IO](3.seconds)
            .evalTap(_ => IO.println(">> Pausing stream <<"))
            .evalTap(_ => signal.set(true))

        val resume =
          Stream
            .sleep[IO](7.seconds)
            .evalTap(_ => IO.println(">> Resuming stream <<"))
            .evalTap(_ => signal.set(false))

        Stream(src, pause, resume).parJoinUnbounded
      }
      .interruptAfter(10.seconds)
      .onFinalize(IO.println("pong"))
      .compile
      .drain

}
