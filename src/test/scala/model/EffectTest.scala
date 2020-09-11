package model

import model.Cards.EnergyCard.EnergyCardImpl
import model.Cards.{Card, EnergyCard, PokemonCard}
import model.EnergyType.EnergyType
import model.exception.MissingEnergyException
import org.scalatest.{FlatSpec, GivenWhenThen}

import scala.util.Random

class EffectTest extends FlatSpec with GivenWhenThen {

  val cardList: Seq[Card] = DataLoader.loadData(SetType.base)
    .filter(c => c.isInstanceOf[Card])
  val pokemonCards = cardList.filter(p => p.isInstanceOf[PokemonCard])
  val energyCards = cardList.filter(p => p.isInstanceOf[EnergyCard])
  var playerHand: Seq[Card] = Seq()
  var iaHand: Seq[Card] = Seq()
  var activePokemon: Card = _
  var pokemonBench: Seq[PokemonCard] = Seq()

  behavior of "First Turn Ia"
  it should "have 7 card" in {
    for (i <- 1 to 7)
      iaHand = iaHand :+ cardList(i)
    assert(iaHand.size == 7)
  }
  it must "have a base Pokemon" in {
    iaHand = iaHand :+ addSpecificPokemonCard("Pikachu")
    assert(iaHand.exists(p => p.asInstanceOf[PokemonCard].evolvesFrom == ""))
  }
  it must "have at least 1 energy " in {
    val energyCard: Card = cardList.filter(
      p => p.isInstanceOf[EnergyCard])
      .filter(energyCard => energyCard.asInstanceOf[EnergyCard].energyType == EnergyType.water).head
    for (i <- 0 to 3)
      iaHand = iaHand :+ energyCard
    assert(iaHand.exists(p => p.isInstanceOf[EnergyCard]))

  }
  it must "choose the pokemon card according to the energy in hand " in {
    iaHand = iaHand :+ addSpecificPokemonCard("Charmander")
    iaHand = iaHand :+ addSpecificPokemonCard("Blastoise")
    activePokemon = iaHand.filter(p => p.isInstanceOf[PokemonCard])
      .find(pkm => pkm.asInstanceOf[PokemonCard].pokemonTypes.head == EnergyType.water && pkm.asInstanceOf[PokemonCard].evolvesFrom == "Wartortle").get
    assert(activePokemon.asInstanceOf[Cards.PokemonCard].name == "Blastoise")

  }

