package edu.stanford.cfuller.imageanalysistools.clustering

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

/**
 * A representation of a single Cluster of ClusterObject that might result from applying a clustering algorithm to a collection of objects.

 * @author Colin J. Fuller
 */
class Cluster : Positioned {

    /**
     * Gets references to all the objects contained in the Cluster.
     * @return      A Set containing a ClusterObject reference for each ClusterObject assigned to the Cluster.
     */
    /**
     * Sets the ClusterObjects that are assigned to the Cluster.
     * @param objectSet     The Set of ClusterObjects that are to be assigned to the Cluster; any previous assignment is erased.
     */
    var objectSet: MutableSet<ClusterObject> = java.util.HashSet<ClusterObject>()
    /**
     * Gets an integer used to uniquely identify the Cluster.
     * @return  The integer ID.
     */
    /**
     * Sets the integer used to uniquely identify the Cluster.
     * @param ID    The integer that will become the ID of the Cluster.
     */
    var id: Int = 0
    /**
     * Gets the centroid of the Cluster.

     * @return      A Vector3D containing the geometric centroid.
     */
    /**
     * Sets the centroid of the Cluster to the specified Vector3D.

     * No checking is performed to verify that this is actually the centroid.

     * @param centroid  The centroid of the Cluster.
     */
    var centroid: Vector3D = Vector3D(Double.NaN, Double.NaN, Double.NaN)

    /**
     * Sets the centroid of the Cluster by its individual components.
     * @param x     The centroid x-coordinate.
     * *
     * @param y     The centroid y-coordinate.
     * *
     * @param z     The centroid z-coordinate.
     */
    fun setCentroidComponents(x: Double, y: Double, z: Double) {
        this.centroid = Vector3D(x, y, z)
    }

    //interface Positioned implementations

    override val position: Vector3D
        get() = this.centroid

    override fun distanceTo(other: Positioned): Double {
        return other.position.add(-1.0, this.position).norm
    }
}
