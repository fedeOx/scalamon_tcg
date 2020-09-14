package model.game

import model.game.Cards.{Card, PokemonCard}

import scala.util.Random

trait Board {
  def deck: Seq[Card]
  def activePokemon: Option[PokemonCard]
  def activePokemon_=(pokemon: Option[PokemonCard]): Unit
  def hand: Seq[Card]
  def prizeCards: Seq[Card]
  def discardStack: Seq[Card]
  def pokemonBench: Seq[PokemonCard]

  def addCardsToHand(cards: Seq[Card])
  def addCardsToPrizeCards(cards: Seq[Card])
  def addCardsToDiscardStack(cards: Seq[Card])

  def destroyActivePokemon(): Unit
  def addPokemonToBench(pokemon: PokemonCard): Unit
  def removePokemonFromBench(pokemon: PokemonCard): Unit
  def popDeck(popNumber: Int): List[Card]
  def shuffleDeckWithHand(): Unit
  def popPrizeCard(): Card
}

object Board {
  def apply(deck: Seq[Card]): Board = BoardImpl(deck)

  private case class BoardImpl(private var _deck: Seq[Card],
                               override var activePokemon: Option[PokemonCard] = Option.empty) extends Board {
    private var _hand: Seq[Card] = List()
    private var _prizeCards: Seq[Card] = List()
    private var _discardStack: Seq[Card] = List()
    private var _pokemonBench: Seq[PokemonCard] = List()

    override def deck: Seq[Card] = _deck
    override def hand: Seq[Card] = _hand
    override def prizeCards: Seq[Card] = _prizeCards
    override def discardStack: Seq[Card] = _discardStack
    override def pokemonBench: Seq[PokemonCard] = _pokemonBench

    override def addCardsToHand(cards: Seq[Card]): Unit = _hand = _hand ++ cards
    override def addCardsToPrizeCards(cards: Seq[Card]): Unit = _prizeCards = _prizeCards ++ cards
    override def addCardsToDiscardStack(cards: Seq[Card]): Unit = ???

    override def destroyActivePokemon(): Unit = ???
    override def addPokemonToBench(pokemon: PokemonCard): Unit = ???
    override def removePokemonFromBench(pokemon: PokemonCard): Unit = ???
    override def popDeck(popNumber: Int): List[Card] = deck match {
      case h :: t if popNumber > 0 => _deck = t; h :: popDeck(popNumber - 1)
      case _ => Nil
    }
    override def shuffleDeckWithHand(): Unit = {
      _deck = _deck ++ _hand
      _hand = List()
      _deck = Random.shuffle(_deck)
    }
    override def popPrizeCard(): Card = ???
  }
}


