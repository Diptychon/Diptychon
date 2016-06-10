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

import javafx.scene.shape.Rectangle;
import de.diptychon.models.algorithms.A_ValidationApproach;
import de.diptychon.models.algorithms.contourExtraction.ContourSet;
import de.diptychon.models.algorithms.contourExtraction.ContourTracer;
import de.diptychon.models.algorithms.validation.featureExtraction.regionBased.ProjectionHalf;
import de.diptychon.models.misc.ImageUtils;

/**
 * This class is used to extract Skeleton and to compare different skeletons
 */
public class SkeletonComparison extends A_ValidationApproach {

    /**
     * Projection of the skeleton
     */
    private final ProjectionHalf projectionHalf;

    /**
     * The number of holes within the skeleton
     */
    private final int numOfHoles;

    /**
     * Creates a new skeleton for a binarized image
     * 
     * @param pixels
     *            the pixels of the image
     * @param w
     *            the width of the image
     * @param h
     *            the height of the image
     */
    public SkeletonComparison(final byte[] pixels, final int w, final int h) {
        final Rectangle mask = new Rectangle(0, 0, w, h);
        final byte[] cropped = ImageUtils.autoCrop(pixels, mask);
        this.pixels = cropped;
        this.width = (int) mask.getWidth();
        this.height = (int) mask.getHeight();
        this.projectionHalf = new ProjectionHalf(this.pixels, this.width,
                this.height);
        final ContourTracer ct = new ContourTracer(this.pixels, this.width,
                this.height);
        final ContourSet cs = ct.getContours();
        this.numOfHoles = cs.getNumberOfInnerContours();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.numOfHoles;
        result = prime
                * result
                + ((this.projectionHalf == null) ? 0 : this.projectionHalf
                        .hashCode());
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
        final SkeletonComparison other = (SkeletonComparison) obj;

        return (this.projectionHalf.equals(other.projectionHalf))
                && (this.numOfHoles == other.numOfHoles);
    }

    /**
     * Factory class to create this ValidationApproach.
     */
    public static class Factory implements A_ValidationApproach.Factory {
        @Override
        public A_ValidationApproach createFeature(final byte[] p, final int w,
                final int h) {
            return new SkeletonComparison(p, w, h);
        }
    }
}
