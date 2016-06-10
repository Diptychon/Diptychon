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

import java.util.ArrayList;
import java.util.Collections;

import javafx.geometry.Point2D;
import de.diptychon.models.algorithms.regionLabeling.ConnectedObjects;
import de.diptychon.models.algorithms.regionLabeling.LabelImage;
import de.diptychon.models.misc.GrayImage;
import de.diptychon.models.misc.ImageUtils;

public class RecursiveGlyphFragmentation {

    private static final int FOREGROUND = 1;

    private static final int BACKGROUND = 0;

    private static final int HORIZONTAL = 1;

    private static final int VERTICAL = 2;

    private GrayImage image;

    private int width;

    private int height;

    private int[][] labelArray;

    private int labelCount;

    private int fragLabelCount;

    private int[][] pixelArray;

    private int[][] extractedLabelArray;

    private int extractedLabelCount;

    private ArrayList<Integer> extractedFragLabels;

    private ArrayList<Integer> fragmentedLabels;

    private int charCount;

    private boolean debug;

    private float fragThres;

    private boolean singleFragment;

    private int pixelCount;

    /**
     * Creates a new RGF
     * 
     * @param image
     *            the binary image
     */
    public RecursiveGlyphFragmentation(final GrayImage image) {
        this.debug = false;
        this.image = image;
        this.pixelCount = 0;
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.extractedFragLabels = new ArrayList<Integer>();
        this.fragmentedLabels = new ArrayList<Integer>();
        this.pixelArray = new int[this.height + 2][this.width + 2];
        this.extractedLabelArray = new int[this.height + 2][this.width + 2];

        for (int y = 0; y < height; ++y) {
            final int offset = y * width;
            for (int x = 0; x < width; ++x) {
                final int index = offset + x;
                if (image.getPixelInt(index) == GrayImage.BLACK) {
                    this.pixelArray[y + 1][x + 1] = FOREGROUND;
                    this.pixelCount++;
                }
            }
        }
    }

    /**
     * Splits the image into fragments at a given separation line
     * 
     * @param sepLine
     *            the separation line
     */
    public ConnectedObjects getLineFragments(ArrayList<Point2D> sepLine) {
        LabelImage li = new LabelImage(this.pixelArray, 4);
        ConnectedObjects co = li.labelImage();
        int labelCount = co.getUsedLabelCount();

        int[][] pixelCopy = new int[this.height + 2][this.width + 2];

        for (int i = 0; i < pixelCopy.length; i++) {
            System.arraycopy(this.pixelArray[i], 0, pixelCopy[i], 0,
                    pixelCopy[i].length);
        }

        for (Point2D p : sepLine) {
            this.pixelArray[(int) Math.round(p.getY()) + 1][(int) Math.round(p
                    .getX()) + 1] = BACKGROUND;
        }

        li = new LabelImage(this.pixelArray, 4);
        co = li.labelImage();

        if (co.getUsedLabelCount() == labelCount) {
            return new ConnectedObjects(pixelCopy, co.getUsedLabelCount());
        }

        int[][] labelMatrix = co.getMatrix();

        // for (Point2D p : sepLine) {
        // labelMatrix[(int) Math.round(p.getY())+1][(int)
        // Math.round(p.getX())+1] = 1;
        // }

        for (Point2D p : sepLine) {
            int x = (int) Math.round(p.getX()) + 1;
            int y = (int) Math.round(p.getY()) + 1;

            int hmax = Math.max(labelMatrix[y][x - 1], labelMatrix[y][x + 1]);
            int hmin = Math.min(labelMatrix[y][x - 1], labelMatrix[y][x + 1]);
            int vmax = Math.max(labelMatrix[y - 1][x], labelMatrix[y + 1][x]);
            int vmin = Math.min(labelMatrix[y - 1][x], labelMatrix[y + 1][x]);
            int diamax1 = Math.max(labelMatrix[y - 1][x - 1],
                    labelMatrix[y + 1][x + 1]);
            int diamax2 = Math.max(labelMatrix[y - 1][x + 1],
                    labelMatrix[y + 1][x - 1]);
            if (vmax != 0) {
                labelMatrix[y][x] = vmax;
            } else if (hmax != 0) {
                labelMatrix[y][x] = hmax;
            } else if (diamax1 != 0) {
                labelMatrix[y][x] = diamax1;
            } else if (diamax2 != 0) {
                labelMatrix[y][x] = diamax2;
            }
        }

        return new ConnectedObjects(labelMatrix, co.getUsedLabelCount());
    }

