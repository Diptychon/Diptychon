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
import de.diptychon.models.misc.GrayImage;

/**
 * The document image in which the search engine is looking for new glyphs.
 */
public class SearchableDocument {

    protected final Image original;

    protected final GrayImage grayscale;
    protected final byte[] grayValues;

    protected final GrayImage binarized;
    protected final byte[] binaryValues;

    public SearchableDocument(Image original, GrayImage grayscale,
            GrayImage binarized) {
        this.original = original;
        this.grayscale = grayscale;
        this.grayValues = grayscale.getPixels();
        this.binarized = binarized;
        this.binaryValues = binarized.getPixels();
    }
}