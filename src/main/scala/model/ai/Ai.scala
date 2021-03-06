package model.ai
import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}

import common.Events._
import common.TurnOwner.TurnOwner
import common.{Observer, TurnOwner}
import model.ai.AiLogicManager.calculateIfWithdrawAndDo
import model.core.{GameManager, TurnManager}
import model.game.Board


case class Ai(gameManager: GameManager, turnManager: TurnManager) extends Thread with Observer {
  private val eventQueue: BlockingQueue[Event] = new ArrayBlockingQueue[Event](20)
  turnManager.addObserver(this)
  gameManager.addObserver(this)
  private var opponentBoard: Board = _
  private var playerBoard: Board = _
  private var turn: TurnOwner = _
  private var isCancelled: Boolean = false

  override def run() {
    try {
      while (!isCancelled) {
        val event: Event = waitForNextEvent()
        event match {
          case event: BuildGameFieldEvent => {
            opponentBoard = event.asInstanceOf[BuildGameFieldEvent].opponentBoard
            playerBoard = event.asInstanceOf[BuildGameFieldEvent].playerBoard
            AiLogicManager.placeCards(opponentBoard.hand,opponentBoard,playerBoard,gameManager,turnManager)
          }
          case event: FlipCoinEvent => turn = if (event.isHead) TurnOwner.Player else TurnOwner.Opponent
          case event: NextTurnEvent => turn = event.turnOwner; if (event.turnOwner == TurnOwner.Opponent) AiLogicManager.doTurn(opponentBoard,playerBoard,gameManager,turnManager)
          case _: PokemonKOEvent => if(opponentBoard.activePokemon.get.isKO) calculateIfWithdrawAndDo(opponentBoard, playerBoard, gameManager)
          case event: DamageBenchEvent if turn == TurnOwner.Opponent => AiLogicManager.dmgToBench(event.pokemonToDamage, event.damage,opponentBoard)
          case _: EndGameEvent => isCancelled = true
          case _ =>
        }
      }
    } catch {
      case e: Exception => println(e)
    }
  }

  def cancel(): Unit = {
    isCancelled = true
  }

  private def waitForNextEvent(): Event = {
    eventQueue.take()
  }

  def notifyEvent(ev: Event): Boolean = {
    eventQueue.offer(ev)
  }

  override def update(event: Event): Unit = {
    notifyEvent(event)
  }
}

