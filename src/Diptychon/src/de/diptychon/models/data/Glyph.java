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

import java.awt.Dimension;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javafx.geometry.BoundingBox;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import de.diptychon.models.algorithms.contourExtraction.ContourSet;
import de.diptychon.models.algorithms.contourExtraction.ContourTracer;
import de.diptychon.models.misc.GrayImage;
import de.diptychon.models.misc.ImageUtils;
import de.diptychon.models.misc.PartialGlyphShape;

/**
 * This class represents a glyph: its connection to a document image, transcript
 * and its properties for displaying it on the user interface.
 */
public class Glyph implements Serializable, Comparable<Glyph> {
    /**
     * The prefix for glyph ids
     */
    public static final String ID_PREFIX = "G_";

    /**
     * SerialVersionUID to keep used data consistent.
     */
    private static final long serialVersionUID = 20121101;

    /**
     * The default color for displaying glyphs
     */
    private transient static final Color FILL = Color.LIGHTGREEN;

    /**
     * image contain the glyph
     */
    private transient Image glyphImage;

    /**
     * The corresponding grayimage
     */
    private GrayImage grayImage;

    /**
     * The corresponding binarized image
     */
    private GrayImage binarizedImage;

    /**
     * The corresponding binarized image
     */
    private int size = 0;

    /**
     * the bounds of this glyph
     */
    private transient BoundingBox boundingBox;

    /**
     * the partial glyph shapes this glyph consists of (for the display on the
     * user interface)
     */
    private ArrayList<PartialGlyphShape> glyph;

    /**
     * the id
     */
    private String ID;

    /**
     * the group id (e.g. all 'a' have group id 'a'), i.e. transcript
     */
    private String groupID;

    /**
     * the id of the line this glyph is contained in
     */
    private String lineID;

    /**
     * true if the glyph is a square fragment
     */
    public boolean isSquareFragment;

    /**
     * true if the glyph is a space object
     */
    public boolean isSpace;

    public boolean isEditorGlyph;

    /**
     * true if the glyph is a handwritten annotation
     */
    public boolean isAnnotation;

    /**
     * true if the glyph is a space object
     */
    public boolean isFormerGlyph;

    /**
     * true if the glyph is a space object
     */
    public boolean isEmpty;

    public boolean isCreatedByEditor;

    /**
     * true if the glyph is a space object
     */
    public String formerID;

    /**
     * true if the glyph is a space object
     */
    private int forcedLayoutX;

    public int getForcedLayoutX() {
        return forcedLayoutX;
    }

    public void setForcedLayoutX(int forcedLayoutX) {
        this.forcedLayoutX = forcedLayoutX;
    }

    /**
     * true if the glyph is a space object
     */
    private int forcedLayoutY;

    public int getForcedLayoutY() {
        return forcedLayoutY;
    }

    public void setForcedLayoutY(int forcedLayoutY) {
        this.forcedLayoutY = forcedLayoutY;
    }

    /**
     * Creates a new glyph
     * 
     * @param pGlyphImage
     *            the corresponding original image
     * @param pGrayImage
     *            the corresponding gray image
     * @param pBinarized
     *            the corresponding binarized image
     * @param pGlyph
     *            the glyph
     * @param pBoundingBox
     *            the bounds
     */
    private Glyph(final Image pGlyphImage, final GrayImage pGrayImage,
            final GrayImage pBinarized,
            final ArrayList<PartialGlyphShape> pGlyph,
            final Rectangle pBoundingBox) {
        this.glyphImage = pGlyphImage;
        this.grayImage = pGrayImage;
        this.binarizedImage = pBinarized;
        this.isSpace = false;
        this.glyph = pGlyph;
        this.forcedLayoutX = -1;
        this.forcedLayoutY = -1;
        this.boundingBox = new BoundingBox(pBoundingBox.getX(),
                pBoundingBox.getY(), pBoundingBox.getWidth(),
                pBoundingBox.getHeight());
    }

