package model.core

import common.Observer
import model.event.Events.Event
import model.event.Events.Event.{BuildGameField, UpdateBoards}
import model.exception.{CardNotFoundException, InvalidOperationException}
import model.game.Cards.{Card, EnergyCard, PokemonCard}
import model.game.DeckType.DeckType
import model.game.EnergyType.EnergyType
import model.game.SetType.SetType
import model.game.{Board, DeckCard, DeckType, EnergyType, SetType, StatusType}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{GivenWhenThen}
import org.scalatest.flatspec.AnyFlatSpec

class GameManagerTest extends AnyFlatSpec with MockFactory with GivenWhenThen  {
  behavior of "The GameManager"

  trait BaseContext {
    val gameManager: GameManager = GameManager()

    def attachObserver(): Observer = {
      val observerMock: Observer = mock[Observer]
      gameManager.addObserver(observerMock)
      observerMock
    }

    def initBoards(setType: SetType, deckType: DeckType, opponentDeckType: DeckType): Unit = {
      val dataLoader: DataLoader = DataLoader()
      val cardsSet: Seq[Card] = dataLoader.loadSet(setType)
      val playerDeckCards: Seq[DeckCard] = dataLoader.loadSingleDeck(deckType)
      val opponentDeckCards: Seq[DeckCard] = dataLoader.loadSingleDeck(opponentDeckType)
      gameManager.initBoards(playerDeckCards, opponentDeckCards, cardsSet)
    }
  }

  trait DataLoaderContext {
    val dataLoader: DataLoader = DataLoader()

    def getPokemon(set: SetType, pokemonName: String): Option[PokemonCard] =
      dataLoader.loadSet(set).filter(p => p.isInstanceOf[PokemonCard] && p.asInstanceOf[PokemonCard].name == pokemonName)
        .map(p => p.asInstanceOf[PokemonCard]).headOption

    def getEnergy(set: SetType, energyType: EnergyType): Option[EnergyCard] =
      dataLoader.loadSet(set).filter(p => p.isInstanceOf[EnergyCard] && p.asInstanceOf[EnergyCard].energyType == energyType)
        .map(p => p.asInstanceOf[EnergyCard]).headOption
  }

  it should "build game field correctly and notify observers when it happens" in new BaseContext {
    (attachObserver().update _).expects(where {e: Event => {
      e.isInstanceOf[BuildGameField]
      e.asInstanceOf[BuildGameField].playerBoard.isInstanceOf[Board]
      e.asInstanceOf[BuildGameField].opponentBoard.isInstanceOf[Board]
    }})
    initBoards(SetType.Base, DeckType.Base1, DeckType.Base2)
    val playerBoard: Board = gameManager.playerBoard
    val opponentBoard: Board = gameManager.opponentBoard
    assert(playerBoard.deck.nonEmpty && opponentBoard.deck.nonEmpty)
    assert(playerBoard.activePokemon.isEmpty && opponentBoard.activePokemon.isEmpty)
    assert(playerBoard.hand.size == GameManager.InitialHandCardNum && opponentBoard.hand.size == GameManager.InitialHandCardNum)
    assert(playerBoard.prizeCards.size == GameManager.InitialPrizeCardNum && opponentBoard.prizeCards.size == GameManager.InitialPrizeCardNum)
    assert(playerBoard.discardStack.isEmpty && opponentBoard.discardStack.isEmpty)
    playerBoard.pokemonBench.foreach(c => assert(c.isEmpty))
    opponentBoard.pokemonBench.foreach(c => assert(c.isEmpty))
  }

  it should "build game field in a way that initially each player must have at least one base PokemonCard in their hands" in
    new BaseContext with DataLoaderContext {
      var cardsSet: Seq[Card] = SetType.values.toList.flatMap(s => dataLoader.loadSet(s))
      for (deck <- DeckType.values) {
          val playerDeckCards: Seq[DeckCard] = dataLoader.loadSingleDeck(deck)
          val opponentDeckCards: Seq[DeckCard] = dataLoader.loadSingleDeck(deck)
          gameManager.initBoards(playerDeckCards, opponentDeckCards, cardsSet)
          assert(gameManager.playerBoard.hand.exists(c => c.isInstanceOf[PokemonCard] && c.asInstanceOf[PokemonCard].isBase)
            && gameManager.opponentBoard.hand.exists(c => c.isInstanceOf[PokemonCard] && c.asInstanceOf[PokemonCard].isBase))
        }
    }

  it should "notify observers when a card is draw from player deck" in new BaseContext {
    initBoards(SetType.Base, DeckType.Base1, DeckType.Base2)
    (attachObserver().update _).expects(where {e: Event => {
      e.isInstanceOf[UpdateBoards]
    }})
    gameManager.drawCard(gameManager.playerBoard)
  }

