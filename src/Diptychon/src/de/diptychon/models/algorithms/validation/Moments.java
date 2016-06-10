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

import java.util.Arrays;

import de.diptychon.models.algorithms.A_ValidationApproach;
import de.diptychon.models.misc.GrayImage;

/**
 * This class provides functionality to calculated the moments for a binarized
 * image up to order p,q
 */
public class Moments extends A_ValidationApproach {
    /**
     * Order P
     */
    private static final int P = 4;

    /**
     * Order Q
     */
    private static final int Q = 4;

    /**
     * Threshold to compare moments
     */
    private static final double MOMENTS_DISTANCE = 0.15;

    public static int getP() {
        return P;
    }

    public static int getQ() {
        return Q;
    }

    /**
     * The moments
     */
    private final double[] moments;

    public double[] getMoments() {
        return moments;
    }

    /**
     * Creates the feature for a binarized image
     * 
     * @param p
     *            the pixels of the image
     * @param w
     *            the width of the image
     * @param h
     *            the height of the image
     */
    public Moments(final byte[] p, final int w, final int h) {
        this.pixels = p;
        this.width = w;
        this.height = h;
        this.moments = new double[(P + 1) * (Q + 1)];
        this.calcMoments();
    }

    /**
     * Calculates the moments up to order p,q
     */
    private void calcMoments() {
        int index = 0;
        for (int p = 0; p <= P; ++p) {
            for (int q = 0; q <= Q; ++q) {
                this.moments[index] = this.normalCentralMoment(p, q);
                ++index;
            }
        }
    }

    /**
     * Calculates the moment for order p,q
     * 
     * @param p
     *            order p
     * @param q
     *            order q
     * @return the moment
     */
    private double moment(final int p, final int q) {
        double Mpq = 0.0;
        for (int y = 0; y < this.height; ++y) {
            final int offset = y * this.width;
            for (int x = 0; x < this.width; ++x) {
                final int index = x + offset;
                if (this.pixels[index] == GrayImage.BLACK) {
                    Mpq += Math.pow(x, p) * Math.pow(y, q);
                }
            }
        }
        return Mpq;
    }

    /**
     * Calculates the central moment for order p,q
     * 
     * @param p
     *            order p
     * @param q
     *            order q
     * @param xCtr
     *            the x center
     * @param yCtr
     *            the y center
     * @return the central moment with order p,q
     */
    private double centralMoment(final int p, final int q, final double xCtr,
            final double yCtr) {
        double cMpq = 0.0;
        for (int y = 0; y < this.height; ++y) {
            final int offset = y * this.width;
            for (int x = 0; x < this.width; ++x) {
                final int index = x + offset;
                if (this.pixels[index] == GrayImage.BLACK) {
                    cMpq += Math.pow(x - xCtr, p) * Math.pow(y - yCtr, q);
                }
            }
        }
        return cMpq;
    }

    /**
     * Calculates the normed, central moment for order p,q
     * 
     * @param p
     *            order p
     * @param q
     *            order q
     * @return the normed central moment with order p,q
     */
    private double normalCentralMoment(final int p, final int q) {
        final double m00 = this.moment(0, 0);
        final double xCtr = this.moment(1, 0) / m00;
        final double yCtr = this.moment(0, 1) / m00;

        final double norm = Math.pow(m00, (double) (p + q + 2) / 2);
        return this.centralMoment(p, q, xCtr, yCtr) / norm;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.moments);
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

        final Moments other = (Moments) obj;

        double sum = 0;
        for (int i = 0; i < this.moments.length; ++i) {
            sum += Math.abs(this.moments[i] - other.moments[i]);
        }
        return sum < MOMENTS_DISTANCE;
    }

    /**
     * Factory class to create this ValidationApproach.
     */
    public static class Factory implements A_ValidationApproach.Factory {
        @Override
        public A_ValidationApproach createFeature(final byte[] p, final int w,
                final int h) {
            return new Moments(p, w, h);
        }
    }
}
