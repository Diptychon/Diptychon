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
package de.diptychon.models.algorithms.validation;

import org.tzi.qsd.description.PolygonalCourse;
import org.tzi.qsd.geometry.Configuration;
import org.tzi.qsd.geometry.Polygon;
import org.tzi.qsd.geometry.generalisation.Approximation;
import org.tzi.qsd.geometry.generalisation.Correction;
import org.tzi.qsd.geometry.generalisation.Generalisation;

import de.diptychon.models.algorithms.A_ValidationApproach;
import de.diptychon.models.algorithms.contourExtraction.ContourSet;
import de.diptychon.models.algorithms.contourExtraction.ContourTracer;
import de.diptychon.models.algorithms.validation.featureExtraction.scopeBased.PolygonalBetweenness;
import de.diptychon.models.algorithms.validation.featureExtraction.scopeBased.PolygonalCurvature;
import de.diptychon.models.algorithms.validation.featureExtraction.scopeBased.PolygonalExtent;
import de.diptychon.models.algorithms.validation.featureExtraction.scopeBased.PolygonalExtremum;
import de.diptychon.models.misc.ImageUtils;

/**
 * This class is used to compare different polygons with each other using some
 * shape features
 */
public final class PolygonComparison extends A_ValidationApproach {
    /**
     * The value for the polygonal approximation
     */
    private static final int POLYGONAL_APPROXIMATION = 50;

    /**
     * the threshold for comparison
     */
    private static final double INTERVAL_THRESHOLD = 0.05d;

    /**
     * the extent for the upper half of the image
     */
    private final double topExtent;

    /**
     * the extent for the lower half of the image
     */
    private final double bottomExtent;

    /**
     * the extent for the left half of the image
     */
    private final double leftExtent;

    /**
     * the extent for the right half of the image
     */
    private final double rightExtent;

    private final int numOfPolygons;

    private Float perimeter;

    private int numOfInnerPolygons;

    /**
     * the polygon
     */
    private Polygon poly = null;

    private float area;

    /**
     * the polygon within the upper half of the image
     */
    private final Polygon top;

    /**
     * the polygon within the lower half of the image
     */
    private final Polygon bottom;

    /**
     * the polygon within the left half of the image
     */
    private final Polygon left;

    /**
     * the polygon within the right half of the image
     */
    private final Polygon right;

    public double getExtent() {
        return extent;
    }

    public double getCurvature() {
        return curvature;
    }

    public double getExtremum() {
        return extremum;
    }

    public double getBetweenness() {
        return betweenness;
    }

    private final double extent;

    private final double curvature;

    private final double extremum;

    private final double betweenness;

    /**
     * Creates a new polygon comparison for a binarized image
     * 
     * @param p
     *            the pixels of the image
     * @param w
     *            the width of the image
     * @param h
     *            the height of the image
     */
    public PolygonComparison(final byte[] p, final int w, final int h) {
        this.perimeter = (float) 0;
        this.width = w;
        this.height = h;
        this.pixels = p;

        byte[] cropped = ImageUtils.cropImage(this.pixels, this.width,
                this.height, 0, 0, this.width, this.height >> 1);
        ContourTracer ct = new ContourTracer(cropped, this.width,
                this.height >> 1);
        ContourSet cs = ct.getContours();
        this.top = this.getBiggestOuterPolygon(cs.outerToPolygons(), false);
        this.topExtent = new PolygonalExtent(
                this.getPolygonalCourseOfBiggestRegion(this.top))
                .getFeatureVector()[0];

        cropped = ImageUtils.cropImage(this.pixels, this.width, this.height, 0,
                this.height >> 1, this.width, this.height - (this.height >> 1));
        ct = new ContourTracer(cropped, this.width, this.height
                - (this.height >> 1));
        cs = ct.getContours();
        this.bottom = this.getBiggestOuterPolygon(cs.outerToPolygons(), false);
        this.bottomExtent = new PolygonalExtent(
                this.getPolygonalCourseOfBiggestRegion(this.bottom))
                .getFeatureVector()[0];

        cropped = ImageUtils.cropImage(this.pixels, this.width, this.height, 0,
                0, this.width >> 1, this.height);
        ct = new ContourTracer(cropped, this.width >> 1, this.height);
        cs = ct.getContours();
        this.left = this.getBiggestOuterPolygon(cs.outerToPolygons(), false);
        this.leftExtent = new PolygonalExtent(
                this.getPolygonalCourseOfBiggestRegion(this.left))
                .getFeatureVector()[0];

        cropped = ImageUtils
                .cropImage(this.pixels, this.width, this.height,
                        this.width >> 1, 0, this.width - (this.width >> 1),
                        this.height);
        ct = new ContourTracer(cropped, this.width - (this.width >> 1),
                this.height);
        cs = ct.getContours();
        this.right = this.getBiggestOuterPolygon(cs.outerToPolygons(), false);
        this.rightExtent = new PolygonalExtent(
                this.getPolygonalCourseOfBiggestRegion(this.right))
                .getFeatureVector()[0];

        cropped = p;
        ct = new ContourTracer(cropped, this.width, this.height);
        cs = ct.getContours();
        this.numOfPolygons = cs.getNumberOfOuterContours();
        this.numOfInnerPolygons = cs.getNumberOfInnerContours();
        this.poly = this.getBiggestOuterPolygon(cs.outerToPolygons(), true);
        float area = 0;
        for (int i = 0; i < this.poly.getPoints().length - 1; i++) {
            if (i == this.poly.getPoints().length - 1) {
                area += (this.poly.getPoints()[i].getX() * this.poly
                        .getPoints()[0].getY())
                        - (this.poly.getPoints()[0].getX() * this.poly
                                .getPoints()[i].getY());
            } else {
                area += (this.poly.getPoints()[i].getX() * this.poly
                        .getPoints()[i + 1].getY())
                        - (this.poly.getPoints()[i + 1].getX() * this.poly
                                .getPoints()[i].getY());
            }
        }
        this.area = Math.abs(area / 2);
        // this.perimeter = cs.getOuterPoints().size();
        this.extent = new PolygonalExtent(
                this.getPolygonalCourseOfBiggestRegion(this.poly))
                .getFeatureVector()[0];
        this.curvature = new PolygonalCurvature(
                this.getPolygonalCourseOfBiggestRegion(this.poly))
                .getFeatureVector()[0];
        this.extremum = new PolygonalExtremum(
                this.getPolygonalCourseOfBiggestRegion(this.poly))
                .getFeatureVector()[0];
        this.betweenness = new PolygonalBetweenness(
                this.getPolygonalCourseOfBiggestRegion(this.poly))
                .getFeatureVector()[0];
    }