    /**
     * Creates a new glyph without corresponding images
     * 
     * @param pBoundingBox
     *            the bounds
     */
    Glyph(final Rectangle pBoundingBox) {
        this.isSpace = false;
        this.boundingBox = new BoundingBox(pBoundingBox.getX(),
                pBoundingBox.getY(), pBoundingBox.getWidth(),
                pBoundingBox.getHeight());
    }

    public void setToSpaceCharacter() {
        this.isSpace = true;
    }

    /**
     * Gets the id
     * 
     * @return the id
     */
    public String getID() {
        return this.ID;
    }

    /**
     * sets the id
     * 
     * @param pID
     *            the id
     */
    public void setID(final int pID) {
        this.ID = ID_PREFIX + pID;
    }

    /**
     * sets the id
     * 
     * @param pID
     *            the id
     */
    public void setID(final String pID) {
        this.ID = pID;
    }

    /**
     * Gets the group id
     * 
     * @return the group id
     */
    public String getGroupID() {
        return this.groupID;
    }

    /**
     * Sets the group id
     * 
     * @param pID
     *            the group id
     */
    public void setGroupID(final String pID) {
        this.groupID = pID;
    }

    /**
     * Sets the line id
     * 
     * @param pID
     *            the line id
     */
    public void setLineID(final String pID) {
        this.lineID = pID;
    }

    /**
     * Gets the line id
     * 
     * @return the line id
     */
    public String getLineID() {
        return this.lineID;
    }

    /**
     * This method is used to get the image
     * 
     * @return the image of glyph
     */
    public Image getImage() {
        return this.glyphImage;
    }

    /**
     * Gets the gray image
     * 
     * @return the gray image
     */
    public GrayImage getGrayImage() {
        return this.grayImage;
    }

    /**
     * Gets the binarized image
     * 
     * @return the binarized image
     */
    public GrayImage getBinarizedImage() {
        return this.binarizedImage;
    }

    public void setBinarizedImage(GrayImage binarizedImage) {
        this.binarizedImage = binarizedImage;
    }

    /**
     * Gets the gray image as javafx image
     * 
     * @return the gray image as javafx image
     */
    public Image getGrayImageAsFXImage() {
        return this.grayImage.getAsFXImage();
    }

    /**
     * Gets the binarized image as javafx image
     * 
     * @return the binarized image as javafx image
     */
    public Image getBinarizedImageAsFXImage() {
        return this.binarizedImage.getAsFXImage();
    }

    /**
     * Gets the pixels of the grayimage
     * 
     * @return the pixels of the grayimage
     */
    public byte[] getGrayImagePixels() {
        return this.grayImage.getPixels();
    }

    /**
     * Gets the pixels of the binarized image
     * 
     * @return the pixels of the binarized image
     */
    public byte[] getBinarizedImagePixels() {
        return this.binarizedImage.getPixels();
    }

    /**
     * Moves the bounds of the glyph after the image was cropped
     * 
     * @param deltaX
     *            the distance to move in x direction
     * @param deltaY
     *            the distance to move in y direction
     */
    void moveByAfterMasking(final int deltaX, final int deltaY) {
        this.boundingBox = new BoundingBox(this.getLayoutX() + deltaX,
                this.getLayoutY() + deltaY, this.getWidth(), this.getHeight());
    }

