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
 * The moment determining the area of a polygon.
 */
public class Area extends Moment {

    /**
     * Constructs the area for a given polygon.
     * 
     * @param polygon
     *            the polygon to be examined regarding its area
     */
    public Area(final Polygon polygon) {
        super(polygon, 0, 0);
    }
}
