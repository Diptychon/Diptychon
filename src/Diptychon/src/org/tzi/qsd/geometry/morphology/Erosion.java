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
package org.tzi.qsd.geometry.morphology;

import de.diptychon.models.misc.GrayImage;

/**
 * An erosion operation for binary raster images.
 */
public class Erosion extends MorphologicalOperation {

    /**
     * Constructs an erosion for a given binary raster image with a standard
     * mask.
     * 
     * @param image
     *            the binary raster image to be eroded
     */
    public Erosion(final GrayImage image) {
        super(image);
    }

    /**
     * Constructs an erosion for a given binary raster image with a given mask.
     * 
     * @param image
     *            the binary raster image to be eroded
     * @param mask
     *            the mask to be applied
     */
    public Erosion(final GrayImage image, final boolean[][] mask) {
        super(image, mask);
    }

    @Override
    protected byte morph(final GrayImage image, final int x, final int y,
            final boolean[][] mask) {
        for (int maskY = 0; maskY < mask.length; maskY++) {
            final int imageY = y - 1 + maskY;
            for (int maskX = 0; maskX < mask[maskY].length; maskX++) {
                final int imageX = x - 1 + maskX;
                if (mask[maskY][maskX]) {
                    if (!this.isInRange(image, imageX, imageY)) {
                        return GrayImage.WHITE;
                    }
                    if (image.getPixel(imageX, imageY) == GrayImage.WHITE) {
                        return GrayImage.WHITE;
                    }
                }
            }
        }
        return GrayImage.BLACK;
    }
}
