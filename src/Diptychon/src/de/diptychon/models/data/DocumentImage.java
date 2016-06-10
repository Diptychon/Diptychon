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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import javax.imageio.ImageIO;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;

import de.diptychon.DiptychonFX;
import de.diptychon.DiptychonLogger;
import de.diptychon.controller.DocumentPanelController;
import de.diptychon.models.algorithms.Binarizer;
import de.diptychon.models.algorithms.RecursiveGlyphFragmentation;
import de.diptychon.models.algorithms.TextLineDetection;
import de.diptychon.models.algorithms.regionLabeling.ConnectedObjects;
import de.diptychon.models.misc.AutoContrastOperation;
import de.diptychon.models.misc.GrayImage;
import de.diptychon.models.misc.ImageUtils;
import de.diptychon.models.representation.Transcript;
import de.diptychon.models.searchEngine.SearchEngine;

/**
 * This class maintains the images file. It provides many methods to operate the
 * file itself and the glyphs.
 */
public class DocumentImage implements Serializable {

    /**
     * the default value of blue
     */
    private static final transient double DEFAULT_BLUE_WEIGTH = 0.082;

    /**
     * the default value of green
     */
    private static final transient double DEFAULT_GREEN_WEIGTH = 0.609;

    /**
     * default value of K
     */
    private static final transient double DEFAULT_K = 0.25;

    /**
     * the default value of red
     */
    private static final transient double DEFAULT_RED_WEIGTH = 0.309;

    /**
     * default value of window size
     */
    private static final transient int DEFAULT_WINDOW_SIZE = 20;

    /**
     * SerialVersionUID to keep used data consistent.
     */
    private static final long serialVersionUID = 20130301;

    /**
     * binary image
     */
    private GrayImage binaryImage;

    /**
     * binary image
     */
    private transient GrayImage binaryImageOld;

    /**
     * Storage of all applied contrast adaption. Needed to restore them after
     * loading the image
     */
    private final LinkedList<AutoContrastOperation> contrastAdaptionHistory;

    /**
     * current blue weight
     */
    private double curBlueWeight;

    /**
     * current green weight
     */
    private double curGreenWeight;

    /**
     * current K
     */
    private double curK;

    /**
     * current red weight
     */
    private double curRedWeight;

    /**
     * The current grayimage
     */
    private transient GrayImage currentImage;

    /**
     * current window size
     */
    private int curWindowSize;

    /**
     * doucument image
     */
    private transient Image documentImage;

    /**
     * The number of extracted glyphs for this image
     */
    private int glyphCounter;

    /**
     * Glyph Feature Statistics for all glyphs
     */
    private HashMap<String, ArrayList<Float>> glyphStats;

    /**
     * Line Feature Statistics for all lines
     */
    private HashMap<String, ArrayList<Float>> lineStats;

    /**
     * Line Feature Statistics for all lines
     */
    private ArrayList<Float> lineStats2;

    /**
     * Document Statistics
     */
    private ArrayList<Float> documentStats;

    /**
     * Word statistics
     */
    private ArrayList<Float> wordStats;

    /**
     * all glyphs without line
     */
    private HashMap<String, Glyph> glyphsWithoutLine;

    /**
     * Hashmap of current fragments
     */
    private HashMap<String, Glyph> fragments;

    /**
     * gray image
     */
    private transient GrayImage grayImage;

    /**
     * All imagelines on this image
     */
    private final HashMap<String, ImageLine> lines;

    /**
     * The bounds of this image. Needed to restore any crop operation after
     * reloading the image
     */
    private transient Rectangle masked;

    /**
     * the path of image file
     * 
     * TODO: Wirklich pfad oder name?
     */
    private final String name;

    /**
     * Current amount of lines
     */
    private int lineCounter;

    /**
     * This method is construction function which initializes the DocumentImage
     * object.
     * 
     * @param n
     *            the path of image file
     */
    public DocumentImage(final String n) {
        this.name = n;
        this.glyphsWithoutLine = new HashMap<>();
        this.fragments = new HashMap<>();
        this.glyphStats = new HashMap<>();
        this.lines = new HashMap<>();
        this.contrastAdaptionHistory = new LinkedList<>();
        this.curK = -1;
        this.curWindowSize = -1;
        this.curRedWeight = -1;
        this.curGreenWeight = -1;
        this.curBlueWeight = -1;
        this.lineCounter = 0;
    }

    /**
     * Accepts a fragment
     * 
     * @param glyph
     *            the fragment
     * @param setID
     *            <code>true</code> if the id should be automatically generated,
     *            <code>false</code> otherwise
     * @return the id of the line the glyph was added to
     */
    public String acceptFragment(final Glyph glyph, final boolean setID,
            final String lineID) {

        if (setID) {
            glyph.setID(this.glyphCounter);
        }

        this.lines.get(lineID).addGlyph(glyph);

        ++this.glyphCounter;
        return lineID;
    }

    /**
     * Accepts a glyph, even if it's bounding box is overlapping another glyph's
     * bounding box
     * 
     * @param glyph
     *            the glyph
     * @param setID
     *            <code>true</code> if the id should be automatically generated,
     *            <code>false</code> otherwise
     * @return the id of the line the glyph was added to
     */
    public String acceptOverlappingGlyph(final Glyph glyph, final boolean setID) {
        if (setID) {
            glyph.setID(this.glyphCounter);
        }
        boolean addedToLine = false;

        String lineID = "";
        final Set<Entry<String, ImageLine>> entries = this.lines.entrySet();
        for (final Entry<String, ImageLine> entry : entries) {
            if (entry.getValue().addGlyphIfContained(glyph)) {
                lineID = entry.getValue().getID();
                addedToLine = true;
                break;
            }
        }
        if (!addedToLine) {
            System.out.println("1) Glyph: " + glyph);
            DiptychonLogger
                    .info("Added extracted glyph (ID: {}) without corresponding line)",
                            glyph.getID());
            this.glyphsWithoutLine.put(glyph.getID(), glyph);
        }
        ++this.glyphCounter;
        return lineID;
    }

    public boolean acceptOverlappingGlyph(final Glyph glyph,
            final boolean setID, final String lineID) {
        final ArrayList<Glyph> allGlyphs = this.getAllGlyphs();
        if (setID) {
            glyph.setID(this.glyphCounter);
        }

        if (this.lines.get(lineID) == null) {
            return false;
        }
        this.lines.get(lineID).addGlyph(glyph);
        ++this.glyphCounter;
        return true;
    }

    /**
     * Accepts a glyph
     * 
     * @param glyph
     *            the glyph
     * @param setID
     *            <code>true</code> if the id should be automatically generated,
     *            <code>false</code> otherwise
     * @return the id of the line the glyph was added to
     */
    public String acceptGlyph(final Glyph glyph, final boolean setID) {
        final ArrayList<Glyph> allGlyphs = this.getAllGlyphs();
        for (final Glyph g : allGlyphs) {
            if (glyph.isAlreadyExtracted(g)) {
                // DiptychonLogger.debug("Glyph (ID: {}) is already extracted (overlaps glyph ({}, ID: {})) and will "
                // + "not be added", glyph.getID(), g.getGroupID(), g.getID());
                return "";
            }
        }
        if (setID) {
            glyph.setID(this.glyphCounter);
        }
        boolean addedToLine = false;

        String lineID = "";
        final Set<Entry<String, ImageLine>> entries = this.lines.entrySet();
        for (final Entry<String, ImageLine> entry : entries) {
            if (entry.getValue().addGlyphIfContained(glyph)) {
                lineID = entry.getValue().getID();
                addedToLine = true;
                break;
            }
        }
        if (!addedToLine) {
            DiptychonLogger
                    .info("1) Added extracted glyph (ID: {}) without corresponding line)",
                            glyph.getID());
            System.out.println("Group-ID:" + glyph.getGroupID() + ">>>>>>");
            System.out.println("Weitere Infos layoutY:" + glyph.getLayoutY());
            this.glyphsWithoutLine.put(glyph.getID(), glyph);
        }
        ++this.glyphCounter;
        return lineID;
    }

    /**
     * Accepts a list of glyphs
     * 
     * @param glyphs
     *            the list of glyphs
     * @param setID
     *            <code>true</code> if the id should be automatically generated,
     *            <code>false</code> otherwise
     */
    public void acceptGlyphs(final ArrayList<Glyph> glyphs, final boolean setID) {
        for (final Glyph g : glyphs) {
            this.acceptGlyph(g, setID);
        }

        System.out
                .println("********** After having accepted a list of glphys ********** ");
    }

    /**
     * Adds an autocontrast operation to the history of this image
     * 
     * @param aco
     *            the operation to add
     */
    public void addAutoContrastOperationToHistory(
            final AutoContrastOperation aco) {
        this.contrastAdaptionHistory.add(aco);
    }

    /**
     * Adds a line to this image
     * 
     * @param imageLine
     *            the line
     * @return the line with set id
     */
    public ImageLine addLine(final ImageLine imageLine) {
        imageLine.setID(++lineCounter);
        this.lines.put(imageLine.getID(), imageLine);

        final Iterator<Glyph> it = this.glyphsWithoutLine.values().iterator();
        while (it.hasNext()) {
            final Glyph glyph = it.next();

            if (imageLine.addGlyphIfContained(glyph)) {
                it.remove();
            }
        }
        return imageLine;
    }

    /**
     * Adds a line at the specified rectangular region
     * 
     * @param rectangle
     *            the rectangular region
     * @return the line with set id
     */
    public ImageLine addLine(final Rectangle rectangle) {
        return this.addLine(new ImageLine(rectangle));
    }

