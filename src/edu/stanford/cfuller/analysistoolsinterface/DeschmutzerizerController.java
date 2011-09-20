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

import edu.stanford.cfuller.imageanalysistools.frontend.DataSummary;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import edu.stanford.cfuller.imageanalysistools.image.io.ImageReader;
import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 *
 * @author cfuller
 */
public class DeschmutzerizerController extends TaskController {

    public static final int ZOOM_IN_KEY_CODE = java.awt.event.KeyEvent.VK_EQUALS;
    public static final int ZOOM_OUT_KEY_CODE = java.awt.event.KeyEvent.VK_MINUS;
    public static final int ZOOM_RESET_KEY_CODE = java.awt.event.KeyEvent.VK_0;

    public static final String PARAMETERS_DIR = "parameters";
    public static final String OUTPUT_DIR = "deschmutzed";

    static final double zoomFactor = 1.51;

    DeschmutzerizerWindow dw;
    DeschmutzerizerInputEventHandler dieh;
    ImageDisplayPanel originalImageDisplay;
    ImageDisplayPanel maskImageDisplay;
    JFrame originalImageWindow;
    JFrame maskWindow;

    Image currentOriginalImage;
    Image currentMaskImage;
    Image currentLabeledMaskImage;

    HashSet selectedRegions;

    BufferedImage colorCodedMaskDisplay;

    java.util.List<String> filenamesToProcess;

    String currentMaskFilename;
    String currentDataFilename;
    String currentLabeledMaskFilename;
    String currentParametersFilename;

    ParameterDictionary currentParameters;

    public void zoomIn() {

        this.originalImageDisplay.zoomImage(zoomFactor, this.maskImageDisplay.getCurrentMousePositionInImageCoordinates());
        this.maskImageDisplay.zoomImage(zoomFactor, this.maskImageDisplay.getCurrentMousePositionInImageCoordinates());
        
    }

    
    public void zoomOut() {
        
        this.originalImageDisplay.zoomImage(1.0/zoomFactor, this.maskImageDisplay.getCurrentMousePositionInImageCoordinates());
        this.maskImageDisplay.zoomImage(1.0/zoomFactor, this.maskImageDisplay.getCurrentMousePositionInImageCoordinates());

    }
    public void resetZoom() {
        
        this.originalImageDisplay.resetZoom();
        this.maskImageDisplay.resetZoom();
    }

    public void shouldDrawMouseDragBox(boolean shouldDraw) {

        if (shouldDraw) {
            this.originalImageDisplay.drawSelectionBox(this.dieh.getMouseDownPoint(), this.dieh.getMouseDragEndPoint());
            this.maskImageDisplay.drawSelectionBox(this.dieh.getMouseDownPoint(), this.dieh.getMouseDragEndPoint());

        } else {
            this.originalImageDisplay.clearSelectionBox();
            this.maskImageDisplay.clearSelectionBox();

        }

    }

