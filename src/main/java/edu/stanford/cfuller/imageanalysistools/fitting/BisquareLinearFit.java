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

package edu.stanford.cfuller.imageanalysistools.fitting;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import edu.stanford.cfuller.imageanalysistools.filter.LocalBackgroundEstimationFilter;

public class BisquareLinearFit {
	
	private boolean noIntercept;
	private double tuningConstant;
	
	final static double DEFAULT_TUNING_CONST = 4.685;
	final static double CONV_NORM = 1e-5;
	final static double CLOSE_TO_ZERO = 1e-6;
	
	/**
	* Constructs a new BisquareLinearFit with default tuning constant and y-intercept enabled.
	*/
	public BisquareLinearFit() {
		this.noIntercept = false;
		this.tuningConstant = DEFAULT_TUNING_CONST;
	}

	
	/**
	* Disables the fitting of the y-intercept for this fit.
	*/
	public void disableIntercept() {
		this.noIntercept = true;
	}
	
	
	/**
	* Performs a robust least squares fit with bisquare weights to the supplied data.
	* 
	* @param indVarValues A RealVector containing the values of the independent variable.
	* @param depVarValues A RealVector containing the values of the dependent variable.
	* @return a RealVector containing two elements: the slope of the fit and the y-intercept of the fit.
	*/
	public RealVector fit(RealVector indVarValues, RealVector depVarValues) {
		
		RealVector uniformWeights = new ArrayRealVector(indVarValues.getDimension(), 1.0);
		
		RealVector lastParams = new ArrayRealVector(2, Double.MAX_VALUE);
						
		RealVector currParams = wlsFit(indVarValues, depVarValues, uniformWeights);
		
		RealVector weights = uniformWeights;
		
		RealVector leverages = this.calculateLeverages(indVarValues);
		
		int c= 0;

		double norm_mult = 1.0;
		
		if (! this.noIntercept) {
		    norm_mult = 2.0;
		}
		
		while (lastParams.subtract(currParams).getNorm() > CONV_NORM*norm_mult) {


			lastParams = currParams;
			
			RealVector stdAdjR = this.calculateStandardizedAdjustedResiduals(indVarValues, depVarValues, leverages, currParams);
			
			weights = calculateBisquareWeights(stdAdjR);			
			
			currParams = wlsFit(indVarValues, depVarValues, weights);
			
		}
		
		return currParams;
		
	}
	
	
	/**
	* Performs a weighted least squares fit with supplied weights to the supplied data.
	* 
	* @param indVarValues A RealVector containing the values of the independent variable.
	* @param depVarValues A RealVector containing the values of the dependent variable.
	* @param weights A RealVector containing the weights for the data points.
	* @return a RealVector containing two elements: the slope of the fit and the y-intercept of the fit.
	*/
	public RealVector wlsFit(RealVector indVarValues, RealVector depVarValues, RealVector weights) {
		
		//initial guess for the fit: unweighted regression.
		
		SimpleRegression unweighted = new SimpleRegression(!this.noIntercept);
		
		for (int i = 0; i < indVarValues.getDimension(); i++) {
			unweighted.addData(indVarValues.getEntry(i), depVarValues.getEntry(i));
		}
		
		RealVector parameters = null;
		
		if (this.noIntercept) {
			parameters = new ArrayRealVector(1, 0.0);
		} else {
			parameters = new ArrayRealVector(2, 0.0);
		}
		
		parameters.setEntry(0, unweighted.getSlope());
		
		if (!this.noIntercept) {
			parameters.setEntry(1, unweighted.getIntercept());
		}
		
		NelderMeadMinimizer nmm = new NelderMeadMinimizer(1e-12);
		WlsObjectiveFunction wof = new WlsObjectiveFunction();
		wof.setIndVar(indVarValues);
		wof.setDepVar(depVarValues);
		wof.setWeights(weights);
		wof.setShouldFitIntercept(!this.noIntercept);
		
		parameters = nmm.optimize(wof, parameters);
		
		if (this.noIntercept) {
			RealVector output = new ArrayRealVector(2,0.0);
			output.setEntry(0, parameters.getEntry(0));
			return output;
		}
		
		return parameters;
		
	}
	
	/**
	* Calculates the leverages of data points for least squares fitting (assuming equal variances).
	* 
	* @param indVarValues The values of the independent variable used for the fitting.
	* @return a RealVector containing a leverage value for each independent variable value.
	*/
	protected RealVector calculateLeverages(RealVector indVarValues) {
		
		RealMatrix indVarMatrix = null;
		
		if (this.noIntercept) {
			indVarMatrix = new Array2DRowRealMatrix(indVarValues.getDimension(), 1);
		} else {
			indVarMatrix = new Array2DRowRealMatrix(indVarValues.getDimension(), 2);
		}
		
		indVarMatrix.setColumnVector(0, indVarValues);
		
		if (!this.noIntercept) {
			indVarMatrix.setColumnVector(1, indVarValues.mapMultiply(0).mapAdd(1));
		}

		RealVector leverages = new ArrayRealVector(indVarValues.getDimension());
		
		QRDecomposition xQR = new QRDecomposition(indVarMatrix);
		
		RealMatrix xR = xQR.getR();
				
		int smallerDim = xR.getRowDimension() <  xR.getColumnDimension() ? xR.getRowDimension() :  xR.getColumnDimension();
		
		RealMatrix xRSq = xR.getSubMatrix(0,smallerDim-1, 0, smallerDim-1);
		
		QRDecomposition xRQR = new QRDecomposition(xRSq);
		
		RealMatrix xRInv = xRQR.getSolver().getInverse();
		
		RealMatrix xxRInv = indVarMatrix.multiply(xRInv);
		
		
		for (int i = 0; i < indVarValues.getDimension(); i++) {
			double sum = 0;
			for (int j = 0; j < xxRInv.getColumnDimension(); j++) {
				sum += Math.pow(xxRInv.getEntry(i,j),2);
			}
			leverages.setEntry(i, sum);
		}
				
		return leverages;
		
	}
	
