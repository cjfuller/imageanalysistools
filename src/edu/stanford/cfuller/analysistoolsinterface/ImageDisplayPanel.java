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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import javax.swing.JLabel;

/**
 *
 * @author cfuller
 */
public class ImageDisplayPanel extends javax.swing.JPanel {

    BufferedImage currentMaskedImage;
    BufferedImage originalImage;
    BufferedImage originalRescaledImage;
    BufferedImage currentDisplayImage;
    JLabel selectionBox;
    Point currentImageCoordinateTopLeft;
    Dimension currentDimensions;
    boolean isMaskingEnabled;
    Point currentImageCenter;

    public final static int MAX_RGB = 255;
    public final static int MAX_GREY = 65535;


    /** Creates new form ImageDisplayPanel */
    public ImageDisplayPanel() {
        selectionBox = new JLabel();
        selectionBox.setVisible(false);
        selectionBox.setBorder(new javax.swing.border.LineBorder(java.awt.Color.GREEN));
        this.add(selectionBox);
        this.currentImageCenter = new Point(0,0);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(this.currentDisplayImage, 0, 0, this);
    }

    public Point getCurrentMousePositionInImageCoordinates() {
        return this.translateDisplayPointToImagePoint(this.getMousePosition());
    }

    public Point translateDisplayPointToImagePoint(Point p) {

        if (p == null) return p;

        double widthFrac = p.getX()*1.0 / this.getSize().getWidth();
        double heightFrac = p.getY()*1.0 / this.getSize().getHeight();

        double imageX = widthFrac * this.currentDimensions.getWidth() + this.currentImageCoordinateTopLeft.getX();
        double imageY = heightFrac * this.currentDimensions.getHeight() + this.currentImageCoordinateTopLeft.getY();

        return new Point((int) imageX, (int) imageY);
    }

    public void compositeWithImage(BufferedImage b, int x0, int y0, int xf, int yf) {

        WritableRaster ar = b.getAlphaRaster();
        WritableRaster mask = b.getRaster();
        WritableRaster source = this.originalRescaledImage.getRaster();
        WritableRaster dest = this.currentMaskedImage.getRaster();

        for (int x = x0; x < xf; x++) {
            for (int y = y0; y < yf; y++) {

                //double alpha = ar.getSample(x,y,0)*1.0/MAX_RGB;


                //double c0 = alpha * mask.getSample(x, y, 0) + (1-alpha) * source.getSample(x, y, 0);
                //double c1 = alpha * mask.getSample(x, y, 0) + (1-alpha) * source.getSample(x, y, 0);
                //double c2 = alpha * mask.getSample(x, y, 0) + (1-alpha) * source.getSample(x, y, 0);

                

                dest.setSample(x,y,0, source.getSample(x,y,0));
                dest.setSample(x,y,1, source.getSample(x,y,0));
                dest.setSample(x,y,2, source.getSample(x,y,0));

            }
        }


        for (int x = x0; x < xf; x++) {
            for (int y = y0; y < yf; y++) {


                double alpha = ar.getSample(x,y,0)*1.0/MAX_RGB;


                double c0 = alpha * mask.getSample(x, y, 0) + (1-alpha) * dest.getSample(x, y, 0);
                double c1 = alpha * 0 + (1-alpha) * dest.getSample(x, y, 0);
                double c2 = alpha * 0 + (1-alpha) * dest.getSample(x, y, 0);



                dest.setSample(x,y,0, c0);
                dest.setSample(x,y,1, c1);
                dest.setSample(x,y,2, c2);
            }
        }
        // a hack to get it to update the zoomed Image...


        this.zoomImage(1.0, this.currentImageCenter);

    }


