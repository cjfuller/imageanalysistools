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

package edu.stanford.cfuller.imageanalysistools.filter;

import java.awt.Rectangle;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Measurements;
import ij.plugin.ContrastEnhancer;
import ij.plugin.filter.PlugInFilter;
import ij.process.Blitter;
import ij.process.ColorProcessor;
import ij.process.FHT;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;

/**
 * A filter that retains only a section of frequency space from an Image's Fourier transform.
 *
 * <p>
 * This filter does not use a reference Image.
 * <p>
 * The argument to the apply method should be the Image to be bandpass filtered.
 * 
 * @author Colin J. Fuller
 */
public class BandpassFilter extends Filter {
	
	
	final static String PARAM_BAND_LOW = "bandpass_filter_low";
	final static String PARAM_BAND_HIGH = "bandpass_filter_high";

    double bandLow;
    double bandHigh;
    
    boolean shouldRescale;

    /**
     * Default constructor; creates a filter that is effectively a no-op until the setBand method is called, unless parameters
     * setting the low and high band limit were specified, which when the apply method is called will override any set or default
     * values.
     */
    public BandpassFilter() {
        bandLow = 0;
        bandHigh = 0;
        this.shouldRescale = false;
       
    }
    
    /**
     * Sets whether the image should be rescaled to its original range after bandpass filtering.
     * 
     * @param shouldRescale		A boolean specifying whether the image will be rescaled.
     */
    public void setShouldRescale(boolean shouldRescale) {
    	this.shouldRescale = shouldRescale;
    }


