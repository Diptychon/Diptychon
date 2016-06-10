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
 * The seven invariant moments by Hu (1962).
 */
public class HuMoments {

    private static final int NUMBER_OF_MOMENTS = 7;

    private final double[] c;

    /**
     * Constructs the Hu moments from pre-computed values.
     * 
     * @param c
     *            the pre-computed values of the seven invariant moment
     */
    public HuMoments(final double[] pC) {
        this.c = pC;
    }

    /**
     * Constructs the Hu moments for a given polygon.
     * 
     * @param polygon
     *            the polygon to be examined
     */
    public HuMoments(final Polygon polygon) {
        final Area area = new Area(polygon);
        final Centroid centroid = new Centroid(polygon, area);

        final double n02 = new NormedCenteredMoment(polygon, 0, 2, centroid,
                area).getMoment();
        final double n03 = new NormedCenteredMoment(polygon, 0, 3, centroid,
                area).getMoment();
        final double n12 = new NormedCenteredMoment(polygon, 1, 2, centroid,
                area).getMoment();
        final double n11 = new NormedCenteredMoment(polygon, 1, 1, centroid,
                area).getMoment();
        final double n20 = new NormedCenteredMoment(polygon, 2, 0, centroid,
                area).getMoment();
        final double n21 = new NormedCenteredMoment(polygon, 2, 1, centroid,
                area).getMoment();
        final double n30 = new NormedCenteredMoment(polygon, 3, 0, centroid,
                area).getMoment();

        this.c = new double[NUMBER_OF_MOMENTS];
        this.c[0] = this.calculateC1(n02, n20);
        this.c[1] = this.calculateC2(n02, n11, n20);
        this.c[2] = this.calculateC3(n03, n12, n21, n30);
        this.c[3] = this.calculateC4(n03, n12, n21, n30);
        this.c[4] = this.calculateC5(n03, n12, n21, n30);
        this.c[5] = this.calculateC6(n02, n03, n11, n12, n20, n21, n30);
        this.c[6] = this.calculateC7(n03, n12, n21, n30);
    }

    public double calculateDistance(final HuMoments other) {
        double distance = 0;
        for (int index = 0; index < this.c.length; index++) {
            distance += Math.pow(this.c[index] - other.c[index], 2);
        }
        return Math.sqrt(distance);
    }

    /**
     * Returns the values of the seven invariant moments.
     * 
     * @return the values of the seven invariant moment
     */
    public double[] getC() {
        return this.c;
    }

    private double calculateC1(final double n02, final double n20) {
        return n20 + n02;
    }

    private double calculateC2(final double n02, final double n11,
            final double n20) {
        return Math.pow(n20 - n02, 2) + 4 * Math.pow(n11, 2);
    }

    private double calculateC3(final double n03, final double n12,
            final double n21, final double n30) {
        return Math.pow(n30 - 3 * n12, 2) + Math.pow(3 * n21 - n03, 2);
    }

    private double calculateC4(final double n03, final double n12,
            final double n21, final double n30) {
        return Math.pow(n30 + n12, 2) + Math.pow(n21 + n03, 2);
    }

    private double calculateC5(final double n03, final double n12,
            final double n21, final double n30) {
        return (n30 - 3 * n12) * (n30 + n12)
                * (Math.pow(n30 + n12, 2) - 3 * Math.pow(n21 + n03, 2))
                + (3 * n21 - n03) * (n21 + n03)
                * (3 * Math.pow(n30 + n12, 2) - Math.pow(n21 + n03, 2));
    }

    private double calculateC6(final double n02, final double n03,
            final double n11, final double n12, final double n20,
            final double n21, final double n30) {
        return (n20 + n02) * (Math.pow(n30 + n12, 2) - Math.pow(n21 + n03, 2))
                + 4 * n11 * (n30 + n12) * (n21 + n03);
    }

    private double calculateC7(final double n03, final double n12,
            final double n21, final double n30) {
        return (3 * n21 - n03) * (n30 + n12)
                * (Math.pow(n30 + n12, 2) - 3 * Math.pow(n21 + n03, 2))
                + (n30 - 3 * n12) * (n21 + n03)
                * (3 * Math.pow(n30 + n12, 2) - Math.pow(n21 + n03, 2));
    }
}