    /**
     * Subtracts the shape of another glyph from this glyph
     * 
     * @param glyph
     *            the glyph to subtract
     */
    public void subtract(final Glyph glyph) {
        // System.out.println("---> Line split");
        if (this.getShapeToDraw(Color.BLACK) == null
                || glyph.getShapeToDraw(Color.BLACK) == null) {
            return;
        }

        final Shape newShape = Shape.subtract(this.getShapeToDraw(Color.BLACK),
                glyph.getShapeToDraw(Color.BLACK));

        // newShape.setLayoutX(0);
        // newShape.setLayoutY(0);
        PartialGlyphShape pgs = new PartialGlyphShape(newShape);
        pgs.getShape().setLayoutX(0 - this.getLayoutX());
        pgs.getShape().setLayoutY(0 - this.getLayoutY());
        int minX2 = 0;
        int minX = (int) (glyph.getShapeToDraw(Color.BLACK).getLayoutX() - this
                .getShapeToDraw(Color.BLACK).getLayoutX());
        if (minX < 0) {
            minX2 = minX * (-1);
            minX = 0;
        }

        int minY2 = 0;
        int minY = (int) (glyph.getShapeToDraw(Color.BLACK).getLayoutY() - this
                .getShapeToDraw(Color.BLACK).getLayoutY());
        if (minY < 0) {
            minY2 = minY * (-1);
            minY = 0;
        }

        this.grayImage.subtract(glyph.getGrayImage(), minX, minY, minX2, minY2);
        this.binarizedImage.subtract(glyph.getGrayImage(), minX, minY, minX2,
                minY2);
        this.glyph.clear();
        this.glyph.add(pgs);

        byte[] binaryArray = this.binarizedImage.getPixels();
        boolean found = false;

        for (int i = 0; i < binaryArray.length; i++) {
            if (binaryArray[i] == 0) {
                found = true;
            }
        }

        if (!found) {
            this.isEmpty = true;
        }

    }

    /**
     * Merges the glyph with another given glyph
     * 
     * @param glyph
     *            the glyph to merge with
     */
    public void merge(final Glyph glyph, final Image image) {
        BoundingBox bb = this.getBoundingBox();
        BoundingBox bb2 = glyph.getBoundingBox();

        double minX, maxX, minY, maxY, minX2, maxX2, minY2, maxY2;

        minX = bb.getMinX();
        maxX = bb.getMaxX();
        minY = bb.getMinY();
        maxY = bb.getMaxY();

        minX2 = bb2.getMinX();
        maxX2 = bb2.getMaxX();
        minY2 = bb2.getMinY();
        maxY2 = bb2.getMaxY();

        double newMinX, newMaxX, newMinY, newMaxY;

        newMinX = Math.min(minX, minX2);
        newMaxX = Math.max(maxX, maxX2);
        newMinY = Math.min(minY, minY2);
        newMaxY = Math.max(maxY, maxY2);

        int newWidth = (int) (newMaxX - newMinX);
        int newHeight = (int) (newMaxY - newMinY);

        BoundingBox newbb = new BoundingBox(newMinX, newMinY, newWidth,
                newHeight);
        this.boundingBox = newbb;

        // Bei neuem X/Y-Minimum der Bounding Box müssen bereits bestehende
        // Shapes versetzt werden
        for (PartialGlyphShape shape : this.glyph) {
            shape.getShape().setLayoutY(
                    shape.getShape().getLayoutY() + minY - newMinY);
            shape.getShape().setLayoutX(
                    shape.getShape().getLayoutX() + minX - newMinX);
        }

        ArrayList<PartialGlyphShape> newShapes = glyph.getShape();
        for (PartialGlyphShape shape : newShapes) {
            shape.getShape().setLayoutX(
                    shape.getShape().getLayoutX() + minX2 - newMinX);
            shape.getShape().setLayoutY(
                    shape.getShape().getLayoutY() + minY2 - newMinY);
        }
        this.glyph.addAll(newShapes);

        this.isSquareFragment = false;
        this.glyphImage = image;
        this.grayImage.merge(glyph.getGrayImage(), (int) (minX - newMinX),
                (int) (minY - newMinY), (int) (minX2 - newMinX),
                (int) (minY2 - newMinY), newWidth, newHeight);
        this.binarizedImage.merge(glyph.getBinarizedImage(),
                (int) (minX - newMinX), (int) (minY - newMinY),
                (int) (minX2 - newMinX), (int) (minY2 - newMinY), newWidth,
                newHeight);
    }

