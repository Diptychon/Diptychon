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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import de.diptychon.DiptychonLogger;
//import de.diptychon.models.algorithms.validation.PolygonComparison;
import de.diptychon.models.data.Glyph;
import de.diptychon.models.data.ImageLine;
import de.diptychon.models.glyphGeometry.BinaryImage;
import de.diptychon.models.misc.GrayImage;
import de.diptychon.models.misc.ImageUtils;
import de.diptychon.ui.views.panels.DocumentPanel;

/**
 * This class implements the template matching according to equation 17.10 of
 * Digitale Bildverarbeitung - Eine Einf&uuml;hrung mit ImageJ by Burger and
 * Burge 2nd edition
 */
public class TemplateMatching {

    int TEMP_NUMBER_OF_ACCEPTED_GLYPHS = 0;

    /**
     * Represents the lowest threshold allowed for template matching ({@value}
     * ). A threshold lower than this, does not make any sense, since it would
     * lead to an enormous amount of possible matches
     */
    private static final int TEMPLATE_MATCHING_THRESHOLD = 25;

    /**
     * Represents the maximum threshold allowed for template matching ({@value}
     * ).
     */
    private static final int TEMPLATE_MATCHING_THRESHOLD_MAX = 255;

    /**
     * The threshold ({@value} ) which is used to filter out possible matches by
     * width. In contrast the threshold for the height is determined by the
     * standard deviation
     */
    private static final double SIZE_THRESHOLD_WIDTH = 0.15d;

    /**
     * The threshold ({@value} ) which is used to do a first filtering by
     * height.
     */
    private static final double SIZE_THRESHOLD_HEIGHT = 0.60d;// 0.20d;

    /**
     * The validation approach which should be used. It might be one of
     * <ul>
     * <li>{@link de.diptychon.models.algorithms.validation.SkeletonComparison
     * SkeletonComparison}</li>
     * <li>{@link de.diptychon.models.algorithms.validation.PolylineComparison
     * PolylineComparison}</li>
     * <li>{@link de.diptychon.models.algorithms.validation.PolygonComparison
     * PolygonComparison}</li>
     * <li>{@link de.diptychon.models.algorithms.validation.Moments Moments}</li>
     * <li><code>null</code></li>
     * </ul>
     * <code>null</code> should be chosen, if no additional validation (except
     * size restriction) of the template matching result should be done.
     * <p>
     * To use a validation approach use
     * <p>
     * {@code A_ValidationApproach.Factory FACTORY = new <validation_approach>.Factory();}
     */
    // private static A_ValidationApproach.Factory FACTORY = new
    // PolygonComparison.Factory(); // = null;
    private static A_ValidationApproach.Factory FACTORY = null;

    /**
     * The height of the input image (not the template)
     */
    private final int imageHeight;

    /**
     * The width of the input image (not the template)
     */
    private final int imageWidth;

    /**
     * The original image
     */
    private final Image original;

    /**
     * Binary image of the original image
     */
    private final GrayImage binaryImage;

    /**
     * The values of the input image (not the template)
     */
    private final byte[] imagePixels;

    /**
     * The binary values of the input image (not the template)
     */
    private final byte[] imagePixelsBinary;

    /**
     * The template used for matching
     */
    private final Template template;
    private final int tWidth;
    private final int tHeight;

    /**
     * The template represented as a glyph
     */
    private final Glyph glyph;

    /**
     * The lines the template matching should be applied at.
     */
    private final ArrayList<ImageLine> lines;

    /**
     * The constructor which initializes all members a creates a new template
     * 
     * @param g
     *            The glyph which will be used as template for matching
     * @param pOriginal
     *            The original image
     * @param grayscale
     *            The grayscale image, which is used for template matching
     * @param binarized
     *            The binarized image, which is used at the end to evaluate the
     *            possible matches
     * @param pLines
     *            The lines the template matching should be applied at.
     */
    public TemplateMatching(final Glyph g, final Image pOriginal,
            final GrayImage grayscale, final GrayImage binarized,
            final ArrayList<ImageLine> pLines) {
        this.glyph = g;
        this.template = new Template(this.glyph.getGrayImagePixels(),
                this.glyph.getWidth(), this.glyph.getHeight());
        this.tWidth = this.glyph.getWidth();
        this.tHeight = this.glyph.getHeight();
        this.original = pOriginal;
        this.binaryImage = binarized;
        this.imagePixelsBinary = binarized.getPixels();
        this.imagePixels = grayscale.getPixels();
        this.imageWidth = grayscale.getWidth();
        this.imageHeight = grayscale.getHeight();
        Collections.sort(pLines);
        this.lines = pLines;

    }

