/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2012 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */

package edu.stanford.cfuller.imageanalysistools.filter;

import edu.stanford.cfuller.imageanalysistools.fitting.ObjectiveFunction;
import edu.stanford.cfuller.imageanalysistools.fitting.NelderMeadMinimizer;
import edu.stanford.cfuller.imageanalysistools.image.WritableImage;
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import edu.stanford.cfuller.imageanalysistools.filter.GaussianFilter;
import edu.stanford.cfuller.imageanalysistools.filter.GradientFilter;

import org.apache.commons.math3.linear.RealVector;



/**
* A Filter that takes an image and segments it using an active contour-based method.
* <p>
* Currently, only one contour per image is supported, and an initial guess must be supplied.
* <p>
* After applying the filter, the argument to the apply method is replaced by a set of points
* on the contour.  (These are unlikely to be continuous.)
* <p>
* This filter does not take a reference image.
* <p>
* The initial contour guess must be supplied as a set of point stored in a RealVector, ordered as:
* {x0, y0, x1, y1, ..., xn, yn}.  These will be circularized for optimizing the contour such that the
* nth point will be adjacent to the zeroth point.
* <p>
* The optimized contour points (in the same format as the initial contour must be applied) are available by
* calling the {@link #getContourPoints()} method after the apply method has been called.
* 
* @author Colin J. Fuller
*/
public class ActiveContourFilter extends Filter {
	
	RealVector contourPoints;
	
	RealVector initialContour;
	
	/**
	* This class is the core of the active contour optimization and provides an "energy"
	* value given a set of points on the contour.  Implements ObjectiveFunction for use
	* with optimizers.
	*/
	protected class ActiveContourObjectiveFunction implements ObjectiveFunction {
		
		WritableImage image;
		WritableImage gradientImage;
		
		double gradient_weight;
		double im_weight;
		double elastic_weight;
		double continuity_weight;
		
		final int defaultGaussianWidth = 5;
		final double defaultGradientWeight = -2.0;
		final double defaultImWeight = 0.0;
		final double defaultElasticWeight = 0.7;
		final double defaultContinuityWeight = 0.7;
		
		/**
		* Constructs a new ActiveContourObjectiveFunction.
		* @param im  the Image that the contour is being used to segment.
		*/
		public ActiveContourObjectiveFunction(WritableImage im) {
			
			this.image = im;
			
			GaussianFilter gausf = new GaussianFilter();
			
			gausf.setWidth(defaultGaussianWidth);
			
			GradientFilter gradf = new GradientFilter();
			
			this.gradientImage = ImageFactory.createWritable(im);
			
			gausf.apply(this.gradientImage);
			
			gradf.apply(this.gradientImage);
			
			this.gradient_weight = defaultGradientWeight;
			this.im_weight = defaultImWeight;
			this.elastic_weight = defaultElasticWeight;
			this.continuity_weight = defaultContinuityWeight;
			
		}
		
		private double calculateAverageDistance(RealVector parameters) {
			
			double averageDistance = 0;
			
			for (int i = 2; i < parameters.getDimension()-1; i+=2) {
				
				double xm1 = parameters.getEntry(i-2);
				double ym1 = parameters.getEntry(i-1);
				double x = parameters.getEntry(i);
				double y = parameters.getEntry(i+1);
				
				averageDistance += Math.pow(Math.pow(x-xm1, 2) + Math.pow(y-ym1, 2), 0.5);
				
			}
			
			double xm1 = parameters.getEntry(parameters.getDimension() - 2);
			double ym1 = parameters.getEntry(parameters.getDimension() - 1);
			double x = parameters.getEntry(0);
			double y = parameters.getEntry(1);
			
			averageDistance += Math.pow(Math.pow(x-xm1, 2) + Math.pow(y-ym1, 2), 0.5);
			
			averageDistance /= (parameters.getDimension() / 2.0);
			
			return averageDistance;
			
		}
		
		/**
		* Evaluates the ObjectiveFunction, providing the energy value for 
		* the supplied set of points on the contour.
		* @param parameters		A RealVector containing the parameters for the
		* 						objective function, which in this case are the points
		* 						on the contour in the format listed in the class documentation.
		* @return 	a double that is the value of the contour energy function
		*/
		public double evaluate(RealVector parameters) {
					
			return this.internalEnergy(parameters) + this.externalEnergy(parameters);
			
		}
		