    /**
     * Splits the image into squares
     * 
     * @param squareSize
     *            the size of the squares to create
     */
    public ConnectedObjects getSquareFragments(int squareSize) {
        int[][] labelMatrix = new int[this.height + 2][this.width + 2];
        int[][] fragLabels = new int[this.height + 2][this.width + 2];
        for (int y = 0; y < height + 2; ++y) {
            for (int x = 0; x < width + 2; ++x) {
                labelMatrix[y][x] = 1 + (y / squareSize * (this.width - 1) * 2 + x
                        / squareSize);
            }
        }
        for (int y = 0; y < height + 2; ++y) {
            for (int x = 0; x < width + 2; ++x) {
                if (this.pixelArray[y][x] != BACKGROUND) {
                    fragLabels[y][x] = labelMatrix[y + 1][x + 1];
                }
            }
        }

        return new ConnectedObjects(fragLabels, (this.height - 1)
                * (this.width - 1) + width - 1 / squareSize);

    }

    /**
     * Recursively fragmentates the image until the desired amount of fragments
     * has been created
     * 
     * @param charCount
     *            the number of characters in the image
     * 
     * @param fragThres
     *            the pixel threshold for fragments to create
     * 
     * @param singleFragment
     *            true if the RGF is supposed to further fragmentate a single
     *            given fragment
     */
    public ConnectedObjects getGlyphFragments(int charCount, float fragThres,
            boolean singleFragment, int noiseThres) {
        this.singleFragment = singleFragment;
        this.fragThres = fragThres;
        this.charCount = charCount;
        LabelImage li = new LabelImage(this.pixelArray, 4);
        ConnectedObjects co = li.labelImage();

        if (this.pixelCount == 1) {
            System.out.println("RGF benötigt Bild mit mehr als einem Pixel");
            return co;
        }

        // Zu kleine Regionen aus dem labelArray entfernen da Rauschen
        ArrayList<Integer> labels = co.getLabelList();
        ArrayList<Integer> remove = new ArrayList<Integer>();
        for (int i = 0; i < labels.size(); i++) {
            if (!singleFragment) {
                if (co.getLabelFrequency(labels.get(i)) < noiseThres) {
                    remove.add(labels.get(i));
                }
            } else {
                // if (co.getLabelFrequency(labels.get(i)) < 5) {
                // remove.add(labels.get(i));
                // }
            }
        }

        // Zu kleine Regionen aus dem pixelArray entfernen da Rauschen
        int[][] labelMatrix = co.getMatrix();

        for (int y = 0; y < labelMatrix.length; y++) {
            for (int x = 0; x < labelMatrix[y].length; x++) {
                if (remove.contains(labelMatrix[y][x])) {
                    this.pixelArray[y][x] = BACKGROUND;
                }
            }
        }

        for (int i = 0; i < remove.size(); i++) {
            co.removeLabel(remove.get(i));
        }

        this.labelArray = co.getMatrix();
        this.labelCount = co.getLabelCount();

        if (this.debug) {
            System.out.println("");
            System.out.println("INITIALES ARRAY MIT MARKIERTEN REGIONEN:");
            System.out.println("");
            for (int y = 0; y < this.labelArray.length; y++) {
                System.out.println("");
                for (int x = 0; x < this.labelArray[y].length; x++) {
                    System.out.print(this.labelArray[y][x]);
                }
            }
        }

        fragmentate();

        if (co.getUsedLabelCount() >= charCount) {
            System.out
                    .println("Wort bereits ausreichend fragmentiert. RGF wird nicht ausgeführt.");
            return co;
        }

        int recursions = 0;

        while (true) {
            if (!this.singleFragment) {
                if (recursions > charCount * 20) {
                    System.out
                            .println("Wort kann nicht weiter fragmentiert werden: Abgebrochen.");
                    break;
                }
            } else {
                // Bei singleFragment == true wird fragThres rekursiv
                // heruntergesetzt
                // Bevor zu kleine Fragmente erzeugt werden, abbrechen
                if (this.fragThres < 10) {
                    System.out
                            .println("Wort kann nicht weiter fragmentiert werden: Abgebrochen.");
                    break;
                }
                this.fragThres--;
            }

            if (this.extractedFragLabels.size() == 0) {
                li = new LabelImage(this.pixelArray, 4,
                        this.extractedLabelArray, 0);
            } else {
                li = new LabelImage(this.pixelArray, 4,
                        this.extractedLabelArray,
                        Collections.max(this.extractedFragLabels));
            }

            co = li.labelImage();

            if (co.getUsedLabelCount() >= charCount) {
                break;
            }

            this.labelArray = co.getMatrix();
            this.labelCount = co.getLabelCount();
            this.fragmentate();
            recursions++;
        }
        return co;
    }

