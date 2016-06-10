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

import de.diptychon.models.misc.GrayImage;

/**
 * The image of the text row in which new glyphs are searched for.
 */
public class SearchableLine {

    public static final int noSpaceCharacterFound = -1;

    protected final GrayImage binarized;
    protected final int xMin;
    protected final int xMax;
    protected final int yMin;
    protected final int yMax;
    protected final String lineID;
    protected int ascenderRow;
    protected int descenderRow;

    private int[][] topsAndBottomsOfWriting;
    private int[] columnProfile;
    private int[] rowProfile;
    private boolean[] vacant;
    private boolean[] vacantWithoutTolerance;

    private int heightMin;
    private int heightMax;
    private int heightMean;
    private int heightStd;

    /**
     * Constructor
     * 
     * @param binaryImage
     *            the whole binary document image in which the line to be
     *            searched in is contained
     * 
     * @param the
     *            coordinates of this line within the binary image
     */
    public SearchableLine(GrayImage binarized, int xMin, int xMax, int yMin,
            int yMax, String lineID) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.lineID = lineID;
        this.binarized = new GrayImage(binarized.getPixelCloned(),
                binarized.getWidth(), binarized.getHeight());
        analyseAllLineColumns();
    }

    /**
     * - Determines the minimal (topsAndBottomsOfWriting[x][0]) and maximal
     * (topsAndBottomsOfWriting[x][1]) vertical positions of the black writing
     * within this image line. - Determines the number of figure (black) pixels
     * in each column x separately columnProfile[x] and the rowProfile[y]. -
     * initialize 'vacant' with true, since nothing is occupied at the
     * beginning.
     */
    private void analyseAllLineColumns() {
        topsAndBottomsOfWriting = new int[xMax][2];
        columnProfile = new int[xMax];
        rowProfile = new int[yMax - yMin + 1];
        vacant = new boolean[xMax];
        vacantWithoutTolerance = new boolean[xMax];

        for (int x = xMin; x < xMax; x++) // for all columns of this text line
        {
            // determine first black pixel beginning from the top of column x
            boolean found = false;
            int y = yMin;
            while (y++ < yMax && !found) {
                found = binarized.getPixelAsInt(x, y) == GrayImage.BLACK;
            }
            if (found)
                topsAndBottomsOfWriting[x][0] = --y;
            else
                topsAndBottomsOfWriting[x][0] = -1;

            // determine first black pixel beginning from the bottom of column x
            found = false;
            y = yMax;
            while (y-- >= yMin && !found) {
                found = binarized.getPixelAsInt(x, y) == GrayImage.BLACK;
            }
            if (found)
                topsAndBottomsOfWriting[x][1] = ++y;
            else
                topsAndBottomsOfWriting[x][1] = -1;

            // Determines the number of figure (black) pixels for column x
            columnProfile[x] = 0;
            for (y = yMin; y < yMax; y++) {
                if (binarized.getPixelAsInt(x, y) == GrayImage.BLACK) {
                    columnProfile[x]++;
                    rowProfile[y - yMin]++;
                }
            }

            // at the beginning column x is not occupied by any candidate glyph
            vacant[x] = true;
            vacantWithoutTolerance[x] = true;
        }

        // mean height of all columns
        // int totalHeightMean = 0;
        // int totalHeightMin = Integer.MAX_VALUE;
        // int totalHeightMax = 0;
        heightMean = 0;
        heightMin = Integer.MAX_VALUE;
        heightMax = 0;
        int numOfColumns = 0;
        for (int x = xMin; x < xMax; x++) // for all columns of this text line
        {
            // int diff = topsAndBottomsOfWriting[x][1] -
            // topsAndBottomsOfWriting[x][0];
            // totalHeightMean = totalHeightMean + diff;
            // if ( columnProfile[x] < totalHeightMin )
            // totalHeightMin = columnProfile[x];
            // if ( columnProfile[x] > totalHeightMax)
            // totalHeightMax = columnProfile[x];
            if (columnProfile[x] > 0) {
                numOfColumns++;
                heightMean = heightMean + columnProfile[x];
                if (columnProfile[x] < heightMin)
                    heightMin = columnProfile[x];
                if (columnProfile[x] > heightMax)
                    heightMax = columnProfile[x];
            }
        }
        // totalHeightMean = totalHeightMean / (xMax - xMin);
        heightMean = heightMean / numOfColumns;
        // double totalHeightVar = 0;
        double heightVar = 0;
        for (int x = xMin; x < xMax; x++) // for all columns of this text line
        {
            if (columnProfile[x] > 0) {
                // int diff = topsAndBottomsOfWriting[x][1] -
                // topsAndBottomsOfWriting[x][0];
                // totalHeightVar = totalHeightVar + Math.pow(diff -
                // totalHeightMean, 2);
                heightVar = heightVar
                        + Math.pow(columnProfile[x] - heightMean, 2);
            }
        }
        // int totalHeightStd = (int)Math.sqrt(totalHeightVar/(xMax-xMin));
        heightStd = (int) Math.sqrt(heightVar / numOfColumns);

        // System.out.println("totalHeightMean: "+totalHeightMean);
        // System.out.println("totalHeightStd: "+totalHeightStd);
        // System.out.println("totalHeightMin: "+totalHeightMin);
        // System.out.println("totalHeightMax: "+totalHeightMax);
        System.out.println("heightMean: " + heightMean);
        System.out.println("heightStd: " + heightStd);
        System.out.println("heightMin: " + heightMin);
        System.out.println("heightMax: " + heightMax);

        // printRowProfile();

        // zur Überprüfung der Korrektheit Bild mit grauem Ober- und Unterrand
        // abspeichern
        // for(int x = 0; x < binarized.getWidth(); x++)
        // for(int y = 0; y < 2; y++)
        // if (topsAndBottomsOfWriting[x][y] != -1)
        // binarized.setToGrey(x, topsAndBottomsOfWriting[x][y], 128);
        // Abspeichern der Bereiche der ascender and descender
        // for(int x = 0; x < binarized.getWidth(); x++)
        // {
        // binarized.setToGrey(x, ascenderRow, 128);
        // binarized.setToGrey(x, descenderRow, 128);
        // }
        // ImageUtils.writeGrayscaleImage("CC\\LineWithGreyTopsBots.png",
        // binarized);
    }

    public void printRowProfile() {
        int totalSum = 0;
        for (int y = yMin; y < yMax; y++) {
            System.out.println("Zeile " + y + " Anzahl = "
                    + rowProfile[y - yMin]);
            totalSum = totalSum + rowProfile[y - yMin];
        }
        int quarter = totalSum / 10;
        System.out.println("totalSum = " + totalSum + " Quarter = " + quarter);

        int firstQuarter = 0;
        int y = yMin;
        while (firstQuarter < quarter) {
            firstQuarter = firstQuarter + rowProfile[y - yMin];
            y++;
        }
        System.out.println("1st Quarter until line: " + y--);
        ascenderRow = y;

        int lastQuarter = 0;
        y = yMax;
        while (lastQuarter < quarter) {
            lastQuarter = lastQuarter + rowProfile[y - yMin];
            y--;
        }
        System.out.println("4th Quarter at line: " + y++);
        descenderRow = y;

        // double mean = 0.0;
        // for (int y = yMin; y < yMax; y++)
        // {
        // mean = mean + (y * (double)(rowProfile[y-yMin] / (double)totalSum));
        // }
        // System.out.println("Erwartungswert "+mean+" \n");
        // double variance = 0.0;
        // for (int y = yMin; y < yMax; y++)
        // {
        // variance = variance + (Math.pow((double)(y - mean), 2) *
        // (double)(rowProfile[y-yMin] / (double)totalSum));
        // }
        // System.out.println("Varianz "+variance+" \n Standardabweichung "+Math.sqrt(variance));
    }

    /**
     * Returns the smallest y-coordinate in column x
     */
    public int getTopAt(int x) {
        return topsAndBottomsOfWriting[x][0];
    }

    /**
     * Returns the largest y-coordinate in column x
     */
    public int getBotAt(int x) {
        return topsAndBottomsOfWriting[x][1];
    }

    /**
     * Returns the number of figure points in column x
     */
    public int getNumOfFigurePoints(int x) {
        return columnProfile[x];
    }

    /**
     * Returns whether <code>h</code> is higher than mean height + standard
     * deviation
     */
    public boolean isTall(int h) {
        return h > heightMax - (2 * heightStd);
    }

    /**
     * Returns the last position of the space character if there is no figure
     * pixel along the columns xStart to xEnd; in this case, the columns are
     * marked to be occupied including all other empty columns which directly
     * follow, in order to avoid two or more space characters which follow
     * directly each other.
     */
    public int isASpaceCharacter(int xStart, int xEnd) {
        if (isVacant(xStart, xEnd)) {
            int x = xStart;
            while (x < xMax && columnProfile[x] == 0)
                x++;
            boolean spaceCharacterFound = x >= xEnd;
            if (spaceCharacterFound) {
                occupy(xStart, x - 1);
                return x - 1;
            } else {
                return noSpaceCharacterFound;
            }
        } else
            return noSpaceCharacterFound;
    }

    /**
     * True, if the section x1 to x2 including boundaries are not occupied yet
     * any candidate glyphs or space characters.
     */
    public boolean isVacant(final int x1, final int x2) {
        int x = x1;
        while (x <= x2 && vacant[x])
            x++;

        return x - 1 == x2;
    }

    /**
     * True, if the section x1 to x2 including boundaries are not occupied yet
     * any candidate glyphs or space characters.
     */
    public boolean isVacantWithoutTolerance(final int x1, final int x2) {
        int x = x1;
        while (x <= x2 && vacantWithoutTolerance[x])
            x++;

        return x - 1 == x2;
    }

    /**
     * Marks section x1 to x2 to be occupied (including boundaries), but minus
     * 25% at the left side and minus 25% at the right hand side. However,
     * vacantWithoutTolerance stores the actual occupation.
     */
    public void occupy(final int x1, final int x2) {
        int quartile = (int) (0.25 * (x2 - x1));
        for (int x = x1 + quartile; x <= x2 - quartile; x++)
            vacant[x] = false;
        for (int x = x1; x <= x2; x++)
            vacantWithoutTolerance[x] = false;
    }
}