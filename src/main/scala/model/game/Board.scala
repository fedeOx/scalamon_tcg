package model.game

import model.exception.BenchPokemonException
import model.game.Cards.{Card, PokemonCard}

import scala.util.Random

trait Board {
  def deck: Seq[Card]
  def activePokemon: Option[PokemonCard]
  def activePokemon_=(pokemon: Option[PokemonCard]): Unit
  def hand: Seq[Card]
  def prizeCards: Seq[Card]
  def discardStack: Seq[Card]
  def pokemonBench: Seq[Option[PokemonCard]]

  /**
   * Adds the specified sequence of cards to the hand of the player
   * @param cards the cards to be added
   */
  def addCardsToHand(cards: Seq[Card])

  /**
   * Removes the specified card from the player hand
   * @param card the card to be removed
   */
  def removeCardFromHand(card: Card): Unit

  /**
   * Adds the specified sequence of cards to the prize cards stack
   * @param cards the cards to be added
   */
  def addCardsToPrizeCards(cards: Seq[Card])

  /**
   * Adds the specified sequence of card to the discard stack
   * @param cards the cards to be added
   */
  def addCardsToDiscardStack(cards: Seq[Card])

  /**
   * Replace the specified pokemon with the bench pokemon in the specified bench position
   * @param newPokemon the pokemon to be placed in the bench
   * @param position the position in the bench
   * @throws model.exception.BenchPokemonException if the specified position is out of bound
   */
  @throws(classOf[BenchPokemonException])
  def putPokemonInBenchPosition(newPokemon: Option[PokemonCard], position: Int): Unit

  /**
   * Draws the specified number of cards from the deck
   * @param popNumber the number of card to be draw
   * @return the list of card draw
   */
  def popDeck(popNumber: Int): List[Card]

  /**
   * Adds hand cards to the deck and then shuffles it
   */
  def shuffleDeckWithHand(): Unit

  /**
   * Draws the specified number of cards from the prize cards stack
   * @param popNumber the number of card to be draw
   * @return the list of card draw
   */
  def popPrizeCard(popNumber: Int): List[Card]
}

object Board {
  def apply(deck: Seq[Card]): Board = BoardImpl(deck)

  private case class BoardImpl(private var _deck: Seq[Card],
                               override var activePokemon: Option[PokemonCard] = Option.empty) extends Board {
    private var _hand: Seq[Card] = List()
    private var _prizeCards: Seq[Card] = List()
    private var _discardStack: Seq[Card] = List()
    private var _pokemonBench: Seq[Option[PokemonCard]] = None :: None :: None :: None :: None :: Nil

    override def deck: Seq[Card] = _deck
    override def hand: Seq[Card] = _hand
    override def prizeCards: Seq[Card] = _prizeCards
    override def discardStack: Seq[Card] = _discardStack
    override def pokemonBench: Seq[Option[PokemonCard]] = _pokemonBench

    override def addCardsToHand(cards: Seq[Card]): Unit = _hand = _hand ++ cards
    override def removeCardFromHand(card: Card): Unit = _hand = _hand.filter(c => c ne card)
    override def addCardsToPrizeCards(cards: Seq[Card]): Unit = _prizeCards = _prizeCards ++ cards
    override def addCardsToDiscardStack(cards: Seq[Card]): Unit = _discardStack = _discardStack ++ cards

    def putPokemonInBenchPosition(newPokemon: Option[PokemonCard], position: Int): Unit = {
      _pokemonBench = updateBenchPosition(newPokemon, _pokemonBench, position)
    }

    override def popDeck(popNumber: Int): List[Card] = _deck match {
      case h :: t if popNumber > 0 => _deck = t; h :: popDeck(popNumber - 1)
      case _ => Nil
    }

    override def shuffleDeckWithHand(): Unit = {
      _deck = _deck ++ _hand
      _hand = List()
      _deck = Random.shuffle(_deck)
    }
    override def popPrizeCard(popNumber: Int): List[Card] = _prizeCards match {
      case h :: t if popNumber > 0 => _prizeCards = t; h :: popPrizeCard(popNumber - 1)
      case _ => Nil
    }

    private def updateBenchPosition(pokemon: Option[PokemonCard], bench: Seq[Option[PokemonCard]], position: Int): List[Option[PokemonCard]] = bench match {
      case h :: t if position > 0 => h :: updateBenchPosition(pokemon, t, position-1)
      case _ :: t => pokemon :: t
      case _ => throw new BenchPokemonException("The specified position is out of bound")
    }
  }
}


