package examples.validation

import scala.util.control.NoStackTrace

import cats.data.{ EitherNel, ValidatedNel }
import cats.effect._
import cats.implicits._
import eu.timepit.refined._
import eu.timepit.refined.api._
import eu.timepit.refined.auto._
import eu.timepit.refined.collection.Contains
import eu.timepit.refined.numeric.Greater
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros._
import shapeless._

object RuntimeValidation extends IOApp.Simple {

  def showName(username: String, name: String, email: String): String =
    s"""
      Hi $name! Your username is $username
      and your email is $email.
     """

  def run: IO[Unit] = p9("", 4)

  val p0: IO[Unit] =
    IO.println(showName("gvolpe@github.com", "12345", "foo"))

  // ----------------- Value classes -------------------
  import types._

  def showNameV(username: UserNameV, name: NameV, email: EmailV): String =
    s"""
      Hi ${name.value}! Your username is ${username.value}
      and your email is ${email.value}.
     """

  val badUserName = UserNameV("gvolpe@github.com")

  val p1: IO[Unit] =
    IO.println(
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
      IO.println(showNameP(u, n, e))
    }.void

  // ----------------- Newtypes -------------------

  def showNameT(username: UserNameT, name: NameT, email: EmailT): String =
    s"""
      Hi ${name.value}! Your username is ${username.value}
      and your email is ${email.value}.
     """

  val p3: IO[Unit] =
    IO.println(
      showNameT(
        UserNameT("gvolpe@github.com"),
        NameT("12345"),
        EmailT("")
      )
    )

  // ----------------- Smart Constructors -------------------

  def mkUsername(value: String): Option[UserNameT] =
    (value.nonEmpty).guard[Option].as(UserNameT(value))

  def mkName(value: String): Option[NameT] =
    (value.nonEmpty).guard[Option].as(NameT(value))

  def mkEmail(value: String): Option[EmailT] =
    (value.contains("@")).guard[Option].as(EmailT(value))

  case object EmptyError   extends NoStackTrace
  case object InvalidEmail extends NoStackTrace

  val p4: IO[Unit] =
    (
      mkUsername("gvolpe").liftTo[IO](EmptyError),
      mkName("George").liftTo[IO](EmptyError),
      mkEmail("123").liftTo[IO](InvalidEmail)
    ).parMapN(showNameT)
      .flatMap(IO.println)

  // ----------------- Refinement Types -------------------

  def showNameR(username: UserNameR, name: NameR, email: EmailR): String =
    s"""
      Hi ${name.value}! Your username is ${username.value}
      and your email is ${email.value}.
     """

  val p5: IO[Unit] =
    IO.println(
      showNameR("jr", "Joe", "jr@gmail.com")
    )

  // ----------------- Newtypes + Refined -------------------

  def showNameTR(username: UserName, name: Name, email: Email): String =
    s"""
      Hi ${name.value}! Your username is ${username.value}
      and your email is ${email.value}.
     """

  val p6: IO[Unit] =
    IO.println(
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
    IO.println(result)
  }

  //--------------- Auto unwrapping ----------------

  val p7: IO[Unit] =
    IO.println(">>>>>>>> Unwrapping Newtype <<<<<<<<") >>
      IO.println(AutoUnwrapping.raw)

  //--------------- Refined + Validated ----------------

  type GTFive = Int Refined Greater[5]
  object GTFive extends RefinedTypeOps[GTFive, Int]

  case class MyType(a: NonEmptyString, b: GTFive)

  def p8(a: String, b: Int): IO[Unit] = {
    val result =
      for {
        x <- NonEmptyString.from(a) // refineV[NonEmpty](a)
        y <- GTFive.from(b) // refineV[Greater[5]](b)
      } yield MyType(x, y)
    IO.println(result)
  }

  def p9(a: String, b: Int): IO[Unit] = {
    val result: EitherNel[String, MyType] =
      (NonEmptyString.from(a).toEitherNel, GTFive.from(b).toEitherNel)
        .parMapN(MyType.apply) // Validated conversion via Parallel
    IO.println(result)
  }

  def p10(a: String, b: Int): IO[Unit] = {
    val result: ValidatedNel[String, MyType] =
      (NonEmptyString.from(a).toValidatedNel, GTFive.from(b).toValidatedNel)
        .mapN(MyType.apply)
    IO.println(result)
  }

  case class Person(
      username: UserName,
      name: Name,
      email: Email
  )

  def p11(u: String, n: String, e: String): IO[Unit] = {
    val result: EitherNel[String, Person] =
      (
        UserNameR.from(u).toEitherNel.map(UserName.apply),
        NameR.from(n).toEitherNel.map(Name.apply),
        EmailR.from(e).toEitherNel.map(Email.apply)
      ).parMapN(Person.apply)
    IO.println(result)
  }

  def p12(u: String, n: String, e: String): IO[Unit] = {
    import NewtypeRefinedOps._
    val result: EitherNel[String, Person] =
      (
        u.as[UserName].validate,
        n.as[Name].validate,
        e.as[Email].validate
      ).parMapN(Person.apply)
    IO.println(result)
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
      (value.nonEmpty).guard[Option].as(new UserNameP(value) {})
  }

  sealed abstract case class NameP(value: String)
  object NameP {
    def apply(value: String): Option[NameP] =
      (value.nonEmpty).guard[Option].as(new NameP(value) {})
  }

  sealed abstract case class EmailP(value: String)
  object EmailP {
    def apply(value: String): Option[EmailP] =
      (value.contains("@")).guard[Option].as(new EmailP(value) {})
  }

  // --- Newtypes ---
  @newtype case class UserNameT(value: String)
  @newtype case class NameT(value: String)
  @newtype case class EmailT(value: String)

  // --- Refinement types ---
  type UserNameR = NonEmptyString
  object UserNameR extends RefinedTypeOps[UserNameR, String]

  type NameR = NonEmptyString
  object NameR extends RefinedTypeOps[NameR, String]

  type EmailR = String Refined Contains['@']
  object EmailR extends RefinedTypeOps[EmailR, String]

  // --- Newtypes + Refinement types ---
  @newtype case class UserName(value: UserNameR)
  @newtype case class Name(value: NameR)
  @newtype case class Email(value: EmailR)

}

object NewtypeRefinedOps {
  import io.estatico.newtype.Coercible
  import io.estatico.newtype.ops._

  final class NewtypePartiallyApplied[A, T](raw: T) {
    def validate[P](implicit
        c: Coercible[Refined[T, P], A],
        v: Validate[T, P]
    ): EitherNel[String, A] =
      refineV[P](raw).toEitherNel.map(_.coerce[A])
  }

  implicit class NewtypeOps[T](raw: T) {
    def as[A]: NewtypePartiallyApplied[A, T] = new NewtypePartiallyApplied[A, T](raw)
  }

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
