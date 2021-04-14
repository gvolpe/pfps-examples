package meetup

import cats.data.EitherNel
import cats.effect._
import cats.implicits._
import io.estatico.newtype.macros._
import eu.timepit.refined._
import eu.timepit.refined.api._
import eu.timepit.refined.auto._
import eu.timepit.refined.collection.{ Contains, NonEmpty }
import eu.timepit.refined.numeric.Greater
import eu.timepit.refined.types.string.NonEmptyString
import scala.util.control.NoStackTrace
import shapeless._

object TypesDemo extends IOApp.Simple {
  def putStrLn[A](a: A): IO[Unit] = IO(println(a))

  def showName(username: String, name: String, email: String): String =
    s"""
      Hi $name! Your username is $username
      and your email is $email.
     """

  def run: IO[Unit] = p9("", 4)

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
      UserNameP("jr"),
      NameP("Joe Reef"),
      EmailP("joe@bar.com")
    ).traverseN { case (u, n, e) =>
      putStrLn(showNameP(u, n, e))
    }.void

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

  case object EmptyError   extends NoStackTrace
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
        validate[UserName](u),
        validate[Name](n),
        validate[Email](e)
      ).parMapN(showNameTR)
    putStrLn(result)
  }

  //--------------- Auto unwrapping ----------------

  val p7: IO[Unit] =
    putStrLn(">>>>>>>> Unwrapping Newtype <<<<<<<<") >>
      putStrLn(AutoUnwrapping.raw)

  //--------------- Refined + Validated ----------------

  case class MyType(a: NonEmptyString, b: Int Refined Greater[5])

  def p8(a: String, b: Int): IO[Unit] = {
    val result =
      for {
        x <- refineV[NonEmpty](a)
        y <- refineV[Greater[5]](b)
      } yield MyType(x, y)
    putStrLn(result)
  }

  def p9(a: String, b: Int): IO[Unit] = {
    val result =
      (refineV[NonEmpty](a).toEitherNel, refineV[Greater[5]](b).toEitherNel)
        .parMapN(MyType.apply) // Validated conversion via Parallel
    putStrLn(result)
  }

  def p10(a: String, b: Int): IO[Unit] = {
    val result =
      (refineV[NonEmpty](a).toValidatedNel, refineV[Greater[5]](b).toValidatedNel)
        .mapN(MyType.apply)
    putStrLn(result)
  }

}

object types {
  // --- Value classes ---
  final case class UserNameV(value: String) extends AnyVal
  final case class NameV(value: String)     extends AnyVal
  final case class EmailV(value: String)    extends AnyVal

  // --- Sealed abstract case classes ---
  sealed abstract case class UserNameP(value: String)
  object UserNameP {
    def apply(value: String): Option[UserNameP] =
      if (value.nonEmpty) new UserNameP(value) {}.some else None
  }

  sealed abstract case class NameP(value: String)
  object NameP {
    def apply(value: String): Option[NameP] =
      if (value.nonEmpty) new NameP(value) {}.some else None
  }

  sealed abstract case class EmailP(value: String)
  object EmailP {
    def apply(value: String): Option[EmailP] =
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

  final class NewtypeRefinedPartiallyApplied[A] {
    def apply[T, P](raw: T)(implicit
        c: Coercible[Refined[T, P], A],
        v: Validate[T, P]
    ): EitherNel[String, A] =
      refineV[P](raw).toEitherNel.map(_.coerce[A])
  }

  def validate[A]: NewtypeRefinedPartiallyApplied[A] = new NewtypeRefinedPartiallyApplied[A]

}

object AutoUnwrapping {
  import eu.timepit.refined.types.numeric.PosInt

  @newsubtype(optimizeOps = false) case class Numer(value: PosInt)

  val n1 = Numer(87)

  val raw: Int = n1 // double unwrapping
}
