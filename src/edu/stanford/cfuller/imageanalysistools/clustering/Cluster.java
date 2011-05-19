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

import org.apache.commons.math.geometry.Vector3D;

import java.util.Set;

/**
 * A representation of a single Cluster of ClusterObject that might result from applying a clustering algorithm to a collection of objects.
 * 
 * @author Colin J. Fuller
 */
public class Cluster implements Positioned{

    private java.util.Set<ClusterObject> objectSet;
    private int ID;
    private org.apache.commons.math.geometry.Vector3D centroid;

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
