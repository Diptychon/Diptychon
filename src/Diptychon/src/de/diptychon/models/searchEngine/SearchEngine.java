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
import java.util.Collections;

import javafx.scene.image.Image;
import de.diptychon.DiptychonFX;
import de.diptychon.controller.DocumentPanelController;
import de.diptychon.models.data.Glyph;
import de.diptychon.models.data.ImageLine;
import de.diptychon.models.misc.GrayImage;
import de.diptychon.models.representation.Transcript;
import de.diptychon.ui.views.dialogs.SearchEngineDialog;

/**
 * The search engine is a temporal object which is always started from scratch
 * when called together with the current settings of the search engine dialog.
 * 
 * It is launched with a set of glyphs which can be found by that search engine.
 * It returns a set of new glyphs which are made available for the Diptychon
 * system.
 * 
 * Different search strategies can be called, each one returns an
 * ArrayList<Glyph>: 1. searchGlyphsInParallel() instantiates class
 * SearchParallel
 * 
 * 2. searchGlyphsSequentially() instantiates class SearchSequentially
 */
public class SearchEngine {

    private GlyphQueries knownGlyphs;
    private SearchableDocument document;
    private SearchableLine line;

    private final Transcript transcript;

    /**
     * Constructor 'knownGlyphs' are searched for
     * 
     * @param in
     *            the text line 'lineID'
     * @param within
     *            the image 'original'
     */
    public SearchEngine(ArrayList<Glyph> knownGlyphs, String lineID,
            ArrayList<ImageLine> lines, Image original, GrayImage grayscale,
            GrayImage binarized, final Transcript t) {
        Collections.sort(lines);
        int lineNo = 0;
        while (!lines.get(lineNo).getID().equals(lineID))
            lineNo++;
        ImageLine l = lines.get(lineNo);

        this.line = new SearchableLine(binarized, l.getMinX(), l.getMaxX(),
                l.getMinY(), l.getMaxY(), l.getID());
        this.document = new SearchableDocument(original, grayscale, binarized);
        this.transcript = t;
        this.knownGlyphs = new GlyphQueries(knownGlyphs);
    }

    /**
     * This search strategy iterates over the text lines only once. At each
     * position all glyphs are searched for and the best one is taken.
     */
    public ArrayList<Glyph> searchGlyphsInParallel() {
        ArrayList<Glyph> restrictToSelectedGlyphs = new ArrayList<Glyph>();
        for (Glyph nextGlyph : knownGlyphs.getAll())
            if (nextGlyphTypeIsSelected(nextGlyph))
                restrictToSelectedGlyphs.add(nextGlyph);
        this.knownGlyphs = new GlyphQueries(restrictToSelectedGlyphs);

        SearchParallel s = new SearchParallel(document, line, knownGlyphs,
                transcript);
        s.start();
        s.extractSpaceCharacters(3);
        return s.foundGlyphs;
    }

    /**
     * This search strategy iterates over all known glyphs as patterns, each of
     * which is searched for individually in the whole text line.
     * 
     * Similar as for the iteration via the cc (corr.-coeff.) it is possible to
     * iterate via other parameters, e.g. the coverRatio.
     */
    public ArrayList<Glyph> searchGlyphsSequentially() {
        double iterateMin = SearchEngineDialog._G_ccThreshMin;
        double iterateMax = SearchEngineDialog._G_ccThreshMax;
        double iterateStep = SearchEngineDialog._G_ccThreshStep;
        double iterations = (iterateMax - iterateMin) / iterateStep;
        SearchSequentially c = new SearchSequentially(document, line,
                transcript);
        c.setCCThreshold(iterateMax);
        c.coverRatio = 1.5;

        // --------------------------
        knownGlyphs.separateGlyphs();

        while (c.getCCThreshold() > iterateMin) {
            for (Glyph nextGlyph : knownGlyphs.getComplex()) {
                transcript
                        .firePropertyChange(
                                DocumentPanelController.PROGRESS_CHANGED,
                                null,
                                (double) DiptychonFX.progressWatch[0]++
                                        / (knownGlyphs.getComplex().size() * iterations));

                if (nextGlyphTypeIsSelected(nextGlyph)) {
                    c.setQueryObject(nextGlyph);
                    c.iterateThroughTextLine();
                    // c.iterateThroughTextLine3DOccupancyGrid();
                }
            }
            c.setCCThreshold(c.getCCThreshold() - iterateStep);
        }

        // ---------------------------
        c.setCCThreshold(iterateMax);
        while (c.getCCThreshold() > iterateMin) {
            for (Glyph nextGlyph : knownGlyphs.getSimple()) {
                transcript
                        .firePropertyChange(
                                DocumentPanelController.PROGRESS_CHANGED,
                                null,
                                (double) DiptychonFX.progressWatch[0]++
                                        / (knownGlyphs.getSimple().size() * iterations));

                if (nextGlyphTypeIsSelected(nextGlyph)) {
                    c.setQueryObject(nextGlyph);
                    c.iterateThroughTextLine();
                }
            }
            c.setCCThreshold(c.getCCThreshold() - iterateStep);
        }
        // ------------------------------------
        c.findRemainingComponents();
        // ------------------------------------
        c.extractSpaceCharacters(3);
        return c.foundGlyphs;
    }