    /**
     * This method is used to get int array pixels from image
     * 
     * @return int array pixels
     */
    public int[] getGrayImageIntPixels() {
        return this.grayImage.getPixelsAsInt();
    }

    /**
     * Gets the bound of this glyph
     * 
     * @return the bounds
     */
    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    /**
     * Gets the x position
     * 
     * @return the x position
     */
    public int getLayoutX() {
        return (int) this.boundingBox.getMinX();
    }

    /**
     * Gets the x position
     * 
     * @return the x position
     */
    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    /**
     * Gets the y position
     * 
     * @return the y position
     */
    public int getLayoutY() {
        return (int) this.boundingBox.getMinY();
    }

    /**
     * This method is used to get the width of glyph
     * 
     * @return width
     */
    public int getWidth() {
        if (this.glyphImage != null) {
            return (int) this.glyphImage.getWidth();
        } else {
            return 0;
        }
    }

    /**
     * This method is used to get the height of glyph
     * 
     * @return height
     */
    public int getHeight() {
        return (int) this.glyphImage.getHeight();
    }

    /**
     * This method returns the height of the left most (probably disconnected)
     * part of the glyph (which is found in the 1st column of the binary glyph
     * image)
     * 
     * @return height of left most part of that glyph
     */
    public int getHeightAtLeftEnd() {
        int topBlack = 0;
        boolean found = false;
        while (topBlack < getHeight() && !found) {
            found = binarizedImage.getPixelAsInt(0, topBlack) == GrayImage.BLACK;
            topBlack++;
        }

        int bottomBlack = getHeight() - 1;
        found = false;
        while (bottomBlack > 0 && !found) {
            found = binarizedImage.getPixelAsInt(0, bottomBlack) == GrayImage.BLACK;
            bottomBlack--;
        }

        if (bottomBlack > topBlack)
            return bottomBlack - topBlack;
        else
            return 0;
    }

    /**
     * Gets the dimension of this glyph
     * 
     * @return the dimension
     */
    public Dimension getDimension() {
        return new Dimension((int) this.glyphImage.getWidth(),
                (int) this.glyphImage.getHeight());
    }

    public int getSize() {
        if (size == 0) {
            byte[] pixels = this.getBinarizedImagePixels();
            for (int i = 0; i < pixels.length; i++) {
                if (pixels[i] == 0) {
                    size++;
                }
            }
        }

        return size;
    }

    /**
     * Checks whether another glyph intersects with this one in a way that it is
     * supposed to be already extracted
     * 
     * @param other
     *            the other glyph
     * @return <code>true</code> if the other glyph was already extracted,
     *         <code>false</code> otherwise
     */
    public boolean isAlreadyExtracted(final Glyph other) {
        // if ( isSpace )
        // return false;
        // else
        // {
        // final byte[] pixels = other.getBinarizedImagePixels();
        // final int width = other.getWidth();
        // final int height = other.getHeight();
        // double sum = 0;
        // for (int y = 0; y < height; ++y)
        // {
        // final int offset = y * width;
        // for (int x = 0; x < width; ++x)
        // {
        // final int index = offset + x;
        // if (pixels[index] == GrayImage.BLACK
        // && this.boundingBox.contains(x + other.getLayoutX(), y +
        // other.getLayoutY()))
        // {
        // ++sum;
        // }
        // }
        // }
        return false;// sum / (width + height) > 1 / 3.;
        // }
    }

    /**
     * Checks whether another glyph intersects with this one in a way that it is
     * supposed to be already extracted
     * 
     * @param other
     *            the other glyph
     * @return <code>true</code> if the other glyph was already extracted,
     *         <code>false</code> otherwise
     */
    public boolean isAlreadyExtractedStrict(final Glyph other) {
        // System.out.println("Glyph:isAlreadyExtractedStrict");
        final byte[] pixels = other.getBinarizedImagePixels();
        final int width = other.getWidth();
        final int height = other.getHeight();
        double sum = 0;
        for (int y = 0; y < height; ++y) {
            final int offset = y * width;
            for (int x = 0; x < width; ++x) {
                final int index = offset + x;
                if (pixels[index] == GrayImage.BLACK
                        && this.boundingBox.contains(x + other.getLayoutX(), y
                                + other.getLayoutY())) {
                    ++sum;
                }
            }
        }
        return sum > 0;
    }

