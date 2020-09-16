package model.core

import common.TurnOwner
import common.TurnOwner.TurnOwner
import model.exception.CoinNotLaunchedException
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec

class TurnManagerTest extends AnyFlatSpec with MockFactory {

  behavior of "The TurnManager"

  it should "flip a coin when required and return the first TurnOwner" in {
    var turnOwner: TurnOwner = TurnManager.flipACoin()
    assert(TurnOwner.values.contains(turnOwner))
    turnOwner = TurnManager.flipACoin()
    assert(TurnOwner.values.contains(turnOwner))
  }
}
