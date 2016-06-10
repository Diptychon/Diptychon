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
package de.diptychon.models.algorithms.validation.featureExtraction;

import org.tzi.qsd.description.PolygonalCourse;

import de.diptychon.models.algorithms.validation.featureExtraction.regionBased.PixelCorrelation;

/**
 * Abstract class which provides basic functionality for the representation of a
 * Feature.
 */
public abstract class A_Feature implements Cloneable {

    /**
     * The feature vector which stores all components for calculated features.
     */
    protected double[] featureVector;

    /**
     * Gets the feature vector.
     * 
     * @return The feature vector.
     */
    public double[] getFeatureVector() {
        return this.featureVector;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final A_Feature newObj = (A_Feature) super.clone();
        System.arraycopy(this.featureVector, 0, newObj.featureVector, 0,
                this.featureVector.length);
        return newObj;
    }

    /**
     * Factory method for creating new features.
     */
    public abstract static class Factory {
        /**
         * Creates a feature which needs the polygonal course as input.
         * 
         * @param course
         *            The polygonal course the feature extraction is based on.
         * @return The feature.
         */
        public A_Feature createFeature(final PolygonalCourse course) {
            return null;
        }

        /**
         * Creates a feature which needs the pixels of a binarized image as
         * input.
         * 
         * @param pixels
         *            The pixels of the image
         * @param width
         *            The width of the image
         * @param height
         *            The height of the image
         * @return The feature.
         */
        public PixelCorrelation createFeature(final int[] pixels,
                final int width, final int height) {
            return null;
        }
    }
}