    /**
     * Gets the partial shapes of this glyph
     * 
     * @return the partial shapes
     */
    public ArrayList<PartialGlyphShape> getShape() {
        return this.glyph;
    }

    /**
     * Get the number of connected components of this glyph which is the number
     * of active PartialGlyphShapes
     * 
     * @return this number
     */

    public int getNumOfComponents() {
        int i = 0;
        for (final PartialGlyphShape p : this.glyph)
            if (p.isActive())
                i++;

        return i;
    }

    /**
     * Gets the complete shape of this glyph
     * 
     * @param pFill
     *            the desired color for visualisation
     * @return the complete shape
     */
    public Shape getShapeToDraw(final Color pFill) {
        // System.out.println("Glyph:getShapeToDraw");
        if (this.glyph.isEmpty()) {
            return null;
        }
        // System.out.println("Glyph:getShapeToDraw:this.glyph.get(0).getShape();");
        Shape draw = this.glyph.get(0).getShape();
        for (final PartialGlyphShape p : this.glyph) {
            // System.out.println("Glyph:getShapeToDraw:for partialGlyphShape p...");
            if (p.isActive()) {
                draw = p.getShape();
                break;
            }
        }
        // System.out.println("Glyph:getShapeToDraw:drawScale...");
        draw.setScaleX(1);
        draw.setScaleY(1);
        for (int i = 0; i < this.glyph.size(); ++i) {
            if (this.glyph.get(i).isActive()) {
                final Shape tmp = this.glyph.get(i).getShape();
                tmp.setScaleX(1);
                tmp.setScaleY(1);
                // System.out.println("Glyph:getShapeToDraw:Shape.union(draw, tmp);");
                draw = Shape.union(draw, tmp);
            }
        }
        // System.out.println("Glyph:getShapeToDraw:draw.setLayoutX(this.getLayoutX());");
        draw.setLayoutX(this.getLayoutX());
        draw.setLayoutY(this.getLayoutY());
        // System.out.println("Glyph:getShapeToDraw:draw.setFill(pFill);");
        draw.setFill(pFill);

        draw.setId(this.ID + "");
        return draw;
    }

    /**
     * Inverts the pixel at position (x,y)
     * 
     * @param x
     *            the x position
     * @param y
     *            the y position
     */
    public void invertPixel(final int x, final int y) {
        this.binarizedImage.invertPixel(x, y);
    }

    /**
     * Sets the pixel at position (x,y) to white
     * 
     * @param x
     *            the x position
     * @param y
     *            the y position
     */
    public void whitenPixel(final int x, final int y) {
        this.binarizedImage.whitenPixel(x, y);
    }

    /**
     * Sets the pixel at position (x,y) to black
     * 
     * @param x
     *            the x position
     * @param y
     *            the y position
     */
    public void blackenPixel(final int x, final int y) {
        this.binarizedImage.blackenPixel(x, y);
    }

