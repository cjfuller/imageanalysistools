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

package edu.stanford.cfuller.imageanalysistools.fitting;

import java.util.Vector;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.integration.LegendreGaussIntegrator;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.optimization.OptimizationException;

import edu.stanford.cfuller.imageanalysistools.fitting.GaussianImageObject.ErrIntFunc;
import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;

/**
 * @author cfuller
 *
 */
public class CentroidImageObject extends ImageObject {

	private static final long serialVersionUID = 1L;
	
	public CentroidImageObject(int label, Image mask, Image parent, ParameterDictionary p) {
		this.init(label, mask, parent, p);
	}
	
	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.fitting.ImageObject#fitPosition(edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary)
	 */
	@Override
	public void fitPosition(ParameterDictionary p)
			throws FunctionEvaluationException, OptimizationException {
		
		if (this.sizeInPixels == 0) {
            this.nullifyImages();
            return;
        }

        this.fitParametersByChannel = new Vector<RealVector>();
        this.fitR2ByChannel = new Vector<Double>();
        this.fitErrorByChannel = new Vector<Double>();
        this.nPhotonsByChannel = new Vector<Double>();

        int numChannels = 0;

        if (p.hasKey("num_wavelengths")) {
            numChannels = p.getIntValueForKey("num_wavelengths");
        } else {
            numChannels = this.parent.getDimensionSizes().get(ImageCoordinate.C);
        }

        for (int channelIndex = 0; channelIndex < numChannels; channelIndex++) {


            this.parentBoxMin.set(ImageCoordinate.C,channelIndex);
            this.parentBoxMax.set(ImageCoordinate.C,channelIndex + 1);

            this.boxImages();

            java.util.Vector<Double> x = new java.util.Vector<Double>();
            java.util.Vector<Double> y = new java.util.Vector<Double>();
            java.util.Vector<Double> z = new java.util.Vector<Double>();
            java.util.Vector<Double> f = new java.util.Vector<Double>();


            for (ImageCoordinate ic : this.parent) {
                x.add((double) ic.get(ImageCoordinate.X));
                y.add((double) ic.get(ImageCoordinate.Y));
                z.add((double) ic.get(ImageCoordinate.Z));
                if (((int) this.mask.getValue(ic)) == this.label) {
                	f.add(parent.getValue(ic));
                } else {
                	f.add(0.0);
                }
            }

            xValues = new double[x.size()];
            yValues = new double[y.size()];
            zValues = new double[z.size()];
            functionValues = new double[f.size()];

            double xCentroid = 0;
            double yCentroid = 0;
            double zCentroid = 0;
            double totalCounts = 0;


            

            for (int i = 0; i < x.size(); i++) {
            	
                xValues[i] = x.get(i);
                yValues[i] = y.get(i);
                zValues[i] = z.get(i);
                functionValues[i] = f.get(i);
                xCentroid += xValues[i] * functionValues[i];
                yCentroid += yValues[i] * functionValues[i];
                zCentroid += zValues[i] * functionValues[i];
                totalCounts += functionValues[i];
            }


            xCentroid /= totalCounts;
            yCentroid /= totalCounts;
            zCentroid /= totalCounts;
            
            RealVector position = new ArrayRealVector(3, 0.0);
         
            position.setEntry(0, xCentroid);
            position.setEntry(1, yCentroid);
            position.setEntry(2, zCentroid);
            
            this.positionsByChannel.add(position);
                       
        }

        this.hadFittingError = false;
        this.nullifyImages();
		
		
		

	}

}
