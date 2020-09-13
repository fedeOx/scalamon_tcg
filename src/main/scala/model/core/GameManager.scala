package model.core

import common.Observable
import model.exception.CardNotFoundException
import model.game.Cards.{Card, PokemonCard}
import model.game.{Board, DeckCard, GameField}

object GameManager extends Observable {

  val InitialHandCardNum = 7
  val InitialPrizeCardNum = 6

  private var playerBoard: Board = _
  private var opponentBoard: Board = _

  def initBoards(playerDeckCards: Seq[DeckCard], opponentDeckCards: Seq[DeckCard], cardsSet: Seq[Card]): GameField = {
    val playerCards: Seq[Card] = buildCardList(playerDeckCards, cardsSet)(List())
    val opponentCards: Seq[Card] = buildCardList(opponentDeckCards, cardsSet)(List())
    playerBoard = buildBoard(playerCards)
    opponentBoard = buildBoard(opponentCards)
    GameField(playerBoard, opponentBoard)
  }

  def addPlayerActivePokemon(pokemon: PokemonCard): Unit = ???
  def addOpponentActivePokemon(pokemon: PokemonCard): Unit = ???
  def addPlayerPokemonToBench(pokemon: PokemonCard, position: Int): Unit = ???
  def addOpponentPokemonToBench(pokemon: PokemonCard, position: Int): Unit = ???
  def destroyPlayerActivePokemon(): Unit = ???
  def destroyOpponentActivePokemon(): Unit = ???
  def removePokemonFromPlayerBench(pokemon: PokemonCard, position: Int): Unit = ???
  def removePokemonFromOpponentBench(pokemon: PokemonCard, position: Int): Unit = ???
  def drawPlayerCard(): Unit = ???
  def drawOpponentCard(): Unit = ???
  def drawPlayerPrizeCard(): Unit = ???
  def drawOpponentPrizeCard(): Unit = ???

  def isPlayerActivePokemonEmpty: Boolean = ???
  def isPlayerBenchLocationEmpty(position: Int): Boolean = ???

  def confirmPlayerAttack(damage: Int): Unit = ???
  def confirmOpponentAttack(damage: Int): Unit = ???

  def swapPlayerActivePokemon(benchPosition: Int): Unit = ???
  def swapOpponentActivePokemon(benchPosition: Int): Unit = ???

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
    board.addCardsToHand(board.popDeck(InitialHandCardNum))
    board.addCardsToPrizeCards(board.popDeck(InitialPrizeCardNum))
    board
  }
}