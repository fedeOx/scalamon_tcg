package model.core

import common.Observable
import model.exception.{BenchPokemonException, CardNotFoundException}
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

  def addPlayerActivePokemon(pokemon: PokemonCard): Unit = playerBoard.activePokemon = Some(pokemon)

  def addOpponentActivePokemon(pokemon: PokemonCard): Unit = opponentBoard.activePokemon = Some(pokemon)

  @throws(classOf[BenchPokemonException])
  def addPlayerPokemonToBench(pokemon: PokemonCard, position: Int): Unit = playerBoard.addPokemonToBench(pokemon, position)

  def addOpponentPokemonToBench(pokemon: PokemonCard, position: Int): Unit = opponentBoard.addPokemonToBench(pokemon, position)

  def destroyPlayerActivePokemon(): Unit = playerBoard.activePokemon = None

  def destroyOpponentActivePokemon(): Unit = opponentBoard.activePokemon = None

  @throws(classOf[BenchPokemonException])
  def removePokemonFromPlayerBench(position: Int): Unit = playerBoard.removePokemonFromBench(position)

  def removePokemonFromOpponentBench(pokemon: PokemonCard, position: Int): Unit = opponentBoard.removePokemonFromBench(position)

  def drawPlayerCard(): Unit = playerBoard.addCardsToHand(playerBoard.popDeck(1))

  def drawOpponentCard(): Unit = opponentBoard.addCardsToHand(opponentBoard.popDeck(1))

  def drawPlayerPrizeCard(): Unit = playerBoard.addCardsToHand(playerBoard.popPrizeCard(1))

  def drawOpponentPrizeCard(): Unit = opponentBoard.addCardsToHand(opponentBoard.popPrizeCard(1))

  def isPlayerActivePokemonEmpty: Boolean = playerBoard.activePokemon.isEmpty
  def isPlayerBenchLocationEmpty(position: Int): Boolean = playerBoard.pokemonBench(position).isEmpty

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
    // A hand must have at least one PokemonCard
    while (!board.hand.exists(c => c.isInstanceOf[PokemonCard])) {
      board.shuffleDeckWithHand()
      board.addCardsToHand(board.popDeck(InitialHandCardNum))
    }
    board.addCardsToPrizeCards(board.popDeck(InitialPrizeCardNum))
    board
  }
}
