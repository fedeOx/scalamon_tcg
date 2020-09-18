package controller

import model.core.{DataLoader, GameManager, TurnManager}
import model.event.Events.Event
import model.exception.{ActivePokemonException, BenchPokemonException, NotEnoughEnergiesException}
import model.game.Cards.{Card, EnergyCard, PokemonCard}
import model.game.{Attack, DeckCard, DeckType, StatusType}
import model.game.DeckType.DeckType
import model.game.EnergyType.EnergyType
import model.game.SetType.SetType

import scala.util.Random

trait Controller {
  /**
   * It makes [[model.core.DataLoader]] loads a list of [[model.game.DeckCard]] and notify them to all observers.
   * @param set the card set chosen by the user
   * @param deck the deck chosen by the user
   */
  def loadDeckCards(set: SetType, deck: DeckType): Unit

  /**
   * It makes [[model.core.GameManager]] init the game field and [[model.core.TurnManager]] decide who will be the
   * player that will start the game. The information produced are propagated to all observers.
   *
   * @param cardList the list of cards composing the deck chosen by the user
   * @param set the card set to whom `cardList` belongs
   */
  def initGame(cardList: Seq[DeckCard], set: SetType): Unit

  /**
   * It makes [[model.core.TurnManager]] propagate event for players cards placing.
   */
  def startGame(): Unit

  /**
   * It inform [[model.core.TurnManager]] when the user has finished his placement turn.
   */
  def playerReady(): Unit

  /**
   * It inform [[model.core.TurnManager]] when the user ends his turn.
   */
  def endTurn(): Unit

  /**
   * Makes the player draw a card from his deck
   */
  def drawACard(): Unit

  /**
   * Makes the player draw a card from the prize card stack
   */
  def drawAPrizeCard(): Unit

  def swap(position: Int): Unit

  def handCardSelected: Option[Card]

  def handCardSelected_=(card: Option[Card])

  @throws(classOf[ActivePokemonException])
  def selectActivePokemonLocation(): Unit

  @throws(classOf[BenchPokemonException])
  def selectBenchLocation(position: Int): Unit

  def declareAttack(attack: Attack): Unit
}

object Controller {
  def apply(): Controller = ControllerImpl()

  private case class ControllerImpl(override var handCardSelected: Option[Card] = None) extends Controller {

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
        GameManager.initBoards(playerDeckCards, opponentDeckCards, setCards)
        TurnManager.flipACoin()
      }
    }.start()

    override def startGame(): Unit = TurnManager.notifyObservers(Event.placeCardsEvent())

    override def playerReady(): Unit = TurnManager.playerReady()

    override def endTurn(): Unit = TurnManager.switchTurn()

    def swap(position: Int): Unit = {
      if (!GameManager.isPlayerActivePokemonEmpty && !GameManager.isPlayerBenchLocationEmpty(position)) {
        if (GameManager.playerActivePokemon.get.isKO) {
          GameManager.destroyPlayerActivePokemon(position)
        } else {
          GameManager.retreatPlayerActivePokemon(position)
        }
      }
    }

    override def drawACard(): Unit = GameManager.drawPlayerCard()

    override def drawAPrizeCard(): Unit = GameManager.drawPlayerPrizeCard()

    override def selectActivePokemonLocation(): Unit = handCardSelected match {

      case Some(c) if c.isInstanceOf[EnergyCard] && !GameManager.isPlayerActivePokemonEmpty =>
        GameManager.addEnergyToPokemon(GameManager.playerActivePokemon.get, c.asInstanceOf[EnergyCard])

      case Some(c) if c.isInstanceOf[PokemonCard] && c.asInstanceOf[PokemonCard].isBase && GameManager.isPlayerActivePokemonEmpty =>
        GameManager.playerActivePokemon = Some(c.asInstanceOf[PokemonCard])

      case Some(c) if c.isInstanceOf[PokemonCard] && !GameManager.isPlayerActivePokemonEmpty
        && c.asInstanceOf[PokemonCard].evolutionName == GameManager.playerActivePokemon.get.name =>
        val evolvedPokemon = GameManager.evolvePokemon(GameManager.playerActivePokemon.get, c.asInstanceOf[PokemonCard])
        GameManager.playerActivePokemon = evolvedPokemon
      case _ => throw new ActivePokemonException()
    }

    override def selectBenchLocation(position: Int): Unit = handCardSelected match {

      case Some(c) if c.isInstanceOf[EnergyCard] && !GameManager.isPlayerBenchLocationEmpty(position) =>
        GameManager.addEnergyToPokemon(GameManager.playerPokemonBench(position).get, c.asInstanceOf[EnergyCard])

      case Some(c) if c.isInstanceOf[PokemonCard] && c.asInstanceOf[PokemonCard].isBase && GameManager.isPlayerBenchLocationEmpty(position) =>
        GameManager.putPokemonToPlayerBench(Some(c.asInstanceOf[PokemonCard]), position)

      case Some(c) if c.isInstanceOf[PokemonCard] && !GameManager.isPlayerBenchLocationEmpty(position)
        && c.asInstanceOf[PokemonCard].evolutionName == GameManager.playerBoard.pokemonBench(position).get.name =>
        val evolvedPokemon = GameManager.evolvePokemon(GameManager.playerPokemonBench(position).get, c.asInstanceOf[PokemonCard])
        GameManager.putPokemonToPlayerBench(evolvedPokemon, position)

      case _ => throw new BenchPokemonException()
    }

    override def declareAttack(attack: Attack): Unit = GameManager.confirmAttack(attack)
  }
}
