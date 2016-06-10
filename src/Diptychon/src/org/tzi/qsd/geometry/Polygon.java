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

import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

import org.tzi.qsd.moments.Centroid;

import javafx.scene.shape.Line;
import de.diptychon.DiptychonLogger;

/**
 * Polygons are defined by a number of vertices. They may be either open
 * (polyline) or closed (polygons). In the case of closed polygons the number of
 * line segments (edges) corresponds to number of vertices; open polylines have
 * one line segment less than the number of vertices
 */
public class Polygon implements AffineTransformable, Cloneable {

    private static enum Orientation {
        CLOCKWISE, COUNTERCLOCKWISE
    };

    private Vector<Point> polygon = new Vector<Point>();

    private boolean closed = false;

    private boolean innerPolygon = false;

    private boolean singlePixel = false;

    private Point[] points = null;

    private Line[] lines = null;

    // public Polygon()
    // {
    // }

    /**
     * Constructs a polygon without vertices. The polygon is open, so that
     * further vertices may be added later with the
     * {@link de.diptychon.models.featureExtraction.qsd.geometry.Polygon#addPoint(Point point)}
     * method.
     */
    public Polygon(final boolean inner) {
        this.innerPolygon = inner;
    }

    /**
     * Constructs a polygon with a first vertice. The polygon is open, so that
     * further vertices may be added later with the
     * {@link de.diptychon.models.featureExtraction.qsd.geometry.Polygon#addPoint(Point point)}
     * method.
     * 
     * @param point
     *            the first vertice of this polygon
     */
    public Polygon(final Point point, final boolean inner) {
        this.innerPolygon = inner;
        this.addPoint(point);
    }

    /**
     * Constructs a polygon with some vertices. The polygon is open, so that
     * further vertices may be added later with the
     * {@link de.diptychon.models.featureExtraction.qsd.geometry.Polygon#addPoint(Point point)}
     * method.
     * 
     * @param points
     *            an array of vertices of this polygon
     */
    public Polygon(final Point[] points, final boolean inner) {
        this(points);
        this.innerPolygon = inner;
    }

    public Polygon(final Point[] points) {
        for (final Point point : points) {
            this.addPoint((Point) point.clone());
        }
    }

    /**
     * Adds a vertice to this plygon.
     * 
     * @param point
     *            the vertice to be added
     */
    public void addPoint(final Point point) {
        if (this.closed) {
            throw new IllegalStateException(
                    "Points cannot be added to closed polygons");
        }
        point.setPolygon(this);
        this.polygon.add(point);
        this.points = null;
        this.lines = null;
    }

    public void setPoints(final Vector<Point> points) {
        this.polygon = null;
        this.polygon = points;
    }

    public void resetPoints() {
        this.points = null;
    }

    public void setToSinglePixel(final boolean sp) {
        this.singlePixel = sp;
    }

    public boolean surroundsSinglePixel() {
        return this.singlePixel;
    }

    /**
     * Removes a vertice from this polygon.
     * 
     * @param point
     *            the vertice to be removed
     */
    public void removePoint(final Point point) {
        if (this.polygon.remove(point)) {
            this.points = null;
            this.lines = null;
        }
    }

    /**
     * Closes this polygon. Closing a polygon that is already closed has no
     * effect. Note that this operation cannot simply be undone by "opening" the
     * polygon again. This is impossible since it is not defined where the
     * polygon should be opened. Theoretically, each of the lines of the
     * polygons could meant to be cut. Instead, it is possible to create the
     * respective polyline by applying the
     * {@link de.diptychon.models.featureExtraction.qsd.geometry.Polygon#getPolyline(int start, int end)}
     * method with the start and the end point of this polygon.
     */
    public void close() {
        this.closed = true;
        if (this.polygon.size() > 2) {
            final Orientation orientation = this.determineOrientation();
            final boolean inner = this.isInnerPolygon();
            if ((!inner && orientation.equals(Orientation.CLOCKWISE))
                    || (inner && orientation
                            .equals(Orientation.COUNTERCLOCKWISE))) {
                Collections.reverse(this.polygon);
                Collections.rotate(this.polygon, 1);
            }
        }
        this.points = null;
        this.lines = null;
    }

    /**
     * Returns the vertices this polygon consists of.
     * 
     * @return the vertices this polygon consists of
     */
    public Point[] getPoints() {
        if (this.points == null) {
            this.points = this.polygon.toArray(new Point[0]);
        }
        return this.points;
    }

    public int[] getHighestIndizes() {
        int maxX = 0;
        int maxY = 0;
        this.getPoints();
        for (final Point p : this.points) {
            if (p.x > maxX) {
                maxX = (int) p.x;
            }
            if (p.y > maxY) {
                maxY = (int) p.y;
            }
        }
        return new int[] { maxX, maxY };
    }

