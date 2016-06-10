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
 * Based on: Workinggroup Qualitative Shape Description (University Bremen)
 */
package org.tzi.qsd.geometry;

public class PointNx2D {

    private final Point[] points;

    public PointNx2D(final double d1, final double d2) {
        this.points = new Point[1];
        this.points[0] = new Point(d1, d2);
    }

    public PointNx2D(final PointNx2D p1, final PointNx2D p2) {
        this.points = new Point[2];
        this.points[0] = p1.points[0];
        this.points[1] = p2.points[0];
    }

    public PointNx2D(final Point[] p) {
        this.points = p;
    }

    public Point[] getPoints() {
        return this.points;
    }
}