    public void processBox(Point start, Point end) {

        Image regionNumberCheck = null;

        if (this.dw.groupSelectionSelected()) {
            regionNumberCheck = this.currentMaskImage;
        } else {
            regionNumberCheck = this.currentLabeledMaskImage;
        }

        int xLower = (int) (start.getX() < end.getX() ? start.getX() : end.getX());
        int yLower = (int) (start.getY() < end.getY() ? start.getY() : end.getY());

        int width = (int) Math.abs(start.getX() - end.getX());
        int height = (int) Math.abs(start.getY() - end.getY());

        if (xLower < 0) {
            width += xLower;
            if (width < 0) width = 0;
            xLower = 0;
        }

        if (yLower < 0) {
            height += yLower;
            if (height < 0) height = 0;
            yLower = 0;
        }

        if (xLower + width > regionNumberCheck.getDimensionSizes().getX()) {
            width = regionNumberCheck.getDimensionSizes().getX() - xLower;
        }

        if (yLower + height > regionNumberCheck.getDimensionSizes().getY()) {
            height = regionNumberCheck.getDimensionSizes().getY() - yLower;
        }
        

        


        ImageCoordinate startCoord = ImageCoordinate.createCoordXYZCT((int) xLower,(int)  yLower, 0,0,0);
        ImageCoordinate endCoord = ImageCoordinate.createCoordXYZCT((int) xLower + width+1,(int) yLower + height +1, 1,1, 1);

        regionNumberCheck.setBoxOfInterest(startCoord, endCoord);

        java.util.HashSet<Integer> tempSelectedRegions = new java.util.HashSet<Integer>();

        for (ImageCoordinate ic : regionNumberCheck) {
            if (regionNumberCheck.getValue(ic) > 0) {
                tempSelectedRegions.add((int) regionNumberCheck.getValue(ic));
            }
        }

        //for (Integer i : tempSelectedRegions) {System.out.println("selected " + i);}


        startCoord.recycle();
        endCoord.recycle();

        regionNumberCheck.clearBoxOfInterest();

        int x0 = regionNumberCheck.getDimensionSizes().get(ImageCoordinate.X);
        int xf = 0;
        int y0 = regionNumberCheck.getDimensionSizes().get(ImageCoordinate.Y);
        int yf = 0;

        java.util.HashSet<Integer> nextSelectedRegions = new java.util.HashSet<Integer>();

        nextSelectedRegions.addAll(this.selectedRegions);

        for (ImageCoordinate ic : regionNumberCheck) {

            int x = ic.get(ImageCoordinate.X);
            int y = ic.get(ImageCoordinate.Y);

            if (tempSelectedRegions.contains((int) regionNumberCheck.getValue(ic))  && ! this.selectedRegions.contains((int) this.currentLabeledMaskImage.getValue(ic))) {
                
                this.colorCodedMaskDisplay.getAlphaRaster().setSample(ic.get(ImageCoordinate.X), ic.get(ImageCoordinate.Y), 0, ImageDisplayPanel.MAX_RGB);

                nextSelectedRegions.add((int) this.currentLabeledMaskImage.getValue(ic));

                if (x < x0) x0 = x;
                if (x >= xf) xf = x+1;
                if (y < y0) y0 = y;
                if (y >= yf) yf = y+1;
                

            } else if (tempSelectedRegions.contains((int) regionNumberCheck.getValue(ic))  && this.selectedRegions.contains((int) this.currentLabeledMaskImage.getValue(ic))) {
                

                nextSelectedRegions.remove((int) this.currentLabeledMaskImage.getValue(ic));

                if (x < x0) x0 = x;
                if (x >= xf) xf = x+1;
                if (y < y0) y0 = y;
                if (y >= yf) yf = y+1;

                this.colorCodedMaskDisplay.getAlphaRaster().setSample(ic.get(ImageCoordinate.X), ic.get(ImageCoordinate.Y), 0, 0);

            }
        
        }

        this.selectedRegions = nextSelectedRegions;

        this.maskImageDisplay.compositeWithImage(this.colorCodedMaskDisplay, x0, y0, xf, yf);

       

    }

    public void processSelectedBox() {

        this.processBox(this.maskImageDisplay.translateDisplayPointToImagePoint(this.dieh.getMouseDownPoint()), this.maskImageDisplay.translateDisplayPointToImagePoint(this.dieh.getMouseUpPoint()));

        

    }

    public void processSelectedPoint() {
        this.processBox(this.maskImageDisplay.translateDisplayPointToImagePoint(this.dieh.getMouseUpPoint()), this.maskImageDisplay.translateDisplayPointToImagePoint(this.dieh.getMouseUpPoint()));
    }

