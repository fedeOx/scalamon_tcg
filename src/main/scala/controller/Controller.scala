package controller

import common.Observable
import model.ai.Ai
import model.core.{DataLoader, GameManager, TurnManager}
import model.effect.EffectManager
import model.event.Events.Event
import model.exception.{CoinNotLaunchedException, InvalidOperationException}
import model.game.Cards.{Card, EnergyCard, PokemonCard}
import model.game.{Attack, Board, CustomDeck, DeckCard, DeckType}
import model.game.SetType.SetType
import scala.util.Random

trait Controller {

  def dataLoader: Observable
  def gameManager: Observable
  def turnManager: Observable

  /**
   * It makes [[model.core.DataLoader]] loads all the available decks and notify them to all observers.
   * @param set the card set chosen by the user
   */
  def loadDecks(set: SetType): Unit

  /**
   * It makes [[model.core.DataLoader]] loads the list of [[model.game.Cards.Card]] of the specified set and notify
   * them to all observers.
   * @param set the set whose cards must be loaded
   */
  def loadSet(set: SetType): Unit

  /**
   * It makes [[model.core.DataLoader]] save a new custom deck.
   * @param customDeck the custom deck to be saved
   */
  def createCustomDeck(customDeck: CustomDeck): Unit

  /**
   * It makes [[model.core.GameManager]] init the game field and [[model.core.TurnManager]] decide who will be the
   * player that will start the game. The information produced are propagated to all observers.
   *
   * @param cardList the list of cards composing the deck chosen by the user
   * @param set the card set to whom `cardList` belongs
   */
  def initGame(cardList: Seq[DeckCard], set: SetType): Unit

  /**
   * It inform [[model.core.TurnManager]] when the user has finished his placement turn.
   * @throws model.exception.CoinNotLaunchedException if [[model.core.TurnManager]] has not launched the initial coin yet
   */
  @throws(classOf[CoinNotLaunchedException])
  def playerReady(): Unit

  /**
   * It inform [[model.core.TurnManager]] when the user ends his turn. It makes [[model.core.GameManager]]
   * update player active pokemon status.
   * @throws model.exception.CoinNotLaunchedException if [[model.core.TurnManager]] has not launched the initial coin yet
   */
  @throws(classOf[CoinNotLaunchedException])
  def endTurn(): Unit

  /**
   * It makes [[model.core.GameManager]] apply the operation associated to the eventual player active pokemon status.
   * It must be called at the beginning of the player turn.
   */
  def activePokemonStatusCheck(): Unit

  /**
   * Makes the player draw a card from his deck
   */
  def drawCard(): Unit

  /**
   * Swap the active pokemon with the benched pokemon in the specified position. If the active pokemon is KO, the benched
   * pokemon takes the active pokemon role. If the active pokemon is not KO, this operation can be considered a retreat.
   * Note that the retreat operation is allowed only one time per turn.
   * @param position the position of the benched pokemon in the bench
   * @throws model.exception.InvalidOperationException in case of retreat, if a pekemon is been already retreat in this turn
   */
  @throws(classOf[InvalidOperationException])
  def swap(position: Int): Unit

  def handCardSelected: Option[Card]

  def handCardSelected_=(card: Option[Card])

  /**
   * To be called when a user select the active pokemon location. It decides what to do given
   * [[controller.Controller#handCardSelected]] value.
   * @throws model.exception.InvalidOperationException if the operation the user tries to do is not allowed
   */
  @throws(classOf[InvalidOperationException])
  def selectActivePokemonLocation(): Unit

  /**
   * To be called when a user select the pokemon bench location. It decides what to do given
   * [[controller.Controller#handCardSelected]] value.
   * @param position the position selected on the pokemon bench
   * @throws model.exception.InvalidOperationException if the operation the user tries to do is not allowed
   */
  @throws(classOf[InvalidOperationException])
  def selectBenchLocation(position: Int): Unit

  /**
   * It makes [[model.core.GameManager]] manage the declaration of an attack from player active pokemon to opponent
   * active pokemon.
   * @param attack the active pokemon attack selected by the user
   * @param attackingBoard the attacking board
   * @param defendingBoard the defending board
   */
  def declareAttack(attackingBoard: Board, defendingBoard: Board, attack: Attack): Unit

  /**
   * It resets the current game in order to start a new one.
   */
  def interruptAi(): Unit
}

object Controller {
  def apply(): Controller = {
    val dataLoader = DataLoader()
    val gameManager = GameManager()
    val turnManager = TurnManager()
    val ai = Ai(gameManager, turnManager)
    ai.start()
    EffectManager.setGameManager(gameManager)
    ControllerImpl(dataLoader, gameManager, turnManager, ai)
  }