	/**
	* Calculates the standardized adjusted residuals (according to the same definition used by MATLAB) of the data points for fitting.
	* 
	* @param indVarValues The values of the independent variable used for the fitting.
	* @param depVarValues The values of the dependent variable used for the fitting.
	* @param leverages the leverages of the independent variables, as compted by {@link #calculateLeverages(RealVector)}
	* @param fitParams the results of a (possibly weighted) least squares fit to the data, containing one or two components: a slope and an optional y-intercept.
	* @return a RealVector containing an adjusted residual value for each data point
	*/
	protected RealVector calculateStandardizedAdjustedResiduals(RealVector indVarValues, RealVector depVarValues, RealVector leverages, RealVector fitParams) {
		
		RealVector predictedValues = indVarValues.mapMultiply(fitParams.getEntry(0));
		
		RealVector denom = leverages.mapMultiply(-1.0).mapAddToSelf(1 + this.CLOSE_TO_ZERO).mapToSelf(new org.apache.commons.math3.analysis.function.Sqrt());
		
		if (!this.noIntercept) {
			predictedValues = predictedValues.mapAdd(fitParams.getEntry(1));
		}
		
		double stddev = 0;
		double mean = 0;
		
		for (int i = 0; i < depVarValues.getDimension(); i++) {
			mean += depVarValues.getEntry(i);
		}
		
		mean/= depVarValues.getDimension();
		
		stddev = depVarValues.mapSubtract(mean).getNorm() * (depVarValues.getDimension()*1.0/(depVarValues.getDimension()-1));
	
		
		RealVector residuals = depVarValues.subtract(predictedValues).ebeDivide(denom);		
				
		RealVector absDev = residuals.map(new org.apache.commons.math3.analysis.function.Abs());
		
		int smallerDim = 2;
		
		if (this.noIntercept) {
			smallerDim = 1;
		}
				
		double[] resArray = residuals.map(new org.apache.commons.math3.analysis.function.Abs()).toArray();
		
		java.util.Arrays.sort(resArray);
		
		RealVector partialRes = new ArrayRealVector(absDev.getDimension() - smallerDim + 1,0.0);
				
		for (int i = smallerDim -1; i < resArray.length; i++) {
			partialRes.setEntry(i - smallerDim + 1, resArray[i]);
		}
		
		double resMAD= 0;
		
		if (partialRes.getDimension() % 2 == 0) {
			resMAD = LocalBackgroundEstimationFilter.quickFindKth(partialRes.getDimension()/2, partialRes) + LocalBackgroundEstimationFilter.quickFindKth(partialRes.getDimension()/2 -1, partialRes);
			resMAD/=2.0;
		} else {
			resMAD = LocalBackgroundEstimationFilter.quickFindKth((partialRes.getDimension()-1)/2, partialRes);
		}
		
		resMAD /= 0.6745;		
		
		
		if (resMAD < stddev * CLOSE_TO_ZERO) {
			resMAD = stddev * CLOSE_TO_ZERO;
		}
		
		return residuals.mapDivide(DEFAULT_TUNING_CONST * resMAD);
		
	}
	
	/**
	* Calculates the weight for the next weighted least squares iteration using the bisquare weighting function.
	* @param stdAdjR the standardized adjusted residuals, as computed by {@link #calculateStandardizedAdjustedResiduals(RealVector, RealVector, RealVector, RealVector)}
	* @return a RealVector containing weights for each data point suitable for weighted least squares fitting.
	*/
	protected RealVector calculateBisquareWeights(RealVector stdAdjR) {
		
		RealVector bisquareWeights = new ArrayRealVector(stdAdjR.getDimension(), 0.0);
		
		for (int i = 0; i < bisquareWeights.getDimension(); i++) {
			if (Math.abs(stdAdjR.getEntry(i)) < 1) {
				bisquareWeights.setEntry(i,Math.pow(1-Math.pow(stdAdjR.getEntry(i),2),2));
			}
		}	
		return bisquareWeights;
	}
	
	/**
	* A class implementing weighted sum of squares on a linear model.
	* Negative weights will be made positive.
	*/
	protected static class WlsObjectiveFunction implements ObjectiveFunction {
		
		private RealVector ind;
		private RealVector dep;
		private RealVector weights;
		private boolean shouldFitIntercept;
		
		public WlsObjectiveFunction() {
			this.ind = null;
			this.dep = null;
			this.weights = null;
			this.shouldFitIntercept = true;
		}
		
		public void setWeights(RealVector weights) {
			this.weights = weights;
		}
		
		public void setDepVar(RealVector dep) {
			this.dep = dep;
		}
		
		public void setIndVar(RealVector ind) {
			this.ind = ind;
		}
		
		public void setShouldFitIntercept(boolean fitInt) {
			this.shouldFitIntercept = fitInt;
		}
		
		public double evaluate(RealVector parameters) {
			
			RealVector predictedResponse = this.ind.mapMultiply(parameters.getEntry(0));
			
			if (this.shouldFitIntercept) {
				
				predictedResponse.mapAddToSelf(parameters.getEntry(1));
				
			}
			
			RealVector sqrRes = this.dep.subtract(predictedResponse).mapToSelf(new org.apache.commons.math3.analysis.function.Power(2));
			
			return sqrRes.ebeMultiply(this.weights).getL1Norm();
						
		}
		
		
	}


	
}
