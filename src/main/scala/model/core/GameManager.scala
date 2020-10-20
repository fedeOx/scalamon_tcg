package model.core

import common.Observable
import model.card.{Card, EnergyCard, PokemonCard}
import common.Events.{EndTurnEvent, BuildGameFieldEvent, EndGameEvent, PokemonKOEvent, UpdateBoardsEvent}
import model.exception.{CardNotFoundException, InvalidOperationException}
import model.game.{Attack, Board, DeckCard, EnergyType, StatusType}

import scala.util.Random

trait GameManager extends Observable {

  /**
   * Initializes player and opponent boards.
   * @param playerDeckCards the players deck cards
   * @param opponentDeckCards the opponent deck cards
   * @param cardsSet the card set to which the deck cards belong to
   */
  def initBoards(playerDeckCards: Seq[DeckCard], opponentDeckCards: Seq[DeckCard], cardsSet: Seq[Card]): Unit

  def playerBoard: Board

  def opponentBoard: Board

  /**
   * Checks if the specified bench position of the specified board is empty.
   * @param position the bench position
   * @param board the board whose bench position must be checked
   * @return true if the bench position is empty
   */
  def isBenchLocationEmpty(position: Int, board: Board = playerBoard): Boolean

  /**
   * Gets the active pokemon of the specified board
   * @param board the board whose active pokemon must be get
   * @return the active pokemon of the specified board
   */
  def activePokemon(board: Board = playerBoard): Option[PokemonCard]

  /**
   * Sets the active pokemon of the specified board
   * @param pokemonCard the pokemon to be set
   * @param board the board whose active pokemon must be set
   */
  def setActivePokemon(pokemonCard: Option[PokemonCard], board: Board = playerBoard): Unit

  /**
   * Gets the pokemon bench of the specified board
   * @param board the board whose pokemon bench must be get
   * @return the pokemon bench of the specified board
   */
  def pokemonBench(board: Board = playerBoard): Seq[Option[PokemonCard]]

  /**
   * Puts a pokemon to the specified position of the bench of the specified board
   * @param pokemonCard the pokemon to be putted
   * @param position the position of the bench
   * @param board the board whose the bench belongs
   */
  def putPokemonToBench(pokemonCard: Option[PokemonCard], position: Int, board: Board = playerBoard): Unit

  /**
   * Draw a card from the deck of the specified board
   * @param board the board whose deck must be popped
   */
  def drawCard(board: Board = playerBoard): Unit

  /**
   * Destroys the active pokemon of the specified board and replace it with the pokemon in the specified bench position.
   * @param replacementBenchPosition the bench position where the new pokemon is
   * @param board the board whose active pokemon must be destroyed
   */
  def destroyActivePokemon(replacementBenchPosition: Int, board: Board = playerBoard): Unit

  /**
   * Retreats the active pokemon of the specified board.
   * @param replacementBenchPosition the bench position where the new pokemon is
   * @param board the board whose active pokemon must be retreated
   * @return true if the retreat succeed, false otherwise
   */
  def retreatActivePokemon(replacementBenchPosition: Int, board: Board = playerBoard): Boolean

  /**
   * Fulfills all the needed checks on active pokemons status at the beginning of the turn.
   * @param board the board of the turn owner
   * @param opponentBoard the board of the opponent
   */
  def activePokemonStartTurnChecks(board: Board, opponentBoard: Board): Unit

  /**
   * Fulfills all the needed checks on active pokemon status at the end of the turn.
   * @param activePokemon the pokemon on which the checks must be done
   */
  def activePokemonEndTurnChecks(activePokemon: PokemonCard): Unit

  /**
   * Adds an energy card to a pokemon.
   * @param pokemonCard the pokemon to which the energy card must be added
   * @param energyCard the energy card to add
   * @param board the board to which the pokemon belongs
   */
  def addEnergyToPokemon(pokemonCard: PokemonCard, energyCard: EnergyCard, board: Board = playerBoard): Unit