    /**
     * This method organizes the template matching according to equation 17.10
     * of Digitale Bildverarbeitung - Eine Einfuehrung mit ImageJ by Burger and
     * Burge. To speed up template matching multithreading is used. For each
     * available processor a new thread is started, that is, why we have to use
     * an organizing method. This method should be used when one wants to apply
     * the template matching to the whole image.
     * 
     * @return An array of lists of accepted matches sorted according to the
     *         correlation coefficient
     */
    public ArrayList<Glyph>[] matchWholeImage() {
        final int availableProcessors = Runtime.getRuntime()
                .availableProcessors();
        DiptychonLogger.trace("Using {} threads for template matching...",
                availableProcessors);
        final Thread[] threads = new Thread[availableProcessors];

        final int stepSize = this.imageHeight / availableProcessors;
        // the last thread might have to match more lines than the other
        // threads.
        final int offsetLast = this.imageHeight % availableProcessors;

        final ArrayList<double[]> threadResults = new ArrayList<double[]>(
                availableProcessors);
        for (int t = 0; t < availableProcessors - 1; ++t) {
            final int ti = t;
            final Thread thread = new Thread(new Runnable() {
                @Override
                public final void run() {
                    /*
                     * the number of the thread has to be added to the results
                     * (ti) to be able to sort them, to correspond to the
                     * original image
                     */
                    threadResults.add(TemplateMatching.this.matchTemplate(ti,
                            new Point(0, ti * stepSize), new Point(
                                    TemplateMatching.this.imageWidth, (ti + 1)
                                            * stepSize - 1)));
                    DiptychonLogger.trace("Thread {} done!", ti);
                }
            }, "TemplateMatching-" + t);
            thread.setPriority(Thread.currentThread().getPriority());
            thread.start();
            threads[ti] = thread;
        }

        threadResults.add(this.matchTemplate(availableProcessors - 1,
                new Point(0, (availableProcessors - 1) * stepSize), new Point(
                        this.imageWidth, availableProcessors
                                * (stepSize + offsetLast))));
        DiptychonLogger.info("Thread {} done!", (availableProcessors - 1));

        /*
         * wait for all threads to terminate and collect results
         */
        for (final Thread thread : threads) {
            if (thread != null) {
                try {
                    thread.join();
                } catch (final InterruptedException e) {
                    DiptychonLogger.error("{}", e);
                }
            }
        }

        final Dimension templateDimension = this.template.dimension();

        final double[] result = new double[this.imageWidth * this.imageHeight];
        // initially fills the array with -1, because the smallest coefficient
        // is -1
        Arrays.fill(result, -1);
        int previousLength = ((templateDimension.height >> 1) * this.imageWidth)
                + (templateDimension.width >> 1);

        if (availableProcessors == 1) {
            final double[] tmp = threadResults.get(0);
            System.arraycopy(tmp, 1, result, previousLength, tmp.length - 1);
        } else {
            /*
             * if the number of threads were > 1, the results has to be sorted,
             * to correspond to the original image
             */
            for (int i = 0; i < availableProcessors; ++i) {
                for (final double[] values : threadResults) {
                    if (i == values[0]) {
                        System.arraycopy(values, 1, result, previousLength,
                                values.length - 1);
                        previousLength += values.length - 1;
                        break;
                    }
                }
            }
        }

        final ArrayList<Glyph>[] extracted = this.checkAlreadyExtracted(this
                .validateTemplateMatching(this.extractGlyphs(result, false)));

        final String groupID = this.glyph.getGroupID();
        for (final ArrayList<Glyph> glyphs : extracted) {
            for (final Glyph g : glyphs) {
                g.setGroupID(groupID);
            }
        }
        return extracted;
    }

    /**
     * This method organizes the template matching according to equation 17.10
     * of Digitale Bildverarbeitung - Eine Einfuehrung mit ImageJ by Burger and
     * Burge. To speed up template matching multithreading is used. For each
     * available processor a new thread is started, that is, why we have to use
     * an organizing method. This method should be used when one wants to apply
     * the template matching to some specified lines only.
     * 
     * @return An array of lists of accepted matches sorted according to the
     *         correlation coefficient
     */
    public ArrayList<Glyph>[] matchInImageLines() {

        final int availableProcessors = Runtime.getRuntime()
                .availableProcessors();
        DiptychonLogger.trace("Using {} threads for template matching...",
                availableProcessors);
        final Thread[] threads = new Thread[availableProcessors];

        final int stepSize = this.lines.size() / availableProcessors;
        final int offsetLines = this.lines.size() % availableProcessors;
        final ArrayList<double[]> threadResults = new ArrayList<double[]>(
                availableProcessors);

        for (int t = 0; t < availableProcessors - 1; ++t) {
            final int ti = t;
            final Thread thread = new Thread(new Runnable() {
                @Override
                public final void run() {
                    final int upperBound = (ti + 1) * stepSize;
                    for (int i = ti * stepSize; i < upperBound; ++i) {
                        threadResults.add(TemplateMatching.this.matchTemplate(
                                i, TemplateMatching.this.lines.get(i)
                                        .getUpperLeftPoint(),
                                TemplateMatching.this.lines.get(i)
                                        .getLowerRightPoint()));
                    }
                    DiptychonLogger.trace("Thread {} done!", ti);
                }
            }, "TemplateMatching-" + t);
            thread.setPriority(Thread.currentThread().getPriority());
            thread.start();
            threads[ti] = thread;
        }

        final int upperBound = availableProcessors * stepSize + offsetLines;
        for (int i = (availableProcessors - 1) * stepSize; i < upperBound; ++i) {
            threadResults.add(TemplateMatching.this.matchTemplate(i, this.lines
                    .get(i).getUpperLeftPoint(), this.lines.get(i)
                    .getLowerRightPoint()));
        }

        DiptychonLogger.info("Thread {} done!", (availableProcessors - 1));

        /*
         * wait for all threads to terminate and collect results
         */
        for (final Thread thread : threads) {
            if (thread != null) {
                try {
                    thread.join();
                } catch (final InterruptedException e) {
                    DiptychonLogger.error("{}", e);
                }
            }
        }

        final Dimension templateDimension = this.template.dimension();

        final double[] result = new double[this.imageWidth * this.imageHeight];
        Arrays.fill(result, -1);

        // int previousLength = lines.get(0).getLayoutY() * imageWidth +
        // lines.get(0).getLayoutX();
        final int previousLength = ((templateDimension.height >> 1) * this.imageWidth)
                + (templateDimension.width >> 1);

        if (availableProcessors == 1) {
            final double[] tmp = threadResults.get(0);
            System.arraycopy(tmp, 1, result, previousLength
                    + this.lines.get(0).getLayoutY() * this.imageWidth
                    + this.lines.get(0).getLayoutX(), tmp.length - 1);
        } else {
            /*
             * if the number of threads were > 1, the results has to be sorted,
             * to correspond to the original image
             */
            for (int i = 0; i < threadResults.size(); ++i) {
                for (final double[] values : threadResults) {
                    if (i == values[0]) {
                        System.arraycopy(values, 1, result, previousLength
                                + this.lines.get(i).getLayoutY()
                                * this.imageWidth
                                + this.lines.get(i).getLayoutX(),
                                values.length - 1);
                        break;
                    }
                }
            }
        }

        final ArrayList<Glyph>[] extracted = this.checkAlreadyExtracted(this
                .validateTemplateMatching(this.extractGlyphs(result, false)));

        final String groupID = this.glyph.getGroupID();
        for (final ArrayList<Glyph> glyphs : extracted) {
            for (final Glyph g : glyphs) {
                g.setGroupID(groupID);
            }
        }

        return extracted;
    }

