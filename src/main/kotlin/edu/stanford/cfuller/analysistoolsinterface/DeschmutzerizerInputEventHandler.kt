package edu.stanford.cfuller.analysistoolsinterface

import java.awt.Point
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseEvent
import javax.swing.event.MouseInputListener

/**

 * @author cfuller
 */
class DeschmutzerizerInputEventHandler(internal var dc: DeschmutzerizerController) : MouseInputListener, KeyListener {

    var mouseDownPoint: Point? = null
        protected set
    var mouseUpPoint: Point? = null
        protected set
    var mouseDragEndPoint: Point? = null
        protected set


    override fun mouseClicked(me: MouseEvent) {


        this.dc.processSelectedPoint()

    }

    override fun mousePressed(me: MouseEvent) {

    }

    override fun mouseReleased(me: MouseEvent) {

        this.dc.shouldDrawMouseDragBox(false)

        this.dc.processSelectedBox()

    }

    override fun mouseEntered(me: MouseEvent) {}

    override fun mouseExited(me: MouseEvent) {}

    override fun mouseDragged(me: MouseEvent) {


    }

    override fun mouseMoved(me: MouseEvent) {}

    override fun keyTyped(ke: KeyEvent) {}

    override fun keyPressed(ke: KeyEvent) {

    }

    override fun keyReleased(ke: KeyEvent) {}

}
