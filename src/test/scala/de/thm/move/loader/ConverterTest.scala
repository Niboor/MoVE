/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.thm.move.loader

import javafx.scene.paint.Color

import de.thm.move.models.LinePattern
import de.thm.move.views.shapes._
import de.thm.move.MoveSpec
import de.thm.move.loader.parser.PropertyParser._
import de.thm.move.loader.parser.ast._
import de.thm.move.types._
import de.thm.move.util.GeometryUtils
import de.thm.move.util.GeometryUtils._

import scala.util.parsing.input.Position
import scala.util.parsing.input.NoPosition

class ConverterTest extends MoveSpec {

  val positiveCoordinateSystem = Some(CoordinateSystem(((0.0,0.0), (100.0,100.0))))

  "ShapeConverter.`getCoordinateSystem`" should "return coordinate systems" in {

    val extent = ( ((-100.0),(-50.0)),((100.0),(50.0)) )
    val ast = Model("ölkj",
    Icon(Some(CoordinateSystem(extent)), List(), NoPosition, NoPosition)
    )
    val conv = new ShapeConverter(4, ShapeConverter.getCoordinateSystem(ast), null)

    ShapeConverter.getCoordinateSystem(ast) shouldBe extent

    val ast2 = Model("ög",
      Icon(Some(CoordinateSystem(extent)), List(), NoPosition, NoPosition)
    )

    val x = ShapeConverter.getCoordinateSystem(ast)
    x shouldBe extent

    val extent2 = ( ((-100.0),(-50.0)),((0.0),(-20.0)) )
    val ast3 = Model("ölkj",
    Icon(Some(CoordinateSystem(extent2)), List(), NoPosition, NoPosition)
    )
    ShapeConverter.getCoordinateSystem(ast3) shouldBe extent2
  }

  "ShapeConverter.`scaledSystemSize" should "return the size of the coordinate system that is multiplied by the given factor" in {
    val conv = new ShapeConverter(2, ( (-100,-100), (50,50) ), null)
    conv.scaledSystemSize shouldBe (150*2,150*2)
  }

  "ShapeConverter.`convertPoint`" should "convert negative x coordinates" in {
    val converter = new ShapeConverter(1, ((-100,0), (100,100)), null)
    converter.convertPoint(-50, 50) shouldBe (50, 50)
    converter.convertPoint(-80, 0) shouldBe (20, 100)
    converter.convertPoint(-100, 100) shouldBe (0, 0)
  }
  it should "convert negative y coordinates" in {
    val converter = new ShapeConverter(1, ((0,-100), (100,100)), null)
    converter.convertPoint(0, -50) shouldBe (0, 150)
    converter.convertPoint(0, -100) shouldBe (0, 200)
    converter.convertPoint(0, 100) shouldBe (0, 0)
  }

  "ShapeConverter" should "convert Lines" in {
    //without origin
    val extent = ( ((0.0),(0.0)),((100.0),(200.0)) )
    val ast = Model("ölkj",
    Icon(Some(CoordinateSystem(extent)),
      List(
          PathElement(
            GraphicItem(),
            List( (10.0,10.0),(50.0,30.0) ),
            Color.BLACK,
            1.0,
            "LinePattern.Dash"
            )
        ), NoPosition, NoPosition
    ))

    val conv = new ShapeConverter(1, ShapeConverter.getCoordinateSystem(ast), null)

    val convertedLine = conv.getShapes(ast).head.asInstanceOf[(ResizableLine, Option[String])]._1
    val startAnchor = convertedLine.getAnchors.head
    val endAnchor = convertedLine.getAnchors.tail.head

    startAnchor.getCenterX shouldBe 10.0
    startAnchor.getCenterY shouldBe (200.0-10.0)
    endAnchor.getCenterX shouldBe 50.0
    endAnchor.getCenterY shouldBe (200.0-30.0)
    convertedLine.getStrokeColor shouldBe Color.BLACK
    convertedLine.getStrokeWidth shouldBe 1.0
    convertedLine.linePattern.get shouldBe LinePattern.Dash


    val points2 = List( (10.0,10.0),(50.0,30.0), (60.0,80.0),(30.0,30.0) )
    val ast2 = Model("ölkj",
      Icon(Some(CoordinateSystem(extent)),
        List(
          PathElement(
            GraphicItem(),
            points2,
            Color.BLACK,
            1.0,
            "LinePattern.Dash"
          )
        ),NoPosition, NoPosition
      ))

    val expectedPoints = points2.map {
      case (x,y) => (x, 200-y)
    }
    val convPath = conv.getShapes(ast2).head.asInstanceOf[(ResizablePath, Option[String])]._1
    convPath.getAnchors.zip(expectedPoints).foreach {
      case (p1,p2) => p2 shouldBe (p1.getCenterX,p1.getCenterY)
    }

    val converter = new ShapeConverter(5, ShapeConverter.getCoordinateSystem(ast2), null)
    val convPath2 = converter.getShapes(ast2).head.asInstanceOf[(ResizablePath, Option[String])]._1
    convPath2.getAnchors.zip(expectedPoints.map(_.map(_*5))).foreach {
      case (p1,p2) => (p1.getCenterX,p1.getCenterY) shouldBe p2
    }
  }

