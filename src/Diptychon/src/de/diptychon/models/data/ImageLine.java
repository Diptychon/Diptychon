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
package de.diptychon.models.data;

import java.awt.Point;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import de.diptychon.DiptychonLogger;
import de.diptychon.models.algorithms.validation.featureExtraction.regionBased.Projection;
import de.diptychon.models.misc.GrayImage;
import de.diptychon.models.misc.ImageUtils;

/**
 * Represents an imageline within a documentimage
 */
public class ImageLine implements Serializable, Comparable<ImageLine> {
    /**
     * The prefix for line ids
     */
    public static final String ID_PREFIX = "L_";

    /**
     * SerialVersionUID to keep used data consistent.
     */
    private static final long serialVersionUID = 20121107;

    /**
     * If there is no connected component in a column of the binary image of
     * that image line, e.g. between words
     */
    public static final int EMPTY_SPACE = -1;

    /**
     * The bounding box of this line will be enlarged by {@value} pixel to check
     * whether a glyph is inside the line or not. The enlargement ensures that
     * even slightly larger glyphs which might exceed the size of a line will be
     * considered to be a line member.
     */
    private static final int ENLARGEMENT = 3;

    /**
     * ENLARGEMENT * 2
     */
    private static final int ENLARGEMENT_2 = ENLARGEMENT << 1;

    /**
     * the bounds of this line
     */
    private transient BoundingBox boundingBox;

    /**
     * All glyphs within this line
     */
    private final HashMap<String, Glyph> glyphs;

    /**
     * the id of this line
     */
    private String ID;

    /**
     * the baseline
     */
    private transient Line baseline;

    /**
     * the upperline
     */
    private transient Line upperLine;

    /**
     * the lowerline
     */
    private transient Line lowerLine;

    /**
     * <code>True</code> if this is a handwritten annotation, <code>false</code>
     * otherwise
     */
    private boolean handwrittenAnnotation;

    /**
     * min and max coordinates of the heights of connected components of this
     * image line
     */
    private int[][] topsAndBottomsOfWriting = null;

    /**
     * Creates a new line with bounds rectangle
     * 
     * @param rectangle
     *            the bounds of the new line
     */
    public ImageLine(final Rectangle rectangle) {
        this(new Line(rectangle.getX(), rectangle.getY(), rectangle.getX()
                + rectangle.getWidth(), rectangle.getY()), new Line(
                rectangle.getX(), rectangle.getY() + rectangle.getHeight(),
                rectangle.getX() + rectangle.getWidth(), rectangle.getY()
                        + rectangle.getHeight()));

    }

    /**
     * Creates a new line by using the upper and lowerline. Baseline is supposed
     * to be located a 1/3 upper-lowerline
     * 
     * @param pUpperline
     *            the upperline
     * @param pLowerline
     *            the lowerline
     */
    public ImageLine(final Line pUpperline, final Line pLowerline) {
        // final double startY = pLowerline.getStartY() -
        // (pLowerline.getStartY() - pUpperline.getStartY()) / 3;
        // final double endY = pLowerline.getEndY() - (pLowerline.getEndY() -
        // pUpperline.getEndY()) / 3;
        this(new Line(pUpperline.getStartX(), pLowerline.getStartY()
                - (pLowerline.getStartY() - pUpperline.getStartY()) / 3,
                pUpperline.getEndX(), pLowerline.getEndY()
                        - (pLowerline.getEndY() - pUpperline.getEndY()) / 3),
                pUpperline, pLowerline);

    }

    /**
     * Creates a new line with all necessary information
     * 
     * @param pBaseline
     *            the baseline
     * @param pUpperline
     *            the upperline
     * @param pLowerline
     *            the lowerline
     */
    public ImageLine(final Line pBaseline, final Line pUpperline,
            final Line pLowerline) {
        this.glyphs = new HashMap<>();
        this.baseline = pBaseline;
        this.baseline.setStroke(Color.BLUE);
        this.upperLine = pUpperline;
        this.upperLine.setStroke(Color.GREEN);
        this.lowerLine = pLowerline;
        this.lowerLine.setStroke(Color.RED);
        this.boundingBox = new BoundingBox(this.upperLine.getStartX(),
                this.upperLine.getStartY(), this.lowerLine.getEndX()
                        - this.upperLine.getStartX(), this.lowerLine.getEndY()
                        - this.upperLine.getStartY());
        this.handwrittenAnnotation = false;
    }