  it should "notify observers when player active pokemon or player bench is updated" in new BaseContext with DataLoaderContext {
    initBoards(SetType.Base, DeckType.Base1, DeckType.Base2)
    val observer: Observer = attachObserver()

    (observer.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdateBoards]
    }})

    Given("a new active pokemon")
    val activePokemon: Option[PokemonCard] = getPokemon(SetType.Base, "Bulbasaur")

    When("it is set as active pokemon")
    gameManager.setActivePokemon(activePokemon)

    Then("the active pokemon location should be not empty")
    assert(gameManager.playerBoard.activePokemon.nonEmpty && gameManager.playerBoard.activePokemon == activePokemon)

    (observer.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdateBoards]
    }})

    Given("a new bench pokemon and a bench position")
    val benchPokemon: Option[PokemonCard] = getPokemon(SetType.Base, "Staryu")
    val benchPosition = 0

    When("the bench pokemon is placed in the bench at the given position")
    gameManager.putPokemonToBench(benchPokemon, benchPosition)

    Then("the bench position should be not empty")
    assert(gameManager.playerBoard.pokemonBench(benchPosition).nonEmpty && gameManager.playerBoard.pokemonBench(benchPosition) == benchPokemon)

    (observer.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdateBoards]
    }})

    Given("a replacement bench position")
    val replacementBenchPosition: Int = benchPosition

    When("the active pokemon is destroyed")
    gameManager.destroyActivePokemon(replacementBenchPosition)

    Then("the active pokemon is replaced with the bench pokemon at the given replacement bench position")
    assert(gameManager.playerBoard.activePokemon.nonEmpty && gameManager.playerBoard.activePokemon == benchPokemon &&
      gameManager.playerBoard.pokemonBench(benchPosition).isEmpty)
  }

  it should "retreat an active pokemon if it has enough energies" in new BaseContext with DataLoaderContext {
    initBoards(SetType.Base, DeckType.Base1, DeckType.Base2)

    Given("an active pokemon with a retreat cost and a pokemon in a bench position")
    val activePokemon: Option[PokemonCard] = getPokemon(SetType.Base, "Bulbasaur")
    gameManager.setActivePokemon(activePokemon)
    val benchPokemon: Option[PokemonCard] = getPokemon(SetType.Base, "Staryu")
    val benchPosition = 0
    gameManager.putPokemonToBench(benchPokemon, benchPosition)

    When("the active pokemon is retreated and does not have enough energies")
    gameManager.retreatActivePokemon(benchPosition, gameManager.playerBoard)

    Then("the active pokemon stays where it is because the retreat cannot happen")
    assert(gameManager.activePokemon(gameManager.playerBoard).nonEmpty && gameManager.activePokemon(gameManager.playerBoard) == activePokemon)

    When("the active pokemon is retreated and has enough energies")
    val energy: Option[EnergyCard] = getEnergy(SetType.Base, EnergyType.Grass)
    gameManager.addEnergyToPokemon(activePokemon.get, energy.get, gameManager.playerBoard)
    gameManager.retreatActivePokemon(benchPosition, gameManager.playerBoard)

    Then("the active pokemon swaps with the benched pokemon at the specified bench position")
    assert(gameManager.activePokemon(gameManager.playerBoard).nonEmpty && gameManager.activePokemon(gameManager.playerBoard) == benchPokemon)
    assert(gameManager.playerBoard.pokemonBench(benchPosition).nonEmpty && gameManager.playerBoard.pokemonBench(benchPosition) == activePokemon)

    Given("an asleep active pokemon")
    gameManager.activePokemon(gameManager.playerBoard).get.status = StatusType.Asleep

    When("the active pokemon tries to retreat")

    Then("an InvalidOperationException should be trown")
    intercept[InvalidOperationException] {
      gameManager.retreatActivePokemon(benchPosition, gameManager.playerBoard)
    }
  }

  /*
  it should "evolve a pokemon correctly" in new BaseContext with DataLoaderContext {
    initBoards(SetType.Base, DeckType.Base1, DeckType.Base2)

    Given("an active pokemon in a specific game condition and its evolution")
    val damage = 20
    val statusType: StatusType.Value = StatusType.Confused
    val activePokemon: Option[PokemonCard] = getPokemon(SetType.Base, "Bulbasaur")
    gameManager.setActivePokemon(activePokemon)
    gameManager.playerBoard.activePokemon.get.status = statusType
    gameManager.playerBoard.activePokemon.get.addDamage(damage, Seq(EnergyType.Colorless))
    val energyCard: Option[EnergyCard] = getEnergy(SetType.Base, EnergyType.Grass)
    gameManager.addEnergyToPokemon(activePokemon.get, energyCard.get)
    val evolution: Option[PokemonCard] = getPokemon(SetType.Base, "Ivysaur")

    When("the active pokemon is evolved")
    gameManager.evolvePokemon(activePokemon.get, evolution.get)

    Then("the active pokemon should be replaced by his evolution that inherit its game condition")
    assert(gameManager.activePokemon(gameManager.playerBoard) == evolution)
    assert(evolution.get.actualHp == evolution.get.initialHp - damage)
    assert(evolution.get.status == statusType)
    assert(evolution.get.hasEnergies(energyCard.get.energyType :: Nil))
  }
   FALLO FARE AL CONTROLLER
  */

    // TESTARE confirmAttack

  it should "throw CardNotFoundException if a DeckCard does not exists in Cards set" in new BaseContext with DataLoaderContext {
    val nonExistentCard: DeckCard = DeckCard("nonExistentID", 1000, Some(SetType.Base), "sausages", "very rare", 3)
    val cardsSet: Seq[Card] = dataLoader.loadSet(SetType.Base)
    var playerDeckCards: Seq[DeckCard] = dataLoader.loadSingleDeck(DeckType.Base1)
    val opponentDeckCards: Seq[DeckCard] = dataLoader.loadSingleDeck(DeckType.Base2)
    playerDeckCards = playerDeckCards :+ nonExistentCard

    intercept[CardNotFoundException] {
      gameManager.initBoards(playerDeckCards, opponentDeckCards, cardsSet)
    }
  }

}