    /**
     * Calculates an auto contrast operation
     * 
     * @param aco
     *            the parameters for the calculation
     */
    public void autoContrastImage(final AutoContrastOperation aco) {
        final Rectangle imageRectangle = aco.getImageRectangle();
        int[] histogramm = aco.getHistogramm();
        DiptychonLogger.debug("saturationLow: {} saturationHigh: {}",
                aco.getSaturationLow(), aco.getSaturationHigh());
        final int width = (int) this.documentImage.getWidth();

        this.resetToGray((int) imageRectangle.getX(),
                (int) imageRectangle.getX() + (int) imageRectangle.getWidth(),
                (int) imageRectangle.getY(), (int) imageRectangle.getY()
                        + (int) imageRectangle.getHeight());
        histogramm = this.getHistogramInRectangle(imageRectangle,
                aco.getHistogramm());
        final byte[] byteBuffer = this.getGrayImage().getPixels();
        final int[] cumHistogramm = this.getCumulativeHistogramm(histogramm);
        final int size = (int) (imageRectangle.getWidth() * imageRectangle
                .getHeight());
        final int aLow = this.getALow(cumHistogramm, aco.getSaturationLow(),
                size);
        final int aHigh = this.getAHigh(cumHistogramm, aco.getSaturationHigh(),
                size);

        final int aMin = aco.getaMin();
        final int aMax = aco.getaMax();

        final double scale = ((aMax - aMin) / (double) (aHigh - aLow));
        DiptychonLogger.debug("aMin: {} aMax: {} aLow: {} aHigh: {} scale: {}",
                aMin, aMax, aLow, aHigh, scale);
        final int upperBoundY = (int) (imageRectangle.getY() + imageRectangle
                .getHeight());
        final int upperBoundX = (int) (imageRectangle.getX() + imageRectangle
                .getWidth());
        for (int y = (int) imageRectangle.getY(); y < upperBoundY; ++y) {
            final int offset = y * width;
            for (int x = (int) imageRectangle.getX(); x < upperBoundX; ++x) {
                final int index = offset + x;
                final int oldVal = byteBuffer[index] & 0xff;
                int newVal = 0;
                if (oldVal < aLow) {
                    newVal = aMin;
                } else if (oldVal > aHigh) {
                    newVal = aMax;
                } else {
                    newVal = (int) (aMin + (oldVal - aLow) * scale);
                }
                // Remove the following comment if you need the debug output,
                // otherwise contrast adjustment would take
                // to long due to the time it needs to write to std_out
                // DiptychonLogger.debug("oldVal: {} newval: {}", oldVal,
                // newVal);
                byteBuffer[index] = (byte) (newVal);
            }
        }
    }

    /**
     * This method is used to binarize the image
     * 
     * @param windowSize
     *            the size of window
     * @param k
     *            K value
     * @return the binarized image
     */
    public GrayImage binarize(final int windowSize, final double k) {
        if (this.binaryImage != null && this.curWindowSize == windowSize
                && this.curK == k) {
            return this.binaryImage;
        }
        if (this.grayImage == null) {
            this.gray(DEFAULT_RED_WEIGTH, DEFAULT_GREEN_WEIGTH,
                    DEFAULT_BLUE_WEIGTH, 0,
                    (int) this.documentImage.getWidth(), 0,
                    (int) this.documentImage.getHeight());
        }
        this.curWindowSize = windowSize;
        this.curK = k;
        final Binarizer binarizer = new Binarizer();
        this.binaryImage = binarizer.bySauvola(this.grayImage, windowSize, k);
        return this.binaryImage;
    }

    public GrayImage binarize(final int windowSize, final double k,
            Rectangle rect) {
        if (this.grayImage == null) {
            this.gray(DEFAULT_RED_WEIGTH, DEFAULT_GREEN_WEIGTH,
                    DEFAULT_BLUE_WEIGTH, 0,
                    (int) this.documentImage.getWidth(), 0,
                    (int) this.documentImage.getHeight());
        }
        this.curWindowSize = windowSize;
        this.curK = k;
        final Binarizer binarizer = new Binarizer();
        this.binaryImage = binarizer.bySauvola(this.grayImage,
                this.binaryImage, windowSize, k, rect);
        return this.binaryImage;
    }

    public void backupBinaryImage() {
        if (this.binaryImage == null) {
            if (this.curK != -1 && this.curWindowSize != -1) {
                this.binarize(this.curWindowSize, this.curK);
            } else {
                this.binarize(DEFAULT_WINDOW_SIZE, DEFAULT_K);
            }
        }
        this.binaryImageOld = new GrayImage(this.binaryImage.getPixelCloned(),
                this.binaryImage.getWidth(), this.binaryImage.getHeight());
    }

    /**
     * This method is used to cancel the operation of binarization.
     */
    public void cancelBinarization() {
        this.binaryImage = new GrayImage(this.binaryImageOld.getPixelCloned(),
                this.binaryImageOld.getWidth(), this.binaryImageOld.getHeight());
    }

    /**
     * This method is used to cancel gray of image by setting the default values
     */
    public void cancelGray() {
        this.gray(DEFAULT_RED_WEIGTH, DEFAULT_GREEN_WEIGTH, DEFAULT_BLUE_WEIGTH);
    }

    /**
     * Clears the autocontrastoperation history
     */
    public void clearAutoContrastOperationHistory() {
        this.contrastAdaptionHistory.clear();
    }

    /**
     * Updates a glyph
     * 
     * @param glyph
     *            the glyph
     */
    public void editGlyph(final Glyph glyph) {
        final String lineID = glyph.getLineID();
        if (lineID != null && !lineID.isEmpty()) {
            this.lines.get(lineID).replaceGlyph(glyph);
        } else {
            this.glyphsWithoutLine.put(glyph.getID(), glyph);
        }

    }

    public void sortGlyphLeft(final String ID, final String lineID) {
        ArrayList<Glyph> glyphs = this.getGlyphs(lineID);
        Collections.sort(glyphs);
        Glyph glyph = this.getGlyph(null, ID);
        int index = glyphs.indexOf(glyph);
        if (index - 1 < 0) {
            return;
        }
        Glyph lglyph = glyphs.get(index - 1);
        int lglyphLayoutX, glyphLayoutX;
        if (lglyph.getForcedLayoutX() != -1) {
            lglyphLayoutX = lglyph.getForcedLayoutX();
        } else {
            lglyphLayoutX = lglyph.getLayoutX();
        }
        if (glyph.getForcedLayoutX() != -1) {
            glyphLayoutX = glyph.getForcedLayoutX();
        } else {
            glyphLayoutX = glyph.getLayoutX();
        }
        glyph.setForcedLayoutX(lglyphLayoutX + lglyph.getWidth() / 2
                - glyph.getWidth() / 2);
        lglyph.setForcedLayoutX(glyphLayoutX + glyph.getWidth() / 2
                - lglyph.getWidth() / 2);
    }

    public void sortGlyphRight(final String ID, final String lineID) {
        ArrayList<Glyph> glyphs = this.getGlyphs(lineID);
        Collections.sort(glyphs);
        Glyph glyph = this.getGlyph(null, ID);
        int index = glyphs.indexOf(glyph);
        if (index + 1 > glyphs.size() - 1) {
            return;
        }
        Glyph rglyph = glyphs.get(index + 1);
        int rglyphLayoutX, glyphLayoutX;
        if (rglyph.getForcedLayoutX() != -1) {
            rglyphLayoutX = rglyph.getForcedLayoutX();
        } else {
            rglyphLayoutX = rglyph.getLayoutX();
        }
        if (glyph.getForcedLayoutX() != -1) {
            glyphLayoutX = glyph.getForcedLayoutX();
        } else {
            glyphLayoutX = glyph.getLayoutX();
        }
        glyph.setForcedLayoutX(rglyphLayoutX + rglyph.getWidth() / 2
                - glyph.getWidth() / 2);
        rglyph.setForcedLayoutX(glyphLayoutX + glyph.getWidth() / 2
                - rglyph.getWidth() / 2);
    }

    public void sortGlyphReset(final String ID) {
        Glyph glyph = this.getGlyph(null, ID);
        glyph.setForcedLayoutX(-1);
    }

    /**
     * This method is used to extract glyph through the selected region
     * 
     * @param mask
     *            the region which is selected by user
     * @return glyph which is extracted
     */
    public Glyph extractGlyph(final Rectangle mask) {
        final Rectangle origin = new Rectangle(mask.getX(), mask.getY(),
                mask.getWidth(), mask.getHeight());
        final GrayImage tmp = ImageUtils.cropGrayImage(
                this.getBinarizedImage(), mask);
        Glyph glyph = null;
        try {
            final byte[] pixels = ImageUtils.autoCrop(tmp.getPixels(), mask);
            final GrayImage binarized = new GrayImage(pixels,
                    (int) mask.getWidth(), (int) mask.getHeight());

            final Image original = ImageUtils.cropFXImage(this.documentImage,
                    mask);
            final GrayImage newGrayImage = ImageUtils.cropGrayImage(
                    this.getGrayImage(), mask);
            glyph = Glyph.extractGlyph(original, newGrayImage, binarized, mask);
        } catch (final IllegalArgumentException e) {
            glyph = new Glyph(origin);
        }
        return glyph;
    }

    /**
     * This method is used to extract spaces through given coordinates
     * 
     * @param spaceCoords
     *            the space coordinates
     * @return
     */
    public ArrayList<Glyph> extractSpaces(final ArrayList<int[]> spaceCoords) {
        ArrayList<Glyph> spaces = new ArrayList<Glyph>();
        for (final int[] coord : spaceCoords) {

            final Rectangle cropSpace = new Rectangle(coord[0], coord[1], 1, 1);

            GrayImage binaryImage = ImageUtils.cropGrayImage(
                    this.getBinarizedImage(), cropSpace);

            Glyph space = Glyph.extractGlyph(
                    ImageUtils.cropFXImage(this.documentImage, cropSpace),
                    ImageUtils.cropGrayImage(this.getGrayImage(), cropSpace),
                    binaryImage, cropSpace);

            space.setGroupID(" ");

            space.isSpace = true;

            spaces.add(space);
        }
        return spaces;
    }

    /**
     * This method is used to remove spaces from a given line
     * 
     * @param lineID
     *            the line ID
     * @return
     */
    public void removeSpacesFromLine(final String lineID) {
        ArrayList<Glyph> lineGlyphs = new ArrayList<Glyph>(this.lines.get(
                lineID).getGlyphs());
        for (Glyph g : lineGlyphs) {
            if (g.isSpace) {
                this.lines.get(lineID).removeGlyph(g.getID());
                this.removeGlyph(g.getID());
            }
        }
    }

