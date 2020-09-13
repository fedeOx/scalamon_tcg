package controller

import model.core.{DataLoader, GameManager}
import model.event.Events.Event
import model.game.Cards.{Card, PokemonCard}
import model.game.{DeckCard, DeckType, GameField}
import model.game.DeckType.DeckType
import model.game.EnergyType.EnergyType
import model.game.SetType.SetType

import scala.util.Random

trait Controller {
  def loadDeckCards(set: SetType, deck: DeckType): Unit
  def initGame(cardList: Seq[DeckCard], set: SetType): Unit
  def startGame(): Unit
  def playerReady(): Unit
  def endTurn(): Unit

  def addActivePokemon(card: PokemonCard): Unit
  def addPokemonToBench(card: PokemonCard): Unit
  def destroyActivePokemon(): Unit
  def drawACard(): Unit
  def drawAPrizeCard(): Unit

  def selectCardFromHand(card: Card): Unit
  def selectActivePokemonLocation(): Unit
  def selectBenchLocation(index: Int): Unit

  def declareAttack(damage:Int, attackerTypes: Seq[EnergyType], recipient: PokemonCard): Unit

  def replaceActivePokemonWith(benchPosition: Int): Unit
}

object Controller {
  def apply(): Controller = ControllerImpl()

  private case class ControllerImpl() extends Controller {

    override def loadDeckCards(set: SetType, deck: DeckType): Unit = new Thread {
      override def run(): Unit = {
        val deckCards: Seq[DeckCard] = DataLoader.loadDeck(set, deck)
        DataLoader.notifyObservers(Event.showDeckCardsEvent(deckCards))
      }
    }.start()

    override def initGame(playerDeckCards: Seq[DeckCard], set: SetType): Unit = new Thread {
      override def run(): Unit = {
        val setCards: Seq[Card] = DataLoader.loadSet(set)
        // TODO start - make AI choose its deck
        val random = new Random()
        val opponentChosenDeckType = DeckType.values.filter(d => d.setType == set)
          .toVector(random.nextInt(DeckType.values.size)) // Choose a random deck from the selected SetType
        val opponentDeckCards: Seq[DeckCard] = DataLoader.loadDeck(set, opponentChosenDeckType)
        // TODO end
        val gameField: GameField = GameManager.initBoards(playerDeckCards, opponentDeckCards, setCards)
        GameManager.notifyObservers(Event.buildGameFieldEvent(gameField))
      }
    }.start()

    override def startGame(): Unit = ???

    override def playerReady(): Unit = ???

    override def endTurn(): Unit = ???

    override def addActivePokemon(card: PokemonCard): Unit = ???

    override def addPokemonToBench(card: PokemonCard): Unit = ???

    override def destroyActivePokemon(): Unit = ???

    override def drawACard(): Unit = ???

    override def drawAPrizeCard(): Unit = ???

    override def selectCardFromHand(card: Card): Unit = ???

    override def selectActivePokemonLocation(): Unit = ???

    override def selectBenchLocation(index: Int): Unit = ???

    override def declareAttack(damage: Int, attackerTypes: Seq[EnergyType], recipient: PokemonCard): Unit = ???

    override def replaceActivePokemonWith(benchPosition: Int): Unit = ???
  }
}
