package model.core

import common.Observable
import model.event.Events.Event
import model.exception.{BenchPokemonException, CardNotFoundException}
import model.game.Cards.{Card, EnergyCard, PokemonCard}
import model.game.{Board, DeckCard, StatusType}

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
    notifyBoardUpdate()
  }

  def playerPokemonBench: Seq[Option[PokemonCard]] = playerBoard.pokemonBench

  def putPokemonToPlayerBench(pokemonCard: Option[PokemonCard], position: Int): Unit = {
    playerBoard.putPokemonInBenchPosition(pokemonCard, position)
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
    notifyBoardUpdate()
  }

  def evolvePokemon(pokemonCard: PokemonCard, evolution: PokemonCard): Option[PokemonCard] = {
    evolution.energiesMap = pokemonCard.energiesMap
    evolution.status = pokemonCard.status
    evolution.actualHp = pokemonCard.initialHp - (pokemonCard.initialHp - pokemonCard.actualHp)
    playerBoard.addCardsToDiscardStack(pokemonCard :: Nil)
    Some(evolution)
  }

  private def swap(activePokemon: Option[PokemonCard], benchPosition: Int): Unit = {
    playerBoard.activePokemon = playerBoard.pokemonBench(benchPosition)
    playerBoard.putPokemonInBenchPosition(activePokemon, benchPosition)
  }
  /*
  def addOpponentActivePokemon(pokemon: PokemonCard): Unit = {
    checkNonEmpty(opponentBoard)
    opponentBoard.get.activePokemon = Some(pokemon)
    this.notifyObservers(Event.updateOpponentBoardEvent())
  }

  def addOpponentPokemonToBench(pokemon: PokemonCard, position: Int): Unit = {
    checkNonEmpty(opponentBoard)
    opponentBoard.get.addPokemonToBench(pokemon, position)
    this.notifyObservers(Event.updateOpponentBoardEvent())
  }

  def destroyOpponentActivePokemon(): Unit = {
    checkNonEmpty(opponentBoard)
    opponentBoard.get.addCardsToDiscardStack(opponentBoard.get.activePokemon.get :: Nil)
    opponentBoard.get.activePokemon = None
    this.notifyObservers(Event.updateOpponentBoardEvent())
  }

  def removePokemonFromOpponentBench(pokemon: PokemonCard, position: Int): Unit = {
    checkNonEmpty(opponentBoard)
    opponentBoard.get.removePokemonFromBench(position)
    this.notifyObservers(Event.updateOpponentBoardEvent())
  }

  def drawOpponentCard(): Unit = {
    checkNonEmpty(opponentBoard)
    opponentBoard.get.addCardsToHand(opponentBoard.get.popDeck(1))
    this.notifyObservers(Event.updateOpponentBoardEvent())
  }

  def drawOpponentPrizeCard(): Unit = {
    checkNonEmpty(opponentBoard)
    opponentBoard.get.addCardsToHand(opponentBoard.get.popPrizeCard(1))
    this.notifyObservers(Event.updateOpponentBoardEvent())
  }
  */

  @throws(classOf[CardNotFoundException])
  @scala.annotation.tailrec
  private def buildCardList(deckCards: Seq[DeckCard], setCards: Seq[Card])(cardList: Seq[Card]): Seq[Card] = deckCards match {
    case h :: t if setCards.exists(sc => sc.imageId == h.imageId) =>
      buildCardList(t, setCards)(cardList ++ List.fill(h.count)(setCards.find(sc => sc.imageId == h.imageId).get))
    case h :: _ => throw new CardNotFoundException("Card " + h.imageId + " not found in the specified set")
    case _ => cardList
  }

  private def buildBoard(cards: Seq[Card]): Board = {
    val board = Board(cards)
    // A hand must have at least one PokemonCard
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