    public Point getMiddlePoint() {
        final Line[] newLines = this.getLines();
        int sumX = 0;
        int sumY = 0;
        for (final Line line : newLines) {
            final Point middle = new Point(
                    (line.getStartX() + line.getEndX()) / 2,
                    (line.getStartY() + line.getEndY()) / 2);

            sumX += middle.x;
            sumY += middle.y;
        }
        return new Point(sumX / newLines.length, sumY / newLines.length);
    }

    /**
     * Returns the edges this polygon consists of.
     * 
     * @return the edges this polygon consists of
     */
    public Line[] getLines() {
        if (this.lines == null) {
            this.lines = new Line[this.polygon.size() > 1 ? (this.polygon
                    .size() - (this.closed ? 0 : 1)) : 0];
            for (int index = 0; index < this.polygon.size() - 1; index++) {
                final Point point = this.polygon.get(index);
                final Point successor = this.polygon.get(index + 1);
                this.lines[index] = new Line(point.x, point.y, successor.x,
                        successor.y);
            }
            if (this.polygon.size() > 1 && this.closed) {
                final Point last = this.polygon.lastElement();
                final Point first = this.polygon.firstElement();
                this.lines[this.lines.length - 1] = new Line(last.x, last.y,
                        first.x, first.y);
            }
        }
        return this.lines;
    }

    public void resetLines() {
        this.lines = null;
    }

    /**
     * Determines the length of this polyline or polygon. That is, the sum of
     * the lengths of all included lines.
     * 
     * @return the length of this polygon
     */
    public double length() {
        double perimeter = 0;
        for (final Line line : this.getLines()) {
            perimeter += Math.sqrt(Math.pow(line.getStartX() - line.getEndX(),
                    2) + Math.pow(line.getStartY() - line.getEndY(), 2));
            ;
        }
        return perimeter;
    }

    /**
     * Returns whether this polygon is a polygon which is inside an other
     * 
     * @return <code>true</code> if the polygon is inside an other,
     *         <code>false</code> otherwise
     */
    public boolean isInnerPolygon() {
        return this.innerPolygon;
    }

    public void setInner() {
        this.innerPolygon = true;
    }

    /**
     * Returns whether this polygon is closed.
     * 
     * @return <code>true</code> if the polygon is closed, <code>false</code>
     *         otherwise
     */
    public boolean isClosed() {
        return this.closed;
    }

    public int[][] toArray(final int height, final int width) {
        final int[][] converted = new int[height][width];

        for (final Point point : this.polygon) {
            converted[(int) point.getY()][(int) point.getX()] = 1;
        }

        return converted;
    }

    public int[] toSimpleArray(final int width, final int height) {
        final int[] converted = new int[(1 + height) * (1 + width)];
        Arrays.fill(converted, 255);

        for (final Point point : this.polygon) {
            converted[(int) point.y * width + (int) point.x] = 0;
        }

        return converted;
    }

