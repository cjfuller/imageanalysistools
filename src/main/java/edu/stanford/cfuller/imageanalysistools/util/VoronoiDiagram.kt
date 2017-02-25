package edu.stanford.cfuller.imageanalysistools.util

import org.apache.commons.math3.geometry.euclidean.twod.Euclidean2D
import org.apache.commons.math3.geometry.euclidean.twod.Line
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D
import org.apache.commons.math3.geometry.partitioning.BSPTree
import org.apache.commons.math3.geometry.partitioning.Hyperplane

private data class PointPair(val first: Vector2D, val second: Vector2D) : Comparable<PointPair> {
    override fun compareTo(other: PointPair): Int {
        return first.distance(second).compareTo(other.first.distance(other.second))
    }
}

/**
 * This class represents a 2D Voronoi Diagram and the methods to construct one.  It
 * takes a series of points, constructs the diagram, and assigns each region in the diagram
 * a unique label.  The ordering of points passed to the constructor determines the region
 * label (1-indexed) in the diagram.
 *
 * @author Colin J. Fuller
 **/
class VoronoiDiagram(pointsInput: List<Vector2D>) {
    val regionLookup: MutableMap<Vector2D, Int> = hashMapOf()
    val diagram = BSPTree<Euclidean2D>()
    init {
        pointsInput.forEachIndexed { i, point -> regionLookup.put(point, i+1) }
        val allPairs: MutableList<PointPair> = mutableListOf()
        pointsInput.forEachIndexed { i, point0 ->
            (i+1 .. pointsInput.size - 1).forEach { j ->
                allPairs.add(PointPair(point0, pointsInput[j]))
            }
        }
        allPairs.sort()
        divideRecursive(diagram, allPairs)
    }

    private fun divideRecursive(leaf: BSPTree<Euclidean2D>, pairsToUse: MutableList<PointPair>) {
        if (pairsToUse.isEmpty()) { return }
        var p = pairsToUse.removeAt(0)
        while (!leaf.insertCut(constructBisectingHyperplane(p.first, p.second))) {
            if (pairsToUse.isEmpty()) { return }
            p = pairsToUse.removeAt(0)
        }

        leaf.attribute = null
        leaf.minus.attribute = null
        leaf.plus.attribute = null

        val secondPairs: MutableList<PointPair> = mutableListOf(*pairsToUse.toTypedArray())

        divideRecursive(leaf.minus, pairsToUse)
        divideRecursive(leaf.plus, secondPairs)
    }

    private fun constructBisectingHyperplane(firstPoint: Vector2D, secondPoint: Vector2D): Hyperplane<Euclidean2D> {
        val midpoint = firstPoint.add(secondPoint).scalarMultiply(0.5)
        val difference = secondPoint.subtract(firstPoint)
        val angle = Math.atan2(difference.y, difference.x) - Math.PI/2.0
        return Line(midpoint, angle)
    }

    private fun isLeaf(node: BSPTree<Euclidean2D>) = node.plus == null && node.minus == null

    /**
     * Gets the number of the region in which a supplied point lies.
     *
     * @param point a Vector2D describing the point whose region is being queried.
     * @return an int that is the label of the region in which the supplied vector lies.
     * 			this label corresponds to the (1-indexed) order in which the points were supplied
     * 			to the constructor.
     */
    fun getRegionNumber(point: Vector2D): Int {
        var node = this.diagram.getCell(point)
        while (node.attribute == null) {
            if (isLeaf(node)) {
                node.attribute = findClosestRegion(point)
                break
            }
            // Bump the point slightly if it's exactly on an edge.
            val eps = 1e-6
            var newpoint = point.add(Vector2D(eps, 0.0))
            node = this.diagram.getCell(newpoint)
            if (node.attribute != null) break
            if (isLeaf(node)) {
                node.attribute = findClosestRegion(point)
                break
            }
            newpoint = point.add(Vector2D(eps, 0.0))
            node = this.diagram.getCell(newpoint)
        }
        return node.attribute as? Int ?: 0
    }

    private fun findClosestRegion(lookup: Vector2D): Int {
        val best = regionLookup.keys.minBy { seedPoint ->
            seedPoint.distance(lookup)
        }
        return regionLookup[best] ?: 0
    }
}