  /**
   * Evolves a pokemon.
   * @param pokemonCard the pokemon to be evolved
   * @param evolution the evolution of the pokemon
   * @param board the board to which the pokemon belongs
   * @return the evolved pokemon
   */
  def evolvePokemon(pokemonCard: PokemonCard, evolution: PokemonCard, board: Board = playerBoard): Option[PokemonCard]

  /**
   * Manages the attack of a pokemon.
   * @param attackingBoard the attacking board
   * @param defendingBoard the defenfing board
   * @param attack the attack to be done
   * @throws model.exception.InvalidOperationException if the active pokemon of the attacking board is paralyzed or asleep
   */
  @throws(classOf[InvalidOperationException])
  def confirmAttack(attackingBoard: Board, defendingBoard: Board, attack: Attack): Unit

  /**
   * Adds the specified damage to the specified pokemon
   * @param pokemon the pokemon to which the damage should be done
   * @param attackingBoard the attacking board
   * @param defendingBoard the defending board to which the pokemon belongs
   * @param damage the damage to be done
   */
  def damageBenchedPokemon(pokemon: PokemonCard, attackingBoard: Board, defendingBoard: Board, damage: Int): Unit
}

object GameManager {
  def apply(): GameManager = GameManagerImpl()

  val InitialHandCardNum = 7
  val InitialPrizeCardNum = 6
  val PoisonDamage = 10
  val ConfusedDamage = 30

