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

import de.diptychon.models.glyphGeometry.BinaryImage;
import de.diptychon.models.misc.GrayImage;

public class GlyphCandidates {

    final static public int OCCUPIED = 128;
    /**
     * each candidate is drawn into this copy of the original image
     */
    protected final GrayImage occupiedPlaces;
    private final int xMin;
    private final int xMax;
    private final int yMin;
    private final int yMax;

    /**
     * Constructor
     * 
     * @param binaryImage
     *            the whole binary document image in which the line to be
     *            searched in is contained
     */
    public GlyphCandidates(GrayImage emptyPlaces, int xMin, int xMax, int yMin,
            int yMax) {
        occupiedPlaces = new GrayImage(emptyPlaces.getPixelCloned(),
                emptyPlaces.getWidth(), emptyPlaces.getHeight());
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }

    public void occupy(int px, int py, BinaryImage candidate) {
        for (int x = 0; x < candidate.getWidth(); x++)
            for (int y = 0; y < candidate.getHeight(); y++)
                if (candidate.getPixel(x, y) == BinaryImage.FOREGROUND)
                    occupiedPlaces.setToGrey(x + px, y + py, OCCUPIED);
    }

    public boolean vacant(int xStart, int xEnd, int yStart, int height) {
        int figurePoints = 0;
        for (int x = xStart; x < xEnd; x++)
            for (int y = yStart; y < yStart + height; y++)
                if (occupiedPlaces.getPixel(x, y) == BinaryImage.FOREGROUND)
                    figurePoints++;
        return figurePoints > 200;
    }

    protected boolean pointVacant(int indexImage) {
        return (occupiedPlaces.getPixels()[indexImage] & 0xff) != OCCUPIED;
    }

    // protected boolean vacant(int indexImage){
    // return (occupiedPlaces.getPixels()[indexImage] & 0xff) != OCCUPIED;
    // }

}