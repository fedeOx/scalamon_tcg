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

  def addCardsToHand(cards: Seq[Card])
  def addCardsToPrizeCards(cards: Seq[Card])
  def addCardsToDiscardStack(cards: Seq[Card])

  /**
   * Adds the specified pokemon card to the specified bench position
   * @param pokemon the pokemon card to be added
   * @param position the bench position
   * @throws model.exception.BenchPokemonException if the specified position is out of bound
   */
  @throws(classOf[BenchPokemonException])
  def addPokemonToBench(pokemon: PokemonCard, position: Int): Unit

  /**
   * Removes a pokemon from the specified bench position
   * @param position the bench position
   * @throws model.exception.BenchPokemonException if the specified position is out of bound
   */
  @throws(classOf[BenchPokemonException])
  def removePokemonFromBench(position: Int): Unit
  def popDeck(popNumber: Int): List[Card]
  def shuffleDeckWithHand(): Unit
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
    override def addCardsToPrizeCards(cards: Seq[Card]): Unit = _prizeCards = _prizeCards ++ cards
    override def addCardsToDiscardStack(cards: Seq[Card]): Unit = ???

    override def addPokemonToBench(pokemon: PokemonCard, position: Int): Unit =
      _pokemonBench = updateBenchPosition(Some(pokemon), _pokemonBench, position)

    override def removePokemonFromBench(position: Int): Unit =
      _pokemonBench = updateBenchPosition(None, _pokemonBench, position)

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


