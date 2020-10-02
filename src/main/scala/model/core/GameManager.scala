package model.core

import common.Observable
import model.event.Events.Event
import model.exception.{CardNotFoundException, InvalidOperationException}
import model.game.Cards.{Card, EnergyCard, PokemonCard}
import model.game.{Attack, Board, DeckCard, StatusType}

import scala.util.Random

trait GameManager extends Observable {
  def initBoards(playerDeckCards: Seq[DeckCard], opponentDeckCards: Seq[DeckCard], cardsSet: Seq[Card]): Unit
  def playerBoard: Board
  def opponentBoard: Board
  def isActivePokemonEmpty(board: Board = playerBoard): Boolean
  def isBenchLocationEmpty(position: Int, board: Board = playerBoard): Boolean
  def activePokemon(board: Board = playerBoard): Option[PokemonCard]
  def setActivePokemon(pokemonCard: Option[PokemonCard], board: Board = playerBoard): Unit
  def pokemonBench(board: Board = playerBoard): Seq[Option[PokemonCard]]
  def putPokemonToBench(pokemonCard: Option[PokemonCard], position: Int, board: Board = playerBoard): Unit
  def drawCard(board: Board = playerBoard): Unit
  def destroyActivePokemon(replacementBenchPosition: Int, board: Board = playerBoard): Unit
  def retreatActivePokemon(replacementBenchPosition: Int, board: Board = playerBoard): Unit
  def activePokemonStartTurnChecks(board: Board, opponentBoard: Board): Unit
  def activePokemonEndTurnChecks(activePokemon: PokemonCard): Unit
  def addEnergyToPokemon(pokemonCard: PokemonCard, energyCard: EnergyCard, board: Board = playerBoard): Unit
  def evolvePokemon(pokemonCard: PokemonCard, evolution: PokemonCard, board: Board = playerBoard): Option[PokemonCard]
  @throws(classOf[InvalidOperationException])
  def confirmAttack(attackingBoard: Board, defendingBoard: Board, attack: Attack): Unit
}

object GameManager {
  def apply(): GameManager = GameManagerImpl()

  private case class GameManagerImpl() extends GameManager {
    val InitialHandCardNum = 7
    val InitialPrizeCardNum = 6
    val PoisonDamage = 10
    val ConfusedDamage = 30

    private var _playerBoard: Option[Board] = None
    private var _opponentBoard: Option[Board] = None

    override def initBoards(playerDeckCards: Seq[DeckCard], opponentDeckCards: Seq[DeckCard], cardsSet: Seq[Card]): Unit = {
      val playerCards: Seq[Card] = buildCardList(playerDeckCards, cardsSet)(List())
      val opponentCards: Seq[Card] = buildCardList(opponentDeckCards, cardsSet)(List())

      _playerBoard = Some(buildBoard(playerCards))
      _opponentBoard = Some(buildBoard(opponentCards))
      this.notifyObservers(Event.buildGameFieldEvent(_playerBoard.get, _opponentBoard.get))
    }

    override def playerBoard: Board = {
      checkNonEmpty(_playerBoard)
      _playerBoard.get
    }

    override def opponentBoard: Board = {
      checkNonEmpty(_opponentBoard)
      _opponentBoard.get
    }

    override def isActivePokemonEmpty(board: Board = playerBoard): Boolean = board.activePokemon.isEmpty

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
        this.notifyObservers(Event.endGameEvent())
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

    override def retreatActivePokemon(replacementBenchPosition: Int, board: Board = playerBoard): Unit = {
      val pokemon = activePokemon(board).get
      if (pokemon.status == StatusType.Asleep ) {
        throw new InvalidOperationException("Your pokemon cannot retreat because its status is " + pokemon.status)
      } else if (pokemon.hasEnergies(pokemon.retreatCost)) {
        pokemon.status = StatusType.NoStatus
        pokemon.removeFirstNEnergies(pokemon.retreatCost.size)
        swap(board, Some(pokemon), replacementBenchPosition)
        notifyBoardUpdate()
      }
    }

    override def activePokemonStartTurnChecks(board: Board, opponentBoard: Board): Unit = {
      val pokemon = activePokemon(board).get
      if (pokemon.immune) {
        pokemon.immune = false
      }
      if (pokemon.status == StatusType.Poisoned) {
        pokemon.addDamage(PoisonDamage, Seq())
        eventuallyRemoveKOActivePokemon(pokemon, board, opponentBoard, isPokemonInCharge = true) // GESTIONE KO + PESCATA CARTA PREMIO + CONTROLLO VITTORIA
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
          } else {
            attack.effect.get.useEffect(attackingBoard, defendingBoard)
            this.notifyObservers(Event.attackEnded())
          }
          eventuallyRemoveKOBenchedPokemon(defendingBoard, attackingBoard)
          eventuallyRemoveKOBenchedPokemon(attackingBoard, defendingBoard)

          eventuallyRemoveKOActivePokemon(attackingPokemon, attackingBoard, defendingBoard, isPokemonInCharge = true)
          eventuallyRemoveKOActivePokemon(defendingPokemon, defendingBoard, attackingBoard, isPokemonInCharge = false)

          notifyBoardUpdate()
        }
      }
    }

    private def eventuallyRemoveKOActivePokemon(activePokemon: PokemonCard, board: Board, otherBoard: Board, isPokemonInCharge: Boolean): Unit = {
      if (activePokemon.isKO) {
        if (!pokemonBench(board).exists(c => c.nonEmpty)) {
          this.notifyObservers(Event.endGameEvent()) // Win check
        } else {
          this.notifyObservers(Event.pokemonKOEvent(isPokemonInCharge,board))
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
        this.notifyObservers(Event.endGameEvent())
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
      case h :: t if setCards.exists(sc => sc.imageId == h.imageId) =>
        buildCardList(t, setCards)(cardList ++ deepCloneCards(List.fill(h.count)(setCards.find(sc => sc.imageId == h.imageId).get)))
      case h :: _ => throw new CardNotFoundException("Card " + h.imageId + " not found in the specified set")
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
      this.notifyObservers(Event.updateBoardsEvent())
    }
  }
}