    /**
     * This method should be used when one wants to apply the template matching
     * to one specified line only.
     * 
     * @return An array of lists of accepted matches sorted according to the
     *         correlation coefficient
     */
    public ArrayList<Glyph>[] matchInSingleLine(final String lineID) {
        double[] tmp = null;
        int lineIterator = 0;
        for (int i = 0; i < this.lines.size(); ++i) {
            if (TemplateMatching.this.lines.get(i).getID().equals(lineID)) {
                int[][] topsBottomsOfTextLine = TemplateMatching.this.lines
                        .get(i).verticalMinsAndMaxes(this.imagePixelsBinary,
                                this.imageWidth, this.imageHeight);
                lineIterator = i;
                tmp = (TemplateMatching.this.matchTemplateAlongTextLine(i,
                        this.lines.get(i).getUpperLeftPoint(), this.lines
                                .get(i).getLowerRightPoint(),
                        topsBottomsOfTextLine));
                // tmp = (TemplateMatching.this.matchTemplate(i,
                // this.lines.get(i).getUpperLeftPoint(),
                // this.lines.get(i).getLowerRightPoint()));
            }
        }

        final Dimension templateDim = this.template.dimension();

        final double[] result = new double[this.imageWidth * this.imageHeight];
        Arrays.fill(result, -1);

        // int previousLength = lines.get(0).getLayoutY() * imageWidth +
        // lines.get(0).getLayoutX();
        final int previousLength = ((templateDim.height >> 1) * this.imageWidth)
                + (templateDim.width >> 1);
        System.arraycopy(tmp, 1, result,
                previousLength + this.lines.get(lineIterator).getLayoutY()
                        * this.imageWidth
                        + this.lines.get(lineIterator).getLayoutX(),
                tmp.length - 1);
        /*
         * <ul> <li>{@link
         * de.diptychon.models.algorithms.validation.SkeletonComparison
         * SkeletonComparison}</li> <li>{@link
         * de.diptychon.models.algorithms.validation.PolylineComparison
         * PolylineComparison}</li> <li>{@link
         * de.diptychon.models.algorithms.validation.PolygonComparison
         * PolygonComparison}</li> <li>{@link
         * de.diptychon.models.algorithms.validation.Moments Moments}</li>
         */
        // this.FACTORY = new
        // de.diptychon.models.algorithms.validation.PolygonComparison.Factory();

        // final ArrayList<Glyph>[] extracted =
        // this.checkAlreadyExtracted(this.validateTemplateMatching(this.extractGlyphs(result,
        // true)));
        final ArrayList<Glyph>[] extracted = this.extractGlyphs(result, true);

        final String groupID = this.glyph.getGroupID();
        for (final ArrayList<Glyph> glyphs : extracted) {
            for (final Glyph g : glyphs) {
                g.setGroupID(groupID); // Zeichenklasse der extrahierten Glyph
                                       // zuordnen
            }
        }
        System.out.println("TEMP_NUMBER_OF_ACCEPTED_GLYPHS "
                + TEMP_NUMBER_OF_ACCEPTED_GLYPHS);
        return extracted;
    }