    private void fragmentate() {
        // this.debug = true;
        int thres = 2;
        int[] occurences = new int[this.labelCount + 1];
        int biggestRegion = 0;

        for (int i = 0; i <= this.labelCount; i++) {
            occurences[i] = 0;
        }

        if (this.labelCount == 0) {
            System.out.println("Keine Regionen gefunden.");
            return;
        }
        // Größe der Regionen hochzählen
        for (int y = 0; y < this.labelArray.length; y++) {
            for (int x = 0; x < this.labelArray[y].length; x++) {
                occurences[this.labelArray[y][x]]++;
            }
        }

        // Größte Region suchen
        // 0-Regionen ignorieren, da diese Hintergrundpixel darstellen

        ArrayList<Integer> sortedRegions = new ArrayList<Integer>();

        int max = 0;

        for (int i = 1; i < occurences.length; i++) {
            if (occurences[i] > max) {
                sortedRegions.add(i);
                max = occurences[i];
                biggestRegion = i;
            }
        }

        boolean seenBefore = false;

        // Prüfen, ob zu bearbeitendes Fragment bereits zuvor durch RGF
        // bearbeitet wurde
        if (this.extractedFragLabels.contains(biggestRegion)) {
            seenBefore = true;
            this.fragmentedLabels.add(biggestRegion);
            // Regionen nicht öfter als viermal (>3) bearbeiten
            if (!singleFragment) {
                while (Collections.frequency(this.fragmentedLabels,
                        biggestRegion) > 3) {
                    if (sortedRegions.indexOf(biggestRegion) != 0) {
                        seenBefore = false;
                        biggestRegion = sortedRegions.get(sortedRegions
                                .indexOf(biggestRegion) - 1);
                        if (this.extractedFragLabels.contains(biggestRegion)) {
                            this.fragmentedLabels.add(biggestRegion);
                            seenBefore = true;
                        } else {
                            break;
                        }
                    } else {
                        biggestRegion = sortedRegions
                                .get(sortedRegions.size() - 1);
                        seenBefore = true;
                        break;
                    }
                }
            }
            if (seenBefore) {
                thres = (int) (thres + 2 * (Collections.frequency(
                        this.fragmentedLabels, biggestRegion)));
            }

        }

        // Bounding Box Koordinaten für größte Region finden
        int minX = Integer.MAX_VALUE, maxX = 0, minY = Integer.MAX_VALUE, maxY = 0;

        for (int y = 0; y < this.labelArray.length; y++) {
            for (int x = 0; x < this.labelArray[y].length; x++) {
                if (this.labelArray[y][x] == biggestRegion) {
                    if (x < minX) {
                        minX = x;
                    }
                    if (y < minY) {
                        minY = y;
                    }
                    if (x > maxX) {
                        maxX = x;
                    }
                    if (y > maxY) {
                        maxY = y;
                    }
                }
            }
        }

        minX--;
        minY--;

        if (minX < 0) {
            minX = 0;
        }

        if (minY < 0) {
            minY = 0;
        }

        int width = maxX - minX;
        int height = maxY - minY;

        // Bild des Wortes auf Bereich der größten Region zuschneiden
        final byte[] pixels = ImageUtils.cropImage(this.image.getPixels(),
                this.image.getWidth(), this.image.getHeight(), minX, minY,
                width, height);
        final GrayImage regionImage = new GrayImage(pixels, width, height);

        int[][] regionArray = new int[height + 2][width + 2];
        int[][] separationArray = new int[height + 2][width + 2];
        int[][] sepLineArray = new int[height + 2][width + 2];
        int[][] testArray = new int[height + 2][width + 2];
        int[][] testArray2 = new int[height + 2][width + 2];
        int[][] pixelSepLineArray = new int[height + 2][width + 2];

        for (int y = 0; y < height; ++y) {
            final int offset = y * width;
            for (int x = 0; x < width; ++x) {
                final int index = offset + x;
                if (regionImage.getPixelInt(index) == GrayImage.BLACK) {
                    regionArray[y + 1][x + 1] = FOREGROUND;
                }
            }
        }

        // Ungewollte Regionen innerhalb des Regionen-Arrays entfernen
        for (int y = 0; y < regionArray.length; y++) {
            for (int x = 0; x < regionArray[y].length; x++) {
                if (this.labelArray[y + minY][x + minX] != biggestRegion) {
                    regionArray[y][x] = BACKGROUND;
                }
            }
        }

        for (int i = 0; i < regionArray.length; i++) {
            System.arraycopy(regionArray[i], 0, pixelSepLineArray[i], 0,
                    regionArray[i].length);
        }

        // ZUM TESTEN
        for (int i = 0; i < regionArray.length; i++) {
            System.arraycopy(regionArray[i], 0, testArray[i], 0,
                    regionArray[i].length);
        }
        for (int i = 0; i < regionArray.length; i++) {
            System.arraycopy(regionArray[i], 0, testArray2[i], 0,
                    regionArray[i].length);
        }
        // ZUM TESTEN

        // Horizontale zusammenhängende Pixel unter Schwellwert suchen und ggf.
        // Trennpixel eintragen
        int countingStart = 0;
        int hthres;

        // Horizontalen Schwellwert runtersetzen, wenn nur einzelne Fragmente
        // fragmentiert werden sollen
        if (this.singleFragment) {
            hthres = 3;
        } else {
            hthres = 5;
        }

        for (int y = 0; y < regionArray.length; y++) {
            boolean counting = false;
            for (int x = 0; x < regionArray[y].length; x++) {
                if (regionArray[y][x] == FOREGROUND) {
                    if (!counting) {
                        counting = true;
                        countingStart = x;
                    }
                }
                if (regionArray[y][x] == BACKGROUND) {
                    if (counting) {
                        // Schwellwert signifikant verringern, da Glyphen selten
                        // horizontal getrennt werden müssen
                        if (x - countingStart + 1 < thres / hthres) {
                            for (int x2 = countingStart; x2 < x; x2++) {
                                separationArray[y][x2] = HORIZONTAL;
                                // ZUM TESTEN
                                testArray2[y][x2] = 2;
                                // ZUM TESTEN
                            }
                        }
                        counting = false;
                    }
                }
            }
        }

        // Vertikal zusammenhängende Pixel unter Schwellwert suchen und ggf.
        // Trennpixel eintragen
        for (int x = 0; x < regionArray[0].length; x++) {
            boolean counting = false;
            for (int y = 0; y < regionArray.length; y++) {
                if (regionArray[y][x] == FOREGROUND) {
                    if (!counting) {
                        counting = true;
                        countingStart = y;
                    }
                }
                if (regionArray[y][x] == BACKGROUND) {
                    if (counting) {
                        if (y - countingStart + 1 < thres) {
                            for (int y2 = countingStart; y2 < y; y2++) {
                                separationArray[y2][x] = VERTICAL;
                                // ZUM TESTEN
                                testArray2[y2][x] = 3;
                                // ZUM TESTEN
                            }
                        }
                        counting = false;
                    }
                }
            }
        }

        // Trennlinien bei Breite/2 der vertikalen Trennregionen zeichnen
        for (int y = 0; y < separationArray.length; y++) {
            boolean counting = false;
            for (int x = 0; x < separationArray[y].length; x++) {
                if (separationArray[y][x] == VERTICAL) {
                    if (!counting) {
                        counting = true;
                        countingStart = x;
                    }
                }
                if (separationArray[y][x] != VERTICAL) {
                    if (counting) {
                        sepLineArray[y][countingStart
                                + ((x - countingStart) / 2)] = VERTICAL;
                        pixelSepLineArray[y][countingStart
                                + ((x - countingStart) / 2)] = 0;

                        // Prüfen, ob Verbindung mit Trennregion in vorheriger
                        // Zeile besteht
                        if (separationArray[y - 1][countingStart
                                + ((x - countingStart) / 2) - 1] == VERTICAL
                                && separationArray[y - 1][countingStart
                                        + ((x - countingStart) / 2)] == VERTICAL
                                && separationArray[y - 1][countingStart
                                        + ((x - countingStart) / 2) + 1] == VERTICAL) {
                            if (sepLineArray[y - 1][countingStart
                                    + ((x - countingStart) / 2) - 1] != VERTICAL
                                    && sepLineArray[y - 1][countingStart
                                            + ((x - countingStart) / 2)] != VERTICAL
                                    && sepLineArray[y - 1][countingStart
                                            + ((x - countingStart) / 2) + 1] != VERTICAL) {
                                sepLineArray[y - 1][countingStart
                                        + ((x - countingStart) / 2)] = VERTICAL;
                                pixelSepLineArray[y - 1][countingStart
                                        + ((x - countingStart) / 2)] = 0;
                            }
                        }

                        // ZUM TESTEN
                        testArray[y][countingStart + ((x - countingStart) / 2)] = 3;
                        // ZUM TESTEN

                        counting = false;
                    }
                }
            }
        }

        // Trennlinien bei Höhe/2 der horizontalen Trennregionen zeichnen
        for (int x = 0; x < separationArray[0].length; x++) {
            boolean counting = false;
            for (int y = 0; y < separationArray.length; y++) {
                if (separationArray[y][x] == HORIZONTAL) {
                    if (!counting) {
                        counting = true;
                        countingStart = y;
                    }
                }
                if (separationArray[y][x] != HORIZONTAL) {
                    if (counting) {
                        sepLineArray[countingStart
                                + ((y - 1 - countingStart) / 2)][x] = HORIZONTAL;
                        pixelSepLineArray[countingStart
                                + ((y - 1 - countingStart) / 2)][x] = 0;
                        // ZUM TESTEN
                        testArray[countingStart + ((y - 1 - countingStart) / 2)][x] = 2;
                        // ZUM TESTEN
                        counting = false;
                    }
                }
            }
        }

        if (this.debug) {
            System.out.println("");
            System.out.println("");
            System.out
                    .println("ARRAY MIT TRENNREGIONEN: (2 = horizontale Trennregion, 3 = vertikale Trennregion)");
            for (int y = 0; y < testArray2.length; y++) {
                System.out.println("");
                for (int x = 0; x < testArray2[y].length; x++) {
                    System.out.print(testArray2[y][x]);
                }
            }

            System.out.println("");
            System.out.println("");
            System.out
                    .println("ARRAY MIT TRENNLINIEN: (2 = horizontale Trennlinien, 3 = vertikale Trennlinien)");
            for (int y = 0; y < testArray.length; y++) {
                System.out.println("");
                for (int x = 0; x < testArray[y].length; x++) {
                    System.out.print(testArray[y][x]);
                }
            }
        }

        final LabelImage li;

        if (this.extractedFragLabels.size() == 0) {
            li = new LabelImage(pixelSepLineArray, 4, 1);
        } else {
            li = new LabelImage(pixelSepLineArray, 4,
                    Collections.max(this.extractedFragLabels) + 1);
        }
        ConnectedObjects co = li.labelImage();

        int[][] fragLabelArray = co.getMatrix();
        this.fragLabelCount = co.getLabelCount();

        int notReconstructed = 1;
        int recursions = 0;

        while (notReconstructed > 0) {
            if (recursions > 100000) {
                System.out
                        .println("Warnung: Trennrisse konnten nicht vollständig rekonstruiert werden.");
                break;
            }
            notReconstructed = 0;
            // Bruchstellen im Fragment Array rekonstruieren
            for (int y = 0; y < sepLineArray.length; y++) {
                for (int x = 0; x < sepLineArray[y].length; x++) {
                    if (sepLineArray[y][x] == HORIZONTAL) {
                        int hmax = Math.max(fragLabelArray[y][x - 1],
                                fragLabelArray[y][x + 1]);
                        int hmin = Math.min(fragLabelArray[y][x - 1],
                                fragLabelArray[y][x + 1]);
                        int vmax = Math.max(fragLabelArray[y - 1][x],
                                fragLabelArray[y + 1][x]);
                        int vmin = Math.min(fragLabelArray[y - 1][x],
                                fragLabelArray[y + 1][x]);
                        int diamax1 = Math.max(fragLabelArray[y - 1][x - 1],
                                fragLabelArray[y + 1][x + 1]);
                        int diamax2 = Math.max(fragLabelArray[y - 1][x + 1],
                                fragLabelArray[y + 1][x - 1]);
                        if (vmax != 0) {
                            fragLabelArray[y][x] = vmax;
                            sepLineArray[y][x] = BACKGROUND;
                        } else if (hmax != 0) {
                            fragLabelArray[y][x] = hmax;
                            sepLineArray[y][x] = BACKGROUND;
                        } else if (diamax1 != 0) {
                            fragLabelArray[y][x] = diamax1;
                            sepLineArray[y][x] = BACKGROUND;
                        } else if (diamax2 != 0) {
                            fragLabelArray[y][x] = diamax2;
                            sepLineArray[y][x] = BACKGROUND;
                        } else {
                            notReconstructed++;
                        }
                    } else if (sepLineArray[y][x] == VERTICAL) {
                        int hmax = Math.max(fragLabelArray[y][x - 1],
                                fragLabelArray[y][x + 1]);
                        int hmin = Math.min(fragLabelArray[y][x - 1],
                                fragLabelArray[y][x + 1]);
                        int vmax = Math.max(fragLabelArray[y - 1][x],
                                fragLabelArray[y + 1][x]);
                        int vmin = Math.min(fragLabelArray[y - 1][x],
                                fragLabelArray[y + 1][x]);
                        int diamax1 = Math.max(fragLabelArray[y - 1][x - 1],
                                fragLabelArray[y + 1][x + 1]);
                        int diamax2 = Math.max(fragLabelArray[y - 1][x + 1],
                                fragLabelArray[y + 1][x - 1]);
                        if (hmax != 0) {
                            fragLabelArray[y][x] = hmax;
                            sepLineArray[y][x] = BACKGROUND;
                        } else if (vmax != 0) {
                            fragLabelArray[y][x] = vmax;
                            sepLineArray[y][x] = BACKGROUND;
                        } else if (diamax1 != 0) {
                            fragLabelArray[y][x] = diamax1;
                            sepLineArray[y][x] = BACKGROUND;
                        } else if (diamax2 != 0) {
                            fragLabelArray[y][x] = diamax2;
                            sepLineArray[y][x] = BACKGROUND;
                        } else {
                            notReconstructed++;
                        }
                    }
                }
            }
            recursions++;
        }

        if (this.debug) {
            System.out.println("");
            System.out.println("");
            System.out
                    .println("ARRAY MIT NEUER FRAGMENTIERUNG: (Regionen mit nur einer Ziffer wird 0 vorangestellt, Ausgabe ist daher in die Breite gestreckt)");
            for (int y = 0; y < fragLabelArray.length; y++) {
                System.out.println("");
                for (int x = 0; x < fragLabelArray[y].length; x++) {
                    if (fragLabelArray[y][x] < 10) {
                        System.out.print("0");
                    }
                    System.out.print(fragLabelArray[y][x]);
                }
            }
        }

        ArrayList<Integer>[] fragNeighbours = (ArrayList<Integer>[]) new ArrayList[fragLabelCount + 1];
        for (int i = 0; i < fragNeighbours.length; i++) {
            fragNeighbours[i] = new ArrayList<Integer>();
        }

        int[] fOccurences = new int[this.fragLabelCount + 1];

        for (int i = 0; i <= this.fragLabelCount; i++) {
            fOccurences[i] = 0;
        }

        if (this.fragLabelCount == 0) {
            System.out.println("Keine fragmentierten Regionen gefunden.");
            return;
        }
        // Größe der fragmentierten Regionen hochzählen
        for (int y = 0; y < fragLabelArray.length; y++) {
            for (int x = 0; x < fragLabelArray[y].length; x++) {
                fOccurences[fragLabelArray[y][x]]++;
            }
        }

        // Unzureichend große Fragmente suchen
        // 0-Regionen ignorieren, da diese Hintergrundpixel darstellen
        ArrayList<Integer> invalidFrags = new ArrayList<Integer>();

        // Größenschwellwert
        int fThres = (int) this.fragThres;

        for (int i = 1; i < fOccurences.length; i++) {
            if (fOccurences[i] < fThres && fOccurences[i] != 0) {
                invalidFrags.add(i);
            }
        }

        int labelsLeft = 0;

        recursions = 0;

        while (invalidFrags.size() > 0) {
            if (recursions > this.fragLabelCount) {
                System.out
                        .println("Alle Fragmente der Region zu klein, um ausreichend große Region zu erzeugen: Abgebrochen");
                break;
            }

            for (int i = 0; i < fragNeighbours.length; i++) {
                fragNeighbours[i] = new ArrayList<Integer>();
            }

            // Nachbarn aller Regionen suchen
            for (int y = 1; y < fragLabelArray.length - 1; y++) {
                for (int x = 1; x < fragLabelArray[y].length - 1; x++) {
                    if (fragLabelArray[y][x] != BACKGROUND) {
                        int[] neighbours = { fragLabelArray[y - 1][x],
                                fragLabelArray[y][x + 1],
                                fragLabelArray[y + 1][x],
                                fragLabelArray[y][x - 1] };

                        for (int i = 0; i < neighbours.length; i++) {
                            if (neighbours[i] != 0
                                    && neighbours[i] != fragLabelArray[y][x]
                                    && !fragNeighbours[fragLabelArray[y][x]]
                                            .contains(neighbours[i])) {
                                fragNeighbours[fragLabelArray[y][x]]
                                        .add(neighbours[i]);
                            }
                        }
                    }
                }
            }

            if (this.debug) {
                System.out.println("INVALID: " + invalidFrags.size());
            }

            // Kleine Fragmente mit Nachbarfragment verschmelzen
            for (int y = 0; y < fragLabelArray.length; y++) {
                for (int x = 0; x < fragLabelArray[y].length; x++) {
                    if (invalidFrags.get(0) == (fragLabelArray[y][x])) {
                        int nmin = Integer.MAX_VALUE;
                        int nmini = 0;
                        int nCount = fragNeighbours[fragLabelArray[y][x]]
                                .size();
                        for (int i = 0; i < nCount; i++) {
                            int nFreq = fOccurences[fragNeighbours[fragLabelArray[y][x]]
                                    .get(i)];
                            if (nFreq < nmin && nFreq != 0) {
                                nmin = nFreq;
                                nmini = i;
                            }

                        }
                        if (nCount != 0) {
                            fragLabelArray[y][x] = fragNeighbours[fragLabelArray[y][x]]
                                    .get(nmini);
                        }
                    }
                }
            }

            for (int i = 0; i <= this.fragLabelCount; i++) {
                fOccurences[i] = 0;
            }

            if (this.fragLabelCount == 0) {
                System.out.println("Keine fragmentierten Regionen gefunden.");
                return;
            }
            // Größe der fragmentierten Regionen hochzählen
            for (int y = 0; y < fragLabelArray.length; y++) {
                for (int x = 0; x < fragLabelArray[y].length; x++) {
                    fOccurences[fragLabelArray[y][x]]++;
                }
            }

            // Unzureichend große Fragmente suchen
            // 0-Regionen ignorieren, da diese Hintergrundpixel darstellen
            invalidFrags = new ArrayList<Integer>();

            // Fragmente mit Pixelzahl unter Größenschwellwert suchen
            // Tatsächliche Anzahl verbliebener Fragmente berechnen

            labelsLeft = 0;
            for (int i = 1; i < fOccurences.length; i++) {
                if (fOccurences[i] > 0) {
                    labelsLeft++;
                }
                if (fOccurences[i] < fThres && fOccurences[i] != 0) {
                    invalidFrags.add(i);
                }
            }
            if (this.debug) {
                for (int y = 0; y < fragLabelArray.length; y++) {
                    System.out.println("");
                    for (int x = 0; x < fragLabelArray[y].length; x++) {
                        if (fragLabelArray[y][x] < 10
                                && fragLabelArray[y][x] != 0) {
                            System.out.print("X");
                        } else if (fragLabelArray[y][x] < 10) {
                            System.out.print("0");
                        }
                        System.out.print(fragLabelArray[y][x]);
                    }
                }
            }
            recursions++;
        }

        if (this.debug) {
            System.out.println("");
            System.out.println("");
            System.out
                    .println("ARRAY MIT FRAGMENTIERUNG NACH VERSCHMELZUNG ZU KLEINER FRAGMENTE:");
            for (int y = 0; y < fragLabelArray.length; y++) {
                System.out.println("");
                for (int x = 0; x < fragLabelArray[y].length; x++) {
                    if (fragLabelArray[y][x] < 10) {
                        System.out.print("0");
                    }
                    System.out.print(fragLabelArray[y][x]);
                }
            }
        }

        // Anzahl bereits extrahierter Fragmente berechnen
        ArrayList<Integer> fragLabels = new ArrayList<Integer>();

        for (int y = 0; y < fragLabelArray.length; y++) {
            for (int x = 0; x < fragLabelArray[y].length; x++) {
                if (!fragLabels.contains(fragLabelArray[y][x])
                        && fragLabelArray[y][x] != BACKGROUND) {
                    fragLabels.add(fragLabelArray[y][x]);
                }
            }
        }

        // Prüfen, ob fragmentiert wurde
        boolean noChange = (fragLabels.size() == 1);

        if (noChange && seenBefore) {
            return;
        }

        // Rekursionsergebnis in globales RGF-Ergebnis-Array schreiben
        for (int y = minY; y < maxY + 1; y++) {
            for (int x = minX; x < maxX + 1; x++) {
                if (fragLabelArray[y - minY][x - minX] != BACKGROUND) {
                    this.extractedLabelArray[y][x] = fragLabelArray[y - minY][x
                            - minX];
                }
            }
        }

        if (this.debug) {
            System.out.println("");
            System.out.println("");
            System.out.println("EXTRACTED LABEL ARRAY:");
            for (int y = 0; y < this.extractedLabelArray.length; y++) {
                System.out.println("");
                for (int x = 0; x < this.extractedLabelArray[y].length; x++) {
                    if (this.extractedLabelArray[y][x] < 10) {
                        System.out.print("0");
                    }
                    System.out.print(this.extractedLabelArray[y][x]);
                }
            }
        }

        // Anzahl global bereits extrahierter Fragmente neuberechnen
        this.extractedFragLabels.clear();

        for (int y = 0; y < this.extractedLabelArray.length; y++) {
            for (int x = 0; x < this.extractedLabelArray[y].length; x++) {
                if (!this.extractedFragLabels
                        .contains(this.extractedLabelArray[y][x])
                        && this.extractedLabelArray[y][x] != BACKGROUND) {
                    this.extractedFragLabels
                            .add(this.extractedLabelArray[y][x]);
                }
            }
        }
        this.extractedLabelCount = extractedFragLabels.size();
    }
}
