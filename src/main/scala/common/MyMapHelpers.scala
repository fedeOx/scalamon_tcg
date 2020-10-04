package common

import scala.collection.mutable

object MyMapHelpers {
  implicit class MyImmutableMap[A, B](map: Map[A, B]) {
    def toMutableMap: mutable.Map[A, B] = mutable.Map(map.toSeq: _*)
  }
  implicit class MyMutableMap[A, B](map: mutable.Map[A, B]) {
    def toImmutableMap: Map[A, B] = Map(map.toSeq: _*)
  }
}
