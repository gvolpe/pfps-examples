package examples.patterns

object DoNotUseSeq extends App {

  val inf: LazyList[Int] = 1 #:: inf.map(_ + 1)
  //val inf: Stream[Int] = 1 #:: inf.map(_ + 1)

  println(inf.toList)

  // it won't be long until you get an OOM error

  /*

    [error] (run-main-0) java.lang.OutOfMemoryError: GC overhead limit exceeded
    [error] java.lang.OutOfMemoryError: GC overhead limit exceeded
    [error]         at java.lang.Integer.valueOf(Integer.java:832)
    [error]         at scala.runtime.java8.JFunction1$mcII$sp.apply(JFunction1$mcII$sp.scala:17)
    [error]         at scala.collection.immutable.LazyList.$anonfun$mapImpl$1(LazyList.scala:484)
    [error]         at scala.collection.immutable.LazyList$$Lambda$5728/710638503.apply(Unknown Source)

 */

}
