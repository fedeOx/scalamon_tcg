package model.card

import model.game.SetType.SetType

trait Card {
  def id: String
  def imageNumber: Int
  def belongingSet: SetType
  def name: String
  def rarity: String
  def belongingSetCode: String
}
