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
import de.diptychon.models.data.Glyph;
import de.diptychon.models.data.ImageLine;
import de.diptychon.models.misc.GrayImage;
import de.diptychon.models.misc.ImageUtils;
import de.diptychon.models.representation.Transcript;

/**
 * Implementation of a sequential search strategy
 * 
 * A single query pattern (a glyph) is searched for by means of: - height
 * comparison between candidate and query - their correlation coefficient -
 * their upper and lower part need to be similar (distribution of figure points)
 * - candidate needs to match query to a high degree (equal pixels), i.e.
 * commonalities > k*differences between candidate and query
 * 
 * iterateThroughTextLine() To iterate through the text line and to mark a found
 * place to be occupied, so that no other glyph will be tested at this location
 * --> high speed --> visited places are only tested by image-columns
 * (imprecise, as glyphs overlap and text lines might be falling down)
 */
public class SearchSequentially extends Search {

    /**
     * Constructor
     * 
     * @params The <code>document</code> with the <code>line</code> to be
     *         searched through.
     */
    public SearchSequentially(SearchableDocument document, SearchableLine line,
            Transcript t) {
        super(document, line, t);
    }

    public void iterateThroughTextLine() // with one queryGlyph
    {
        final int yMax = (int) Math.min(imageHeight - queryHeight,
                Math.max(line.yMin, line.yMax));

        if (queryWidth > 1 && // avoid space character images as queries
                querySize > 60) // avoid points, commas, and tiny stuff
        {
            // along the whole text line from left to right
            for (int x = line.xMin; x < line.xMax - queryWidth; x++) {
                // analyse height of candidate within text line, along width of
                // queryGlyh
                int yTop = Integer.MAX_VALUE; // upper most coordinate of the
                                              // script
                int yBot = 0; // lower most coordinate of the script
                for (int xT = x; xT <= x + queryWidth && xT < line.xMax; xT++) {
                    if (line.getTopAt(xT) != ImageLine.EMPTY_SPACE) {
                        if (line.getTopAt(xT) < yTop)
                            yTop = line.getTopAt(xT);
                        if (line.getBotAt(xT) > yBot)
                            yBot = line.getBotAt(xT);
                    }
                }
                // now, yTop and yBot are determined

                for (int y = line.yMin; y < yMax; ++y) {
                    if (queryCoversTextZone(y, yTop, yBot)) {
                        if (line.isVacant(x, x + queryWidth - 1)) {
                            double corrCoef = correlationCoefficient(x, y);
                            if (corrCoef > getCCThreshold()) {
                                GlyphCandidate c = new GlyphCandidate(x, y,
                                        queryGlyph, document, corrCoef);
                                if (c.getRC() != null) {
                                    if (candidateMatchesQuery(c)) {
                                        foundGlyphs.add(c
                                                .instantiateAsGlyph(line));
                                        line.occupy(x, x + queryWidth - 1);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void findRemainingComponents() {
        int x = line.xMin;
        while (x < line.xMax) {
            int xStart = x;
            while (x < line.xMax && line.isVacantWithoutTolerance(x, x))
                x++;
            if (x > xStart + 10) // die kleinen im Original ausradieren -->
                                 // stören dann nicht mehr in 'Fragment'
            {
                line.occupy(xStart, x - 1);

                int x1 = xStart;
                int x2 = x - 1;
                int y1 = line.yMin;
                int y2 = line.yMax;
                Image origCropped = ImageUtils.cropFXImage(
                        document.original,
                        new Rectangle(x1, y1, Math.abs(x2 - x1), Math.abs(y2
                                - y1)));

                // Bild-crop speichern
                final byte[] cropped = ImageUtils.cropImage(
                        document.grayValues,
                        (int) (document.original.getWidth()),
                        (int) (document.original.getHeight()), x1, y1,
                        Math.abs(x2 - x1), Math.abs(y2 - y1));
                final GrayImage gray = new GrayImage(cropped,
                        Math.abs(x2 - x1), Math.abs(y2 - y1));
                // ImageUtils.writeGrayscaleImage("test\\"+x1+".png",
                // gray);

                // Binärbildausschnitt speichern
                final byte[] croppedPartBinaer = ImageUtils.cropImage(
                        document.binaryValues,
                        (int) (document.original.getWidth()),
                        (int) (document.original.getHeight()), x1, y1,
                        Math.abs(x2 - x1), Math.abs(y2 - y1));
                final GrayImage binaer = new GrayImage(croppedPartBinaer,
                        Math.abs(x2 - x1), Math.abs(y2 - y1));
                // ImageUtils.writeGrayscaleImage("test\\binaer"+x1+".png",
                // binaer);

                Glyph unknownGlyph = Glyph.extractGlyph(
                        origCropped,
                        gray,
                        binaer,
                        new Rectangle(x1, y1, Math.abs(x2 - x1), Math.abs(y2
                                - y1)));
                unknownGlyph.setGroupID("_");
                foundGlyphs.add(unknownGlyph);
            } else
                x++;
        }
    }

}