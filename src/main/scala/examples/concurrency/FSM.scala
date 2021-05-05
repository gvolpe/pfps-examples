package examples.concurrency

import cats.effect.{ IO, IOApp }
import cats.syntax.all._
import cats.{ Id, Show }
import fs2.Stream

case class FSM[F[_], S, I, O](run: (S, I) => F[(S, O)])

object FSM {
  def id[S, I, O](run: (S, I) => Id[(S, O)]) = FSM(run)
}

sealed trait Gem
object Gem {
  case object Diamond  extends Gem
  case object Emerald  extends Gem
  case object Ruby     extends Gem
  case object Sapphire extends Gem

  val all: List[Gem] =
    List(Diamond, Emerald, Ruby, Sapphire)
}

object FSMApp extends IOApp.Simple {

  type State  = Map[Gem, Int]
  type Result = String

  implicit val showState: Show[State] =
    new Show[State] {
      def show(t: State): String =
        s"""
         |Gem -> Count \n
         |------------\n
         |${t.toList.sortBy(_._2).reverse.map { case (g, c) => s"$g: $c" }.mkString("\n")}
        """.stripMargin
    }

  val source: Stream[IO, Gem] =
    Stream.emits(
      Gem.all ++ List(Gem.Diamond, Gem.Ruby, Gem.Diamond)
    )

  val initial: State =
    Gem.all.map(_ -> 0).toMap

  val fsm: FSM[Id, State, Gem, Result] =
    FSM.id { case (m, g) =>
      val out = m.updatedWith(g)(_.map(_ + 1))
      out -> out.show
    }

  def run: IO[Unit] =
    source
      .mapAccumulate(initial)(fsm.run)
      .map(_._2)
      .lastOr("No results")
      .evalMap(IO.println)
      .compile
      .drain

}