    /**
     * Performs the template matching from a starting point <code>startAt</code>
     * to an end point <code>endAt</code>, but only for connected components
     * within that text line, which have approximately the same height like the
     * template.
     * 
     * @param threadNumber
     *            the number of the thread which is running this method
     * @param startAt
     *            the starting point for template matching (upper-left of
     *            text-line)
     * @param endAt
     *            the end point for template matching (bottom-right of
     *            text-line)
     * @param topsBottomsOfTextLine
     *            coordinates of min and max vertical positions for each column
     *            in that text line
     * 
     * @return an array where the first entry is the number of the corresponding
     *         thread and the rest of the entries are the correlation
     *         coefficients
     */
    private double[] matchTemplateAlongTextLine(final int threadNumber,
            final Point2D startAt, final Point2D endAt,
            int[][] topsBottomsOfTextLine) {
        final Dimension templateDim = this.template.dimension();
        final int yMax = (int) Math.min(this.imageHeight - templateDim.height,
                Math.max(startAt.getY(), endAt.getY()));
        final double[] corrCoeffs = new double[1 + ((int) (this.imageWidth * (yMax
                - Math.min(startAt.getY(), endAt.getY()) + 1)))];

        Arrays.fill(corrCoeffs, -1);
        corrCoeffs[0] = threadNumber;

        for (int x = (int) startAt.getX(); x < (int) endAt.getX(); x = x + 1) {
            // along the whole text line from left to right
            // determine height of the script within the text line (along the
            // width of the template)
            int heightOfScript = 0;
            int heightDiff = 0;
            int yTop = Integer.MAX_VALUE; // upper most coordinate of the script
                                          // in the text line
            int yBot = 0; // lower most coordinate of the script in the text
                          // line

            for (int xT = x; xT <= x + templateDim.width
                    && xT < (int) endAt.getX(); xT++) {
                if (topsBottomsOfTextLine[xT][0] != ImageLine.EMPTY_SPACE) {
                    heightDiff = topsBottomsOfTextLine[xT][1]
                            - topsBottomsOfTextLine[xT][0];
                    if (heightDiff > heightOfScript)
                        heightOfScript = heightDiff;
                    if (topsBottomsOfTextLine[xT][0] < yTop)
                        yTop = topsBottomsOfTextLine[xT][0];
                    if (topsBottomsOfTextLine[xT][1] > yBot)
                        yBot = topsBottomsOfTextLine[xT][1];
                }
            }

            // System.out.println("\n\n\n Height of Script: "+ heightOfScript);

            // determine subimage of the width of the template and the height of
            // the textline (just for test)
            /*
             * final int textLineTop = (int)startAt.getY(); final int
             * textLineBot = (int)endAt.getY(); final int textLineHeight =
             * Math.abs(textLineTop-textLineBot); final byte[] textLineBinaer =
             * ImageUtils.cropImage(this.imagePixelsBinary, this.imageWidth,
             * this.imageHeight, x, textLineTop, templateDim.width,
             * textLineHeight); //BinaryImage binImage = new
             * BinaryImage(croppedPartBinaer, templateDim.width,
             * templateDim.height); final GrayImage imageToHDD = new
             * GrayImage(textLineBinaer, templateDim.width, textLineHeight);
             * ImageUtils .writeGrayscaleImage("CC\\Zeile-"
             * +templateDim.width+" x "+templateDim.height+" "+x+".png",
             * imageToHDD);
             */

            for (int y = (int) startAt.getY(); y <= yMax; ++y) {
                // height of script approximately equal to height of template?
                // (+/- 10 pixels)
                // if (heightOfScript >= templateDim.height - 10 &&
                // heightOfScript <= templateDim.height + 10
                // )
                // {

                if (y >= yTop - 5
                        && y <= yTop + 5
                        && // template starts approx. at top of regions in the
                           // image
                        y + templateDim.height >= yBot - 5
                        && y + templateDim.height <= yBot + 5) // and stops at
                                                               // bottom
                {

                    // subimages which are tested, are saved in the following
                    // file with diagonal of template

                    /*
                     * GrayImage g = new GrayImage(binaryImage.getPixelCloned(),
                     * binaryImage.getWidth(), binaryImage.getHeight()); for
                     * (int xi = x; xi <= x+templateDim.width; xi++){
                     * g.setToGrey(xi, yTop, 200); g.setToGrey(xi, yBot, 63); }
                     * int x1=x; int y1=y; while ( x1++<x+templateDim.width )
                     * g.setToGrey(x1, y1++, 0);
                     * 
                     * final byte[] g_cropped =
                     * ImageUtils.cropImage(g.getPixels(), this.imageWidth,
                     * this.imageHeight, x, y, templateDim.width,
                     * templateDim.height); BinaryImage g_asBinIm = new
                     * BinaryImage(g_cropped, templateDim.width,
                     * templateDim.height); ImageUtils.writeGrayscaleImage(
                     * "CC\\x="+ x +"-y="+ y +".png", g_asBinIm);
                     */

                    final int offset = (int) ((y - startAt.getY()) * this.imageWidth);
                    final int index = 1 + (int) ((x - startAt.getX()) + offset);
                    double value = this.matchTemplateAt(x, y);

                    // determine subimage
                    final byte[] croppedPart = ImageUtils.cropImage(
                            this.imagePixelsBinary, this.imageWidth,
                            this.imageHeight, x, y, templateDim.width,
                            templateDim.height);
                    BinaryImage binImage = new BinaryImage(croppedPart,
                            templateDim.width, templateDim.height);
                    binImage.labelConnectedComponents();
                    int sizeOfCandidate = binImage.getRegionConfiguration()
                            .getArea();

                    if (value > 0.7 && sizeOfCandidate > glyph.getSize() - 20
                            && sizeOfCandidate < glyph.getSize() + 20
                            && glyph.getSize() > 100) {

                        // verify whether the number of holes are equal between
                        // template and new subimage
                        // binImage.closing();
                        // binImage.labelConnectedComponents();
                        // if ( binImage.getNumOfHoles() ==
                        // template.getNumOfHoles() )
                        // if ( binImage.matches(glyph.getBinarizedImage()) )
                        // {
                        corrCoeffs[index] = value;
                        value = Math.round(100.0 * value) / 100.0; // Korrelationskoeffizienten
                                                                   // auf zwei
                                                                   // Nachkommastellen
                                                                   // runden

                        // Template speichern
                        // final byte[] templatePixels =
                        // glyph.getBinarizedImagePixels(); //
                        // this.template.pixels();
                        // final GrayImage templateToHDD = new
                        // GrayImage(templatePixels, templateDim.width,
                        // templateDim.height);
                        // ImageUtils.writeGrayscaleImage("CC\\CC "+value+" Koord"+x+"-"+y+" Template "+glyph.getSize()+" .png",
                        // templateToHDD);

                        // Bild-crop speichern
                        // final byte[] croppedPart2 =
                        // ImageUtils.cropImage(this.imagePixels,
                        // this.imageWidth, this.imageHeight, x, y,
                        // templateDim.width, templateDim.height);
                        // final GrayImage imageToHDD = new
                        // GrayImage(croppedPart2, templateDim.width,
                        // templateDim.height);
                        // ImageUtils.writeGrayscaleImage("CC\\CC "+value+" Koord"+x+"-"+y+" Bildausschnitt.png",
                        // imageToHDD);

                        // Binärbildausschnitt speichern
                        // final byte[] croppedPartBinaer =
                        // ImageUtils.cropImage(this.imagePixelsBinary,
                        // this.imageWidth, this.imageHeight, x, y,
                        // templateDim.width, templateDim.height);
                        // final GrayImage imageBinaerToHDD = new
                        // GrayImage(croppedPartBinaer, templateDim.width,
                        // templateDim.height);
                        // ImageUtils.writeGrayscaleImage("CC\\CC "+value+" Koord"+x+"-"+y+" BildausschnittBinaer "+sizeOfCandidate+" .png",
                        // imageBinaerToHDD);

                    } // if (numOfHoles...
                      // }
                } // if (yTop...
                  // } // if (heightOfScript ...
            } // for (int y...
        }
        return corrCoeffs;
    }

