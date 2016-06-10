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
import java.util.Comparator;

import de.diptychon.models.data.Glyph;

/**
 * Represents all transcribed, that is known, glyphs which are used in the
 * searchEngine.
 */
public class GlyphQueries {

    private final ArrayList<Glyph> glyphs;
    private ArrayList<Glyph> simpleGlyphs;
    private ArrayList<Glyph> complexGlyphs;

    public GlyphQueries(final ArrayList<Glyph> pGlyphs) {
        glyphs = pGlyphs;
        sortAlphabetically();
        // sortBySize();
        // sortByWidth();
        // sortByBoundingBoxSize();
    }

    /**
     * Separates the set of all glyphs into two sub-sets according to the
     * complexity of the glyphs.
     */
    public void separateGlyphs() {
        simpleGlyphs = new ArrayList<Glyph>();
        complexGlyphs = new ArrayList<Glyph>();
        for (Glyph g : glyphs) {
            System.out.println(g.getGroupID());
            if (g.getGroupID().equals("a") || g.getGroupID().equals("e")
                    || g.getGroupID().equals("m") || g.getGroupID().equals("n")
                    || g.getGroupID().equals("u") || g.getGroupID().equals("r")
                    || g.getGroupID().equals("s"))
                complexGlyphs.add(g);
            else
                simpleGlyphs.add(g);
        }
    }

    public ArrayList<Glyph> getAll() {
        return glyphs;
    }

    public ArrayList<Glyph> getSimple() {
        return simpleGlyphs;
    }

    public ArrayList<Glyph> getComplex() {
        return complexGlyphs;
    }

    public void sortAlphabetically() {
        Collections.sort(glyphs, new Comparator<Glyph>() {
            @Override
            public int compare(Glyph g1, Glyph g2) {
                return g1.getGroupID().compareTo(g2.getGroupID());
            }
        });
    }

    public void sortBySize() {
        Collections.sort(glyphs, new Comparator<Glyph>() {
            @Override
            public int compare(Glyph g1, Glyph g2) {
                return g2.getSize() - g1.getSize();
            }
        });
    }

    public void sortByWidth() {
        Collections.sort(glyphs, new Comparator<Glyph>() {
            @Override
            public int compare(Glyph g1, Glyph g2) {
                return g2.getWidth() - g1.getWidth();
            }
        });
    }

    public void sortByBoundingBoxSize() {
        Collections.sort(glyphs, new Comparator<Glyph>() {
            @Override
            public int compare(Glyph g1, Glyph g2) {
                return (g2.getWidth() * g2.getHeight())
                        - (g1.getWidth() * g1.getHeight());
            }
        });
    }
}