package view

/***
 * Enum that represents all the card Types, based on the zone of the field
 * in which they are positioned
 */
object CardType extends Enumeration {

  type CardType = String

  val Active = "Active"
  val Bench = "Bench"
  val Hand = "Hand"
  val DiscardStack = "DiscardStack"
  val Prize = "Prize"
  val Deck = "Deck"
  val Placeholder = "Placeholder"

}
