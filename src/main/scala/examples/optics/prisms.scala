package examples.optics

import monocle.Prism

object prisms extends App {

  sealed trait Vehicle
  case object Car extends Vehicle
  case object Boat extends Vehicle

  type Car = Car.type

  val vPrism: Prism[Vehicle, Car] =
    Prism.partial[Vehicle, Car] { case c: Car => c }(identity)

  val a = vPrism(Car) // Car
  val b = vPrism.getOption(Boat) // None
  val c = vPrism.getOption(Car) // Some(Car)

  println("Prisms example")
}