    /**
     * Applies the bandpass filter to an Image, removing the range of frequency space specified by the setBand method.
     * @param im    The Image to be bandpass filtered; it will be replaced by its filtered version.
     */
    public void apply(Image im) {
    	
    	im.clearBoxOfInterest(); //just in case
    	
        float oldMin = Float.MAX_VALUE;
        float oldMax = -1.0f*Float.MAX_VALUE;
        
        for (ImageCoordinate ic : im) {
        	if (im.getValue(ic) < oldMin) {
        		oldMin = im.getValue(ic);
        	}
        	
        	if (im.getValue(ic) > oldMax) {
        		oldMax = im.getValue(ic);
        	}
        }
        
        
    	
        if (this.params.hasKey(PARAM_BAND_LOW)) {
        	this.bandLow = this.params.getDoubleValueForKey(PARAM_BAND_LOW);
        }
        
        if (this.params.hasKey(PARAM_BAND_HIGH)) {
        	this.bandHigh = this.params.getDoubleValueForKey(PARAM_BAND_HIGH);
        }
        
        ImagePlus imp = im.toImagePlus();
        
        IJFFTFilter ijf = new IJFFTFilter();
        
        IJFFTFilter.setFilterLargeDia(this.bandHigh);
        IJFFTFilter.setFilterSmallDia(this.bandLow);
        
        ijf.setup("", imp);
        
        for (int i = 0; i < imp.getStackSize(); i++) {
        	
        	imp.setSliceWithoutUpdate(i+1);
        	
        	ImageProcessor proc = imp.getProcessor();
        	
        	ijf.run(proc);
        	
        }
                
       // imp.show();
        
        im.copy(new Image(imp));
        
        float newMin = Float.MAX_VALUE;
        float newMax = -1.0f*Float.MAX_VALUE;
        
        for (ImageCoordinate ic : im) {
        	if (im.getValue(ic) < newMin) {
        		newMin = im.getValue(ic);
        	}
        	
        	if (im.getValue(ic) > newMax) {
        		newMax = im.getValue(ic);
        	}
        }
        
        
        float oldRange = oldMax-oldMin;
        float newRange = newMax-newMin;
                        
        if (this.shouldRescale) {
        
	        for (ImageCoordinate ic : im) {
	        	im.setValue(ic, (im.getValue(ic) - newMin)/newRange*oldRange + oldMin);
	        }
	       
        }
	        

//        FastFourierTransformer fft = new org.apache.commons.math3.transform.FastFourierTransformer();
//
//        int ydimPowOfTwo = im.getDimensionSizes().get(ImageCoordinate.Y);
//        int xdimPowOfTwo = im.getDimensionSizes().get(ImageCoordinate.X);
//
//        if (!FastFourierTransformer.isPowerOf2(ydimPowOfTwo) || !FastFourierTransformer.isPowerOf2(xdimPowOfTwo)) {
//
//            xdimPowOfTwo = (int) Math.pow(2, Math.ceil(Math.log(im.getDimensionSizes().get(ImageCoordinate.X)) / Math.log(2)));
//            ydimPowOfTwo = (int) Math.pow(2, Math.ceil(Math.log(im.getDimensionSizes().get(ImageCoordinate.Y))/Math.log(2)));
//        }
//
//        for (int p =0; p < im.getPlaneCount(); p++) {
//
//            im.selectPlane(p);
//
//            double[][] rowImage = new double[ydimPowOfTwo][xdimPowOfTwo];
//            for (int i =0; i < ydimPowOfTwo; i++) {
//                java.util.Arrays.fill(rowImage[i], 0); // ensures zero-padding
//            }
//            Complex[][] colMajorImage = new Complex[xdimPowOfTwo][ydimPowOfTwo];
//
//            for (ImageCoordinate ic : im) {
//                rowImage[ic.get(ImageCoordinate.Y)][ic.get(ImageCoordinate.X)] = im.getValue(ic);
//            }
//
//            for (int r = 0; r < rowImage.length; r++) {
//                double[] row = rowImage[r];
//                Complex[] transformedRow = fft.transform(row);
//
//                for (int c = 0; c < colMajorImage.length; c++) {
//                    colMajorImage[c][r] = transformedRow[c];
//                }
//            }
//
//            for (int c = 0; c < colMajorImage.length; c++) {
//                colMajorImage[c] = fft.transform(colMajorImage[c]);
//            }
//
//            int NFx = xdimPowOfTwo/2 + 1;
//            int NFy = ydimPowOfTwo/2 + 1;
//
//            double cutoffXUpper = NFx*this.bandHigh;
//            double cutoffXLower = NFx*this.bandLow;
//
//            double cutoffYUpper = NFy* this.bandHigh;
//            double cutoffYLower = NFy* this.bandLow;
//
//            //zero the frequency components
//
//            for (int c = 0; c < NFx; c++) {
//                for (int r = 0; r < NFy; r++) {
//
//                    int cOpp = colMajorImage.length - c;
//
//                    int rOpp = colMajorImage[0].length - r;
//
//                    if (c < cutoffXUpper && c > cutoffXLower) {
//
//                        colMajorImage[c][r] = new Complex(0,0);
//
//                        if (c > 0) {
//                            colMajorImage[cOpp][r] = new Complex(0,0);
//                            if (r > 0) {
//                                colMajorImage[cOpp][rOpp] = new Complex(0,0);
//                            }
//                        }
//
//                    } else if (r < cutoffYUpper && r > cutoffYLower) {
//                        colMajorImage[c][r] = new Complex(0,0);
//
//                        if (r > 0) {
//                            colMajorImage[c][rOpp] = new Complex(0,0);
//                            if (c > 0) {
//                                colMajorImage[cOpp][rOpp] = new Complex(0,0);
//                            }
//                        }
//                    }
//
//
//                }
//
//            }
//
//            //inverse transform
//
//            for (int c = 0; c < colMajorImage.length; c++) {
//                colMajorImage[c] = fft.inversetransform(colMajorImage[c]);
//            }
//
//            Complex[] tempRow = new Complex[rowImage.length];
//
//            //also calculate min/max values
//            double newMin = Double.MAX_VALUE;
//            double newMax = 0;
//
//            for (int r = 0; r < rowImage.length; r++) {
//
//                for (int c = 0; c < colMajorImage.length; c++) {
//                    tempRow[c] = colMajorImage[c][r];
//                }
//
//                Complex[] transformedRow = fft.inversetransform(tempRow);
//
//                for (int c = 0; c < colMajorImage.length; c++) {
//                    rowImage[r][c] = transformedRow[c].abs();
//                    if (rowImage[r][c] < newMin) newMin = rowImage[r][c];
//                    if (rowImage[r][c] > newMax) newMax = rowImage[r][c];
//                }
//            }
//
//            //rescale values to same min/max as before
//
//            Histogram h = new Histogram(im);
//
//            double oldMin = h.getMinValue();
//            double oldMax = h.getMaxValue();
//
//            double scaleFactor = (oldMax - oldMin)/(newMax - newMin);
//
//
//            for (ImageCoordinate ic : im) {
//                im.setValue(ic, (float) ((rowImage[ic.get(ImageCoordinate.Y)][ic.get(ImageCoordinate.X)] - newMin)*scaleFactor + oldMin));
//            }
//
//
//        }
//
//        im.clearBoxOfInterest();


    }