  private case class GameManagerImpl() extends GameManager {

    private var _playerBoard: Option[Board] = None
    private var _opponentBoard: Option[Board] = None

    override def initBoards(playerDeckCards: Seq[DeckCard], opponentDeckCards: Seq[DeckCard], cardsSet: Seq[Card]): Unit = {
      val playerCards: Seq[Card] = buildCardList(playerDeckCards, cardsSet)(List())
      val opponentCards: Seq[Card] = buildCardList(opponentDeckCards, cardsSet)(List())

      _playerBoard = Some(buildBoard(playerCards))
      _opponentBoard = Some(buildBoard(opponentCards))
      this.notifyObservers(BuildGameFieldEvent(_playerBoard.get, _opponentBoard.get))
    }

    override def playerBoard: Board = {
      checkNonEmpty(_playerBoard)
      _playerBoard.get
    }

    override def opponentBoard: Board = {
      checkNonEmpty(_opponentBoard)
      _opponentBoard.get
    }

    override def isBenchLocationEmpty(position: Int, board: Board = playerBoard): Boolean = board.pokemonBench(position).isEmpty

    override def activePokemon(board: Board = playerBoard): Option[PokemonCard] = board.activePokemon

    override def setActivePokemon(pokemonCard: Option[PokemonCard], board: Board = playerBoard): Unit = {
      board.activePokemon = pokemonCard
      board.removeCardFromHand(pokemonCard.get)
      notifyBoardUpdate()
    }

    override def pokemonBench(board: Board = playerBoard): Seq[Option[PokemonCard]] = board.pokemonBench

    override def putPokemonToBench(pokemonCard: Option[PokemonCard], position: Int, board: Board = playerBoard): Unit = {
      board.putPokemonInBenchPosition(pokemonCard, position)
      board.removeCardFromHand(pokemonCard.get)
      notifyBoardUpdate()
    }

    override def drawCard(board: Board = playerBoard): Unit = {
      if (board.deck.isEmpty) {
        this.notifyObservers(EndGameEvent())
      } else {
        board.addCardsToHand(board.popDeck(1))
        notifyBoardUpdate()
      }
    }

    override def destroyActivePokemon(replacementBenchPosition: Int, board: Board = playerBoard): Unit = {
      board.addCardsToDiscardStack(activePokemon(board).get :: Nil)
      swap(board, None, replacementBenchPosition)
      collapseBench(board)
      notifyBoardUpdate()
    }

    override def retreatActivePokemon(replacementBenchPosition: Int, board: Board = playerBoard): Boolean = {
      var retreatSuccess = false
      val pokemon = activePokemon(board).get
      if (pokemon.status == StatusType.Asleep ) {
        throw new InvalidOperationException("Your pokemon cannot retreat because its status is " + pokemon.status)
      } else if (pokemon.hasEnergies(pokemon.retreatCost)) {
        pokemon.status = StatusType.NoStatus
        pokemon.removeFirstNEnergies(pokemon.retreatCost.size)
        swap(board, Some(pokemon), replacementBenchPosition)
        notifyBoardUpdate()
        retreatSuccess = true
      }
      retreatSuccess
    }

    override def activePokemonStartTurnChecks(board: Board, opponentBoard: Board): Unit = {
      val pokemon = activePokemon(board).get
      if (pokemon.immune) {
        pokemon.immune = false
      }
      pokemon.damageModifier = 0
      if (pokemon.status == StatusType.Poisoned) {
        pokemon.addDamage(PoisonDamage, Seq())
        eventuallyRemoveKOActivePokemon(pokemon, board, opponentBoard, isPokemonInCharge = false)
      }
    }

    override def activePokemonEndTurnChecks(activePokemon: PokemonCard): Unit = activePokemon.status match {
      case StatusType.Paralyzed => activePokemon.status = StatusType.NoStatus
      case StatusType.Asleep => if (new Random().nextInt(2) == 1) activePokemon.status = StatusType.NoStatus
      case _ => // permanent status
    }

    override def addEnergyToPokemon(pokemonCard: PokemonCard, energyCard: EnergyCard, board: Board = playerBoard): Unit = {
      pokemonCard.addEnergy(energyCard)
      board.removeCardFromHand(energyCard)
      notifyBoardUpdate()
    }

    override def evolvePokemon(pokemonCard: PokemonCard, evolution: PokemonCard, board: Board = playerBoard): Option[PokemonCard] = {
      evolution.energiesMap = pokemonCard.energiesMap
      evolution.actualHp = evolution.initialHp - (pokemonCard.initialHp - pokemonCard.actualHp)
      board.addCardsToDiscardStack(pokemonCard :: Nil)
      Some(evolution)
    }

    @throws(classOf[InvalidOperationException])
    override def confirmAttack(attackingBoard: Board, defendingBoard: Board, attack: Attack): Unit ={
      if (activePokemon(attackingBoard).nonEmpty && activePokemon(defendingBoard).nonEmpty) {
        val attackingPokemon = activePokemon(attackingBoard).get
        val defendingPokemon = activePokemon(defendingBoard).get
        if (attackingPokemon.status == StatusType.Asleep ||
          attackingPokemon.status == StatusType.Paralyzed) {
          throw new InvalidOperationException("Your pokemon cannot attack because its status is " + attackingPokemon.status)
        } else {
          if (attackingPokemon.status == StatusType.Confused && new Random().nextInt(2) == 0) {
            attackingPokemon.addDamage(ConfusedDamage, Seq())
          } else if (attackingPokemon.hasEnergies(attack.cost)) {
            attack.effect.get.useEffect(attackingBoard, defendingBoard,this)
          }
          this.notifyObservers(EndTurnEvent())

          eventuallyRemoveKOBenchedPokemon(defendingBoard, attackingBoard)
          eventuallyRemoveKOBenchedPokemon(attackingBoard, defendingBoard)

          eventuallyRemoveKOActivePokemon(attackingPokemon, attackingBoard, defendingBoard, isPokemonInCharge = true)
          eventuallyRemoveKOActivePokemon(defendingPokemon, defendingBoard, attackingBoard, isPokemonInCharge = false)

          notifyBoardUpdate()
        }
      }
    }

    override def damageBenchedPokemon(pokemon: PokemonCard, attackingBoard: Board, defendingBoard: Board, damage: Int): Unit = {
      pokemon.addDamage(damage, Seq(EnergyType.Colorless))
      eventuallyRemoveKOBenchedPokemon(defendingBoard, attackingBoard)
    }

    private def eventuallyRemoveKOActivePokemon(activePokemon: PokemonCard, board: Board, otherBoard: Board, isPokemonInCharge: Boolean): Unit = {
      if (activePokemon.isKO) {
        if (!pokemonBench(board).exists(c => c.nonEmpty)) {
          this.notifyObservers(EndGameEvent()) // Win check
        } else {
          this.notifyObservers(PokemonKOEvent(isPokemonInCharge,board))
          drawPrizeCard(otherBoard)
        }
      }
    }

    private def eventuallyRemoveKOBenchedPokemon(actualBoard: Board, otherBoard: Board): Unit = {
      for ((p, i) <- pokemonBench(actualBoard).zipWithIndex
           if p.nonEmpty && p.get.isKO) {
        destroyBenchPokemon(i, actualBoard)
        drawPrizeCard(otherBoard)
      }
    }

    private def drawPrizeCard(board: Board): Unit = {
      board.addCardsToHand(board.popPrizeCard(1))
      if (board.prizeCards.isEmpty) { // Win check
        this.notifyObservers(EndGameEvent())
      }
    }

    private def destroyBenchPokemon(benchPosition: Int, board: Board): Unit = {
      board.addCardsToDiscardStack(pokemonBench(board)(benchPosition).get :: Nil)
      board.putPokemonInBenchPosition(None, benchPosition)
      collapseBench(board)
    }

    private def collapseBench(board: Board): Unit = {
      for((c, i) <- collapseToLeft(pokemonBench(board)).zipWithIndex) {
        board.putPokemonInBenchPosition(c, i)
      }
    }

    private def swap(board: Board, activePokemon: Option[PokemonCard], benchPosition: Int): Unit = {
      board.activePokemon = board.pokemonBench(benchPosition)
      board.putPokemonInBenchPosition(activePokemon, benchPosition)
    }

    private def collapseToLeft[A](l: Seq[Option[A]]): List[Option[A]] = l match {
      case h :: t if h.isEmpty => collapseToLeft(t) :+ h
      case h :: t if h.nonEmpty => h :: collapseToLeft(t)
      case _ => Nil
    }

    @throws(classOf[CardNotFoundException])
    @scala.annotation.tailrec
    private def buildCardList(deckCards: Seq[DeckCard], setCards: Seq[Card])(cardList: Seq[Card]): Seq[Card] = deckCards match {
      case h :: t if setCards.exists(sc => sc.id == h.id) =>
        buildCardList(t, setCards)(cardList ++ deepCloneCards(List.fill(h.count)(setCards.find(sc => sc.id == h.id).get)))
      case h :: _ => throw new CardNotFoundException("Card " + h.id + " not found in the specified set")
      case _ => cardList
    }

    private def deepCloneCards(l: Seq[Card]): Seq[Card] = l match {
      case h :: t if h.isInstanceOf[PokemonCard] => Seq(h.asInstanceOf[PokemonCard].clonePokemonCard) ++ deepCloneCards(t)
      case h :: t if h.isInstanceOf[EnergyCard] => Seq(h.asInstanceOf[EnergyCard].cloneEnergyCard) ++ deepCloneCards(t)
      case _ => l
    }

    private def buildBoard(cards: Seq[Card]): Board = {
      val board = Board(cards)
      // A hand must have at least one base PokemonCard
      while (!board.hand.exists(c => c.isInstanceOf[PokemonCard] && c.asInstanceOf[PokemonCard].isBase)) {
        board.shuffleDeckWithHand()
        board.addCardsToHand(board.popDeck(InitialHandCardNum))
      }
      board.addCardsToPrizeCards(board.popDeck(InitialPrizeCardNum))
      board
    }

    private def checkNonEmpty[A](args: Option[A]*): Unit = {
      if (args.exists(x => x.isEmpty)) throw new IllegalStateException()
    }

    private def notifyBoardUpdate(): Unit = {
      this.notifyObservers(UpdateBoardsEvent())
    }
  }
}
