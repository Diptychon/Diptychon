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

import java.util.ArrayList;

import de.diptychon.models.data.Glyph;
import de.diptychon.models.data.ImageLine;
import de.diptychon.models.representation.Transcript;
import de.diptychon.ui.views.dialogs.SearchEngineDialog;

public class SearchParallel extends Search {

    private double iterateMin;
    private double iterateMax;
    private double iterateStep;
    private double iterations;

    private final GlyphQueries knownGlyphs;

    /**
     * Constructor
     * 
     * @params The <code>document</code> with the <code>line</code> to be
     *         searched through by the GlyphQueries <code>gq</code>.
     */
    public SearchParallel(SearchableDocument document, SearchableLine line,
            GlyphQueries gq, Transcript t) {
        super(document, line, t);
        knownGlyphs = gq;
        knownGlyphs.separateGlyphs();
    }

    public void start() {
        coverRatio = 1.5;

        iterateMin = 0.0;
        iterateMax = 0.1;
        iterateStep = 0.1;
        iterations = (iterateMax - iterateMin) / iterateStep;
        iterateThroughTextLine(knownGlyphs.getComplex());

        iterateMin = SearchEngineDialog._G_ccThreshMin;
        iterateMax = SearchEngineDialog._G_ccThreshMax;
        iterateStep = SearchEngineDialog._G_ccThreshStep;
        iterations = (iterateMax - iterateMin) / iterateStep;
        iterateThroughTextLine(knownGlyphs.getSimple());
    }

    public void iterateThroughTextLine(ArrayList<Glyph> patternGlyphs) {
        final int yMax = (int) Math.min(imageHeight - queryHeight,
                Math.max(line.yMin, line.yMax));
        ArrayList<GlyphCandidate> candidatesInColumn = new ArrayList<GlyphCandidate>();

        // along the whole text line from left to right
        int column = line.xMin;
        while (column < line.xMax) {
            setCCThreshold(iterateMax);

            while (getCCThreshold() > iterateMin) {
                for (int x = column - 5; x < column + 5 && x < line.xMax; x++) {

                    // take the next glyph as a query pattern
                    for (Glyph nextGlyphQuery : patternGlyphs) {

                        // setCCThresholdDependingOnQuery(
                        // nextGlyphQuery.getGroupID() );
                        setQueryObject(nextGlyphQuery);
                        updateProgressWatch(patternGlyphs.size()
                                * (line.xMax - line.xMin) * 10);
                        if (queryWidth > 1 && // avoid space character images as
                                              // queries
                                querySize > 60 && // avoid points, commas, and
                                                  // tiny stuff
                                x >= line.xMin && x < line.xMax - queryWidth) // do
                                                                              // not
                                                                              // walk
                                                                              // over
                                                                              // the
                                                                              // ends
                        {
                            // analyse height of candidate within text line,
                            // along width of queryGlyh
                            int yTop = Integer.MAX_VALUE; // upper most
                                                          // coordinate of the
                                                          // script
                            int yBot = 0; // lower most coordinate of the script
                            for (int xT = x; xT <= x + queryWidth
                                    && xT < line.xMax; xT++) {
                                if (line.getTopAt(xT) != ImageLine.EMPTY_SPACE) {
                                    if (line.getTopAt(xT) < yTop)
                                        yTop = line.getTopAt(xT);
                                    if (line.getBotAt(xT) > yBot)
                                        yBot = line.getBotAt(xT);
                                }
                            }

                            for (int y = line.yMin; y < yMax; ++y) {
                                if (queryCoversTextZone(y, yTop, yBot)) {
                                    if (line.isVacant(x, x + queryWidth - 1)) {
                                        double corrCoef = correlationCoefficient(
                                                x, y);
                                        if (corrCoef > getCCThreshold()) {
                                            GlyphCandidate c = new GlyphCandidate(
                                                    x, y, queryGlyph, document,
                                                    corrCoef);
                                            if (c.getRC() != null)
                                                if (candidateMatchesQuery(c))
                                                    candidatesInColumn.add(c);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (candidatesInColumn.size() > 0) {
                    GlyphCandidate best = candidatesInColumn.get(0);
                    for (GlyphCandidate g : candidatesInColumn) {
                        if (
                        // g.commonalitiesWithGlyph >
                        // best.commonalitiesWithGlyph &&
                        // g.differencesToGlyph < best.differencesToGlyph
                        g.similarity() > best.similarity()
                                &&
                                // g.correlationCoefficient() >
                                // best.correlationCoefficient() //&&
                                Math.abs(g.getRC().getArea() - querySize) < Math
                                        .abs(best.getRC().getArea() - querySize) // &&
                        // g.getRC().getArea() > best.getRC().getArea()
                        )
                            best = g;
                    }
                    foundGlyphs.add(best.instantiateAsGlyph(line));
                    line.occupy(best.getX(),
                            best.getX() + best.binCropped.getWidth() - 1);
                    column = best.getX() + best.binCropped.getWidth() - 1;
                    candidatesInColumn = new ArrayList<GlyphCandidate>();
                } else
                    column = column + 1;

                setCCThreshold(getCCThreshold() - iterateStep);
            }
        }
    }

    // private void setCCThresholdDependingOnQuery(String s)
    // {
    // switch (s) {
    // // case "a": ccThreshold = 0.2; break;
    // // case "c": ccThreshold = 0.6; break;
    // // case "i": ccThreshold = 0.9; break;
    // // case "m": ccThreshold = 0.1; break;
    // case "n": setCCThreshold( 0.0 ); break;
    // // case "u": ccThreshold = 0.1; break;
    // default : setCCThreshold( 0.8 ); break;
    // }
    // }

}