  it must "attack" in {
    val waterEnergy: EnergyCard = energyCards.filter(energy => energy.asInstanceOf[EnergyCard].energyType == EnergyType.water).head.asInstanceOf[EnergyCard]
    activePokemon.asInstanceOf[PokemonCard].addEnergy(waterEnergy)
    activePokemon.asInstanceOf[PokemonCard].addEnergy(waterEnergy)
    activePokemon.asInstanceOf[PokemonCard].addEnergy(waterEnergy)

    println("BLASTOISE  Initial Hp " + activePokemon.asInstanceOf[PokemonCard].actualHp)
    activePokemon.asInstanceOf[PokemonCard].attacks.head.effect.get.useEffect(Seq(activePokemon.asInstanceOf[PokemonCard]), activePokemon.asInstanceOf[PokemonCard])
    println("Energy Stored " + activePokemon.asInstanceOf[PokemonCard].totalEnergiesStored)
    println("Final Hp " + activePokemon.asInstanceOf[PokemonCard].actualHp)

    iaHand = iaHand :+ addSpecificPokemonCard("Mewtwo")
    activePokemon = iaHand.filter(p => p.isInstanceOf[PokemonCard])
      .find(pkm => pkm.asInstanceOf[PokemonCard].name == "Mewtwo").get
    println("MEWTWO  Initial Hp " + activePokemon.asInstanceOf[PokemonCard].actualHp)
    activePokemon.asInstanceOf[PokemonCard].addEnergy(waterEnergy)
    activePokemon.asInstanceOf[PokemonCard].addEnergy(waterEnergy)
    activePokemon.asInstanceOf[PokemonCard].addEnergy(waterEnergy)
    activePokemon.asInstanceOf[PokemonCard].attacks.head.effect.get.useEffect(Seq(activePokemon.asInstanceOf[PokemonCard]), activePokemon.asInstanceOf[PokemonCard])
    println("Energy Stored " + activePokemon.asInstanceOf[PokemonCard].totalEnergiesStored)
    println("Final Hp " + activePokemon.asInstanceOf[PokemonCard].actualHp)

    iaHand = iaHand :+ addSpecificPokemonCard("Magneton")
    activePokemon = iaHand.filter(p => p.isInstanceOf[PokemonCard])
      .find(pkm => pkm.asInstanceOf[PokemonCard].name == "Magneton").get
    activePokemon.asInstanceOf[PokemonCard].actualHp = 200
    println("added 2 pokemon to enemy Bench")
    pokemonBench = pokemonBench :+ iaHand.head.asInstanceOf[PokemonCard]
    pokemonBench = pokemonBench :+ iaHand(1).asInstanceOf[PokemonCard]
    println("Hp in pos0 " + pokemonBench.head.actualHp)
    println("Hp in pos1 " + pokemonBench(1).actualHp)
    println("Magneton  Initial Hp " + activePokemon.asInstanceOf[PokemonCard].actualHp)
    activePokemon.asInstanceOf[PokemonCard].attacks(1).effect.get.useEffect(pokemonBench, activePokemon.asInstanceOf[PokemonCard])
    println(" Magneton Final Hp " + activePokemon.asInstanceOf[PokemonCard].actualHp)
    println("Hp in pos0 " + pokemonBench.head.actualHp)
    println("Hp in pos1 " + pokemonBench(1).actualHp)

    iaHand = iaHand :+ addSpecificPokemonCard("Nidoking")
    activePokemon = iaHand.filter(p => p.isInstanceOf[PokemonCard])
      .find(pkm => pkm.asInstanceOf[PokemonCard].name == "Nidoking").get
    println("Nidoking  Initial Hp " + activePokemon.asInstanceOf[PokemonCard].actualHp)

    println("Hp in pos0 " + pokemonBench.head.actualHp)
    println("Hp in pos1 " + pokemonBench(1).actualHp)
    activePokemon.asInstanceOf[PokemonCard].attacks.head.effect.get.useEffect(Seq(pokemonBench.head), activePokemon.asInstanceOf[PokemonCard])
    println(" Nidoking Final Hp " + activePokemon.asInstanceOf[PokemonCard].actualHp)
    println("Hp in pos0 " + pokemonBench.head.actualHp)
    println("Hp in pos1 " + pokemonBench(1).actualHp)


    iaHand = iaHand :+ addSpecificPokemonCard("Zapdos")
    activePokemon = iaHand.filter(p => p.isInstanceOf[PokemonCard])
      .find(pkm => pkm.asInstanceOf[PokemonCard].name == "Zapdos").get
    println("Zapdos  Initial Hp " + activePokemon.asInstanceOf[PokemonCard].actualHp)
    pokemonBench.head.actualHp = 100
    pokemonBench(1).actualHp = 100
    println("Hp in pos0 " + pokemonBench.head.actualHp)
    println("Hp in pos1 " + pokemonBench(1).actualHp)
    activePokemon.asInstanceOf[PokemonCard].attacks.head.effect.get.useEffect(Seq(pokemonBench.head), activePokemon.asInstanceOf[PokemonCard])
    println(" Zapdos Final Hp " + activePokemon.asInstanceOf[PokemonCard].actualHp)
    println("Hp in pos0 " + pokemonBench.head.actualHp)
    println("Hp in pos1 " + pokemonBench(1).actualHp)


    iaHand = iaHand :+ addSpecificPokemonCard("Beedrill")
    activePokemon = iaHand.filter(p => p.isInstanceOf[PokemonCard])
      .find(pkm => pkm.asInstanceOf[PokemonCard].name == "Beedrill").get
    pokemonBench.head.actualHp = 100
    pokemonBench(1).actualHp = 100
    println("BEEDRILL")
    println("Hp in pos0 " + pokemonBench.head.actualHp)
    activePokemon.asInstanceOf[PokemonCard].attacks.head.effect.get.useEffect(Seq(pokemonBench.head), activePokemon.asInstanceOf[PokemonCard])
    println("Hp in pos0 " + pokemonBench.head.actualHp)

  }

  def addSpecificPokemonCard(name: String): Card = {
    val pokemonCard = cardList.filter(
      p => p.isInstanceOf[PokemonCard])
      .filter(pkmCard => pkmCard.asInstanceOf[PokemonCard].name == name).head
    pokemonCard
  }



}
