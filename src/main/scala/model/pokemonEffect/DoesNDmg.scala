package model.pokemonEffect

import model.Cards.PokemonCard
import model.EnergyType


case class DoesNDmg(baseDmgCount: Int) extends AttackEffect {
  override def useEffect(defendingPokemons: Seq[PokemonCard], attackingPokemon: PokemonCard): Unit = defendingPokemons.foreach(pkm =>
    pkm.addDamage(totalDmg,attackingPokemon.pokemonTypes)
  )
  override var args: Map[String, Any] = Map.empty[String, Any]
  override var totalDmg: Int = totalDmg + baseDmgCount
}

//es. Blastoise (atk)
//es. MewTwo    (def)
trait ForEachEnergyAttachedTo extends AttackEffect{
  abstract override def useEffect(defendingPokemons: Seq[PokemonCard], attackingPokemon: PokemonCard): Unit = {
    var pokemonToApply : PokemonCard = null
     getStringArgFromMap("atk_or_def") match {
       case "atk" => pokemonToApply = attackingPokemon
       case "def" => pokemonToApply = defendingPokemons.head
     }
    //defendingPokemons.foreach(pkm => pkm.addDamage(getIntArgFromMap("dmgCount") * pokemonToApply.totalEnergiesStored, Seq(EnergyType.colorless)))
    totalDmg += getIntArgFromMap("dmgCount") * pokemonToApply.totalEnergiesStored
    super.useEffect(defendingPokemons: Seq[PokemonCard], attackingPokemon: PokemonCard)
  }
}

trait ForEachDamageCount extends AttackEffect {
  abstract override def useEffect(defendingPokemons: Seq[PokemonCard], attackingPokemon: PokemonCard): Unit = {
    //defendingPokemons.foreach(pkm => pkm.addDamage(-(attackingPokemon.actualHp - attackingPokemon.initialHp), attackingPokemon.pokemonTypes))
    totalDmg +=  (-(attackingPokemon.actualHp - attackingPokemon.initialHp))
    super.useEffect(defendingPokemons: Seq[PokemonCard], attackingPokemon: PokemonCard)
  }
}

//es. Magneton
trait DmgMySelf extends AttackEffect {
  abstract override def useEffect(defendingPokemons: Seq[PokemonCard], attackingPokemon: PokemonCard): Unit = {
   attackingPokemon.actualHp = attackingPokemon.actualHp- getIntArgFromMap("DmgMyself")
    //totalDmg += attackingPokemon.actualHp- getIntArgFromMap("DmgMyself")
    super.useEffect(defendingPokemons: Seq[PokemonCard], attackingPokemon: PokemonCard)
  }
}





class DoesNDmgForEachEnergyAttachedTo(dmgCount: Int) extends DoesNDmg(dmgCount) with ForEachEnergyAttachedTo
class DoesNDmgForEachDamageCount(dmgCount: Int) extends DoesNDmg(dmgCount) with ForEachDamageCount
class DoesNDmgAndDmgMyself(dmgCount: Int) extends DoesNDmg(dmgCount) with DmgMySelf






