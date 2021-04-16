package examples.mid

import cats._
import cats.data.NonEmptyList
import cats.effect.{ IO, IOApp, LiftIO }
import derevo.derive
import derevo.tagless.applyK
import tofu.higherKind.Mid
import tofu.syntax.monadic._

object MidTown extends IOApp.Simple {
  def run: IO[Unit] =
    UserStore
      .make(Metrics.make[IO], Logger.make[IO])
      .register("gvolpe")
      .flatMap { res =>
        IO.println(s"[MAIN] - Program ended with $res")
      }
}

trait Metrics[F[_]] {
  def timed[A](metricsKey: String)(f: F[A]): F[A]
}

object Metrics {
  def make[F[_]: FlatMap: LiftIO]: Metrics[F] =
    new Metrics[F] {
      def timed[A](metricsKey: String)(fa: F[A]): F[A] =
        IO.println(s"[METR] - Key: $metricsKey").to[F] >> fa
    }
}

trait Logger[F[_]] {
  def info(str: String): F[Unit]
}

object Logger {
  def make[F[_]: LiftIO]: Logger[F] =
    new Logger[F] {
      def info(str: String): F[Unit] =
        IO.println(s"[INFO] - $str").to[F]
    }
}

@derive(applyK)
trait UserStore[F[_]] {
  def register(username: String): F[Int]
}

object UserStore {
  def make[F[_]: Monad](
      metrics: Metrics[F],
      logger: Logger[F]
  ): UserStore[F] =
    NonEmptyList
      .of[UserStore[Mid[F, *]]](
        new UserLogger(logger),
        new UserMetrics(metrics)
      )
      .reduce
      .attach {
        new UserStore[F] {
          def register(username: String): F[Int] =
            username.length.pure[F]
        }
      }

  private final class UserLogger[F[_]: FlatMap](L: Logger[F]) extends UserStore[Mid[F, *]] {
    def register(username: String): Mid[F, Int] = fa =>
      L.info(s"Calling UserStore with username: $username") *> fa.flatTap { len =>
        L.info(s"UserStore returned $len")
      }
  }

  private final class UserMetrics[F[_]](M: Metrics[F]) extends UserStore[Mid[F, *]] {
    def register(username: String): Mid[F, Int] = fa => M.timed("timings.user")(fa)
  }
}
