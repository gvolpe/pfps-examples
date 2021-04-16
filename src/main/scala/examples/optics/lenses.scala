package examples.optics

import monocle.Lens
import monocle.macros.GenLens
import monocle.syntax.all._

object lenses extends App {

  case class StreetName(value: String)
  case class StreetNumber(value: Int)
  case class Address(streetName: StreetName, streetNumber: StreetNumber)

  case class PersonName(value: String)
  case class PersonAge(value: Int)

  case class Person(name: PersonName, age: PersonAge, address: Address)

  val _Address    = GenLens[Person](_.address)
  val _StreetName = GenLens[Address](_.streetName)

  val composedLens: Lens[Person, StreetName] =
    _Address.andThen(_StreetName)

  val person = Person(
    name = PersonName("Homer Simpson"),
    age = PersonAge(39),
    address = Address(
      streetName = StreetName("Evergreen Terrace"),
      streetNumber = StreetNumber(742)
    )
  )

  println("Lenses example using the new Focus API")

  println(person.focus(_.address).get)
  println(person.focus(_.address.streetName).get)
  println(person.focus(_.address.streetName).replace(StreetName("foo")))

  println("Lenses example using the classic encoding")

  println(_Address.get(person))                            // current address
  println(composedLens.get(person))                        // current street name
  println(composedLens.replace(StreetName("foo"))(person)) // person with new address

}