    /**
     * Gets the most upper left point
     * 
     * @return the most up left point
     */
    public Point getUpperLeftPoint() {
        return new Point((int) this.boundingBox.getMinX(),
                (int) this.boundingBox.getMinY());
    }

    /**
     * Gets the most lower right point
     * 
     * @return the most lower right point
     */
    public Point getLowerRightPoint() {
        return new Point((int) this.boundingBox.getMaxX(),
                (int) this.boundingBox.getMaxY());
    }

    /**
     * Checks whether this line is marked as handwritten annotation
     * 
     * @return <code>true</code> if this is a handwritten annotation,
     *         <code>false</code> otherwise
     */
    public boolean isHandwrittenAnnotation() {
        return this.handwrittenAnnotation;
    }

    /**
     * Inverts the current handwritten annotation status
     */
    public void setHandwrittenAnnotation() {
        this.handwrittenAnnotation = !this.handwrittenAnnotation;
    }

    /**
     * Moves this line by deltaX and deltaY and fixes the width accordingly
     * 
     * @param deltaX
     *            move the line by
     * @param deltaY
     *            move the line by
     * @param width
     *            the new width of the image
     */
    public void moveByAfterMaskingAndFixWidth(final int deltaX,
            final int deltaY, final int width) {
        double newStartX = this.baseline.getStartX() + deltaX;
        double newEndX = this.baseline.getEndX() + deltaX;
        if (newStartX < 0) {
            newStartX = 0;
        }
        if (newEndX >= width) {
            newEndX = width - 1;
        }

        this.boundingBox = new BoundingBox(newStartX, this.getLayoutY()
                + deltaY, newEndX - newStartX, this.getHeight());

        this.baseline.setStartX(newStartX);
        this.baseline.setEndX(newEndX);
        this.baseline.setStartY(this.baseline.getStartY() + deltaY);
        this.baseline.setEndY(this.baseline.getEndY() + deltaY);

        this.upperLine.setStartX(newStartX);
        this.upperLine.setEndX(newEndX);
        this.upperLine.setStartY(this.upperLine.getStartY() + deltaY);
        this.upperLine.setEndY(this.upperLine.getEndY() + deltaY);

        this.lowerLine.setStartX(newStartX);
        this.lowerLine.setEndX(newEndX);
        this.lowerLine.setStartY(this.lowerLine.getStartY() + deltaY);
        this.lowerLine.setEndY(this.lowerLine.getEndY() + deltaY);
    }

    /**
     * Gets the x coordinate of this line
     * 
     * @return the x coordinate
     */
    public int getLayoutX() {
        return (int) this.boundingBox.getMinX();
    }

    public void setLayoutX(double minX) {
        this.boundingBox = new BoundingBox(minX, this.boundingBox.getMinY(),
                this.boundingBox.getWidth(), this.boundingBox.getHeight());
    }

    /**
     * Gets the y coordinate of this line
     * 
     * @return the y coordinate
     */
    public int getLayoutY() {
        return (int) this.boundingBox.getMinY();
    }

    public void setLayoutY(double minY) {
        this.boundingBox = new BoundingBox(this.boundingBox.getMinX(), minY,
                this.boundingBox.getWidth(), this.boundingBox.getHeight());
    }

    /**
     * This method is used to get the width of glyph
     * 
     * @return width
     */
    public double getWidth() {
        return this.boundingBox.getWidth();
    }

    public void setWidth(double width) {
        this.boundingBox = new BoundingBox(this.boundingBox.getMinX(),
                this.boundingBox.getMinY(), width, this.boundingBox.getHeight());
    }

    /**
     * This method is used to get the height of glyph
     * 
     * @return height
     */
    public double getHeight() {
        return this.boundingBox.getHeight();
    }

    public void setHeight(double height) {
        this.boundingBox = new BoundingBox(this.boundingBox.getMinX(),
                this.boundingBox.getMinY(), this.boundingBox.getWidth(), height);
    }

    /**
     * Sets the id of this line
     * 
     * @param pID
     *            the id
     */
    public void setID(final int pID) {
        this.ID = ID_PREFIX + pID;
    }

