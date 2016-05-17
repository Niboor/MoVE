/**
 * Copyright (C) 2016 Nicola Justus <nicola.justus@mni.thm.de>
 */

package de.thm.move.config

import de.thm.move.models.CommonTypes._

trait Config {
  def getAll: List[(String,String)]
  def getString(key:String):Option[String]
  def getInt(key:String):Option[Int] =
    try {
      getString(key).map(_.toInt)
    } catch {
      case _:NumberFormatException => None
    }

  def getDouble(key:String): Option[Double] =
    try {
      getString(key).map(_.toDouble)
    } catch {
      case _:NumberFormatException => None
    }

  def getFloat(key:String): Option[Float] = getDouble(key).map(_.toFloat)
  def getBoolean(key:String): Option[Boolean] = getString(key).map {
    case "true" => true
    case _ => false
  }

  def getPoint(xKey:String, yKey:String): Option[Point] =
    for {
      x <- getDouble(xKey)
      y <- getDouble(yKey)
    } yield (x,y)
}
