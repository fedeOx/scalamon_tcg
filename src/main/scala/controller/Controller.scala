package controller

import common.TurnOwner.TurnOwner
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

    /*
    override def addActivePokemon(card: PokemonCard): Unit = {
      if (GameManager.isPlayerActivePokemonEmpty) {
        GameManager.playerBoard.activePokemon = Some(card)
        notifyBoardUpdate()
      } else {
        throw new ActivePokemonException("An active pokemon is already present on player board")
      }
    }

    override def addPokemonToBench(card: PokemonCard, position: Int): Unit = {
      if (GameManager.isPlayerBenchLocationEmpty(position)) {
        GameManager.playerBoard.addPokemonToBench(card, position)
        notifyBoardUpdate()
      } else {
        throw new BenchPokemonException("A pokemon is already present in position " + position + " of the bench")
      }
    }
     */

    // Chiamato ogni volta che un pokemon viene messo KO o ritirato (la position è = a quella scelta dall'utente)
    def swap(position: Int): Unit = {
      if (GameManager.playerBoard.activePokemon.nonEmpty && GameManager.playerBoard.pokemonBench(position).nonEmpty) {
        if (GameManager.playerBoard.activePokemon.get.isKO) {
          val playerBoard = GameManager.playerBoard
          playerBoard.addCardsToDiscardStack(GameManager.playerBoard.activePokemon.get :: Nil)
          playerBoard.activePokemon = playerBoard.pokemonBench(position)
          playerBoard.removePokemonFromBench(position)
          notifyBoardUpdate()
        } else {
          val activePokemon = GameManager.playerBoard.activePokemon.get
          if (activePokemon.hasEnergies(activePokemon.retreatCost)) {
            activePokemon.status = StatusType.NoStatus
            GameManager.playerBoard.activePokemon = GameManager.playerBoard.pokemonBench(position)
            GameManager.playerBoard.addPokemonToBench(activePokemon, position)
            notifyBoardUpdate()
          }
        }
      }
    }

    /*
    private def destroyActivePokemon(position: Int): Unit = {
      if (!GameManager.isPlayerActivePokemonEmpty) {
        val playerBoard = GameManager.playerBoard
        playerBoard.addCardsToDiscardStack(GameManager.playerBoard.activePokemon.get :: Nil)
        playerBoard.activePokemon = playerBoard.pokemonBench.head
        notifyBoardUpdate()
      }
    }

    override def destroyPokemonFromBench(position: Int): Unit = {
      if (!GameManager.isPlayerBenchLocationEmpty(position)) {
        val benchedPokemon = GameManager.playerBoard.pokemonBench(position).get
        GameManager.playerBoard.removePokemonFromBench(position)
        GameManager.playerBoard.addCardsToDiscardStack(benchedPokemon :: Nil)
        notifyBoardUpdate()
      }
    }
     */

    override def drawACard(): Unit = GameManager.drawPlayerCard()

    override def drawAPrizeCard(): Unit = GameManager.drawPlayerPrizeCard()

    override def selectActivePokemonLocation(): Unit = handCardSelected match {
      case Some(c) if c.isInstanceOf[EnergyCard] && !GameManager.isPlayerActivePokemonEmpty => // Add energy card
        GameManager.playerBoard.activePokemon.get.addEnergy(c.asInstanceOf[EnergyCard]); handCardSelected = Option.empty; notifyBoardUpdate()
      case Some(c) if c.isInstanceOf[PokemonCard] && c.asInstanceOf[PokemonCard].isBase && GameManager.isPlayerActivePokemonEmpty => // Place active pokemon
        GameManager.playerBoard.activePokemon = Some(c.asInstanceOf[PokemonCard]); handCardSelected = Option.empty; notifyBoardUpdate()
      case Some(c) if c.isInstanceOf[PokemonCard] && !GameManager.isPlayerActivePokemonEmpty  // Evolve active pokemon
        && c.asInstanceOf[PokemonCard].evolutionName == GameManager.playerBoard.activePokemon.get.name =>
        val handPokemonCard: PokemonCard = c.asInstanceOf[PokemonCard]
        val activePokemonCard: PokemonCard = GameManager.playerBoard.activePokemon.get
        handPokemonCard.energiesMap = activePokemonCard.energiesMap
        handPokemonCard.status = activePokemonCard.status
        handPokemonCard.actualHp = handPokemonCard.initialHp - (activePokemonCard.initialHp - activePokemonCard.actualHp)
        GameManager.playerBoard.addCardsToDiscardStack(activePokemonCard :: Nil)
        GameManager.playerBoard.activePokemon = Some(handPokemonCard)
        handCardSelected = Option.empty
        notifyBoardUpdate()
      case _ => throw new ActivePokemonException()
    }

    override def selectBenchLocation(position: Int): Unit = handCardSelected match {
      case Some(c) if c.isInstanceOf[EnergyCard] && !GameManager.isPlayerBenchLocationEmpty(position) => // Add energy card
        GameManager.playerBoard.pokemonBench(position).get.addEnergy(c.asInstanceOf[EnergyCard]); handCardSelected = Option.empty; notifyBoardUpdate()
      case Some(c) if c.isInstanceOf[PokemonCard] && c.asInstanceOf[PokemonCard].isBase && GameManager.isPlayerBenchLocationEmpty(position) => // Place bench pokemon
        GameManager.playerBoard.addPokemonToBench(c.asInstanceOf[PokemonCard], position); handCardSelected = Option.empty; notifyBoardUpdate()
      case Some(c) if c.isInstanceOf[PokemonCard] && !GameManager.isPlayerBenchLocationEmpty(position)  // Evolve bench pokemon
        && c.asInstanceOf[PokemonCard].evolutionName == GameManager.playerBoard.pokemonBench(position).get.name =>
        val handPokemonCard: PokemonCard = c.asInstanceOf[PokemonCard]
        val benchPokemonCard: PokemonCard = GameManager.playerBoard.pokemonBench(position).get
        handPokemonCard.energiesMap = benchPokemonCard.energiesMap
        handPokemonCard.status = benchPokemonCard.status
        handPokemonCard.actualHp = handPokemonCard.initialHp - (benchPokemonCard.initialHp - benchPokemonCard.actualHp)
        GameManager.playerBoard.addCardsToDiscardStack(benchPokemonCard :: Nil)
        GameManager.playerBoard.addPokemonToBench(handPokemonCard, position)
        handCardSelected = Option.empty
        notifyBoardUpdate()
      case _ => throw new BenchPokemonException()
    }

    override def declareAttack(attack: Attack): Unit = {
      //attack.effect.get.useEffect()
      // Manca da controllare se qualche pokemon nel campo di gioco è andato KO
      // se muore il pokemon del giocatore o dell'ia -> GameManager.notifyObservers(Event.pokemonKOEvent())
      // sempre -> notifyBoardUpdate()
    };

    /*
    override def replaceActivePokemonWith(benchPosition: Int): Unit = {
      if (GameManager.playerBoard.activePokemon.nonEmpty && GameManager.playerBoard.pokemonBench(benchPosition).nonEmpty) {
        val activePokemon = GameManager.playerBoard.activePokemon.get
        if (activePokemon.hasEnergies(activePokemon.retreatCost)) {
          activePokemon.status = StatusType.NoStatus
          GameManager.playerBoard.activePokemon = GameManager.playerBoard.pokemonBench(benchPosition)
          GameManager.playerBoard.addPokemonToBench(activePokemon, benchPosition)
          notifyBoardUpdate()
        } else {
          throw new NotEnoughEnergiesException("The active pokemon has not enough energies to make a retreat")
        }
      }
    }
     */

    private def notifyBoardUpdate(): Unit = {
      GameManager.notifyObservers(Event.updatePlayerBoardEvent())
    }
  }
}