    /**
     * Sets the band in frequency space to be retained in the Fourier transformed Image.
     * <p>
     * The two arguments specify the lower and upper bounds of the range to be retained, in terms of size in pixels, rather than frequency.
     * So for the low parameter, specify the lower size (and therefore the higher frequency); likewise, specify the larger size (and smaller frequency) for
     * the high parameter.
     *
     *
     * @param low   The lower bound of the sizes in pixels to be filtered.
     * @param high  The upper bound of the sizes in pixels to be filtered.
     */
    public void setBand(double low, double high) {
        this.bandLow = low;
        this.bandHigh = high;
    }

    
    /*
     * This has been taken directly from the ImageJ class FFTFilter and modified for
     * non-interactive use of the filter.
     */
    protected static class IJFFTFilter implements  PlugInFilter, Measurements {

    	private ImagePlus imp;
    	private FHT fht;
    	private int slice;
    	private int stackSize = 1;	
    	
    	private static double filterLargeDia = 40.0;
    	private static double  filterSmallDia = 3.0;
    	private static int choiceIndex = 0;
    	private static double toleranceDia = 5.0;
    	private static boolean doScalingDia = false;
    	private static boolean saturateDia = false;
    	private static boolean displayFilter = false;
    	private static boolean processStack = false;
    	
    	public static void setFilterLargeDia(double large) {
    		filterLargeDia = large;
    	}
    	
    	public static void setFilterSmallDia(double small) {
    		filterSmallDia = small;
    	}
    	
    	//0 = none, 1 = horizontal, 2 = vertical
    	public static void setChoice(int choice) {
    		choiceIndex = choice;
    	}
    	
    	public static void setTolerance(double tol) {
    		toleranceDia = tol;
    	}
    	
    	public static void setSaturate(boolean sat) {
    		saturateDia = sat;
    	}
    	
    	public static void setDisplay(boolean display) {
    		displayFilter = display;
    	}
    	
    	public static void setProcessStack(boolean process) {
    		processStack = process;
    	}

    	public int setup(String arg, ImagePlus imp) {
     		this.imp = imp;
     		if (imp==null)
     			{IJ.noImage(); return DONE;}
     		stackSize = imp.getStackSize();
    		fht  = (FHT)imp.getProperty("FHT");
    		if (fht!=null) {
    			IJ.error("FFT Filter", "Spatial domain image required");
    			return DONE;
    		}
    		if (!showBandpassDialog(imp))
    			return DONE;
    		else
    			return processStack?DOES_ALL+DOES_STACKS+PARALLELIZE_STACKS:DOES_ALL;
    	}

    	public void run(ImageProcessor ip) {
    		slice++;
    		filter(ip);
    	}
    	
