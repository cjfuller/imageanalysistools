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


/**
 * Created by IntelliJ IDEA.
 * User: cfuller
 * Date: Dec 8, 2010
 * Time: 5:27:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClusterObject implements Positioned{

    private Cluster currentCluster;
    private int mostProbableCluster;
    private org.apache.commons.math.geometry.Vector3D centroid;
    private int nPixels;
    private double prob;

    public ClusterObject() {
        prob = 0;
        nPixels = 0;
        mostProbableCluster = 0;
        centroid =null;
        currentCluster = null;
    }

    public Cluster getCurrentCluster() {
        return currentCluster;
    }

    public void setCurrentCluster(Cluster currentCluster) {
        this.currentCluster = currentCluster;
    }

    public Vector3D getCentroid() {
        return centroid;
    }

    public void setCentroid(Vector3D centroid) {
        this.centroid = centroid;
    }

    public void setCentroidComponents(double x, double y, double z) {
        this.centroid = new Vector3D(x, y, z);
    }

    public int getnPixels() {
        return nPixels;
    }

    public void setnPixels(int nPixels) {
        this.nPixels = nPixels;
    }

    public void incrementnPixels() {
        this.nPixels++;
    }

    public double getProb() {
        return prob;
    }

    public void setProb(double prob) {
        this.prob = prob;
    }

    public int getMostProbableCluster() {
        return mostProbableCluster;
    }

    public void setMostProbableCluster(int mostProbableCluster) {
        this.mostProbableCluster = mostProbableCluster;
    }

    //interface Positioned implementations

    public Vector3D getPosition() {return this.getCentroid();}

    public double distanceTo(Positioned other) {
       return other.getPosition().add(-1.0, this.getPosition()).getNorm();
    }

}
