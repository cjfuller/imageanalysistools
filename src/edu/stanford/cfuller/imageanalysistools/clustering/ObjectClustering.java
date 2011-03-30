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

package edu.stanford.cfuller.imageanalysistools.clustering;

import edu.stanford.cfuller.imageanalysistools.image.Image;
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
import edu.stanford.cfuller.imageanalysistools.image.Histogram;
import edu.stanford.cfuller.imageanalysistools.filter.RelabelFilter;
import edu.stanford.cfuller.imageanalysistools.filter.LabelFilter;
import edu.stanford.cfuller.imageanalysistools.filter.MaskFilter;
import edu.stanford.cfuller.imageanalysistools.filter.GaussianFilter;
import org.apache.commons.math.geometry.Vector3D;
import org.apache.commons.math.linear.ArrayRealVector;
import edu.stanford.cfuller.imageanalysistools.random.RandomGenerator;

import java.util.Vector;

/**
 * Utilites for doing clustering of objects in an Image.
 */

public class ObjectClustering {


    /**
     * Sets up a set of ClusterObjects and a set of Clusters from two Image masks, one labeled with individual objects, and one labeled with all objects in a single cluster grouped with a single label.
     *
     * @param labeledByObject       An Image mask with all objects in the Image labeled with an unique greylevel value.  These labels must start with 1 and be consecutive.
     * @param labeledByCluster      An Image mask with all the objects in each cluster labeled with the same unique greylevel value.  These labels must start with 1 and be consecutive.
     * @param clusterObjects        A Vector of ClusterObjects that will contain the initialized ClusterObjects on return; this can be empty, and any contents will be erased.
     * @param clusters              A Vector of Clusters that will contain the initialized Clusters on return; this can be empty, and any contents will be erased.
     * @param k                     The number of clusters in the Image.  This must be the same as the number of unique nonzero greylevels in the labeledByCluster Image.
     * @return                      The number of ClusterObjects in the Image.
     */
    public static int initializeObjectsAndClustersFromClusterImage(Image labeledByObject, Image labeledByCluster, Vector<ClusterObject> clusterObjects, Vector<Cluster> clusters, int k) {

        clusters.clear();

        for (int j = 0; j< k; j++) {

            clusters.add(new Cluster());
            clusters.get(j).setID(j+1);

        }

        Histogram h = new Histogram(labeledByObject);

        int n = h.getMaxValue();

        clusterObjects.clear();

        for (int j= 0; j < n; j++) {
            clusterObjects.add(new ClusterObject());
            clusterObjects.get(j).setCentroidComponents(0,0,0);
            clusterObjects.get(j).setnPixels(0);
        }

        for (ImageCoordinate i : labeledByObject) {

            if (labeledByObject.getValue(i) > 0) {

                int value = (int) (labeledByObject.getValue(i));

                clusterObjects.get(value-1).incrementnPixels();
                clusterObjects.get(value-1).setCentroid(clusterObjects.get(value-1).getCentroid().add(new Vector3D(i.getX(), i.getY(), i.getZ())));


            }

        }

        for (int j = 0; j < n; j++) {
            ClusterObject current = clusterObjects.get(j);
            current.setCentroid(current.getCentroid().scalarMultiply(1.0/current.getnPixels()));
        }

        for (ImageCoordinate i : labeledByObject) {

            int clusterValue = (int) labeledByCluster.getValue(i);
            int objectValue = (int) labeledByObject.getValue(i);
            if (clusterValue == 0 || objectValue == 0) {continue;}

            clusters.get(clusterValue - 1).getObjectSet().add(clusterObjects.get(objectValue-1));
            
        }

        for (Cluster c : clusters) {

            int objectCounter = 0;

            c.setCentroidComponents(0,0,0);

            for (ClusterObject co : c.getObjectSet()) {

                objectCounter++;
                co.setCurrentCluster(c);

                c.setCentroid(c.getCentroid().add(co.getCentroid()));
                
            }

            c.setCentroid(c.getCentroid().scalarMultiply(1.0/objectCounter));

        }


        return n;
        
    }

