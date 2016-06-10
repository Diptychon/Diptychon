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
 * An abstract class representing morphological operations. Concrete
 * implementations realise {@link qsd.geometry.morphology.Dilatation} and
 * {@link qsd.geometry.morphology.Erosion} with arbitrary masks.
 */
public abstract class MorphologicalOperation {

    private static final boolean F = false;

    private static final boolean T = true;

    /**
     * A diamond mask for morphological operations.
     */
    public static final boolean[][] MASK_DIAMOND = { { F, T, F }, { T, T, T },
            { F, T, F } };

    /**
     * A rectangle mask for morphological operations.
     */
    public static final boolean[][] MASK_RECTANGLE = { { F, F, F },
            { F, T, T }, { F, T, T } };

    /**
     * An octagon mask for morphological operations.
     */
    public static final boolean[][] MASK_OCTAGON = { { T, T, T, T, T },
            { T, T, T, T, T }, { T, T, T, T, T }, { T, T, T, T, T },
            { T, T, T, T, T }, };

    private final GrayImage binaryImage;

    protected MorphologicalOperation(final GrayImage image) {
        this(image, MASK_OCTAGON);
    }

    protected MorphologicalOperation(final GrayImage image,
            final boolean[][] mask) {
        this.binaryImage = this.morph(image, mask);
    }

    /**
     * Returns the binary raster image resulting from the applied morphological
     * operation.
     * 
     * @return the resulting binary raster image
     */
    public GrayImage getImage() {
        return this.binaryImage;
    }

    private GrayImage morph(final GrayImage source, final boolean[][] mask) {
        final int width = source.getWidth();
        final int height = source.getHeight();
        final byte[] pixels = new byte[width * height];
        for (int y = 0; y < height; y++) {
            final int offset = y * width;
            for (int x = 0; x < width; x++) {
                final int index = offset + x;
                pixels[index] = this.morph(source, x, y, mask);
            }
        }
        return new GrayImage(pixels, width, height);
    }

    protected abstract byte morph(final GrayImage image, final int x,
            final int y, final boolean[][] mask);

    protected boolean isInRange(final GrayImage image, final int x, final int y) {
        return (x >= 0 && y >= 0 && x < image.getWidth() && y < image
                .getHeight());
    }
}
