package examples.optics

import monocle.Prism

object prisms extends App {

  sealed trait Vehicle
  case object Car  extends Vehicle
  case object Boat extends Vehicle

  type Car = Car.type

  val vPrism: Prism[Vehicle, Car] =
    Prism.partial[Vehicle, Car] { case c: Car => c }(identity)

  val a = vPrism(Car)            // Car
  val b = vPrism.getOption(Boat) // None
  val c = vPrism.getOption(Car)  // Some(Car)

  println("Prisms example")

  val ps: Prism[Option[String], String] =
    Prism.partial[Option[String], String] { case Some(v) => v }(Option.apply)

  val pi: Prism[String, Int] =
    Prism.partial[String, Int] { case v if v.toIntOption.isDefined => v.toInt }(_.toString)

  val pp: Prism[Option[String], Int] =
    ps.composePrism(pi)

}
