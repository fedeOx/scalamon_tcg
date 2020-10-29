package controller

import common.{CoinUtil, Observable}
import model.ai.Ai
import model.card.{Card, EnergyCard, PokemonCard}
import model.core.{DataLoader, GameManager, TurnManager}
import common.Events.{CustomDeckSavedEvent, ShowDeckCardsEvent, ShowSetCardsEvent}
import model.exception.{CoinNotLaunchedException, InvalidOperationException}
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
   * It makes [[model.core.DataLoader]] loads all the available custom decks and notify them to all observers.
   */
  def loadCustomDecks(): Unit

  /**
   * It makes [[model.core.DataLoader]] loads the list of [[model.card.Card]] of the specified set and notify
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
   * @param setList the cards set list to whom `cardList` belongs
   */
  def initGame(cardList: Seq[DeckCard], setList: Seq[SetType]): Unit

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
  def resetGame(): Unit

  /**
   * It adds the specified damage to the specified benched pokemon
   * @param pokemon the pokemon to which the damage should be done
   * @param attackingBoard the attacking board
   * @param defendingBoard the defending board to which the pokemon belongs
   * @param damage the damage to be done
   */
  def damageBenchedPokemon(pokemon: PokemonCard, attackingBoard: Board, defendingBoard: Board, damage: Int): Unit
}

object Controller {
  def apply(): Controller = ControllerImpl(DataLoader(), GameManager(), TurnManager())

  private case class ControllerImpl(override val dataLoader: DataLoader,
                                    override val gameManager: GameManager,
                                    override val turnManager: TurnManager,
                                    override var handCardSelected: Option[Card] = None) extends Controller {

    private var isPlayerReady = false
    private var energyCardAlreadyAssigned = false
    private var pokemonAlreadyRetreated = false

    private val ai = Ai(gameManager, turnManager)
    ai.start() // starts AI thread

    override def loadDecks(set: SetType): Unit =
      executeInNewThread(() => dataLoader.notifyObservers(ShowDeckCardsEvent(dataLoader.loadDecks(set))))

    override def loadCustomDecks(): Unit =
      executeInNewThread(() => dataLoader.notifyObservers(ShowDeckCardsEvent(dataLoader.loadCustomDecks())))

    override def loadSet(set: SetType): Unit =
      executeInNewThread(() => dataLoader.notifyObservers(ShowSetCardsEvent(dataLoader.loadSet(set))))

    override def createCustomDeck(customDeck: CustomDeck): Unit =
      executeInNewThread(() => dataLoader.notifyObservers(CustomDeckSavedEvent(dataLoader.saveCustomDeck(customDeck))))

    override def initGame(playerDeckCards: Seq[DeckCard], setList: Seq[SetType]): Unit = executeInNewThread(() => {
      val setCards: Seq[Card] = setList.flatMap(s => dataLoader.loadSet(s))
      gameManager.initBoards(playerDeckCards, chooseDeckForAI(setList), setCards)
      turnManager.flipACoin()
    })

    override def playerReady(): Unit = {
      isPlayerReady = true
      turnManager.playerReady()
    }

    override def endTurn(): Unit = {
      energyCardAlreadyAssigned = false
      pokemonAlreadyRetreated = false
      gameManager.activePokemon() match {
        case Some(p) => gameManager.activePokemonEndTurnChecks(p)
        case _ =>
      }
      turnManager.switchTurn()
    }

    override def activePokemonStatusCheck(): Unit = gameManager.activePokemon() match {
      case Some(_) => gameManager.activePokemonStartTurnChecks(gameManager.playerBoard, gameManager.opponentBoard)
      case _ =>
    }

    def swap(position: Int): Unit = gameManager.activePokemon() match {
      case Some(p) if p.isKO => gameManager.destroyActivePokemon(position)
      case Some(_) if !pokemonAlreadyRetreated => pokemonAlreadyRetreated = gameManager.retreatActivePokemon(position)
      case _ => throw new InvalidOperationException("You have already retreat a pokemon in this turn")
    }

    override def drawCard(): Unit = gameManager.drawCard()

    override def selectActivePokemonLocation(): Unit =
      handleUserGameFieldSelection(gameManager.activePokemon())

    override def selectBenchLocation(position: Int): Unit =
      handleUserGameFieldSelection(gameManager.pokemonBench()(position), Some(position))

    override def declareAttack(attackingBoard: Board, defendingBoard: Board, attack: Attack): Unit =
      executeInNewThread(() => gameManager.confirmAttack(attackingBoard, defendingBoard, attack))

    override def resetGame(): Unit = ai.interrupt(); CoinUtil.reset()

    override def damageBenchedPokemon(pokemon: PokemonCard, attackingBoard: Board, defendingBoard: Board, damage: Int): Unit =
      gameManager.damageBenchedPokemon(pokemon, attackingBoard, defendingBoard, damage)

    private def executeInNewThread(runnable: Runnable): Unit = new Thread(runnable).start()

    private def chooseDeckForAI(availableSets: Seq[SetType]): Seq[DeckCard] =
      dataLoader.loadSingleDeck(DeckType(new Random().nextInt(DeckType.values.count(d => availableSets.contains(d.setType)))))

    private def handleUserGameFieldSelection(pokemonCard: Option[PokemonCard], position: Option[Int] = None): Unit =
      (handCardSelected, pokemonCard) match {
        case (Some(c), Some(p)) if c.isInstanceOf[EnergyCard] && isPlayerReady && !energyCardAlreadyAssigned =>
          gameManager.addEnergyToPokemon(p, c.asInstanceOf[EnergyCard])
          energyCardAlreadyAssigned = true
          handCardSelected = None

        case (Some(c), None) if c.isInstanceOf[PokemonCard] && c.asInstanceOf[PokemonCard].isBase =>
          if (position.isEmpty)
            gameManager.setActivePokemon(Some(c.asInstanceOf[PokemonCard]))
          else
            gameManager.putPokemonToBench(Some(c.asInstanceOf[PokemonCard]), position.get)
          handCardSelected = None

        case (Some(c), Some(p)) if c.isInstanceOf[PokemonCard] && isPlayerReady && c.asInstanceOf[PokemonCard].evolutionName == p.name =>
          val evolvedPokemon = gameManager.evolvePokemon(p, c.asInstanceOf[PokemonCard])
          if (position.isEmpty)
            gameManager.setActivePokemon(evolvedPokemon)
          else
            gameManager.putPokemonToBench(evolvedPokemon, position.get)
          handCardSelected = None

        case _ => throw new InvalidOperationException("Operation not allowed")
      }
  }
}
