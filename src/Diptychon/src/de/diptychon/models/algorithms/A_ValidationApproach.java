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

/**
 * This class provides the basic functionality for validaton approaches used
 * after template matching.
 */
public abstract class A_ValidationApproach {

    /**
     * The width of the image
     */
    protected int width;

    /**
     * The height of the image
     */
    protected int height;

    /**
     * The pixel values of the image
     */
    protected byte[] pixels;

    /**
     * Returns the name of the implementing class
     * 
     * @return the name of the implementing class
     */
    public String getName() {
        final String[] name = this.getClass().getName().split("\\.");
        return name[name.length - 1];
    }

    /**
     * Interface used to create a new ValidationApproach.
     */
    public interface Factory {
        /**
         * Factory method which returns a new instance of the surrounding class
         * 
         * @param p
         *            pixels of an image
         * @param w
         *            width the image <code>p</code>
         * @param h
         *            height the image <code>p</code>
         * @return New instance of the surrounding class
         */
        A_ValidationApproach createFeature(final byte[] p, final int w,
                final int h);
    }
}
