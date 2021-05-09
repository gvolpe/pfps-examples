package examples.derivation

import cats._
import cats.syntax.all._
import io.estatico.newtype.macros.newtype

object ManualDerivation extends App {

  case class Person(age: Person.Age, name: Person.Name)

  object Person {
    @newtype case class Age(value: Int)
    object Age {
      implicit val eq: Eq[Age]       = deriving
      implicit val order: Order[Age] = deriving
      implicit val show: Show[Age]   = deriving
    }

    @newtype case class Name(value: String)
    object Name {
      implicit val eq: Eq[Name]       = deriving
      implicit val order: Order[Name] = deriving
      implicit val show: Show[Name]   = deriving
    }

    implicit val eq: Eq[Person] = Eq.and(Eq.by(_.age), Eq.by(_.name))

    implicit val order: Order[Person] = Order.by(_.name)

    implicit val show: Show[Person] =
      Show[String].contramap[Person] { p =>
        s"Name: ${p.name.show}, Age: ${p.age.show}"
      }
  }

  Order[Person]

  val p1 = Person(Person.Age(33), Person.Name("Gabriel"))
  val p2 = Person(Person.Age(29), Person.Name("Alicja"))

  println(p1 === p1)
  println(p1 === p2)
  println(p1 < p2)
  println(p1.show)

}
