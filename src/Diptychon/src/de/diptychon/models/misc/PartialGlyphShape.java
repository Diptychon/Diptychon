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

import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

/**
 * Represents a part of a glyph shape
 */
public class PartialGlyphShape implements Serializable {
    /**
     * SerialVersionUID to keep used data consistent.
     */
    private static final long serialVersionUID = 20121101;

    /**
     * <code>true</code> if this part is active, <code>false</code> otherwise
     */
    private boolean isActive;

    /**
     * The shape of this part
     */
    private transient Shape shape;

    /**
     * Creates a new partial shape, initially set to active
     * 
     * @param pShape
     *            the shape
     */
    public PartialGlyphShape(final Shape pShape) {
        this.shape = pShape;
        this.isActive = true;
    }

    /**
     * Gets the shape
     * 
     * @return the shape
     */
    public Shape getShape() {
        return this.shape;
    }

    /**
     * Sets the shape
     * 
     * @param pShape
     *            the shape
     */
    public void setShape(final Shape pShape) {
        this.shape = pShape;
    }

    /**
     * Sets the color to fill this shape with
     * 
     * @param color
     *            the color to fill this shape with
     */
    public void setFill(final Color color) {
        this.shape.setFill(color);
    }

    /**
     * Checks whether this shape is active or not
     * 
     * @return <code>true</code> if this part is active, <code>false</code>
     *         otherwise
     */
    public boolean isActive() {
        return this.isActive;
    }

    /**
     * Sets this shape to <code>active</code>
     * 
     * @param active
     *            <code>true</code> if this part is active, <code>false</code>
     *            otherwise
     */
    public void setActive(final boolean active) {
        this.isActive = active;
    }

    /**
     * Inverts the active flag of this shape
     */
    public void invertActive() {
        this.isActive = !this.isActive;
    }

    /**
     * Gets the width of this shape
     * 
     * @return the width of this shape
     */
    public int getWidth() {
        return (int) this.shape.getLayoutBounds().getWidth();
    }

    /**
     * Gets the height of this shape
     * 
     * @return the height of this shape
     */
    public int getHeight() {
        return (int) this.shape.getLayoutBounds().getHeight();
    }

    /**
     * Gets the x coordinate of this shape
     * 
     * @return the x coordinate of this shape
     */
    public int getX() {
        return (int) this.shape.getLayoutX();
    }

    /**
     * Gets the y coordinate of this shape
     * 
     * @return the y coordinate of this shape
     */
    public int getY() {
        return (int) this.shape.getLayoutY();
    }

    /**
     * Moves the x coordinate of this shape by x
     * 
     * @param x
     *            the distance to move this shape
     */
    public void moveXBy(final int x) {
        this.shape.setLayoutX(this.getX() + x);
    }

    /**
     * Moves the y coordinate of this shape by y
     * 
     * @param y
     *            the distance to move this shape
     */
    public void moveYBy(final int y) {
        this.shape.setLayoutY(this.getY() + y);
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
    }
}
