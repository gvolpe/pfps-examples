package examples

import cats.effect._
import cats.implicits._
import cats.effect.Console.io._
import scala.concurrent.duration._

object DoNotUseSeq extends IOApp {
  val inf: LazyList[Int] = 1 #:: inf.map(_ + 1)
  //val inf: Stream[Int] = 1 #:: inf.map(_ + 1)
  def api: IO[Seq[Int]] = IO.pure(inf)

  def usage: IO[Unit] =
    api
      .map(_.toList.foldLeft(0)((acc, n) => acc + n))
      .flatMap(putStrLn(_))

  def run(args: List[String]): IO[ExitCode] =
    usage.as(ExitCode.Success)

}
