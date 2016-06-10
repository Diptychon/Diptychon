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

import org.tzi.qsd.geometry.Polygon;

/**
 * Normal centered moments of polygons according to Steger (1996).
 */
public class NormedCenteredMoment extends Moment {

    /**
     * Constructs a normal centered moment for a given polygon.
     * 
     * @param polygon
     *            the polygon to be examined
     * @param p
     *            p-value of the moment
     * @param q
     *            q-value of the moment
     */
    public NormedCenteredMoment(final Polygon polygon, final int p, final int q) {
        this(polygon, p, q, new Centroid(polygon), new Area(polygon));
    }

    /**
     * Constructs a normal centered moment for a given polygon.
     * 
     * @param polygon
     *            the polygon to be examined
     * @param p
     *            p-value of the moment
     * @param q
     *            q-value of the moment
     * @param centroid
     *            the centroid of the given polygon
     */
    public NormedCenteredMoment(final Polygon polygon, final int p,
            final int q, final Centroid centroid) {
        this(polygon, p, q, centroid, new Area(polygon));
    }

    /**
     * Constructs a normal centered moment for a given polygon.
     * 
     * @param polygon
     *            the polygon to be examined
     * @param p
     *            p-value of the moment
     * @param q
     *            q-value of the moment
     * @param centroid
     *            the centroid of the given polygon
     * @param area
     *            the area of the given polygon
     */
    public NormedCenteredMoment(final Polygon polygon, final int p,
            final int q, final Centroid centroid, final Area area) {
        super(polygon, p, q, centroid, area);
    }
}
