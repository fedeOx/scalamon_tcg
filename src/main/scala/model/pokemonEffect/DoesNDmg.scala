package model.pokemonEffect

import model.Cards.PokemonCard
import model.EnergyType


case class DoesNDmg(baseDmgCount: Int) extends AttackEffect {
  override def useEffect(defendingPokemons: Seq[PokemonCard], attackingPokemon: PokemonCard): Unit = defendingPokemons.size match {
    case 1 => defendingPokemons.head.addDamage(totalDmg,attackingPokemon.pokemonTypes)
    case _ => defendingPokemons.foreach(pkm => pkm.addDamage(totalDmg,Seq(EnergyType.colorless)))
  }

  override var args: Map[String, Any] = Map.empty[String, Any]
  override var totalDmg: Int = totalDmg + baseDmgCount
}

//es. Blastoise (atk)
//es. MewTwo    (def)
trait ForEachEnergyAttachedTo extends AttackEffect{
  abstract override def useEffect(defendingPokemons: Seq[PokemonCard], attackingPokemon: PokemonCard): Unit = {
    var pokemonToApply : PokemonCard = null
    val limitedTo = getStringArgFromMap("limited").toInt
    getStringArgFromMap("atk_or_def") match {
       case "atk" => pokemonToApply = attackingPokemon
       case "def" => pokemonToApply = defendingPokemons.head
     }
    var dmgToAdd = pokemonToApply.totalEnergiesStored

    //es. Blastoise / Poliwrath
    if(limitedTo > 0) {
      dmgToAdd = pokemonToApply.totalEnergiesStored - pokemonToApply.attacks(getStringArgFromMap("attackPosition").toInt-1).cost.size
      if(dmgToAdd > limitedTo )
        dmgToAdd = limitedTo
    }

    totalDmg += getIntArgFromMap("dmgCount") * dmgToAdd

    super.useEffect(defendingPokemons: Seq[PokemonCard], attackingPokemon: PokemonCard)
  }
}

trait ForEachDamageCount extends AttackEffect {
  abstract override def useEffect(defendingPokemons: Seq[PokemonCard], attackingPokemon: PokemonCard): Unit = {
    totalDmg +=  (-(attackingPokemon.actualHp - attackingPokemon.initialHp))
    super.useEffect(defendingPokemons: Seq[PokemonCard], attackingPokemon: PokemonCard)
  }
}

//es. Magneton
trait DmgMySelf extends AttackEffect {
  abstract override def useEffect(defendingPokemons: Seq[PokemonCard], attackingPokemon: PokemonCard): Unit = {
   attackingPokemon.actualHp = attackingPokemon.actualHp- getIntArgFromMap("DmgMyself")
    super.useEffect(defendingPokemons: Seq[PokemonCard], attackingPokemon: PokemonCard)
  }
}





class DoesNDmgForEachEnergyAttachedTo(dmgCount: Int) extends DoesNDmg(dmgCount) with ForEachEnergyAttachedTo
class DoesNDmgForEachDamageCount(dmgCount: Int) extends DoesNDmg(dmgCount) with ForEachDamageCount
class DoesNDmgAndDmgMyself(dmgCount: Int) extends DoesNDmg(dmgCount) with DmgMySelf






