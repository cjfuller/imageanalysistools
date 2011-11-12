/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2011 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */

package edu.stanford.cfuller.analysistoolsinterface;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputListener;

/**
 *
 * @author cfuller
 */
public class DeschmutzerizerInputEventHandler implements MouseInputListener, KeyListener {

    protected java.awt.Point mouseDownPoint;
    protected java.awt.Point mouseUpPoint;
    protected java.awt.Point mouseDragEndPoint;

    DeschmutzerizerController dc;

    public DeschmutzerizerInputEventHandler(DeschmutzerizerController dc) {
        this.dc = dc;
    }


    @Override
    public void mouseClicked(MouseEvent me) {


        this.dc.processSelectedPoint();

    }

    @Override
    public void mousePressed(MouseEvent me) {

    }

    @Override
    public void mouseReleased(MouseEvent me) {

        this.dc.shouldDrawMouseDragBox(false);

        this.dc.processSelectedBox();

    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    @Override
    public void mouseDragged(MouseEvent me) {


    }

    @Override
    public void mouseMoved(MouseEvent me) {
    }

    @Override
    public void keyTyped(KeyEvent ke) {
    }

    @Override
    public void keyPressed(KeyEvent ke) {

    }

    @Override
    public void keyReleased(KeyEvent ke) {
    }

    public Point getMouseDownPoint() {
        return this.mouseDownPoint;
    }

    public Point getMouseUpPoint() {
        return this.mouseUpPoint;
    }

    public Point getMouseDragEndPoint() {
        return this.mouseDragEndPoint;
    }

}