  it should "convert Rectangels" in {
    val ast = Model("ölk",
      Icon(positiveCoordinateSystem,
        List(
          RectangleElement(GraphicItem(),
            FilledShape(),
            extent = ( (205,179),(348,36) )
          )
        ),NoPosition, NoPosition
      )
    )

    val conv = new ShapeConverter(1, ShapeConverter.getCoordinateSystem(ast), null)
    val convRec = conv.getShapes(ast).head.asInstanceOf[(ResizableRectangle, Option[String])]._1
    convRec.getXY shouldBe (205,defaultCoordinateSystemSize.y-179)
    convRec.getWidth  shouldBe 348-205
    convRec.getHeight shouldBe 179-36

    val multiplier = 5
    val conv2 = new ShapeConverter(multiplier, ShapeConverter.getCoordinateSystem(ast), null)
    val convRec2 = conv2.getShapes(ast).head.asInstanceOf[(ResizableRectangle, Option[String])]._1
    convRec2.getXY shouldBe (205*multiplier,(defaultCoordinateSystemSize.y-179)*multiplier)
    convRec2.getWidth  shouldBe ((348-205)*multiplier)
    convRec2.getHeight shouldBe ((179-36)*multiplier)
  }

  it should "convert Ellipses" in {
    val ast = Model("ölk",
      Icon(positiveCoordinateSystem,
        List(
          Ellipse(GraphicItem(),
            FilledShape(),
            extent = ( (205,179),(348,36) )
          )
        ),NoPosition, NoPosition
      )
    )

    val conv = new ShapeConverter(1, ShapeConverter.getCoordinateSystem(ast), null)
    val convCircle = conv.getShapes(ast).head.asInstanceOf[(ResizableCircle, Option[String])]._1
    val middleP = GeometryUtils.middleOfLine(205,
      defaultCoordinateSystemSize.y-179,
      348, defaultCoordinateSystemSize.y-36
      )
    convCircle.getWidth  shouldBe (348-205)
    convCircle.getHeight shouldBe (179-36)

    val multiplier = 2
    val conv2 = new ShapeConverter(multiplier, ShapeConverter.getCoordinateSystem(ast), null)
    val conv2Circle = conv2.getShapes(ast).head.asInstanceOf[(ResizableCircle, Option[String])]._1

    val middleP2 = GeometryUtils.middleOfLine(205*multiplier,
      (defaultCoordinateSystemSize.y-179)*multiplier,
      348*multiplier, (defaultCoordinateSystemSize.y-36)*multiplier
      )

    conv2Circle.getXY shouldBe middleP2
    conv2Circle.getWidth  shouldBe ((348-205)*multiplier)
    conv2Circle.getHeight shouldBe ((179-36)*multiplier)
  }

  it should "convert Polygons" in {
    val points = List( (205.0,179.0),(348.0,36.0),(420.0,50.0) )
    val ast = Model("ölk",
      Icon(positiveCoordinateSystem,
        List(
          Polygon(GraphicItem(),
            FilledShape(),
            points
          )
        ),NoPosition, NoPosition
      )
    )

    val expPoints = points.map {
      case (x,y) => (x, defaultCoordinateSystemSize.y-y)
    }

    val conv = new ShapeConverter(1, ShapeConverter.getCoordinateSystem(ast), null)
    val convPolygon = conv.getShapes(ast).head.asInstanceOf[(ResizablePolygon, Option[String])]._1
    convPolygon.getAnchors.zip(expPoints).foreach {
      case (anchor,p2) =>
      val p1 = (anchor.getCenterX,anchor.getCenterY)
      p2 shouldBe p1
    }

    val multiplier = 4
    val conv2 = new ShapeConverter(multiplier, ShapeConverter.getCoordinateSystem(ast), null)
    val conv2Polygon = conv2.getShapes(ast).head.asInstanceOf[(ResizablePolygon, Option[String])]._1
    conv2Polygon.getAnchors.zip(expPoints.map(_.map(_*multiplier))).foreach {
      case (anchor,p2) =>
        val p1 = (anchor.getCenterX,anchor.getCenterY)
        p2 shouldBe p1
    }
  }

  it should "convert Images" in {
    val extent = ( (10.0,10.0),(200.0,100.0) )
    val ast =
      Model("bitmap",
        Icon(positiveCoordinateSystem,
          List(
            ImageURI(GraphicItem(),
              extent,
              "modelica://test3/quokka.jpg"
            )
          ),NoPosition, NoPosition
        )
      )

    //val  conv = new ShapeConverter(1, ShapeConverter.getCoordinateSystem(ast).head)
    //conv.getShapes(ast).head.asInstanceOf[ResizableImage]
  }

