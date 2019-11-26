package examples.newtypes

import cats.effect._
import cats.implicits._
import io.estatico.newtype.macros._
import io.estatico.newtype.ops._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.collection.Contains
import eu.timepit.refined.types.string.NonEmptyString

object valueclasses {
  case class User(username: Username, email: Email)

  def lookup(username: Username, email: Email): IO[Option[User]] =
    IO.pure(User(username, email).some)

  case class Username private (val value: String) extends AnyVal
  case class Email private (val value: String) extends AnyVal

  def mkUsername(value: String): Option[Username] =
    if (value.nonEmpty) Username(value).some
    else none[Username]

  def mkEmail(value: String): Option[Email] =
    if (value.contains("@")) Email(value).some
    else none[Email]

  val foo =
    (
      mkUsername("aeinstein"),
      mkEmail("aeinstein@research.com")
    ).mapN {
      case (username, email) => lookup(username, email)
    }

  val bar =
    (mkUsername("aeinstein"), mkEmail("aeinstein@research.com")).mapN {
      case (username, email) =>
        lookup(username.copy(value = ""), email)
    }
}

object newts {
  @newtype case class Username(value: String)
  @newtype case class Email(value: String)

  val foo: Username = "gvolpe".coerce[Username]

  //val bar = foo.copy(value = "") // copy does not exist
}

object refinement {
  type Username = String Refined Contains['g']

  def foo(username: Username): String =
    username.value

  foo("gvolpe")
}

object refinednewts {
  @newtype case class Brand(value: NonEmptyString)

  val foo: Brand = ("a": NonEmptyString).coerce[Brand]
  val bar: Brand = Brand("a")
}
