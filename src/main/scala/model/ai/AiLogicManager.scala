package model.ai

import common.Events.UpdateBoardsEvent
import model.card.{Card, EnergyCard, PokemonCard}
import model.core.{GameManager, TurnManager}
import model.exception.InvalidOperationException
import model.game.{Board, EnergyType}

object AiLogicManager {

  /**
   * executes the logic of the initial turn of artificial intelligence
   *
   * @param hand
   * @param opponentBoard
   * @param playerBoard
   * @param gameManager
   * @param turnManager
   */
  def placeCards(hand: Seq[Card], opponentBoard: Board, playerBoard: Board, gameManager: GameManager, turnManager: TurnManager): Unit = {
    val basePokemons = hand.filter(pkm => pkm.isInstanceOf[PokemonCard] && pkm.asInstanceOf[PokemonCard].evolutionName == "").asInstanceOf[Seq[PokemonCard]]
    val pokemonWithMaxWeight = WeightCalculator.calculateOrderedSeq(basePokemons, opponentBoard, playerBoard, WeightCalculatorType.PlaceCard).head
    //drop active pokemon from hand
    gameManager.setActivePokemon(Some(pokemonWithMaxWeight.pokemonCard), opponentBoard)
    //populate bench with basePokemon
    populateBench(opponentBoard, playerBoard, gameManager)
    Thread.sleep(1000)
    turnManager.playerReady()
  }

  /**
   * performs the turn logic of artificial intelligence
   *
   * @param opponentBoard
   * @param playerBoard
   * @param gameManager
   * @param turnManager
   */
  def doTurn(opponentBoard: Board, playerBoard: Board, gameManager: GameManager, turnManager: TurnManager): Unit = {
       if (opponentBoard.activePokemon.get.isKO)
      calculateIfWithdrawAndDo(opponentBoard, playerBoard, gameManager)

    gameManager.activePokemonStartTurnChecks(opponentBoard, playerBoard)
    gameManager.drawCard(opponentBoard)
    populateBench(opponentBoard, playerBoard, gameManager)
    gameManager.notifyObservers(UpdateBoardsEvent())
    Thread.sleep(1500)
    //evolve all pkm
    evolveAll(opponentBoard, playerBoard, gameManager)
    gameManager.notifyObservers(UpdateBoardsEvent())
    Thread.sleep(1500)
    //calculate if the retreat of the active pokemon is convenient and Do it
    if (gameManager.pokemonBench(opponentBoard).head.isDefined && opponentBoard.activePokemon.get.retreatCost.size <= opponentBoard.activePokemon.get.totalEnergiesStored) {
      try {
        calculateIfWithdrawAndDo(opponentBoard, playerBoard, gameManager)
      } catch {
        case _: InvalidOperationException => println("can't retreat")
      }
    }
    //assignEnergy
    val getEnergy = opponentBoard.hand.filter(energy => energy.isInstanceOf[EnergyCard])
    if (getEnergy.nonEmpty)
      calculateAssignEnergy(opponentBoard, gameManager)

    gameManager.notifyObservers(UpdateBoardsEvent())
    Thread.sleep(1000)
    try {
      if (opponentBoard.activePokemon.get.hasEnergies(opponentBoard.activePokemon.get.attacks.last.cost)) {
        gameManager.confirmAttack(opponentBoard, playerBoard, opponentBoard.activePokemon.get.attacks.last)
      } else if (opponentBoard.activePokemon.get.hasEnergies(opponentBoard.activePokemon.get.attacks.head.cost)) {
        gameManager.confirmAttack(opponentBoard, playerBoard, opponentBoard.activePokemon.get.attacks.head)
      }
    } catch {
      case ex : InvalidOperationException => println(ex.getMessage)
    }
    gameManager.activePokemonEndTurnChecks(opponentBoard.activePokemon.get)
    turnManager.switchTurn()
  }


  /**
   * choose a certain number of bench pokemon and do the damage to it
   *
   * @param pokemonToDmg number of pokemon to dmg
   * @param dmgToDo      value of the damage to be inflicted on each pokemon
   * @param opponentBoard
   */
  def dmgToBench(pokemonToDmg: Int, dmgToDo: Int, opponentBoard: Board): Unit = {
    var numberOfPokemonToDmg = pokemonToDmg
    if (pokemonToDmg > opponentBoard.pokemonBench.count(c => c.isDefined)) {
      numberOfPokemonToDmg = opponentBoard.pokemonBench.count(c => c.isDefined)
    }
    for (i <- 0 until numberOfPokemonToDmg) {
      opponentBoard.pokemonBench.filter(c => c.isDefined)(i).get.addDamage(dmgToDo, Seq(EnergyType.Colorless))
    }
  }

  /**
   * place  pokemon on the bench
   */
  private def populateBench(opponentBoard: Board, playerBoard: Board, gameManager: GameManager): Unit = {
    val basePokemons: Seq[PokemonCard] = opponentBoard.hand.filter(pkm => pkm.isInstanceOf[PokemonCard] && pkm.asInstanceOf[PokemonCard].evolutionName == "").asInstanceOf[Seq[PokemonCard]]
    basePokemons.zipWithIndex.foreach {
      case (pkm, i) => if (opponentBoard.pokemonBench.count(card => card.nonEmpty) < 5) {
        gameManager.putPokemonToBench(Some(pkm), opponentBoard.pokemonBench.count(card => card.nonEmpty), opponentBoard)
      }
    }
  }

