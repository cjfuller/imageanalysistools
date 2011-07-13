/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Colin J. Fuller's code.
 *
 * The Initial Developer of the Original Code is
 * Colin J. Fuller.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): Colin J. Fuller
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package edu.stanford.cfuller.imageanalysistools.method;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;

import edu.stanford.cfuller.imageanalysistools.filter.ConvolutionFilter;
import edu.stanford.cfuller.imageanalysistools.filter.Filter;
import edu.stanford.cfuller.imageanalysistools.filter.Kernel;
import edu.stanford.cfuller.imageanalysistools.filter.LabelFilter;
import edu.stanford.cfuller.imageanalysistools.filter.MaximumSeparabilityThresholdingFilter;
import edu.stanford.cfuller.imageanalysistools.fitting.DifferentialEvolutionMinimizer;
import edu.stanford.cfuller.imageanalysistools.fitting.ObjectiveFunction;
import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import edu.stanford.cfuller.imageanalysistools.image.io.ImageReader;

/**
 * @author cfuller
 *
 */
public class RandomKernelMethod extends Method {

	final int kDim = 5;
	
	Complex[][] transformStorage;
	
	LabelFilter LF;
	MaximumSeparabilityThresholdingFilter MSTF;
	ArrayList<KinetochorePair> pairs;
	HashSet<Integer> pairedKinetochores;
	
	protected Kernel generateRandomKernel() {
				
		ImageCoordinate kernelDimensions = ImageCoordinate.createCoord(kDim, kDim, 1, 1, 1);
		
		double[] kernel = new double[kDim*kDim];
		
		for (int i =0; i < kernel.length; i++) {
			
			kernel[i] = Math.random()*2 - 1;
			
		}
		
		Kernel k = new Kernel(kernel, kernelDimensions);
		
		return k;
		
	}
	
	
	protected class KinetochorePair {
		public int one;
		public int two;
		