    /**
     * Performs the template matching from a starting point <code>startAt</code>
     * to an end point <code>endAt</code>
     * 
     * @param threadNumber
     *            the number of the thread which is running this method
     * @param startAt
     *            the starting point for template matching
     * @param endAt
     *            the end point for template matching
     * @return an array where the first entry is the number of the corresponding
     *         thread and the rest of the entries are the correlation
     *         coefficients
     */
    private double[] matchTemplate(final int threadNumber,
            final Point2D startAt, final Point2D endAt) {
        final Dimension templateDimension = this.template.dimension();

        final int xMax = (int) Math.min(this.imageWidth
                - templateDimension.width, endAt.getX());
        final int yMax = (int) Math.min(this.imageHeight
                - templateDimension.height,
                Math.max(startAt.getY(), endAt.getY()));

        final double[] corrCoeffValues = new double[1 + ((int) (this.imageWidth * (yMax
                - Math.min(startAt.getY(), endAt.getY()) + 1)))];
        Arrays.fill(corrCoeffValues, -1);
        corrCoeffValues[0] = threadNumber;

        for (int y = (int) startAt.getY(); y <= yMax; ++y) {
            final int offset = (int) ((y - startAt.getY()) * this.imageWidth);
            for (int x = (int) startAt.getX(); x <= xMax; ++x) {
                final int index = 1 + (int) ((x - startAt.getX()) + offset);
                double value = this.matchTemplateAt(x, y);
                corrCoeffValues[index] = value;
            }
        }
        return corrCoeffValues;
    }

    /**
     * This method calculates the correlation coefficient for a template at
     * position <code>(x, y)</code>
     * 
     * @param x
     *            the x coordinate to match the template at
     * @param y
     *            the x coordinate to match the template at
     * @return the correlation coefficient at position <code>(x, y)</code>
     */
    private double matchTemplateAt(final int x, final int y) {
        final Dimension templateDimension = this.template.dimension();
        final byte[] templatePixels = this.template.pixels();
        int sumI = 0;
        int sumI2 = 0;
        int covIR = 0;
        for (int yt = 0; yt < templateDimension.height; ++yt) {
            final int offsetImage = (yt + y) * this.imageWidth;
            final int offsetTemplate = yt * templateDimension.width;
            for (int xt = 0; xt < templateDimension.width; ++xt) {
                final int indexImage = xt + x + offsetImage;
                final int indexTemplate = xt + offsetTemplate;
                final int valImage = this.imagePixels[indexImage] & 0xff;
                final int valTemplate = templatePixels[indexTemplate] & 0xff;
                sumI += valImage;
                sumI2 += valImage * valImage;
                covIR += valImage * valTemplate;
            }
        }
        final double meanI = sumI / (double) this.template.size();
        final double val0 = this.template.size() * meanI * this.template.mean();
        final double val1 = covIR - val0;
        final double val2 = Math.sqrt(sumI2 - this.template.size() * meanI
                * meanI)
                * this.template.sigma();
        return val1 / val2;
    }

