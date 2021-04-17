package examples.patterns

import cats.effect.{ IO, IOApp }
import cats.syntax.all._
import cats.{ Applicative, Functor }
import monocle.Iso

import Api.Answer
import Proxy.Result

object BooleanBlindness extends IOApp.Simple {

  val boolApi = BoolApi.make[IO]
  val api     = Api.make[IO]
  val proxy   = Proxy.make(boolApi)

  def run: IO[Unit] =
    for {
      _ <- IO.println("<<< A boolean blindness example using ifM >>>")
      _ <- p1
      _ <- IO.println("<<< A clear Answer ADT >>>")
      _ <- p2
      _ <- IO.println("<<< A proxy over a Boolean API >>>")
      _ <- p3
      _ <- IO.println("<<< A typed filter for lists with clear intentions >>>")
      _ <- p4
      _ <- p5
    } yield ()

  val p1 =
    boolApi.get.ifM(IO.println("YES"), IO.println("NO"))

  val p2 = api.get.flatMap {
    case Answer.Yes => IO.println("YES!")
    case Answer.No  => IO.println("NO!")
  }

  val p3 =
    proxy.get.flatMap {
      case Result.Yes => IO.println("Yes, proxy")
      case Result.No  => IO.println("No, proxy")
    }

  import TypedFilter._

  val p4 = IO.println {
    List.range(1, 11).filterBy(n => if (n > 5) Pred.Keep else Pred.Discard)
  }

  val p5 = IO.println {
    List.range(1, 11).filterBy(n => if (n > 5) Pred.Discard else Pred.Keep)
  }

}

object TypedFilter {
  sealed trait Pred
  object Pred {
    case object Keep    extends Pred
    case object Discard extends Pred

    val _Bool: Iso[Boolean, Pred] =
      Iso[Boolean, Pred](if (_) Keep else Discard) {
        case Keep    => true
        case Discard => false
      }
  }

  implicit class ListOps[A](xs: List[A]) {
    def filterBy(p: A => Pred): List[A] =
      xs.filter(a => Pred._Bool.reverseGet(p(a)))
  }
}

trait Api[F[_]] {
  def get: F[Answer]
}

object Api {
  sealed trait Answer
  object Answer {
    case object Yes extends Answer
    case object No  extends Answer
  }

  def make[F[_]: Applicative]: Api[F] =
    new Api[F] {
      def get: F[Answer] = Answer.No.pure[F].widen
    }
}

trait Proxy[F[_]] {
  def get: F[Result]
}

object Proxy {
  sealed trait Result
  object Result {
    case object Yes extends Result
    case object No  extends Result

    val _Bool: Iso[Boolean, Result] =
      Iso[Boolean, Result](if (_) Yes else No) {
        case Yes => true
        case No  => false
      }
  }

  def make[F[_]: Functor](
      boolApi: BoolApi[F]
  ): Proxy[F] =
    new Proxy[F] {
      def get: F[Result] =
        boolApi.get.map(Result._Bool.get)
    }
}

trait BoolApi[F[_]] {
  def get: F[Boolean]
}

object BoolApi {
  def make[F[_]: Applicative]: BoolApi[F] =
    new BoolApi[F] {
      def get: F[Boolean] = true.pure[F]
    }
}
