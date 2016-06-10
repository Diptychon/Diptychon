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

import java.io.Serializable;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * This method is used to encapsulate the operation of gray image.
 */
public class GrayImage implements Serializable {
    /** White pixels. */
    public transient static final byte WHITE = (byte) 255;

    /** Black pixels. */
    public transient static final byte BLACK = 0;

    /**
     * The maximum intensity of a gray image
     */
    private transient static final int MAX_VALUE = 255;

    /**
     * Constant which represents the value which is needed to convert a byte
     * into an int
     */
    private transient static final int BYTE_CONVERT = 0xff;

    /**
     * SerialVersionUID to keep used data consistent.
     */
    private static final long serialVersionUID = 20121101;

    /**
     * byte array of pixels
     */
    private byte[] pixels;

    /**
     * width of gray image
     */
    private int width;

    /**
     * height of gray image
     */
    private int height;

    /**
     * width * height
     */
    private int size;

    /**
     * This method is used to construct the gray image object
     * 
     * @param pPixels
     *            pixels of the image
     * @param pWidth
     *            width
     * @param pHeight
     *            height
     */
    public GrayImage(final byte[] pPixels, final int pWidth, final int pHeight) {
        this.pixels = pPixels.clone();
        this.width = pWidth;
        this.height = pHeight;
        this.size = pWidth * pHeight;
    }

    /**
     * This method is used to get pixel of image
     * 
     * @param pos
     *            the index of pixel array
     * @return the pixel which is specified by pos
     */
    public int getPixelInt(final int pos) {
        if (pos >= this.size) {
            return GrayImage.WHITE & BYTE_CONVERT;
        } else {
            return this.pixels[pos] & BYTE_CONVERT;
        }
    }

    /**
     * Gets the pixel value at position pos (includes boundary check)
     * 
     * @param pos
     *            the position
     * @return the pixel value
     */
    public byte getPixel(final int pos) {
        if (pos >= this.size) {
            return GrayImage.WHITE;
        } else {
            return this.pixels[pos];
        }
    }

    /**
     * This method is used to get the pixel in the form of int.
     * 
     * @param x
     *            x coordinate in the image
     * @param y
     *            y coordinate in the image
     * @return pixel[x,y]
     */
    public int getPixelAsInt(final int x, final int y) {
        return this.pixels[this.index(x, y)] & BYTE_CONVERT;
    }

    /**
     * This method is used to get the pixel in the form of byte.
     * 
     * @param x
     *            x coordinate in the image
     * @param y
     *            y coordinate in the image
     * @return pixel[x,y]
     */
    public byte getPixel(final int x, final int y) {
        final int pos = this.index(x, y);
        if (pos >= this.width * this.height) {
            return GrayImage.WHITE;
        } else {
            return this.pixels[this.index(x, y)];
        }
    }

    /**
     * Gets a cloned version of the pixels of this image
     * 
     * @return the cloned version
     */
    public byte[] getPixelCloned() {
        final byte[] cloned = new byte[this.pixels.length];
        System.arraycopy(this.pixels, 0, cloned, 0, this.pixels.length);
        return cloned;
    }

    /**
     * Inverts the pixel at position (x,y)
     * 
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     */
    public void invertPixel(final int x, final int y) {
        final int index = this.index(x, y);
        this.pixels[index] = (byte) (GrayImage.WHITE - this.pixels[index]);
    }

    public void invertImage() {
        for (int y = 0; y < this.height; y++)
            for (int x = 0; x < this.width; x++)
                invertPixel(x, y);
    }

    /**
     * Subtracts the given GrayImage from this image
     * 
     * @param image
     *            the image to merge with
     * @param minX
     *            the minimal X position of this image
     * @param minY
     *            the minimal Y position of this image
     * @param minX2
     *            the minimal X position of the image to subtract
     * @param minY2
     *            the minimal Y position of the image to subtract
     */
    public void subtract(GrayImage image, int minX, int minY, int minX2,
            int minY2) {
        for (int y = minY; y < this.height; y++) {
            for (int x = minX; x < this.width; x++) {
                if (y - minY + minY2 >= image.getHeight()
                        || x - minX + minX2 >= image.getWidth()) {
                    break;
                }
                if (image.pixels[image
                        .index(x - minX + minX2, y - minY + minY2)] != WHITE) {
                    this.pixels[this.index(x, y)] = WHITE;
                }
            }
        }
    }

