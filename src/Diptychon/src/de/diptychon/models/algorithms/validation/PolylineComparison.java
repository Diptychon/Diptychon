/*
 * This file is part of Diptychon.
 *
 * Diptychon is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Diptychon is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Diptychon. If not, see <http://www.gnu.org/licenses/>.
 */
package de.diptychon.models.algorithms.validation;

import java.awt.geom.AffineTransform;
import java.util.Arrays;

import org.tzi.qsd.description.PolygonalCourse;
import org.tzi.qsd.geometry.Configuration;
import org.tzi.qsd.geometry.Point;
import org.tzi.qsd.geometry.Polygon;
import org.tzi.qsd.geometry.generalisation.Approximation;
import org.tzi.qsd.geometry.generalisation.Generalisation;

import javafx.scene.shape.Line;
import de.diptychon.models.algorithms.A_ValidationApproach;
import de.diptychon.models.algorithms.validation.featureExtraction.scopeBased.PolygonalExtent;

/**
 * This class implements the comparison of two polylines
 */
public class PolylineComparison extends A_ValidationApproach {
    /**
     * The maximum approximation error allowed for the {@link #de Approximation}
     * of a polygon or polyline
     */
    private static final int POLYGONAL_APPROXIMATION = 50;

    /**
     * The maximum distance allowed when comparing two glyphs
     */
    private static final int POLYLINE_DISTANCE = 100;

    /**
     * A glyph which is compared should fit into extent +/- INTERVAL_THRESHOLD
     */
    private static final double INTERVAL_THRESHOLD = 0.07;

    /**
     * Represents the corresponding contour profiles as polylines
     */
    private Polygon top, bottom, left, right;

    /**
     * Represents the corresponding {@link PolygonalCourse polygonal courses}
     * for each polyline
     */
    private final PolygonalCourse pc_Top, pc_Bottom, pc_Left, pc_Right;

    /**
     * The {@link PolygonalExtent extent} of each polyline
     */
    private final double topExtent, bottomExtent, leftExtent, rightExtent;

    /**
     * Initializes a new instance of this object.
     * 
     * @param p
     *            The pixels of the image for which the polylines are to be
     *            extracted
     * @param w
     *            The width of the image
     * @param h
     *            The height of the image
     */
    public PolylineComparison(final byte[] p, final int w, final int h) {
        this.pixels = p;
        this.width = w;
        this.height = h;

        this.extractPolylines(this.pixels, this.width, this.height);

        this.pc_Top = this.getPolygonalCourse(this.top);
        this.topExtent = new PolygonalExtent(this.pc_Top).getFeatureVector()[0];

        this.pc_Bottom = this.getPolygonalCourse(this.bottom);
        this.bottomExtent = new PolygonalExtent(this.pc_Bottom)
                .getFeatureVector()[0];

        this.pc_Left = this.getPolygonalCourse(this.left);
        this.leftExtent = new PolygonalExtent(this.pc_Left).getFeatureVector()[0];

        this.pc_Right = this.getPolygonalCourse(this.right);
        this.rightExtent = new PolygonalExtent(this.pc_Right)
                .getFeatureVector()[0];
    }

    /**
     * Extracts the polylines for an image
     * 
     * @param croppedPixels
     *            the pixels of the image
     * @param width
     *            the width of the image
     * @param height
     *            the height of the image
     */
    private void extractPolylines(final byte[] croppedPixels, final int width,
            final int height) {
        final int[] indizesTop = new int[width];
        Arrays.fill(indizesTop, height / 2);
        final int[] indizesBottom = new int[width];
        Arrays.fill(indizesBottom, height / 2);
        final int[] indizesLeft = new int[height];
        Arrays.fill(indizesLeft, width / 2);
        final int[] indizesRight = new int[height];
        Arrays.fill(indizesRight, width / 2);

        for (int y = 0; y < height; ++y) {
            final int offset = y * width;
            for (int x = 0; x < width; ++x) {
                final int index = offset + x;
                if (croppedPixels[index] == 0) {
                    if (y < (height / 2)) {
                        indizesTop[x] = y < indizesTop[x] ? y : indizesTop[x];
                    } else {
                        indizesBottom[x] = y > indizesBottom[x] ? y
                                : indizesBottom[x];
                    }
                    if (x < (width / 2)) {
                        indizesLeft[y] = x < indizesLeft[y] ? x
                                : indizesLeft[y];
                    } else {
                        indizesRight[y] = x > indizesRight[y] ? x
                                : indizesRight[y];
                    }
                }
            }
        }
        final Point[] pointsTop = new Point[width];
        final Point[] pointsBottom = new Point[width];
        for (int i = 0; i < indizesTop.length; ++i) {
            pointsTop[i] = new Point(i, indizesTop[i]);
            pointsBottom[i] = new Point(i, indizesBottom[i]);
        }

        final Point[] pointsLeft = new Point[height];
        final Point[] pointsRight = new Point[height];
        for (int i = 0; i < indizesLeft.length; ++i) {
            pointsLeft[i] = new Point(indizesLeft[i], i);
            pointsRight[i] = new Point(indizesRight[i], i);
        }

        this.top = this.approximate(this.reducePoints(new Polygon(pointsTop)));
        this.bottom = this.approximate(this.reducePoints(new Polygon(
                pointsBottom)));
        this.left = this
                .approximate(this.reducePoints(new Polygon(pointsLeft)));
        this.right = this.approximate(this
                .reducePoints(new Polygon(pointsRight)));
    }