    /**
     * Sets up a set of ClusterObjects and a set of Clusters from an Image mask with each object labeled with a unique greylevel.
     *
     * @param im                The Image mask with each cluster object labeled with a unique greylevel.  These must start at 1 and be consecutive.
     * @param clusterObjects    A Vector of ClusterObjects that will contain the initialized ClusterObjects on return; this may be empty, and any contents will be erased.
     * @param clusters          A Vector of Clusters that will contain the initialized Clusters on return; this may be empty, and any contents will be erased.
     * @param k                 The number of Clusters to generate.
     * @return                  The number of ClusterObjects in the Image.
     */
    public static int initializeObjectsAndClustersFromImage(Image im, Vector<ClusterObject> clusterObjects, Vector<Cluster> clusters, int k) {

        int n = 0;

        clusters.clear();

        for (int j =0; j < k; j++) {

            clusters.add(new Cluster());
            clusters.get(j).setID(j+1);

        }

        Histogram h = new Histogram(im);

        n = h.getMaxValue();

        clusterObjects.clear();

        for(int j =0; j < n; j++) {

            clusterObjects.add(new ClusterObject());

            clusterObjects.get(j).setCentroidComponents(0,0,0);

            clusterObjects.get(j).setnPixels(0);

        }

        for (ImageCoordinate i : im) {

            if (im.getValue(i) > 0){

                ClusterObject current = clusterObjects.get((int) im.getValue(i) - 1);

                current.incrementnPixels();

                current.setCentroid(current.getCentroid().add(new Vector3D(i.getX(), i.getY(), i.getZ())));

            }

        }

        for (int j = 0; j < n; j++) {
            ClusterObject current = clusterObjects.get(j);
            current.setCentroid(current.getCentroid().scalarMultiply(1.0/current.getnPixels()));
        }

        //initialize clusters using kmeans++ strategy

        double[] probs = new double[n];
        double[] cumulativeProbs = new double[n];


        java.util.Arrays.fill(probs, 0);
        java.util.Arrays.fill(cumulativeProbs, 0);

        //choose the initial cluster

        int initialClusterObject = (int) Math.floor(n*RandomGenerator.rand());

        //java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("initialClusterObject: " + Integer.toString(initialClusterObject));

        //java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("k: " + Integer.toString(k));

        //java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("n: " + Integer.toString(n));


        clusters.get(0).setCentroid(clusterObjects.get(initialClusterObject).getCentroid());

        clusters.get(0).getObjectSet().add(clusterObjects.get(initialClusterObject));

        for (int j =0; j< n; j++) {

            clusterObjects.get(j).setCurrentCluster(clusters.get(0));
        }

        //assign the remainder of the clusters


        for (int j = 1; j < k; j++) {

            double probSum = 0;

            for (int m = 0; m < n; m++) {
                double minDist = Double.MAX_VALUE;

                Cluster bestCluster = null;

                for (int p = 0; p < j; p++) {

                    double tempDist = clusterObjects.get(m).distanceTo(clusters.get(p));

                    if (tempDist < minDist) {
                        minDist = tempDist;

                        bestCluster = clusters.get(p);
                    }

                }

                probs[m] = minDist;
                probSum += minDist;

                clusterObjects.get(m).setCurrentCluster(bestCluster);
            }

            for (int m = 0; m < n; m++) {
                probs[m] = probs[m]/probSum;
                if (m == 0) {cumulativeProbs[m] = probs[m];}
                else{cumulativeProbs[m] = cumulativeProbs[m-1]+probs[m];}
            }  double randNum = RandomGenerator.rand();
            int nextCenter = 0;

            for (int m = 0; m < n; m++) {
                if (randNum < cumulativeProbs[m]) {
                    nextCenter = m;
                    break;
                }
            }

            clusters.get(j).setCentroid(clusterObjects.get(nextCenter).getCentroid());


        }

        for (int m = 0; m < n; m++) {

            double minDist = Double.MAX_VALUE;

            Cluster bestCluster = null;

            for (int p = 0; p < k; p++) {

                double tempDist = clusterObjects.get(m).distanceTo(clusters.get(p));

                if (tempDist < minDist) {

                    minDist = tempDist;
                    bestCluster = clusters.get(p);
                }
            }

            clusterObjects.get(m).setCurrentCluster(bestCluster);
            bestCluster.getObjectSet().add(clusterObjects.get(m));

        }


        return n;
    }