    /**
     * Updates the shape of a glyph
     * 
     * @param readFromHDD
     *            <code>true</code> if it was read from hdd, <code>false</code>
     *            otherwise. if true it needs special treatment
     */
    public void updateShape(final boolean readFromHDD) {
        // System.out.println("Glyph:updateShape");
        final ContourTracer ct = new ContourTracer(this.binarizedImage);
        final ContourSet cs = ct.getContours();
        final ArrayList<PartialGlyphShape> newGlyph = cs.getGlyphShape();

        for (final PartialGlyphShape pgs : this.glyph) {
            if (!pgs.isActive()) {
                for (final PartialGlyphShape pgs2 : newGlyph) {
                    if (!readFromHDD) {
                        final Shape tmp = Shape.union(pgs.getShape(),
                                pgs2.getShape());
                        final int size = pgs.getWidth() * pgs.getHeight();
                        final double epsilon = .0000001;
                        if (Math.abs(tmp.getLayoutBounds().getWidth()
                                * tmp.getLayoutBounds().getHeight() - size) <= epsilon) {
                            pgs2.setActive(false);
                        }
                    } else {
                        pgs2.setActive(false);
                    }
                }
            }
        }
        this.glyph = newGlyph;
        for (final PartialGlyphShape s : this.glyph) {
            s.setFill(FILL);
        }
    }

    /**
     * Extracts a glyph
     * 
     * @param pOriginal
     *            the original image
     * @param pGrayImage
     *            the gray image
     * @param pBinarizedImage
     *            the binarized image
     * @param pRectangle
     *            the bounds
     * @return the extracted glyph
     */
    public static Glyph extractGlyph(final Image pOriginal,
            final GrayImage pGrayImage, final GrayImage pBinarizedImage,
            final Rectangle pRectangle) {
        // System.out.println("Glyph:extractGlyph");
        final ContourTracer ct = new ContourTracer(pBinarizedImage);
        final ContourSet cs = ct.getContours();

        final ArrayList<PartialGlyphShape> glyph = cs.getGlyphShape();
        for (final PartialGlyphShape s : glyph)
            s.setFill(FILL);

        // to include empty space between words
        if (pBinarizedImage.getWidth() * pBinarizedImage.getHeight() < 2) {
            Rectangle r = new Rectangle();
            r.setX(0);
            r.setY(0);
            r.setWidth(1);
            r.setHeight(1);
            glyph.add(new PartialGlyphShape(r));
        }

        return new Glyph(pOriginal, pGrayImage, pBinarizedImage, glyph,
                pRectangle);
    }

