package common

import common.CoinUtil.CoinValue.CoinValue

import scala.util.Random

object CoinUtil {

  object CoinValue extends Enumeration {
    type CoinValue = Value
    val Head: Value = Value("head")
    val Tail: Value = Value("tail")
  }

  def flipACoin(): CoinValue = new Random().nextInt(2) match {
    case n if n == 0 => CoinValue.Tail
    case _ => CoinValue.Head
  }

}