    public static double getInterClusterDistances(Vector<ClusterObject> clusterObjects, Vector<Cluster> clusters, int k, int n) {

        double[] intraClusterDists = new double[k];
        int[] intraCounts = new int[k];
        double[] maxIntra = new double[k];

        java.util.Arrays.fill(intraClusterDists, 0.0);
        java.util.Arrays.fill(intraCounts, 0);
        java.util.Arrays.fill(maxIntra, 0.0);


        for (int i =0; i < n; i++) {

            int i_ID = clusterObjects.get(i).getCurrentCluster().getID();

            for (int j = i+1; j < n; j++) {

                int j_ID = clusterObjects.get(j).getCurrentCluster().getID();


                if ( i_ID == j_ID) {

                    double dist = clusterObjects.get(i).distanceTo(clusterObjects.get(j));

                    intraClusterDists[i_ID-1]+= dist;

                    intraCounts[i_ID-1]++;

                    if (dist > maxIntra[i_ID-1]) {
                        maxIntra[i_ID-1] = dist;
                    }

                }

            }
        }

        double ratios = 0;
        int ratio_counts = 0;

        for (int i =0; i < k; i++) {

            if(clusters.get(i).getObjectSet().size() == 0) {

                continue;

            }

            for (int j = i+1; j < k; j++) {

                if (clusters.get(j).getObjectSet().size() == 0) {
                    continue;
                }

                double maxDist = 0;

                for (ClusterObject i_obj : clusters.get(i).getObjectSet()) {

                    for (ClusterObject j_obj : clusters.get(j).getObjectSet()) {

                        double dist = i_obj.distanceTo(j_obj);

                        if (dist > maxDist) {
                            maxDist = dist;
                        }


                    }

                }

                final double zeroCountScalingFactor = 4;

                if (intraCounts[i] == 0) {
                    intraCounts[i] = 1;
                    intraClusterDists[i] = maxDist/zeroCountScalingFactor;
                }

                if (intraCounts[j] == 0) {
                    intraCounts[j] = 1;
                    intraClusterDists[j] = maxDist/zeroCountScalingFactor;
                }

                ratios += 2*(intraClusterDists[i]/intraCounts[i] + intraClusterDists[j]/intraCounts[j])/maxDist;
                ratio_counts++;
                

            }


        }




        if (ratio_counts == 0) {
            double maxElement = 0;

            for (double d : maxIntra) {
                if (d>maxElement) maxElement = d;
            }

            ratios = 4*intraClusterDists[0]/(maxElement + 1e-9);
            ratio_counts = 1;
        }

        return ratios/ratio_counts;
    }


    /**
     * Relabels an Image mask that is labeled with an individual greylevel for each cluster object to have all objects in a single cluster labeled with the same value.
     *
     * 
     * @param output            The Image mask labeled with unique greylevels for each cluster object; this should have regions labeled according to the same scheme used to generate the clusters and objects.  It will be overwritten.
     * @param clusterObjects    A Vector containing the ClusterObjects corresponding to the labeled objects in the Image mask.
     * @param clusters          A Vector containing the Clusters comprised of the ClusterObjects that will determine the labels in the output Image.
     * @param k                 The number of Clusters.
     */
    public static void clustersToMask(Image output, Vector<ClusterObject> clusterObjects, Vector<Cluster> clusters, int k) {

        for (ImageCoordinate i : output) {

            int value = (int) output.getValue(i);

            if (value > 0) {

                output.setValue(i, clusterObjects.get(value-1).getCurrentCluster().getID());
            }

        }


    }

    /**
     * Performs a long-range gaussian filtering on an Image mask labeled by ClusterObject.
     *
     * This is useful to isolate general areas of an Image that contain objects, and is often an excellent approximation or starting point for the clustering.
     *
     * @param input     The Image to be filtered (this will be left unchanged).
     * @return          The filtered Image.
     */
    public static Image gaussianFilterMask(Image input) {

        Image origCopy = new Image(input);
        GaussianFilter gf = new GaussianFilter();

        final int MAX_VALUE = 4095;

        for (ImageCoordinate i : origCopy) {
            if (origCopy.getValue(i) > 0) {
                origCopy.setValue(i, MAX_VALUE);
            }
        }

        gf.setWidth(origCopy.getDimensionSizes().getX()/4);



        gf.apply(origCopy);

        return origCopy;
        
    }