    /**
     * Gets the baseline of this line
     * 
     * @return the baseline
     */
    public Line getBaseline() {
        return this.baseline;
    }

    /**
     * Gets the upperline of this line
     * 
     * @return the upperline
     */

    public Line getUpperline() {
        return this.upperLine;
    }

    /**
     * Gets the lowerline of this line
     * 
     * @return the lowerline
     */
    public Line getLowerline() {
        return this.lowerLine;
    }

    /**
     * Checks whether a line is contained in this one
     * 
     * @param imageLineOther
     *            the other line
     * @return <code>True</code> if the line is contained, <code>false</code>
     *         otherwise
     */
    public boolean contains(final ImageLine imageLineOther) {
        final Line baselineOther = imageLineOther.getBaseline();

        boolean contained = this.boundingBox.contains(new Point2D(baselineOther
                .getStartX(), baselineOther.getStartY()))
                && this.boundingBox.contains(new Point2D(baselineOther
                        .getEndX(), baselineOther.getEndY()));

        if (!contained) {
            final Line upperlineOther = imageLineOther.getUpperline();
            final Line lowerlineOther = imageLineOther.getLowerline();

            contained = this.boundingBox.contains(new Point2D(upperlineOther
                    .getStartX(), upperlineOther.getStartY()))
                    && this.boundingBox.contains(new Point2D(lowerlineOther
                            .getEndX(), lowerlineOther.getEndY()));
            if (!contained) {
                contained = this.boundingBox
                        .contains(new Point2D(lowerlineOther.getStartX(),
                                lowerlineOther.getStartY()))
                        && this.boundingBox.contains(new Point2D(upperlineOther
                                .getEndX(), upperlineOther.getEndY()));
            }
        }

        return contained;
    }

    /**
     * Checks whether a glyph is contained in this line
     * 
     * @param glyph
     *            the glyph
     * @return <code>True</code> if the glyph is contained <code>false</code>
     *         otherwise
     */
    public boolean contains(final Glyph glyph) {
        return new BoundingBox(this.boundingBox.getMinX() - ENLARGEMENT,
                this.boundingBox.getMinY() - ENLARGEMENT,
                this.boundingBox.getWidth() + ENLARGEMENT_2,
                this.boundingBox.getHeight() + ENLARGEMENT_2).contains(glyph
                .getBoundingBox());
    }

    /**
     * Checks whether a glyph is contained in this line (without line
     * enlargement)
     * 
     * @param glyph
     *            the glyph
     * @return <code>True</code> if the glyph is contained <code>false</code>
     *         otherwise
     */
    public boolean contains2(final Glyph glyph) {
        return new BoundingBox(this.boundingBox.getMinX(),
                this.boundingBox.getMinY(), this.boundingBox.getWidth(),
                this.boundingBox.getHeight()).contains(glyph.getBoundingBox());
    }

    /**
     * Gets the id of this line
     * 
     * @return the id
     */
    public String getID() {
        return this.ID;
    }

    /**
     * Adds a glyph if it lays inside this line
     * 
     * @param glyph
     *            the glyph
     * @return <code>True</code> if the glyph was added, <code>false</code>
     *         otherwise
     */
    public boolean addGlyphIfContained(final Glyph glyph) {
        if (this.contains(glyph)) {
            DiptychonLogger.info(
                    "Added extracted glyph (ID: {}) to Line (ID: {})",
                    glyph.getID(), this.getID());
            glyph.setLineID(this.getID());
            this.glyphs.put(glyph.getID(), glyph);
            return true;
        }
        return false;
    }

    /**
     * Remove a glyph if it does not lay inside this line
     * 
     * @param glyph
     *            the glyph
     * @return <code>True</code> if the glyph was removed, <code>false</code>
     *         otherwise
     */
    public boolean removeGlyphIfNotContained(final Glyph glyph) {
        if (!this.contains(glyph)) {
            DiptychonLogger.info(
                    "Removed extracted glyph (ID: {}) from Line (ID: {})",
                    glyph.getID(), this.getID());
            glyph.setLineID(this.ID);
            // this.glyphs.remove(glyph.getID());
            return true;
        }
        return false;
    }

    /**
     * Adds a glyph to this line
     * 
     * @param glyph
     *            the glyph
     */
    public void addGlyph(final Glyph glyph) {
        glyph.setLineID(this.ID);
        this.glyphs.put(glyph.getID(), glyph);
    }

