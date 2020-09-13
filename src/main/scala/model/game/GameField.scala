package model.game

trait GameField {
  def playerBoard: Board
  def opponentBoard: Board
}

object GameField {
  def apply(playerBoard: Board, opponentBoard: Board): GameField = GameFieldImpl(playerBoard, opponentBoard)

  private case class GameFieldImpl(override val playerBoard: Board,
                                   override val opponentBoard: Board) extends GameField
}