    @Override
    public void startTask() {

        this.currentParameters = null;

        this.selectedRegions = new java.util.HashSet();

        this.dw = new DeschmutzerizerWindow(this);

        this.dw.addWindowListener(this);

        this.dieh = new DeschmutzerizerInputEventHandler(this);

        this.dw.setImageFilename(Preferences.userNodeForPackage(this.getClass()).get("deschmutzerizer_filename", this.dw.getImageFilename()));

        this.dw.setVisible(true);


        this.originalImageDisplay = new ImageDisplayPanel();
        this.originalImageWindow = new JFrame();
        this.originalImageWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.originalImageWindow.setLocationRelativeTo(this.dw);
        this.originalImageWindow.setLocation((int) (this.originalImageWindow.getLocation().getX()-this.dw.getWidth()/2), (int) (this.originalImageWindow.getLocation().getY() + this.dw.getHeight()/2));



        this.maskImageDisplay = new ImageDisplayPanel();
        this.maskWindow = new JFrame();
        this.maskWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.maskWindow.setLocationRelativeTo(this.dw);
        this.maskWindow.setLocation((int) (this.maskWindow.getLocation().getX()+this.dw.getWidth()*0.6), (int) (this.maskWindow.getLocation().getY() - this.dw.getHeight()/2));


       
        this.maskImageDisplay.addKeyListener(this.dieh);
        this.maskWindow.addKeyListener(this.dieh);

        
        this.maskImageDisplay.addMouseListener(this.dieh);
        this.maskWindow.addMouseListener(this.dieh);

        
        this.maskImageDisplay.addMouseMotionListener(this.dieh);
        this.maskWindow.addMouseMotionListener(this.dieh);

        this.originalImageWindow.getContentPane().add(this.originalImageDisplay);
        this.maskWindow.getContentPane().add(this.maskImageDisplay);


    }

    private void finish(){

        this.dw.setDone(true);
        this.dw.setStartButtonEnabled(true);
        if (this.currentParameters != null) {
            try {
                DataSummary.SummarizeData((new File(this.getOutputFilename(this.currentDataFilename))).getParent(), (new File(this.currentParametersFilename)).getParent());
            } catch (java.io.IOException e ) {
                LoggingUtilities.severe("encountered exception while making data summary");
            }
        }
    }

    private void createOutputDirectory(String outputFilename) {
        File dirFile = (new File(outputFilename)).getParentFile();
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
    }

    private void finishCurrentImage() {


        this.originalImageWindow.setVisible(false);
        this.maskWindow.setVisible(false);


        Image outputImage = new Image(this.currentMaskImage);
        Image outputImageLabeled = new Image(this.currentLabeledMaskImage);
        String outputImageFilename = this.getOutputFilename(this.currentMaskFilename);
        String outputImageLabeledFilename = this.getOutputFilename(this.currentLabeledMaskFilename);
        this.createOutputDirectory(outputImageFilename);

        for (ImageCoordinate ic : outputImage) {
            if (this.selectedRegions.contains((int) this.currentLabeledMaskImage.getValue(ic))) {
                outputImage.setValue(ic, 0);
                outputImageLabeled.setValue(ic, 0);
            }
        }


        outputImage.writeToFile(outputImageFilename);
        outputImageLabeled.writeToFile(outputImageLabeledFilename);

        String outputDataFilename = this.getOutputFilename(this.currentDataFilename);
        this.createOutputDirectory(outputDataFilename);


        try {
            BufferedReader input = new BufferedReader(new FileReader(this.currentDataFilename));

            PrintWriter output = new PrintWriter(new FileWriter(outputDataFilename));

            String currentLine = input.readLine();

            while (currentLine != null) {

                String[] split = currentLine.split(" ");

                int regionNumber =(int)  Double.parseDouble(split[split.length - 2]);

                if (! this.selectedRegions.contains(regionNumber)) {
                    output.println(currentLine);
                }

                currentLine = input.readLine();
                
            }

            output.close();

        } catch (java.io.IOException e) {
            LoggingUtilities.warning("encountered IO exception while writing deschmutzed data");
        }


    }