  private case class ControllerImpl(override val dataLoader: DataLoader,
                                    override val gameManager: GameManager,
                                    override val turnManager: TurnManager,
                                    private val ai: Ai,
                                    override var handCardSelected: Option[Card] = None) extends Controller {

    private var isPlayerReady = false
    private var energyCardAlreadyAssigned = false
    private var pokemonAlreadyRetreated = false

    override def loadDecks(set: SetType): Unit = new Thread {
      override def run(): Unit = {
        val deckCards: Map[String, Seq[DeckCard]] = dataLoader.loadDecks(set)
        dataLoader.notifyObservers(Event.showDeckCardsEvent(deckCards))
      }
    }.start()

    override def loadSet(set: SetType): Unit = new Thread {
      override def run(): Unit = {
        val setCards: Seq[Card] = dataLoader.loadSet(set)
        dataLoader.notifyObservers(Event.showSetCardsEvent(setCards))
      }
    }.start()

    override def createCustomDeck(customDeck: CustomDeck): Unit = new Thread {
      override def run(): Unit = {
        val success = dataLoader.saveCustomDeck(customDeck)
        dataLoader.notifyObservers(Event.customDeckSavedEvent(success))
      }
    }.start()

    override def initGame(playerDeckCards: Seq[DeckCard], set: SetType): Unit = new Thread {
      override def run(): Unit = {
        val setCards: Seq[Card] = dataLoader.loadSet(set)
        val opponentChosenDeckType = DeckType.values.filter(d => d.setType == set)
          .toVector(new Random().nextInt(DeckType.values.size)) // Choose a random deck from the selected SetType
        val opponentDeckCards: Seq[DeckCard] = dataLoader.loadSingleDeck(opponentChosenDeckType)
        gameManager.initBoards(playerDeckCards, opponentDeckCards, setCards)
        turnManager.flipACoin()
      }
    }.start()

    override def playerReady(): Unit = {
      isPlayerReady = true
      turnManager.playerReady()
    }

    override def endTurn(): Unit = {
      energyCardAlreadyAssigned = false
      pokemonAlreadyRetreated = false
      if (!gameManager.isActivePokemonEmpty(gameManager.playerBoard)) {
        gameManager.activePokemonEndTurnChecks(gameManager.activePokemon().get)
      }
      turnManager.switchTurn()
    }

    override def activePokemonStatusCheck(): Unit = {
      if (!gameManager.isActivePokemonEmpty()) {
        gameManager.activePokemonStartTurnChecks(gameManager.playerBoard, gameManager.opponentBoard)
      }
    }

    def swap(position: Int): Unit = {
      if (!gameManager.isActivePokemonEmpty() && !gameManager.isBenchLocationEmpty(position)) {
        if (gameManager.activePokemon().get.isKO) {
          gameManager.destroyActivePokemon(position)
        } else if (!pokemonAlreadyRetreated) {
          val oldActivePokemon: PokemonCard = gameManager.activePokemon().get
          gameManager.retreatActivePokemon(position)
          pokemonAlreadyRetreated = oldActivePokemon ne gameManager.activePokemon().get
        } else {
          throw new InvalidOperationException("You have already retreat a pokemon in this turn")
        }
      }
    }

    override def drawCard(): Unit = gameManager.drawCard()

    override def selectActivePokemonLocation(): Unit = handCardSelected match {

      case Some(c) if c.isInstanceOf[EnergyCard] && isPlayerReady && !gameManager.isActivePokemonEmpty() && !energyCardAlreadyAssigned =>
        gameManager.addEnergyToPokemon(gameManager.activePokemon().get, c.asInstanceOf[EnergyCard])
        energyCardAlreadyAssigned = true
        handCardSelected = None

      case Some(c) if c.isInstanceOf[PokemonCard] && c.asInstanceOf[PokemonCard].isBase && gameManager.isActivePokemonEmpty() =>
        gameManager.setActivePokemon(Some(c.asInstanceOf[PokemonCard])); handCardSelected = None

      case Some(c) if c.isInstanceOf[PokemonCard] && !gameManager.isActivePokemonEmpty() && isPlayerReady
        && c.asInstanceOf[PokemonCard].evolutionName == gameManager.activePokemon().get.name =>
        val evolvedPokemon = gameManager.evolvePokemon(gameManager.activePokemon().get, c.asInstanceOf[PokemonCard])
        gameManager.setActivePokemon(evolvedPokemon)
        handCardSelected = None

      case _ => throw new InvalidOperationException("Operation not allowed on active pokemon location")
    }

    override def selectBenchLocation(position: Int): Unit = handCardSelected match {

      case Some(c) if c.isInstanceOf[EnergyCard] && isPlayerReady && !gameManager.isBenchLocationEmpty(position) && !energyCardAlreadyAssigned =>
        gameManager.addEnergyToPokemon(gameManager.pokemonBench()(position).get, c.asInstanceOf[EnergyCard])
        energyCardAlreadyAssigned = true
        handCardSelected = None

      case Some(c) if c.isInstanceOf[PokemonCard] && c.asInstanceOf[PokemonCard].isBase && gameManager.isBenchLocationEmpty(position) =>
        gameManager.putPokemonToBench(Some(c.asInstanceOf[PokemonCard]), position); handCardSelected = None

      case Some(c) if c.isInstanceOf[PokemonCard] && !gameManager.isBenchLocationEmpty(position) && isPlayerReady
        && c.asInstanceOf[PokemonCard].evolutionName == gameManager.playerBoard.pokemonBench(position).get.name =>
        val evolvedPokemon = gameManager.evolvePokemon(gameManager.pokemonBench()(position).get, c.asInstanceOf[PokemonCard])
        gameManager.putPokemonToBench(evolvedPokemon, position)
        handCardSelected = None

      case _ => throw new InvalidOperationException("Operation not allowed on pokemon bench location")
    }

    override def declareAttack(attackingBoard: Board, defendingBoard: Board, attack: Attack): Unit = new Thread {
      override def run() : Unit = {
        gameManager.confirmAttack(attackingBoard, defendingBoard, attack)
      }
    }.start()

    override def interruptAi(): Unit = ai.interrupt()
  }
}