    	void filter(ImageProcessor ip) {
    		ImageProcessor ip2 = ip;
    		if (ip2 instanceof ColorProcessor) {
    			showStatus("Extracting brightness");
    			ip2 = ((ColorProcessor)ip2).getBrightness();
    		} 
    		Rectangle roiRect = ip2.getRoi();		
    		int maxN = Math.max(roiRect.width, roiRect.height);
    		double sharpness = (100.0 - toleranceDia) / 100.0;
    		boolean doScaling = doScalingDia;
    		boolean saturate = saturateDia;
    		
    		IJ.showProgress(1,20);

    		/* 	tile mirrored image to power of 2 size		
    			first determine smallest power 2 >= 1.5 * image width/height
    		  	factor of 1.5 to avoid wrap-around effects of Fourier Trafo */

    		int i=2;
    		while(i<1.5 * maxN) i *= 2;		
            
            // Calculate the inverse of the 1/e frequencies for large and small structures.
            double filterLarge = 2.0*filterLargeDia / (double)i;
            double filterSmall = 2.0*filterSmallDia / (double)i;
            
    		// fit image into power of 2 size 
    		Rectangle fitRect = new Rectangle();
    		fitRect.x = (int) Math.round( (i - roiRect.width) / 2.0 );
    		fitRect.y = (int) Math.round( (i - roiRect.height) / 2.0 );
    		fitRect.width = roiRect.width;
    		fitRect.height = roiRect.height;
    		
    		// put image (ROI) into power 2 size image
    		// mirroring to avoid wrap around effects
    		showStatus("Pad to "+i+"x"+i);
    		ip2 = tileMirror(ip2, i, i, fitRect.x, fitRect.y);
    		IJ.showProgress(2,20);
    		
    		// transform forward
    		showStatus(i+"x"+i+" forward transform");
    		FHT fht = new FHT(ip2);
    		fht.setShowProgress(false);
    		fht.transform();
    		IJ.showProgress(9,20);
    		//new ImagePlus("after fht",ip2.crop()).show();	

    		// filter out large and small structures
    		showStatus("Filter in frequency domain");
    		filterLargeSmall(fht, filterLarge, filterSmall, choiceIndex, sharpness);
    		//new ImagePlus("filter",ip2.crop()).show();
    		IJ.showProgress(11,20);

    		// transform backward
    		showStatus("Inverse transform");
    		fht.inverseTransform();
    		IJ.showProgress(19,20);
    		//new ImagePlus("after inverse",ip2).show();	
    		
    		// crop to original size and do scaling if selected
    		showStatus("Crop and convert to original type");
    		fht.setRoi(fitRect);
    		ip2 = fht.crop();
    		if (doScaling) {
    			ImagePlus imp2 = new ImagePlus(imp.getTitle()+"-filtered", ip2);
    			new ContrastEnhancer().stretchHistogram(imp2, saturate?1.0:0.0);
    			ip2 = imp2.getProcessor();
    		}

    		// convert back to original data type
    		int bitDepth = imp.getBitDepth(); 
    		switch (bitDepth) {
    			case 8: ip2 = ip2.convertToByte(doScaling); break;
    			case 16: ip2 = ip2.convertToShort(doScaling); break;
    			case 24:
    				ip.snapshot();
    				showStatus("Setting brightness");
    				((ColorProcessor)ip).setBrightness((FloatProcessor)ip2);
    				break;
    			case 32: break;
    		}

    		// copy filtered image back into original image
    		if (bitDepth!=24) {
    			ip.snapshot();
    			ip.copyBits(ip2, roiRect.x, roiRect.y, Blitter.COPY);
    		}
    		ip.resetMinAndMax();
    		IJ.showProgress(20,20);
    	}
    	
    	void showStatus(String msg) {
    		if (stackSize>1 && processStack)
    			IJ.showStatus("FFT Filter: "+slice+"/"+stackSize);
    		else
    			IJ.showStatus(msg);
    	}

