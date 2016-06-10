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

import java.io.IOException;
import java.io.Serializable;

import javafx.scene.shape.Rectangle;

/**
 * Represents an contrast operation. Necessary to be able to reapply contrast
 * operations to an image when reloaded
 */
public class AutoContrastOperation implements Serializable {

    /**
     * SerialVersionUID to keep used data consistent.
     */
    private static final long serialVersionUID = 202121129;

    /**
     * The position and size of the area where this operation was applied to
     */
    private transient Rectangle imageRectangle;

    /**
     * The saturation on the left side of this histogram
     */
    private final double saturationLow;

    /**
     * The saturation on the right side of this histogram
     */
    private final double saturationHigh;

    /**
     * The minimum pixel intensity
     */
    private final int aMin;

    /**
     * The maximum pixel intensity
     */
    private final int aMax;

    /**
     * The histogram of the area where this operation is applied to
     */
    private int[] histogram;

    /**
     * Creates a new AutoContrastOperation
     * 
     * @param pImageRectangle
     *            The position and size of the area where this operation was
     *            applied to
     * @param pSaturationLow
     *            The saturation on the left side of this histogram
     * @param pSaturationHigh
     *            The saturation on the right side of this histogram
     * @param pAMin
     *            The minimum pixel intensity
     * @param pAMax
     *            The maximum pixel intensity
     * @param pHistogramm
     *            The histogram of the area where this operation is applied to
     */
    public AutoContrastOperation(final Rectangle pImageRectangle,
            final double pSaturationLow, final double pSaturationHigh,
            final int pAMin, final int pAMax, final int[] pHistogramm) {
        this.imageRectangle = pImageRectangle;
        this.saturationLow = pSaturationLow;
        this.saturationHigh = pSaturationHigh;
        this.aMin = pAMin;
        this.aMax = pAMax;
        this.histogram = pHistogramm;
    }

    /**
     * Gets the area this operation is applied to
     * 
     * @return the area this operation is applied to
     */
    public Rectangle getImageRectangle() {
        return this.imageRectangle;
    }

    /**
     * Gets the saturation of the left side
     * 
     * @return the saturation of the left side
     */
    public double getSaturationLow() {
        return this.saturationLow;
    }

    /**
     * Gets the saturation of the right side
     * 
     * @return the saturation of the right side
     */
    public double getSaturationHigh() {
        return this.saturationHigh;
    }

    /**
     * Gets the minimum pixel intensity
     * 
     * @return the minimum pixel intensity
     */
    public int getaMin() {
        return this.aMin;
    }

    /**
     * Gets the maximum pixel intensity
     * 
     * @return the maximum pixel intensity
     */
    public int getaMax() {
        return this.aMax;
    }

    /**
     * Gets the histogram of the area this op is applied to
     * 
     * @return the histogram of the area this op is applied to
     */
    public int[] getHistogramm() {
        return this.histogram;
    }

    /**
     * Sets the histogram of the area this op is applied to
     * 
     * @param pHistogramm
     *            the histogram of the area this op is applied to
     */
    public void setHistogramm(final int[] pHistogramm) {
        this.histogram = pHistogramm;
    }

    /**
     * Writes this Object to Outputstream s
     * 
     * @param s
     *            the outputstream to write to
     * @throws IOException
     *             thrown when the Outputstream is not writable, ...
     */
    private synchronized void writeObject(final java.io.ObjectOutputStream s)
            throws IOException {
        s.defaultWriteObject();
        s.writeInt((int) this.imageRectangle.getX());
        s.writeInt((int) this.imageRectangle.getY());
        s.writeInt((int) this.imageRectangle.getWidth());
        s.writeInt((int) this.imageRectangle.getHeight());
    }

    /**
     * Reads this Object from InputStream s
     * 
     * @param s
     *            the InputStream to read from
     * @throws IOException
     *             thrown when the InputStream is not readable, ...
     * @throws ClassNotFoundException
     *             thrown when the class is not found or the serialversionUID
     *             are different
     */
    private void readObject(final java.io.ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.imageRectangle = new Rectangle(s.readInt(), s.readInt(),
                s.readInt(), s.readInt());
    }
}
