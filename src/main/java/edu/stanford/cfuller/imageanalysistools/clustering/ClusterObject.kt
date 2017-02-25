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

package edu.stanford.cfuller.imageanalysistools.clustering

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D


/**
 * Representation of an Object identified in an image with some defined spatial position that can be clustered.

 * @author Colin J. Fuller
 */
class ClusterObject : Positioned {

    /**
     * Gets a reference to the Cluster in which the ClusterObject is currently contained.
     * @return  The Cluster containing the ClusterObject.
     */
    /**
     * Sets the Cluster to which the ClusterObject is currently assigned.

     * This operation will remove any previous assignment.

     * @param currentCluster    the Cluster to which to assign the ClusterObject.
     */
    var currentCluster: Cluster? = null
    /**
     * Gets the ID of the cluster that is most likely to contain the ClusterObject.
     * @return  The ID, as an int.
     */
    /**
     * Sets the ID of the cluster that is most likely to contain the ClusterObject.
     * @param mostProbableCluster
     */
    var mostProbableCluster: Int = 0
    /**
     * Gets the centroid of the ClusterObject.

     * @return  A Vector3D containing the components of the centroid of the ClusterObject.
     */
    /**
     * Sets the centroid of the ClusterObject to the specified Vector3D.

     * Any existing centroid is discarded.

     * @param centroid  The Vector3D to which to set the centroid.
     */
    var centroid: Vector3D? = null
    private var nPixels: Int = 0
    /**
     * Gets the probability (density) of the ClusterObject being found at its location given the Cluster to which it is assigned.
     * @return  The probability of the ClusterObject.
     */
    /**
     * Sets the probability (density) of the ClusterObject being found at its location given the Cluster to which it is assigned.
     * @param prob  The probability of the ClusterObject.
     */
    var prob: Double = 0.toDouble()

    /**
     * Constructs a default ClusterObject.
     */
    init {
        prob = 0.0
        nPixels = 0
        mostProbableCluster = 0
        centroid = null
        currentCluster = null
    }

    /**
     * Sets the centroid of the ClusterObject by its individual components.

     * Any existing centroid is discarded.

     * @param x     The x-component of the centroid.
     * *
     * @param y     The y-component of the centroid.
     * *
     * @param z     The z-component of the centroid.
     */
    fun setCentroidComponents(x: Double, y: Double, z: Double) {
        this.centroid = Vector3D(x, y, z)
    }

    /**
     * Gets the number of pixels contained in the ClusterObject in the original image.
     * @return  The number of pixels in the ClusterObject.
     */
    fun getnPixels(): Int {
        return nPixels
    }

    /**
     * Sets the number of pixels contained in the ClusterObject in the oridinal image.
     * @param nPixels   The number of pixels in the ClusterObject.
     */
    fun setnPixels(nPixels: Int) {
        this.nPixels = nPixels
    }

    /**
     * Increments the number of pixels contained in the ClusterObject by 1.
     */
    fun incrementnPixels() {
        this.nPixels++
    }

    //interface Positioned implementations

    override val position: Vector3D
        get() = this.centroid

    override fun distanceTo(other: Positioned): Double {
        return other.position.add(-1.0, this.position).norm
    }

}