    /**
     * Gets the polygonal course of a polygon
     * 
     * @param p
     *            the polygon
     * @return the polygonal course
     */
    private PolygonalCourse getPolygonalCourse(final Polygon p) {
        final Configuration config = new Configuration(p);
        return new PolygonalCourse(config);
    }

    /**
     * Approximates a polygon
     * 
     * @param polygon
     *            the polygon
     * @return the appromximated polygon
     */
    private Polygon approximate(final Polygon polygon) {
        Configuration config = new Configuration(polygon);
        final Generalisation generalisation = new Approximation(
                POLYGONAL_APPROXIMATION);
        config = generalisation.generalise(config);
        return config.getPolygons()[0];
    }

    /**
     * Gets the extents
     * 
     * @return the extents
     */
    private double[] getExtents() {
        return new double[] { this.topExtent, this.bottomExtent,
                this.leftExtent, this.rightExtent };
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((this.bottom == null) ? 0 : this.bottom.hashCode());
        long temp;
        temp = Double.doubleToLongBits(this.bottomExtent);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result
                + ((this.left == null) ? 0 : this.left.hashCode());
        temp = Double.doubleToLongBits(this.leftExtent);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result
                + ((this.pc_Bottom == null) ? 0 : this.pc_Bottom.hashCode());
        result = prime * result
                + ((this.pc_Left == null) ? 0 : this.pc_Left.hashCode());
        result = prime * result
                + ((this.pc_Right == null) ? 0 : this.pc_Right.hashCode());
        result = prime * result
                + ((this.pc_Top == null) ? 0 : this.pc_Top.hashCode());
        result = prime * result
                + ((this.right == null) ? 0 : this.right.hashCode());
        temp = Double.doubleToLongBits(this.rightExtent);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result
                + ((this.top == null) ? 0 : this.top.hashCode());
        temp = Double.doubleToLongBits(this.topExtent);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        final PolylineComparison other = (PolylineComparison) obj;

        final double[] extents = this.getExtents();
        final double[] otherExtents = other.getExtents();

        boolean isEqual = true;
        int counter = 0;
        for (int i = 0; i < extents.length; ++i) {
            final double lowerBound = extents[i] - INTERVAL_THRESHOLD;
            final double upperBound = extents[i] + INTERVAL_THRESHOLD;
            isEqual &= lowerBound <= otherExtents[i]
                    && otherExtents[i] <= upperBound;
            if (lowerBound <= otherExtents[i] && otherExtents[i] <= upperBound) {
                ++counter;
            }
        }
        isEqual = counter >= 4;
        final double[] dist = this.calcDistance(other);

        return isEqual && dist[0] < POLYLINE_DISTANCE;
    }

    /**
     * Reduces the number of points within a polygon be removing colinear points
     * 
     * @param polygon
     *            the polygon
     * @return the reduced polygon
     */
    private Polygon reducePoints(final Polygon polygon) {
        final double epsilon = 0.000001;
        final Point[] points = polygon.getPoints();
        if (points.length < 3) {
            return polygon;
        }
        final Polygon reduced = new Polygon(polygon.isInnerPolygon());
        int index = 0;
        do {
            final Point p = points[(index - 1 + points.length) % points.length];
            final Point t = points[index];
            final Point s = points[(index + 1) % points.length];
            if ((Math.abs(t.x - p.x) < epsilon && Math.abs(t.x - s.x) < epsilon)
                    || (Math.abs(t.y - p.y) < epsilon && Math.abs(t.y - s.y) < epsilon)
                    || (Math.abs((t.x - p.x) - (s.x - t.x)) < epsilon && Math
                            .abs((t.y - p.y) - (s.y - t.y)) < epsilon)) {
                if (index == 0
                        || (index == points.length - 1 && reduced.getPoints().length == 1)) {
                    reduced.addPoint(new Point(t.x, t.y));
                }
                index = (index + 1) % points.length;
                continue;
            }
            reduced.addPoint(new Point(t.x, t.y));
            index = (index + 1) % points.length;
        } while (index != 0);

        return reduced;
    }