    /**
     * Merges this image with a given GrayImage
     * 
     * @param image
     *            the image to merge with
     * @param minX
     *            the minimal X position of this image in the resulting new
     *            image
     * @param minY
     *            the minimal Y position of this image in the resulting new
     *            image
     * @param minX2
     *            the minimal X position of the image to merge with in the
     *            resulting new image
     * @param minY2
     *            the minimal Y position of the image to merge with in the
     *            resulting new image
     * @param newWidth
     *            the new width of this image after merging
     * @param newHeight
     *            the new height of this image after merging
     */
    public void merge(GrayImage image, int minX, int minY, int minX2,
            int minY2, int newWidth, int newHeight) {
        byte[] newPixels = new byte[newWidth * newHeight];
        for (int i = 0; i < newWidth * newHeight; i++) {
            newPixels[i] = WHITE;
        }
        // byte[] pixels2 = image.getPixelCloned();

        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                if (this.pixels[this.index(x, y)] != WHITE) {
                    newPixels[(y + minY) * newWidth + x + minX] = this.pixels[this
                            .index(x, y)];
                }
            }
        }
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.getPixels()[image.index(x, y)] != WHITE) {
                    if (newPixels[(y + minY2) * newWidth + x + minX2] == WHITE) {
                        newPixels[(y + minY2) * newWidth + x + minX2] = image
                                .getPixels()[image.index(x, y)];
                    }
                }
            }
        }

        this.width = newWidth;
        this.height = newHeight;
        this.pixels = newPixels;
        this.size = newWidth * newHeight;
    }

    /**
     * Sets the pixel at position (x,y) to white
     * 
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     */
    public void whitenPixel(final int x, final int y) {
        this.pixels[this.index(x, y)] = GrayImage.WHITE;
    }

    /**
     * Sets the pixel at position (x,y) to black
     * 
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     */
    public void blackenPixel(final int x, final int y) {
        this.pixels[this.index(x, y)] = GrayImage.BLACK;
    }

    /**
     * Sets the pixel at position (x,y) to grey
     * 
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     */
    public void setToGrey(final int x, final int y, final int greyValue) {
        this.pixels[this.index(x, y)] = (byte) greyValue;
    }

    /**
     * Convenience method to calculate the array index for position (x,y)
     * 
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     * @return the corresponding array index
     */
    private int index(final int x, final int y) {
        return y * this.width + x;
    }

    /**
     * This method is used to get width of image
     * 
     * @return width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * This method is used to get height of image
     * 
     * @return height
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * This method is used to get pixels
     * 
     * @return array of pixels
     */
    public byte[] getPixels() {
        return this.pixels;
    }

    /**
     * Converts the pixels of this image to JavaFX pixel values
     * 
     * @return the pixels as javafx pixels
     */
    private int[] getJavaFXPixelsAsInt() {
        final int[] intPixels = new int[this.pixels.length];
        int index = 0;
        for (final byte by : this.pixels) {
            final Color col = Color.gray((by & BYTE_CONVERT)
                    / (double) MAX_VALUE);
            final int r = ((int) (col.getRed() * MAX_VALUE)) << 16;
            final int g = ((int) (col.getGreen() * MAX_VALUE)) << 8;
            final int b = ((int) (col.getBlue() * 255));
            final int fullAlpha = 0xff000000;
            intPixels[index] = fullAlpha | r | g | b;
            ++index;
        }
        return intPixels;
    }

    /**
     * Gets the pixels of this image as int array
     * 
     * @return the pixels as int array
     */
    public int[] getPixelsAsInt() {
        final int[] intBuffer = new int[this.pixels.length];
        int index = 0;
        for (final byte p : this.pixels) {
            intBuffer[index] = p & 0xff;
            ++index;
        }
        return intBuffer;
    }

    /**
     * Gets the pixels of this image as an 2D int array
     * 
     * @return the pixels as an 2D int array, so that: object-pixels are 1
     *         background-pixels are 0
     */
    public int[][] getPixelsAs2DInt() {
        int[][] img = new int[this.getWidth()][this.getHeight()];
        for (int x = 0; x < getWidth(); ++x) {
            for (int y = 0; y < this.getHeight(); ++y) {
                if (getPixelAsInt(x, y) == GrayImage.BLACK)
                    img[x][y] = 1;
                else
                    img[x][y] = 0;
            }
        }
        return img;
    }

    /**
     * Gets this image as JavaFX Image
     * 
     * @return this image as javafx image
     */
    public Image getAsFXImage() {
        return ImageUtils.getJavaFXImage(this.getJavaFXPixelsAsInt(),
                this.width, this.height);
    }
}
