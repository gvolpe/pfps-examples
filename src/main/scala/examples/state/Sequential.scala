package examples.state

import cats.data._
import cats.effect._

object Sequential extends IOApp.Simple {

  val nextInt: State[Int, Int] =
    State(s => (s + 1, s * 2))

  def seq: State[Int, Int] =
    for {
      n1 <- nextInt
      n2 <- nextInt
      n3 <- nextInt
    } yield n1 + n2 + n3

  val ioNextInt: StateT[IO, Int, Int] =
    StateT(s => IO.pure(s + 1 -> s * 2))

  val ioa: StateT[IO, Int, Int] =
    for {
      n1 <- ioNextInt
      n2 <- ioNextInt
      n3 <- ioNextInt
    } yield n1 + n2 + n3

  def run: IO[Unit] =
    ioa.run(1).flatMap(IO.println).start.void

}
