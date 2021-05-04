package examples.concurrency

import scala.concurrent.duration._

import cats.effect.kernel.Async
import cats.effect.std.{ Console, Queue, Random }
import cats.effect.{ IO, IOApp }
import cats.syntax.all._
import fs2.Stream
import fs2.concurrent.Topic

object PubSub extends IOApp.Simple {

  def run: IO[Unit] = multiple

  val multiple: IO[Unit] =
    (Random.scalaUtilRandom[IO], Topic[IO, Int]).tupled.flatMap { case (random, topic) =>
      def consumer(id: Int) =
        topic
          .subscribe(10)
          .evalMap(n => IO.println(s"Consumer #$id got: $n"))
          .onFinalize(IO.println(s"Finalizing consumer #$id"))

      val producer =
        Stream
          .eval(random.betweenInt(1, 11))
          .evalMap(topic.publish1)
          .repeat
          .metered(1.second)
          .onFinalize(IO.println("Finalizing producer"))

      producer
        .concurrently(
          Stream(
            consumer(1),
            consumer(2),
            consumer(3)
          ).parJoin(3)
        )
        .interruptAfter(5.seconds)
        .onFinalize(IO.println("Interrupted"))
        .compile
        .drain
    }

  val ceVersion: IO[Unit] =
    Random.scalaUtilRandom[IO].flatMap { implicit random =>
      Queue.bounded[IO, Int](100).flatMap { q =>
        val producer = mkProducer(q)
        val consumer = mkConsumer(q)

        (producer, consumer).parTupled.void.timeoutTo(5.seconds, IO.println("Interrupted"))
      }
    }

  val fs2Version: IO[Unit] =
    Random.scalaUtilRandom[IO].flatMap { implicit random =>
      Queue.bounded[IO, Int](100).flatMap { q =>
        val consumer = Stream.eval(mkConsumer(q))
        val producer = Stream.eval(mkProducer(q))

        consumer
          .concurrently(producer)
          .interruptAfter(5.seconds)
          .onFinalize(IO.println("Interrupted"))
          .compile
          .drain
      }
    }

  val allInIO =
    (
      Random.scalaUtilRandom[IO],
      Queue.bounded[IO, Int](100)
    ).tupled.flatMap { case (random, q) =>
      val producer =
        random
          .betweenInt(1, 11)
          .flatMap(q.offer)
          .flatTap(_ => IO.sleep(1.second))
          .foreverM

      val consumer =
        q.take.flatMap { n =>
          IO.println(s"Consumed #$n")
        }.foreverM

      (producer, consumer).parTupled.void
        .timeoutTo(5.seconds, IO.println("Interrupted"))
    }

  def mkConsumer[F[_]: Async: Console](
      q: Queue[F, Int]
  ): F[Unit] =
    q.take.flatMap(n => Console[F].println(s"Consumed #$n")).foreverM

  def mkProducer[F[_]: Async: Random](
      q: Queue[F, Int]
  ): F[Unit] =
    (Random[F].betweenInt(1, 11).flatMap(q.offer) >> Async[F].sleep(1.second)).foreverM

}