    /**
     * Within this method all glyphs are extracted and a first filtering by size
     * is performed, i.e. sorted out by size
     * 
     * @param result
     *            the correlation coefficients, at this moment within the range
     *            [-1..1], getting normalised to [0..255] within this method
     * @return an array of lists with the accepted glyphs sorted by threshold
     */
    private ArrayList<Glyph>[] extractGlyphs(final double[] result,
            boolean fullMax) {
        int[] correlationCoefficients = null;
        if (fullMax) {
            // correlationCoefficients =
            // this.thresholdAndNeighbourhood(this.normalizeCorrelationCoeff(result,
            // true));
            correlationCoefficients = this.normalizeCorrelationCoeff(result,
                    true);
        } else {
            correlationCoefficients = this.thresholdAndNeighbourhood(this
                    .normalizeCorrelationCoeff(result, false));
        }
        final Dimension templateDimension = this.template.dimension();
        /*
         * Establish a new array to store size checked glyphs
         */
        @SuppressWarnings("unchecked")
        ArrayList<Glyph>[] coefficientsAndGlyphs = new ArrayList[TEMPLATE_MATCHING_THRESHOLD_MAX + 1];

        for (int i = 0; i <= TEMPLATE_MATCHING_THRESHOLD_MAX; ++i) {
            coefficientsAndGlyphs[i] = new ArrayList<>();
        }

        int counterAll = 0;
        int counterRejected = 0;
        int index = 0;
        for (final int value : correlationCoefficients) {
            if (value >= TEMPLATE_MATCHING_THRESHOLD) {
                ++counterAll;
                final int x = index % this.imageWidth;
                final int y = index / this.imageWidth;
                int posX = x - (templateDimension.width >> 1);
                final int height_2 = templateDimension.height >> 1;
                final double factor = 1.25;
                final int radius = (int) Math.ceil(height_2 * factor);

                int posY = y - radius;
                if (posY < 0)
                    posY = 0;
                int cropHeight = 2 * radius;
                if (posY + cropHeight > this.imageHeight)
                    cropHeight = cropHeight
                            - (posY + cropHeight - this.imageHeight);

                byte[] pixels = ImageUtils.cropImage(this.imagePixelsBinary,
                        this.imageWidth, this.imageHeight, posX, posY,
                        templateDimension.width, cropHeight);
                final Rectangle mask = new Rectangle(0, 0,
                        templateDimension.width, cropHeight);
                try {
                    pixels = ImageUtils.autoCrop(pixels, mask);
                } catch (final IllegalArgumentException e) {
                    ++index; // we do not need to handle this exception, since
                             // it is just an indicator
                    continue; // for an empty image region, which means that
                              // there is no glyph
                }
                /*
                 * first size restriction according to width
                 */
                /*
                 * if (templateDimension.width * (1 - SIZE_THRESHOLD_WIDTH) <
                 * mask.getWidth() && mask.getWidth() < templateDimension.width
                 * * (1 + SIZE_THRESHOLD_WIDTH) && templateDimension.height * (1
                 * - SIZE_THRESHOLD_HEIGHT) < mask.getHeight() &&
                 * mask.getHeight() < templateDimension.height * (1 +
                 * SIZE_THRESHOLD_HEIGHT)) {
                 */
                final byte[] gray = ImageUtils.cropImage(this.imagePixels,
                        this.imageWidth, this.imageHeight, mask);

                final Image originalCropped = ImageUtils.cropFXImage(
                        this.original, new Rectangle(posX + mask.getX(), posY
                                + mask.getY(), (int) mask.getWidth(),
                                (int) mask.getHeight()));

                final GrayImage grayImage = new GrayImage(gray,
                        (int) mask.getWidth(), (int) mask.getHeight());
                // final GrayImage binImage = new GrayImage(pixels, (int)
                // mask.getWidth(), (int) mask.getHeight());
                final BinaryImage binImage = new BinaryImage(pixels,
                        (int) mask.getWidth(), (int) mask.getHeight());
                // binImage.labelConnectedComponents();
                // binImage.confineToLargestRegions( glyph.getNumOfComponents()
                // );
                // binImage.whitenBorderColumns(5);
                // binImage.keepForegroundPixelsWhichMatch(
                // glyph.getBinarizedImage() );
                // binImage.removeRegionsWhichAreLessThan(20);
                // binImage.closing();

                // //////////////////////////////////////////////////////////////////
                /*
                 * PixelReader pixelReader = originalCropped.getPixelReader();
                 * int width = (int)originalCropped.getWidth(); int height =
                 * (int)originalCropped.getHeight(); byte[] c = new
                 * byte[width*height]; final GrayImage imageToHDD = new
                 * GrayImage(c, width, height);
                 * 
                 * for (int y1 = 0; y1 < height; y1++){ for (int x1 = 0; x1 <
                 * width; x1++){ Color color = pixelReader.getColor(x1, y1);
                 * final int r = ((int) (color.getRed() * 255)) << 16; final int
                 * g = ((int) (color.getGreen() * 255)) << 8; final int b =
                 * ((int) (color.getBlue() * 255)); final int fullAlpha =
                 * 0xff000000; int greyValue = fullAlpha | r | g | b;
                 * imageToHDD.setToGrey(x1, y1, greyValue); } }
                 * ImageUtils.writeGrayscaleImage ("CC\\CC "
                 * +value+" Koord"+x+"-"+y+" Bildausschnitt.png", imageToHDD);
                 */// //////////////////////////////////////////////////////////////////

                coefficientsAndGlyphs[value]
                        .add(Glyph.extractGlyph(
                                originalCropped,
                                grayImage,
                                binImage,
                                new Rectangle(posX + mask.getX(), posY
                                        + mask.getY(), (int) mask.getWidth(),
                                        (int) mask.getHeight())));
                TEMP_NUMBER_OF_ACCEPTED_GLYPHS++;
                /*
                 * } else { ++counterRejected; }
                 */}
            ++index;
        }
        DiptychonLogger.info("Found {} possible matches.", counterAll);
        DiptychonLogger.info("Rejected {} due to size restrictions (I).",
                counterRejected);

        // DiptychonLogger.info("Calculation Standarddeviation...");
        /*
         * Second size restriction, using the standard deviation in height
         */
        // coefficientsAndGlyphs =
        // this.calcStandardDeviation(coefficientsAndGlyphs);

        return coefficientsAndGlyphs;
    }

