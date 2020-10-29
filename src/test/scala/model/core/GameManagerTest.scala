package model.core

import common.Observer
import model.card.{Card, EnergyCard, PokemonCard}
import common.Events.{EndTurnEvent, BuildGameFieldEvent, EndGameEvent, Event, PokemonKOEvent, UpdateBoardsEvent}
import model.exception.{CardNotFoundException, InvalidOperationException}
import model.game.DeckType.DeckType
import model.game.EnergyType.EnergyType
import model.game.SetType.SetType
import model.game.{Board, DeckCard, DeckType, EnergyType, SetType, StatusType}
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
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
      e.isInstanceOf[BuildGameFieldEvent]
      e.asInstanceOf[BuildGameFieldEvent].playerBoard.isInstanceOf[Board]
      e.asInstanceOf[BuildGameFieldEvent].opponentBoard.isInstanceOf[Board]
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
      e.isInstanceOf[UpdateBoardsEvent]
    }})
    gameManager.drawCard(gameManager.playerBoard)
  }

  it should "notify observers when player active pokemon or player bench is updated" in new BaseContext with DataLoaderContext {
    initBoards(SetType.Base, DeckType.Base1, DeckType.Base2)
    val playerBoard: Board = gameManager.playerBoard
    val observer: Observer = attachObserver()

    (observer.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdateBoardsEvent]
    }})

    Given("a new active pokemon")
    val activePokemon: Option[PokemonCard] = getPokemon(SetType.Base, "Bulbasaur")

    When("it is set as active pokemon")
    gameManager.setActivePokemon(activePokemon)

    Then("the active pokemon location should be not empty")
    assert(playerBoard.activePokemon.nonEmpty && playerBoard.activePokemon == activePokemon)

    (observer.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdateBoardsEvent]
    }})

    Given("a new bench pokemon and a bench position")
    val benchPokemon: Option[PokemonCard] = getPokemon(SetType.Base, "Staryu")
    val benchPosition = 0

    When("the bench pokemon is placed in the bench at the given position")
    gameManager.putPokemonToBench(benchPokemon, benchPosition)

    Then("the bench position should be not empty")
    assert(playerBoard.pokemonBench(benchPosition).nonEmpty && playerBoard.pokemonBench(benchPosition) == benchPokemon)

    (observer.update _).expects(where {e: Event => {
      e.isInstanceOf[UpdateBoardsEvent]
    }})

    Given("a replacement bench position")
    val replacementBenchPosition: Int = benchPosition

    When("the active pokemon is destroyed")
    gameManager.destroyActivePokemon(replacementBenchPosition)

    Then("the active pokemon is replaced with the bench pokemon at the given replacement bench position")
    assert(playerBoard.activePokemon.nonEmpty && playerBoard.activePokemon == benchPokemon &&
      playerBoard.pokemonBench(benchPosition).isEmpty)
  }

  it should "retreat an active pokemon if it has enough energies" in new BaseContext with DataLoaderContext {
    initBoards(SetType.Base, DeckType.Base1, DeckType.Base2)
    val playerBoard: Board = gameManager.playerBoard

    Given("an active pokemon with a retreat cost and a pokemon in a bench position")
    val activePokemon: Option[PokemonCard] = getPokemon(SetType.Base, "Bulbasaur")
    gameManager.setActivePokemon(activePokemon)
    val benchPokemon: Option[PokemonCard] = getPokemon(SetType.Base, "Staryu")
    val benchPosition = 0
    gameManager.putPokemonToBench(benchPokemon, benchPosition)

    When("the active pokemon is retreated and does not have enough energies")
    gameManager.retreatActivePokemon(benchPosition, playerBoard)

    Then("the active pokemon stays where it is because the retreat cannot happen")
    assert(gameManager.activePokemon(playerBoard).nonEmpty && gameManager.activePokemon(playerBoard) == activePokemon)

    When("the active pokemon is retreated and has enough energies")
    val energy: Option[EnergyCard] = getEnergy(SetType.Base, EnergyType.Grass)
    gameManager.addEnergyToPokemon(activePokemon.get, energy.get, playerBoard)
    gameManager.retreatActivePokemon(benchPosition, playerBoard)

    Then("the active pokemon swaps with the benched pokemon at the specified bench position")
    assert(gameManager.activePokemon(playerBoard).nonEmpty && gameManager.activePokemon(playerBoard) == benchPokemon)
    assert(playerBoard.pokemonBench(benchPosition).nonEmpty && playerBoard.pokemonBench(benchPosition) == activePokemon)

    Given("an asleep active pokemon")
    gameManager.activePokemon(playerBoard).get.status = StatusType.Asleep

    When("the active pokemon tries to retreat")

    Then("an InvalidOperationException should be trown")
    intercept[InvalidOperationException] {
      gameManager.retreatActivePokemon(benchPosition, playerBoard)
    }
  }

  it should "manage correctly a declaration of attack" in new BaseContext with DataLoaderContext {
    initBoards(SetType.Base, DeckType.Base1, DeckType.Base2)
    val playerBoard: Board = gameManager.playerBoard
    val opponentBoard: Board = gameManager.opponentBoard

    Given("an attacking pokemon and a defending pokemon")
    val attackingPokemon: Option[PokemonCard] = getPokemon(SetType.Base, "Bulbasaur")
    val energyCard: Option[EnergyCard] = getEnergy(SetType.Base, EnergyType.Grass)
    attackingPokemon.get.addEnergy(energyCard.get)
    attackingPokemon.get.addEnergy(energyCard.get)
    attackingPokemon.get.addEnergy(energyCard.get)
    var defendingPokemon: Option[PokemonCard] = getPokemon(SetType.Base, "Staryu")
    gameManager.setActivePokemon(attackingPokemon, playerBoard)
    gameManager.setActivePokemon(defendingPokemon, opponentBoard)

    When("the attacking pokemon attacks and its status is Paralyzed or Asleep")
    attackingPokemon.get.status =  StatusType.Asleep

    Then("an InvalidOperationException should be thrown")
    intercept[InvalidOperationException] {
      gameManager.confirmAttack(playerBoard, opponentBoard, attackingPokemon.get.attacks.head)
    }

    When("the attacking pokemon attacks and destroys the defending pokemon and the bench is not empty")
    var initialPrizeCardsNumber: Int = playerBoard.prizeCards.size
    val defendingBenchedPokemon: Option[PokemonCard] = getPokemon(SetType.Base, "Caterpie")
    defendingPokemon.get.actualHp = attackingPokemon.get.attacks.head.damage.get
    attackingPokemon.get.status =  StatusType.NoStatus
    opponentBoard.putPokemonInBenchPosition(defendingBenchedPokemon, 0)
    val observer: Observer = attachObserver()
    inSequence {
      (observer.update _).expects(where {e: Event => {
        e.isInstanceOf[EndTurnEvent]
      }})
      (observer.update _).expects(where {e: Event => {
        e.isInstanceOf[PokemonKOEvent]
      }})
      (observer.update _).expects(where {e: Event => {
        e.isInstanceOf[UpdateBoardsEvent]
      }})
    }
    gameManager.confirmAttack(playerBoard, opponentBoard, attackingPokemon.get.attacks.head)

    Then("the attacking player draws a prize card and observers should be notified correctly")
    assert(playerBoard.prizeCards.size == initialPrizeCardsNumber - 1)
    initialPrizeCardsNumber = playerBoard.prizeCards.size

    When("the attacking pokemon attacks and destroys the defending pokemon and the bench is empty")
    defendingPokemon = defendingBenchedPokemon
    opponentBoard.activePokemon = defendingPokemon
    opponentBoard.putPokemonInBenchPosition(None, 0)
    opponentBoard.pokemonBench.foreach(p => assert(p.isEmpty))
    defendingPokemon.get.actualHp = attackingPokemon.get.attacks.head.damage.get

    Then("observers should be notified correctly")
    inSequence {
      (observer.update _).expects(where {e: Event => {
        e.isInstanceOf[EndTurnEvent]
      }})
      (observer.update _).expects(where {e: Event => {
        e.isInstanceOf[EndGameEvent]
      }})
      (observer.update _).expects(where {e: Event => {
        e.isInstanceOf[UpdateBoardsEvent]
      }})
    }
    gameManager.confirmAttack(playerBoard, opponentBoard, attackingPokemon.get.attacks.head)
  }

  it should "throw CardNotFoundException if a DeckCard does not exists in Cards set" in new BaseContext with DataLoaderContext {
    val nonExistentCard: DeckCard = DeckCard("nonExistentID", Some(SetType.Base), "sausages", "very rare", 3)
    val cardsSet: Seq[Card] = dataLoader.loadSet(SetType.Base)
    var playerDeckCards: Seq[DeckCard] = dataLoader.loadSingleDeck(DeckType.Base1)
    val opponentDeckCards: Seq[DeckCard] = dataLoader.loadSingleDeck(DeckType.Base2)
    playerDeckCards = playerDeckCards :+ nonExistentCard

    intercept[CardNotFoundException] {
      gameManager.initBoards(playerDeckCards, opponentDeckCards, cardsSet)
    }
  }

}