    /**
     * Auto extract lines
     * 
     * @return the extracted lines
     */
    public Collection<ImageLine> findLines(final int top, final int bottom) {
        final ArrayList<Glyph> allGlyphs = this.getAllGlyphs();
        this.glyphsWithoutLine.clear();
        this.glyphCounter = 0;
        this.lines.clear();
        this.lineCounter = 0;
        final LinkedList<ImageLine> newLines = new TextLineDetection(
                this.getBinarizedImage()).detectLines();

        for (final ImageLine il : newLines) {
            il.setLayoutY(il.getLayoutY()
                    - (il.getHeight() * ((float) top / 100)));
            il.setHeight(il.getHeight()
                    + (il.getHeight() * ((float) top / 100))
                    + (il.getHeight() * bottom / 100));
            this.addLine(il);
        }

        this.acceptGlyphs(allGlyphs, false);
        return this.getLines();
    }

    /**
     * Calculates a_high for autocontrastoperation
     * 
     * @param cumHistogramm
     *            the cummulative histogramm
     * @param saturationHigh
     *            the saturation
     * @param size
     *            the size
     * @return a_high
     */
    private int getAHigh(final int[] cumHistogramm,
            final double saturationHigh, final int size) {
        final double threshold = (1 - saturationHigh) * size;
        int i = cumHistogramm.length - 1;
        while (i >= 0 && cumHistogramm[i] >= threshold) {
            --i;
        }
        if (i < 0) {
            return cumHistogramm.length - 1;
        } else {
            return i;
        }
    }

    /**
     * Gets all glyphs of this image
     * 
     * @return the glyphs of this image
     */
    public ArrayList<Glyph> getAllGlyphs() {
        final ArrayList<Glyph> allGlyphs = new ArrayList<>();
        allGlyphs.addAll(this.glyphsWithoutLine.values());

        final Set<Entry<String, ImageLine>> entries = this.lines.entrySet();
        for (final Entry<String, ImageLine> entry : entries) {
            allGlyphs.addAll(entry.getValue().getGlyphs());
        }
        Collections.sort(allGlyphs);
        return allGlyphs;
    }

    public Image getCroppedImage(final Rectangle bounds) {
        return ImageUtils.cropFXImage(this.documentImage, bounds);

    }

    /**
     * Gets all glyphs of this image
     * 
     * @return the glyphs of this image
     */
    public ArrayList<Glyph> getAllFragments() {
        final ArrayList<Glyph> allFragments = new ArrayList<>();
        allFragments.addAll(this.fragments.values());

        return allFragments;
    }

    /**
     * Calculates a_low for autocontrastoperation
     * 
     * @param cumHistogramm
     *            the cummulative histogramm
     * @param saturationLow
     *            the saturation
     * @param size
     *            the size
     * @return a_low
     */
    private int getALow(final int[] cumHistogramm, final double saturationLow,
            final int size) {
        final double threshold = saturationLow * size;
        int i = 0;
        while (i < cumHistogramm.length && cumHistogramm[i] <= threshold) {
            ++i;
        }
        if (i == cumHistogramm.length) {
            return 0;
        } else {
            return i;
        }
    }

    /**
     * This method is used to get binarized image which is in FXimage format
     * 
     * @return the image in javafx
     */
    public Image getBinarizedAsFXImage() {
        if (this.binaryImage == null) {
            this.binarize(DEFAULT_WINDOW_SIZE, DEFAULT_K);
        }
        return this.binaryImage.getAsFXImage();
    }

    /**
     * Gets the binarized image
     * 
     * @return the binarized image
     */
    public GrayImage getBinarizedImage() {
        if (this.binaryImage == null) {
            if (this.curK != -1 && this.curWindowSize != -1) {
                this.binarize(this.curWindowSize, this.curK);
            } else {
                this.binarize(DEFAULT_WINDOW_SIZE, DEFAULT_K);
            }
        }
        return this.binaryImage;
    }

    /**
     * Gets the binarizing parameters
     * 
     * @return the binarizing parameters
     */
    public Object[] getBinarizeParameters() {
        return new Object[] { this.curK, this.curWindowSize };
    }

    /**
     * Calculates the cumulative histogram
     * 
     * @param histogramm
     *            the normal histogram
     * @return the cumulative histogram
     */
    private int[] getCumulativeHistogramm(final int[] histogramm) {
        final int[] cumHistogramm = new int[histogramm.length];
        for (int i = 1; i < histogramm.length; ++i) {
            cumHistogramm[i] = cumHistogramm[i - 1] + histogramm[i];
        }
        return cumHistogramm;
    }

    /**
     * Gets the current version of this image
     * 
     * @param showBinarizedImage
     *            <code>true</code> if the binarized image should be shown
     *            <code>false</code> otherwise
     * @return the current version of this image
     */
    public Image getCurrentImage(final boolean showBinarizedImage) {
        if (showBinarizedImage) {
            this.currentImage = this.getBinarizedImage();
        } else {
            this.currentImage = this.getGrayImage();
        }
        return this.currentImage.getAsFXImage();
    }

    /**
     * Gets the glyph with id ID
     * 
     * @param glyphID
     *            the id
     * @return the glyph
     */
    public Glyph getGlyph(final String lineID, final String glyphID) {
        if (lineID == null) {
            final Set<Entry<String, ImageLine>> entries = this.lines.entrySet();
            for (final Entry<String, ImageLine> entry : entries) {
                final Glyph g = entry.getValue().getGlyph(glyphID);
                if (g != null) {
                    return g;
                }
            }
            return this.glyphsWithoutLine.get(glyphID);
        } else {
            return this.lines.get(lineID).getGlyph(glyphID);
        }
    }

    /**
     * Gets all glyphs from the line with id lineID
     * 
     * @param lineID
     *            the id of the line
     * @return all glyphs from the line with id lineID
     */
    public ArrayList<Glyph> getGlyphs(final String lineID) {
        final ArrayList<Glyph> lineGlyphs = new ArrayList<>(this.lines.get(
                lineID).getGlyphs());
        return lineGlyphs;
    }

    /**
     * Gets the current grayimage
     * 
     * @return the current grayimage
     */
    public GrayImage getGrayImage() {
        if (this.grayImage == null) {
            this.gray(DEFAULT_RED_WEIGTH, DEFAULT_GREEN_WEIGTH,
                    DEFAULT_BLUE_WEIGTH);
        }
        return this.grayImage;
    }

    /**
     * This method is used to get gray image as FXImage
     * 
     * @return Image which is FXImage.
     */
    public Image getGrayImageAsFXImage() {
        if (this.grayImage == null) {
            this.gray(DEFAULT_RED_WEIGTH, DEFAULT_GREEN_WEIGTH,
                    DEFAULT_BLUE_WEIGTH);
        }
        return this.grayImage.getAsFXImage();
    }

    /**
     * Gets the current grayscale parameters
     * 
     * @return the current grayscale parameters
     */
    public double[] getGrayParameters() {
        return new double[] { this.curRedWeight, this.curGreenWeight,
                this.curBlueWeight };
    }

    /**
     * Gets the histogram within the specified rectangular region
     * 
     * @param imageRectangle
     *            the specified rectangular region
     * @param histogramm
     *            the reference to the histogram
     * @return the histogram
     */
    public int[] getHistogramInRectangle(final Rectangle imageRectangle,
            final int[] histogramm) {
        final GrayImage gi = this.getGrayImage();
        Arrays.fill(histogramm, 0);
        final int upperBoundX = (int) (imageRectangle.getX() + imageRectangle
                .getWidth());
        final int upperBoundY = (int) (imageRectangle.getY() + imageRectangle
                .getHeight());
        for (int y = (int) imageRectangle.getY(); y < upperBoundY; ++y) {
            for (int x = (int) imageRectangle.getX(); x < upperBoundX; ++x) {
                final int value = gi.getPixelAsInt(x, y);
                histogramm[value]++;
            }
        }
        return histogramm;
    }

    /**
     * Gets all lines from this image
     * 
     * @return all lines
     */
    public Collection<ImageLine> getLines() {
        return this.lines.values();
    }

    /**
     * This method is used to get the path of image file which is hold by
     * documentimage object
     * 
     * @return the path of image file
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the corresponding glyphs for a search request
     * 
     * @param results
     *            the result of the request
     * @return the glyphs
     */
    public ArrayList<Glyph[]> getSearchedGlyphs(
            final ArrayList<String[]> results) {
        final ArrayList<ImageLine> sortedImageLines = new ArrayList<>(
                this.lines.values());
        Collections.sort(sortedImageLines);
        final ArrayList<Glyph[]> resultingGlyphs = new ArrayList<>(
                results.size());
        for (final String[] res : results) {
            final Glyph[] glyphRes = new Glyph[res.length];
            int counter = 0;
            for (final String id : res) {
                glyphRes[counter] = this.getGlyph(null, id);
                ++counter;
            }
            resultingGlyphs.add(glyphRes);
        }
        return resultingGlyphs;
    }

    /**
     * Grays an image with the specified parameter
     * 
     * @param redWeight
     *            the red weight
     * @param greenWeight
     *            the green weight
     * @param blueWeight
     *            the blue weight
     * @return the grayimage
     */
    public GrayImage gray(final double redWeight, final double greenWeight,
            final double blueWeight) {
        return this.gray(redWeight, greenWeight, blueWeight, 0,
                (int) this.documentImage.getWidth(), 0,
                (int) this.documentImage.getHeight());
    }

