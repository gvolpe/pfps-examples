package examples.optics

import monocle.Lens
import monocle.syntax.all._

object lenses extends App {

  case class StreetName(value: String)
  case class StreetNumber(value: Int)
  case class Address(streetName: StreetName, streetNumber: StreetNumber)

  case class PersonName(value: String)
  case class PersonAge(value: Int)

  case class Person(name: PersonName, age: PersonAge, address: Address)

  val addressLens: Lens[Person, Address] =
    Lens[Person, Address](_.address)(a => p => p.copy(address = a))

  val streetNameLens: Lens[Address, StreetName] =
    Lens[Address, StreetName](_.streetName)(s => a => a.copy(streetName = s))

  val composedLens: Lens[Person, StreetName] =
    addressLens.andThen(streetNameLens)

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

  println(addressLens.get(person))                         // current address
  println(composedLens.get(person))                        // current street name
  println(composedLens.replace(StreetName("foo"))(person)) // person with new address

}
