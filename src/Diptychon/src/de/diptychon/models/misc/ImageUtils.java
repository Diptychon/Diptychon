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

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import javax.imageio.ImageIO;

import de.diptychon.DiptychonLogger;

/**
 * This class provides the operations of image such convention, crop, mask and
 * so on.
 */
public final class ImageUtils {

    /**
     * Empty Default constructor
     */
    private ImageUtils() {

    }

    /**
     * Writes a grayscale image to hdd
     * 
     * @param filename
     *            the filename to write to
     * @param grayimage
     *            the grayimage to write
     */
    public static void writeGrayscaleImage(final String filename,
            final GrayImage grayimage) {
        try {
            final FileOutputStream out = new FileOutputStream(
                    new File(filename));
            writeGrayscaleImage(out, grayimage);
            out.flush();
            out.close();
        } catch (final IOException e) {
            DiptychonLogger.error("{}", e);
        }
    }

    /**
     * Writes a grayscale image to the stream
     * 
     * @param stream
     *            the stream to write to
     * @param grayimage
     *            the grayimage to write
     */
    public static void writeGrayscaleImage(final OutputStream stream,
            final GrayImage grayimage) throws IOException {
        final BufferedImage image = new BufferedImage(grayimage.getWidth(),
                grayimage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        final WritableRaster wr = image.getRaster();

        wr.setDataElements(0, 0, grayimage.getWidth(), grayimage.getHeight(),
                grayimage.getPixels());
        ImageIO.write(image, "png", stream);
    }

    /**
     * This method is used to transform raw pixels into javafx image.
     * 
     * @param rawPixels
     *            array of integer pixels
     * @param width
     *            width of image
     * @param height
     *            height of image
     * @return javafx image
     */
    public static Image getJavaFXImage(final int[] rawPixels, final int width,
            final int height) {
        final WritableImage image = new WritableImage(width, height);
        final PixelWriter pixelWriter = image.getPixelWriter();
        pixelWriter.setPixels(0, 0, width, height,
                PixelFormat.getIntArgbInstance(), rawPixels, 0, width);
        return image;
    }

    /**
     * This method is used to transform a buffered image into javafx image
     * 
     * @param image
     *            buffered image
     * @return javafx image
     */
    public static Image getJavaFXImage(final BufferedImage image) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", out);
            out.flush();
        } catch (final IOException e) {
            DiptychonLogger.error("{}", e);
        }
        final ByteArrayInputStream in = new ByteArrayInputStream(
                out.toByteArray());
        return new Image(in);
    }

    /**
     * This method is used to crop the image according to the given region
     * 
     * @param mask
     *            rectangle region
     * @param image
     *            the original image
     * @return the cropped image
     */
    public static Image cropFXImage(final Image image, final Rectangle mask) {
        return createNewImage(mask, image, 0, 0);
    }

    /**
     * This method is used to crop the image as the gray image according to the
     * mask
     * 
     * @param mask
     *            rectangle mask
     * @param image
     *            the oringinal image
     * @return gray image
     */
    public static GrayImage cropFXImageAsGrayImage(final Rectangle mask,
            final Image image) {
        return createNewGrayImage(mask, image, 0, 0, (int) mask.getWidth(),
                (int) mask.getHeight());
    }

    /**
     * This method is used to create a new gray image
     * 
     * @param mask
     *            rectangle mask
     * @param image
     *            the original image
     * @param x
     *            start x coordinate
     * @param y
     *            start y coordinate
     * @param imageWidth
     *            width of image
     * @param imageHeight
     *            height of image
     * @return new gray image
     */
    private static GrayImage createNewGrayImage(final Rectangle mask,
            final Image image, final int x, final int y, final int imageWidth,
            final int imageHeight) {
        final PixelReader pixelReader = image.getPixelReader();
        final int width = (int) mask.getWidth();
        final int height = (int) mask.getHeight();
        final int[] intBuffer = new int[width * height];
        pixelReader.getPixels((int) mask.getX(), (int) mask.getY(), width,
                height, PixelFormat.getIntArgbInstance(), intBuffer, 0, width);
        final byte[] byteBuffer = new byte[intBuffer.length];
        int index = 0;
        for (final int i : intBuffer) {
            final int r = (i >> 16) & 0xFF;
            final int g = (i >> 8) & 0xFF;
            final int b = i & 0xFF;
            final Color col = Color.rgb(r, g, b);
            final int gray = (int) (col.getRed() * 255);
            byteBuffer[index] = (byte) gray;
            ++index;
        }
        return new GrayImage(byteBuffer, width, height);
    }

    /**
     * This method is used to create a new JavaFX image.
     * 
     * @param mask
     *            rectangle mask
     * @param image
     *            the original mask
     * @param x
     *            start x coordinate
     * @param y
     *            start y coordinate
     * @return a new created image
     */
    private static Image createNewImage(final Rectangle mask,
            final Image image, final int x, final int y) {
        final PixelReader pixelReader = image.getPixelReader();

        final int width = (int) mask.getWidth();
        final int height = (int) mask.getHeight();

        final int[] buffer = new int[width * height];

        pixelReader.getPixels((int) mask.getX(), (int) mask.getY(), width,
                height, PixelFormat.getIntArgbInstance(), buffer, 0, width);

        final WritableImage writableImage = new WritableImage(width, height);
        final PixelWriter pixelWriter = writableImage.getPixelWriter();

        pixelWriter.setPixels(0, 0, width, height,
                PixelFormat.getIntArgbInstance(), buffer, 0, width);

        return writableImage;
    }

    /**
     * Automatically crops an grayimage in such a way that there is no white
     * border left
     * 
     * @param image
     *            the image to crop
     * @param mask
     *            reference to the bounds (will be changed if the image is
     *            cropped)
     * @return the cropped image
     * @throws IllegalArgumentException
     *             if the image cannot be cropped since it only consists of
     *             white pixels
     */
    public static GrayImage autoCrop(final GrayImage image, final Rectangle mask)
            throws IllegalArgumentException {
        final byte[] pixels = ImageUtils.autoCrop(image.getPixels(), mask);
        return new GrayImage(pixels, (int) mask.getWidth(),
                (int) mask.getHeight());
    }

    /**
     * This method is used to crop the image automatically.
     * 
     * @param pixels
     *            the pixels of original image
     * @param mask
     *            reference to the bounds (will be changed if the image is
     *            cropped)
     * @return the cropped pixels
     * @throws IllegalArgumentException
     *             if the image cannot be cropped since it only consists of
     *             white pixels
     */
    public static byte[] autoCrop(final byte[] pixels, final Rectangle mask)
            throws IllegalArgumentException {
        final int width = (int) mask.getWidth();
        final int height = (int) mask.getHeight();
        int leftX = width;
        int rightX = 0;
        int topY = height;
        int bottomY = 0;
        for (int y = 0; y < height; ++y) {
            final int offset = y * width;
            for (int x = 0; x < width; ++x) {
                final int index = offset + x;
                if (pixels[index] == GrayImage.BLACK) {
                    if (x < leftX) {
                        leftX = x;
                    }
                    if (x > rightX) {
                        rightX = x;
                    }
                    if (y < topY) {
                        topY = y;
                    }
                    if (y > bottomY) {
                        bottomY = y;
                    }
                }
            }
        }
        final byte[] cropped = cropImage(pixels, width, height, leftX, topY,
                rightX - leftX + 1, bottomY - topY + 1);
        mask.setX(mask.getX() + leftX);
        mask.setY(mask.getY() + topY);
        mask.setWidth(rightX - leftX + 1);
        mask.setHeight(bottomY - topY + 1);
        return cropped;
    }

    /**
     * Crops a grayimage to given width and height
     * 
     * @param image
     *            the image to crop
     * @param cropWidth
     *            the desired width
     * @param cropHeight
     *            the desired height
     * @return the cropped image
     * @throws IllegalArgumentException
     *             if the parameter width or height cannot be used
     */
    public static GrayImage cropGrayImage(final GrayImage image,
            final int cropWidth, final int cropHeight)
            throws IllegalArgumentException {
        final byte[] cropped = cropImage(image.getPixels(), image.getWidth(),
                image.getHeight(), 0, 0, cropWidth, cropHeight);
        return new GrayImage(cropped, cropWidth, cropHeight);
    }

    /**
     * Crops a grayimage to given bounds
     * 
     * @param image
     *            the image to crop
     * @param mask
     *            the bounds
     * @return the cropped image
     * @throws IllegalArgumentException
     *             if the bounds do not fit the image
     */
    public static GrayImage cropGrayImage(final GrayImage image,
            final Rectangle mask) throws IllegalArgumentException {
        final byte[] cropped = cropImage(image.getPixels(), image.getWidth(),
                image.getHeight(), (int) mask.getX(), (int) mask.getY(),
                (int) mask.getWidth(), (int) mask.getHeight());
        return new GrayImage(cropped, (int) mask.getWidth(),
                (int) mask.getHeight());
    }

    /**
     * Crops a an image to given bounds
     * 
     * @param pixels
     *            the pixels of the image
     * @param width
     *            the width of the image
     * @param height
     *            the height of the image
     * @param mask
     *            reference to the bounds (will be changed if the image is
     *            cropped)
     * @return the cropped image as byte[]
     * @throws IllegalArgumentException
     *             if the bounds do not fit the image
     */
    public static byte[] cropImage(final byte[] pixels, final int width,
            final int height, final Rectangle mask)
            throws IllegalArgumentException {
        return cropImage(pixels, width, height, (int) mask.getX(),
                (int) mask.getY(), (int) mask.getWidth(),
                (int) mask.getHeight());
    }

    /**
     * This method is used to crop the image according to the given parameters
     * 
     * @param pixels
     *            the pixels of original image
     * @param width
     *            width of image
     * @param height
     *            height of image
     * @param x
     *            the start x coordinate
     * @param y
     *            the start y coordinate
     * @param cropWidth
     *            the cropped width
     * @param cropHeight
     *            the cropped height
     * @return cropped pixels
     * @throws IllegalArgumentException
     *             if the bounds do not fit the image
     */
    public static byte[] cropImage(final byte[] pixels, final int width,
            final int height, final int x, final int y, final int cropWidth,
            final int cropHeight) throws IllegalArgumentException {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException(
                    "x and y must be greater than or equal 0");
        } else if (width <= x || height <= y) {
            throw new IllegalArgumentException(
                    "x and y must be smaller than width or height");
        } else if (cropWidth < 0 || cropHeight < 0) {
            throw new IllegalArgumentException(
                    "cropWidth and cropHeight must be greater than 0");
        } else if (width < cropWidth || height < cropHeight) {
            throw new IllegalArgumentException(
                    "cropWidth and cropHeight must be smaller than width or height");
        } else if ((x + cropWidth) > width) {
            throw new IllegalArgumentException(
                    "cropWidth + x must be smaller than width");
        } else if ((y + cropHeight) > height) {
            DiptychonLogger.error("Height + 1 <= y + cropHeight");
            DiptychonLogger.error("{} <= {} + {} {}", height + 1, y,
                    cropHeight, (height + 1 <= (y + cropHeight)));
            DiptychonLogger.error("cropHeight + y must be smaller than height");
            throw new IllegalArgumentException(
                    "cropHeight + y must be smaller than height");
        }

        final byte[] cropped = new byte[cropWidth * cropHeight];
        for (int j = y; j < y + cropHeight; ++j) {
            final int offsetImage = j * width;
            final int offsetCropped = (j - y) * (cropWidth);
            for (int i = x; i < x + cropWidth; ++i) {
                final int indexImage = i + offsetImage;
                final int indexCropped = (i - x) + offsetCropped;
                cropped[indexCropped] = pixels[indexImage];
            }
        }
        return cropped;
    }

    /**
     * Writes a JavaFX Image to OutputStream s
     * 
     * @param s
     *            the OutputStream
     * @param image
     *            the image
     * @throws IOException
     *             thrown when outputstream is null or not writable
     */
    public static void FXimageToOutputStream(
            final java.io.ObjectOutputStream s, final Image image)
            throws IOException {
        final int imgWidth = (int) image.getWidth();
        final int imgHeight = (int) image.getHeight();
        final int[] rawPixels = new int[imgWidth * imgHeight];
        image.getPixelReader().getPixels(0, 0, imgWidth, imgHeight,
                PixelFormat.getIntArgbInstance(), rawPixels, 0, imgWidth);
        s.writeInt(imgWidth);
        s.writeInt(imgHeight);
        s.writeObject(rawPixels);
    }

    /**
     * Reads a JavaFX Image to InputStream s
     * 
     * @param s
     *            the InputStream
     * @return the read image
     * @throws IOException
     *             thrown when InputStream is null or not readable
     * @throws ClassNotFoundException
     *             exception
     */
    public static Image FXimageFromInputStream(final java.io.ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        final int imgWidth = s.readInt();
        final int imgHeight = s.readInt();
        int[] rawPixels = new int[imgWidth * imgHeight];
        rawPixels = (int[]) s.readObject();
        return ImageUtils.getJavaFXImage(rawPixels, imgWidth, imgHeight);
    }
}
