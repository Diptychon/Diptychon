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

import java.awt.Point;
import java.util.ArrayList;

import de.diptychon.models.misc.GrayImage;

/**
 * This class provides the functionality to calculate the projections for a
 * binarized image
 */
public class Projection {
    /**
     * the default estimated character width
     */
    private static final int DEFAULT_ESTIMATED_CHARACTER_WIDTH_HEURISTIC = 5;

    /**
     * the vertical projection
     */
    private final int[] vertical;

    /**
     * the horizontal projection
     */
    private final int[] horizontal;

    /**
     * Creates a new Projection for a binarized image
     * 
     * @param binaryImage
     *            the binarized image
     */
    public Projection(final GrayImage binaryImage) {
        final int width = binaryImage.getWidth();
        final int height = binaryImage.getHeight();
        this.vertical = new int[width];
        this.horizontal = new int[height];

        for (int y = 0; y < height; ++y) {
            final int offset = y * width;
            for (int x = 0; x < width; ++x) {
                if (binaryImage.getPixel(x + offset) == GrayImage.BLACK) {
                    ++this.vertical[x];
                    ++this.horizontal[y];
                }
            }
        }
    }

    /**
     * Finds gaps in the vertical projections
     * 
     * @param numberOfGaps
     *            the number of gaps which should be found
     * @param numOfElements
     *            the number of elements between 2 gaps
     * @param estimatedAverageCharacterWidth
     *            the estimated average character width * numberOfElements is
     *            used to calculate the distance to the next gap
     * @param height
     *            a
     * @return the indices of the gaps
     */
    public ArrayList<Point> findVerticalGaps(final int numberOfGaps,
            final ArrayList<Integer> numOfElements,
            int estimatedAverageCharacterWidth, final int height) {
        if (estimatedAverageCharacterWidth == -1) {
            estimatedAverageCharacterWidth = DEFAULT_ESTIMATED_CHARACTER_WIDTH_HEURISTIC;
        }
        final int[] verticalSmooth = new int[this.vertical.length];
        for (int i = 2; i < verticalSmooth.length - 2; ++i) {
            verticalSmooth[i] = (this.vertical[i - 2] + this.vertical[i - 1]
                    + this.vertical[i] + this.vertical[i + 1] + this.vertical[i + 2]) / 3;
        }

        int startThreshold = -1;
        ArrayList<Point> gaps;

        int minGap = 0;
        boolean resetMinGap = false;
        do {
            if (resetMinGap) {
                minGap = 0;
                resetMinGap = false;
            }
            int start = 0;
            boolean foundGap = false;
            gaps = new ArrayList<>();
            ++startThreshold;
            int indexGap = 0;
            if (numOfElements.size() == 0) {
                numOfElements.add(1);
            }
            int numOfElements1 = numOfElements.get(indexGap);
            int offset = 0;
            int i = 0;
            if (numOfElements.get(indexGap) != 0) {
                offset = numOfElements1 * estimatedAverageCharacterWidth;
                i += offset;
                i = Math.min(verticalSmooth.length - 1, i);
            }

            for (; i < verticalSmooth.length; ++i) {
                if (verticalSmooth[i] <= startThreshold & !foundGap) {
                    start = i;
                    foundGap = true;
                } else if (verticalSmooth[i] > startThreshold && foundGap) {
                    if (i - start >= minGap) {
                        gaps.add(new Point(start, i - 1));
                        ++indexGap;

                        if (indexGap >= numOfElements.size()) {
                            ++minGap;
                            --startThreshold;
                            gaps.clear();
                            resetMinGap = false;
                            break;
                        }
                        numOfElements1 = numOfElements.get(indexGap);
                        offset = 0;
                        if (numOfElements.get(indexGap) != 0) {
                            offset = numOfElements1
                                    * estimatedAverageCharacterWidth;
                            i += offset;
                            i = Math.min(verticalSmooth.length - 1, i);
                        }
                    }
                    foundGap = false;
                }
                resetMinGap = true;
            }
        } while (gaps.size() < numberOfGaps && startThreshold < height);

        if (height <= startThreshold) {
            gaps = new ArrayList<>();
        }

        return gaps;
    }

    /**
     * Finds gaps in the vertical projections
     * 
     * @param numberOfGaps
     *            the number of gaps which should be found
     * @return the indices of the gaps
     */
    public ArrayList<Point> findVerticalGaps(final int numberOfGaps) {
        final int[] verticalSmooth = new int[this.vertical.length];
        for (int i = 2; i < verticalSmooth.length - 2; ++i) {
            verticalSmooth[i] = (this.vertical[i - 2] + this.vertical[i - 1]
                    + this.vertical[i] + this.vertical[i + 1] + this.vertical[i + 2]) / 3;
        }

        int startThreshold = -1;
        ArrayList<Point> gaps;

        boolean foundGap = false;
        int start = -1;
        do {
            ++startThreshold;
            gaps = new ArrayList<>();
            int index = 0;
            for (final int i : verticalSmooth) {
                if (i <= startThreshold & !foundGap) {
                    start = index;
                    foundGap = true;
                } else if (i > startThreshold && foundGap) {
                    gaps.add(new Point(start, index - 1));
                    foundGap = false;
                }
                ++index;
            }
        } while (gaps.size() < numberOfGaps);

        int minGap = 0;
        if (gaps.size() > numberOfGaps) {
            foundGap = false;
            start = -1;
            do {
                ++minGap;
                gaps = new ArrayList<>();
                int index = 0;
                for (final int i : verticalSmooth) {
                    if (i <= startThreshold & !foundGap) {
                        start = index;
                        foundGap = true;
                    } else if (i > startThreshold && foundGap) {
                        if (index - start >= minGap) {
                            gaps.add(new Point(start, index - 1));
                        }
                        foundGap = false;
                    }
                    ++index;
                }
            } while (gaps.size() >= numberOfGaps);
        }

        if (gaps.size() < numberOfGaps) {
            foundGap = false;
            start = -1;
            --minGap;
            gaps = new ArrayList<>();
            int index = 0;
            for (final int i : verticalSmooth) {
                if (i <= startThreshold & !foundGap) {
                    start = index;
                    foundGap = true;
                } else if (i > startThreshold && foundGap) {
                    if (index - start >= minGap) {
                        gaps.add(new Point(start, index - 1));
                    }
                    foundGap = false;
                }
                ++index;
            }
        }

        return gaps;
    }

    /**
     * Gets the vertical projection
     * 
     * @return the vertical projection
     */
    public int[] getVerticalProjection() {
        return this.vertical;
    }

    /**
     * Gets the horizontal projection
     * 
     * @return the horizontal projection
     */
    public int[] getHorizontalProjection() {
        return this.horizontal;
    }

    /**
     * Gets the projections
     * 
     * @return the projections
     */
    public int[][] getProjections() {
        return new int[][] { this.horizontal, this.vertical };
    }
}