    /**
     * Returns whether this polygon is simple. That is, it is determined whether
     * any pair of segments intersects. If two lines intersect the polygon is
     * complex, otherwise simple. Note that two adjacent lines may intersect in
     * their common point without violating the definition of simplicity.
     * 
     * @return <code>true</code> if the polygon is simple, <code>false</code>
     *         otherwise
     */
    public boolean isSimple() {
        final Line[] newLines = this.getLines();
        for (int reference = 0; reference < newLines.length; reference++) {
            for (int comparison = reference + 2; comparison < newLines.length; comparison++) {
                final Line line1 = newLines[reference];
                final Line line2 = newLines[comparison];

                if (Line2D.linesIntersect(line1.getStartX(), line1.getStartY(),
                        line1.getEndX(), line1.getEndY(), line2.getStartX(),
                        line2.getStartY(), line2.getEndX(), line2.getEndY())
                        && !(reference == 0 && comparison == newLines.length - 1)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determines whether a poin lies within the area of this polygon. Note that
     * this method does not check whether a point is a vertice of the polygon.
     * In order to find out whether a point is a vertice, get the list of all
     * vertices with
     * {@link de.diptychon.models.featureExtraction.qsd.geometry.Polygon#getPoints()}
     * and compare them by iteration.
     * <p>
     * The underlying algorithm is fairly simple. A line that is parallel with
     * the x-axis and passes the point is created. Afterwards, it is determined
     * how many of the edges of the polygons intersect with this auxiliary line.
     * If the number of intersections on the left hand side of the point is odd
     * it is located within the polygon. It is outside if the number is even.
     * 
     * @param point
     *            the point to be checked
     * @return <code>true</code> if the point lies within the polygon,
     *         <code>false</code> otherwise
     */
    public boolean contains(final Point point) {
        boolean odd = false;
        final Line[] newLines = this.getLines();
        for (final Line line : newLines) {
            if (checkLeftHorizontalIntersection(line, point)) {
                odd = !odd;
            }
        }
        if (!this.closed && newLines.length > 0) {
            final Line line = new Line(this.polygon.lastElement().x,
                    this.polygon.lastElement().y,
                    this.polygon.firstElement().x,
                    this.polygon.firstElement().y);
            if (checkLeftHorizontalIntersection(line, point)) {
                odd = !odd;
            }
        }
        return odd;
    }

    /**
     * Creates a new flipped polygon from the original one. It may be chosen
     * whether the flipping is conduced vertically or horizontally.
     * 
     * @param vertical
     *            <code>true</code> if the polygon is supposed to be flipped
     *            vertically, <code>false</code> otherwise
     * @return the flipped polygon
     */
    public Polygon getFlipped(final boolean vertical) {
        final Point center = new Centroid(this);
        final Polygon flipped = new Polygon(this.innerPolygon);
        for (final Point point : this.polygon) {
            final double x;
            final double y;
            if (vertical) {
                x = 2 * center.getX() - point.getX();
                y = point.getY();
            } else {
                x = point.getX();
                y = 2 * center.getY() - point.getY();
            }
            flipped.addPoint(new Point(x, y));
        }
        if (this.closed) {
            flipped.close();
        }
        return flipped;
    }

    /**
     * Creates the polyline between two points of this polygon. If the polygon
     * is itself a polygon (i.e., open) the start point much be less than the
     * end point, otherwise an exception is thrown.
     * 
     * @param start
     *            the end point of the polyline to be created
     * @param end
     *            the end point of the polyline to be created
     * @return the polyline between the start and end point
     * @throws IllegalArgumentException
     *             if the polygon is open and the start point is greater than
     *             the end point
     */
    public Polygon getPolyline(final int start, int end) {
        if (start >= end) {
            if (!this.isClosed()) {
                throw new IllegalArgumentException(
                        "Start point may not be greater than end point for polylines");
            }
            end += this.polygon.size();
        }
        final Polygon polyline = new Polygon(this.innerPolygon);
        for (int index = start; index <= end; index++) {
            final Point point = this.polygon.get(index % this.polygon.size());
            polyline.addPoint((Point) point.clone());
        }
        return polyline;
    }

    @Override
    public void transform(final AffineTransform transform) {
        for (final Point point : this.polygon) {
            point.transform(transform);
        }
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            DiptychonLogger.error("{}", e);
        }
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.closed ? 1231 : 1237);
        result = prime * result + (this.innerPolygon ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(this.lines);
        result = prime * result + Arrays.hashCode(this.points);
        result = prime * result
                + ((this.polygon == null) ? 0 : this.polygon.hashCode());
        result = prime * result + (this.singlePixel ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Polygon)) {
            return false;
        }
        final Polygon other = (Polygon) o;
        if (this.closed != other.closed
                || this.polygon.size() != other.polygon.size()) {
            return false;
        }
        for (int index = 0; index < this.polygon.size(); index++) {
            final Point referencePoint = this.polygon.get(index);
            final Point comparisonPoint = other.polygon.get(index);
            if (!referencePoint.equals(comparisonPoint)) {
                return false;
            }
        }
        return true;
    }

    // Auxiliary methods applied when closing polygon:

    private Orientation determineOrientation() {
        if (!this.closed) {
            return null;
        }
        final double area = this.computeSignedDoubleArea();
        return (area < 0) ? Orientation.COUNTERCLOCKWISE
                : Orientation.CLOCKWISE;
    }

    private double computeSignedDoubleArea() {
        double sum = 0;
        for (int i = 0; i < this.polygon.size(); ++i) {
            final Point first = this.polygon.get(i);
            final Point second = this.polygon
                    .get((i + 1) % this.polygon.size());
            sum += first.x * second.y - second.x * first.y;
        }
        return sum;
    }

    // Auxiliary methods applied when determining whether a point lies within a
    // polygon:

    private static boolean checkLeftHorizontalIntersection(final Line line,
            final Point point) {
        return checkLineExtrinsicallyLeftOfPoint(line, point)
                && checkHorizontalIntersection(point, line);
    }

    private static boolean checkHorizontalIntersection(final Point point,
            final Line line) {
        return checkOrder(line.getStartY(), point.getY(), line.getEndY());
    }

    private static boolean checkOrder(final double a, final double b,
            final double c) {
        return (a < b && b <= c || a >= b && b > c);
    }

    private static boolean checkLineExtrinsicallyLeftOfPoint(final Line line,
            final Point point) {
        final double extrinsicHeight = line.getEndY() - line.getStartY(); // this.end.getY()
                                                                          // -
                                                                          // this.start.getY();
        final double extrinsicWidth = line.getEndX() - line.getStartX();
        final double verticalPosition = point.getY() - line.getStartY();
        final double horizontalPosition = point.getX() - line.getStartX();
        final double horizontalIntersection = verticalPosition
                / extrinsicHeight * extrinsicWidth;
        return horizontalIntersection < horizontalPosition;
    }
}
