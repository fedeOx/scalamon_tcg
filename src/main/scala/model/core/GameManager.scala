package model.core

import common.Observable
import model.event.Events.Event
import model.exception.CardNotFoundException
import model.game.Cards.{Card, EnergyCard, PokemonCard}
import model.game.{Attack, Board, DeckCard, StatusType}

import scala.util.Random

object GameManager extends Observable {

  val InitialHandCardNum = 7
  val InitialPrizeCardNum = 6

  private var _playerBoard: Option[Board] = None
  private var _opponentBoard: Option[Board] = None

  def initBoards(playerDeckCards: Seq[DeckCard], opponentDeckCards: Seq[DeckCard], cardsSet: Seq[Card]): Unit = {
    val playerCards: Seq[Card] = buildCardList(playerDeckCards, cardsSet)(List())
    val opponentCards: Seq[Card] = buildCardList(opponentDeckCards, cardsSet)(List())

    _playerBoard = Some(buildBoard(playerCards))
    _opponentBoard = Some(buildBoard(opponentCards))
    this.notifyObservers(Event.buildGameFieldEvent(_playerBoard.get, _opponentBoard.get))
  }

  def playerBoard: Board = {
    checkNonEmpty(_playerBoard)
    _playerBoard.get
  }

  def opponentBoard: Board = {
    checkNonEmpty(_opponentBoard)
    _opponentBoard.get
  }

  def isPlayerActivePokemonEmpty: Boolean = playerBoard.activePokemon.isEmpty

  def isPlayerBenchLocationEmpty(position: Int): Boolean = playerBoard.pokemonBench(position).isEmpty

  def playerActivePokemon: Option[PokemonCard] = playerBoard.activePokemon

  def playerActivePokemon_=(pokemonCard: Option[PokemonCard]): Unit = {
    playerBoard.activePokemon = pokemonCard
    playerBoard.removeCardFromHand(pokemonCard.get)
    notifyBoardUpdate()
  }

  def playerPokemonBench: Seq[Option[PokemonCard]] = playerBoard.pokemonBench

  def putPokemonToPlayerBench(pokemonCard: Option[PokemonCard], position: Int): Unit = {
    playerBoard.putPokemonInBenchPosition(pokemonCard, position)
    playerBoard.removeCardFromHand(pokemonCard.get)
    notifyBoardUpdate()
  }

  def drawPlayerCard(): Unit = {
    playerBoard.addCardsToHand(playerBoard.popDeck(1))
    notifyBoardUpdate()
  }

  def drawPlayerPrizeCard(): Unit = {
    playerBoard.addCardsToHand(playerBoard.popPrizeCard(1))
    notifyBoardUpdate()
  }

  def destroyPlayerActivePokemon(replacementBenchPosition: Int): Unit = {
    playerBoard.addCardsToDiscardStack(GameManager.playerBoard.activePokemon.get :: Nil)
    swap(None, replacementBenchPosition)
    notifyBoardUpdate()
  }

  def retreatPlayerActivePokemon(replacementBenchPosition: Int): Unit = {
    val activePokemon = GameManager.playerBoard.activePokemon.get
    if (activePokemon.hasEnergies(activePokemon.retreatCost)) {
      activePokemon.status = StatusType.NoStatus
      swap(Some(activePokemon), replacementBenchPosition)
      notifyBoardUpdate()
    }
  }

  def addEnergyToPokemon(pokemonCard: PokemonCard, energyCard: EnergyCard): Unit = {
    pokemonCard.addEnergy(energyCard)
    playerBoard.removeCardFromHand(energyCard)
    notifyBoardUpdate()
  }

  def evolvePokemon(pokemonCard: PokemonCard, evolution: PokemonCard): Option[PokemonCard] = {
    evolution.energiesMap = pokemonCard.energiesMap
    evolution.status = pokemonCard.status
    evolution.actualHp = pokemonCard.initialHp - (pokemonCard.initialHp - pokemonCard.actualHp)
    playerBoard.addCardsToDiscardStack(pokemonCard :: Nil)
    Some(evolution)
  }

  def confirmAttack(attack: Attack): Unit ={
    if (playerActivePokemon.nonEmpty && opponentBoard.activePokemon.nonEmpty) {
      attack.effect.get.useEffect()
      if (playerActivePokemon.get.isKO || opponentBoard.activePokemon.get.isKO) {
        this.notifyObservers(Event.pokemonKOEvent())
      }
      if (playerPokemonBench.filter(c => c.nonEmpty).exists(c => c.get.isKO)) {
        for((c, i) <- collapseToLeft(playerPokemonBench).zipWithIndex) {
          putPokemonToPlayerBench(c, i)
        }
      }
      notifyBoardUpdate()
    }
    //if (playerActivePokemon.get.isKO)
    // Manca da controllare se qualche pokemon nel campo di gioco è andato KO
    // se muore il pokemon del giocatore o dell'ia -> GameManager.notifyObservers(Event.pokemonKOEvent())
    // sempre -> notifyBoardUpdate();
  }

  private def swap(activePokemon: Option[PokemonCard], benchPosition: Int): Unit = {
    playerBoard.activePokemon = playerBoard.pokemonBench(benchPosition)
    playerBoard.putPokemonInBenchPosition(activePokemon, benchPosition)
  }

  private def collapseToLeft[A](bench: Seq[Option[A]]): List[Option[A]] = bench match {
    case h :: t if h.isEmpty => collapseToLeft(t) :+ h
    case h :: t if h.nonEmpty => h :: collapseToLeft(t)
    case _ => Nil
  }

  @throws(classOf[CardNotFoundException])
  @scala.annotation.tailrec
  private def buildCardList(deckCards: Seq[DeckCard], setCards: Seq[Card])(cardList: Seq[Card]): Seq[Card] = deckCards match {
    case h :: t if setCards.exists(sc => sc.imageId == h.imageId) =>
        buildCardList(t, setCards)(cardList ++ List.fill(h.count)(deepCloneCards(setCards).find(sc => sc.imageId == h.imageId).get))
    case h :: _ => throw new CardNotFoundException("Card " + h.imageId + " not found in the specified set")
    case _ => cardList
  }

  private def deepCloneCards(l: Seq[Card]): Seq[Card] = l match {
    case h :: t if h.isInstanceOf[PokemonCard] => Seq(h.asInstanceOf[PokemonCard].clonePokemoCard) ++ deepCloneCards(t)
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
    this.notifyObservers(Event.updatePlayerBoardEvent())
  }
}
