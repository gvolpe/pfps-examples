package examples.state

import cats.Functor
import cats.effect.kernel.Ref
import cats.effect.{ IO, IOApp }
import cats.syntax.all._

object CounterApp extends IOApp.Simple {

  def run: IO[Unit] =
    Counter.make[IO].flatMap { c =>
      for {
        _ <- c.get.flatMap(IO.println)
        _ <- c.incr
        _ <- c.get.flatMap(IO.println)
        _ <- c.incr.replicateA(5).void
        _ <- c.get.flatMap(IO.println)
      } yield ()
    }

}

trait Counter[F[_]] {
  def incr: F[Unit]
  def get: F[Int]
}

object Counter {
  def make[F[_]: Functor: Ref.Make]: F[Counter[F]] =
    Ref.of[F, Int](0).map { ref =>
      new Counter[F] {
        def incr: F[Unit] = ref.update(_ + 1)
        def get: F[Int]   = ref.get
      }
    }
}
