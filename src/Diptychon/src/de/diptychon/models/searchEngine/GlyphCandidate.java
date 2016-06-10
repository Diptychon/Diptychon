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
package de.diptychon.models.searchEngine;

import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import de.diptychon.models.algorithms.Template;
import de.diptychon.models.data.Glyph;
import de.diptychon.models.glyphGeometry.BinaryImage;
import de.diptychon.models.glyphGeometry.Region;
import de.diptychon.models.glyphGeometry.RegionConfiguration;
import de.diptychon.models.misc.GrayImage;
import de.diptychon.models.misc.ImageUtils;
import de.diptychon.ui.views.dialogs.SearchEngineDialog;

/**
 * During the search process glyph candidates are found, remembered by this
 * class and later instantiated by the Diptychon class Glyph.
 */
public class GlyphCandidate {
    protected final Image origCropped;
    protected final GrayImage grayCropped;
    protected final BinaryImage binCropped;
    protected final Rectangle mask;
    protected final Glyph queryGlyph;
    public int commonWithGlyph;
    public int differentToGlyph;
    private final int x;
    private final int y;
    private double foundWithThisCorrelationCoefficient;
    private final RegionConfiguration rc;

    private static int instancecounter = 10000;

    /**
     * Constructs a candidate glphy at position x, y that matches the queryGlyph
     * in document.
     */
    public GlyphCandidate(int x, int y, Glyph queryGlyph,
            SearchableDocument document, double cc) {
        this.x = x;
        this.y = y;
        this.foundWithThisCorrelationCoefficient = cc;

        this.queryGlyph = queryGlyph;
        Template queryGlyphImage = new Template(
                queryGlyph.getGrayImagePixels(), queryGlyph.getWidth(),
                queryGlyph.getHeight());
        int cropWidth = (int) queryGlyphImage.dimension().getWidth();
        int cropHeight = (int) queryGlyphImage.dimension().getHeight();

        int imgWidth = document.grayscale.getWidth();
        int imgHeight = document.grayscale.getHeight();

        byte[] pixels = ImageUtils.cropImage(document.binaryValues, imgWidth,
                imgHeight, x, y, cropWidth, cropHeight);
        mask = new Rectangle(0, 0, cropWidth, cropHeight);
        try {
            pixels = ImageUtils.autoCrop(pixels, mask);
        } catch (final IllegalArgumentException e) {
            ; // for an empty image region, which means that there is no glyph
        }

        byte[] gray = ImageUtils.cropImage(document.grayValues, imgWidth,
                imgHeight, mask);

        grayCropped = new GrayImage(gray, (int) mask.getWidth(),
                (int) mask.getHeight());
        binCropped = new BinaryImage(pixels, (int) mask.getWidth(),
                (int) mask.getHeight());
        origCropped = ImageUtils.cropFXImage(document.original, new Rectangle(x
                + mask.getX(), y + mask.getY(), (int) mask.getWidth(),
                (int) mask.getHeight()));

        // cut out new glyph precisely according to the query glyph
        binCropped.labelConnectedComponents();
        binCropped.confineToLargestRegions(queryGlyph.getNumOfComponents());
        // binCropped.whitenBorderColumns(5);
        binCropped.keepForegroundPixelsWhichMatch(queryGlyph
                .getBinarizedImage());
        // binCropped.removeRegionsWhichAreLessThan(20);
        // binCropped.closing();
        int[] match = binCropped.matches(queryGlyph.getBinarizedImage());
        commonWithGlyph = match[0];
        differentToGlyph = match[1];
        rc = binCropped.getRegionConfiguration();
    }

    public double similarity() {
        if (differentToGlyph == 0) {
            return Double.POSITIVE_INFINITY;
        } else {
            return commonWithGlyph * 1.0 / differentToGlyph;
        }
    }

    public RegionConfiguration getRC() {
        return rc;
    }

    protected void saveOnHDD(String s) {
        ImageUtils.writeGrayscaleImage("CC\\CC" + instancecounter + s + ".png",
                binCropped);
        ImageUtils.writeGrayscaleImage("CC\\CC" + instancecounter + " Query "
                + s + ".png", queryGlyph.getBinarizedImage());
        instancecounter++;
    }

    public double correlationCoefficient() {
        return foundWithThisCorrelationCoefficient;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * This candidate is instantiated as a new glyph.
     */
    protected Glyph instantiateAsGlyph(SearchableLine line) {
        BinaryImage tmp = new BinaryImage(queryGlyph.getBinarizedImagePixels(),
                queryGlyph.getWidth(), queryGlyph.getHeight());
        tmp.labelConnectedComponents();
        RegionConfiguration queryRC = tmp.getRegionConfiguration();

        System.out.println();
        System.out.println((instancecounter++) + ") Query-Zeichen = "
                + queryGlyph.getGroupID());
        System.out.printf("Matched mit Korr.-Koeff. = %.2f\n",
                foundWithThisCorrelationCoefficient);

        float sizeTolerance = SearchEngineDialog._G_glyphSize / 100;
        System.out
                .println("Größe Kandidat = "
                        + rc.getArea()
                        + " und Query = "
                        + queryGlyph.getSize()
                        + " Toleranzbereich: ["
                        + (queryGlyph.getSize() - (sizeTolerance * queryGlyph
                                .getSize()))
                        + "; "
                        + (queryGlyph.getSize() + (sizeTolerance * queryGlyph
                                .getSize())) + "]");

        System.out.println("Is tall Kandidat = " + line.isTall(rc.getHeight())
                + " Query = " + line.isTall(queryRC.getHeight()));
        System.out.println("Top similar bottom = "
                + rc.topSimilarBottomPart(queryRC));

        System.out.print("Instantiate a glyph: comm. points = "
                + commonWithGlyph);
        System.out.print(" diff. points = " + differentToGlyph);
        System.out.println(" i.e. coverage is "
                + (commonWithGlyph > 2.0 * differentToGlyph));

        double total = commonWithGlyph + differentToGlyph;
        String c = new java.text.DecimalFormat("#")
                .format(100 * (commonWithGlyph / total));
        String d = new java.text.DecimalFormat("#")
                .format(100 * (differentToGlyph / total));

        // saveOnHDD(" "+c+" "+d+" ");

        // Kontur extrahiert durch die Klasse Contour
        int regionNr = 0;
        for (Region r : rc.getAll()) {
            r.saveContour(regionNr++);
        }

        // Kontur extrahiert in BinaryImage
        // GrayImage saveContour = binCropped.extractContourPoints();
        // ImageUtils.writeGrayscaleImage("CC\\compactness"+
        // (instanceCounter++)+".png", saveContour);

        Glyph newGlyph = Glyph.extractGlyph(origCropped, grayCropped,
                binCropped, new Rectangle(x + mask.getX(), y + mask.getY(),
                        (int) mask.getWidth(), (int) mask.getHeight()));
        newGlyph.setGroupID(queryGlyph.getGroupID());
        return newGlyph;
    }
}