    /**
     * Returns true if the glyph-type of nextGlyph is selected in the dialog
     * box.
     */
    private boolean nextGlyphTypeIsSelected(Glyph nextGlyph) {
        return (SearchEngineDialog._G_a && nextGlyph.getGroupID().equals("a"))
                || (SearchEngineDialog._G_b && nextGlyph.getGroupID().equals(
                        "b"))
                || (SearchEngineDialog._G_c && nextGlyph.getGroupID().equals(
                        "c"))
                || (SearchEngineDialog._G_d && nextGlyph.getGroupID().equals(
                        "d"))
                || (SearchEngineDialog._G_e && nextGlyph.getGroupID().equals(
                        "e"))
                || (SearchEngineDialog._G_f && nextGlyph.getGroupID().equals(
                        "f"))
                || (SearchEngineDialog._G_g && nextGlyph.getGroupID().equals(
                        "g"))
                || (SearchEngineDialog._G_h && nextGlyph.getGroupID().equals(
                        "h"))
                || (SearchEngineDialog._G_i && nextGlyph.getGroupID().equals(
                        "i"))
                || (SearchEngineDialog._G_j && nextGlyph.getGroupID().equals(
                        "j"))
                || (SearchEngineDialog._G_k && nextGlyph.getGroupID().equals(
                        "k"))
                || (SearchEngineDialog._G_l && nextGlyph.getGroupID().equals(
                        "l"))
                || (SearchEngineDialog._G_m && nextGlyph.getGroupID().equals(
                        "m"))
                || (SearchEngineDialog._G_n && nextGlyph.getGroupID().equals(
                        "n"))
                || (SearchEngineDialog._G_o && nextGlyph.getGroupID().equals(
                        "o"))
                || (SearchEngineDialog._G_p && nextGlyph.getGroupID().equals(
                        "p"))
                || (SearchEngineDialog._G_q && nextGlyph.getGroupID().equals(
                        "q"))
                || (SearchEngineDialog._G_r && nextGlyph.getGroupID().equals(
                        "r"))
                || (SearchEngineDialog._G_s && nextGlyph.getGroupID().equals(
                        "s"))
                || (SearchEngineDialog._G_t && nextGlyph.getGroupID().equals(
                        "t"))
                || (SearchEngineDialog._G_u && nextGlyph.getGroupID().equals(
                        "u"))
                || (SearchEngineDialog._G_v && nextGlyph.getGroupID().equals(
                        "v"))
                || (SearchEngineDialog._G_w && nextGlyph.getGroupID().equals(
                        "w"))
                || (SearchEngineDialog._G_x && nextGlyph.getGroupID().equals(
                        "x"))
                || (SearchEngineDialog._G_y && nextGlyph.getGroupID().equals(
                        "y"))
                || (SearchEngineDialog._G_z && nextGlyph.getGroupID().equals(
                        "z"))
                || (SearchEngineDialog._G_um && nextGlyph.getGroupID().equals(
                        "um"))
                || (SearchEngineDialog._G_et && nextGlyph.getGroupID().equals(
                        "et"));
    }

}