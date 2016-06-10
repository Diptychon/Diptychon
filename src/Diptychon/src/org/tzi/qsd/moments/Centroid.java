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
 * The moment determining the centroid of a polygon.
 */
@SuppressWarnings("serial")
public class Centroid extends Point {

    /**
     * Constructs the standard centroid with coordinated (0, 0).
     */
    public Centroid() {
        super(0, 0);
    }

    /**
     * Constructs the centroid for a given polygon.
     * 
     * @param polygon
     *            the polygon to be examined regarding its centroid
     */
    public Centroid(final Polygon polygon) {
        this(polygon, new Area(polygon));
    }

    /**
     * Constructs the centroid for a given polygon. This constructor can be
     * applied if the area of the polygon has already been determined before. In
     * this case it is more sufficient to pass the area as an additional
     * parameter instead of computing it again.
     * 
     * @param polygon
     *            the polygon to be examined regarding its centroid
     * @param area
     *            the area of the polygon to be examined
     */
    public Centroid(final Polygon polygon, final Area area) {
        super(determineCentroid(polygon, area));
    }

    private static Point determineCentroid(final Polygon polygon,
            final Area area) {
        final double centroidX = new Moment(polygon, 1, 0).getMoment()
                / area.getMoment();
        final double centroidY = new Moment(polygon, 0, 1).getMoment()
                / area.getMoment();
        return new Point(centroidX, centroidY);
    }
}
