/**
 * A filter that takes a 2D mask containing labeled regions an converts it to a Voronoi diagram.
 * Each region's geometric centroid is used as the point around which to calculated the diagram.
 * The interior of each region is labeled the same as the region used to generate it.  Edges are
 * filled with zero.  Edges may be slightly wider than expected, in order that pixels
 * in adjacent regions are not 8-connected (so that a LabelFilter will not merge them).
 *
 *
 * The argument to the apply method should be the Image with labeled regions.
 *
 *
 * This filter does not use a reference Image.

 * @author Colin J. Fuller
 */
package edu.stanford.cfuller.imageanalysistools.filter

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D


import edu.stanford.cfuller.imageanalysistools.image.Histogram
import edu.stanford.cfuller.imageanalysistools.image.WritableImage
import edu.stanford.cfuller.imageanalysistools.image.Image
import edu.stanford.cfuller.imageanalysistools.image.ImageFactory
import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate


import edu.stanford.cfuller.imageanalysistools.util.VoronoiDiagram

class VoronoiFilter : Filter() {
    /**
     * Applies the Filter to the supplied Image.
     * @param im    The Image to whose labeled region will be converted to a Voronoi diagram.
     */
    override fun apply(im: WritableImage) {
        val regionCentroids = java.util.HashMap<Int, Vector2D>()
        for (ic in im) {
            val label = im.getValue(ic).toInt()
            if (label == 0) continue

            if (!regionCentroids.containsKey(label)) {
                regionCentroids.put(label, Vector2D(0.0, 0.0))
            }
            regionCentroids.put(
                    label,
                    regionCentroids[label]!!.add(Vector2D(ic[ImageCoordinate.X].toDouble(), ic[ImageCoordinate.Y].toDouble())))
        }
        val h = Histogram(im)
        val regions = java.util.ArrayList<Int>()

        for (i in regionCentroids.keys) {
            regionCentroids.put(i, regionCentroids[i]!!.scalarMultiply(1.0 / h.getCounts(i)))
            regions.add(i)
        }
        regions.sort()

        val orderedCentroids = java.util.ArrayList<Vector2D>()
        regions.mapTo(orderedCentroids) { regionCentroids[it]!! }

        val regionCentroidsIm = ImageFactory.createWritable(im.dimensionSizes, 0.0f)
        val diagram = ImageFactory.createWritable(im.dimensionSizes, 1.0f)
        val cenTemp = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0)

        for (i in regions) {
            val cen = regionCentroids[i]!!
            cenTemp[ImageCoordinate.X] = cen.x.toInt()
            cenTemp[ImageCoordinate.Y] = cen.y.toInt()
            regionCentroidsIm.setValue(cenTemp, i.toFloat())
        }

        cenTemp.recycle()

        diagram.asSequence()
                .filter { isOnEightConnectedBoundary(orderedCentroids, it) }
                .forEach { diagram.setValue(it, 0.0f) }

        val lf = LabelFilter()
        val oolbsf = OneToOneLabelBySeedFilter()
        lf.apply(diagram)
        oolbsf.referenceImage = regionCentroidsIm
        oolbsf.apply(diagram)
        im.copy(diagram)
    }

    /**
     * Checks whether a given coordinate would be on a boundary of a
     * Voronoi diagram created from the given points.

     */
    fun isOnEightConnectedBoundary(points: List<Vector2D>, ic: ImageCoordinate): Boolean {
        val x = ic[ImageCoordinate.X]
        val y = ic[ImageCoordinate.Y]
        var closestIndex = 0
        var nextIndex = 0
        var closestDist = java.lang.Double.MAX_VALUE
        var nextDist = java.lang.Double.MAX_VALUE

        for (i in points.indices) {
            val pt = points[i]
            val dist = Math.hypot(pt.x - x, pt.y - y)
            if (dist < closestDist) {
                nextDist = closestDist
                nextIndex = closestIndex
                closestDist = dist
                closestIndex = i
            } else if (dist < nextDist) {
                nextDist = dist
                nextIndex = i
            }
        }
        val projectedCoordinate = this.projectPointOntoVector(points[closestIndex], Vector2D(x.toDouble(), y.toDouble()), points[nextIndex])
        val distToNext = points[nextIndex].subtract(projectedCoordinate).norm
        val distToClosest = points[closestIndex].subtract(projectedCoordinate).norm
        val cutoff = 1.3 * Math.sqrt(2.0)
        return distToNext - distToClosest < cutoff
    }

    private fun projectPointOntoVector(origin: Vector2D, pointToProject: Vector2D, pointOnVector: Vector2D): Vector2D {
        val onto = pointOnVector.subtract(origin).normalize()
        val toProj = pointToProject.subtract(origin)
        val projected = origin.add(onto.scalarMultiply(onto.dotProduct(toProj)))
        return projected
    }

    private fun isOnEightConnectedBoundary(ic: ImageCoordinate, noBoundaryLabels: Image): Boolean {
        val icClone = ImageCoordinate.cloneCoord(ic)
        val value = noBoundaryLabels.getValue(ic)
        for (x_off in -1..1) {
            for (y_off in -1..1) {
                icClone[ImageCoordinate.X] = ic[ImageCoordinate.X] + x_off
                icClone[ImageCoordinate.Y] = ic[ImageCoordinate.Y] + y_off
                if (noBoundaryLabels.inBounds(icClone)) {
                    val newvalue = noBoundaryLabels.getValue(icClone)
                    if (newvalue != value) {
                        icClone.recycle()
                        return true
                    }
                }
            }
        }
        icClone.recycle()
        return false
    }
}
