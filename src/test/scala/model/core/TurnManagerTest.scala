package model.core

import common.{CoinUtil, Observer}
import common.TurnOwner.TurnOwner
import common.Events.{Event, FlipCoinEvent, NextTurnEvent}
import model.exception.CoinNotLaunchedException
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec

class TurnManagerTest extends AnyFlatSpec with MockFactory {
  behavior of "The TurnManager"

  val observerMock: Observer = mock[Observer]
  val turnManager: TurnManager = TurnManager()
  turnManager.addObserver(observerMock)
  CoinUtil.addObserver(observerMock)

  it should "launch a CoinNotLaunchedException if a player is ready before the coin is launched" in {
    intercept[CoinNotLaunchedException] {
      turnManager.playerReady()
    }
  }

  it should "launch a CoinNotLaunchedException when someone tries to switch turn before the coin is launched" in {
    intercept[CoinNotLaunchedException] {
      turnManager.switchTurn()
    }
  }

  it should "flip a coin when required and return the first TurnOwner" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[FlipCoinEvent]
    }})
    turnManager.flipACoin()
  }

  it should "notify observers when both human player and AI player are ready to play" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[NextTurnEvent]
      e.asInstanceOf[NextTurnEvent].turnOwner.isInstanceOf[TurnOwner]
    }})
    turnManager.playerReady() // AI player is ready
    turnManager.playerReady() // Human player is ready
  }

  it should "notify observers when a player ends his turn" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[NextTurnEvent]
      e.asInstanceOf[NextTurnEvent].turnOwner.isInstanceOf[TurnOwner]
    }})
    turnManager.switchTurn()
  }
}
