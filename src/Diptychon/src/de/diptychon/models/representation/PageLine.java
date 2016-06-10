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
package de.diptychon.models.representation;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import javafx.geometry.BoundingBox;
import javafx.scene.shape.Rectangle;

/**
 * Represents a PageLine
 */
public class PageLine implements Serializable, Comparable<PageLine> {
    /**
     * SerialVersionUID to keep used data consistent.
     */
    private static final long serialVersionUID = 20121107;

    /**
     * The id this line
     */
    private final String ID;

    /**
     * The boundingbox of this line
     */
    private transient BoundingBox boundingBox;

    /**
     * The encoded Transcription of this line
     */
    private String[] encoding;

    /**
     * <code>true</code> if this line is marked as handwritten annotation,
     * <code>false</code> otherwise
     */
    private boolean handwrittenAnnotation;

    /**
     * Creates a new PageLine with id <code>pID</code> and position and size
     * <code>bounds</code>
     * 
     * @param pID
     *            the id of the line
     * @param bounds
     *            the position and size of the line
     */
    public PageLine(final String pID, final Rectangle bounds) {
        this.boundingBox = new BoundingBox(bounds.getX(), bounds.getY(),
                bounds.getWidth(), bounds.getHeight());
        this.ID = pID;
        this.encoding = new String[] { "" };
        this.handwrittenAnnotation = false;
    }

    /**
     * Gets the id of this line
     * 
     * @return the id of this line
     */
    public String getID() {
        return this.ID;
    }

    /**
     * Checks whether this line is marked as handwritten annotation
     * 
     * @return <code>true</code> if this line is marked as handwritten
     *         annotation, <code>false</code> otherwise
     */
    public boolean isHandwrittenAnnotation() {
        return this.handwrittenAnnotation;
    }

    /**
     * Marks this line to handwritten annotation (if it was not before,
     * otherwise it is unset)
     */
    public void setHandwrittenAnnotation() {
        this.handwrittenAnnotation = !this.handwrittenAnnotation;
    }

    /**
     * Adds an encoding to the current transcription
     * 
     * @param pEncoding
     *            the encoding to add
     */
    public void addCharacterToEncoding(final String pEncoding) {
        final ArrayList<String> tmp = new ArrayList<String>(
                Arrays.asList(this.encoding));
        tmp.add(pEncoding);
        this.encoding = new String[tmp.size()];
        this.encoding = tmp.toArray(this.encoding);
    }

    /**
     * Sets the encoded transcription of this line
     * 
     * @param pEncoding
     *            the encoded transcription
     */
    public void transcribe(final String[] pEncoding) {
        this.encoding = pEncoding;
    }

    /**
     * Gets the encoded transcription of this line
     * 
     * @return the encoded transcription
     */
    public String[] getEncoding() {
        return this.encoding;
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
        // DiptychonLogger.debug("Writing page line (ID: {}) to hdd",
        // this.getID());
        s.defaultWriteObject();
        final double x = this.boundingBox.getMinX();
        final double y = this.boundingBox.getMinY();
        final double shapeWidth = this.boundingBox.getWidth();
        final double shapeHeight = this.boundingBox.getHeight();
        s.writeDouble(x);
        s.writeDouble(y);
        s.writeDouble(shapeWidth);
        s.writeDouble(shapeHeight);
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
        final double x = s.readDouble();
        final double y = s.readDouble();
        final double shapeWidth = s.readDouble();
        final double shapeHeight = s.readDouble();
        this.boundingBox = new BoundingBox(x, y, shapeWidth, shapeHeight);
        // DiptychonLogger.debug("Read page line (ID: {}) from hdd",
        // this.getID());
    }

    /**
     * Gets the bounds of this line
     * 
     * @return the bounds of this line
     */
    public Rectangle getBounds() {
        return new Rectangle(this.boundingBox.getMinX(),
                this.boundingBox.getMinY(), this.boundingBox.getWidth(),
                this.boundingBox.getHeight());
    }

    /**
     * Updates the size of this line
     * 
     * @param rectangle
     *            the new size of this line
     */
    public void updateSize(final Rectangle rectangle) {
        this.boundingBox = new BoundingBox(rectangle.getX(), rectangle.getY(),
                rectangle.getWidth(), rectangle.getHeight());
    }

    /**
     * Checks whether this line was already transcribed or not
     * 
     * @return <code>true</code> if this line has not be transcribed,
     *         <code>false</code> otherwise
     */
    public boolean isEmpty() {
        return this.encoding == null
                || (this.encoding.length == 1 && this.encoding[0].isEmpty());
    }

    /**
     * Removes the transcription of this line
     */
    public void clearTranscription() {
        this.encoding = new String[] {};
    }

    @Override
    public int compareTo(final PageLine other) {
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
}
