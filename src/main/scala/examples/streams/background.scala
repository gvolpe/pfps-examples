package examples.streams

import cats.effect._
import cats.effect.implicits._
import cats.implicits._
import fs2._
import fs2.concurrent.Queue
import scala.concurrent.duration.FiniteDuration
import cats.effect.Temporal

trait Background[F[_]] {
  def schedule[A](
      fa: F[A],
      duration: FiniteDuration
  ): F[Unit]
}

object Background {
  def apply[F[_]](implicit ev: Background[F]): Background[F] = ev

  def simple[F[_]: Concurrent: Temporal]: Background[F] =
    new Background[F] {

      def schedule[A](
          fa: F[A],
          duration: FiniteDuration
      ): F[Unit] =
        (Temporal[F].sleep(duration) *> fa).start.void

    }

  def resource[F[_]: Concurrent: Temporal]: Resource[F, Background[F]] =
    Stream
      .eval(Queue.unbounded[F, (FiniteDuration, F[Any])])
      .flatMap { q =>
        val bg = new Background[F] {
          def schedule[A](fa: F[A], duration: FiniteDuration): F[Unit] =
            q.enqueue1(duration -> fa.widen)
        }

        val process = q.dequeue.map {
          case (duration, fa) =>
            Stream.eval_(fa.attempt).delayBy(duration)
        }.parJoinUnbounded

        Stream.emit(bg).concurrently(process)
      }
      .compile
      .resource
      .lastOrError

  def manualResource[F[_]: Concurrent: Temporal]: Resource[F, Background[F]] =
    Resource.suspend(
      Queue.unbounded[F, (FiniteDuration, F[Any])].map { q =>
        val bg = new Background[F] {
          def schedule[A](fa: F[A], duration: FiniteDuration): F[Unit] =
            q.enqueue1(duration -> fa.widen)
        }

        val bgStream = q.dequeue.map {
          case (duration, fa) =>
            Stream.eval_(fa.attempt).delayBy(duration)
        }.parJoinUnbounded

        Resource
          .make(bgStream.compile.drain.start)(_.cancel)
          .as(bg)
      }
    )

}
