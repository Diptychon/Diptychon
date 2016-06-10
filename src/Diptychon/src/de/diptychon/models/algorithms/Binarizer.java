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
package de.diptychon.models.algorithms;

import java.util.Arrays;

import javafx.scene.shape.Rectangle;
import de.diptychon.models.misc.GrayImage;
import de.diptychon.models.misc.IntegralImage;

/**
 * This class provide the operations of binarization.
 */
public class Binarizer {

    /**
     * This method is used to binarize the image by using Sauvola method.
     * 
     * @param grayImage
     *            the image being binarized
     * @param windowSize
     *            window size value
     * @param k
     *            k value
     * @return the binarized image
     */
    public GrayImage bySauvola(final GrayImage grayImage, final int windowSize,
            final double k) {
        final int wHalf = windowSize >> 1;
        final int width = grayImage.getWidth();
        final int height = grayImage.getHeight();

        assert (wHalf + wHalf + 3 < width || wHalf + wHalf + 3 < height);

        final IntegralImage intImg = new IntegralImage(grayImage, width, height);
        final byte[] result = new byte[width * height];
        Arrays.fill(result, GrayImage.WHITE);

        int y = 0;
        while (y < height) {
            final int yMin = Math.max(0, y - wHalf);
            final int yMax = Math.min(height - 1, y + wHalf);
            final int row = yMax * width;
            int x = 0;
            while (x < width) {
                final int xMin = Math.max(0, x - wHalf);
                final int xMax = Math.min(width - 1, x + wHalf);
                final IntegralImage.Stats stats = intImg.createNewStats(xMin,
                        yMin, xMax, yMax);
                final double threshold = stats.getMean()
                        * (1.0 + k * (stats.getStdDev() * 0.0078125 - 1.0));
                final int pos = row + xMax;
                if (grayImage.getPixelInt(pos) < threshold) {
                    result[pos] = GrayImage.BLACK;
                }
                ++x;
            }
            ++y;
        }
        return new GrayImage(result, width, height);
    }

    public GrayImage bySauvola(final GrayImage grayImage,
            final GrayImage binaryImage, final int windowSize, final double k,
            Rectangle rect) {
        final int wHalf = windowSize >> 1;
        final int width = grayImage.getWidth();
        final int height = grayImage.getHeight();

        assert (wHalf + wHalf + 3 < width || wHalf + wHalf + 3 < height);

        final IntegralImage intImg = new IntegralImage(grayImage, width, height);
        // final byte[] result = new byte[width * height];
        final byte[] result = binaryImage.getPixelCloned();
        // Arrays.fill(result, GrayImage.WHITE);

        int y = (int) rect.getY() - wHalf;
        while (y < rect.getY() - wHalf + rect.getHeight()) {
            final int yMin = Math.max(0, y - wHalf);
            final int yMax = Math.min(height - 1, y + wHalf);
            final int row = yMax * width;
            int x = (int) rect.getX() - wHalf;
            while (x < rect.getX() - wHalf + rect.getWidth()) {
                final int xMin = Math.max(0, x - wHalf);
                final int xMax = Math.min(width - 1, x + wHalf);
                final int pos = row + xMax;
                result[pos] = GrayImage.WHITE;

                final IntegralImage.Stats stats = intImg.createNewStats(xMin,
                        yMin, xMax, yMax);
                final double threshold = stats.getMean()
                        * (1.0 + k * (stats.getStdDev() * 0.0078125 - 1.0));

                if (grayImage.getPixelInt(pos) < threshold) {
                    result[pos] = GrayImage.BLACK;
                }
                ++x;
            }
            ++y;
        }
        return new GrayImage(result, width, height);
    }

}
