package model.core

import common.Observer
import model.event.Events.Event
import model.event.Events.Event.{BuildGameField, ShowDeckCards, UpdateBoards}
import model.exception.{CardNotFoundException, InvalidOperationException}
import model.game.Cards.EnergyCard.EnergyCardType
import model.game.Cards.{Card, EnergyCard, PokemonCard}
import model.game.EnergyType.EnergyType
import model.game.{Board, DeckCard, DeckType, EnergyType, SetType, StatusType}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{GivenWhenThen, OneInstancePerTest}
import org.scalatest.flatspec.AnyFlatSpec

class GameManagerTest extends AnyFlatSpec with MockFactory with GivenWhenThen  {

  behavior of "The GameManager"

  val cardsSet: Seq[Card] = DataLoader.loadSet(SetType.Base)
  var playerDeckCards: Seq[DeckCard] = DataLoader.loadSingleDeck(SetType.Base, DeckType.Base1)
  val opponentDeckCards: Seq[DeckCard] = DataLoader.loadSingleDeck(SetType.Base, DeckType.Base2)
  val observerMock: Observer = mock[Observer]
  GameManager.addObserver(observerMock)

  it should "build game field correctly and notify observers when it happens" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[BuildGameField]
      e.asInstanceOf[BuildGameField].playerBoard.isInstanceOf[Board]
      e.asInstanceOf[BuildGameField].opponentBoard.isInstanceOf[Board]
    }})
    GameManager.initBoards(playerDeckCards, opponentDeckCards, cardsSet)
    val playerBoard = GameManager.playerBoard
    val opponentBoard = GameManager.opponentBoard
    assert(playerBoard.deck.nonEmpty && opponentBoard.deck.nonEmpty)
    assert(playerBoard.activePokemon.isEmpty && opponentBoard.activePokemon.isEmpty)
    assert(playerBoard.hand.size == GameManager.InitialHandCardNum && opponentBoard.hand.size == GameManager.InitialHandCardNum)
    assert(playerBoard.prizeCards.size == GameManager.InitialPrizeCardNum && opponentBoard.prizeCards.size == GameManager.InitialPrizeCardNum)
    assert(playerBoard.discardStack.isEmpty && opponentBoard.discardStack.isEmpty)
    playerBoard.pokemonBench.foreach(c => assert(c.isEmpty))
    opponentBoard.pokemonBench.foreach(c => assert(c.isEmpty))
  }

  it should "build game field in a way that initially each player must have at least one base PokemonCard in their hands" in {
    for (set <- SetType.values) {
      for (deck <- DeckType.values
           if deck.setType == set) {
        val cardsSet: Seq[Card] = DataLoader.loadSet(set)
        val playerDeckCards: Seq[DeckCard] = DataLoader.loadSingleDeck(set, deck)
        val opponentDeckCards: Seq[DeckCard] = DataLoader.loadSingleDeck(set, deck)
        (observerMock.update _).expects(where {e: Event => {
          e.isInstanceOf[BuildGameField]
          e.asInstanceOf[BuildGameField].playerBoard.isInstanceOf[Board]
          e.asInstanceOf[BuildGameField].opponentBoard.isInstanceOf[Board]
        }})
        GameManager.initBoards(playerDeckCards, opponentDeckCards, cardsSet)
        assert(GameManager.playerBoard.hand.exists(c => c.isInstanceOf[PokemonCard] && c.asInstanceOf[PokemonCard].isBase)
          && GameManager.opponentBoard.hand.exists(c => c.isInstanceOf[PokemonCard] && c.asInstanceOf[PokemonCard].isBase))
      }
    }
  }

  it should "notify observers when a card is draw from player deck" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdateBoards]
    }})
    GameManager.drawCard(GameManager.playerBoard)
  }

  it should "notify observers when player active pokemon or player bench is updated" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdateBoards]
    }}).repeat(3)

    Given("a new active pokemon")
    val newActivePokemon: PokemonCard = PokemonCard("1", "base1", Seq(EnergyType.Colorless), "myActivePokemon", 100, Nil, Nil, Nil, "", Nil)
    When("it is set as active pokemon")
    GameManager.setActivePokemon(Some(newActivePokemon))
    Then("the active pokemon location should be not empty")
    assert(GameManager.playerBoard.activePokemon.nonEmpty && GameManager.playerBoard.activePokemon.get == newActivePokemon)

    Given("a new bench pokemon and a bench position")
    val newBenchPokemon: PokemonCard = PokemonCard("2", "base1", Seq(EnergyType.Colorless), "myBenchPokemon", 100, Nil, Nil, Nil, "", Nil)
    val benchPosition = 0
    When("the bench pokemon is put int the bench at the given position")
    GameManager.putPokemonToBench(Some(newBenchPokemon), benchPosition)
    Then("the bench position should be not empty")
    assert(GameManager.playerBoard.pokemonBench(benchPosition).nonEmpty && GameManager.playerBoard.pokemonBench(benchPosition).get == newBenchPokemon)

    Given("a replacement bench position")
    val replacementBenchPosition = benchPosition
    When("the active pokemon is destroyed")
    GameManager.destroyActivePokemon(replacementBenchPosition)
    Then("the active pokemon is replaced with the bench pokemon at the given replacement bench position")
    assert(GameManager.playerBoard.activePokemon.nonEmpty && GameManager.playerBoard.activePokemon.get == newBenchPokemon &&
      GameManager.playerBoard.pokemonBench(benchPosition).isEmpty)
  }

  it should "retreat an active pokemon if it has enough energies and notify" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdateBoards]
    }}).anyNumberOfTimes()

    Given("an active pokemon with a retreat cost")
    val retreatCost: Seq[EnergyType] = Seq(EnergyType.Colorless, EnergyType.Colorless)
    val activePokemon: PokemonCard = PokemonCard("1", "base1", Seq(EnergyType.Colorless), "myActivePokemon", 100, Nil,
      Nil, retreatCost, "", Nil)
    GameManager.setActivePokemon(Some(activePokemon))
    val energyCard: EnergyCard = EnergyCard("1", "base-1", EnergyType.Grass, EnergyCardType.basic)

    Given("a pokemon in a bench position")
    val benchPokemon: PokemonCard = PokemonCard("2", "base1", Seq(EnergyType.Colorless), "myBenchPokemon", 100, Nil, Nil, Nil, "", Nil)
    val benchPosition = 0
    GameManager.putPokemonToBench(Some(benchPokemon), benchPosition)

    When("the active pokemon is retreated and does not have enough energies")
    GameManager.retreatActivePokemon(benchPosition, GameManager.playerBoard)

    Then("the active pokemon stays where it is because the retreat cannot happen")
    assert(GameManager.activePokemon(GameManager.playerBoard).nonEmpty && GameManager.activePokemon(GameManager.playerBoard).get == activePokemon)

    When("the active pokemon is retreated and has enough energies")
    GameManager.addEnergyToPokemon(activePokemon, energyCard, GameManager.playerBoard)
    GameManager.addEnergyToPokemon(activePokemon, energyCard, GameManager.playerBoard)
    GameManager.retreatActivePokemon(benchPosition, GameManager.playerBoard)

    Then("the active pokemon swaps with the benched pokemon at the specified bench position")
    assert(GameManager.activePokemon(GameManager.playerBoard).nonEmpty && GameManager.activePokemon(GameManager.playerBoard).get == benchPokemon)

    Given("an asleep active pokemon")
    GameManager.activePokemon(GameManager.playerBoard).get.status = StatusType.Asleep
    When("the active pokemon tries to retreat")
    Then("an InvalidOperationException should be trown")
    intercept[InvalidOperationException] {
      GameManager.retreatActivePokemon(benchPosition, GameManager.playerBoard)
    }
  }

  /*
  it should "evolve a pokemon if required" in {
    (observerMock.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdateBoards]
    }}).anyNumberOfTimes()

    Given("an active pokemon in a specific game condition and its evolution")
    val activePokemon: PokemonCard = PokemonCard("1", "base1", Seq(EnergyType.Colorless), "myActivePokemon", 100, Nil,
      Nil, Nil, "", Nil)
    val damage = 50
    activePokemon.status = StatusType.Confused
    activePokemon.actualHp = activePokemon.initialHp - damage
    val energyCard: EnergyCard = EnergyCard("1", "base-1", EnergyType.Grass, EnergyCardType.basic)
    GameManager.addEnergyToPokemon(activePokemon, energyCard, GameManager.playerBoard)
    GameManager.addEnergyToPokemon(activePokemon, energyCard, GameManager.playerBoard)
    val evolution: PokemonCard = PokemonCard("2", "base1", Seq(EnergyType.Colorless), "myEvolution", 120, Nil,
      Nil, Nil, "myActivePokemon", Nil)

    When("the active pokemon is evolved")
  }
  FALLO FARE AL CONTROLLER
   */

  // TESTARE confirmAttack

  it should "throw CardNotFoundException if a DeckCard does not exists in Cards set" in {
    val nonExistentCard: DeckCard = DeckCard("nonExistentID", "sausages", "very rare", 3)
    playerDeckCards = playerDeckCards :+ nonExistentCard

    intercept[CardNotFoundException] {
      GameManager.initBoards(playerDeckCards, opponentDeckCards, cardsSet)
    }
  }
}
