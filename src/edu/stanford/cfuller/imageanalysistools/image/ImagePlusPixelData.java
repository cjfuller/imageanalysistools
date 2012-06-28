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

package edu.stanford.cfuller.imageanalysistools.image;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;


/**
 * A type of PixelData that uses an ImageJ ImagePlus as its underlying representation.
 * 
 * @author Colin J. Fuller
 *
 */
public class ImagePlusPixelData extends PixelData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5430441630713231848L;

	private ImagePlus imPl;
	
	private static final String imagePlusDimensionOrder = "XYCZT";
	
	int currentStackIndex;
	
	protected void init(int size_x, int size_y, int size_z, int size_c, int size_t, String dimensionOrder) {
		dimensionOrder = dimensionOrder.toUpperCase();
		this.pixels = null;
		
		this.dimensionSizes = new java.util.Hashtable<String, Integer>();
		
		this.size_x = size_x;
		this.size_y = size_y;
		this.size_z = size_z;
		this.size_c = size_c;
		this.size_t = size_t;
		
		dimensionSizes.put("X", size_x);
		dimensionSizes.put("Y", size_y);
		dimensionSizes.put("Z", size_z);
		dimensionSizes.put("C", size_c);
		dimensionSizes.put("T", size_t);
		
		offsetSizes = new java.util.Hashtable<String, Integer>();
		
		offsetSizes.put(dimensionOrder.substring(0,1), 1);
		
		for (int c = 1; c < dimensionOrder.length(); c++) {
			String curr = dimensionOrder.substring(c, c+1);
			String last = dimensionOrder.substring(c-1,c);


			offsetSizes.put(curr, dimensionSizes.get(last)*offsetSizes.get(last));
		}
		
		x_offset = offsetSizes.get("X");
		y_offset = offsetSizes.get("Y");
		z_offset = offsetSizes.get("Z");
		c_offset = offsetSizes.get("C");
		t_offset = offsetSizes.get("T");
				
		this.dimensionOrder = dimensionOrder;
		
		this.byteOrder = java.nio.ByteOrder.BIG_ENDIAN;
		
		this.currentStackIndex = 0;
		
	}
	
	protected void initNewImagePlus() {
		int n_planes = this.size_z*this.size_c*this.size_t;
    	
    	int width = this.size_x;
    	
    	int height = this.size_y;
    	
    	ImageStack stack = new ImageStack(width, height);
    	    	    	
    	for (int i = 0; i < n_planes; i++) {
    		
    		FloatProcessor fp = new FloatProcessor(width, height, new float[width*height], null);
    		
    		if (i == 0) {stack.update(fp);}
    		    		
    		stack.addSlice("", fp);
    		
    	}
    	    	
    	ImagePlus imPl = new ImagePlus("output", stack);
    	
    	imPl.setDimensions(this.size_c, this.size_z, this.size_t);
    	
    	this.imPl = imPl;
	}
	
	protected ImagePlusPixelData() {}
	
	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#PixelData(edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate, int, String)
	 */
	public ImagePlusPixelData(ImageCoordinate sizes, int data_type, String dimensionOrder) {
		super(sizes, data_type, dimensionOrder);
		this.initNewImagePlus();
	}
	
	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#PixelData(int, int, int, int, int, int, String)
	 */
	public ImagePlusPixelData(int size_x, int size_y, int size_z, int size_c, int size_t, int data_type, String dimensionOrder) {

		super(size_x, size_y, size_z, size_c, size_t, data_type, dimensionOrder);
		this.initNewImagePlus();
		
	}
	
	/**
	 * Creates a new ImagePlusPixelData from an existing ImagePlus.
	 * 
	 * @param imPl	The ImagePlus to use.  This will not be copied, but used and potentially modified in place.
	 */
	public ImagePlusPixelData(ImagePlus imPl) {
		
		this.imPl = imPl;
		
		//ImageConverter ic = new ImageConverter(this.imPl);
		
		//ic.convertToGray32();
		
		this.init(imPl.getWidth(), imPl.getHeight(), imPl.getNSlices(), imPl.getNChannels(), imPl.getNFrames(), imagePlusDimensionOrder);
		
		this.dataType = loci.formats.FormatTools.FLOAT;
		
	}

	/**
	* @deprecated makes it difficult not to duplicate storage in memory; use {@link #setPlane(int, int, int, byte[])} instead.
	* 
	* Sets up the PixelData object based on a byte array and the format information.
	* <p>
	* Don't call this twice to replace the underlying bytes.  After reading the byte array, this will rearrange the ordering of the data, so 
	* this will be garbled on a second call.
	* 
	* @param bytes		The byte array representation of the pixels.
	*/
	@Deprecated
	public void setBytes(byte[] bytes) {
	
		this.convertedPixels = new float[this.size_x*this.size_y*this.size_z*this.size_c*this.size_t];
		this.pixels = bytes;
		super.updateConvertedPixelsFromBytes();
		
		this.pixels = null;
		
		for (int t = 0; t < size_t; t++) {
			for (int c = 0; c < size_c; c++) {
				for(int z = 0; z < size_z; z++) {
					for (int y = 0; y < size_y; y++) {
						for (int x = 0; x < size_x; x++) {
							
							
							int slice = this.imPl.getStackIndex(c+1, z+1, t+1); //planes are 1-indexed in ImagePlus
														
							if (slice != this.currentStackIndex) {
								this.imPl.setSliceWithoutUpdate(slice);
								this.currentStackIndex = slice;
							}
							
							this.imPl.getProcessor().setf(x,y,super.getPixel(x,y,z,c,t));
							
						}
					}
				}
			}
		}

		
		this.convertedPixels = null;
		this.offsetSizes = null;
		this.x_offset = -1;
		this.y_offset = -1;
		this.z_offset = -1;
		this.c_offset = -1;
		this.t_offset = -1;
		this.dimensionOrder = "XYCZT";
		this.dataType=loci.formats.FormatTools.FLOAT;
		
	}
	
	/**
    * Sets the raw byte representation of one plane of the pixel data to the specified array.
    *<p>
    * Pixel values should be represented by the numeric type, byte order, and dimension order specified when initializing the PixelData.
    * This will not be checked for the correct format.
    *<p>
    * The internal numerical representation of the pixel data will be updated immediately.
    * 
    * @param zIndex	  the z-dimension index of the plane being set (0-indexed)
    * @param cIndex	  the c-dimension index of the plane being set (0-indexed)
    * @param tIndex	  the t-dimension index of the plane being set (0-indexed)
    * @param plane    A byte array containing the new pixel data for the specified plane.
    */
	@Override
	public void setPlane(int zIndex, int cIndex, int tIndex, byte[] plane) {
		
		if (!(this.dimensionOrder.startsWith("XY") || this.dimensionOrder.startsWith("YX"))) {
            throw new UnsupportedOperationException("Setting a single plane as a byte array is not supported for images whose dimension order does not start with XY or YX."); 
        }

		java.nio.ByteBuffer in = java.nio.ByteBuffer.wrap(plane);
				
		in.order(this.byteOrder);
		
		
		int requestedStackIndex = this.imPl.getStackIndex(cIndex+1, zIndex+1, tIndex+1); //planes are 1-indexed in ImagePlus
		if (requestedStackIndex != this.currentStackIndex) {
			this.imPl.setSliceWithoutUpdate(requestedStackIndex);
			this.currentStackIndex = requestedStackIndex;
		}
		
		ImageProcessor imp = this.imPl.getProcessor();
		

						
		if (this.dataType == loci.formats.FormatTools.INT8) {
			
			java.nio.ByteBuffer convBuffer = in;
			
			for (int y = 0; y < this.size_y; y++) {
				for (int x = 0; x < this.size_x; x++) {

					int offset = x*x_offset + y*y_offset;

					//this.setPixel(x, y, zIndex, cIndex, tIndex, (float) (convBuffer.get(offset)));
					imp.setf(x,y,(float)(convBuffer.get(offset)));
				}
			}
			
        } else if (this.dataType == loci.formats.FormatTools.UINT8) {

			java.nio.ByteBuffer convBuffer = in;

			for (int y = 0; y < this.size_y; y++) {
				for (int x = 0; x < this.size_x; x++) {

					int offset = x*x_offset + y*y_offset;

					imp.setf(x,y, (float) (convBuffer.get(offset)));

				}
			}			
			
		} else if (this.dataType == loci.formats.FormatTools.INT16) {
		
			java.nio.ShortBuffer convBuffer = in.asShortBuffer();
			
			for (int y = 0; y < this.size_y; y++) {
				for (int x = 0; x < this.size_x; x++) {

					int offset = x*x_offset + y*y_offset;

					imp.setf(x,y, (float) (convBuffer.get(offset)));

				}
			}


        } else if (this.dataType == loci.formats.FormatTools.UINT16) {

			java.nio.ShortBuffer convBuffer = in.asShortBuffer();

			for (int y = 0; y < this.size_y; y++) {
				for (int x = 0; x < this.size_x; x++) {

					int offset = x*x_offset + y*y_offset;

					imp.setf(x,y, (float) (convBuffer.get(offset)));

				}
			}

		} else if (this.dataType == loci.formats.FormatTools.INT32) {
			
			java.nio.IntBuffer convBuffer = in.asIntBuffer();
			
			for (int y = 0; y < this.size_y; y++) {
				for (int x = 0; x < this.size_x; x++) {

					int offset = x*x_offset + y*y_offset;

					imp.setf(x,y, (float) (convBuffer.get(offset)));

				}
			}

        } else if (this.dataType == loci.formats.FormatTools.UINT32) {

			java.nio.IntBuffer convBuffer = in.asIntBuffer();

            for (int y = 0; y < this.size_y; y++) {
				for (int x = 0; x < this.size_x; x++) {

					int offset = x*x_offset + y*y_offset;

					imp.setf(x,y, (float) (convBuffer.get(offset)));

				}
			}

			
		} else if (this.dataType == loci.formats.FormatTools.FLOAT) {
			
			java.nio.FloatBuffer convBuffer = in.asFloatBuffer();
			
			for (int y = 0; y < this.size_y; y++) {
				for (int x = 0; x < this.size_x; x++) {

					int offset = x*x_offset + y*y_offset;

					imp.setf(x,y, (float) (convBuffer.get(offset)));

				}
			}
			
			
		} else if (this.dataType == loci.formats.FormatTools.DOUBLE) {
		
			java.nio.DoubleBuffer convBuffer = in.asDoubleBuffer();
		
			
			for (int y = 0; y < this.size_y; y++) {
				for (int x = 0; x < this.size_x; x++) {

					int offset = x*x_offset + y*y_offset;

					imp.setf(x,y, (float) (convBuffer.get(offset)));

				}
			}

        } else {
            
			java.nio.ByteBuffer convBuffer = in;


            for (int y = 0; y < this.size_y; y++) {
				for (int x = 0; x < this.size_x; x++) {

					int offset = x*x_offset + y*y_offset;

					imp.setf(x,y, (float) (convBuffer.get(offset)));

				}
			}
			
		}

		
		
	}
	
	public void getBytes(byte[] bytes) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Getting values by byte array not supported from ImagePlusPixelData.");
	}
	
	public byte[] getPlane(int index) throws UnsupportedOperationException {

		//convert the index to xyczt ordering just in case the ImagePlus representation changes under the hood.
		
		int c_index = index % size_c;
		
		int z_index = ((index - c_index)/size_c) % size_z;
		
		int t_index = (index - c_index - size_c*z_index)/(size_z*size_c);
				
		index = this.imPl.getStackIndex(c_index+1, z_index+1, t_index+1);
		
		
		if (index != this.currentStackIndex) {
			this.imPl.setSliceWithoutUpdate(index);
			this.currentStackIndex = index;
		}
		
		
		float[] pixelData = (float[]) this.imPl.getProcessor().getPixels();
		
		java.nio.ByteBuffer out = java.nio.ByteBuffer.allocate(this.size_x*this.size_y*loci.formats.FormatTools.getBytesPerPixel(this.dataType));

		out.order(this.byteOrder);
		
		for (float f: pixelData) {
			out.putFloat(f);
		}
		
		return out.array();
	}
	
	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#getPixel(int, int, int, int, int)
	 */
	public float getPixel(int x, int y, int z, int c, int t) {
		int requestedStackIndex = this.imPl.getStackIndex(c+1, z+1, t+1); //planes are 1-indexed in ImagePlus
		if (requestedStackIndex != this.currentStackIndex) {
			this.imPl.setSliceWithoutUpdate(requestedStackIndex);
			this.currentStackIndex = requestedStackIndex;
		}

		return imPl.getProcessor().getf(x,y);

	}
	
	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#setPixel(int, int, int, int, int, float)
	 */
	public void setPixel(int x, int y, int z, int c, int t, float value) {
		int requestedStackIndex = this.imPl.getStackIndex(c+1, z+1, t+1); //planes are 1-indexed in ImagePlus
		if (requestedStackIndex != this.currentStackIndex) {
			this.imPl.setSliceWithoutUpdate(requestedStackIndex);
			this.currentStackIndex = requestedStackIndex;
		}
		
		imPl.getProcessor().setf(x,y,value);
	}
	
	/**
	 * Does nothing.
	 */
	protected void updateConvertedPixelsFromBytes() {
		
	}
	
	/**
	 * Does nothing.
	 */
	protected void updateBytesFromConvertedPixels() {
		
	}
	
	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#toImagePlus()
	 */
	public ImagePlus toImagePlus() {
		return this.imPl;
	}
	
	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.image.PixelData#getDimensionOrder()
	 */
	public String getDimensionOrder() {
		return imagePlusDimensionOrder;
	}
	
}