    public void setImage(BufferedImage b, boolean willBeMasked) {

        this.originalImage = b;

        if (willBeMasked) {

            this.currentMaskedImage = new BufferedImage(this.originalImage.getWidth(), this.originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

            this.originalRescaledImage = new BufferedImage(this.originalImage.getWidth(), this.originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);


            for (int x =0; x < b.getWidth(); x++) {
                for (int y = 0; y < b.getHeight(); y++) {
                    this.originalRescaledImage.getRaster().setSample(x,y,0,this.originalImage.getRaster().getSample(x,y,0));
                    this.originalRescaledImage.getRaster().setSample(x,y,1,this.originalImage.getRaster().getSample(x,y,0));
                    this.originalRescaledImage.getRaster().setSample(x,y,2,this.originalImage.getRaster().getSample(x,y,0));
                    this.originalRescaledImage.getAlphaRaster().setSample(x,y,0,MAX_RGB);
                    this.currentMaskedImage.getRaster().setSample(x,y,0,this.originalImage.getRaster().getSample(x,y,0));
                    this.currentMaskedImage.getRaster().setSample(x,y,1,this.originalImage.getRaster().getSample(x,y,0));
                    this.currentMaskedImage.getRaster().setSample(x,y,2,this.originalImage.getRaster().getSample(x,y,0));
                    this.currentMaskedImage.getAlphaRaster().setSample(x,y,0,MAX_RGB);
                }

            }

            this.rescaleImageIntensity(this.originalRescaledImage);

        } else {
            this.currentMaskedImage = this.originalImage;
            this.originalRescaledImage= this.originalImage;
        }

        this.isMaskingEnabled = willBeMasked;


        this.rescaleImageIntensity(this.currentMaskedImage);

        this.currentImageCoordinateTopLeft = new Point(0,0);
        this.currentDimensions = new Dimension(b.getWidth(), b.getHeight());

        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

        double widthScaleFactor = screenSize.getWidth()*0.5/this.currentDimensions.getWidth();
        double heightScaleFactor = screenSize.getHeight() * 0.5 / this.currentDimensions.getHeight();

        double scaleFactor = widthScaleFactor < heightScaleFactor ? widthScaleFactor : heightScaleFactor;

        if (scaleFactor > 1) scaleFactor = 1;

        Dimension newImageSize = new Dimension((int) (this.currentDimensions.getWidth()*scaleFactor), (int) (this.currentDimensions.getHeight() * scaleFactor));

        this.currentDisplayImage = this.resizeImage(this.currentMaskedImage, newImageSize);

        this.setSize(newImageSize);

        this.repaint();

    }

    public void zoomImage(double factor, Point center) {
        this.currentImageCenter = center;
        Dimension nextDimensions = new Dimension((int) (this.currentDimensions.getWidth()/factor), (int) (this.currentDimensions.getHeight()/factor));
        if (nextDimensions.getWidth() > this.originalImage.getWidth()) nextDimensions.setSize(this.originalImage.getWidth(), nextDimensions.getHeight());
        if (nextDimensions.getHeight() > this.originalImage.getHeight()) nextDimensions.setSize(nextDimensions.getWidth(), this.originalImage.getHeight());

        Point nextUpperLeft = new Point((int) (-1.0*nextDimensions.getWidth()/2.0 +center.getX()), (int) (-1.0*nextDimensions.getHeight()/2.0 + center.getY()));

        nextUpperLeft.setLocation(nextUpperLeft.getX() < 0? 0 : nextUpperLeft.getX(), nextUpperLeft.getY() < 0 ? 0 : nextUpperLeft.getY());

        int x0 = (int) nextUpperLeft.getX();
        int y0 = (int) nextUpperLeft.getY();
        int width = (int) nextDimensions.getWidth();
        int height = (int) nextDimensions.getHeight();

        if (width < 1 || height < 1) {

            if (originalImage.getWidth() < originalImage.getHeight()) {
                width = 1;
                height = (int) (width * originalImage.getHeight() *1.0/originalImage.getWidth());
            } else {
                height = 1;
                width = (int) (height * originalImage.getWidth() *1.0/originalImage.getHeight());
            }

        }

        width = (int) (height * originalImage.getWidth() *1.0/originalImage.getHeight());


        if (x0 + width > originalImage.getWidth()) {x0 = originalImage.getWidth() - width;}
        if (y0 + height > originalImage.getHeight()) {y0 = originalImage.getHeight() - height;}


        BufferedImage nextImage = currentMaskedImage.getSubimage(x0,y0,width,height);

        this.currentDisplayImage = this.resizeImage(nextImage, this.getSize());
        this.currentDimensions = new Dimension(width, height);
        this.currentImageCoordinateTopLeft = new Point(x0, y0);

        this.repaint();

    }

    public void resetZoom() {
        //this.setImage(this.originalImage, this.isMaskingEnabled);
        final double extremelySmallSoomFactor = 1e-6;
        this.zoomImage(extremelySmallSoomFactor, new Point(0,0));
    }

    protected BufferedImage resizeImage(BufferedImage toResize, Dimension newSize) {

        double scaleFactorWidth = newSize.getWidth()*1.0 / toResize.getWidth();
        double scaleFactorHeight = newSize.getHeight()*1.0 / toResize.getHeight();

        AffineTransformOp trans = new AffineTransformOp(AffineTransform.getScaleInstance(scaleFactorWidth, scaleFactorHeight), AffineTransformOp.TYPE_BICUBIC);

        return trans.filter(toResize, null);


    }


    public void drawSelectionBox(Point start, Point end) {

        int x_lower = (int) (start.getX() < end.getX() ? start.getX() : end.getX());
        int y_lower = (int) (start.getY() < end.getY() ? start.getY() : end.getY());

        int width = (int) Math.abs(start.getX() - end.getX());
        int height = (int) Math.abs(start.getY() - end.getY());

        selectionBox.setBounds(x_lower, y_lower, width, height);
        selectionBox.setVisible(true);
        this.repaint(0,0,0,this.getWidth(), this.getHeight());

    }
    public void clearSelectionBox() {
        selectionBox.setVisible(false);
        this.repaint();
    }

    public BufferedImage rescaleImageIntensity(BufferedImage b) {

        int minPossibleValue = 0;
        int maxPossibleValue = 0;

        if (b.getType() == BufferedImage.TYPE_USHORT_GRAY) {
            minPossibleValue = (MAX_GREY + 1)/8;
            maxPossibleValue = MAX_GREY-minPossibleValue;
        } else {
            minPossibleValue  = (MAX_RGB + 1)/8;
            maxPossibleValue = MAX_RGB - minPossibleValue;
        }

        WritableRaster wr = b.getRaster();

        int[] max = new int[wr.getNumBands()];
        java.util.Arrays.fill(max, 1);

        for (int x = 0; x < b.getWidth(); x++) {
            for (int y = 0; y < b.getHeight(); y++) {
                for (int c = 0; c < wr.getNumBands(); c++) {
                    if (wr.getSample(x,y,c) > max[c]) {
                        max[c] = wr.getSample(x,y,c);
                    }
                }
            }
        }

        for (int x = 0; x < b.getWidth(); x++) {
            for (int y = 0; y < b.getHeight(); y++) {
                for (int c = 0; c < wr.getNumBands(); c++) {
                    int offset = wr.getSample(x,y,c) > 0 ? minPossibleValue : 0;
                    double newValue = maxPossibleValue*1.0 / max[c] * wr.getSample(x,y,c) + offset;
                    if (newValue > maxPossibleValue + minPossibleValue) {newValue =maxPossibleValue + minPossibleValue;}
                    wr.setSample(x,y,c, newValue);
                }
            }
        }

        return b;


    }

}