    /**
     * Calculates the distance between two polylines
     * <p>
     * There can be distinguished between two distances: the minDistance and the
     * distance between corresponding segments
     * 
     * @param other
     *            the polyline to compare with
     * @return the distance
     */
    private double[] calcDistance(final PolylineComparison other) {
        final Polygon[] polyline = new Polygon[] { this.top, this.bottom,
                this.left, this.right };
        final Polygon[] polylineOther = new Polygon[] { other.top,
                other.bottom, other.left, other.right };
        double minDistances = 0;
        double correspondingDistances = 0;
        for (int i = 0; i < polyline.length; ++i) {
            minDistances += this.calcSumOfMinDistances(polyline[i],
                    polylineOther[i]);
            correspondingDistances += this.calcCorrespondingSegmentsDistance(
                    polyline[i], polylineOther[i]);
        }
        return new double[] { minDistances, correspondingDistances };
    }

    /**
     * Calculates the minimum distance
     * <p>
     * This is done by taking into account the distance to the nearest segment
     * 
     * @param polyline
     *            the polyline
     * @param polylineOther
     *            the polyline to calculate the distance to
     * @return this sum of min distances
     */
    private double calcSumOfMinDistances(final Polygon polyline,
            final Polygon polylineOther) {
        final Point middlePoint = polyline.getMiddlePoint();
        final Point middlePointOther = polylineOther.getMiddlePoint();
        final AffineTransform at = new AffineTransform();
        at.translate(middlePoint.x - middlePointOther.x, middlePoint.y
                - middlePointOther.y);
        polylineOther.transform(at);
        double overallDistance = 0;
        final Line[] lines = polyline.getLines();
        final Line[] linesOther = polylineOther.getLines();
        for (final Line line : lines) {
            final Point middle = new Point(
                    (line.getStartX() + line.getEndX()) / 2,
                    (line.getStartY() + line.getEndY()) / 2);
            double minDist = Double.MAX_VALUE;
            for (final Line lineOther : linesOther) {
                final Point middleOther = new Point(
                        (lineOther.getStartX() + lineOther.getEndX()) / 2,
                        (lineOther.getStartY() + lineOther.getEndY()) / 2);
                final double tmpDist = middle.distance(middleOther);
                minDist = tmpDist < minDist ? tmpDist : minDist;

            }
            overallDistance += minDist;
        }
        return overallDistance;
    }

    /**
     * Calculates the distance between corresponding segments
     * <p>
     * This is done by taking into account the distance corresponding segment of
     * the other polyline
     * 
     * @param polyline
     *            the polyline
     * @param polylineOther
     *            the polyline to calculate the distance to
     * @return this sum distances to corresponding segments
     */
    private double calcCorrespondingSegmentsDistance(final Polygon polyline,
            final Polygon polylineOther) {
        final Point middlePoint = polyline.getMiddlePoint();
        final Point middlePointOther = polylineOther.getMiddlePoint();
        final AffineTransform at = new AffineTransform();
        at.translate(middlePoint.x - middlePointOther.x, middlePoint.y
                - middlePointOther.y);
        polylineOther.transform(at);
        double distance = 0;
        final Line[] lines = polyline.getLines();
        final Line[] linesOther = polylineOther.getLines();
        final double stepWidth = linesOther.length / (double) lines.length;

        for (int i = 0, j = 0; i < lines.length && j < linesOther.length; ++i, j = (int) (i * stepWidth)) {
            final Point middle = new Point(
                    (lines[i].getStartX() + lines[i].getEndX()) / 2,
                    (lines[i].getStartY() + lines[i].getEndY()) / 2);
            final Point middleOther = new Point(
                    (linesOther[j].getStartX() + linesOther[j].getEndX()) / 2,
                    (linesOther[j].getStartY() + linesOther[j].getEndY()) / 2);
            distance += middle.distance(middleOther);
        }

        return distance;
    }

    /**
     * Factory class to create this ValidationApproach.
     */
    public static class Factory implements A_ValidationApproach.Factory {
        @Override
        public A_ValidationApproach createFeature(final byte[] p, final int w,
                final int h) {
            return new PolylineComparison(p, w, h);
        }
    }
}