    	/** Puts ImageProcessor (ROI) into a new ImageProcessor of size width x height y at position (x,y).
    	The image is mirrored around its edges to avoid wrap around effects of the FFT. */
    	public ImageProcessor tileMirror(ImageProcessor ip, int width, int height, int x, int y) {
    		if (IJ.debugMode) IJ.log("FFT.tileMirror: "+width+"x"+height+" "+ip);
    		if (x < 0 || x > (width -1) || y < 0 || y > (height -1)) {
    			IJ.error("Image to be tiled is out of bounds.");
    			return null;
    		}
    		
    		ImageProcessor ipout = ip.createProcessor(width, height);
    		
    		ImageProcessor ip2 = ip.crop();
    		int w2 = ip2.getWidth();
    		int h2 = ip2.getHeight();
    				
    		//how many times does ip2 fit into ipout?
    		int i1 = (int) Math.ceil(x / (double) w2);
    		int i2 = (int) Math.ceil( (width - x) / (double) w2);
    		int j1 = (int) Math.ceil(y / (double) h2);
    		int j2 = (int) Math.ceil( (height - y) / (double) h2);		

    		//tile		
    		if ( (i1%2) > 0.5)
    			ip2.flipHorizontal();
    		if ( (j1%2) > 0.5)
    			ip2.flipVertical();
    					
    		for (int i=-i1; i<i2; i += 2) {
    			for (int j=-j1; j<j2; j += 2) {
    				ipout.insert(ip2, x-i*w2, y-j*h2);
    			}
    		}
    		
    		ip2.flipHorizontal();
    		for (int i=-i1+1; i<i2; i += 2) {
    			for (int j=-j1; j<j2; j += 2) {
    				ipout.insert(ip2, x-i*w2, y-j*h2);
    			}
    		}
    		
    		ip2.flipVertical();
    		for (int i=-i1+1; i<i2; i += 2) {
    			for (int j=-j1+1; j<j2; j += 2) {
    				ipout.insert(ip2, x-i*w2, y-j*h2);
    			}
    		}
    		
    		ip2.flipHorizontal();
    		for (int i=-i1; i<i2; i += 2) {
    			for (int j=-j1+1; j<j2; j += 2) {
    				ipout.insert(ip2, x-i*w2, y-j*h2);
    			}
    		}
    		
    		return ipout;
    	}		
    	

