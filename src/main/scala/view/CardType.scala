package view

/***
 * Enum that represents all the card Types, based on the zone of the field
 * in which they are positioned
 */
object CardType extends Enumeration {

  type CardType = String

  val active = "Active"
  val bench = "Bench"
  val hand = "Hand"
  val discardStack = "DiscardStack"
  val prize = "Prize"
  val deck = "Deck"

}