    public int getNumOfPolygons() {
        return numOfPolygons;
    }

    public float getPerimeter() {
        return perimeter;
    }

    public float getArea() {
        return this.area;
    }

    public int getNumOfInnerPolygons() {
        return numOfInnerPolygons;
    }

    /**
     * Gets the polygonal course of a polygon
     * 
     * @param p
     *            the polygon
     * @return the polygonal course
     */
    private PolygonalCourse getPolygonalCourseOfBiggestRegion(final Polygon p) {
        final Configuration config = new Configuration(p);
        return new PolygonalCourse(config);
    }

    /**
     * Gets the biggest outer polygon within a configuration
     * 
     * @param config
     *            the configuration
     * @return the biggest polygon
     */
    private Polygon getBiggestOuterPolygon(Configuration config,
            boolean setPerimeter) {
        int biggest = 0;
        int index = 0;
        for (final Polygon p : config.getPolygons()) {
            if (p.length() > config.getPolygons()[biggest].length()) {
                biggest = index;
            }
            ++index;
        }

        final Correction correction = new Correction(false);
        // CHECKSTYLE:OFF
        config = correction.correct(config);
        // CHECKSTYLE:ON
        final Generalisation generalisation = new Approximation(
                POLYGONAL_APPROXIMATION);
        // CHECKSTYLE:OFF
        config = generalisation.generalise(config);
        // CHECKSTYLE:ON

        if (!(config.getPolygons().length > 0)) {
            System.out
                    .println("Warnung: Konnte keine Polygone für Merkmalsbestimmung finden.");
            return null;
        }
        if (setPerimeter) {
            ContourTracer ct = new ContourTracer(this.pixels, this.width,
                    this.height);
            ct.getContours();
            if (biggest > ct.getPerimeters().size() - 1) {
                System.out.println("Warnung: Konnte Umfang nicht bestimmen.");
            } else {
                this.perimeter = ct.getPerimeters().get(biggest);
            }
        }
        return config.getPolygons()[biggest];

    }

    /**
     * Gets the extents
     * 
     * @return the extents
     */
    private double[] getExtents() {
        return new double[] { this.topExtent, this.bottomExtent,
                this.leftExtent, this.rightExtent };
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((this.bottom == null) ? 0 : this.bottom.hashCode());
        long temp;
        temp = Double.doubleToLongBits(this.bottomExtent);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result
                + ((this.left == null) ? 0 : this.left.hashCode());
        temp = Double.doubleToLongBits(this.leftExtent);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result
                + ((this.right == null) ? 0 : this.right.hashCode());
        temp = Double.doubleToLongBits(this.rightExtent);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result
                + ((this.top == null) ? 0 : this.top.hashCode());
        temp = Double.doubleToLongBits(this.topExtent);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        final PolygonComparison other = (PolygonComparison) obj;

        final double[] extents = this.getExtents();
        final double[] otherExtents = other.getExtents();

        boolean isEqual = true;
        for (int i = 0; i < extents.length; ++i) {
            final double lowerBound = extents[i] * (1 - INTERVAL_THRESHOLD);
            final double upperBound = extents[i] * (1 + INTERVAL_THRESHOLD);
            isEqual &= lowerBound <= otherExtents[i]
                    && otherExtents[i] <= upperBound;
        }
        return isEqual;
    }

    /**
     * Factory class to create this ValidationApproach.
     */
    public static class Factory implements A_ValidationApproach.Factory {
        @Override
        public A_ValidationApproach createFeature(final byte[] p, final int w,
                final int h) {
            return new PolygonComparison(p, w, h);
        }
    }
}
