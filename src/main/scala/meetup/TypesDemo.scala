package meetup

import cats.data.ValidatedNel
import cats.effect._
import cats.implicits._
import io.estatico.newtype.macros._
import eu.timepit.refined._
import eu.timepit.refined.api._
import eu.timepit.refined.auto._
import eu.timepit.refined.collection.{ Contains, NonEmpty }
import eu.timepit.refined.types.string.NonEmptyString
import scala.util.control.NoStackTrace
import shapeless._

object TypesDemo extends IOApp {
  def putStrLn[A](a: A): IO[Unit] = IO(println(a))

  def showName(username: String, name: String, email: String): String =
    s"""
      Hi $name! Your username is $username
      and your email is $email.
     """

  val p0: IO[Unit] =
    putStrLn(showName("gvolpe@github.com", "12345", "foo"))

  // ----------------- Value classes -------------------
  import types._

  def showNameV(username: UserNameV, name: NameV, email: EmailV): String =
    s"""
      Hi ${name.value}! Your username is ${username.value}
      and your email is ${email.value}.
     """

  val badUserName = UserNameV("gvolpe@github.com")

  val p1: IO[Unit] =
    putStrLn(
      showNameV(
        badUserName.copy(value = ""),
        NameV("12345"),
        EmailV("foo")
      )
    )

  // ----------------- Sealed abstract case classes -------------------

  def showNameP(username: UserNameP, name: NameP, email: EmailP): String =
    s"""
      Hi ${name.value}! Your username is ${username.value}
      and your email is ${email.value}.
     """

  //new UserNameP("jr") {} // this only works because it's in the same file
  // NameP("123") // does not compile

  val p2: IO[Unit] =
    (
      UserNameP.make("jr"),
      NameP.make("Joe Reef"),
      EmailP.make("joe@bar.com")
    ).tupled.fold(IO.unit) {
      case (u, n, e) => putStrLn(showNameP(u, n, e))
    }

  // ----------------- Newtypes -------------------

  def showNameT(username: UserNameT, name: NameT, email: EmailT): String =
    s"""
      Hi ${name.value}! Your username is ${username.value}
      and your email is ${email.value}.
     """

  val p3: IO[Unit] =
    putStrLn(
      showNameT(
        UserNameT("gvolpe@github.com"),
        NameT("12345"),
        EmailT("")
      )
    )

  // ----------------- Smart Constructors -------------------

  def mkUsername(value: String): Option[UserNameT] =
    if (value.nonEmpty) UserNameT(value).some else None

  def mkName(value: String): Option[NameT] =
    if (value.nonEmpty) NameT(value).some else None

  def mkEmail(value: String): Option[EmailT] =
    if (value.contains("@")) EmailT(value).some else None

  case object EmptyError extends NoStackTrace
  case object InvalidEmail extends NoStackTrace

  val p4: IO[Unit] =
    (
      mkUsername("gvolpe").liftTo[IO](EmptyError),
      mkName("George").liftTo[IO](EmptyError),
      mkEmail("123").liftTo[IO](InvalidEmail)
    ).parMapN(showNameT)
      .flatMap(putStrLn)

  // ----------------- Refinement Types -------------------

  def showNameR(username: UserNameR, name: NameR, email: EmailR): String =
    s"""
      Hi ${name.value}! Your username is ${username.value}
      and your email is ${email.value}.
     """

  val p5: IO[Unit] =
    putStrLn(
      showNameR("jr", "Joe", "jr@gmail.com")
    )

  // ----------------- Newtypes + Refined -------------------

  def showNameTR(username: UserName, name: Name, email: Email): String =
    s"""
      Hi ${name.value}! Your username is ${username.value}
      and your email is ${email.value}.
     """

  val p6: IO[Unit] =
    putStrLn(
      showNameTR(
        UserName("jr"),
        Name("John"),
        Email("foo@bar.com")
      )
    )

  def c6(u: String, n: String, e: String): IO[Unit] = {
    import NewtypeRefinedOps._
    val result =
      (
        validate[UserName, NonEmpty](u),
        validate[Name, NonEmpty](n),
        validate[Email, Contains['@']](e)
      ).mapN(showNameTR)
    putStrLn(result)
  }

