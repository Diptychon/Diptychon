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
 *
 * Based on: Arne Schuldt
 */
package org.tzi.qsd.geometry.generalisation;

import java.awt.geom.Line2D;

import org.tzi.qsd.geometry.Configuration;
import org.tzi.qsd.geometry.Point;
import org.tzi.qsd.geometry.Polygon;

import javafx.scene.shape.Line;

/**
 * Polygonal approximation according to the algorithm of Mitzias & Mertzios
 * (1994). The approximation factor defines the maximal approximation error in
 * relation to the length of the polygon. As an example, a factor of 100 means
 * that the approximation error may not exceed one per cent of the length of the
 * polygon's outline.
 */
public class Approximation extends Generalisation {

    private static final int STANDARD_APPROXIMATION_FACTOR = 100;

    private double factor = STANDARD_APPROXIMATION_FACTOR;

    /**
     * Constructs an approximation with a standard approximation factor.
     */
    public Approximation() {
    }

    /**
     * Constructs an approximation with a given approximation factor.
     * 
     * @param f
     *            the approximation factor
     */
    public Approximation(final double f) {
        this.factor = f;
    }

    @Override
    public Configuration generalise(final Polygon p) {
        if (!p.isSimple()) {
            throw new IllegalArgumentException(
                    "Only simple polygons can be approximated.");
        }
        Polygon polygon = (Polygon) p.clone();
        if (polygon.isClosed()) {
            final int mostDistantPoint = findMostDistantPoint(polygon);
            polygon = shift(polygon, mostDistantPoint);
        }
        final double startParameter = polygon.length() / this.factor;
        return new Configuration(approximateSimple(polygon, startParameter));
    }

    private static Polygon approximateSimple(final Polygon polygon,
            final double parameter) {
        if (parameter < 1.0) {
            return polygon;
        }
        final Polygon approximated = approximate(polygon, parameter);
        if (!approximated.isSimple()) {
            return approximateSimple(polygon, parameter * 0.95);
        }
        return approximated;
    }

    private static Polygon approximate(final Polygon polygon,
            final double parameter) {
        final Point[] points = polygon.getPoints();
        if (points.length < 3) {
            return polygon;
        }
        int start = 0;
        final Point first = (Point) points[start].clone();
        final Polygon approximated = new Polygon(first,
                polygon.isInnerPolygon());
        // for (int end = start + 2; end <= points.length; end = start + 2)
        for (int end = start + 2; end < points.length; end = start + 2) {
            // for (; end <= points.length; end++)
            for (; end < points.length; ++end) {
                final Polygon subpolygon = polygon.getPolyline(start, end
                        % points.length);
                // final Polygon subpolygon = polygon.getPolyline(start, end);
                final int index = findNextPoint(subpolygon, parameter);
                if (index != -1) {
                    start += index;
                    final Point point = (Point) points[start].clone();
                    approximated.addPoint(point);
                    break;
                }
            }
            if (end >= points.length) {
                break;
            }
        }
        if (!polygon.isClosed()) {
            final Point last = (Point) points[points.length - 1].clone();
            approximated.addPoint(last);
        } else {
            approximated.close();
        }
        return approximated;
    }

    private static int findNextPoint(final Polygon subpolygon,
            final double parameter) {
        if (subpolygon == null) {
            return -1;
        }
        final Point[] points = subpolygon.getPoints();
        final Line reference = new Line(points[0].x, points[0].y,
                points[points.length - 1].x, points[points.length - 1].y);
        int maxErrorIndex = 1;
        double maxError = 0;
        for (int index = maxErrorIndex; index < points.length; index++) {
            final Point comparison = points[index];
            final double error = Line2D.ptSegDist(reference.getStartX(),
                    reference.getStartY(), reference.getEndX(),
                    reference.getEndY(), comparison.getX(), comparison.getY());

            if (error > maxError) {
                maxError = error;
                maxErrorIndex = index;
            }
        }
        return (maxError > parameter) ? maxErrorIndex : -1;
    }
}
