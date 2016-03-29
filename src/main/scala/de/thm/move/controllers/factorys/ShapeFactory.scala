/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */


package de.thm.move.controllers.factorys

import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle

import de.thm.move.models.CommonTypes._
import de.thm.move.models.SelectedShape
import de.thm.move.views.Anchor
import de.thm.move.views.shapes._

object ShapeFactory {
  def newImage(img:Image):ResizableImage = {
    new ResizableImage(img)
  }

  private def setDefaultColor[T <: ColorizableShape](shape:T)(fillColor:Color, strokeColor:Color, strokeWidth:Int): T = {
    shape.colorizeShape(fillColor, strokeColor)
    shape.setStrokeWidth(strokeWidth)
    shape
  }

  def newRectangle(point:Point, width:Double, height:Double): (Color, Color, Int) => ResizableRectangle = {
    val rectangle = new ResizableRectangle(point, width, height)
    (setDefaultColor(rectangle) _)
  }

  def newLine(start:Point, end:Point, strokeSize:Int): (Color, Color, Int) => ResizableLine = {
    val line = new ResizableLine(start, end, strokeSize)
    (setDefaultColor(line) _)
  }

  def newCircle(point:Point, width:Double, height:Double): (Color, Color, Int) => ResizableCircle = {
    val circle = new ResizableCircle(point, width, height)
    (setDefaultColor(circle) _)
  }

  def newAnchor(point:Point):Anchor = {
    val (x,y) = point
    new Anchor(x,y)
  }

  def newPolygon(points:List[Point]): (Color, Color, Int) => ResizablePolygon = {
    val polygon = ResizablePolygon(points)
    (setDefaultColor(polygon) _)
  }

  def createTemporaryShape(shape:SelectedShape.SelectedShape, p:Point)(stroke:Color): ResizableShape = {
    (shape match {
      case SelectedShape.Rectangle =>
        newRectangle(p, 2,2)
      case SelectedShape.Circle =>
        newCircle(p,2,2)
      case SelectedShape.Line =>
        newLine(p,p, 2)
    })(null, stroke, 2)
  }
}