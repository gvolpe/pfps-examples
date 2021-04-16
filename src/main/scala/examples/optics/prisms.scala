package examples.optics

import monocle.Prism
import monocle.macros.GenPrism
import monocle.std.option.some

object prisms extends App {

  sealed trait Vehicle
  object Vehicle {
    case object Car  extends Vehicle
    case object Boat extends Vehicle

    val __Car  = GenPrism[Vehicle, Car.type]
    val __Boat = GenPrism[Vehicle, Boat.type]
  }

  import Vehicle._

  println("Prisms example")

  println(__Car.getOption(Boat)) // None
  println(__Car.getOption(Car))  // Some(Car)

  val __StringInt: Prism[String, Int] =
    Prism[String, Int](_.toIntOption)(_.toString)

  val __OptStringInt: Prism[Option[String], Int] =
    some.andThen(__StringInt)

}
