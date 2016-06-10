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

import java.awt.Dimension;

import de.diptychon.models.glyphGeometry.BinaryImage;
import de.diptychon.models.misc.GrayImage;

/**
 * This class represents the template which is used for
 * {@link de.diptychon.models.algorithms.TemplateMatching Template Matching}
 */
public class Template {

    /**
     * The pixels of the template
     */
    private final byte[] pixels;

    /**
     * The dimension of the template, i.e. width and height
     */
    private final Dimension dimension;

    /**
     * The size of the template (width * height)
     */
    private final int size;

    /**
     * The number of holes within the regions of this template
     */
    private int numOfHoles = -1;

    /**
     * The mean value of the template [0..255]
     */
    private double mean;

    /**
     * The variance of the template values (sumOfPixelValues * sumOfPixelValues
     * - size * mean * mean)
     */
    private double sigma;

    /**
     * Default Constructor
     * 
     * @param p
     *            The pixels of the template
     * @param w
     *            The width of the template
     * @param h
     *            The height of the template
     */
    public Template(final byte[] p, final int w, final int h) {
        this.pixels = p;
        this.dimension = new Dimension(w, h);
        this.size = w * h;
        this.numOfHoles = -1;
        this.calcTemplateValues();
    }

    /**
     * Calculates the mean value {@link #mean mean} and the variance
     * {@link #sigma sigma}
     */
    private void calcTemplateValues() {
        int sumR = 0;
        int sumR2 = 0;
        // calculates sum and square of sum of all pixel values
        for (int y = 0; y < this.dimension.height; y++) {
            final int offset = y * this.dimension.width;
            for (int x = 0; x < this.dimension.width; x++) {
                final int index = x + offset;
                final double valT = this.pixels[index] & 0xff;
                sumR += valT;
                sumR2 += valT * valT;
            }
        }
        this.mean = sumR / (double) this.size;
        this.sigma = Math.sqrt(sumR2 - this.size * this.mean * this.mean);
    }

    /**
     * Returns the {@link #mean mean value} of the template
     * 
     * @return the {@link #mean mean value}
     */
    public double mean() {
        return this.mean;
    }

    /**
     * Returns the variance {@link #sigma sigma} of the template
     * 
     * @return the {@link #sigma sigma}
     */
    public double sigma() {
        return this.sigma;
    }

    /**
     * Returns the size of the template
     * 
     * @return the size of the template
     */
    public int size() {
        return this.size;
    }

    /**
     * Returns the dimension of the template
     * 
     * @return the dimension of the template
     */
    public Dimension dimension() {
        return this.dimension;
    }

    /**
     * Returns the width of the template
     * 
     * @return the width of the template
     */
    public int width() {
        return this.dimension.width;
    }

    /**
     * Returns the height of the template
     * 
     * @return the height of the template
     */
    public int height() {
        return this.dimension.height;
    }

    /**
     * Returns the pixels of the template as array
     * 
     * @return the pixels of the template
     */
    public byte[] pixels() {
        return this.pixels;
    }

    /**
     * Returns the number of holes within the regions of the template image
     */
    public int getNumOfHoles() {
        if (numOfHoles == -1) {
            Binarizer b = new Binarizer();
            GrayImage gImg = new GrayImage(pixels(), width(), height());
            BinaryImage bImg = new BinaryImage(b.bySauvola(gImg, 20, 0.25)
                    .getPixels(), width(), height());

            bImg.closing();
            bImg.labelConnectedComponents();
            numOfHoles = bImg.getNumOfHoles();
        }
        return numOfHoles;
    }
}