		private double continuityTerm(RealVector parameters) {
			
			double ct = 0;
			double averageDistance = this.calculateAverageDistance(parameters);

	    	for (int i = 2; i < parameters.getDimension()-1; i+=2) {

		      double xm1 = parameters.getEntry(i-2);
		      double ym1 = parameters.getEntry(i-1);
		      double x = parameters.getEntry(i);
		      double y = parameters.getEntry(i+1);

		      double dist = Math.pow(Math.pow(x - xm1,2) + Math.pow(y - ym1,2), 0.5);

		      ct += Math.pow(averageDistance - dist,2);

		    }

		    //end condition

		    double xm1 = parameters.getEntry(parameters.getDimension()-2);
		    double ym1 = parameters.getEntry(parameters.getDimension()-1);
		    double x = parameters.getEntry(0);
		    double y = parameters.getEntry(1);

		    double dist = Math.pow(Math.pow(x - xm1,2) + Math.pow(y - ym1,2), 0.5);


		    ct += Math.pow(averageDistance - dist,2);

		    ct *= this.continuity_weight;		
		
			return ct;
			
			
		}
		
		private double curvatureTerm(RealVector parameters) {
			
			double energy = 0;

			for (int i = 2; i < parameters.getDimension()-2; i+=2) {

			  double xm1 = parameters.getEntry(i-2);
			  double ym1 = parameters.getEntry(i-1);

			  double x = parameters.getEntry(i);
			  double y = parameters.getEntry(i+1);

			  double xp1 = parameters.getEntry(i+2);
			  double yp1 = parameters.getEntry(i+3);

			  energy += Math.pow(xm1 + xp1 - 2*x, 2) + Math.pow(ym1 + yp1 - 2*y,2);

			}

			//now do end conditions


			int dim = parameters.getDimension();

			//first

			double xm1 = parameters.getEntry(dim-2);
			double ym1 = parameters.getEntry(dim-1);

			double x = parameters.getEntry(0);
			double y = parameters.getEntry(1);

			double xp1 = parameters.getEntry(2);
			double yp1 = parameters.getEntry(3);

			energy += Math.pow(xm1 + xp1 - 2*x, 2) + Math.pow(ym1 + yp1 - 2*y,2);

			//last

			xm1 = parameters.getEntry(dim-4);
			ym1 = parameters.getEntry(dim-3);

			x = parameters.getEntry(dim-2);
			y = parameters.getEntry(dim-1);

			xp1 = parameters.getEntry(0);
			yp1 = parameters.getEntry(1);

			energy += Math.pow(xm1 + xp1 - 2*x, 2) + Math.pow(ym1 + yp1 - 2*y,2);

			energy *= this.elastic_weight;			
			
			return energy;
			
		}
		
		private double internalEnergy(RealVector parameters) {
			return continuityTerm(parameters) + curvatureTerm(parameters);
		}
	
		private double externalEnergy(RealVector parameters) {
			
			ImageCoordinate ic = ImageCoordinate.createCoordXYZCT(0,0,0,0,0);

	    	double energy = 0;

	   	 	for (int i = 0; i < parameters.getDimension() - 1; i+= 2) {

		      ic.set(ImageCoordinate.X, (int) (parameters.getEntry(i)));
		      ic.set(ImageCoordinate.Y, (int) (parameters.getEntry(i+1)));

		      energy += this.gradient_weight*(Math.abs(this.gradientImage.getValue(ic)));
		      energy += this.im_weight*(this.image.getValue(ic));

		    }
		
		    return energy;
			
		}
	
	
	
	}
	
	/**
	* Applies the filter, replacing the supplied image by the set of points on the optimized contour.
	* The image will have value 1 at these points and 0 elsewhere.
	* @param im  	the Image to be filtered.
	*/
	public void apply(WritableImage im) {
				
		ActiveContourObjectiveFunction acof = new ActiveContourObjectiveFunction(im);
		
		NelderMeadMinimizer nmm = new NelderMeadMinimizer();
				
		RealVector out = nmm.optimize(acof, initialContour);
		
		out = nmm.optimize(acof, out);
		
		this.contourPoints = out;
		
		for (ImageCoordinate ic : im) {
			
			im.setValue(ic, 0);
			
		}
		
		ImageCoordinate ic = ImageCoordinate.createCoordXYZCT(0,0,0,0,0);
		
		for (int i = 0; i < this.contourPoints.getDimension()-1; i+=2) {
			ic.set(ImageCoordinate.X, (int) this.contourPoints.getEntry(i));
			ic.set(ImageCoordinate.Y, (int) this.contourPoints.getEntry(i+1));
			
			im.setValue(ic,1);
		}
		
	}
	
	/**
	* Gets the points on the optimized contour after the apply method has been called
	* in the format specified in the class intro documentation.
	* @return 	a RealVector containing the points on the contour.
	*/
	public RealVector getContourPoints() {
		
		return this.contourPoints;
		
	}
	
	/**
	* Sets the initial guess for the contour.
	* @param	points	a RealVector containing the initial contour points in the format specified
	* 					in the class intro documentation.
	*/
	public void setInitialContour(RealVector points) {
		this.initialContour = points;
	}
	
	
	
}


