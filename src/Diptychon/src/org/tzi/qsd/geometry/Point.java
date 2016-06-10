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
package org.tzi.qsd.geometry;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * A point is the basic geometric entity within the QSD framework. The position
 * of each point in the two-dimensional plane is defined by its x- and
 * y-coordinates.
 * <p>
 * Two points can define a
 * {@link de.diptychon.models.featureExtraction.qsd.geometry.Line} segment,
 * multiple points a
 * {@link de.diptychon.models.featureExtraction.qsd.geometry.Polygon}.
 */
@SuppressWarnings("serial")
public class Point extends Point2D.Double implements AffineTransformable,
        Cloneable {

    private static final double DOUBLE_THRESHOLD = 1E-5;

    private Polygon polygon;

    /**
     * Constructs a point defined by the provided coordinates.
     * 
     * @param x
     *            the x-coordinates of this point
     * @param y
     *            the y-coordinates of this point
     */
    public Point(final double x, final double y) {
        super(x, y);
    }

    /**
     * Constructs a pointdefined by the provided point.
     * 
     * @param point
     *            the coordinates of this point
     */
    public Point(final Point2D point) {
        this(point.getX(), point.getY());
    }

    /**
     * Sets the polygon this point belongs to.
     * 
     * @param polygon
     *            the new polygon
     */
    protected void setPolygon(final Polygon polygon) {
        this.polygon = polygon;
    }

    /**
     * Returns the polygon this point belongs to.
     * 
     * @return the polygon this point belongs to
     */
    public Polygon getPolygon() {
        return this.polygon;
    }

    public void moveBy(final double dx, final double dy) {
        this.x += dx;
        this.y += dy;
    }

    public void multiply(final int factor) {
        this.x *= factor;
        this.y *= factor;
    }

    /**
     * Computes the distance to another point.
     * 
     * @param point
     *            another point
     * @return the distance to the other point
     */
    public double distance(final Point point) {
        return Point2D.distance(this.x, this.y, point.getX(), point.getY());
    }

    @Override
    public void transform(final AffineTransform transform) {
        transform.transform(this, this);
    }

    @Override
    public Object clone() {
        return super.clone();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Point)) {
            return false;
        }
        final Point other = (Point) o;
        return this.distance(other) < DOUBLE_THRESHOLD;
    }

    @Override
    public String toString() {
        return "(" + this.getX() + ", " + this.getY() + ")";
    }
}
