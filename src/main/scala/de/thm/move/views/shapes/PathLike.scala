package de.thm.move.views.shapes

import javafx.geometry.Bounds
import javafx.scene.input.MouseEvent

import de.thm.move.controllers.implicits.FxHandlerImplicits._
import de.thm.move.Global
import de.thm.move.history.History
import de.thm.move.models.CommonTypes.Point
import de.thm.move.views.Anchor
import de.thm.move.util.PointUtils._
import de.thm.move.util.JFxUtils._

/** An element that is represented by a path.
  *
  * This trait adds moving/resizing the shape for free.
  * Due to a initializing problem please be careful and make sure you overwrite
  * getAnchors:List[Anchors] as follows:
  * {{{
  *   overwrite val getAnchors: List[Anchor] = genAnchors
  * }}}
  * genAnchors is already implemented, all you have to do is add the line above to your '''concrete''' class.
  * This trait can't overwrite getAnchors, this would result in a NullPointerException or in a emptylist because
  * edgeCount isn't proper initialized when getAnchors will be initialized!
 */
trait PathLike {
  self: ResizableShape =>
  /** Count of the edges of this shape
    *
    * Overwrite this field as a lazy val to avoid initialization problems! If you don't
    * use a lazy val this field is 0 and getAnchors will return an empty list!
    */
  val edgeCount:Int

  /* Overwrite as lazy val in order to initialize this field AFTER edgeCount is initialized!
    *
    * If this is a strict val it will initialized before edgeCount and result in a empty list
    * */
  override lazy val getAnchors: List[Anchor] = genAnchors

  protected def genAnchors = List.tabulate(edgeCount) { idx =>
    val (x,y) = getEdgePoint(idx)
    val anchor = new Anchor(x,y)

    var startP = (0.0,0.0)
    var mouseP = startP
    anchor.setOnMousePressed(withConsumedEvent { me:MouseEvent =>
      startP = (me.getSceneX,me.getSceneY)
      mouseP = startP
    })
    anchor.setOnMouseDragged(withConsumedEvent { mv: MouseEvent =>
      val delta = (mv.getSceneX - mouseP.x, mv.getSceneY - mouseP.y)
      resize(idx, delta)
      mouseP = (mv.getSceneX, mv.getSceneY)
    })
    anchor.setOnMouseReleased(withConsumedEvent { mv:MouseEvent =>
      //calculate delta (offset from original position) for un-/redo
      val deltaRedo = (mv.getSceneX - startP.x, mv.getSceneY - startP.y)
      val deltaUndo = deltaRedo.map(_*(-1))

      val cmd = History.
        newCommand(
          resize(idx, deltaRedo),
          resize(idx, deltaUndo)
        )
      Global.history.save(cmd)
    })
    anchor
  }


  boundsInLocalProperty().addListener { (_:Bounds, _:Bounds) =>
    boundsChanged()
  }

  rotateProperty().addListener { (_:Number, newV:Number) =>
    boundsChanged()
  }


  private def boundsChanged(): Unit = {
    indexWithAnchors.foreach { case (idx, anchor) =>
      val (x,y) = localToParentPoint(getEdgePoint(idx))
      anchor.setCenterX(x)
      anchor.setCenterY(y)
    }
  }

  private lazy val indexes:List[Int] = (0 until edgeCount).toList
  private lazy val indexWithAnchors = indexes.zip(getAnchors)

  def getEdgePoint(idx:Int): Point
  def resize(idx:Int, delta:Point): Unit
  override def move(delta:Point):Unit = indexWithAnchors.foreach {
    case (idx, anchor) =>
      resize(idx, delta)
  }
}