  /**
   * Evolve the pokemon in the field if their evolution is present in hand
   */
  private def evolveAll(opponentBoard: Board, playerBoard: Board, gameManager: GameManager): Unit = {
    val evolution: Seq[PokemonCard] = opponentBoard.hand.filter(pkm => pkm.isInstanceOf[PokemonCard] && pkm.asInstanceOf[PokemonCard].evolutionName != "").asInstanceOf[Seq[PokemonCard]]
    if (evolution.size > 0)
      evolve(evolution)

    @scala.annotation.tailrec
    def evolve(pokemons: Seq[PokemonCard]): Unit = pokemons match {
      case evolution :: t if opponentBoard.activePokemon.get.name == evolution.evolutionName => {
        opponentBoard.activePokemon = gameManager.evolvePokemon(opponentBoard.activePokemon.get, evolution, opponentBoard)
        evolve(t)
      }
      case evolution :: t if gameManager.pokemonBench(opponentBoard).head.isDefined => evolveBench(opponentBoard.pokemonBench.filter(pkm => pkm.isDefined), evolution); evolve(t)
      case List() =>
      case _ =>
    }

    @scala.annotation.tailrec
    def evolveBench(bench: Seq[Option[PokemonCard]], evolution: PokemonCard) {
      bench match {
        case List() =>
        case benchPkm :: _ if benchPkm.get.name == evolution.evolutionName => {
          val getbenchedIndex: Int = opponentBoard.pokemonBench.filter(card => card.isDefined).indexWhere(pkm => pkm.get.name == benchPkm.get.name)
          gameManager.putPokemonToBench(gameManager.evolvePokemon(benchPkm.get, evolution, opponentBoard), getbenchedIndex, opponentBoard)
        } //pokemon found in bench
        case _ :: t => evolveBench(t, evolution)
      }
    }
  }

  /**
   * I calculate and eventually withdraw the active pokemon with a pokemon from my bench
   */
   def calculateIfWithdrawAndDo(opponentBoard: Board, playerBoard: Board, gameManager: GameManager): Unit = {
    val aiBench = opponentBoard.pokemonBench.filter(c => c.isDefined).flatten
    val activePokemonweight: Int = WeightCalculator.calculatePokemonWeight(opponentBoard.activePokemon.get, opponentBoard, playerBoard, WeightCalculatorType.WithDraw)
    val benchPokemonsMaxweight = WeightCalculator.calculateOrderedSeq(aiBench, opponentBoard, playerBoard, WeightCalculatorType.WithDraw).head
    val benchIndex = opponentBoard.pokemonBench.indexWhere(pkm => pkm.contains(benchPokemonsMaxweight.pokemonCard))
    if (activePokemonweight < benchPokemonsMaxweight.weight) {
      if (!opponentBoard.activePokemon.get.isKO) {
        println("NO KO - metto il pokemon con indice "+benchIndex + "  -  "+ opponentBoard.pokemonBench(benchIndex).get.name +" HP: "+ opponentBoard.pokemonBench(benchIndex).get.actualHp)
        println("NO KO -al posto di : "+ opponentBoard.activePokemon.get)
        gameManager.retreatActivePokemon(benchIndex, opponentBoard)
      } else {
        println("KO - metto il pokemon con indice "+benchIndex + "  -  "+ opponentBoard.pokemonBench(benchIndex).get.name +" HP: "+ opponentBoard.pokemonBench(benchIndex).get.actualHp)
        println("KO - al posto di : "+ opponentBoard.activePokemon.get)
        gameManager.destroyActivePokemon(benchIndex, opponentBoard)
      }
    }
  }

  /**
   * Calculate and eventually assign an energy to a pokemon
   */
  private def calculateAssignEnergy(opponentBoard: Board, gameManager: GameManager): Unit = {
    var returnedIndex = -1
    returnedIndex = assignEnergy(opponentBoard.activePokemon.get, opponentBoard, gameManager)
    if (returnedIndex == -1) {
      if (opponentBoard.pokemonBench.count(c => c.isDefined) > 0)
        returnedIndex = iterateSeqToAssign(opponentBoard.pokemonBench.filter(c => c.isDefined))
    }

    def iterateSeqToAssign(seqToIterate: Seq[Option[PokemonCard]]): Int = {
      var index = -1
      seqToIterate.foreach(pkm => {
        if (index < 0)
          index = assignEnergy(pkm.get, opponentBoard, gameManager)
      })
      index
    }
  }

  /**
   * Gives a pokemon an energy if present in the hand
   *
   * @param pokemon pokemon to assign energy to
   * @return energy index in the hand
   */
  private def assignEnergy(pokemon: PokemonCard, opponentBoard: Board, gameManager: GameManager): Int = {
    var energyIndex = -1
    val pokemonType = pokemon.pokemonTypes.head
    var totalEnergyCount = 0
    if (!pokemon.hasEnergies(pokemon.attacks.last.cost)) {
      energyIndex = opponentBoard.hand.indexWhere(card => card.isInstanceOf[EnergyCard] && card.asInstanceOf[EnergyCard].energyType == pokemonType)
      if (energyIndex == -1) {
        val numberOfColorlessNeededForAtk: Int = pokemon.attacks.last.cost.count(card => card.isInstanceOf[EnergyCard] && card.asInstanceOf[EnergyCard].energyType == EnergyType.Colorless)
        pokemon.energiesMap.filter(p => p._1 != pokemonType).foreach(map => totalEnergyCount += map._2)
        if (numberOfColorlessNeededForAtk < totalEnergyCount)
          energyIndex = opponentBoard.hand.indexWhere(card => card.isInstanceOf[EnergyCard])
      }
      if (energyIndex >= 0)
        gameManager.addEnergyToPokemon(pokemon, opponentBoard.hand(energyIndex).asInstanceOf[EnergyCard], opponentBoard)
    }
    energyIndex
  }
}
