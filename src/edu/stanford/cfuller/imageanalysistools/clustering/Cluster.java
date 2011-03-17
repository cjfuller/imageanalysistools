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
 * Created by IntelliJ IDEA.
 * User: cfuller
 * Date: Dec 8, 2010
 * Time: 5:31:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class Cluster implements Positioned{

    private java.util.Set<ClusterObject> objectSet;
    private int ID;
    private org.apache.commons.math.geometry.Vector3D centroid;
    private org.apache.commons.math.geometry.Vector3D covariance;

    public Cluster() {

        objectSet = new java.util.HashSet<ClusterObject>();
        ID = 0;
        centroid = null;
        covariance = null;
    }

    public Set<ClusterObject> getObjectSet() {
        return objectSet;
    }

    public void setObjectSet(Set<ClusterObject> objectSet) {
        this.objectSet = objectSet;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public Vector3D getCentroid() {
        return centroid;
    }

    public void setCentroid(Vector3D centroid) {
        this.centroid = centroid;
    }

    public Vector3D getCovariance() {
        return covariance;
    }

    public void setCovariance(Vector3D covariance) {
        this.covariance = covariance;
    }

    public void setCentroidComponents(double x, double y, double z) {
        this.centroid = new Vector3D(x, y, z);
    }

    public void setCovarianceComponents(double xx, double xy, double yy) {
        this.covariance = new Vector3D(xx, xy, yy);
    }


    //interface Positioned implementations

    public Vector3D getPosition() {return this.getCentroid();}

    public double distanceTo(Positioned other) {
       return other.getPosition().add(-1.0, this.getPosition()).getNorm();
    }



}