  it should "convert Rectangles containing a origin" in {
    val origin:Point = (10,10)
    val ast = Model("ölk",
      Icon(positiveCoordinateSystem,
        List(
          RectangleElement(GraphicItem(origin = origin),
            FilledShape(),
            extent = ( (-10,50),(30,-40) )
          )
        ),NoPosition, NoPosition
      )
    )

    val conv = new ShapeConverter(1, ShapeConverter.getCoordinateSystem(ast), null)
    val rect = conv.getShapes(ast).head.asInstanceOf[(ResizableRectangle, Option[String])]._1

    val expXY:Point = (10-10,defaultCoordinateSystemSize.y-(10+50))
    val expW = 10+30
    val expH = 50+40

    rect.getXY     shouldBe expXY
    rect.getWidth  shouldBe expW
    rect.getHeight shouldBe expH

    val origin2:Point = (50,30)
    val ast2 = Model("ölk",
      Icon(positiveCoordinateSystem,
        List(
          RectangleElement(GraphicItem(origin = origin2),
            FilledShape(),
            extent = ( (-5,70),(10,-20) )
          )
        ),NoPosition, NoPosition
      )
      )

      val multiplier = 2
      val conv2 = new ShapeConverter(multiplier, ShapeConverter.getCoordinateSystem(ast), null)

      val rect2 = conv2.getShapes(ast2).head.asInstanceOf[(ResizableRectangle, Option[String])]._1
      val expXY2:Point = (50-5,defaultCoordinateSystemSize.y-(30+70))
      val expW2 = 15
      val expH2 = 70+20

      rect2.getXY shouldBe expXY2.map(_*multiplier)
      rect2.getWidth  shouldBe (expW2*multiplier)
      rect2.getHeight shouldBe (expH2*multiplier)
  }

  it should "convert Ellipses containing a origin" in  {
    val origin:Point = (50,100)
    val ast = Model("ölk",
      Icon(positiveCoordinateSystem,
        List(
          Ellipse(GraphicItem(origin = origin),
            FilledShape(),
            extent = ( (-10,50),(30,-40) )
          )
        ),NoPosition, NoPosition
      )
    )

    val conv = new ShapeConverter(1, ShapeConverter.getCoordinateSystem(ast), null)
    val circ = conv.getShapes(ast).head.asInstanceOf[(ResizableCircle, Option[String])]._1

    val expCenterXY = (origin.x, defaultCoordinateSystemSize.y-origin.y)
    val expWRadius = asRadius(10+30)
    val expHRadius = asRadius(50+40)

    circ.getRadiusX shouldBe expWRadius
    circ.getRadiusY shouldBe expHRadius
    circ.getBoundsInLocal.getMinX shouldBe (40.0 +- 1)
    circ.getBoundsInLocal.getMaxX shouldBe (80.0 +- 1)
    circ.getBoundsInLocal.getMinY shouldBe ((defaultCoordinateSystemSize.y - 150) +- 1)
  }

  it should "convert icons with negative coordinate systems" in {
    val origin:Point = (-75, 30)
    val ast = Model("abc",
      Icon(Some(CoordinateSystem( ((-100,0), (200,200)) )),
        List(
          RectangleElement(GraphicItem(origin = origin),
            FilledShape(),
            extent = ( (-10,20),(10,-10) )
          )
        ),NoPosition, NoPosition
        )
      )
    val conv = new ShapeConverter(1, ShapeConverter.getCoordinateSystem(ast), null)
    val rec = conv.getShapes(ast).head.asInstanceOf[(ResizableRectangle, Option[String])]._1

    rec.getXY shouldBe ( 100.0-75-10, 200.0-(30+20) )
    rec.getWidth shouldBe (10.0+10 +- 1)
    rec.getHeight shouldBe (20.0+10 +-1)

    val origin2:Point = (-50, -50)
    val ast2 = Model("abc",
      Icon(Some(CoordinateSystem( ((-100,-50), (200,200)) )),
        List(
          RectangleElement(GraphicItem(origin = origin2),
            FilledShape(),
            extent = ( (-10,20),(10,-10) )
          )
        ),NoPosition, NoPosition
      )
    )
    val conv2 = new ShapeConverter(2, ShapeConverter.getCoordinateSystem(ast2), null)
    val rec2 = conv2.getShapes(ast2).head.asInstanceOf[(ResizableRectangle, Option[String])]._1

    rec2.getXY shouldBe ( (100.0-50-10)*2, (250.0-20)*2)
    rec2.getWidth shouldBe ((10.0+10)*2 +- 1)
    rec2.getHeight shouldBe ((20.0+10)*2 +-1)

//    val origin3:Point = (-30, -30)
//    val ast3 = Model("abc",
//      Icon(Some(CoordinateSystem( ((-100,-100), (200,200)) )),
//        List(
//          Ellipse(GraphicItem(origin = origin3),
//            FilledShape(),
//            extent = ( (-10,20),(10,-10) )
//          )
//        ),NoPosition, NoPosition
//      )
//    )
//    val conv3 = new ShapeConverter(1, ShapeConverter.getCoordinateSystem(ast3), null)
//    val circ = conv3.getShapes(ast3).head.asInstanceOf[(ResizableCircle, Option[String])]._1
//    circ.getXY shouldBe (100.0-30, 300.0-70)
  }
}