    private void readCurrentMaskAndDataFilenames(String nextImageToProcess) {

        File nextToProcessFile = new File(nextImageToProcess);

       // System.out.println(nextImageToProcess);

        String parametersFilename = null;

        String parametersDir = nextToProcessFile.getParent() + File.separator + PARAMETERS_DIR;

        File parametersDirFile = new File(parametersDir);

        for (File f : parametersDirFile.listFiles()) {
            if (f.getName().matches(".*" + nextToProcessFile.getName() + ".*")) {
                parametersFilename = f.getAbsolutePath();
                break;
            }
        }

        if (parametersFilename == null) {
            this.currentParameters = null;
            this.currentParametersFilename = null;
            this.currentMaskFilename = null;
            this.currentLabeledMaskFilename = null;
            this.currentDataFilename = null;
            return;
        }

        ParameterDictionary p = ParameterDictionary.readParametersFromFile(parametersFilename);

        this.currentParameters = p;
        this.currentParametersFilename = parametersFilename;

        String maskFilename = nextToProcessFile.getParent() + File.separator + p.getValueForKey("mask_output_filename");

        this.currentMaskFilename = maskFilename;
        this.currentLabeledMaskFilename = maskFilename;
        if (p.hasKey("secondary_mask_output_filename")) {
            this.currentLabeledMaskFilename = nextToProcessFile.getParent() + File.separator + p.getValueForKey("secondary_mask_output_filename");
        }
        this.currentDataFilename = nextToProcessFile.getParent() + File.separator + p.getValueForKey("data_output_filename");

    }

    private String getOutputFilename(String inputFilename) {
        File inputFile = new File(inputFilename);

        String outputFilename = inputFile.getParent() + File.separator + OUTPUT_DIR + File.separator + inputFile.getName();
        return outputFilename;
    }