    /**
     * Gets all glyphs of this line as Collection
     * 
     * @return the glyphs
     */
    public Collection<Glyph> getGlyphs() {
        return this.glyphs.values();
    }

    /**
     * Gets all glyphs of this line as hashmap
     * 
     * @return the glyphs
     */
    public HashMap<String, Glyph> getGlyphsMap() {
        return this.glyphs;
    }

    /**
     * Gets the glyph with id id
     * 
     * @param id
     *            the id
     * @return the glyph
     */
    public Glyph getGlyph(final String id) {
        return this.glyphs.get(id);
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
        // DiptychonLogger.debug("Writing image line (ID: {}) to hdd",
        // this.getID());
        s.defaultWriteObject();
        // save boundingbox
        s.writeDouble(this.boundingBox.getMinX());
        s.writeDouble(this.boundingBox.getMinY());
        s.writeDouble(this.boundingBox.getWidth());
        s.writeDouble(this.boundingBox.getHeight());
        // save basline
        s.writeDouble(this.baseline.getStartX());
        s.writeDouble(this.baseline.getStartY());
        s.writeDouble(this.baseline.getEndX());
        s.writeDouble(this.baseline.getEndY());
        // save upperline
        s.writeDouble(this.upperLine.getStartX());
        s.writeDouble(this.upperLine.getStartY());
        s.writeDouble(this.upperLine.getEndX());
        s.writeDouble(this.upperLine.getEndY());
        // save lowerline
        s.writeDouble(this.lowerLine.getStartX());
        s.writeDouble(this.lowerLine.getStartY());
        s.writeDouble(this.lowerLine.getEndX());
        s.writeDouble(this.lowerLine.getEndY());
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
        // DiptychonLogger.debug("Read image line from hdd...");
        s.defaultReadObject();
        // DiptychonLogger.debug("Glyphs in line complete...");
        this.boundingBox = new BoundingBox(s.readDouble(), s.readDouble(),
                s.readDouble(), s.readDouble());
        this.baseline = new Line(s.readDouble(), s.readDouble(),
                s.readDouble(), s.readDouble());
        this.upperLine = new Line(s.readDouble(), s.readDouble(),
                s.readDouble(), s.readDouble());
        this.lowerLine = new Line(s.readDouble(), s.readDouble(),
                s.readDouble(), s.readDouble());
        // DiptychonLogger.debug("(ID: {}) done.", this.getID());
    }

    /**
     * Gets the bounds of this line
     * 
     * @return the bounds
     */
    public Rectangle getBounds() {
        return new Rectangle(this.boundingBox.getMinX(),
                this.boundingBox.getMinY(), this.boundingBox.getWidth(),
                this.boundingBox.getHeight());
    }

    public int getMinX() {
        return (int) this.boundingBox.getMinX();
    }

    public int getMaxX() {
        return (int) this.boundingBox.getMaxX();
    }

    public int getMinY() {
        return (int) this.boundingBox.getMinY();
    }

    public int getMaxY() {
        return (int) this.boundingBox.getMaxY();
    }

    /**
     * Splits a line into glyphs
     * 
     * @param original
     *            the original image
     * @param grayImage
     *            the gray image
     * @param binaryImage
     *            the binarized image
     * @param decoding
     *            the transcription
     * @param encoding
     *            the encoded transcription
     * @return split line suggestion
     */
    public Line[] split(final Image original, final GrayImage grayImage,
            final GrayImage binaryImage, final String[] decoding,
            final String[] encoding) {
        return this.split2(original, grayImage, binaryImage, decoding,
                encoding, -1);
    }

    /**
     * Splits a line into glyphs
     * 
     * @param original
     *            the original image
     * @param grayImage
     *            the gray image
     * @param binaryImage
     *            the binarized image
     * @param decoding
     *            the transcription
     * @param encoding
     *            the encoded transcription
     * @param width
     *            the estimated average character width
     * @return split line suggestion
     */