    /**
     * Calculates the standard deviation in height and filters out every glyph
     * which does not fit average +/-standard deviation
     * 
     * @param coefficientsAndGlyphs
     *            The list of accepted matches before second size restriction,
     *            which takes the standard deviation into account
     * @return the size filtered array of lists of glyphs sorted by correlation
     *         coefficient
     */
    private ArrayList<Glyph>[] calcStandardDeviation(
            final ArrayList<Glyph>[] coefficientsAndGlyphs) {
        @SuppressWarnings("unchecked")
        final ArrayList<Glyph>[] coefficientsAndGlyphsSizeFiltered = new ArrayList[TEMPLATE_MATCHING_THRESHOLD_MAX + 1];

        for (int i = 0; i < coefficientsAndGlyphsSizeFiltered.length; ++i) {
            coefficientsAndGlyphsSizeFiltered[i] = new ArrayList<>();
        }

        double averageHeight = 0;
        double counterNumOfGlyphs = 0;
        for (int i = TEMPLATE_MATCHING_THRESHOLD; i < coefficientsAndGlyphs.length; ++i) {
            counterNumOfGlyphs += coefficientsAndGlyphs[i].size();
            for (final Glyph g : coefficientsAndGlyphs[i]) {
                averageHeight += g.getHeight();
            }
        }
        System.out.println("counterNumOfGlyphs: " + counterNumOfGlyphs);
        averageHeight /= counterNumOfGlyphs;
        DiptychonLogger.info("Average height: {}", averageHeight);

        double standardDeviationHeight = 0;
        for (int i = TEMPLATE_MATCHING_THRESHOLD; i < coefficientsAndGlyphs.length; ++i) {
            for (final Glyph g : coefficientsAndGlyphs[i]) {
                standardDeviationHeight += (g.getHeight() - averageHeight)
                        * (g.getHeight() - averageHeight);
            }
        }
        standardDeviationHeight /= (counterNumOfGlyphs - 1);
        standardDeviationHeight = Math.sqrt(standardDeviationHeight);
        DiptychonLogger.info("Standarddeviation height: {}",
                standardDeviationHeight);
        standardDeviationHeight = Math.ceil(standardDeviationHeight);

        int counterRejected = 0;
        for (int i = TEMPLATE_MATCHING_THRESHOLD; i < coefficientsAndGlyphs.length; ++i) {
            for (final Glyph g : coefficientsAndGlyphs[i]) {
                if (Math.abs(averageHeight - g.getHeight()) <= standardDeviationHeight) {
                    coefficientsAndGlyphsSizeFiltered[i].add(g);
                } else {
                    ++counterRejected;
                }
            }
        }
        DiptychonLogger.info(
                "Rejected {} due to size restriction test II (StDv).",
                counterRejected);
        return coefficientsAndGlyphsSizeFiltered;
    }

