package model.core

import common.Observer
import common.TurnOwner.TurnOwner
import model.event.Events.Event
import model.event.Events.Event.{FlipCoin, NextTurn}
import model.exception.CoinNotLaunchedException
import org.scalamock.scalatest.MockFactory
import org.scalatest.OneInstancePerTest
import org.scalatest.flatspec.AnyFlatSpec

class TurnManagerTest extends AnyFlatSpec with MockFactory {

  behavior of "The TurnManager"

  val observerMock: Observer = mock[Observer]
  TurnManager.addObserver(observerMock)

  it should "launch a CoinNotLaunchedException if a player is ready before the coin is launched" in {
    intercept[CoinNotLaunchedException] {
      TurnManager.playerReady()
    }
  }

  it should "launch a CoinNotLaunchedException when someone tries to switch turn before the coin is launched" in {
    intercept[CoinNotLaunchedException] {
      TurnManager.switchTurn()
    }
  }

  it should "flip a coin when required and return the first TurnOwner" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[FlipCoin]
      e.asInstanceOf[FlipCoin].coinValue.isInstanceOf[TurnOwner]
    }})
    TurnManager.flipACoin()
  }

  it should "notify observers when both human player and AI player are ready to play" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[NextTurn]
      e.asInstanceOf[NextTurn].turnOwner.isInstanceOf[TurnOwner]
    }})
    TurnManager.playerReady() // AI player is ready
    TurnManager.playerReady() // Human player is ready
  }

  it should "notify observers when a player ends his turn" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[NextTurn]
      e.asInstanceOf[NextTurn].turnOwner.isInstanceOf[TurnOwner]
    }})
    TurnManager.switchTurn()
  }
}