    public Line[] split(final Image original, final GrayImage grayImage,
            final GrayImage binaryImage, final String[] decoding,
            final String[] encoding, final int width) {
        final Line[] splits = new Line[decoding.length + 1];

        int spaceCounter = 0;
        final ArrayList<Integer> spaceIndices = new ArrayList<>();
        final ArrayList<Integer> numOfElementsBetweenSpaces = new ArrayList<>();
        int index = 0;
        int elementCounter = 0;
        for (final String s : decoding) {
            if (s.equals(" ")) {
                ++spaceCounter;
                spaceIndices.add(index);
                numOfElementsBetweenSpaces.add(elementCounter);
                elementCounter = -1;
            }
            ++elementCounter;
            ++index;
        }
        if (elementCounter != 0) {
            numOfElementsBetweenSpaces.add(elementCounter);
        }

        final Projection p = new Projection(binaryImage);
        final ArrayList<Point> gaps = p.findVerticalGaps(spaceCounter,
                numOfElementsBetweenSpaces, width, (int) original.getHeight());
        if (gaps.size() == 0 && spaceCounter != 0) {
            return null;
        }

        int lastSpace = 0;
        int endLastGap = 0;
        int gapIndex = 0;
        int splitIndex = 0;
        for (final Integer i : spaceIndices) {
            final int numOfElements = i - lastSpace;
            final int endWort = gaps.get(gapIndex).x;
            int splitAt = 0;
            if (numOfElements > 0) {
                splitAt = (endWort - endLastGap) / numOfElements;
            }
            for (int j = 0; j < numOfElements; ++j) {
                Color strokeColor;
                if (j == 0) {
                    strokeColor = Color.GREEN;
                } else {
                    strokeColor = Color.RED;
                }
                final int foo = j * splitAt + endLastGap;
                final Line l = new Line(this.getLayoutX() + foo,
                        this.getLayoutY(), this.getLayoutX() + foo,
                        this.getLayoutY() + this.getHeight());

                l.setStroke(strokeColor);
                l.setStrokeWidth(3);
                l.setOpacity(0.6);

                splits[splitIndex] = l;
                ++splitIndex;
            }
            final Line l = new Line(this.getLayoutX() + endWort,
                    this.getLayoutY(), this.getLayoutX() + endWort,
                    this.getLayoutY() + this.getHeight());
            l.setStroke(Color.GREEN);
            l.setStrokeWidth(3);
            l.setOpacity(0.6);
            splits[splitIndex] = l;
            endLastGap = gaps.get(gapIndex).y;
            lastSpace = i + 1;
            ++splitIndex;
            ++gapIndex;
        }

        final int numOfElements = decoding.length - lastSpace;
        int endWort;
        if (gaps.size() == gapIndex) {
            endWort = (int) this.getWidth();
        } else {
            endWort = gaps.get(gapIndex).x;
        }
        int splitAt = 0;
        if (numOfElements > 0) {
            splitAt = (endWort - endLastGap) / numOfElements;
        }
        for (int j = 0; j < numOfElements; ++j) {
            Color strokeColor;
            if (j == 0) {
                strokeColor = Color.GREEN;
            } else {
                strokeColor = Color.RED;
            }
            final int foo = j * splitAt + endLastGap;
            final Line l = new Line(this.getLayoutX() + foo, this.getLayoutY(),
                    this.getLayoutX() + foo, this.getLayoutY()
                            + this.getHeight());

            l.setStroke(strokeColor);
            l.setStrokeWidth(3);
            l.setOpacity(0.6);

            splits[splitIndex] = l;
            ++splitIndex;
        }
        final Line l = new Line(this.getLayoutX() + endWort, this.getLayoutY(),
                this.getLayoutX() + endWort, this.getLayoutY()
                        + this.getHeight());
        l.setStroke(Color.GREEN);
        l.setStrokeWidth(3);
        l.setOpacity(0.6);
        splits[splitIndex] = l;
        return splits;
    }

    /**
     * Splits a line into words and runs the Recursive Glyph Fragmentation
     * 
     * @param original
     *            the original image
     * @param grayImage
     *            the gray image
     * @param binaryImage
     *            the binarized image
     * @param decoding
     *            the transcription
     * @param encoding
     *            the encoded transcription
     * @return split line suggestion
     */
    public Line[] split2(final Image original, final GrayImage grayImage,
            final GrayImage binaryImage, final String[] decoding,
            final String[] encoding) {
        return this.split2(original, grayImage, binaryImage, decoding,
                encoding, -1);
    }