    /**
     * Gets the minimal and maximal values of the result of template matching
     * (lying in the range [-1..1])
     * 
     * @param correlationCoefficients
     *            the correlation coefficients resulting from template matching
     * @return the minimal and maximal value of the result of template matching
     *         (lying in the range [-1..1])
     */
    private double[] getMinMax(final double[] correlationCoefficients) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < correlationCoefficients.length; ++i) {
            if (correlationCoefficients[i] < min
                    && !Double.isInfinite(correlationCoefficients[i])) {
                min = correlationCoefficients[i];
            }
            if (correlationCoefficients[i] > max
                    && !Double.isInfinite(correlationCoefficients[i])) {
                max = correlationCoefficients[i];
            }
        }
        return new double[] { min, max };
    }

    /**
     * Normalizes the result of template matching, which lies in the range
     * [-1..1], to [0..255]
     * 
     * @param correlationCoefficients
     *            the values to be normalized
     * @return the normalized values in the range [0..255]
     */
    private int[] normalizeCorrelationCoeff(
            final double[] correlationCoefficients, boolean fullMax) {
        final double[] minMax = this.getMinMax(correlationCoefficients);
        final double min = minMax[0];
        double max = 0;
        if (fullMax) {
            max = 1;
        } else {
            max = minMax[1];
        }
        final int[] newValues = new int[correlationCoefficients.length];
        final double normalizingFactor = 1 / (max - min);
        if (min != max) {
            for (int i = 0; i < correlationCoefficients.length; ++i) {
                newValues[i] = (int) Math.round(TEMPLATE_MATCHING_THRESHOLD_MAX
                        * (correlationCoefficients[i] - min)
                        * normalizingFactor);
            }
        }
        return newValues;
    }

    /**
     * This method extracts the positions a possible match is located at.
     * Therefore all values above the {@link #TEMPLATE_MATCHING_THRESHOLD
     * Template Matching Threshold} are taken into account. For each of the
     * values it is checked whether they are the highest value within a region
     * or not. If a value is the highest value within a region, this value is
     * kept, otherwise it is set to 0.
     * 
     * @param correlationCoefficients
     *            The correlation coefficients which are to be analysed
     * @return the corrected values in an array, i.e. if a value is 0 there is
     *         no possible match or a better match within a specific radius. !=
     *         0 otherwise
     */
    private int[] thresholdAndNeighbourhood(final int[] correlationCoefficients) {
        final Dimension templateDimension = this.template.dimension();
        final int xc = templateDimension.width >> 1;// division by 2
        final int yc = templateDimension.height >> 1;// division by 2
        final int[] correctedValues = new int[correlationCoefficients.length];
        /*
         * within this radius a value is checked whether it is the maximum or
         * not
         */
        final int neighbourhoodRadiusX = Math.min(10, xc);
        final int neighbourhoodRadiusY = Math.min(10, yc);

        for (int y = yc; y < this.imageHeight - yc; ++y) {
            final int offset = y * this.imageWidth;
            for (int x = xc; x < this.imageWidth - xc; ++x) {
                final int index = x + offset;
                final int value = correlationCoefficients[index];
                if (value > TEMPLATE_MATCHING_THRESHOLD) {
                    boolean bestValue = true;
                    for (int yn = y - neighbourhoodRadiusY; yn <= y
                            + neighbourhoodRadiusY; ++yn) {
                        final int offsetNeighbour = yn * this.imageWidth;
                        for (int xn = x - neighbourhoodRadiusX; xn <= x
                                + neighbourhoodRadiusX; ++xn) {
                            final int indexNeighbour = xn + offsetNeighbour;
                            /*
                             * without this condition the result would always be
                             * 0
                             */
                            if (yn == y && xn == x) {
                                continue;
                            }
                            final int neighbour = correlationCoefficients[indexNeighbour];
                            /*
                             * check if any neighbor value is greater
                             */
                            if (neighbour >= value) {
                                correlationCoefficients[index] = 0;
                                bestValue = false;
                                break;
                            }
                        }
                        /*
                         * if it is not the best value in the region, we can
                         * break because the value will have been set to 0
                         */
                        if (!bestValue) {
                            break;
                        }
                    }
                    if (bestValue) {
                        correctedValues[index] = value;
                    }
                } else {
                    correlationCoefficients[index] = 0;
                }
            }
        }
        return correctedValues;
    }

    /**
     * Within this method the next validation step will be applied. It can be
     * one of the approaches presented at {@link #FACTORY Validation Approach}
     * 
     * @param glyphsWithThreshold
     *            The sorted array of glyphs which should be validated
     * @return An array of lists of the validated and accepted glyphs sorted by
     *         correlation coefficient
     */
    private ArrayList<Glyph>[] validateTemplateMatching(
            final ArrayList<Glyph>[] glyphsWithThreshold) {
        if (FACTORY == null) {
            return glyphsWithThreshold;
        }
        final A_ValidationApproach templateData = FACTORY.createFeature(
                this.glyph.getBinarizedImagePixels(), this.template.width(),
                this.template.height());

        @SuppressWarnings("unchecked")
        final ArrayList<Glyph>[] validated = new ArrayList[TEMPLATE_MATCHING_THRESHOLD_MAX + 1];
        for (int i = 0; i < validated.length; ++i) {
            validated[i] = new ArrayList<Glyph>();
        }
        int counterSize = 0;
        int counterRejected = 0;
        for (int i = TEMPLATE_MATCHING_THRESHOLD; i <= TEMPLATE_MATCHING_THRESHOLD_MAX; ++i) {
            for (final Glyph g : glyphsWithThreshold[i]) {
                ++counterSize;
                final Dimension glyphDimension = g.getDimension();

                final A_ValidationApproach compareTo = FACTORY.createFeature(
                        g.getBinarizedImagePixels(), glyphDimension.width,
                        glyphDimension.height);
                if (templateData.equals(compareTo)) {
                    validated[i].add(g);
                } else {
                    ++counterRejected;
                }
            }
        }

        DiptychonLogger.info(
                "Rejected {} due to {} comparison. Remaining glyphs: {}",
                counterRejected, templateData.getName(),
                (counterSize - counterRejected));

        return validated;
    }

    /**
     * This method checks if possible matches occurs multiple times. This could
     * happen because the neighborhood search would be too restrictive when
     * choosing a greater radius.
     * <p>
     * If a glyph has multiple occurrences, the one with the highest threshold
     * will be used.
     * 
     * @param validated
     *            The list of validated glyphs which has to be checked for
     *            multiple occurrences
     * @return The filtered possible matches without multiple occurrences of a
     *         glyph
     */
    private ArrayList<Glyph>[] checkAlreadyExtracted(
            final ArrayList<Glyph>[] validated) {
        DiptychonLogger.trace("Checking for already extracted Glyphs...");
        @SuppressWarnings("unchecked")
        final ArrayList<Glyph>[] coefficientsAndGlyphsCleaned = new ArrayList[TEMPLATE_MATCHING_THRESHOLD_MAX + 1];

        for (int i = 0; i < coefficientsAndGlyphsCleaned.length; ++i) {
            coefficientsAndGlyphsCleaned[i] = new ArrayList<>();
        }

        int countNotAlreadyExtracted = 0;
        for (int i = TEMPLATE_MATCHING_THRESHOLD; i <= TEMPLATE_MATCHING_THRESHOLD_MAX; ++i) {
            final ArrayList<Glyph> tmpGlyphList = validated[i];
            final int size = tmpGlyphList.size();
            for (int j = 0; j < size; ++j) {
                final Glyph g = tmpGlyphList.get(j);
                boolean alreadyExtracted = false;
                for (int k = i + 1; k < validated.length; ++k) {
                    for (int l = 0; l < validated[k].size(); ++l) {
                        final Glyph compareWith = validated[k].get(l);
                        if (g.isAlreadyExtracted(compareWith)) {
                            alreadyExtracted = true;
                            break;
                        }
                    }
                }
                if (!alreadyExtracted) {
                    ++countNotAlreadyExtracted;
                    g.setID(DocumentPanel.TemplateMatchingIDColor.ACCEPT
                            .getID());
                    coefficientsAndGlyphsCleaned[i].add(g);
                }
            }
        }
        DiptychonLogger.info(
                "Remaining glyphs after \"Already Extracted Test\": {}",
                countNotAlreadyExtracted);
        return coefficientsAndGlyphsCleaned;
    }
}