    /**
     * This method is used to change the gray scale of the image within a
     * specified region
     * 
     * @param redWeight
     *            red weight
     * @param greenWeight
     *            green weight
     * @param blueWeight
     *            blue weight
     * @param xStart
     *            the x coordinate of the starting point
     * @param xEnd
     *            the x coordinate of the end point
     * @param yStart
     *            the y coordinate of the starting point
     * @param yEnd
     *            the y coordinate of the end point
     * @return gray scaled image
     */
    public GrayImage gray(final double redWeight, final double greenWeight,
            final double blueWeight, int xStart, int xEnd, int yStart, int yEnd) {
        if (this.grayImage != null && this.curRedWeight == redWeight
                && this.curGreenWeight == greenWeight
                && this.curBlueWeight == blueWeight && xStart == 0
                && yStart == 0 && xEnd == this.grayImage.getWidth()
                && yEnd == this.grayImage.getHeight()) {
            return this.grayImage;
        }
        this.curRedWeight = redWeight;
        this.curGreenWeight = greenWeight;
        this.curBlueWeight = blueWeight;
        final PixelReader pixelReader = this.documentImage.getPixelReader();

        final int width = (int) this.documentImage.getWidth();
        final int height = (int) this.documentImage.getHeight();
        byte[] pixels;
        if (this.grayImage != null) {
            pixels = this.grayImage.getPixels();
        } else {
            pixels = new byte[width * height];
            xStart = 0;
            yStart = 0;
            xEnd = width;
            yEnd = height;
        }
        for (int y = yStart; y < yEnd; ++y) {
            final int offset = y * width;
            for (int x = xStart; x < xEnd; ++x) {
                final int index = offset + x;
                final Color col = pixelReader.getColor(x, y);
                final double r = col.getRed() * redWeight;
                final double g = col.getGreen() * greenWeight;
                final double b = col.getBlue() * blueWeight;
                pixels[index] = (byte) ((r + g + b) * 255);
            }
        }
        this.grayImage = new GrayImage(pixels, width, height);
        return this.grayImage;
    }

    /**
     * This method is used to load the image specified by path
     * 
     * @param path
     *            the path of image file
     * @param showBinarizedImage
     *            <code>true</code> if the binarized image should be shown
     *            <code>false</code> otherwise
     * @return the object of documentimage which type is PlanarImage
     */
    public Image load(final String path, final boolean showBinarizedImage) {
        if (this.documentImage == null) {
            try {
                final BufferedImage img = Imaging.getBufferedImage(new File(
                        path + this.name));
                this.reactOnImageType(img);
            } catch (final ImageReadException e) {
                try {
                    // unfortunately Apache Imaging can only work sequential
                    // baseline jpeg
                    // so we have to use a workaround in that case
                    final BufferedImage img = ImageIO.read(new File(path
                            + this.name));
                    this.reactOnImageType(img);
                } catch (final IOException e1) {
                    DiptychonLogger.error("{}", e1);
                }
            } catch (final IOException e) {
                DiptychonLogger.error("{}", e);
            }
        }
        return this.getCurrentImage(showBinarizedImage);
    }

    /**
     * Crops the image after reloading
     */
    private void maskAfterReload() {
        this.documentImage = ImageUtils.cropFXImage(this.documentImage,
                this.masked);
    }

    /**
     * This method is used to mask the image
     * 
     * @param mask
     *            the region which is marked.
     * @return the ids of the glyphs which were removed due to cropping
     */
    public ArrayList<String> maskImage(final Rectangle mask) {
        this.documentImage = ImageUtils.cropFXImage(this.documentImage, mask);

        this.grayImage = this.getGrayImage();
        if (!(this.grayImage.getWidth() == mask.getWidth() && this.grayImage
                .getHeight() == mask.getHeight())) {
            this.grayImage = ImageUtils
                    .cropGrayImage(this.getGrayImage(), mask);
        }

        this.binaryImage = this.getBinarizedImage();
        if (!(this.binaryImage.getWidth() == mask.getWidth() && this.binaryImage
                .getHeight() == mask.getHeight())) {
            this.binaryImage = ImageUtils.cropGrayImage(
                    this.getBinarizedImage(), mask);
        }

        if (this.masked == null) {
            this.masked = mask;
        } else {
            this.masked = new Rectangle(this.masked.getX() + mask.getX(),
                    this.masked.getY() + mask.getY(), mask.getWidth(),
                    mask.getHeight());
        }

        final int deltaX = (int) -mask.getX();
        final int deltaY = (int) -mask.getY();

        this.glyphsWithoutLine = this.moveAndFilterGlyphs(
                this.glyphsWithoutLine, deltaX, deltaY);

        final int imageWidth = this.grayImage.getWidth();
        final int imageHeight = this.grayImage.getHeight();

        final ArrayList<String> removedIDs = new ArrayList<>();
        final Iterator<Entry<String, ImageLine>> it = this.lines.entrySet()
                .iterator();
        while (it.hasNext()) {
            final Map.Entry<String, ImageLine> entry = it.next();

            final ImageLine il = entry.getValue();
            il.moveByAfterMaskingAndFixWidth(deltaX, deltaY, imageWidth);
            HashMap<String, Glyph> lineGlyphs = il.getGlyphsMap();
            lineGlyphs = this.moveAndFilterGlyphs(lineGlyphs, deltaX, deltaY);

            final Rectangle bounds = il.getBounds();

            if (bounds.getWidth() < 0 || bounds.getY() < 0 || bounds.getY() < 0
                    || bounds.getY() > imageHeight
                    || bounds.getY() + bounds.getHeight() > imageHeight) {
                DiptychonLogger.debug(
                        "Removing Line (ID: {}) due to image masking. "
                                + "Line would lie outside of the image",
                        il.getID());
                removedIDs.add(il.getID());
                it.remove();
            }
        }
        return removedIDs;
    }

