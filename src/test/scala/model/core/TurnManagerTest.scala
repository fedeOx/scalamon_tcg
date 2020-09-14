package model.core

import common.{Observer, TurnOwner}
import common.TurnOwner.TurnOwner
import controller.Controller
import model.event.Events.Event
import model.event.Events.Event.{NextTurn, ShowDeckCards}
import model.exception.CoinNotLaunchedException
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
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