  //--------------- Auto unwrapping ----------------

  val p7: IO[Unit] =
    putStrLn(">>>>>>>> Unwrapping Newtype <<<<<<<<") >>
        putStrLn(AutoUnwrapping.raw)

  //--------------- Refined + Validated ----------------

  case class MyType(a: NonEmptyString, b: NonEmptyString)

  def p8(a: String, b: String): IO[Unit] = {
    val result =
      for {
        x <- refineV[NonEmpty](a)
        y <- refineV[NonEmpty](b)
      } yield MyType(x, y)
    putStrLn(result)
  }

  def p9(a: String, b: String): IO[Unit] = {
    val result =
      (refineV[NonEmpty](a), refineV[NonEmpty](b))
        .parMapN(MyType.apply) // Validated conversion via Parallel
    putStrLn(result)
  }

  def p10(a: String, b: String): IO[Unit] = {
    val result =
      (refineV[NonEmpty](a).toValidatedNel, refineV[NonEmpty](b).toValidatedNel)
        .mapN(MyType.apply)
    putStrLn(result)
  }

  def run(args: List[String]): IO[ExitCode] =
    c6("", "", "foo").as(ExitCode.Success)
}

object types {
  // --- Value classes ---
  final case class UserNameV(value: String) extends AnyVal
  final case class NameV(value: String) extends AnyVal
  final case class EmailV(value: String) extends AnyVal

  // --- Sealed abstract case classes ---
  sealed abstract case class UserNameP(value: String)
  object UserNameP {
    def make(value: String): Option[UserNameP] =
      if (value.nonEmpty) new UserNameP(value) {}.some else None
  }

  sealed abstract case class NameP(value: String)
  object NameP {
    def make(value: String): Option[NameP] =
      if (value.nonEmpty) new NameP(value) {}.some else None
  }

  sealed abstract case class EmailP(value: String)
  object EmailP {
    def make(value: String): Option[EmailP] =
      if (value.contains("@")) new EmailP(value) {}.some else None
  }

  // --- Newtypes ---
  @newtype case class UserNameT(value: String)
  @newtype case class NameT(value: String)
  @newtype case class EmailT(value: String)

  // --- Refinement types ---
  type UserNameR = NonEmptyString
  type NameR     = NonEmptyString
  type EmailR    = String Refined Contains['@']

  // --- Newtypes + Refinement types ---
  @newtype case class UserName(value: NonEmptyString)
  @newtype case class Name(value: NonEmptyString)
  @newtype case class Email(value: String Refined Contains['@'])

}

object NewtypeRefinedOps {
  import io.estatico.newtype.Coercible
  import io.estatico.newtype.ops._

  final class NewtypeRefinedPartiallyApplied[A, P] {
    def apply[T](raw: T)(
        implicit v: Validate[T, P],
        c: Coercible[Refined[T, P], A]
    ): ValidatedNel[String, A] =
      refineV[P](raw).toValidatedNel.map(_.coerce[A])
  }

  def validate[A, P]: NewtypeRefinedPartiallyApplied[A, P] = new NewtypeRefinedPartiallyApplied[A, P]

}

object AutoUnwrapping {
  import io.estatico.newtype.Coercible
  import io.estatico.newtype.ops._
  import types._
  //implicit def autoUnwrap[F[_, _], T, P](tp: F[T, P])(implicit rt: RefType[F]): T =

  // Doesn't work yet -_-
  implicit def autoUnwrapNewtypeOfRefined[F[_, _]: RefType, T, P, A: Coercible[F[T, P], *]](a: A): T =
    autoUnwrap[F, T, P](a.repr.asInstanceOf[F[T, P]])

  //implicit def autoUnwrapNewtype[A: Coercible[B, *], B](a: A): B =
  //  a.repr

  val u1 = UserName("gvolpe")
  val u2 = UserNameT("jconway")

  val raw: NonEmptyString = u1.value

}