    /**
     * Splits a line into words and runs the Recursive Glyph Fragmentation
     * 
     * @param original
     *            the original image
     * @param grayImage
     *            the gray image
     * @param binaryImage
     *            the binarized image
     * @param decoding
     *            the transcription
     * @param encoding
     *            the encoded transcription
     * @param width
     *            the estimated average character width
     * @return split line suggestion
     */

    public Line[] split2(final Image original, final GrayImage grayImage,
            final GrayImage binaryImage, final String[] decoding,
            final String[] encoding, final int width) {

        int spaceCounter = 0;
        final ArrayList<Integer> spaceIndices = new ArrayList<>();
        final ArrayList<Integer> numOfElementsBetweenSpaces = new ArrayList<>();
        int index = 0;
        int elementCounter = 0;
        for (final String s : decoding) {
            if (s.equals(" ")) {
                ++spaceCounter;
                spaceIndices.add(index);
                numOfElementsBetweenSpaces.add(elementCounter);
                elementCounter = -1;
            }
            ++elementCounter;
            ++index;
        }
        if (elementCounter != 0) {
            numOfElementsBetweenSpaces.add(elementCounter);
        }

        final Line[] splits = new Line[spaceCounter * 1 + 2];

        final Projection p = new Projection(binaryImage);
        final ArrayList<Point> gaps = p.findVerticalGaps(spaceCounter,
                numOfElementsBetweenSpaces, width, (int) original.getHeight());
        if (gaps.size() == 0 && spaceCounter != 0) {
            return null;
        }

        int lastSpace = 0;
        int endLastGap = 0;
        int gapIndex = 0;
        int splitIndex = 0;
        for (final Integer i : spaceIndices) {
            final int numOfElements = i - lastSpace;
            final int endWort = gaps.get(gapIndex).x;
            int splitAt = 0;
            if (numOfElements > 0) {
                splitAt = (endWort - endLastGap) / numOfElements;
            }
            for (int j = 0; j < numOfElements; ++j) {
                Color strokeColor;
                if (j == 0) {
                    strokeColor = Color.GREEN;
                } else {
                    continue;
                }
                final int foo = j * splitAt + endLastGap;
                final Line l = new Line(this.getLayoutX() + foo,
                        this.getLayoutY(), this.getLayoutX() + foo,
                        this.getLayoutY() + this.getHeight());

                l.setStroke(strokeColor);
                l.setStrokeWidth(3);
                l.setOpacity(0.6);

                splits[splitIndex] = l;
                ++splitIndex;
            }
            // final Line l = new Line(this.getLayoutX() + endWort,
            // this.getLayoutY(), this.getLayoutX() + endWort,
            // this.getLayoutY() + this.getHeight());
            // l.setStroke(Color.GREEN);
            // l.setStrokeWidth(3);
            // l.setOpacity(0.6);
            // splits[splitIndex] = l;
            endLastGap = gaps.get(gapIndex).y;
            lastSpace = i + 1;
            // ++splitIndex;
            ++gapIndex;
        }

        final int numOfElements = decoding.length - lastSpace;
        int endWort;
        if (gaps.size() == gapIndex) {
            endWort = (int) this.getWidth();
        } else {
            endWort = gaps.get(gapIndex).x;
        }
        int splitAt = 0;
        if (numOfElements > 0) {
            splitAt = (endWort - endLastGap) / numOfElements;
        }
        for (int j = 0; j < numOfElements; ++j) {
            Color strokeColor;
            if (j == 0) {
                strokeColor = Color.GREEN;
            } else {
                continue;
            }
            final int foo = j * splitAt + endLastGap;
            final Line l = new Line(this.getLayoutX() + foo, this.getLayoutY(),
                    this.getLayoutX() + foo, this.getLayoutY()
                            + this.getHeight());

            l.setStroke(strokeColor);
            l.setStrokeWidth(3);
            l.setOpacity(0.6);

            splits[splitIndex] = l;
            ++splitIndex;
        }
        final Line l = new Line(this.getLayoutX() + endWort, this.getLayoutY(),
                this.getLayoutX() + endWort, this.getLayoutY()
                        + this.getHeight());
        l.setStroke(Color.GREEN);
        l.setStrokeWidth(3);
        l.setOpacity(0.6);
        splits[splitIndex] = l;
        return splits;
    }

