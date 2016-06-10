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
package org.tzi.qsd.geometry.generalisation;

import java.util.ArrayList;
import java.util.Vector;

import org.tzi.qsd.geometry.Configuration;
import org.tzi.qsd.geometry.Point;
import org.tzi.qsd.geometry.PointNx2D;
import org.tzi.qsd.geometry.Polygon;

import de.diptychon.models.algorithms.contourExtraction.Contour;

public class Correction {
    // @formatter:off
    private static final int displayFactor = 10;

    private static final double DELTA = .5;

    private static final Point P00 = new Point(DELTA, DELTA);

    private static final Point P01 = new Point(-DELTA, DELTA);

    private static final Point P02 = new Point(0, DELTA);

    private static final Point P10 = new Point(DELTA, -DELTA);

    private static final Point P11 = new Point(-DELTA, -DELTA);

    private static final Point P12 = new Point(0, -DELTA);

    private static final Point P20 = new Point(DELTA, 0);

    private static final Point P21 = new Point(-DELTA, 0);

    private static final PointNx2D P00P10 = new PointNx2D(new Point[] { P00, P10 });

    private static final PointNx2D P01P00 = new PointNx2D(new Point[] { P01, P00 });

    private static final PointNx2D P10P11 = new PointNx2D(new Point[] { P10, P11 });

    private static final PointNx2D P11P01 = new PointNx2D(new Point[] { P11, P01 });

    private static final PointNx2D P11P01P00 = new PointNx2D(new Point[] { P11, P01, P00 });

    private static final PointNx2D P10P11P01 = new PointNx2D(new Point[] { P10, P11, P01 });

    private static final PointNx2D P00P10P11 = new PointNx2D(new Point[] { P00, P10, P11 });

    private static final PointNx2D P01P00P10 = new PointNx2D(new Point[] { P01, P00, P10 });

    private static final PointNx2D P11P01P00P10 = new PointNx2D(new Point[] { P11, P01, P00, P10 });

    private static final PointNx2D P10P11P01P00 = new PointNx2D(new Point[] { P10, P11, P01, P00 });

    private static final PointNx2D P00P10P11P01 = new PointNx2D(new Point[] { P00, P10, P11, P01 });

    private static final PointNx2D P01P00P10P11 = new PointNx2D(new Point[] { P01, P00, P10, P11 });

    private static final Object[][] ROW_COL_MAPPING = {{P01P00, P01P00P10, P01, P01P00, P21, P11P01, P11, null},
                    {P01P00, P01P00P10, P01, P11P01P00, P01, P11P01, null, P01P00P10P11},
                    {P00, P00P10, P02, P01P00, P01, null, P00P10, P00P10P11},
                    {P00, P01P00P10, P00, P01P00, null, P00P10P11P01, P00P10, P00P10P11},
                    {P20, P00P10, P00, null, P10P11, P10P11P01, P10, P10P11},
                    {P10, P00P10, null, P10P11P01P00, P10P11, P10P11P01, P10, P00P10P11},
                    {P10, null, P11P01, P11P01P00, P11, P11P01, P12, P10P11},
                    {null, P11P01P00P10, P11P01, P11P01P00, P11, P10P11P01, P11, P10P11}, };

    // @formatter:on

    private final boolean useDisplayFactor;

    public Correction() {
        this(false);
    }

    public Correction(final boolean udp) {
        this.useDisplayFactor = udp;
    }

    public Configuration correct(final Configuration configuration) {
        for (final Polygon polygon : configuration.getPolygons()) {
            if (this.useDisplayFactor && polygon.surroundsSinglePixel()) {
                final Point[] tmp = polygon.getPoints();
                for (int i = 0; i < tmp.length; ++i) {
                    tmp[i].multiply(displayFactor);
                }
                continue;
            }
            final Point[] points = polygon.getPoints();
            final Vector<Point> newPoints = new Vector<Point>(points.length * 2);

            Point a = points[points.length - 1];
            Point b = points[0];
            Point c = points[1];
            Point[] tmp = this.correctPoint(0, a, b, c);
            assert (tmp != null);
            for (final Point p : tmp) {
                newPoints.add(p);
            }
            for (int i = 1; i < points.length - 1; ++i) {
                a = points[i - 1];
                b = points[i];
                c = points[i + 1];

                tmp = this.correctPoint(i, a, b, c);
                assert (tmp != null);
                for (final Point p : tmp) {
                    newPoints.add(p);
                }
            }
            a = points[points.length - 2];
            b = points[points.length - 1];
            c = points[0];
            tmp = this.correctPoint(points.length - 1, a, b, c);
            assert (tmp != null);
            for (final Point p : tmp) {
                newPoints.add(p);
            }

            for (int i = newPoints.size() - 1; i > 0; --i) {
                if (newPoints.get(i).equals(newPoints.get(i - 1))) {
                    newPoints.remove(i);
                }
            }
            if (newPoints.get(0).equals(newPoints.get(newPoints.size() - 1))) {
                newPoints.remove(newPoints.size() - 1);
            }

            polygon.resetPoints();
            polygon.setPoints(newPoints);
        }
        return configuration;
    }

