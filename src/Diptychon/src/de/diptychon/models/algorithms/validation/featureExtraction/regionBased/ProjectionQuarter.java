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
package de.diptychon.models.algorithms.validation.featureExtraction.regionBased;

import java.util.Arrays;

/**
 * This class provides the functionality to calculate the projections for a
 * partitioned binarized image
 */
public class ProjectionQuarter {

    /**
     * The value of a foreground pixel
     */
    private static final int FOREGROUND = 255;

    /**
     * the vertical projection of the upper left half
     */
    private final int[] verticalTopLeft;

    /**
     * the vertical projection of the upper right half
     */
    private final int[] verticalTopRight;

    /**
     * the vertical projection of the lower left half
     */
    private final int[] verticalBottomLeft;

    /**
     * the vertical projection of the lower right half
     */
    private final int[] verticalBottomRight;

    /**
     * the horizontal projection of the upper left half
     */
    private final int[] horizontalTopLeft;

    /**
     * the horizontal projection of the upper right half
     */
    private final int[] horizontalTopRight;

    /**
     * the horizontal projection of the lower left half
     */
    private final int[] horizontalBottomLeft;

    /**
     * the horizontal projection of the lower right half
     */
    private final int[] horizontalBottomRight;

    /**
     * Creates a new Projection for a binarized image
     * 
     * @param pixels
     *            the pixels of the image
     * @param width
     *            the width of the image
     * @param height
     *            the height of the image
     */
    public ProjectionQuarter(final int[] pixels, final int width,
            final int height) {
        final int width2 = width >> 1;
        final int offsetWidth2 = (int) Math.round(width / 2.d);
        final int height2 = height >> 1;
        final int offsetHeight2 = (int) Math.round(height / 2.d);

        this.verticalTopLeft = new int[width2];

        this.verticalTopRight = new int[offsetWidth2];

        this.verticalBottomLeft = new int[width2];

        this.verticalBottomRight = new int[offsetWidth2];

        this.horizontalTopLeft = new int[height2];

        this.horizontalTopRight = new int[height2];

        this.horizontalBottomLeft = new int[offsetHeight2];

        this.horizontalBottomRight = new int[offsetHeight2];

        for (int y = 0; y < height; ++y) {
            final int offset = y * width;
            for (int x = 0; x < width; ++x) {
                final int index = x + offset;
                final int pixel = pixels[index];
                if (pixel == FOREGROUND) {
                    if (x < width2 && y < height2) {
                        ++this.horizontalTopLeft[y];
                        ++this.verticalTopLeft[x];
                    } else if (width2 <= x && y < height2) {
                        ++this.horizontalTopRight[y];
                        ++this.verticalTopRight[x - width2];
                    } else if (x < width2 && height2 <= y) {
                        ++this.horizontalBottomLeft[y - height2];
                        ++this.verticalBottomLeft[x];
                    } else if (width2 <= x && height2 <= y) {
                        ++this.horizontalBottomRight[y - height2];
                        ++this.verticalBottomRight[x - width2];
                    } else {
                        assert (false);
                    }
                }
            }
        }
    }

    /**
     * Gets the projections
     * 
     * @return the projections
     */
    public int[][] getProjections() {
        return new int[][] { this.horizontalTopLeft, this.horizontalTopRight,
                this.horizontalBottomLeft, this.horizontalBottomRight,
                this.verticalTopLeft, this.verticalTopRight,
                this.verticalBottomLeft, this.verticalBottomRight };
    }

    /**
     * Checks whether a projection has no holes
     * 
     * @param projection
     *            the projection
     * @return <code>true</code> if the projection has no holes,
     *         <code>false</code> otherwise
     */
    private boolean isClosed(final int[] projection) {
        boolean isClosed = true;

        for (int i = 1; i < projection.length - 1; ++i) {
            if (projection[i - 1] != 0 && projection[i + 1] != 0) {
                isClosed &= (projection[i] != 0);
                if (!isClosed) {
                    break;
                }
            }
        }
        return isClosed;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.horizontalBottomLeft);
        result = prime * result + Arrays.hashCode(this.horizontalBottomRight);
        result = prime * result + Arrays.hashCode(this.horizontalTopLeft);
        result = prime * result + Arrays.hashCode(this.horizontalTopRight);
        result = prime * result + Arrays.hashCode(this.verticalBottomLeft);
        result = prime * result + Arrays.hashCode(this.verticalBottomRight);
        result = prime * result + Arrays.hashCode(this.verticalTopLeft);
        result = prime * result + Arrays.hashCode(this.verticalTopRight);
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

        final ProjectionQuarter other = (ProjectionQuarter) obj;

        final int[][] projectionsFirst = this.getProjections();
        final int[][] projectionsOther = other.getProjections();

        boolean isEqual = true;
        for (int i = 0; i < projectionsFirst.length; ++i) {
            final boolean isClosed = this.isClosed(projectionsFirst[i]);
            final boolean isClosedOther = other.isClosed(projectionsOther[i]);

            isEqual &= !(isClosed ^ isClosedOther);
        }

        return isEqual;
    }
}
