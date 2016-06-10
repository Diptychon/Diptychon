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

import de.diptychon.models.misc.GrayImage;

/**
 * This class provides the functionality to calculate the projections for a
 * partitioned binarized image
 */
public class ProjectionHalf {

    /**
     * the vertical projection of the upper half
     */
    private final int[] verticalTop;

    /**
     * the vertical projection of the lower half
     */
    private final int[] verticalBottom;

    /**
     * the horizontal projection of the left half
     */
    private final int[] horizontalLeft;

    /**
     * the horizontal projection of the right half
     */
    private final int[] horizontalRight;

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
    public ProjectionHalf(final byte[] pixels, final int width, final int height) {
        final double overlapping = 0.15;
        final int width2 = width >> 1; // factor 2
        final int height2 = height >> 1; // factor 2

        this.verticalTop = new int[width];

        this.verticalBottom = new int[width];

        this.horizontalLeft = new int[height];

        this.horizontalRight = new int[height];

        final int heightBorder = (int) (height2 * overlapping);
        final int widthBorder = (int) (width2 * overlapping);

        for (int y = 0; y < height; ++y) {
            final int offset = y * width;
            for (int x = 0; x < width; ++x) {
                final int index = x + offset;
                final int pixel = pixels[index];
                if (pixel == GrayImage.BLACK) {
                    if (y < height2 + heightBorder) {
                        ++this.verticalTop[x];
                    } else if (height2 - heightBorder <= y) {
                        ++this.verticalBottom[x];
                    } else {
                        assert (false);
                    }
                    if (x < width2 + widthBorder) {
                        ++this.horizontalLeft[y];
                    } else if (width2 - widthBorder <= x) {
                        ++this.horizontalRight[y];
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
        return new int[][] { this.horizontalLeft, this.horizontalRight,
                this.verticalTop, this.verticalBottom };
    }

    /**
     * Counts the number of hills and valleys within the projection
     * 
     * @param projection
     *            the projection
     * @return the number of hills and valleys
     */
    private int[] numOfClusters(final int[] projection) {
        boolean hasPreviosForeground = false;
        boolean hasPreviosBackground = false;
        int foregroundClusterCounter = 0;
        int backgroundClusterCounter = 0;
        for (int i = 1; i < projection.length - 1; ++i) {
            if (projection[i] != 0) {
                if (!hasPreviosForeground) {
                    ++foregroundClusterCounter;
                }
                hasPreviosBackground = false;
                hasPreviosForeground = true;
            } else if (projection[i] == 0) {
                if (!hasPreviosBackground) {
                    ++backgroundClusterCounter;
                }
                hasPreviosBackground = true;
                hasPreviosForeground = false;
            }
        }
        return new int[] { foregroundClusterCounter, backgroundClusterCounter };
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.horizontalLeft);
        result = prime * result + Arrays.hashCode(this.horizontalRight);
        result = prime * result + Arrays.hashCode(this.verticalBottom);
        result = prime * result + Arrays.hashCode(this.verticalTop);
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

        final ProjectionHalf other = (ProjectionHalf) obj;

        final int[][] projectionsFirst = this.getProjections();
        final int[][] projectionsOther = other.getProjections();

        boolean isEqual = true;
        for (int i = 0; i < projectionsFirst.length; ++i) {
            final int[] numOfClusters = this.numOfClusters(projectionsFirst[i]);
            final int[] numOfClustersOther = other
                    .numOfClusters(projectionsOther[i]);

            isEqual &= (numOfClusters[0] == numOfClustersOther[0] & numOfClusters[1] == numOfClustersOther[1]);
        }

        return isEqual;
    }
}