    public Contour correct(final Contour contour) {
        final Point[] points = contour.getPoints();
        final ArrayList<Point> newPoints = new ArrayList<>(points.length * 2);

        Point a = points[points.length - 1];
        Point b = points[0];
        Point c = points[1];
        Point[] tmp = this.correctPoint(0, a, b, c);
        assert (tmp != null);
        for (final Point p : tmp) {
            newPoints.add(p);
        }
        for (int i = 1; i < points.length - 1; ++i) {
            a = points[i - 1];
            b = points[i];
            c = points[i + 1];

            tmp = this.correctPoint(i, a, b, c);
            assert (tmp != null);
            for (final Point p : tmp) {
                newPoints.add(p);
            }
        }
        a = points[points.length - 2];
        b = points[points.length - 1];
        c = points[0];
        tmp = this.correctPoint(points.length - 1, a, b, c);
        assert (tmp != null);
        for (final Point p : tmp) {
            newPoints.add(p);
        }

        for (int i = newPoints.size() - 1; i > 0; --i) {
            if (newPoints.get(i).equals(newPoints.get(i - 1))) {
                newPoints.remove(i);
            }
        }
        if (newPoints.get(0).equals(newPoints.get(newPoints.size() - 1))) {
            newPoints.remove(newPoints.size() - 1);
        }

        contour.resetPoints();
        contour.setPoints(newPoints);

        return contour;
    }

    private Point[] correctPoint(final int i, final Point a, final Point b,
            final Point c) {
        final int row = this.getRow(a, b);
        final int col = this.getColumn(b, c);
        final Object obj = ROW_COL_MAPPING[row][col];
        if (obj instanceof Point) {
            final Point insert = (Point) b.clone();
            insert.moveBy(((Point) obj).getX(), ((Point) obj).getY());
            if (this.useDisplayFactor) {
                insert.multiply(Correction.displayFactor);
            }
            return new Point[] { insert };
        } else if (obj instanceof PointNx2D) {
            return this.insertPoints(i, a, b, c, (PointNx2D) obj);
        }
        return null;
    }

    private Point[] insertPoints(final int index, final Point a, final Point b,
            final Point c, final PointNx2D pN) {
        final Point[] points = pN.getPoints();
        final Point[] tmp = new Point[points.length];
        for (int i = 0; i < points.length; ++i) {
            final Point insert = (Point) b.clone();
            insert.moveBy(points[i].getX(), points[i].getY());
            if (this.useDisplayFactor) {
                insert.multiply(Correction.displayFactor);
            }
            tmp[i] = insert;
        }
        return tmp;
    }

    private int getRow(final Point a, final Point b) {
        if (b.x == a.x && b.y > a.y) {
            return 0;
        }
        if (b.x > a.x && b.y > a.y) {
            return 1;
        }
        if (b.x > a.x && b.y == a.y) {
            return 2;
        }
        if (b.x > a.x && b.y < a.y) {
            return 3;
        }
        if (b.x == a.x && b.y < a.y) {
            return 4;
        }
        if (b.x < a.x && b.y < a.y) {
            return 5;
        }
        if (b.x < a.x && b.y == a.y) {
            return 6;
        }
        if (b.x < a.x && b.y > a.y) {
            return 7;
        }
        return -1;
    }

    private int getColumn(final Point b, final Point c) {
        if (b.x == c.x && b.y > c.y) {
            return 0;
        }
        if (b.x < c.x && b.y > c.y) {
            return 1;
        }
        if (b.x < c.x && b.y == c.y) {
            return 2;
        }
        if (b.x < c.x && b.y < c.y) {
            return 3;
        }
        if (b.x == c.x && b.y < c.y) {
            return 4;
        }
        if (b.x > c.x && b.y < c.y) {
            return 5;
        }
        if (b.x > c.x && b.y == c.y) {
            return 6;
        }
        if (b.x > c.x && b.y > c.y) {
            return 7;
        }
        return -1;
    }
}
