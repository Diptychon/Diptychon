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
package de.diptychon.models.algorithms.contourExtraction;

import java.util.ArrayList;

import org.tzi.qsd.geometry.Point;
import org.tzi.qsd.geometry.Polygon;

import javafx.scene.shape.Rectangle;
import de.diptychon.models.algorithms.RectangularRegion;

/**
 * Representation of the contour of an object.
 */
public abstract class Contour {
    /**
     * The initial size of the contour point buffer
     */
    private static final int INITIAL_SIZE = 50;

    /**
     * The found contour points.
     */
    protected ArrayList<Point> points = null;

    /**
     * Default constructor.
     */
    public Contour() {
        this.points = new ArrayList<Point>(Contour.INITIAL_SIZE);
    }

    /**
     * Gets the contour points
     * 
     * @return the contour points.
     */
    public Point[] getPoints() {
        Point[] tmp = new Point[this.points.size()];
        tmp = this.points.toArray(tmp);
        return tmp;
    }

    /**
     * Liefert den ersten (irgendeinen) Punkt der Kontur
     * 
     * @returns an arbitrary point of this contour.
     */
    public Point getPoint(int i) {
        return this.points.get(i);
    }

    /**
     * Adds a point to the contour
     * 
     * @param p
     *            the point to be added.
     */
    public void addPoint(final Point p) {
        this.points.add(p);
    }

    /**
     * Empties the list of contour points.
     */
    public void resetPoints() {
        this.points = null;
    }

    /**
     * Sets the contour points to x
     * 
     * @param newPoints
     *            The new contour points.
     */
    public void setPoints(final ArrayList<Point> newPoints) {
        this.points = newPoints;
    }

    /**
     * Gets the length of the contour
     * 
     * @return The length of the contour.
     */
    public int getLength() {
        return this.points.size();
    }

    /**
     * Gets the boundingbox of the contour.
     * 
     * @return The boundingbox as Region
     */
    public RectangularRegion getBoundingboxAsRegion() {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (final Point p : this.points) {
            minX = Math.min(minX, (int) p.x);
            maxX = Math.max(maxX, (int) p.x);
            minY = Math.min(minY, (int) p.y);
            maxY = Math.max(maxY, (int) p.y);
        }
        return new RectangularRegion(minX, minY, maxX, maxY);
    }

    /**
     * Gets the Rectangle boundingBox of the contour.
     * 
     * @return The boundingbox as Rectangle
     */
    public Rectangle getBoundingboxAsRectangle() {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (final Point p : this.points) {
            minX = Math.min(minX, (int) p.x);
            maxX = Math.max(maxX, (int) p.x);
            minY = Math.min(minY, (int) p.y);
            maxY = Math.max(maxY, (int) p.y);
        }
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Moves the contour by delta
     * 
     * @param dx
     *            The delta the contour should be moved in x direction
     * @param dy
     *            The delta the contour should be moved in y direction
     */
    public void moveBy(final double dx, final double dy) {
        for (final Point p : this.points) {
            p.x += dx;
            p.y += dy;
        }
    }

    /**
     * Converts the contours to a polygon
     * 
     * @param inner
     *            True if the inner contours should be converted to a polygon
     *            false otherwise
     * @return A Polygon which corresponds to the contour points
     */
    public Polygon toPolygon(final boolean inner) {
        final double movePoint = 0.5;
        final Polygon polygon = new Polygon(inner);
        if (this.points.size() == 1) {
            final Point p = this.points.get(0);
            final double x = p.getX();
            final double y = p.getY();
            polygon.addPoint(new org.tzi.qsd.geometry.Point(
                    x - movePoint, y - movePoint));
            polygon.addPoint(new org.tzi.qsd.geometry.Point(
                    x + movePoint, y - movePoint));
            polygon.addPoint(new org.tzi.qsd.geometry.Point(
                    x + movePoint, y + movePoint));
            polygon.addPoint(new org.tzi.qsd.geometry.Point(
                    x - movePoint, y + movePoint));
            polygon.close();
            return polygon;
        }
        for (final Point p : this.points) {
            polygon.addPoint(new org.tzi.qsd.geometry.Point(
                    p.x, p.y));
        }
        polygon.close();
        // return polygon;
        return Contour.reducePoints(Contour.cutOffAnnexes(polygon));
    }

    /**
     * Cuts of annexes from a polygon
     * 
     * @param polygon
     *            the polygon
     * @return the reduced polygon
     */
    // CHECKSTYLE:OFF
    private static Polygon cutOffAnnexes(Polygon polygon)
    // CHECKSTYLE:ON
    {
        final org.tzi.qsd.geometry.Point[] points = polygon
                .getPoints();
        if (points.length < 3) {
            return polygon;
        }
        for (int current = 0; current < points.length - 1; current++) {
            final org.tzi.qsd.geometry.Point reference = points[current];
            for (int forward = current + 1; forward < points.length; forward++) {
                final org.tzi.qsd.geometry.Point comparison = points[forward];
                if (reference.equals(comparison)) {
                    final int distance = forward - current;
                    final int remaining = points.length - distance;
                    if (remaining > distance) {
                        // CHECKSTYLE:OFF
                        polygon = polygon.getPolyline((forward + 1)
                                % points.length, current);
                    } else {
                        polygon = polygon.getPolyline((current + 1)
                                % points.length, forward);
                        // CHECKSTYLE:ON
                    }
                    polygon.close();
                    return cutOffAnnexes(polygon);
                }
            }
        }
        return polygon;
    }

    /**
     * Reduces the number of points within a polygon be removing colinear
     * points.
     * 
     * @param polygon
     *            the polygon
     * @return the reduced polygon
     */
    private static Polygon reducePoints(final Polygon polygon) {
        final double epsilon = 0.000001;
        final org.tzi.qsd.geometry.Point[] points = polygon
                .getPoints();
        if (points.length < 3) {
            return polygon;
        }
        final Polygon reduced = new Polygon(polygon.isInnerPolygon());
        int index = 0;
        do {
            final org.tzi.qsd.geometry.Point p = points[(index - 1 + points.length)
                    % points.length];
            final org.tzi.qsd.geometry.Point t = points[index];
            final org.tzi.qsd.geometry.Point s = points[(index + 1)
                    % points.length];
            if ((Math.abs(t.x - p.x) < epsilon && Math.abs(t.x - s.x) < epsilon)
                    || (Math.abs(t.y - p.y) < epsilon && Math.abs(t.y - s.y) < epsilon)
                    || (Math.abs((t.x - p.x) - (s.x - t.x)) < epsilon && Math
                            .abs((t.y - p.y) - (s.y - t.y)) < epsilon)) {
                index = (index + 1) % points.length;
                continue;
            }
            reduced.addPoint(new org.tzi.qsd.geometry.Point(
                    t.x, t.y));
            index = (index + 1) % points.length;
        } while (index != 0);
        reduced.close();
        return reduced;
    }
}
