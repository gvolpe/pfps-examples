package examples.optics

import monocle.Lens

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
    addressLens.composeLens(streetNameLens)

  println("Lenses example")

  val person = Person(
    name = PersonName("Homer Simpson"),
    age = PersonAge(39),
    address = Address(
      streetName = StreetName("Evergreen Terrace"),
      streetNumber = StreetNumber(742)
    )
  )

  println(addressLens.get(person))                     // current address
  println(composedLens.get(person))                    // current street name
  println(composedLens.set(StreetName("foo"))(person)) // person with new address

}
