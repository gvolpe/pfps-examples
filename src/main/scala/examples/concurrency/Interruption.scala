package examples.concurrency

import scala.concurrent.duration._
import scala.util.Random

import cats.effect.kernel.Deferred
import cats.effect.{ IO, IOApp }
import cats.syntax.all._
import fs2.Stream

object Interruption extends IOApp.Simple {

  def run: IO[Unit] = coordinated.compile.drain

  val pingPong: Stream[IO, Unit] =
    Stream
      .repeatEval(IO.println("ping"))
      .metered(1.second)
      .interruptAfter(3.seconds)
      .onFinalize(IO.println("pong"))

  val coordinated: Stream[IO, Unit] =
    Stream
      .eval(Deferred[IO, Either[Throwable, Unit]])
      .flatMap { switch =>
        Stream
          .repeatEval(IO(Random.nextInt(5)))
          .metered(1.second)
          .evalTap(IO.println)
          .evalTap { n =>
            switch.complete(().asRight).void.whenA(n == 0)
          }
          .interruptWhen(switch)
          .onFinalize(IO.println("Interrupted!"))
      }
      .void

}
