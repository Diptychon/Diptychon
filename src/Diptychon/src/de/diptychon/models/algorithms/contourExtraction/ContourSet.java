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
import java.util.LinkedList;
import java.util.Stack;

import org.tzi.qsd.geometry.Configuration;
import org.tzi.qsd.geometry.Point;
import org.tzi.qsd.geometry.generalisation.Correction;

import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import de.diptychon.models.algorithms.RectangularRegion;
import de.diptychon.models.misc.PartialGlyphShape;

/**
 * Represents a set of outer and inner contours
 */
public class ContourSet {

    /**
     * The outer Contours
     */
    private ArrayList<OuterContour> outerContours = null;

    /**
     * The innter contours
     */
    private ArrayList<InnerContour> innerContours = null;

    /**
     * Creates a new ContourSet
     */
    public ContourSet() {
        this.outerContours = new ArrayList<OuterContour>();
        this.innerContours = new ArrayList<InnerContour>();
    }

    /**
     * Gets the number of outer contours
     * 
     * @return the number of outer contours
     */
    public int getNumberOfOuterContours() {
        return this.outerContours.size();
    }

    /**
     * Gets the number of innter contours
     * 
     * @return the number of innter contours
     */
    public int getNumberOfInnerContours() {
        return this.innerContours.size();
    }

    public ArrayList<OuterContour> getOuterContours() {
        return this.outerContours;
    }

    public ArrayList<InnerContour> getInnerContours() {
        return this.innerContours;
    }

    /**
     * Determines the largest outer contour and store points of all others (to
     * be able to grasp (remove) them somewhere else)
     * 
     * @return stack of points of all contours besides of the largest one
     */
    public Stack<Point> getPointsOfAllContoursButTheLargestOne() {
        int longest = 0;
        int soFarLargest = -1;
        Stack<Point> toBeRemoved = new Stack<Point>();

        for (int i = 0; i < this.outerContours.size(); i++) {
            if (this.outerContours.get(i).getLength() > longest) {
                if (soFarLargest != -1)
                    toBeRemoved.push(this.outerContours.get(soFarLargest)
                            .getPoint(0)); // jetzt doch weg, da doch nicht die
                                           // Längste Kontur

                longest = this.outerContours.get(i).getLength();
                soFarLargest = i;
            } else {
                toBeRemoved.push(this.outerContours.get(i).getPoint(0)); // nicht
                                                                         // die
                                                                         // längste
                                                                         // Kontur,
                                                                         // also
                                                                         // auf
                                                                         // den
                                                                         // Wegwerf-Stack
                                                                         // tun
            }
        }
        return toBeRemoved;
    }

    /**
     * Gets the regions (by editing the reference regions) and the average glyph
     * height. Regions has to be at least of height minHeight and of widht
     * minWidth
     * 
     * @param regions
     *            the reference to the regions
     * @param minHeight
     *            the minimum height
     * @param minWidth
     *            the minimum width
     * @return the average glyph height
     */
    public double getRegions(final LinkedList<RectangularRegion> regions,
            final int minHeight, final int minWidth) {
        long averageGlyphHeight = 0;
        for (final OuterContour oc : this.outerContours) {
            final RectangularRegion tmp = oc.getBoundingboxAsRegion();
            if (tmp.y2 - tmp.y1 + 1 > minHeight
                    && tmp.x2 - tmp.x1 + 1 > minWidth) {
                averageGlyphHeight += tmp.y2 - tmp.y1 + 1;
                regions.add(tmp);
            }
        }
        return averageGlyphHeight / (double) regions.size();
    }

    /**
     * Gets all points which are part of the outer contours
     * 
     * @return all points which are part of the outer contours
     */
    public ArrayList<Point> getOuterPoints() {
        final ArrayList<Point> points = new ArrayList<Point>();
        for (final OuterContour oc : this.outerContours) {
            for (final Point p : oc.points) {
                points.add(p);
            }
        }
        return points;
    }

    /**
     * Gets the contours as partial glyph shapes
     * 
     * @return the partial glyph shapes
     */
    public ArrayList<PartialGlyphShape> getGlyphShape() {
        final double moveBack = -0.5;
        this.moveBy(moveBack, moveBack);
        final ArrayList<PartialGlyphShape> glyphShapes = new ArrayList<>(
                this.outerContours.size());
        for (final OuterContour oc : this.outerContours) {
            final Correction correction = new Correction(false);
            correction.correct(oc);
            Shape outerPolygon = new Polygon();
            ObservableList<Double> polygonPointList = ((Polygon) outerPolygon)
                    .getPoints();
            for (final Point p : oc.points) {
                polygonPointList.add(p.getX());
                polygonPointList.add(p.getY());
            }
            for (int i = this.innerContours.size() - 1; i >= 0; --i) {
                final InnerContour ic = this.innerContours.get(i);
                final Point p0 = ic.points.get(0);
                final Point2D tmp = new Point2D(p0.x, p0.y);
                if (!outerPolygon.contains(tmp)) {
                    continue;
                }
                final Polygon innerPolygon = new Polygon();
                polygonPointList = innerPolygon.getPoints();
                for (final Point p : ic.points) {
                    polygonPointList.add(p.getX());
                    polygonPointList.add(p.getY());
                }
                outerPolygon = Shape.subtract(outerPolygon, innerPolygon);
                this.innerContours.remove(i);
            }
            glyphShapes.add(new PartialGlyphShape(outerPolygon));
        }
        return glyphShapes;
    }

    /**
     * Adds an outercontour
     * 
     * @param oc
     *            the outercontour
     */
    public void addContour(final OuterContour oc) {
        this.outerContours.add(oc);
    }

    /**
     * Adds an innercontour
     * 
     * @param ic
     *            the innercontour
     */
    public void addContour(final InnerContour ic) {
        this.innerContours.add(ic);
    }

    /**
     * Moves contours by distance
     * 
     * @param dx
     *            the distance in x direction
     * @param dy
     *            the distance in y direction
     */
    public void moveBy(final double dx, final double dy) {
        for (final OuterContour oc : this.outerContours) {
            oc.moveBy(dx, dy);
        }
        for (final InnerContour ic : this.innerContours) {
            ic.moveBy(dx, dy);
        }
    }

    /**
     * Converts the outercontours to polygons
     * 
     * @return the outercontours as configuration of polygons
     */
    public Configuration outerToPolygons() {
        final Configuration config = new Configuration();
        for (final OuterContour oc : this.outerContours) {
            config.addPolygon(oc.toPolygon(false));
        }
        return config;
    }
}
