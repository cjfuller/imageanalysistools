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

package edu.stanford.cfuller.imageanalysistools.clustering;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Set;

/**
 * A representation of a single Cluster of ClusterObject that might result from applying a clustering algorithm to a collection of objects.
 * 
 * @author Colin J. Fuller
 */
public class Cluster implements Positioned{

    private java.util.Set<ClusterObject> objectSet;
    private int ID;
    private Vector3D centroid;

    /**
     * Constructs an empty cluster.
     */
    public Cluster() {

        objectSet = new java.util.HashSet<ClusterObject>();
        ID = 0;
        centroid = null;
    }

    /**
     * Gets references to all the objects contained in the Cluster.
     * @return      A Set containing a ClusterObject reference for each ClusterObject assigned to the Cluster.
     */
    public Set<ClusterObject> getObjectSet() {
        return objectSet;
    }

    /**
     * Sets the ClusterObjects that are assigned to the Cluster.
     * @param objectSet     The Set of ClusterObjects that are to be assigned to the Cluster; any previous assignment is erased.
     */
    public void setObjectSet(Set<ClusterObject> objectSet) {
        this.objectSet = objectSet;
    }

    /**
     * Gets an integer used to uniquely identify the Cluster.
     * @return  The integer ID.
     */
    public int getID() {
        return ID;
    }

    /**
     * Sets the integer used to uniquely identify the Cluster.
     * @param ID    The integer that will become the ID of the Cluster.
     */
    public void setID(int ID) {
        this.ID = ID;
    }

    /**
     * Gets the centroid of the Cluster.
     *
     * @return      A Vector3D containing the geometric centroid.
     */
    public Vector3D getCentroid() {
        return centroid;
    }

    /**
     * Sets the centroid of the Cluster to the specified Vector3D.
     *
     * No checking is performed to verify that this is actually the centroid.
     * 
     * @param centroid  The centroid of the Cluster.
     */
    public void setCentroid(Vector3D centroid) {
        this.centroid = centroid;
    }

    /**
     * Sets the centroid of the Cluster by its individual components.
     * @param x     The centroid x-coordinate.
     * @param y     The centroid y-coordinate.
     * @param z     The centroid z-coordinate.
     */
    public void setCentroidComponents(double x, double y, double z) {
        this.centroid = new Vector3D(x, y, z);
    }

    //interface Positioned implementations

    public Vector3D getPosition() {return this.getCentroid();}

    public double distanceTo(Positioned other) {
       return other.getPosition().add(-1.0, this.getPosition()).getNorm();
    }



}