    public Glyph copy() {
        // System.out.println("Glyph:copy");
        Rectangle r = new Rectangle();
        r.setX(this.boundingBox.getMinX());
        r.setY(this.boundingBox.getMinY());
        r.setWidth(this.boundingBox.getWidth());
        r.setHeight(this.boundingBox.getHeight());

        PixelReader pixelReader = this.glyphImage.getPixelReader();

        int width = (int) this.glyphImage.getWidth();
        int height = (int) this.glyphImage.getHeight();

        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                pixelWriter.setColor(x, y, color);
            }
        }

        Image image = writableImage;
        GrayImage grayImage = new GrayImage(this.grayImage.getPixels(),
                this.getWidth(), this.getHeight());
        GrayImage binarizedImage = new GrayImage(
                this.binarizedImage.getPixels(), this.getWidth(),
                this.getHeight());

        Glyph glyph = extractGlyph(image, grayImage, binarizedImage, r);
        glyph.setGroupID(this.getGroupID());
        glyph.setID(this.getID());
        glyph.setLineID(this.getLineID());
        glyph.forcedLayoutX = this.forcedLayoutX;
        glyph.forcedLayoutY = this.forcedLayoutY;
        glyph.isSpace = this.isSpace;
        glyph.isAnnotation = this.isAnnotation;
        glyph.isFormerGlyph = this.isFormerGlyph;
        glyph.isSquareFragment = this.isSquareFragment;
        glyph.formerID = this.formerID;
        return glyph;
    }

    /**
     * Sets the shape which contains the coordinates (x,y) to active or inactive
     * 
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     */
    public void invertActiveShape(final int x, final int y) {
        // System.out.println("Glyph:invertActiveShape");
        for (final PartialGlyphShape s : this.glyph) {
            if (s.getShape().contains(x, y)) {
                s.invertActive();
                break;
            }
        }
    }

    /**
     * Removes all inactive partial shapes
     */
    public void removeInactive() {
        // System.out.println("Glyph:removeInactive");
        final ArrayList<PartialGlyphShape> inactive = new ArrayList<>();
        for (int i = this.glyph.size() - 1; i >= 0; --i) {
            if (!this.glyph.get(i).isActive()) {
                inactive.add(this.glyph.get(i));
                this.glyph.remove(i);
            }
        }

        final Shape s = this.getShapeToDraw(FILL);
        if (s == null) {
            return;
        }

        final BoundingBox oldBounds = this.boundingBox;
        final BoundingBox tempBounds = (BoundingBox) s.getLayoutBounds();

        final BoundingBox newBounds = new BoundingBox(oldBounds.getMinX()
                + tempBounds.getMinX(), oldBounds.getMinY()
                + tempBounds.getMinY(), tempBounds.getWidth(),
                tempBounds.getHeight());

        this.boundingBox = newBounds;

        final Rectangle crop = new Rectangle(tempBounds.getMinX(),
                tempBounds.getMinY(), tempBounds.getWidth(),
                tempBounds.getHeight());

        this.glyphImage = ImageUtils.cropFXImage(this.glyphImage, crop);
        this.grayImage = ImageUtils.cropGrayImage(this.grayImage, crop);
        this.binarizedImage = ImageUtils.cropGrayImage(this.binarizedImage,
                crop);
        this.updateShape2(inactive, (int) crop.getX(), (int) crop.getY());
    }

    /**
     * Updates a glyph. In order to not set inactive regions to active again, we
     * keep them in mind
     * 
     * @param inactive
     *            the inactive regions
     * @param moveXBy
     *            the distance the glyph is moved in x direction
     * @param moveYBy
     *            the distance the glyph is moved in y direction
     */
    private void updateShape2(final ArrayList<PartialGlyphShape> inactive,
            final int moveXBy, final int moveYBy) {
        // System.out.println("Glyph:updateShape2");
        final ContourTracer ct = new ContourTracer(this.binarizedImage);
        final ContourSet cs = ct.getContours();
        final ArrayList<PartialGlyphShape> newGlyph = cs.getGlyphShape();

        for (final PartialGlyphShape pgs : inactive) {
            final int size = pgs.getWidth() * pgs.getHeight();
            for (final PartialGlyphShape pgs2 : newGlyph) {
                pgs2.moveXBy(moveXBy);
                pgs2.moveYBy(moveYBy);
                final Shape tmp = Shape.union(pgs.getShape(), pgs2.getShape());
                final double epsilon = .0000001;
                if (Math.abs(tmp.getLayoutBounds().getWidth()
                        * tmp.getLayoutBounds().getHeight() - size) <= epsilon) {
                    pgs2.setActive(false);
                }
                pgs2.moveXBy(-moveXBy);
                pgs2.moveYBy(-moveYBy);
            }
        }
        this.glyph = newGlyph;
        for (final PartialGlyphShape s : this.glyph) {
            s.setFill(FILL);
        }
    }

    /**
     * Checks whether parts of this glyphs intersects with the border. If so
     * those are initially set to inactive
     */
    public void intersectsBorder() {
        // System.out.println("Glyph:intersectsBorder");
        if (this.glyph.size() == 1) {
            return;
        }
        final double x1 = this.boundingBox.getMinX();
        final double y1 = this.boundingBox.getMinY();
        final double x2 = this.boundingBox.getMaxX();
        final double y2 = this.boundingBox.getMaxY();

        double maxSize = 0;
        int index = -1;
        int counter = 0;
        for (int i = 0; i < this.glyph.size(); ++i) {
            final PartialGlyphShape g = this.glyph.get(i);
            if (!g.isActive()) {
                ++counter;
                continue;
            }
            final Shape tmp = g.getShape();
            if (tmp.intersects(0, 0, x2 - x1 + 1, 1)
                    || tmp.intersects(0, y2 - y1, x2 - x1 + 1, 1)
                    || tmp.intersects(0, 0, 1, y2 - y1 + 1)
                    || tmp.intersects(x2 - x1, 0, 1, y2 - y1 + 1)) {
                g.setActive(false);
                ++counter;
            }

            final double size = g.getShape().getLayoutBounds().getHeight()
                    * g.getShape().getLayoutBounds().getWidth();
            if (maxSize < size) {
                maxSize = size;
                index = i;
            }
        }
        if (counter == this.glyph.size()) {
            this.glyph.get(index).setActive(true);
        }
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
        // System.out.println("Glyph:writeObject");
        // DiptychonLogger.debug("Writing glyph (ID: {}) to hdd", this.getID());
        s.defaultWriteObject();
        final double x = this.getLayoutX();
        final double y = this.getLayoutY();
        final double shapeWidth;
        final double shapeHeight;
        shapeWidth = this.getWidth();
        shapeHeight = this.getHeight();
        s.writeDouble(x);
        s.writeDouble(y);
        s.writeDouble(shapeWidth);
        s.writeDouble(shapeHeight);

        ImageUtils.FXimageToOutputStream(s, this.glyphImage);
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
        // System.out.println("Glyph:readObject");
        // DiptychonLogger.debug("Reading single glyph from hdd");
        s.defaultReadObject();
        // DiptychonLogger.debug("(ID: {})", this.getID());
        // DiptychonLogger.debug("(GroupID: {})", this.getGroupID());
        final double x = s.readDouble();
        final double y = s.readDouble();
        final double shapeWidth = s.readDouble();
        final double shapeHeight = s.readDouble();
        final BoundingBox bbox = new BoundingBox(x, y, shapeWidth, shapeHeight);
        this.boundingBox = bbox;
        final ArrayList<Boolean> active = new ArrayList<>();
        // //System.out.println("GLYPH SIZE: " + this.glyph.size());
        if (!this.isCreatedByEditor) {
            this.updateShape(true);
        }
        for (final PartialGlyphShape pgs : this.glyph) {
            active.add(pgs.isActive());
        }
        int index = 0;
        if (this.isCreatedByEditor) {
            this.updateShape(true);
        }
        // //System.out.println("ACTIVE SIZE: " + active.size());
        for (final Boolean b : active) {
            // //System.out.println("INDEX: " + index);
            // //System.out.println("B: " + b);
            if (index >= this.glyph.size()) {
                // System.out.println("WARNUNG: GLYPH SHAPE INKONSISTENZ");
                break;
            }
            this.glyph.get(index).setActive(b);
            // //System.out.println(index + "DONE");
            ++index;
        }
        this.glyphImage = ImageUtils.FXimageFromInputStream(s);

    }

    /**
     * Gets the id of this glyph but without the glyph prefix
     * 
     * @return the id of this glyph without prefix
     */
    public int getIDWithoutPrefix() {
        // System.out.println("Glyph:getIDWithoutPrefix");
        return Integer.valueOf(this.ID.substring(ID_PREFIX.length(),
                this.ID.length()));
    }

    // only usable for glyphs within one line, since the comparison just
    // compares the x coordinates
    @Override
    public int compareTo(final Glyph other) {
        // System.out.println("Glyph:compareTo");
        int thisLayoutX, otherLayoutX;
        if (this.getForcedLayoutX() != -1) {
            thisLayoutX = this.getForcedLayoutX();
        } else {
            thisLayoutX = this.getLayoutX();
        }
        if (other.getForcedLayoutX() != -1) {
            otherLayoutX = other.getForcedLayoutX();
        } else {
            otherLayoutX = other.getLayoutX();
        }

        if (thisLayoutX + this.getWidth() / 2 < otherLayoutX + other.getWidth()
                / 2) {
            return -1;
        } else if (thisLayoutX + this.getWidth() / 2 > otherLayoutX
                + other.getWidth() / 2) {
            return 1;
        } else {
            return 0;
        }
    }
}
