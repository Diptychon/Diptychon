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
package de.diptychon.models.misc;

/**
 * Adapted version from lmls version in Venod which calculates the integral
 * image
 */
public class IntegralImage {
    /**
     * size = width * height, the area of image
     */
    private final int size;

    /**
     * width of image
     */
    private final int width;

    /**
     * height of image
     */
    private final int height;

    /**
     * the integral image
     */
    private final long[] integral;

    /**
     * the integral square image
     */
    private final long[] integralSq;

    /**
     * gray image
     */
    private final GrayImage grayImage;

    /**
     * This method is used to construct the IntergralImage.
     * 
     * @param pGrayImage
     *            gray image
     * @param pWidth
     *            width of image
     * @param pHeight
     *            height of image
     */
    public IntegralImage(final GrayImage pGrayImage, final int pWidth,
            final int pHeight) {
        this.grayImage = pGrayImage;
        this.width = pWidth;
        this.height = pHeight;
        this.size = this.width * this.height;
        this.integral = new long[this.size];
        this.integralSq = new long[this.size];
        this.calculateIntegrals();
    }

    /**
     * Viola & Jones 2004 version: ii(x,y) = \sum_{0<=x'<=x, 0<=y'<=y} i(x',y')
     * recursion s(x, y) = s(x, y - 1) + i(x, y) //cumulative row sums ii(x, y)
     * = ii(x - 1, y) + s(x, y) with s(x, -1) = 0 and ii(-1, y) = 0 -- this
     * seems like the fastest way under Scala2.8RC6
     */
    private void calculateIntegrals() {
        final long[] rowSums = new long[this.width];
        final long[] rowSumsSq = new long[this.width];
        int pos = 0;
        while (pos < this.size) {
            final int pixel = this.grayImage.getPixelInt(pos);
            rowSums[0] += pixel;
            rowSumsSq[0] += pixel * pixel;
            this.integral[pos] += rowSums[0];
            this.integralSq[pos] += rowSumsSq[0];
            int x = 1;
            while (x < this.width) {
                ++pos;
                final int pixel2 = this.grayImage.getPixelInt(pos);
                rowSums[x] += pixel2;
                this.integral[pos] = this.integral[pos - 1] + rowSums[x];
                rowSumsSq[x] += pixel2 * pixel2;
                this.integralSq[pos] = this.integralSq[pos - 1] + rowSumsSq[x];
                ++x;
            }
            ++pos;
        }
    }

    /**
     * This method is used to calculate the index in an pixel array
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @return the index
     */
    private int index(final int x, final int y) {
        return y * this.width + x;
    }

    /**
     * Integrates within the specified rectangle
     * 
     * @param intImg
     *            the integralimage
     * @param xMin
     *            xMin
     * @param yMin
     *            yMin
     * @param xMax
     *            xMax
     * @param yMax
     *            yMax
     * @return sum over squared entries in the rectangular area
     */
    private long integrate(final long[] intImg, final int xMin, final int yMin,
            final int xMax, final int yMax) {
        final int pos = this.index(xMax, yMax);
        final int maxRow = this.index(0, yMax);
        final int minRow = this.index(0, yMin - 1);

        if (xMin == 0) {
            if (yMin == 0) {
                return intImg[pos]; // origin
            } else { // first column, yMin > 0
                return intImg[pos] - intImg[minRow + xMax]; // up: (xMax, yMin -
                                                            // 1)
            }
        } else if (yMin == 0) { // first row
            return intImg[pos] - intImg[maxRow + xMin - 1]; // left: (xMin - 1,
                                                            // yMax)
        } else { // rest of the image
            final int leftPos = maxRow + xMin - 1; // left: (xMin - 1, yMax)
            final int upperPos = minRow + xMax; // up: (xMax, yMin - 1)
            final int upperLeftPos = minRow + xMin - 1; // ul: (xMin - 1, yMin -
                                                        // 1)
            return intImg[pos] + intImg[upperLeftPos] - intImg[upperPos]
                    - intImg[leftPos];
        }
    }

    /**
     * $\sum_{x, y \in r} image(x ,y)^2$
     * 
     * @param xMin
     *            xMin
     * @param yMin
     *            yMin
     * @param xMax
     *            xMax
     * @param yMax
     *            yMax
     * @return sum over squared entries in the rectangular area
     */
    private Long rectangleSum(final int xMin, final int yMin, final int xMax,
            final int yMax) {
        return this.integrate(this.integral, xMin, yMin, xMax, yMax);
    }

    /**
     * $\sum_{x, y \in r} image(x ,y)^2$
     * 
     * @param xMin
     *            xMin
     * @param yMin
     *            yMin
     * @param xMax
     *            xMax
     * @param yMax
     *            yMax
     * @return sum over squared entries in the rectangular area
     */
    private long rectangleSumSq(final int xMin, final int yMin, final int xMax,
            final int yMax) {
        return this.integrate(this.integralSq, xMin, yMin, xMax, yMax);
    }

    /**
     * Creates the statistics for a given rectangular area
     * 
     * @param p_xMin
     *            xMin
     * @param p_yMin
     *            yMin
     * @param p_xMax
     *            xMax
     * @param p_yMax
     *            yMax
     * @return statistics for the given rectangular area
     */
    public Stats createNewStats(final int p_xMin, final int p_yMin,
            final int p_xMax, final int p_yMax) {
        return new Stats(p_xMin, p_yMin, p_xMax, p_yMax);
    }

    /**
     * Mean, standard deviation and variance in the given rectangle. Variance is
     * normalized by 1/N.
     * 
     * @param r
     *            rectangular area where mean and variance are to be calculated
     */
    public class Stats {
        /**
         * minimum of x coordinate
         */
        private final int xMin;

        /**
         * minimum of y coordinate
         */
        private final int yMin;

        /**
         * maximum of y coordinate
         */
        private final int yMax;

        /**
         * maximum of x coordinate
         */
        private final int xMax;

        /**
         * area of image
         */
        private final double area;

        /**
         * sum of black pixels
         */
        private final long sum;

        /**
         * mean
         */
        private final double mean;

        /**
         * standard deviation
         */
        private final double stdDev;

        /**
         * variance
         */
        private final double variance;

        /**
         * This method is used to construct the Stats object
         * 
         * @param p_xMin
         *            minimum of x
         * @param p_yMin
         *            minimum of y
         * @param p_xMax
         *            maximum of x
         * @param p_yMax
         *            maximum of y
         */
        public Stats(final int p_xMin, final int p_yMin, final int p_xMax,
                final int p_yMax) {
            this.xMin = p_xMin;
            this.yMin = p_yMin;
            this.xMax = p_xMax;
            this.yMax = p_yMax;
            this.area = (this.xMax - this.xMin + 1)
                    * (this.yMax - this.yMin + 1);
            this.sum = IntegralImage.this.rectangleSum(this.xMin, this.yMin,
                    this.xMax, this.yMax);
            this.mean = this.sum / this.area;
            this.variance = (IntegralImage.this.rectangleSumSq(this.xMin,
                    this.yMin, this.xMax, this.yMax) - this.sum * this.mean)
                    / this.area;
            this.stdDev = Math.sqrt(this.variance);
        }

        /**
         * This method is used to get mean
         * 
         * @return mean
         */
        public double getMean() {
            return this.mean;
        }

        /**
         * This method is used to get standard deviation
         * 
         * @return standard deviation
         */
        public double getStdDev() {
            return this.stdDev;
        }
    }
}
