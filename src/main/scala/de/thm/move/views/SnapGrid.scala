package de.thm.move.views

import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Line

import de.thm.move.controllers.implicits.FxHandlerImplicits._

class SnapGrid(topPane:Pane) extends Pane {

  val verticalLineId = "vertical-grid-line"
  val horizontalLineId = "horizontal-grid-line"

  setPickOnBounds(false)
  getStyleClass.add("snap-grid-pane")

  heightProperty().addListener { (_:Number, newH:Number) =>
    val newLines = recalculateHorizontalLines(newH.doubleValue())
    getChildren.removeIf { node:Node => node.getId == horizontalLineId }
    getChildren.addAll(newLines: _*)
    ()
  }
  widthProperty().addListener { (_:Number, newW:Number) =>
    val newLines = recalculateVerticalLines(newW.doubleValue())
    getChildren.removeIf { node:Node => node.getId == verticalLineId }
    getChildren.addAll(newLines:_*)
    ()
  }

  def recalculateHorizontalLines(height:Double): Seq[Line] =
    for(i <- 1 to (height/20).toInt) yield {
      val line = newGridLine
      line.setId(horizontalLineId)
      line.setStartX(0)
      line.endXProperty().bind(widthProperty())
      val y = i*20
      line.setStartY(y)
      line.setEndY(y)
      line
    }

  def recalculateVerticalLines(width:Double): Seq[Line] =
    for(i <- 1 to (width/20).toInt) yield {
      val line = newGridLine
      line.setId(verticalLineId)
      line.setStartY(0)
      line.endYProperty().bind(heightProperty())
      val x = i*20
      line.setStartX(x)
      line.setEndX(x)
      line
    }

  def newGridLine: Line = {
    val line = new Line()
    line.getStyleClass.add("grid-line")
    line
  }
}