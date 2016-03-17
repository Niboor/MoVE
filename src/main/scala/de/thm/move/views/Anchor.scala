package de.thm.move.views

import javafx.scene.paint.Color
import javafx.scene.shape.{Ellipse, Circle}
import de.thm.move.models.CommonTypes.Point

class Anchor(x:Double, y:Double, fillColor:Color = Anchor.anchorFill) extends Ellipse {
  def this(p:Point) = this(p._1, p._2)

  this.setCenterX(x)
  this.setCenterY(y)
  this.setFill(fillColor)
  this.setRadiusX(Anchor.anchorWeight)
  this.setRadiusY(Anchor.anchorHeight)
}

object Anchor {
  val anchorWeight = 5.0
  val anchorHeight = 5.0
  val anchorFill = Color.RED
}