    	/*
    	filterLarge: down to which size are large structures suppressed?
    	filterSmall: up to which size are small structures suppressed?
    	filterLarge and filterSmall are given as fraction of the image size 
    				in the original (untransformed) image.
    	stripesHorVert: filter out: 0) nothing more  1) horizontal  2) vertical stripes
    				(i.e. frequencies with x=0 / y=0)
    	scaleStripes: width of the stripe filter, same unit as filterLarge
    	*/
    	void filterLargeSmall(ImageProcessor ip, double filterLarge, double filterSmall, int stripesHorVert, double scaleStripes) {
    		
    		int maxN = ip.getWidth();
    			
    		float[] fht = (float[])ip.getPixels();
    		float[] filter = new float[maxN*maxN];
    		for (int i=0; i<maxN*maxN; i++)
    			filter[i]=1f;		

    		int row;
    		int backrow;
    		float rowFactLarge;
    		float rowFactSmall;
    		
    		int col;
    		int backcol;
    		float factor;
    		float colFactLarge;
    		float colFactSmall;
    		
    		float factStripes;
    		
    		// calculate factor in exponent of Gaussian from filterLarge / filterSmall

    		double scaleLarge = filterLarge*filterLarge;
    		double scaleSmall = filterSmall*filterSmall;
    		scaleStripes = scaleStripes*scaleStripes;
    		//float FactStripes;

    		// loop over rows
    		for (int j=1; j<maxN/2; j++) {
    			row = j * maxN;
    			backrow = (maxN-j)*maxN;
    			rowFactLarge = (float) Math.exp(-(j*j) * scaleLarge);
    			rowFactSmall = (float) Math.exp(-(j*j) * scaleSmall);
    			

    			// loop over columns
    			for (col=1; col<maxN/2; col++){
    				backcol = maxN-col;
    				colFactLarge = (float) Math.exp(- (col*col) * scaleLarge);
    				colFactSmall = (float) Math.exp(- (col*col) * scaleSmall);
    				factor = (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall;
    				switch (stripesHorVert) {
    					case 1: factor *= (1 - (float) Math.exp(- (col*col) * scaleStripes)); break;// hor stripes
    					case 2: factor *= (1 - (float) Math.exp(- (j*j) * scaleStripes)); // vert stripes
    				}
    				
    				fht[col+row] *= factor;
    				fht[col+backrow] *= factor;
    				fht[backcol+row] *= factor;
    				fht[backcol+backrow] *= factor;
    				filter[col+row] *= factor;
    				filter[col+backrow] *= factor;
    				filter[backcol+row] *= factor;
    				filter[backcol+backrow] *= factor;
    			}
    		}

    		//process meeting points (maxN/2,0) , (0,maxN/2), and (maxN/2,maxN/2)
    		int rowmid = maxN * (maxN/2);
    		rowFactLarge = (float) Math.exp(- (maxN/2)*(maxN/2) * scaleLarge);
    		rowFactSmall = (float) Math.exp(- (maxN/2)*(maxN/2) * scaleSmall);	
    		factStripes = (float) Math.exp(- (maxN/2)*(maxN/2) * scaleStripes);
    		
    		fht[maxN/2] *= (1 - rowFactLarge) * rowFactSmall; // (maxN/2,0)
    		fht[rowmid] *= (1 - rowFactLarge) * rowFactSmall; // (0,maxN/2)
    		fht[maxN/2 + rowmid] *= (1 - rowFactLarge*rowFactLarge) * rowFactSmall*rowFactSmall; // (maxN/2,maxN/2)
    		filter[maxN/2] *= (1 - rowFactLarge) * rowFactSmall; // (maxN/2,0)
    		filter[rowmid] *= (1 - rowFactLarge) * rowFactSmall; // (0,maxN/2)
    		filter[maxN/2 + rowmid] *= (1 - rowFactLarge*rowFactLarge) * rowFactSmall*rowFactSmall; // (maxN/2,maxN/2)

    		switch (stripesHorVert) {
    			case 1: fht[maxN/2] *= (1 - factStripes);
    					fht[rowmid] = 0;
    					fht[maxN/2 + rowmid] *= (1 - factStripes);
    					filter[maxN/2] *= (1 - factStripes);
    					filter[rowmid] = 0;
    					filter[maxN/2 + rowmid] *= (1 - factStripes);
    					break; // hor stripes
    			case 2: fht[maxN/2] = 0;
    					fht[rowmid] *=  (1 - factStripes);
    					fht[maxN/2 + rowmid] *= (1 - factStripes);
    					filter[maxN/2] = 0;
    					filter[rowmid] *=  (1 - factStripes);
    					filter[maxN/2 + rowmid] *= (1 - factStripes);
    					break; // vert stripes
    		}		
    		
    		//loop along row 0 and maxN/2	
    		rowFactLarge = (float) Math.exp(- (maxN/2)*(maxN/2) * scaleLarge);
    		rowFactSmall = (float) Math.exp(- (maxN/2)*(maxN/2) * scaleSmall);			
    		for (col=1; col<maxN/2; col++){
    			backcol = maxN-col;
    			colFactLarge = (float) Math.exp(- (col*col) * scaleLarge);
    			colFactSmall = (float) Math.exp(- (col*col) * scaleSmall);
    			
    			switch (stripesHorVert) {
    				case 0:
    					fht[col] *= (1 - colFactLarge) * colFactSmall;
    					fht[backcol] *= (1 - colFactLarge) * colFactSmall;
    					fht[col+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall;
    					fht[backcol+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall;
    					filter[col] *= (1 - colFactLarge) * colFactSmall;
    					filter[backcol] *= (1 - colFactLarge) * colFactSmall;
    					filter[col+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall;
    					filter[backcol+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall;	
    					break;			
    				case 1:
    					factStripes = (float) Math.exp(- (col*col) * scaleStripes);
    					fht[col] *= (1 - colFactLarge) * colFactSmall * (1 - factStripes);
    					fht[backcol] *= (1 - colFactLarge) * colFactSmall * (1 - factStripes);
    					fht[col+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall * (1 - factStripes);
    					fht[backcol+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall * (1 - factStripes);
    					filter[col] *= (1 - colFactLarge) * colFactSmall * (1 - factStripes);
    					filter[backcol] *= (1 - colFactLarge) * colFactSmall * (1 - factStripes);
    					filter[col+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall * (1 - factStripes);
    					filter[backcol+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall * (1 - factStripes);
    					break;
    				case 2:
    					factStripes = (float) Math.exp(- (maxN/2)*(maxN/2) * scaleStripes); 
    					fht[col] = 0;
    					fht[backcol] = 0;
    					fht[col+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall * (1 - factStripes);
    					fht[backcol+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall * (1 - factStripes);
    					filter[col] = 0;
    					filter[backcol] = 0;
    					filter[col+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall * (1 - factStripes);
    					filter[backcol+rowmid] *= (1 - colFactLarge*rowFactLarge) * colFactSmall*rowFactSmall * (1 - factStripes);
    			}
    		}
    		
    		// loop along column 0 and maxN/2
    		colFactLarge = (float) Math.exp(- (maxN/2)*(maxN/2) * scaleLarge);
    		colFactSmall = (float) Math.exp(- (maxN/2)*(maxN/2) * scaleSmall);
    		for (int j=1; j<maxN/2; j++) {
    			row = j * maxN;
    			backrow = (maxN-j)*maxN;
    			rowFactLarge = (float) Math.exp(- (j*j) * scaleLarge);
    			rowFactSmall = (float) Math.exp(- (j*j) * scaleSmall);

    			switch (stripesHorVert) {
    				case 0:
    					fht[row] *= (1 - rowFactLarge) * rowFactSmall;
    					fht[backrow] *= (1 - rowFactLarge) * rowFactSmall;
    					fht[row+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall;
    					fht[backrow+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall;
    					filter[row] *= (1 - rowFactLarge) * rowFactSmall;
    					filter[backrow] *= (1 - rowFactLarge) * rowFactSmall;
    					filter[row+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall;
    					filter[backrow+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall;
    					break;
    				case 1:
    					factStripes = (float) Math.exp(- (maxN/2)*(maxN/2) * scaleStripes);
    					fht[row] = 0;
    					fht[backrow] = 0;
    					fht[row+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall * (1 - factStripes);
    					fht[backrow+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall * (1 - factStripes);
    					filter[row] = 0;
    					filter[backrow] = 0;
    					filter[row+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall * (1 - factStripes);
    					filter[backrow+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall * (1 - factStripes);
    					break;
    				case 2:
    					factStripes = (float) Math.exp(- (j*j) * scaleStripes);
    					fht[row] *= (1 - rowFactLarge) * rowFactSmall * (1 - factStripes);
    					fht[backrow] *= (1 - rowFactLarge) * rowFactSmall * (1 - factStripes);
    					fht[row+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall * (1 - factStripes);
    					fht[backrow+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall * (1 - factStripes);
    					filter[row] *= (1 - rowFactLarge) * rowFactSmall * (1 - factStripes);
    					filter[backrow] *= (1 - rowFactLarge) * rowFactSmall * (1 - factStripes);
    					filter[row+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall * (1 - factStripes);
    					filter[backrow+maxN/2] *= (1 - rowFactLarge*colFactLarge) * rowFactSmall*colFactSmall * (1 - factStripes);	
    			}
    		}
    		if (displayFilter && slice==1) {
    			FHT f = new FHT(new FloatProcessor(maxN, maxN, filter, null));
    			f.swapQuadrants();
    			new ImagePlus("Filter", f).show();
    		}
    	}	

    	boolean showBandpassDialog(ImagePlus imp) {
    		return true;
    	}

    }


}
