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
package de.diptychon.models.algorithms;

/**
 * Contains information about a region, e.g. bounding boxes, ...
 */
public class RectangularRegion implements Comparable<RectangularRegion> {
    /**
     * left boundary
     */
    public int x1;

    /**
     * right boundary
     */
    public int x2;

    /**
     * top boundary
     */
    public int y1;

    /**
     * bottom boundary
     */
    public int y2;

    /**
     * Creates a new region whit its boundaries
     * 
     * @param pX1
     *            left boundary
     * @param pY1
     *            top boundary
     * @param pX2
     *            right boundary
     * @param pY2
     *            bottom boundary
     */
    public RectangularRegion(final int pX1, final int pY1, final int pX2,
            final int pY2) {
        this.x1 = pX1;
        this.x2 = pX2;
        this.y1 = pY1;
        this.y2 = pY2;
    }

    /**
     * Compares two regions based on there left boundary
     * 
     * @param o
     *            the object to be compared
     * @return if this object is less than, equal to, or greater than the
     *         specified object
     */
    @Override
    public int compareTo(final RectangularRegion o) {
        if (this.x1 < o.x1) {
            return -1;
        }
        if (this.x1 == o.x1) {
            return 0;
        } else {
            return 1;
        }
    }
}
