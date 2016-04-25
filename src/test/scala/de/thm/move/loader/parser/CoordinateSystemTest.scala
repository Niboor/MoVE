package de.thm.move.loader.parser

import org.junit.Assert._
import org.junit.Test

import de.thm.move.loader.parser.PropertyParser._
import de.thm.move.loader.parser.ast._

import scala.util.parsing.input.Position
import scala.util.parsing.input.NoPosition

class CoordinateSystemTest {

  @Test
  def minimalSystem: Unit = {
    val str =
      """
        |model SimpleModel
        | annotation(
        |   Icon (
        |      coordinateSystem (
        |      extent = {{0,0},{756,504}}
        |      ),
        |     graphics = {}
        |   )
        | );
        |end SimpleModel;
      """.stripMargin

      val exp =
        Model("SimpleModel",
              Icon(
                Some(CoordinateSystem( ((0,0),(756,504)) )),
                List(),NoPosition, NoPosition
                )
        )
    iconEqual(exp, withParseSuccess(str))
  }

  @Test
  def fullSystem: Unit = {
    val str =
      """
        |model SimpleModel
        | annotation(
        |   Icon (
        |      coordinateSystem (
        |      extent = {{0,0},{756,504}},
        |      initialScale = 0.5,
        |      preserveAspectRatio = false
        |      ),
        |     graphics = {}
        |   )
        | );
        |end SimpleModel;
      """.stripMargin

      val exp =
        Model("SimpleModel",
              Icon(
                Some(CoordinateSystem(
                  ((0,0),(756,504)),
                  false, 0.5
                  )),
                List(),NoPosition,NoPosition
                )
            )
    iconEqual(exp, withParseSuccess(str))
  }
}