		public KinetochorePair(int one, int two) {
			this.one = one;
			this.two = two;
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.stanford.cfuller.imageanalysistools.method.Method#go()
	 */
	@Override
	public void go() {
		
		pairs = new ArrayList<KinetochorePair>();
		
		pairedKinetochores = new HashSet<Integer>();
		
		pairs.add(new KinetochorePair(8,16));
		pairs.add(new KinetochorePair(14,25));
		pairs.add(new KinetochorePair(9,20));
		pairs.add(new KinetochorePair(13,22));
		pairs.add(new KinetochorePair(50,69));
		pairs.add(new KinetochorePair(63,77));
		pairs.add(new KinetochorePair(67,80));
		pairs.add(new KinetochorePair(79,83));
		pairs.add(new KinetochorePair(108,119));
		pairs.add(new KinetochorePair(117,127));
		pairs.add(new KinetochorePair(121,129));
		pairs.add(new KinetochorePair(115,124));
		
		for (KinetochorePair kp : pairs) {
			pairedKinetochores.add(kp.one);
			pairedKinetochores.add(kp.two);
		}
		

		
		LF = new LabelFilter();
		MSTF = new MaximumSeparabilityThresholdingFilter();
		
		LF.setParameters(this.parameters);
		MSTF.setParameters(this.parameters);
		
		
		ConvolutionFilter cf = new ConvolutionFilter();
		ConvolutionFilter cf2 = new ConvolutionFilter();
		ConvolutionFilter cf3 = new ConvolutionFilter();
		
		Kernel k = generateRandomKernel();
		
		cf.setParameters(this.parameters);
		cf2.setParameters(this.parameters);
		cf3.setParameters(this.parameters);
		
		
        java.util.Vector<Filter> filters = new java.util.Vector<Filter>();

		filters.add(cf);
		filters.add(cf2);
		filters.add(cf3);
		
		int count = 0;
		
		Image ref = null;
		
		try {
			//ref = (new ImageReader()).read("/Users/cfuller/Desktop/watson-ref.ome.tif");
			ref = (new ImageReader()).read("/Users/cfuller/Desktop/KSHREC_images/20110630/maxintproj/output_mask/deschmutzed/20110630_cs1_001.nd2_proj.ome.tif.CentromereFindingMethod.out.0.ome.tif");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.transformStorage = ConvolutionFilter.transform(this.images.get(0), 0);

		
		
		//double[] scores = new double[count];
		
		//int i = 0;
		
		DifferentialEvolutionMinimizer min = new DifferentialEvolutionMinimizer();
		
		KernelObjectiveFunction kof = new KernelObjectiveFunction(ref);
		
		RealVector parameterLowerBounds = new ArrayRealVector(3*kDim*kDim, -1.0);
		RealVector parameterUpperBounds = new ArrayRealVector(3*kDim*kDim, 1.0);
		
		int populationSize = 50;
		
		double tol = 1e-1;
        double scaleFactor =0.2;
        double crossoverFrequency = 0.1;
        
        int maxIterations = 100;
		
		RealVector minParams = min.minimize(kof, parameterLowerBounds, parameterUpperBounds, populationSize, scaleFactor, maxIterations, crossoverFrequency, tol);
		
		//while (i < count) {
		
		
		ImageCoordinate dim = ImageCoordinate.createCoord(kDim, kDim, 1, 1, 1);
		
			Kernel kFinal = new Kernel(minParams.getSubVector(0, minParams.getDimension()/3).getData(), dim);
			Kernel kFinal2 = new Kernel(minParams.getSubVector(minParams.getDimension()/3, minParams.getDimension()/3).getData(), dim);
			Kernel kFinal3 = new Kernel(minParams.getSubVector(2*minParams.getDimension()/3, minParams.getDimension()/3).getData(), dim);
		
	        Image toProcess = new Image(this.images.get(0));
	        
	        cf.setKernel(kFinal);
	        
	        cf2.setKernel(kFinal2);
	        cf3.setKernel(kFinal3);
	        
	        iterateOnFiltersAndStoreResult(filters, toProcess, new edu.stanford.cfuller.imageanalysistools.metric.ZeroMetric());
	     
	        double score = scoreImage(this.getStoredImages().get(this.getStoredImages().size() - 1), ref);
	        
			dim.recycle();

	        
	        //scores[i] = score;
	        
	        //i++;
	        	        
		//}
		
		//System.out.println(java.util.Arrays.toString(scores));
//		
//		int minIndex = 0;
//		double minScore = Integer.MAX_VALUE;
//		
//		for (int j = 0; j < scores.length; j++) {
//			if (scores[j] < minScore) {
//				minIndex = j;
//				minScore = scores[j];
//			}
//		}
		
		System.out.println("max score: " + score);
		System.out.println("best parameters: " + minParams.toString());
		
        
	}

	
	protected double scoreImage(Image output, Image ref) {
				

		MSTF.apply(output);
		LF.apply(output);
		
		HashMap<Integer, Integer> assigned = new HashMap<Integer, Integer>();
		HashMap<Integer, Set<Integer> > inverseAssigned = new HashMap<Integer, Set<Integer> >();
		
		HashSet<Integer> multiplyAssigned = new HashSet<Integer>();
		
		for (ImageCoordinate ic : ref) {
			
			if (ref.getValue(ic) > 0 && output.getValue(ic) > 0) {
				
				int refInt = (int) ref.getValue(ic);
				int outInt = (int) output.getValue(ic);
				
				if (assigned.containsKey(refInt) && assigned.get(refInt) != outInt) {
					multiplyAssigned.add(refInt);
					assigned.remove(refInt);
					continue;
				}
				
				if (!multiplyAssigned.contains(refInt)) {
					assigned.put(refInt, outInt);
				}
				
				if (!inverseAssigned.containsKey(outInt)) {
					inverseAssigned.put(outInt, new HashSet<Integer>());
				}
				
				inverseAssigned.get(outInt).add(refInt);
				
			}
			
		}
		
		double score = 0;
		
		for (KinetochorePair kp : pairs) {
			
			int oneAssn = 0;
			int twoAssn = 0;
			
			if (assigned.containsKey(kp.one)) {
			
				oneAssn = assigned.get(kp.one);
				
			}
			
			if (assigned.containsKey(kp.two)) {
				twoAssn = assigned.get(kp.two);
			}
			
			if (oneAssn > 0 && oneAssn == twoAssn) {
				score -= 1;
			}
			
			
			for (Integer i : pairedKinetochores) {
				
				if (i == kp.one || i == kp.two) continue; 
				
				int iAssn = 0;
				
				if (!assigned.containsKey(i)) {
					continue;
				}
				
				iAssn = assigned.get(i);
				
				if ((iAssn == oneAssn || iAssn == twoAssn) && iAssn > 0) {
					
					score += 1;
					
				}
				
				
			}
			
			for (Integer i : inverseAssigned.keySet()) {
				if (inverseAssigned.get(i).size() == 2) {
					score += 0.1;
				} else if (inverseAssigned.get(i).size() > 2) {
					score -= 0.01*inverseAssigned.get(i).size();
				}
			}
			
			
		}
		
		return score;
		
		
		
//		
//		
//		double sum_out = 0;
//		double sum_ref = 0;
//		int count = 0;
//		double product = 0;
//		
//		for (ImageCoordinate ic : output) {
//			
//			sum_out += output.getValue(ic);
//			sum_ref += ref.getValue(ic);
//			count++;
//			product += output.getValue(ic)*ref.getValue(ic);
//			
//		}
//		
//		
//		return (product* sum_ref/sum_out);
		
	}
	
	protected class KernelObjectiveFunction implements ObjectiveFunction{
				
		ImageCoordinate dim;
		Image ref;
		
		public KernelObjectiveFunction(Image ref){
			
			this.dim = ImageCoordinate.createCoord(kDim, kDim, 1, 1, 1);

			this.ref = ref;
						
		}
		
		public double evaluate(RealVector kernelParameters) {
			
			Kernel k = new Kernel(kernelParameters.getSubVector(0, kernelParameters.getDimension()/3).getData(), dim);
			Kernel k2 = new Kernel(kernelParameters.getSubVector(kernelParameters.getDimension()/3, kernelParameters.getDimension()/3).getData(), dim);
			Kernel k3 = new Kernel(kernelParameters.getSubVector(2*kernelParameters.getDimension()/3, kernelParameters.getDimension()/3).getData(), dim);
			
			ConvolutionFilter cf = new ConvolutionFilter();
			
			cf.setKernel(k);
			
			cf.setTransform(transformStorage);
			
			Image temp = new Image(images.get(0));
			
			cf.apply(temp);
			
			cf.setTransform(null);
			
			cf.setKernel(k2);
			
			cf.apply(temp);
			
			cf.setKernel(k3);
			
			cf.apply(temp);
			
			return scoreImage(temp, ref);
									
			
		}
		
		
		protected void finalize() throws Throwable {
			
			this.dim.recycle();
			
		}
		
	}

}
