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
package org.tzi.qsd.moments;

import org.tzi.qsd.geometry.Point;
import org.tzi.qsd.geometry.Polygon;

/**
 * General moments of polygons according to Steger (1996).
 */
public class Moment {

    private final double moment;

    /**
     * Constructs a moment for a given polygon.
     * 
     * @param polygon
     *            the polygon to be examined
     * @param p
     *            p-value of the moment
     * @param q
     *            q-value of the moment
     */
    public Moment(final Polygon polygon, final int p, final int q) {
        this(polygon, p, q, new Centroid(), null);
    }

    protected Moment(final Polygon polygon, final int p, final int q,
            final Centroid centroid) {
        this(polygon, p, q, centroid, null);
    }

    protected Moment(final Polygon polygon, final int p, final int q,
            final Centroid centroid, final Area area) {
        this.moment = this.calculateMoment(polygon, p, q, centroid, area);
    }

    private double calculateMoment(final Polygon polygon, final int p,
            final int q, final Centroid centroid, final Area area) {
        final double factor = (p + q + 2) * (p + q + 1) * over(p + q, p);
        final Point[] points = polygon.getPoints();
        double sum = 0;
        for (int i = 0; i < points.length; i++) {
            final double pointX = points[i].x - centroid.x;
            final double pointY = points[i].y - centroid.y;
            final double successorX = points[(i + 1) % points.length].x
                    - centroid.x;
            final double successorY = points[(i + 1) % points.length].y
                    - centroid.y;
            double subsum = 0;
            for (int k = 0; k <= p; k++) {
                for (int l = 0; l <= q; l++) {
                    subsum += ((over(k + l, l) * over(p + q - k - l, q - l))
                            * Math.pow(successorX, k) * Math.pow(pointX, p - k)
                            * Math.pow(successorY, l) * Math.pow(pointY, q - l));
                }
            }
            sum += subsum * (successorX * pointY - pointX * successorY);
        }
        sum /= factor;
        if (area == null) {
            return sum;
        }
        return sum / Math.pow(area.getMoment(), 1 + (p + q) / 2.0);
    }

    /**
     * Returns the respective moment of this object.
     * 
     * @return the moment of this object
     */
    public double getMoment() {
        return this.moment;
    }

    private static double over(int a, int b) {
        double result = 1;
        while (b > 0) {
            result *= a / (double) b;
            a--;
            b--;
        }
        return result;
    }
}
