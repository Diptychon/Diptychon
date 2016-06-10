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

import java.util.ArrayList;

import de.diptychon.models.algorithms.validation.featureExtraction.A_Feature;

/**
 * Feature proposed in Ho, Tin Kam (1995). "Random Decision Forest". Proceedings
 * of the 3rd International Conference on Document Analysis and Recognition,
 * Montreal, QC, 14-16 August 1995. pp. 278-282.
 */
public class PixelCorrelation extends A_Feature {
    /**
     * Constant which represents the transformed value of a background pixel
     */
    private static final int BACKGROUND = 0;

    /**
     * Constant which represents the transformed value of a foreground pixel
     */
    private static final int FOREGROUND = 1;

    /**
     * Calculates the pixel correlation for a given image
     * 
     * @param pixels
     *            the pixels of the image
     * @param width
     *            the width of the image
     * @param height
     *            the height of the image
     */
    public PixelCorrelation(final int[] pixels, final int width,
            final int height) {
        final ArrayList<Integer> fv = new ArrayList<Integer>();

        final int[][] h = new int[width][height];
        final int[][] v = new int[width][height];
        final int[][] n = new int[width][height];
        final int[][] s = new int[width][height];

        for (int y = 0; y < height; ++y) {
            final int offset = y * width;
            for (int x = 0; x < width; ++x) {
                final int index = x + offset;
                h[x][y] = -1;
                v[x][y] = -1;
                n[x][y] = -1;
                s[x][y] = -1;

                final int pixel = pixels[index] != 0 ? FOREGROUND : BACKGROUND;
                if (x + 2 < width) {
                    final int i = index + 2;
                    h[x][y] = pixel
                            & (pixels[i] != 0 ? FOREGROUND : BACKGROUND);
                }

                if (y + 2 < height) {
                    int i = index + 2 * width;
                    v[x][y] = pixel
                            & (pixels[i] != 0 ? FOREGROUND : BACKGROUND);
                    if (x + 2 < width) {
                        i += 2;
                        n[x][y] = pixel
                                & (pixels[i] != 0 ? FOREGROUND : BACKGROUND);
                    }
                    if (x - 2 >= 0) {
                        i -= 4;
                        s[x][y] = pixel
                                & (pixels[i] != 0 ? FOREGROUND : BACKGROUND);
                    }
                }
            }
        }

        for (int y = 0; y < height - 3; y += 2) {
            for (int x = 0; x < width - 2; ++x) {
                fv.add(h[x][y] | h[x][y + 1] | h[x][y + 2] | h[x][y + 3]);
            }
        }

        for (int y = 0; y < height - 2; ++y) {
            for (int x = 0; x < width - 3; x += 2) {
                fv.add(v[x][y] | v[x + 1][y] | v[x + 2][y] | v[x + 3][y]);
            }
        }

        for (int y = 0; y < height - 5; y += 2) {
            for (int x = 3; x < width - 2; x += 2) {
                fv.add(n[x][y] | n[x - 1][y + 1] | n[x - 2][y + 2]
                        | n[x - 3][y + 3]);
            }

            for (int x = 2; x < width - 3; x += 2) {
                fv.add(s[x][y] | s[x + 1][y + 1] | s[x + 2][y + 2]
                        | s[x + 3][y + 3]);
            }
        }

        this.featureVector = new double[fv.size()];
        for (int i = 0; i < this.featureVector.length; ++i) {
            this.featureVector[i] = fv.get(i);
        }
    }

    /**
     * Factory class to create this feature
     */
    public static class Factory extends A_Feature.Factory {
        @Override
        public PixelCorrelation createFeature(final int[] pixels,
                final int width, final int height) {
            return new PixelCorrelation(pixels, width, height);
        }
    }

}