    private void processNextImage() {


        this.selectedRegions.clear();

        if (this.filenamesToProcess.isEmpty()) {this.finish(); return;}

        String nextToProcess = this.filenamesToProcess.remove(0);

        this.readCurrentMaskAndDataFilenames(nextToProcess);

        String maskFilename = this.currentMaskFilename;
        String labeledMaskFilename = this.currentLabeledMaskFilename;

        ImageReader ir = new ImageReader();

        Image currentImage = null;
        Image currentMask = null;
        Image currentLabeledMask = null;

        Image existingMask = null;

        try {

            currentImage = ir.read(nextToProcess);
            currentMask = ir.read(maskFilename);
            currentLabeledMask = ir.read(labeledMaskFilename);

            if (this.dw.useExistingMask()) {
                try {
                    existingMask = ir.read(this.getOutputFilename(maskFilename));
                } catch (Exception e) {
                    LoggingUtilities.warning("encountered exception while attempting to load deschmutzerized mask, continuing without");
                    existingMask = null;
                }
            }

        } catch (java.io.IOException e) {
            LoggingUtilities.warning("encountered exception while reading " + nextToProcess + ".  Continuing.");
            this.processNextImage();
            return;
        } catch (IllegalArgumentException e) {
            LoggingUtilities.warning("encountered exception while reading " + nextToProcess + ".  Continuing.");
            this.processNextImage();
            return;
        }


        this.currentOriginalImage = currentImage;
        this.currentMaskImage = currentMask;
        this.currentLabeledMaskImage = currentLabeledMask;

        
        int channel = 0;
        
        if(this.currentParameters != null && this.currentParameters.hasKey("marker_channel_index")) {
        	channel = this.currentParameters.getIntValueForKey("marker_channel_index");
        }
                
        ImageCoordinate start = ImageCoordinate.createCoordXYZCT(0,0,0,channel,0);
        ImageCoordinate dims = ImageCoordinate.cloneCoord(this.currentOriginalImage.getDimensionSizes());
        dims.set(ImageCoordinate.C, 1);
        

        this.currentOriginalImage = this.currentOriginalImage.subImage(dims, start);
        

        
        
        start.recycle();
        dims.recycle();

        


        this.colorCodedMaskDisplay = new BufferedImage(this.currentMaskImage.getDimensionSizes().getX(), this.currentMaskImage.getDimensionSizes().getY(), BufferedImage.TYPE_INT_ARGB);

        WritableRaster wr = this.colorCodedMaskDisplay.getRaster();
        WritableRaster ar = this.colorCodedMaskDisplay.getAlphaRaster();

        final int max_value = ImageDisplayPanel.MAX_RGB;

        for (int x =0; x < wr.getWidth(); x++)  {
            for (int y = 0; y < wr.getHeight(); y++) {

                wr.setSample(x, y, 0, max_value);
                wr.setSample(x, y, 1, 0);
                wr.setSample(x, y, 1, 0);
                ar.setSample(x, y, 0, 0);

            }
        }

        if (existingMask != null) {
            for (ImageCoordinate ic : existingMask) {
                if (this.currentLabeledMaskImage.getValue(ic) > 0 && existingMask.getValue(ic) == 0) {
                    ar.setSample(ic.getX(), ic.getY(), 0, max_value);
                    this.selectedRegions.add((int) this.currentLabeledMaskImage.getValue(ic));
                }
            }
        }



        this.maskWindow.setVisible(true);
        this.originalImageWindow.setVisible(true);

        
        
        this.originalImageDisplay.setImage(this.currentOriginalImage.toBufferedImage(), false);

        this.maskImageDisplay.setImage(this.currentMaskImage.toBufferedImage(), true);
        //this.maskImageDisplay.compositeWithImage(this.colorCodedMaskDisplay);


        int titleBarSize = (int) (this.maskWindow.getSize().getHeight() - this.maskWindow.getContentPane().getSize().getHeight());
        //System.out.println(titleBarSize);

        Dimension newSize = new Dimension((int) this.maskImageDisplay.getSize().getWidth(), (int) this.maskImageDisplay.getSize().getHeight() + titleBarSize);

        //System.out.println(newSize);

        this.maskWindow.setSize(newSize);
        this.originalImageWindow.setSize(newSize);

        if (this.dw.useExistingMask()) {
            this.maskImageDisplay.compositeWithImage(this.colorCodedMaskDisplay, 0, 0, this.colorCodedMaskDisplay.getWidth(), this.colorCodedMaskDisplay.getHeight());
        }


    }

    public void startButtonPressed() {
        
        Preferences.userNodeForPackage(this.getClass()).put("deschmutzerizer_filename", this.dw.getImageFilename());

        this.filenamesToProcess = new java.util.LinkedList<String>();

        File toProcess = new File(this.dw.getImageFilename());

        if (toProcess.isDirectory()) {
            for (File f : toProcess.listFiles()) {
                this.filenamesToProcess.add(f.getAbsolutePath());
            }
        } else {
            this.filenamesToProcess.add(toProcess.getAbsolutePath());
        }

        this.dw.setStartButtonEnabled(false);

        this.processNextImage();
        

    }

    public void browseButtonPressed() {
        String path = this.dw.getImageFilename();
        JFileChooser fc = null;
        if ((new File(path)).exists()) {
            fc = new JFileChooser(path);
        } else {
            fc = new JFileChooser();
        }
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        int retVal = fc.showOpenDialog(this.dw);

        if (retVal == JFileChooser.APPROVE_OPTION) {

            String selected = fc.getSelectedFile().getAbsolutePath();


            dw.setImageFilename(selected);
        }
    }

    public void continueButtonPressed() {
        this.finishCurrentImage();
        this.processNextImage();
    }

    public void selectAllButtonPressed() {

        this.processBox(new Point(0,0), new Point(this.currentOriginalImage.getDimensionSizes().getX(), this.currentOriginalImage.getDimensionSizes().getY()));
        
    }



}
