package examples.streams

import scala.concurrent.duration._

import cats.effect.kernel.{ Resource, Temporal }
import cats.effect.std.Queue
import cats.effect.syntax.spawn._
import cats.syntax.all._
import fs2._

object StreamResource {

  def make[F[_]: Temporal]: Resource[F, Background[F]] =
    Resource.suspend(
      Queue.unbounded[F, (FiniteDuration, F[Any])].map { q =>
        val bg = new Background[F] {
          def schedule[A](
              fa: F[A],
              duration: FiniteDuration
          ): F[Unit] =
            q.offer(duration -> fa.widen)
        }

        q.take
          .flatMap { case (duration, fa) =>
            fa.attempt >> Temporal[F].sleep(duration)
          }
          .background
          .as(bg)
      }
    )

  def resource[F[_]: Temporal]: Resource[F, Background[F]] =
    Stream
      .eval(Queue.unbounded[F, (FiniteDuration, F[Any])])
      .flatMap { q =>
        val bg = new Background[F] {
          def schedule[A](
              fa: F[A],
              duration: FiniteDuration
          ): F[Unit] =
            q.offer(duration -> fa.widen)
        }

        val process =
          Stream
            .repeatEval(q.take)
            .map { case (duration, fa) =>
              Stream.eval(fa.attempt).drain.delayBy(duration)
            }
            .parJoinUnbounded

        Stream.emit(bg).concurrently(process)
      }
      .compile
      .resource
      .lastOrError

}

trait Background[F[_]] {
  def schedule[A](fa: F[A], duration: FiniteDuration): F[Unit]
}