    /**
     * Splits a line into glyphs
     * 
     * @param splits
     *            the splitline suggestion
     * @param original
     *            the original image
     * @param grayImage
     *            the gray image
     * @param binaryImage
     *            the binarized image
     * @param decoding
     *            the transcription
     * @param encoding
     *            the encoded transcription
     */

    public void split(final ArrayList<Line> splits, final Image original,
            final GrayImage grayImage, final GrayImage binaryImage,
            final String[] decoding, final String[] encoding) {
        System.out.println("================> ImageLine.java: split");
        Collections.sort(splits, new Comparator<Line>() {
            @Override
            public int compare(final Line o1, final Line o2) {
                final String[] split1 = o1.getId().split("_");
                final String[] split2 = o2.getId().split("_");
                final int id1 = Integer.valueOf(split1[split1.length - 1]);
                final int id2 = Integer.valueOf(split2[split2.length - 1]);
                if (id1 < id2) {
                    return -1;
                } else if (id1 == id2) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });

        for (int i = 0; i < splits.size() - 1; ++i) {
            final Line start = splits.get(i);
            final Line end = splits.get(i + 1);

            final Rectangle crop = new Rectangle(start.getStartX()
                    - this.getLayoutX(), 0, end.getEndX() - start.getStartX(),
                    this.getHeight());

            GrayImage gBinaryImage = ImageUtils
                    .cropGrayImage(binaryImage, crop);

            Image gOriginal = null;
            GrayImage gGrayImage = null;
            Glyph g = null;
            try {
                gBinaryImage = ImageUtils.autoCrop(gBinaryImage, crop);
                gOriginal = ImageUtils.cropFXImage(original, crop);
                gGrayImage = ImageUtils.cropGrayImage(grayImage, crop);
                g = Glyph.extractGlyph(
                        gOriginal,
                        gGrayImage,
                        gBinaryImage,
                        new Rectangle(this.getLayoutX() + crop.getX(), this
                                .getLayoutY() + crop.getY(), gGrayImage
                                .getWidth(), gGrayImage.getHeight()));

            } catch (final IllegalArgumentException e) {
                // no need to handle since that exception is an evidence for a
                // space character
            }

            if (g != null && g.getShapeToDraw(Color.GREEN) != null) {
                g.setID(encoding[i]);
                g.setLineID(this.ID);
                if (decoding[i].startsWith("$")) {
                    g.setGroupID(decoding[i].substring(1,
                            decoding[i].length() - 1));
                } else {
                    g.setGroupID(decoding[i]);
                }
                this.glyphs.put(g.getID(), g);
            } else {
                g = new Glyph(new Rectangle(this.getLayoutX() + crop.getX(),
                        this.getLayoutY() + crop.getY(), 1, 1));
                g.setLineID(this.ID);
                g.setID(encoding[i]);
                g.setGroupID(decoding[i]);
                this.glyphs.put(g.getID(), g);
            }
        }

    }

    /**
     * Updates the position and size of this line
     * 
     * @param rectangle
     *            the new position and size
     */
    public void updateSize(final Rectangle rectangle) {
        this.boundingBox = new BoundingBox(rectangle.getX(), rectangle.getY(),
                rectangle.getWidth(), rectangle.getHeight());
        this.upperLine = new Line(rectangle.getX(), rectangle.getY(),
                rectangle.getX() + rectangle.getWidth(), rectangle.getY());

        this.lowerLine = new Line(rectangle.getX(), rectangle.getY()
                + rectangle.getHeight(), rectangle.getX()
                + rectangle.getWidth(), rectangle.getY()
                + rectangle.getHeight());
    }

    /**
     * Removes all glyphs from this line
     */
    public void removeGlyphs() {
        this.glyphs.clear();
    }

    /**
     * Updates the ids of this line if the transcription was changed
     * 
     * @param decoded
     *            the transcription
     * @param encoding
     *            the encoded transcription
     */
    public void updateGlyphIDs(final String[] decoded, final String[] encoding) {
        int index = 0;
        for (final String e : encoding) {
            if (!decoded[index].equals(" ")) {
                if (decoded[index].startsWith("$")) {
                    this.glyphs.get(e).setGroupID(
                            decoded[index].substring(1,
                                    decoded[index].length() - 1));
                } else {
                    this.glyphs.get(e).setGroupID(decoded[index]);
                }
            }
            ++index;
        }
    }

    @Override
    public int compareTo(final ImageLine other) {
        if (this.boundingBox.getMinY() < other.boundingBox.getMinY()) {
            return -1;
        }
        if (this.boundingBox.getMinY() > other.boundingBox.getMinY()) {
            return 1;
        } else {
            if (this.boundingBox.getMinX() < other.boundingBox.getMinX()) {
                return -1;
            }
            if (this.boundingBox.getMinX() > other.boundingBox.getMinX()) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * Removes the glyph with id id
     * 
     * @param id
     *            the id
     * @return the glyph
     */
    public Glyph removeGlyph(final String id) {
        return this.glyphs.remove(id);
    }

    /**
     * Sets the glyphs for this line
     * 
     * @param pGlyphs
     *            the glyphs
     */
    public void setGlyphs(final ArrayList<Glyph> pGlyphs) {
        for (final Glyph g : pGlyphs) {
            this.addGlyph(g);
        }
    }

    /**
     * Replaces a glyph by another one
     * 
     * @param glyph
     *            the new glyph
     */
    public void replaceGlyph(final Glyph glyph) {
        this.glyphs.put(glyph.getID(), glyph);
    }

    // ********************************************************
    // Methods for anylsing the binary image of that image line
    // ********************************************************

    /**
     * Determines the minimal and maximal vertical positions of all connected
     * components within this image line
     * 
     * @param binaryImage
     *            the whole binarized document image
     * 
     * @param imageWidth
     *            of the whole binarized document image
     * 
     * @param imageHeight
     *            of the whole binarized document image
     */

    public int[][] verticalMinsAndMaxes(final byte[] binaryImage,
            final int imageWidth, final int imageHeight) {
        if (topsAndBottomsOfWriting != null)
            return topsAndBottomsOfWriting;

        int yMin = (int) this.boundingBox.getMinY(); // top of line
        int yMax = (int) this.boundingBox.getMaxY(); // bottom of line
        int xMin = (int) this.boundingBox.getMinX(); // left of line
        int xMax = (int) this.boundingBox.getMaxX(); // right of line

        // determine binary image line
        // final byte[] croppedPart = ImageUtils.cropImage(binaryImage,
        // imageWidth, imageHeight, xMin, yMin, xMax-xMin, yMax-yMin);
        // final GrayImage binaryImageLine = new GrayImage(croppedPart,
        // xMax-xMin, yMax-yMin);
        // ImageUtils.writeGrayscaleImage("CC\\TextLine.png",
        // binaryImageLine);

        GrayImage binaryImageLine = new GrayImage(binaryImage, imageWidth,
                imageHeight);
        topsAndBottomsOfWriting = new int[imageWidth][2];

        for (int x = xMin; x < xMax; x++) // for all columns of this text line
        {
            boolean found = false;
            int y = yMin;
            while (y++ < yMax && !found) // determine first black pixel
                                         // beginning from the top of the text
                                         // line
            {
                found = binaryImageLine.getPixelAsInt(x, y) == GrayImage.BLACK;
            }
            if (found)
                topsAndBottomsOfWriting[x][0] = --y;
            else
                topsAndBottomsOfWriting[x][0] = -1;

            found = false;
            y = yMax;
            while (y-- >= yMin && !found) // determine first black pixel
                                          // beginning from the bottom of the
                                          // text line
            {
                found = binaryImageLine.getPixelAsInt(x, y) == GrayImage.BLACK;
            }
            if (found)
                topsAndBottomsOfWriting[x][1] = ++y;
            else
                topsAndBottomsOfWriting[x][1] = -1;
        }

        // zur Überprüfung der Korrektheit Bild mit grauem Ober- und Unterrand
        // abspeichern
        /*
         * for(int x = 0; x < binaryImageLine.getWidth(); x++) for(int y = 0; y
         * < 2; y++) if (topsAndBottomsOfWriting[x][y] != -1)
         * binaryImageLine.setToGrey(x, topsAndBottomsOfWriting[x][y], 128);
         * ImageUtils.writeGrayscaleImage( "CC\\TextLineWithGreyBoundary.png",
         * binaryImageLine);
         */
        return topsAndBottomsOfWriting;
    }
}
