package examples.state

import java.time.{ Instant, ZoneOffset }

import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.effect.{ IO, IOApp }
import cats.syntax.all._
import cats.{ Applicative, Monad }
import eu.timepit.refined.auto._
import eu.timepit.refined.types.numeric.NonNegInt
import io.estatico.newtype.macros.newtype

object Tagless extends IOApp.Simple {

  def run: IO[Unit] = //testIncrBy10 >> testNoIncr
    Counter.make[IO].flatMap { c =>
      tagless(c) >>= IO.println
    }

  def testIncrBy10: IO[Unit] = {
    implicit val time: Time[IO] = Time.of[IO](Instant.parse("2021-05-08T12:52:54.966933505Z"))
    implicit val log: Log[IO]   = Log.noop[IO]

    for {
      c <- Counter.make[IO]
      p <- tagless[IO](c)
    } yield {
      assert(p === 10, "Expected result === 10")
    }
  }

  def testNoIncr: IO[Unit] = {
    implicit val time: Time[IO] = Time.of[IO](Instant.parse("2021-05-08T08:52:54.966933505Z"))
    implicit val log: Log[IO]   = Log.noop[IO]

    for {
      c <- Counter.make[IO]
      p <- tagless[IO](c)
    } yield {
      assert(p === 0, "Expected result === 0")
    }
  }

  def concrete(c: Counter[IO]): IO[Int] =
    for {
      x <- c.get
      _ <- IO.println(s"Current count: $x")
      t <- IO(Instant.now().atZone(ZoneOffset.UTC).getHour())
      _ <- IO.println(s"Current hour: $t")
      _ <- c.incr.replicateA(10).void.whenA(t >= 12)
      y <- c.get
    } yield y

  def tagless[F[_]: Log: Monad: Time](c: Counter[F]): F[Int] =
    for {
      x <- c.get
      _ <- Log[F].info(s"Current count: $x")
      t <- Time[F].getHour
      _ <- Log[F].info(s"Current hour: $t")
      _ <- c.incr.replicateA(10).void.whenA(t.int >= 12)
      y <- c.get
    } yield y

}

trait Time[F[_]] {
  def getHour: F[Time.Hour]
}

object Time {
  @newtype case class Hour(int: NonNegInt)

  object Hour {
    def from(instant: Instant): Hour =
      Hour(NonNegInt.unsafeFrom(instant.atZone(ZoneOffset.UTC).getHour()))
  }

  def apply[F[_]: Time]: Time[F] = implicitly

  def of[F[_]: Applicative](instant: Instant): Time[F] =
    new Time[F] {
      def getHour: F[Hour] =
        Hour.from(instant).pure[F]
    }

  implicit def forSync[F[_]: Sync]: Time[F] =
    new Time[F] {
      def getHour: F[Hour] = Sync[F].delay {
        Hour.from(Instant.now())
      }
    }
}

trait Log[F[_]] {
  def info(str: String): F[Unit]
}

object Log {
  def apply[F[_]: Log]: Log[F] = implicitly

  def noop[F[_]: Applicative]: Log[F] =
    new Log[F] {
      def info(str: String): F[Unit] = Applicative[F].unit
    }

  implicit def forConsole[F[_]: Console]: Log[F] =
    new Log[F] {
      def info(str: String): F[Unit] =
        Console[F].println(str)
    }
}
