package meetup

import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._

object StateDemo extends IOApp {
  def putStrLn[A](a: A): IO[Unit] = IO(println(a))

  def run(args: List[String]): IO[ExitCode] =
    p2.as(ExitCode.Success)

  // ------- Sharing a Ref directly -------
  def incrByOne(ref: Ref[IO, Int]): IO[Unit] =
    putStrLn("Increasing counter by one") *>
      ref.update(_ + 1)

  def incrByTwo(ref: Ref[IO, Int]): IO[Unit] =
    putStrLn("Increasing counter by two") *>
      ref.update(_ + 2)

  val p1: IO[Unit] =
    Ref.of[IO, Int](0).flatMap { ref =>
      incrByOne(ref) >> incrByTwo(ref) >>
        // We can access the Ref and alter its state which may be undesirable
        ref.get.flatMap(n => if (n % 3 == 0) ref.set(100) else IO.unit) >>
        ref.get.flatMap(putStrLn)
    }

  // ------- Encapsulating Ref in a tagless algebra -------
  trait Counter[F[_]] {
    def incr: F[Unit]
    def get: F[Int]
  }

  object Counter {
    def make[F[_]: Sync]: F[Counter[F]] =
      Ref.of[F, Int](0).map { ref =>
        new Counter[F] {
          def incr: F[Unit] =
            ref.update(_ + 1)
          def get: F[Int] =
            ref.get
        }
      }
  }

  def incrByTen(counter: Counter[IO]): IO[Unit] =
    counter.incr.replicateA(10).void

  val p2: IO[Unit] =
    Counter.make[IO].flatMap { c =>
      // Sharing state only via the TF algebra
      c.incr >> incrByTen(c) >> c.get.flatMap(putStrLn)
    }
}