    /**
     * Applies basic clustering to an Image with objects.
     *
     * This will use the long-range gaussian filtering approach to assign clusters; objects sufficiently near to each other will be smeared into a single object and assigned to the same cluster.
     *
     * @param input             An Image mask labeled such that each object in the Image is assigned a unique nonzero greylevel value.  These should start at 1 and be consecutive.
     * @param original          The original image (not currently used... this is here to maintain the interface with a previous version that used this image)
     * @param gaussianFiltered  The mask with a long range Gaussian filter applied (as from {@link #gaussianFilterMask}).  This is an optional parameter;
     *                          input null to have this automatically generated by the method.  This parameter is
     *                          chiefly useful to save computation time when running the clutering multiple times.
     *                          This will be modified, so if planning to reuse the Gaussian filtered image, pass in a copy.
     */
    public static Image doBasicClustering(Image input, Image original, Image gaussianFiltered) {

        RelabelFilter rlf = new RelabelFilter();
        LabelFilter lf = new LabelFilter();
        MaskFilter mf = new MaskFilter();

        mf.setReferenceImage(input);


        Histogram h_individualCentromeres = new Histogram(input);


        Image origCopy = null;

        if (gaussianFiltered == null) {

            origCopy = gaussianFilterMask(input);

        } else {
            origCopy = gaussianFiltered;
        }

        lf.apply(origCopy);

        //java.util.logging.Logger.getLogger("debug").info("finished with filters");

        Image mapped = new Image(origCopy);

        Histogram h_mapped_0 = new Histogram(origCopy);

        //first, find the centroid of each cluster

        org.apache.commons.math.linear.RealVector centroids_x = new ArrayRealVector(h_mapped_0.getMaxValue()+1);
        org.apache.commons.math.linear.RealVector centroids_y = new ArrayRealVector(h_mapped_0.getMaxValue()+1);

        org.apache.commons.math.linear.RealVector counts = new ArrayRealVector(h_mapped_0.getMaxValue()+1);


        centroids_x.mapMultiplyToSelf(0.0);
        centroids_y.mapMultiplyToSelf(0.0);
        counts.mapMultiplyToSelf(0.0);

        for (ImageCoordinate i : origCopy){
            if (origCopy.getValue(i) > 0) {
                int value = (int) origCopy.getValue(i);
                centroids_x.setEntry(value, centroids_x.getEntry(value) + i.getX());
                centroids_y.setEntry(value, centroids_y.getEntry(value) + i.getY());
                counts.setEntry(value, counts.getEntry(value) + 1);
            }
        }
        for (int i = 0; i < counts.getDimension(); i++) {
            if (counts.getEntry(i) == 0) {
                counts.setEntry(i, 1);
                centroids_x.setEntry(i, -1*origCopy.getDimensionSizes().getX());
                centroids_y.setEntry(i, -1*origCopy.getDimensionSizes().getY());
            }
            centroids_x.setEntry(i, centroids_x.getEntry(i)/counts.getEntry(i));
            centroids_y.setEntry(i, centroids_y.getEntry(i)/counts.getEntry(i));

        }

        for (ImageCoordinate i : origCopy) {

            if (mapped.getValue(i) > 0 || input.getValue(i) == 0) continue;

            double minDistance = Double.MAX_VALUE;
            int minIndex = 0;

            for (int j = 0; j < centroids_x.getDimension(); j++) {
                double dist = Math.hypot(centroids_x.getEntry(j) - i.getX(), centroids_y.getEntry(j) - i.getY());
                if (dist < minDistance) {
                    minDistance = dist;
                    minIndex = j;
                }
            }

            mapped.setValue(i, minIndex);

        }

        Histogram h_mapped = new Histogram(mapped);

        //if a centromere lies across a boundary, some of its pixels might be in different regions-- correct this by simple majority

        // hmm... maybe this is not so important... is just choosing the first one encountered faster?
/*
        org.apache.commons.math.linear.RealMatrix mappingcounts = new Array2DRowRealMatrix(h_individualCentromeres.getMaxValue() + 1, h_mapped.getMaxValue() + 1);

        mappingcounts = mappingcounts.scalarMultiply(0.0);

        for (ImageCoordinate i : mapped) {

            if (mapped.getValue(i) > 0) {
                mappingcounts.setEntry((int) input.getValue(i), (int) mapped.getValue(i), 1+mappingcounts.getEntry((int) input.getValue(i), (int) mapped.getValue(i)));
            }

        }
*/


        int[] centromereAssignments = new int[h_individualCentromeres.getMaxValue()+1];
        java.util.Arrays.fill(centromereAssignments, 0);
/*
        for (int i =1; i < centromereAssignments.length; i++) {
            int maxCounts = 0;
            int assignment = 0;

            for (int j = 1; j < mappingcounts.getColumnDimension(); j++) {
                if (mappingcounts.getEntry(i,j) > maxCounts) {
                    maxCounts = (int) mappingcounts.getEntry(i,j);
                    assignment = j;
                }
            }

            centromereAssignments[i] = assignment;
        }
*/

/*
        for (ImageCoordinate i : mapped) {

            if (input.getValue(i) > 0 ) {

                mapped.setValue(i, centromereAssignments[(int) input.getValue(i)]);

            }
        }
*/
        for (ImageCoordinate i : mapped) {

            if (input.getValue(i) > 0 ) {

                int value = (int) input.getValue(i);

                if (centromereAssignments[value] > 0) {
                    mapped.setValue(i, centromereAssignments[value]);
                } else {
                    centromereAssignments[value] = (int) mapped.getValue(i);
                }

            }
        }


        mf.apply(mapped);
        origCopy.copy(mapped);
        mf.setReferenceImage(origCopy);

        mf.apply(input);
        rlf.apply(input);
        rlf.apply(origCopy);

        return origCopy;
    }