    /**
     * Matches all glyphs as templates for a single line
     * 
     * @param lineID
     *            the id of the line
     * @param transcript
     *            ...
     *
     * @return a list of Glyphs which have been found
     */
    public ArrayList<Glyph> matchAllTemplatesSingleLine(final String lineID,
            final Transcript transcript) {
        final ArrayList<Glyph> glyphs = this.getAllGlyphs();
        if (glyphs.size() == 0)
            return null;

        DiptychonFX.progressWatch = new int[1];
        DiptychonFX.progressWatch[0] = 0;

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                transcript
                        .firePropertyChange(
                                DocumentPanelController.PROGRESS_CHANGED,
                                null,
                                (double) DiptychonFX.progressWatch[0]
                                        / (glyphs.size()));
            }
        });
        thread.start();

        final ArrayList<ImageLine> newLines = new ArrayList<>(this.getLines());

        SearchEngine se = new SearchEngine(glyphs, lineID, newLines,
                this.documentImage, this.getGrayImage(),
                this.getBinarizedImage(), transcript);

        // ArrayList<Glyph> matchedGlyphs = se.searchGlyphsInParallel();
        ArrayList<Glyph> matchedGlyphs = se.searchGlyphsSequentially();

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                transcript.firePropertyChange(
                        DocumentPanelController.PROGRESS_CHANGED, null,
                        (double) 100);
            }
        });

        System.out.println("TOTAL NUM OF FOUND GLYPH CANDIDATES: "
                + matchedGlyphs.size());

        return matchedGlyphs;
    }

    /**
     * Moves all glyphs after cropping
     * 
     * @param glyphs
     *            the glyphs
     * @param deltaX
     *            the delta to move in x direction
     * @param deltaY
     *            the delta to move in y direction
     * @return the moved glyphs
     */
    private HashMap<String, Glyph> moveAndFilterGlyphs(
            final HashMap<String, Glyph> glyphs, final int deltaX,
            final int deltaY) {
        final int imageWidth = this.grayImage.getWidth();
        final int imageHeight = this.grayImage.getHeight();

        final Set<Entry<String, Glyph>> entries = glyphs.entrySet();

        for (final Entry<String, Glyph> e : entries) {
            final Glyph g = e.getValue();
            g.moveByAfterMasking(deltaX, deltaY);
            if ((g.getLayoutX() + g.getWidth()) >= imageWidth
                    || (g.getLayoutY() + g.getHeight()) >= imageHeight
                    || g.getLayoutX() < 0 || g.getLayoutY() < 0) {
                DiptychonLogger.debug(
                        "Removing Glyph (ID: {}) due to image masking. "
                                + "Glyph would lie outside of the image",
                        g.getLineID() + "_" + g.getID());
            }
        }

        return glyphs;
    }

    /**
     * According to the imagetype it has to be handle differently to create the
     * correct corresponding javafx image
     * 
     * @param img
     *            the image
     */
    private void reactOnImageType(final BufferedImage img) {
        if (img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            final int width = img.getWidth();
            final int height = img.getHeight();
            int[] rgbArray = new int[width * height];
            rgbArray = img.getRGB(0, 0, width, height, rgbArray, 0, width);
            this.documentImage = ImageUtils.getJavaFXImage(rgbArray, width,
                    height);
        } else {
            this.documentImage = ImageUtils.getJavaFXImage(img);
        }
        if (this.masked != null) {
            this.maskAfterReload();
        }
        this.reapplyAutocontrast();
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
        DiptychonLogger.debug("Reading document image from hdd");
        s.defaultReadObject();
        final double x = s.readDouble();
        final double y = s.readDouble();
        final double width = s.readDouble();
        final double height = s.readDouble();
        if (x != 0.d && y != 0.d && width != 0.d && height != 0.d) {
            this.masked = new Rectangle(x, y, width, height);
        } else {
            this.masked = null;
        }
    }

    /**
     * Applies the contrast history after image reloading
     */
    public void reapplyAutocontrast() {
        if (this.contrastAdaptionHistory != null) {
            for (final AutoContrastOperation aco : this.contrastAdaptionHistory) {
                this.autoContrastImage(aco);
            }
        }
    }

    /**
     * Removes the glyph with id id
     * 
     * @param id
     *            the id
     * @return the id of the line the glyph was removed from
     */
    public String removeGlyph(final String id) {
        if (this.glyphsWithoutLine.remove(id) != null) {
            DiptychonLogger.debug(
                    "Glyph (ID: {} was removed. It was not within a line.", id);
            return "";
        }
        String lineID = "";
        final Set<Entry<String, ImageLine>> entries = this.lines.entrySet();
        for (final Entry<String, ImageLine> entry : entries) {
            if (entry.getValue().removeGlyph(id) != null) {
                lineID = entry.getValue().getID();
                DiptychonLogger.debug(
                        "Glyph (ID: {}) was removed from line (ID: {}).", id,
                        lineID);
                break;
            }
        }
        return lineID;
    }

    /**
     * Removes all glyphs from the specified line.
     * 
     * @param lineID
     *            the id of the line
     */
    public void removeGlyphsFromLine(final String lineID) {
        this.lines.get(lineID).removeGlyphs();
    }

    /**
     * Removes the line with id id from this image
     * 
     * @param id
     *            the id
     */
    public void removeTextline(final String id) {
        this.lines.remove(id);
    }

    /**
     * Resets the complete image to gray, which means that
     * autocontrastoperations will be undone
     */
    public void resetToGray() {
        this.resetToGray(0, (int) this.documentImage.getWidth(), 0,
                (int) this.documentImage.getHeight());
    }

    /**
     * Resets the specified region of the image to gray, which means that
     * autocontrastoperations will be undone
     * 
     * @param xStart
     *            the x coordinate of the starting point
     * @param xEnd
     *            the x coordinate of the end point
     * @param yStart
     *            the y coordinate of the starting point
     * @param yEnd
     *            the y coordinate of the end point
     */
    public void resetToGray(final int xStart, final int xEnd, final int yStart,
            final int yEnd) {
        final double tmpRed = this.curRedWeight;
        final double tmpGreen = this.curGreenWeight;
        final double tmpBlue = this.curBlueWeight;
        this.curRedWeight = 0.d;
        this.curGreenWeight = 0.d;
        this.curBlueWeight = 0.d;
        this.gray(tmpRed, tmpGreen, tmpBlue, xStart, xEnd, yStart, yEnd);
    }

    /**
     * Sets the glyphs of the line with id lineID
     * 
     * @param lineID
     *            the id of the line
     * @param glyphs
     *            the glyphs
     */
    public void setGlyphs(final String lineID, final ArrayList<Glyph> glyphs) {
        this.lines.get(lineID).setGlyphs(glyphs);
    }

    /**
     * Marks a line as handwritten annotation if it was not, otherwise it will
     * be unset
     * 
     * @param lineID
     *            the id of the line
     */
    public void setHandwrittenAnnotation(final String lineID) {
        this.lines.get(lineID).setHandwrittenAnnotation();
    }

    /**
     * Sets the current version of the image (grayscale or binarized)
     * 
     * @param showBinarizedImage
     *            <code>true</code> if the binarized image should be shown
     *            <code>false</code> otherwise
     * @return the current version of the image
     */
    public Image showBinarizedImage(final boolean showBinarizedImage) {
        return this.getCurrentImage(showBinarizedImage);
    }

    /**
     * Splits an imageline by using the information of the transcription
     * 
     * @param decoding
     *            the transcription
     * @param encoding
     *            the encoded transcription
     * @param lineID
     *            the id of the line
     * @return the splitlinesuggestion
     */
    public Line[] splitImageLine(final String[] decoding,
            final String[] encoding, final String lineID) {
        final ImageLine il = this.lines.get(lineID);
        final Image original = ImageUtils.cropFXImage(this.documentImage,
                il.getBounds());
        final GrayImage newGrayImage = ImageUtils.cropGrayImage(
                this.getGrayImage(), il.getBounds());
        final GrayImage newBinaryImage = ImageUtils.cropGrayImage(
                this.getBinarizedImage(), il.getBounds());

        return il.split(original, newGrayImage, newBinaryImage, decoding,
                encoding);
    }

    /**
     * Splits an imageline
     * 
     * @param decoding
     *            the transcription
     * @param encoding
     *            the encoded transcription
     * @param splits
     *            the splits to apply
     * @param lineID
     *            the id of the line
     */
    public ArrayList<Glyph> splitImageLineApply2(final String[] decoding,
            final String[] encoding, final ArrayList<Line> splits,
            final String lineID, float fragSize, float fragCount, int noiseThres) {
        final ArrayList<Integer> spaceIndices = new ArrayList<Integer>();
        final ArrayList<Integer> numOfElementsBetweenSpaces = new ArrayList<Integer>();
        int index = 0;
        int elementCounter = 0;
        int totalElements = 0;
        if (decoding[0] == null) {
            totalElements = 1;
        } else {
            for (final String s : decoding) {
                if (index == decoding.length - 1) {
                    numOfElementsBetweenSpaces.add(elementCounter + 1);
                }
                if (s.equals(" ")) {
                    if (index != decoding.length - 1) {
                        if (decoding[index - 1].equals(" ")) {
                            continue;
                        }
                    }
                    spaceIndices.add(index);
                    numOfElementsBetweenSpaces.add(elementCounter);
                    elementCounter = -1;
                }
                ++elementCounter;
                ++index;
            }

            for (int num : numOfElementsBetweenSpaces) {
                totalElements = totalElements + num;
            }
        }
        final ImageLine il = this.lines.get(lineID);

        ArrayList<GrayImage> binaryImages = new ArrayList<GrayImage>();
        ArrayList<Integer> starts = new ArrayList<Integer>();

        ArrayList<Glyph> spaces = new ArrayList<Glyph>();

        for (int i = splits.size() - 1; i > 0; i--) {
            final Line start = splits.get(i);
            final Line end = splits.get(i - 1);

            starts.add((int) start.getStartX());

            final Rectangle crop = new Rectangle(start.getStartX(),
                    start.getStartY(), end.getEndX() - start.getStartX(),
                    il.getHeight());

            binaryImages.add(ImageUtils.cropGrayImage(this.getBinarizedImage(),
                    crop));

            if (i != splits.size() - 1) {
                final Rectangle cropSpace = new Rectangle(start.getStartX(),
                        start.getStartY(), 2, il.getHeight());

                GrayImage binaryImage = ImageUtils.cropGrayImage(
                        this.getBinarizedImage(), cropSpace);

                Glyph space = Glyph.extractGlyph(ImageUtils.cropFXImage(
                        this.documentImage, cropSpace), ImageUtils
                        .cropGrayImage(this.getGrayImage(), cropSpace),
                        binaryImage, cropSpace);

                space.setGroupID(" ");

                spaces.add(space);
            }

        }

        ArrayList<Glyph> glyphs = new ArrayList<Glyph>();

        int lastID = 0;

        for (int h = 0; h < binaryImages.size(); h++) {

            RecursiveGlyphFragmentation rgf = new RecursiveGlyphFragmentation(
                    binaryImages.get(h));
            ConnectedObjects co;

            if (fragSize == 0) {
                fragSize = 50;
            }
            if (fragCount == 0.0) {
                fragCount = (float) 1.4;
            }

            if (splits.size() == 2) {
                co = rgf.getGlyphFragments((int) (totalElements * fragCount),
                        fragSize, false, noiseThres);
            } else {
                co = rgf.getGlyphFragments(
                        (int) (numOfElementsBetweenSpaces.get(h) * fragCount),
                        fragSize, false, noiseThres);
            }
            ArrayList<Integer> labels = co.getLabelList();
            int[][] fragmentArray = co.getMatrix();

            /*
             * System.out.println(""); System.out.println("FRAGMENT ARRAY:");
             * System.out.println(""); for (int y = 0; y < fragmentArray.length;
             * y++) { System.out.println(""); for (int x = 0; x <
             * fragmentArray[y].length; x++) { if (fragmentArray[y][x] < 10) {
             * System.out.print("0"); } System.out.print(fragmentArray[y][x]); }
             * }
             */

            lastID = co.getUsedLabelCount() - 1;

            for (int i = 0; i < co.getUsedLabelCount(); i++) {
                int minX = co.getLabelMinX(labels.get(i));
                int minY = co.getLabelMinY(labels.get(i));
                int maxX = co.getLabelMaxX(labels.get(i));
                int maxY = co.getLabelMaxY(labels.get(i));
                int width = maxX - minX + 1;
                int height = maxY - minY + 1;

                byte[] glyphArray = new byte[width * height];
                for (int y = minY; y < maxY + 1; y++) {
                    for (int x = minX; x < maxX + 1; x++) {
                        if (fragmentArray[y][x] == labels.get(i)) {
                            glyphArray[(y - minY) * width + x - minX] = (byte) 0;
                        } else {
                            glyphArray[(y - minY) * width + x - minX] = (byte) 255;
                        }
                    }
                }

                Rectangle mask = new Rectangle();
                mask.setX(il.getLayoutX() + minX + starts.get(h)
                        - starts.get(0));
                mask.setY(il.getLayoutY() + minY);
                mask.setWidth(width);
                mask.setHeight(height);

                final GrayImage image = new GrayImage(glyphArray, width, height);

                mask.setX(mask.getX() - 1);
                mask.setY(mask.getY() - 1);

                final Glyph glyph = Glyph.extractGlyph(
                        ImageUtils.cropFXImage(this.documentImage, mask),
                        ImageUtils.cropGrayImage(this.getGrayImage(), mask),
                        image, mask);

                glyph.setGroupID("X");
                glyph.setID("F_" + i);
                glyphs.add(glyph);
            }

        }

        ArrayList<Glyph> fragsToRemove = new ArrayList<Glyph>();

        for (Glyph g : glyphs) {
            for (Glyph f : il.getGlyphs()) {
                if (g.isAlreadyExtractedStrict(f)) {
                    g.subtract(f);
                    if (g.isEmpty) {
                        fragsToRemove.add(g);
                    }
                }
                if (f.isSpace) {
                    fragsToRemove.add(f);
                }
            }
        }

        for (Glyph f : il.getGlyphs()) {
            if (!f.isSpace) {
                f.formerID = f.getID();
                f.setID("F_" + lastID);
                f.isFormerGlyph = true;
                lastID++;
            }
        }

        glyphs.addAll(il.getGlyphs());

        glyphs.removeAll(fragsToRemove);

        Collections.sort(glyphs);
        return glyphs;
    }

    /**
     * Splits an imageline
     * 
     * @param decoding
     *            the transcription
     * @param encoding
     *            the encoded transcription
     * @param splits
     *            the splits to apply
     * @param lineID
     *            the id of the line
     */
    public void splitImageLineApply(final String[] decoding,
            final String[] encoding, final ArrayList<Line> splits,
            final String lineID) {
        this.lines.get(lineID).removeGlyphs();
        final ImageLine il = this.lines.get(lineID);
        final Image original = ImageUtils.cropFXImage(this.documentImage,
                il.getBounds());
        final GrayImage newGrayImage = ImageUtils.cropGrayImage(
                this.getGrayImage(), il.getBounds());
        final GrayImage newBinaryImage = ImageUtils.cropGrayImage(
                this.getBinarizedImage(), il.getBounds());

        this.lines.get(lineID).split(splits, original, newGrayImage,
                newBinaryImage, decoding, encoding);
    }

    /**
     * Updates a split line suggestion
     * 
     * @param decoding
     *            the transcription
     * @param encoding
     *            the encoded transcription
     * @param lineID
     *            the id of the line
     * @param width
     *            the estimated average width of characters
     * @return the updated suggestion
     */
    public Line[] splitImageLinePreview(final String[] decoding,
            final String[] encoding, final String lineID, final int width) {
        final ImageLine il = this.lines.get(lineID);
        final Image original = ImageUtils.cropFXImage(this.documentImage,
                il.getBounds());
        final GrayImage newGrayImage = ImageUtils.cropGrayImage(
                this.getGrayImage(), il.getBounds());
        final GrayImage newBinaryImage = ImageUtils.cropGrayImage(
                this.getBinarizedImage(), il.getBounds());

        return il.split(original, newGrayImage, newBinaryImage, decoding,
                encoding, width);
    }

    /**
     * Updates a split line suggestion
     * 
     * @param decoding
     *            the transcription
     * @param encoding
     *            the encoded transcription
     * @param lineID
     *            the id of the line
     * @param width
     *            the estimated average width of characters
     * @return the updated suggestion
     */
    public Line[] splitImageLinePreview2(final String[] decoding,
            final String[] encoding, final String lineID, final int width) {
        final ImageLine il = this.lines.get(lineID);
        final Image original = ImageUtils.cropFXImage(this.documentImage,
                il.getBounds());
        final GrayImage newGrayImage = ImageUtils.cropGrayImage(
                this.getGrayImage(), il.getBounds());
        final GrayImage newBinaryImage = ImageUtils.cropGrayImage(
                this.getBinarizedImage(), il.getBounds());

        return il.split2(original, newGrayImage, newBinaryImage, decoding,
                encoding, width);
    }

    /**
     * This method is used to unload the image file by setting data members to
     * null
     */
    public void unload() {
        // Warum ist das auskommentiert? (Marius)
        // this.documentImage = null;
    }

    /**
     * Updates the binary image
     */
    public void updateBinaryImage() {
        if (this.curK != -1 && this.curWindowSize != -1) {
            final double tmpK = this.curK;
            final int tmpWindowSize = this.curWindowSize;
            this.curK = 0;
            this.curWindowSize = 0;
            this.binarize(tmpWindowSize, tmpK);
        } else {
            this.binarize(DEFAULT_WINDOW_SIZE, DEFAULT_K);
        }
    }

    /**
     * Updates the glyph ids within an image line if the transcription was
     * changed
     * 
     * @param decoded
     *            the transcription
     * @param encoding
     *            the encoded transcription
     * @param lineID
     *            the id of the line
     */
    public void updateGlyphIDs(final String[] decoded, final String[] encoding,
            final String lineID) {
        this.lines.get(lineID).updateGlyphIDs(decoded, encoding);
    }

    /**
     * Updates the glyph statistics if the transcription was changed
     * 
     */
    public void updateGlyphStats() {
        ArrayList<Glyph> glyphs = this.getAllGlyphs();
        if (this.glyphStats != null) {
            this.glyphStats.clear();
        } else {
            this.glyphStats = new HashMap<>();
        }

        ArrayList<String> groupCount = new ArrayList<String>();

        for (final Glyph g : glyphs) {
            if (g.isAnnotation) {
                continue;
            }

            String GID = g.getGroupID();
            groupCount.add(GID);
            if (this.glyphStats.get(GID) != null) {
                this.glyphStats.get(GID).set(0,
                        (this.glyphStats.get(GID).get(0) + g.getWidth()));
                this.glyphStats.get(GID).set(1,
                        (this.glyphStats.get(GID).get(1) + g.getHeight()));
                this.glyphStats.get(GID).set(2,
                        (this.glyphStats.get(GID).get(2) + g.getSize()));
            } else {
                ArrayList<Float> statsArray = new ArrayList<>();
                statsArray.add((float) g.getWidth());
                statsArray.add((float) g.getHeight());
                statsArray.add((float) g.getSize());
                statsArray.add((float) 0);
                this.glyphStats.put(GID, statsArray);
            }
        }

        for (final String GID : this.glyphStats.keySet()) {
            int frequency = Collections.frequency(groupCount, GID);
            this.glyphStats.get(GID).set(0,
                    (this.glyphStats.get(GID).get(0) / frequency));
            this.glyphStats.get(GID).set(1,
                    (this.glyphStats.get(GID).get(1) / frequency));
            this.glyphStats.get(GID).set(2,
                    (this.glyphStats.get(GID).get(2) / frequency));
            this.glyphStats.get(GID).set(3, (float) frequency);
            System.out.println(GID + ": BREITE "
                    + this.glyphStats.get(GID).get(0) + " HÖHE "
                    + this.glyphStats.get(GID).get(1) + " PIXEL "
                    + this.glyphStats.get(GID).get(2) + " HÄUFIGKEIT "
                    + this.glyphStats.get(GID).get(3));
        }

    }

    /**
     * Updates the document statistics
     * 
     */
    public void updateDocumentStats() {
        ArrayList<ImageLine> lines = new ArrayList<ImageLine>(this.getLines());
        ArrayList<Glyph> glyphs = this.getAllGlyphs();

        if (this.documentStats != null) {
            this.documentStats.clear();
            this.documentStats.add((float) 0);
            this.documentStats.add((float) 0);
            this.documentStats.add((float) 0);
            this.documentStats.add((float) 0);
            this.documentStats.add((float) 0);
            this.documentStats.add((float) 0);
            this.documentStats.add((float) 0);
            this.documentStats.add((float) 0);
            this.documentStats.add((float) 0);
        } else {
            this.documentStats = new ArrayList<Float>();
            this.documentStats.add((float) 0);
            this.documentStats.add((float) 0);
            this.documentStats.add((float) 0);
            this.documentStats.add((float) 0);
            this.documentStats.add((float) 0);
            this.documentStats.add((float) 0);
            this.documentStats.add((float) 0);
            this.documentStats.add((float) 0);
            this.documentStats.add((float) 0);
        }

        int count = 0;
        int abbcount = 0;
        for (final ImageLine l : lines) {
            if (l.isHandwrittenAnnotation()) {
                continue;
            }
            this.documentStats.set(0, (this.documentStats.get(0) + 1));
            ArrayList<Float> distancesDown = new ArrayList<Float>();
            ArrayList<Float> distancesUp = new ArrayList<Float>();
            for (final ImageLine l2 : lines) {
                if (l != l2) {
                    float distance = (float) ((l2.getLayoutY() + l2.getHeight() / 2) - (l
                            .getLayoutY() + l.getHeight() / 2));
                    if (distance > 0) {
                        distancesDown.add(distance);
                    }
                    distance = (float) ((l.getLayoutY() + l.getHeight() / 2) - (l2
                            .getLayoutY() + l2.getHeight() / 2));
                    if (distance > 0) {
                        distancesUp.add(distance);
                    }
                }
            }
            if (distancesDown.size() > 0) {
                this.documentStats.set(2,
                        (this.documentStats.get(2) + Collections
                                .min(distancesDown)));
            }
            if (distancesUp.size() > 0) {
                this.documentStats.set(2,
                        (this.documentStats.get(2) + Collections
                                .min(distancesUp)));
            }

            ArrayList<Glyph> lineGlyphs = new ArrayList<Glyph>(l.getGlyphs());
            if (lineGlyphs.size() > 0) {
                Collections.sort(lineGlyphs);
                for (int i = 0; i < lineGlyphs.size(); i++) {
                    if (lineGlyphs.get(i).getGroupID().length() > 1) {
                        abbcount++;
                        if (i > 0) {
                            if (i < lineGlyphs.size() - 1) {
                                if (lineGlyphs.get(i - 1).isSpace
                                        && !lineGlyphs.get(i + 1).isSpace) { // NUR
                                                                             // LINKS
                                                                             // SPACE
                                    this.documentStats.set(3,
                                            this.documentStats.get(3) + 1);
                                    this.documentStats.set(7,
                                            this.documentStats.get(7) + 1); // NUR
                                                                            // RECHTS
                                                                            // GLYPHE
                                }
                            } else {
                                if (lineGlyphs.get(i - 1).isSpace) {
                                    this.documentStats.set(3,
                                            this.documentStats.get(3) + 1);
                                    this.documentStats.set(5,
                                            this.documentStats.get(5) + 1); // ABKÜRZUNG
                                                                            // ALLEINESTEHEND
                                } else {
                                    this.documentStats.set(6,
                                            this.documentStats.get(6) + 1); // NUR
                                                                            // LINKS
                                                                            // GLYPHE
                                }
                            }
                        }
                        if (i < lineGlyphs.size() - 1) {
                            if (i > 0) {
                                if (!lineGlyphs.get(i - 1).isSpace
                                        && (lineGlyphs.get(i + 1).isSpace
                                                || lineGlyphs.get(i + 1)
                                                        .getGroupID()
                                                        .equals("?")
                                                || lineGlyphs.get(i + 1)
                                                        .getGroupID()
                                                        .equals(".") || lineGlyphs
                                                .get(i + 1).getGroupID()
                                                .equals(","))) { // NUR RECHTS
                                                                 // SPACE
                                    this.documentStats.set(4,
                                            this.documentStats.get(4) + 1);
                                    this.documentStats.set(6,
                                            this.documentStats.get(6) + 1); // NUR
                                                                            // LINKS
                                                                            // GLYPHE
                                }
                            } else {
                                if (lineGlyphs.get(i + 1).isSpace
                                        || lineGlyphs.get(i + 1).getGroupID()
                                                .equals("?")
                                        || lineGlyphs.get(i + 1).getGroupID()
                                                .equals(".")
                                        || lineGlyphs.get(i + 1).getGroupID()
                                                .equals(",")) {
                                    this.documentStats.set(4,
                                            this.documentStats.get(4) + 1);
                                    this.documentStats.set(5,
                                            this.documentStats.get(5) + 1); // ABKÜRZUNG
                                                                            // ALLEINESTEHEND

                                } else {
                                    this.documentStats.set(7,
                                            this.documentStats.get(7) + 1); // NUR
                                                                            // RECHTS
                                                                            // GLYPHE
                                }
                            }
                        }
                        if (i > 0 && i < lineGlyphs.size() - 1) {
                            if (lineGlyphs.get(i - 1).isSpace
                                    && lineGlyphs.get(i + 1).isSpace) {
                                this.documentStats.set(5,
                                        this.documentStats.get(5) + 1); // ABKÜRZUNG
                                                                        // ALLEINESTEHEND
                            } else if (!lineGlyphs.get(i - 1).isSpace
                                    && !(lineGlyphs.get(i + 1).isSpace
                                            || lineGlyphs.get(i + 1)
                                                    .getGroupID().equals("?")
                                            || lineGlyphs.get(i + 1)
                                                    .getGroupID().equals(".") || lineGlyphs
                                            .get(i + 1).getGroupID()
                                            .equals(","))) {
                                this.documentStats.set(8,
                                        this.documentStats.get(8) + 1); // BEIDE
                                                                        // SEITEN
                                                                        // GLYPHE
                            }
                        }
                    }
                }
            }

            count++;
        }
        this.documentStats
                .set(2, (this.documentStats.get(2) / (count * 2 - 2)));

        count = 0;

        for (final Glyph g : glyphs) {
            if (!g.isSpace && !g.isAnnotation) {
                this.documentStats.set(1, (this.documentStats.get(1) + 1));
                count++;
            }
        }

        System.out.println("ANZAHL GLYPHEN: " + this.documentStats.get(1));
        System.out.println("ANZAHL ZEILEN: " + this.documentStats.get(0));
        System.out.println("MITTLERER ZEILENABSTAND: "
                + this.documentStats.get(2));
        /*
         * System.out.println("ABKÜRZUNGEN MIT LEERZEICHEN NUR LINKS: " +
         * this.documentStats.get(3));
         * System.out.println("ABKÜRZUNGEN MIT LEERZEICHEN NUR RECHTS: " +
         * this.documentStats.get(4));
         * System.out.println("ABKÜRZUNGEN MIT LEERZEICHEN AUF BEIDEN SEITEN: "
         * + this.documentStats.get(5));
         * System.out.println("ABKÜRZUNGEN MIT GLYPHE NUR LINKS: " +
         * this.documentStats.get(6));
         * System.out.println("ABKÜRZUNGEN MIT GLYPHE NUR RECHTS: " +
         * this.documentStats.get(7));
         * System.out.println("ABKÜRZUNGEN MIT GLYPHE AUF BEIDEN SEITEN: " +
         * this.documentStats.get(8));
         */

        System.out.println("ABKÜRZUNGEN INSGESAMT: " + abbcount);
        System.out.println("ABKÜRZUNGEN ALLEINSTEHEND: "
                + this.documentStats.get(5));
        System.out.println("ABKÜRZUNGEN AM WORTENDE: "
                + this.documentStats.get(6));
        System.out.println("ABKÜRZUNGEN AM WORTANFANG: "
                + this.documentStats.get(7));
        System.out.println("ABKÜRZUNGEN IN WORTMITTE: "
                + this.documentStats.get(8));

    }

    /**
     * Updates the word statistics
     * 
     */
    public void updateWordStats() {
        ArrayList<ImageLine> lines = new ArrayList<ImageLine>(this.getLines());

        if (this.wordStats != null) {
            this.wordStats.clear();
            this.wordStats.add((float) 0);
            this.wordStats.add((float) 0);
            this.wordStats.add((float) 0);
        } else {
            this.wordStats = new ArrayList<Float>();
            this.wordStats.add((float) 0);
            this.wordStats.add((float) 0);
            this.wordStats.add((float) 0);
        }

        int lastSpace = -1;
        int wordCount = 0;
        for (final ImageLine l : lines) {
            if (l.isHandwrittenAnnotation()) {
                continue;
            }
            lastSpace = -1;
            ArrayList<Glyph> lineGlyphs = new ArrayList<Glyph>(l.getGlyphs());
            if (lineGlyphs.size() > 0) {
                wordCount++;
                Collections.sort(lineGlyphs);
                for (int i = 0; i < lineGlyphs.size(); i++) {
                    if (lineGlyphs.get(i).isSpace) {
                        wordCount++;
                        float distance = lineGlyphs.get(i + 1).getLayoutX()
                                - (lineGlyphs.get(i - 1).getLayoutX() + lineGlyphs
                                        .get(i - 1).getWidth());
                        this.wordStats.set(0, this.wordStats.get(0) + distance);
                        this.wordStats.set(1, this.wordStats.get(1)
                                + ((i - lastSpace) - 1));
                        lastSpace = i;
                    } else if (i == lineGlyphs.size() - 1) {
                        this.wordStats.set(1, this.wordStats.get(1)
                                + ((i - lastSpace)));
                    }
                }
            }
        }

        this.wordStats.set(0, this.wordStats.get(0) / wordCount);
        this.wordStats.set(1, this.wordStats.get(1) / wordCount);
        this.wordStats.set(2, (float) wordCount);
        System.out.println("MITTLERER WORTABSTAND: " + this.wordStats.get(0));
        System.out.println("MITTLERE ANZAHL ZEICHEN IN WORT: "
                + this.wordStats.get(1));
        System.out.println("ANZAHL WÖRTER: " + this.wordStats.get(2));
    }

    private float calcLineSlant(ArrayList<Point2D> points) {

        if (points.size() < 2) {
            return 0;
        }

        int MAXN = 1000;
        double[] x = new double[MAXN];
        double[] y = new double[MAXN];

        // first pass: read in data, compute xbar and ybar
        double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
        for (int n = 0; n < points.size(); n++) {
            x[n] = points.get(n).getX();
            y[n] = points.get(n).getY();
            sumx += x[n];
            sumx2 += x[n] * x[n];
            sumy += y[n];
        }
        double xbar = sumx / points.size();
        double ybar = sumy / points.size();

        // second pass: compute summary statistics
        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (int i = 0; i < points.size(); i++) {
            xxbar += (x[i] - xbar) * (x[i] - xbar);
            yybar += (y[i] - ybar) * (y[i] - ybar);
            xybar += (x[i] - xbar) * (y[i] - ybar);
        }
        double beta1 = xybar / xxbar;
        double beta0 = ybar - beta1 * xbar;

        Float[] endpoints = { (float) (beta1 * points.get(0).getX() + beta0),
                (float) (beta1 * points.get(points.size() - 1).getX() + beta0) };

        return endpoints[0] - endpoints[1];

        // print results
    }

    /**
     * Updates the line statistics
     * 
     */
    public void updateLineStats() {
        ArrayList<ImageLine> lines = new ArrayList<ImageLine>(this.getLines());
        if (this.lineStats2 != null) {
            this.lineStats2.clear();
            this.lineStats2.add((float) 0);
            this.lineStats2.add((float) 0);
            this.lineStats2.add((float) 0);
            this.lineStats2.add((float) 0);
            this.lineStats2.add((float) 0);
            this.lineStats2.add((float) 0);
            this.lineStats2.add((float) 0);
        } else {
            this.lineStats2 = new ArrayList<Float>();
            this.lineStats2.add((float) 0);
            this.lineStats2.add((float) 0);
            this.lineStats2.add((float) 0);
            this.lineStats2.add((float) 0);
            this.lineStats2.add((float) 0);
            this.lineStats2.add((float) 0);
            this.lineStats2.add((float) 0);
        }

        int count = 0;
        int emptylines = 0;
        int annotations = 0;
        int slantdir = 0;
        for (final ImageLine l : lines) {
            if (l.isHandwrittenAnnotation()) {
                continue;
            }
            ArrayList<Glyph> lineGlyphs = new ArrayList<Glyph>(l.getGlyphs());
            Collections.sort(lineGlyphs);
            ArrayList<Point2D> points = new ArrayList<Point2D>();
            for (final Glyph g : lineGlyphs) {
                points.add(new Point2D(g.getLayoutX() + g.getWidth() / 2, g
                        .getLayoutY() + g.getHeight() / 2));
            }
            this.lineStats2.set(0,
                    (this.lineStats2.get(0) + (float) l.getWidth()));
            this.lineStats2.set(1,
                    (this.lineStats2.get(1) + (float) l.getHeight()));
            if (!l.isHandwrittenAnnotation()) {
                this.lineStats2.set(2,
                        (this.lineStats2.get(2) + l.getLayoutX()));
                this.lineStats2.set(3, (this.lineStats2.get(3) + (float) (this
                        .getGrayImage().getWidth() - (l.getLayoutX() + l
                        .getWidth()))));
            } else {
                annotations++;
            }
            if (l.getGlyphs().size() == 0) {
                emptylines++;
            }
            this.lineStats2.set(4, (this.lineStats2.get(4) + l.getGlyphs()
                    .size()));
            this.lineStats2.set(5, (this.lineStats2.get(5) + Math.abs(this
                    .calcLineSlant(points))));
            if (this.calcLineSlant(points) > 0) {
                slantdir++;
            } else {
                slantdir--;
            }

            count++;
        }
        this.lineStats2.set(0, (this.lineStats2.get(0) / count));
        this.lineStats2.set(1, (this.lineStats2.get(1) / count));
        this.lineStats2
                .set(2, (this.lineStats2.get(2) / (count - annotations)));
        this.lineStats2
                .set(3, (this.lineStats2.get(3) / (count - annotations)));
        this.lineStats2.set(4, (this.lineStats2.get(4) / (count - emptylines)));
        this.lineStats2.set(5, (this.lineStats2.get(5) / (count - emptylines)));
        this.lineStats2.set(6, (float) slantdir);

        System.out.println("MITTLERE ZEILENBREITE" + ": "
                + this.lineStats2.get(0));
        System.out.println("MITTLERE ZEILENHÖHE" + ": "
                + this.lineStats2.get(1));
        System.out.println("MITTLERER ZEILENABSTAND ZUM RAND LINKS" + ": "
                + this.lineStats2.get(2));
        System.out.println("MITTLERER ZEILENABSTAND ZUM RAND RECHTS" + ": "
                + this.lineStats2.get(3));
        System.out.println("MITTLERE ANZAHL ZEICHEN IN ZEILE" + ": "
                + this.lineStats2.get(4));
        System.out.println("MITTLERE ZEILENNEIGUNG" + ": "
                + this.lineStats2.get(5));
        if (this.lineStats2.get(6) > 0) {
            System.out
                    .println("HÄUFIGSTE ZEILENNEIGUNG: Nach rechts aufsteigend");
        } else if (this.lineStats2.get(6) < 0) {
            System.out
                    .println("HÄUFIGSTE ZEILENNEIGUNG: Nach rechts abfallend");
        } else {
            System.out
                    .println("HÄUFIGSTE ZEILENNEIGUNG: Steigend und fallen ausgeglichen");
        }

    }

    public ArrayList<Float> getLineStats() {
        return lineStats2;
    }

    public ArrayList<Float> getDocumentStats() {
        return documentStats;
    }

    public ArrayList<Float> getWordStats() {
        return wordStats;
    }

    public HashMap<String, ArrayList<Float>> getGlyphStats() {
        return glyphStats;
    }

    /**
     * Updates the position and size of a line and checks for new/obsolete
     * glyphs
     * 
     * @param rectangle
     *            the new position and size (containing the id)
     */
    public boolean updateTextlineSize(final Rectangle rectangle) {
        final ImageLine il = this.lines.get(rectangle.getId());
        il.updateSize(rectangle);
        boolean found = false;

        Iterator<Glyph> it = this.glyphsWithoutLine.values().iterator();

        while (it.hasNext()) {
            final Glyph glyph = it.next();

            if (il.addGlyphIfContained(glyph)) {
                found = true;
                it.remove();
            }
        }

        Iterator<Glyph> it2 = il.getGlyphsMap().values().iterator();
        while (it2.hasNext()) {
            final Glyph glyph = it2.next();

            if (!il.contains(glyph)) {
                found = true;
                glyph.setID(this.glyphCounter);
                this.glyphCounter++;
                this.glyphsWithoutLine.put(glyph.getID(), glyph);
                glyph.setLineID(null);
                it2.remove();
            }
        }

        return found;

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
        DiptychonLogger.debug("Write document image to hdd");
        s.defaultWriteObject();
        if (this.masked != null) {
            s.writeDouble(this.masked.getX());
            s.writeDouble(this.masked.getY());
            s.writeDouble(this.masked.getWidth());
            s.writeDouble(this.masked.getHeight());
        } else {
            for (int i = 0; i < 4; ++i) {
                s.writeDouble(0.d);
            }
        }
    }

    public ArrayList<Glyph> separateGlyph(Glyph glyph) {
        RecursiveGlyphFragmentation rgf = new RecursiveGlyphFragmentation(
                glyph.getBinarizedImage());

        ConnectedObjects co = rgf.getGlyphFragments(2,
                glyph.getWidth() * glyph.getHeight() / 15, true, 20);

        ArrayList<Integer> labels = co.getLabelList();
        int[][] fragmentArray = co.getMatrix();

        ArrayList<Glyph> fragments = new ArrayList<Glyph>();

        for (int i = 0; i < co.getUsedLabelCount(); i++) {
            int minX = co.getLabelMinX(labels.get(i));
            int minY = co.getLabelMinY(labels.get(i));
            int maxX = co.getLabelMaxX(labels.get(i));
            int maxY = co.getLabelMaxY(labels.get(i));
            int width = maxX - minX + 1;
            int height = maxY - minY + 1;

            byte[] glyphArray = new byte[width * height];
            for (int y = minY; y < maxY + 1; y++) {
                for (int x = minX; x < maxX + 1; x++) {
                    if (fragmentArray[y][x] == labels.get(i)) {
                        glyphArray[(y - minY) * width + x - minX] = (byte) 0;
                    } else {
                        glyphArray[(y - minY) * width + x - minX] = (byte) 255;
                    }
                }
            }

            final GrayImage image = new GrayImage(glyphArray, width, height);

            Rectangle mask = new Rectangle();
            mask.setX(glyph.getLayoutX() + minX - 1);
            mask.setY(glyph.getLayoutY() + minY - 1);
            mask.setWidth(width);
            mask.setHeight(height);

            final Glyph fragment = Glyph.extractGlyph(
                    ImageUtils.cropFXImage(this.documentImage, mask),
                    ImageUtils.cropGrayImage(this.getGrayImage(), mask), image,
                    mask);

            fragment.setGroupID("X");
            fragment.setID(glyph.getID() + "s" + i);

            fragments.add(fragment);

        }

        Collections.sort(fragments);
        return fragments;
    }

    public ArrayList<Glyph> separateGlyph(final Glyph glyph,
            final ArrayList<Point2D> sepLine) {
        RecursiveGlyphFragmentation rgf = new RecursiveGlyphFragmentation(
                glyph.getBinarizedImage());

        ConnectedObjects co = rgf.getLineFragments(sepLine);

        ArrayList<Integer> labels = co.getLabelList();
        int[][] fragmentArray = co.getMatrix();

        ArrayList<Glyph> fragments = new ArrayList<Glyph>();

        for (int i = 0; i < co.getUsedLabelCount(); i++) {
            int minX = co.getLabelMinX(labels.get(i));
            int minY = co.getLabelMinY(labels.get(i));
            int maxX = co.getLabelMaxX(labels.get(i));
            int maxY = co.getLabelMaxY(labels.get(i));
            int width = maxX - minX + 1;
            int height = maxY - minY + 1;

            byte[] glyphArray = new byte[width * height];
            for (int y = minY; y < maxY + 1; y++) {
                for (int x = minX; x < maxX + 1; x++) {
                    if (fragmentArray[y][x] == labels.get(i)) {
                        glyphArray[(y - minY) * width + x - minX] = (byte) 0;
                    } else {
                        glyphArray[(y - minY) * width + x - minX] = (byte) 255;
                    }
                }
            }

            final GrayImage image = new GrayImage(glyphArray, width, height);

            Rectangle mask = new Rectangle();
            mask.setX(glyph.getLayoutX() + minX - 1);
            mask.setY(glyph.getLayoutY() + minY - 1);
            mask.setWidth(width);
            mask.setHeight(height);

            final Glyph fragment = Glyph.extractGlyph(
                    ImageUtils.cropFXImage(this.documentImage, mask),
                    ImageUtils.cropGrayImage(this.getGrayImage(), mask), image,
                    mask);

            fragment.setGroupID("X");
            fragment.setID(glyph.getID() + "s" + i);

            fragments.add(fragment);

        }

        Collections.sort(fragments);
        return fragments;
    }

    public ArrayList<Glyph> squareFragGlyph(Glyph glyph, int squareSize) {
        RecursiveGlyphFragmentation rgf = new RecursiveGlyphFragmentation(
                glyph.getBinarizedImage());

        ConnectedObjects co = rgf.getSquareFragments(squareSize);

        ArrayList<Integer> labels = co.getLabelList();
        int[][] fragmentArray = co.getMatrix();

        ArrayList<Glyph> fragments = new ArrayList<Glyph>();

        for (int i = 0; i < co.getUsedLabelCount(); i++) {
            int minX = co.getLabelMinX(labels.get(i));
            int minY = co.getLabelMinY(labels.get(i));
            int maxX = co.getLabelMaxX(labels.get(i));
            int maxY = co.getLabelMaxY(labels.get(i));
            int width = maxX - minX + 1;
            int height = maxY - minY + 1;

            byte[] glyphArray = new byte[width * height];
            for (int y = minY; y < maxY + 1; y++) {
                for (int x = minX; x < maxX + 1; x++) {
                    if (fragmentArray[y][x] == labels.get(i)) {
                        glyphArray[(y - minY) * width + x - minX] = (byte) 0;
                    } else {
                        glyphArray[(y - minY) * width + x - minX] = (byte) 255;
                    }
                }
            }

            final GrayImage image = new GrayImage(glyphArray, width, height);

            Rectangle mask = new Rectangle();
            mask.setX(glyph.getLayoutX() + minX - 1);
            mask.setY(glyph.getLayoutY() + minY - 1);
            mask.setWidth(width);
            mask.setHeight(height);

            final Glyph fragment = Glyph.extractGlyph(
                    ImageUtils.cropFXImage(this.documentImage, mask),
                    ImageUtils.cropGrayImage(this.getGrayImage(), mask), image,
                    mask);

            fragment.setGroupID("X");
            fragment.setID("F_");
            fragment.isSquareFragment = true;

            /*
             * if (fragment.getWidth()*fragment.getHeight() < 3) {
             * System.out.println("width: " + fragment.getWidth());
             * System.out.println("height: " + fragment.getHeight()); }
             */
            fragments.add(fragment);

            /*
             * System.out.println("");
             * System.out.println("SINGLE FRAGMENT ARRAY:");
             * System.out.println("");
             * 
             * for (int y = 0; y < height; ++y) { final int offset = y * width;
             * System.out.println(); for (int x = 0; x < width; ++x) { final int
             * index = offset + x; System.out.print(glyphArray[index]); } }
             */

        }
        /*
         * System.out.println(""); System.out.println("FRAGMENT ARRAY:");
         * System.out.println(""); for (int y = 0; y < fragmentArray.length;
         * y++) { System.out.println(""); for (int x = 0; x <
         * fragmentArray[y].length; x++) { if (fragmentArray[y][x] < 10) {
         * System.out.print("00"); } else if (fragmentArray[y][x] < 100) {
         * System.out.print("0"); } System.out.print(fragmentArray[y][x]); } }
         */
        Collections.sort(fragments);
        return fragments;
    }
}
