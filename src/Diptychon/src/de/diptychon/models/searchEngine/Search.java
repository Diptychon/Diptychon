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

import java.awt.Dimension;
import java.util.ArrayList;

import javafx.scene.shape.Rectangle;
import de.diptychon.DiptychonFX;
import de.diptychon.controller.DocumentPanelController;
import de.diptychon.models.algorithms.Template;
import de.diptychon.models.data.Glyph;
import de.diptychon.models.glyphGeometry.BinaryImage;
import de.diptychon.models.glyphGeometry.RegionConfiguration;
import de.diptychon.models.misc.GrayImage;
import de.diptychon.models.misc.ImageUtils;
import de.diptychon.models.representation.Transcript;
import de.diptychon.ui.views.dialogs.SearchEngineDialog;

/**
 * Each search algorithm needs to extend this class.
 */
abstract class Search {

    protected final SearchableDocument document;
    protected final SearchableLine line;

    protected final int imageHeight;

    private double ccThreshold;
    protected float sizeTolerance;
    protected double coverRatio;

    protected Glyph queryGlyph = null;
    protected Template queryGlyphImage = null;
    protected int queryWidth = -1;
    protected int queryHeight = -1;
    protected int querySize = -1;
    protected RegionConfiguration queryRC = null;

    private Transcript progressWatchTranscript;

    /**
     * All found matches
     */
    protected ArrayList<Glyph> foundGlyphs;

    /**
     * Constructor
     */
    public Search(SearchableDocument document, SearchableLine line, Transcript t) {
        this.document = document;
        this.imageHeight = document.grayscale.getHeight();
        this.line = line;
        this.foundGlyphs = new ArrayList<>();
        this.sizeTolerance = SearchEngineDialog._G_glyphSize / 100;
        this.coverRatio = 1.0;
        this.progressWatchTranscript = t;
    }

    protected void updateProgressWatch(int i) {
        progressWatchTranscript.firePropertyChange(
                DocumentPanelController.PROGRESS_CHANGED, null,
                (double) DiptychonFX.progressWatch[0]++ / i);
    }

    public double getCCThreshold() {
        return ccThreshold;
    }

    public void setCCThreshold(double t) {
        ccThreshold = t;
    }

    /**
     * Sets new query object
     */
    public void setQueryObject(Glyph aGlyph) {
        queryGlyph = aGlyph;
        queryGlyphImage = new Template(aGlyph.getGrayImagePixels(),
                aGlyph.getWidth(), aGlyph.getHeight());
        queryWidth = queryGlyph.getWidth();
        queryHeight = queryGlyph.getHeight();
        querySize = queryGlyph.getSize();

        BinaryImage tmp = new BinaryImage(queryGlyph.getBinarizedImagePixels(),
                queryWidth, queryHeight);
        tmp.labelConnectedComponents();
        queryRC = tmp.getRegionConfiguration();
    }

    /**
     * QueryGlyph starts approximately at top of text zone and stops at the
     * bottom.
     */
    protected boolean queryCoversTextZone(int y, int yTop, int yBot) {
        return y > yTop - 5 && y < yTop + 5 && y + queryHeight > yBot - 5
                && y + queryHeight < yBot + 5;
    }

    protected boolean candidateMatchesQuery(GlyphCandidate c) {
        return
        // c.getRC().getHeight() > queryRC.getHeight() - 2 && bringt
        // c.getRC().getHeight() < queryRC.getHeight() + 2 && nichts
        c.getRC().getArea() > queryRC.getArea()
                - (sizeTolerance * queryRC.getArea())
                && c.getRC().getArea() < queryRC.getArea()
                        + (sizeTolerance * queryRC.getArea())
                && ((line.isTall(c.getRC().getHeight()) && line.isTall(queryRC
                        .getHeight())) || (!line.isTall(c.getRC().getHeight()) && !line
                        .isTall(queryRC.getHeight())))
                && c.getRC().topSimilarBottomPart(queryRC) &&
                // c.getRC().getNumOfHolesLarger(5) ==
                // queryRC.getNumOfHolesLarger(5) && nicht gut
                c.commonWithGlyph > coverRatio * c.differentToGlyph;
    }

    /**
     * @param the
     *            x and y coordinates of the top-left, where to match the query
     * @return the correlation coefficient at position <code>(x, y)</code>
     */
    protected double correlationCoefficient(final int x, final int y) {
        final Dimension query = queryGlyphImage.dimension();
        final byte[] queryValues = queryGlyphImage.pixels();
        int sumI = 0;
        int sumI2 = 0;
        int covIR = 0;

        for (int yt = 0; yt < query.height; ++yt) {
            final int offsetImage = (yt + y) * document.grayscale.getWidth();
            final int offsetQuery = yt * query.width;
            for (int xt = 0; xt < query.width; ++xt) {
                final int indexImage = xt + x + offsetImage;
                final int indexQuery = xt + offsetQuery;
                final int valImage = document.grayValues[indexImage] & 0xff;
                final int valQuery = queryValues[indexQuery] & 0xff;
                sumI += valImage;
                sumI2 += valImage * valImage;
                covIR += valImage * valQuery;
            }
        }
        final double meanI = sumI / (double) queryGlyphImage.size();
        final double val0 = queryGlyphImage.size() * meanI
                * queryGlyphImage.mean();
        final double val1 = covIR - val0;
        final double val2 = Math.sqrt(sumI2 - queryGlyphImage.size() * meanI
                * meanI)
                * queryGlyphImage.sigma();

        return val1 / val2;
    }

    /**
     * Include space characters at empty columns, but not at the start or end of
     * the text line.
     */
    public void extractSpaceCharacters(int spaceWidth) {
        for (int x = line.xMin; x < line.xMax - spaceWidth; x++) {
            int lastColumnOfSpaceChar = line.isASpaceCharacter(x, x
                    + spaceWidth - 1);
            if (lastColumnOfSpaceChar != SearchableLine.noSpaceCharacterFound
                    && x > line.xMin
                    && lastColumnOfSpaceChar < line.xMax - spaceWidth - 1) {
                final Rectangle cropSpace = new Rectangle(
                        lastColumnOfSpaceChar, line.yMin, 1, 1);
                GrayImage binaryImage = ImageUtils.cropGrayImage(
                        document.binarized, cropSpace);
                Glyph space = Glyph
                        .extractGlyph(ImageUtils.cropFXImage(document.original,
                                cropSpace), ImageUtils.cropGrayImage(
                                document.grayscale, cropSpace), binaryImage,
                                cropSpace);
                space.setGroupID(" ");
                space.isSpace = true;
                foundGlyphs.add(space);
            }
        }
    }
}