    /**
     * Applies complex clustering to an Image with objects.
     *
     * This will first use the basic clustering to initially assign clusters and then attempt to subdivide those clusters using Gaussian mixture model clustering on each initial cluster.
     *
     * @param input             An Image mask labeled such that each object in the Image is assigned a unique nonzero greylevel value.  These should start at 1 and be consecutive.
     * @param original          The original image (not currently used... this is here to maintain the interface with a previous version that used this image)
     * @param maxClusters       A rough upper bound to the number of clusters expected in the Image.  More clusters than this may be found, but if a clustering iteration finds more clusters than this as the best solution, it will terminate the clustering.
     * @param gaussianFiltered  The mask with a long range Gaussian filter applied (as from {@link #gaussianFilterMask}).  This is an optional parameter;
     *                          input null to have this automatically generated by the method.  This parameter is
     *                          chiefly useful to save computation time when running the clutering multiple times.
     *                          This will be modified, so if planning to reuse the Gaussian filtered image, pass in a copy.
     */
    public static void doComplexClustering(Image input, Image original, int maxClusters, Image gaussianFiltered) {

        //debug output
        //input.writeToFile("/Users/cfuller/Desktop/filter_intermediates/input.ome.tif");

        Image output = doBasicClustering(input, original, gaussianFiltered);

        doClusteringWithInitializedClusters(input,original, maxClusters, output);


    }

    /**
     * Applies the complex clustering to an Image with objects that have already been grouped into initial guesses of clusters.
     *
     * This will use the cluster guesses as a starting point and attempt to subdivide these clusters using Gaussian mixture model clustering on each cluster individually.
     *
     * @param input             An Image mask labeled such that each object in the Image is assigned a unique nonzero greylevel value.  These should start at 1 and be consecutive.
     * @param original          The original image (not currently used... this is here to maintain the interface with a previous version that used this image)
     * @param maxClusters       A rough upper bound to the number of clusters expected in the Image.  More clusters than this may be found, but if a clustering iteration finds more clusters than this as the best solution, it will terminate the clustering.
     * @param clusterImage      A version of the Image mask relabeled such that each object in the Image is assigned a greylevel value corresponding to its cluster.  Each cluster should have a uniqe value, these should start at 1, and they should be consecutive.
     */
    public static void doClusteringWithInitializedClusters(Image input, Image original, int maxClusters, Image clusterImage) {

        //input.writeToFile("/Users/cfuller/Desktop/filter_intermediates/input.ome.tif");
        //original.writeToFile("/Users/cfuller/Desktop/filter_intermediates/original.ome.tif");
        //clusterImage.writeToFile("/Users/cfuller/Desktop/filter_intermediates/clusterImage.ome.tif");

        final double interdist_cutoff =0.89;


        Vector<ClusterObject> clusterObjects = new Vector<ClusterObject>();
        Vector<Cluster> clusters = new Vector<Cluster>();


        double maxRatio = 0;

        int maxIndex = 0;

        double bestRatio = 1.0;

        int bestK = 0;

        double lastL = -1.0*Double.MAX_VALUE;
        double lastLDiff = 0;

        Image bestImage = null;

        double overallMaxL = 1.0*Double.MAX_VALUE;

        int repeatThis = 0;
        int numRepeats = 3;

        RelabelFilter rlf = new RelabelFilter();

        Image origCopy = new Image(clusterImage);

        Histogram h_ssf = new Histogram(origCopy);

        int k_init = h_ssf.getMaxValue();

        int numAttempts = 0;

        int lastBestK = 0;

        for (int k = 1; k <= maxClusters; k++) {

            numAttempts++;

            int orig_k = k;

            int numTrials = 10*k;

            double interdist;

            double intradist;

            double currMaxL = -1.0*Double.MAX_VALUE;

            int numSingluar = 0;

            if (k == 1) numTrials = 1;

            int n=0;

            double L = -1.0*Double.MAX_VALUE;

            Image candidateNewBestImage = null;

            if (numAttempts == 1 || bestImage == null) {

                k = k_init;

                bestImage = new Image(origCopy);

                bestK = k_init;
                
            }

            candidateNewBestImage = new Image(bestImage);

            Histogram h = new Histogram(bestImage);

            int currentMaxImageValue = h.getMaxValue();

            double sumL = 0;

            Image singleCluster = new Image(input);

            Image dividedClusterTemp = new Image(singleCluster);


            //java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("Starting Clustering Iteration " + Integer.toString(numAttempts));
            

            for (int clusterNumber = 1; clusterNumber <= h.getMaxValue(); clusterNumber++) {


                singleCluster.copy(input);

                dividedClusterTemp.copy(singleCluster);

                ImageCoordinate clusterMin = ImageCoordinate.cloneCoord(singleCluster.getDimensionSizes());
                ImageCoordinate clusterMax = ImageCoordinate.createCoord(0,0,0,0,0);

                for (ImageCoordinate i : singleCluster) {

                    if (bestImage.getValue(i) != clusterNumber) {

                        singleCluster.setValue(i, 0);
                        
                    } else {

                        //find the min and max bounds of this cluster
                        
                        if (i.getX() < clusterMin.getX() ) {clusterMin.setX(i.getX());}
                        if (i.getY() < clusterMin.getY() ) {clusterMin.setY(i.getY());}
                        if (i.getZ() < clusterMin.getZ() ) {clusterMin.setZ(i.getZ());}
                        if (i.getC() < clusterMin.getC() ) {clusterMin.setC(i.getC());}
                        if (i.getT() < clusterMin.getT() ) {clusterMin.setT(i.getT());}

                        if (i.getX() >= clusterMax.getX() ) {clusterMax.setX(i.getX()+1);}
                        if (i.getY() >= clusterMax.getY() ) {clusterMax.setY(i.getY()+1);}
                        if (i.getZ() >= clusterMax.getZ() ) {clusterMax.setZ(i.getZ()+1);}
                        if (i.getC() >= clusterMax.getC() ) {clusterMax.setC(i.getC()+1);}
                        if (i.getT() >= clusterMax.getT() ) {clusterMax.setT(i.getT()+1);}

                    }
                }

                singleCluster.setBoxOfInterest(clusterMin, clusterMax);

                rlf.apply(singleCluster);

                Histogram hSingleCluster = new Histogram(singleCluster);

                int nSingleCluster = hSingleCluster.getMaxValue();

                boolean accepted = false;

                double tempBestRatio = Double.MAX_VALUE;

                double tempBestL = 0;

                int origCurrentMaxImageValue = currentMaxImageValue;

                Image tempCandidateNewBestImage = new Image(candidateNewBestImage);

                int kMax = ((bestK < 3) ? 6 : 4);

                if (kMax > nSingleCluster) {kMax = nSingleCluster;}

                for (int tempK = 2; tempK < kMax; tempK++) {

                    //java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("tempK: " + Integer.toString(tempK));


                    for (int repeatCounter =0; repeatCounter < numRepeats; repeatCounter++) {

                        boolean accept = false;

                        int tempCurrentMaxImageValue = origCurrentMaxImageValue;

                        n = initializeObjectsAndClustersFromImage(singleCluster, clusterObjects, clusters, tempK);

                        L = DEGaussianMixtureModelClustering.go(singleCluster, clusterObjects, clusters, tempK, n);


                        interdist = getInterClusterDistances(clusterObjects, clusters, tempK, n);

                        if (interdist < interdist_cutoff && interdist < tempBestRatio) {

                            accept = true;
                            accepted = true;
                            tempBestRatio = interdist;

                            tempBestL = L;

                        }

                        if (accept) {


                            dividedClusterTemp.copy(singleCluster);


                            dividedClusterTemp.setBoxOfInterest(clusterMin, clusterMax);
                            
                            clustersToMask(dividedClusterTemp, clusterObjects, clusters, tempK);

                            //dividedClusterTemp.writeToFile("/Users/cfuller/Desktop/filter_intermediates/divided_" + clusterNumber + ".ome.tif");

                            int newClusterValue = tempCurrentMaxImageValue;

                            tempCandidateNewBestImage.copy(candidateNewBestImage);

                            for (ImageCoordinate i : singleCluster) {


                                if (dividedClusterTemp.getValue(i) > 1) {

                                    tempCandidateNewBestImage.setValue(i, newClusterValue + dividedClusterTemp.getValue(i) -1);

                                }
                            }

                            tempCurrentMaxImageValue = newClusterValue + tempK - 1;

                            currentMaxImageValue = tempCurrentMaxImageValue;
                        }

                        clusterObjects.clear();
                        clusters.clear();

                    }

                }

                if (accepted) {

                    sumL += tempBestL;
                    candidateNewBestImage.copy(tempCandidateNewBestImage);
                } else {

                    if (nSingleCluster > 0) {

                        n = initializeObjectsAndClustersFromImage(singleCluster, clusterObjects, clusters, 1);
                        sumL += DEGaussianMixtureModelClustering.go(singleCluster, clusterObjects, clusters, 1, n);

                    }

                    clusterObjects.clear();

                    clusters.clear();

                }

                dividedClusterTemp.clearBoxOfInterest();
                singleCluster.clearBoxOfInterest();
                clusterMin.recycle();
                clusterMax.recycle();

            }

            k = currentMaxImageValue;

            //candidateNewBestImage.writeToFile("/Users/cfuller/Desktop/filter_intermediates/candidate.ome.tif");

            n = initializeObjectsAndClustersFromClusterImage(input, candidateNewBestImage, clusterObjects, clusters, k);
            L= sumL;

            double tempL = -1.0*L;

            double Ldiff = 0;

            if (numAttempts ==1) {
                lastL = tempL;
                lastLDiff = -1;
                Ldiff = 0;


            } else {
            
                Ldiff = (L - lastL)/Math.abs(lastL);
            }

            interdist = getInterClusterDistances(clusterObjects, clusters, clusters.size(), clusterObjects.size());

            if (interdist == -1) {
                interdist = 1;
            }

            double ratio = interdist;

            if (numAttempts == 1) {

                overallMaxL = tempL;

                bestRatio = Double.MAX_VALUE;

            }

            if (tempL >= overallMaxL && ratio < bestRatio) {

                bestRatio = ratio;

                lastBestK = bestK;
                bestK= k;

                repeatThis = 0;

                if (bestImage == null) {
                    bestImage = new Image(input);
                } else {
                    bestImage.copy(input);
                }

                clustersToMask(bestImage, clusterObjects, clusters, bestK);

                rlf.apply(bestImage);

                overallMaxL = tempL;

                lastL = tempL;

                lastLDiff = Ldiff;

            }

            if (tempL > currMaxL) {currMaxL = tempL;}

            clusters.clear();
            clusterObjects.clear();

            if (++repeatThis < numRepeats) { k = orig_k;}
            else {repeatThis = 0;}

            if (orig_k > k) {

                k = orig_k;
            }

            candidateNewBestImage = null;

            if (k > maxClusters) break;
            if (repeatThis == 0 && bestK == lastBestK) break;
            if (numAttempts >= maxClusters) break;

        }

        input.copy(bestImage);
        //java.util.logging.Logger.getLogger("edu.stanford.cfuller.imageanalysistools").info("Best guess number of clusters: " + bestK);

    }

}
