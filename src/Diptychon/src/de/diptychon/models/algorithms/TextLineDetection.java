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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import javafx.scene.shape.Line;
import de.diptychon.DiptychonLogger;
import de.diptychon.models.algorithms.contourExtraction.ContourSet;
import de.diptychon.models.algorithms.contourExtraction.ContourTracer;
import de.diptychon.models.data.ImageLine;
import de.diptychon.models.misc.GrayImage;

/**
 * Dient zur Erkennung der Basislinien eines Textdokumentes
 */
public class TextLineDetection {
    /** Schwellwert zur vertikalen Trennung von Bounding Boxes */
    private static final double SEPARATION_THRESHOLD = 0.25;

    /**
     * Maximale Entfernung von Bounding Boxes in einer Reihe (ausgehend von der
     * Breite des Bildes, 1 = 100%)
     */
    private static final double MAX_HORIZONTAL_GAP = 0.09;

    /** Mindesthoehe aller Bounding Boxes in Pixeln */
    private static final int MIN_HEIGHT = 15;

    /** Mindesthoehe aller Bounding Boxes in Pixeln */
    private static final int MIN_WIDTH = 10;

    /**
     * Parameter to define the number of neighbors which should be checked
     * whether the current line lays inside or not
     */
    private static final int RANGE_CHECK = 5;

    /**
     * The binary image in which the lines are searched for
     */
    private final GrayImage binaryImage;

    /**
     * The width of the binary image
     */
    private final int width;

    /**
     * The height of the binary image
     */
    private final int height;

    /**
     * Constructor
     * 
     * @param binImage
     *            the binary image in which lines should be searched for
     */
    public TextLineDetection(final GrayImage binImage) {
        this.binaryImage = binImage;
        this.width = binImage.getWidth();
        this.height = binImage.getHeight();
    }

    /**
     * Gibt eine Liste von Linien zurueck, die potenzielle Basislinien eines
     * Textes darstellen.
     * 
     * @return Liste der gefundenen Linien
     */
    public LinkedList<ImageLine> detectLines() {
        DiptychonLogger.debug("Find regions");
        final ContourTracer ct = new ContourTracer(this.binaryImage);
        final ContourSet cs = ct.getContours();
        /** Liste aller Bounding Boxes */
        final LinkedList<RectangularRegion> regions = new LinkedList<>();
        final double averageGlyphHeight = cs.getRegions(regions, MIN_HEIGHT,
                MIN_WIDTH);

        DiptychonLogger.debug("Separate Boxes");
        /** Neue Liste f�r die separierten und gefilterten Boxen */
        final LinkedList<RectangularRegion> filteredBoxes = this.separateBoxes(
                regions, averageGlyphHeight);

        // Sortiere die neuen Bounding Boxes anhand der x-Koordinate
        Collections.sort(filteredBoxes);

        DiptychonLogger.debug("Merge Boxes to rows");
        // Weise den Bounding Boxes Reihen zu
        final LinkedList<Row> rows = this.createRows(filteredBoxes,
                (int) (this.width * MAX_HORIZONTAL_GAP));

        DiptychonLogger.debug("Calculate baselines");
        /** Ergebnisliste, die alle Linien enthält */
        final LinkedList<Line> lines = this.calculateBaselines(rows);

        if (lines.size() == 0) {
            DiptychonLogger.debug("No lines found");
            return new LinkedList<ImageLine>();
        } else {
            DiptychonLogger.debug("Shift baselines to find upper/lower line");
            final LinkedList<ImageLine> extractedLines = this
                    .sortLinesForLineShift(lines, averageGlyphHeight);
            DiptychonLogger.debug("Filter lines contained in others");
            return this.sortOutSmallLinesContainedInOthers(extractedLines);
        }
    }

    /**
     * Calculates the baseline for all rows. For details see Christophs master
     * thesis
     * 
     * @param rows
     *            the rows
     * @return all found lines
     */
    private LinkedList<Line> calculateBaselines(final LinkedList<Row> rows) {
        final LinkedList<Line> lines = new LinkedList<>();
        // Durchlaufe alle Reihen, finde dabei abschliessende Eckenpaare und
        // bestimme die Basislinie
        for (final Row row : rows) {
            // Liste der Extrempunkte
            final LinkedList<Pixel> convexExtremes = new LinkedList<Pixel>();

            // Boxen durchlaufen und in jeder nach dem abschliessenden Eckenpaar
            // suchen
            for (final RectangularRegion box : row.boxes) {
                // Enthaelt Information, wo sich eine horizontale Kante befindet
                final boolean[] edges = new boolean[(box.x2 - box.x1) + 2];
                // Anfang und Ende einer Kante
                int edgeStart = 0, edgeEnd = 0;

                for (int y = box.y1; y <= box.y2; ++y) {
                    for (int x = box.x1; x <= box.x2; ++x) {
                        if (y + 1 < this.height) {
                            // Wenn XOR 1 ergibt: true
                            if ((this.binaryImage.getPixel(x, y) == GrayImage.BLACK && this.binaryImage
                                    .getPixel(x, y + 1) != GrayImage.BLACK)
                                    || (this.binaryImage.getPixel(x, y) != GrayImage.BLACK && this.binaryImage
                                            .getPixel(x, y + 1) == GrayImage.BLACK)) {
                                edges[x - box.x1] = true;
                            } else {
                                edges[x - box.x1] = false;
                            }
                        }
                    }
                    // true wenn eine Kante begonnen hat
                    boolean started = false;
                    // Pruefe, wo Kanten anfangen und aufhoeren
                    for (int i = 0; i < edges.length; ++i) {
                        if (edges[i] && !started) {
                            edgeStart = (i - 1) + box.x1;
                            started = true;
                        } else if (!edges[i] && started) {
                            edgeEnd = (i - 1) + box.x1;
                            // Abschliessendes Eckenpaar erkennen
                            if (this.binaryImage.getPixel(edgeStart, y) != GrayImage.BLACK
                                    && this.binaryImage.getPixel(edgeStart + 1,
                                            y) == GrayImage.BLACK
                                    && this.binaryImage.getPixel(edgeStart,
                                            y + 1) != GrayImage.BLACK
                                    && this.binaryImage.getPixel(edgeStart + 1,
                                            y + 1) != GrayImage.BLACK
                                    && this.binaryImage.getPixel(edgeEnd, y) == GrayImage.BLACK
                                    && this.binaryImage
                                            .getPixel(edgeEnd + 1, y) != GrayImage.BLACK
                                    && this.binaryImage
                                            .getPixel(edgeEnd, y + 1) != GrayImage.BLACK
                                    && this.binaryImage.getPixel(edgeEnd + 1,
                                            y + 1) != GrayImage.BLACK) {

                                // Konvexen Extrempunkt mit Gewichtung in der
                                // Mitte setzen
                                convexExtremes
                                        .add(new Pixel(
                                                ((edgeStart + 1) + ((edgeEnd - (edgeStart + 1)) / 2)),
                                                y,
                                                (edgeEnd - (edgeStart + 1)) + 1));
                            }
                            started = false;
                        }

                    }

                }
            }

            // Methode der kleinsten Quadrate mit Filterung nach Caesar et al.
            if (convexExtremes.size() > 2) {
                // Dient dazu, dass nur beim ersten Durchlauf der Schwellwert
                // zum Abbrechen bestimmt wird
                boolean firstrun = true;
                // Am weitesten entfernten Pixel zum Loeschen merken
                Pixel worstPixel = null;
                // Durchschnittliche Distanz, die jeder Punkt erfuellen muss
                int averageDistance = 0;

                // Steigung und Hoehe der Linie
                double alpha1 = 0, alpha0 = 0;
                do {
                    int n = 0;
                    worstPixel = null;
                    // aufsummierte X- und Y-Werte
                    double sumx = 0, sumy = 0;
                    for (final Pixel p : convexExtremes) {
                        sumx += p.x * p.weight;
                        sumy += p.y * p.weight;
                        n += p.weight;
                    }
                    // Durchschnitt x und y
                    final double xbar = sumx / n;
                    final double ybar = sumy / n;
                    // Hilfsvariablen fuer eine uebersichtlichere Rechnung
                    double top = 0, bottom = 0;

                    // Formel nach Wikipedia zur Berechnung
                    for (final Pixel p : convexExtremes) {
                        for (int i = 0; i < p.weight; ++i) {
                            top += (p.x - xbar) * (p.y - ybar);
                            bottom += (p.x - xbar) * (p.x - xbar);
                        }
                    }
                    if (bottom != 0) {
                        alpha1 = top / bottom;
                    } else {
                        alpha1 = 0;
                    }

                    alpha0 = ybar - alpha1 * xbar;

                    // Distanzen berechnen und ggf. Pixel zum Loeschen merken
                    for (final Pixel p : convexExtremes) {
                        p.distance = Math.pow((alpha1 * p.x + alpha0) - p.y, 2);
                        if (firstrun) {
                            averageDistance += p.distance;
                        }

                        if (worstPixel == null
                                || p.distance > worstPixel.distance) {
                            worstPixel = p;
                        }
                    }
                    if (firstrun) {
                        if (convexExtremes.size() > 0) {
                            averageDistance /= n;
                        }
                        firstrun = false;
                    }

                    convexExtremes.remove(worstPixel);
                } while (convexExtremes.size() > 2 && worstPixel != null
                        && worstPixel.distance > averageDistance * 0.7);

                // Linie hinzufuegen: Falls zu extrem auf den Bereich der Bbx
                // begrenzen
                if ((int) (alpha1 * row.left + alpha0) <= row.bottom
                        && (int) (alpha1 * row.left + alpha0) >= row.top
                        && (int) (alpha1 * row.right + alpha0) <= row.bottom
                        && (int) (alpha1 * row.right + alpha0) >= row.top) {
                    lines.add(new Line(row.left,
                            (int) (alpha1 * row.left + alpha0), row.right,
                            (int) (alpha1 * row.right + alpha0)));
                } // zu extrem: begrenzen
                else {
                    // Anfang und Ende der Linie (horizontal)
                    int start = 0;
                    int end = 0;
                    // Anfangspunkt finden
                    for (int x = row.left; x <= row.right; ++x) {
                        if ((int) (alpha1 * x + alpha0) <= row.bottom
                                && (int) (alpha1 * x + alpha0) >= row.top) {
                            start = x;
                            break;
                        }
                    }
                    // Endpunkt finden
                    for (int x = row.right; x >= row.left; --x) {
                        if ((int) (alpha1 * x + alpha0) <= row.bottom
                                && (int) (alpha1 * x + alpha0) >= row.top) {
                            end = x;
                            break;
                        }
                    }
                    lines.add(new Line(start, (int) (alpha1 * start + alpha0),
                            end, (int) (alpha1 * end + alpha0)));
                }
            }
        }
        return lines;
    }

    /**
     * Separates the found boxes
     * 
     * @param regions
     *            the found boxes
     * @param averageGlyphHeight
     *            the average glyph height
     * @return all separated boxes
     */
    private LinkedList<RectangularRegion> separateBoxes(
            final LinkedList<RectangularRegion> regions,
            final double averageGlyphHeight) {
        final LinkedList<RectangularRegion> filteredBoxes = new LinkedList<RectangularRegion>();
        // Bounding Boxes zur Separierung durchlaufen
        for (final RectangularRegion bbx : regions) {
            // die Anzahl des hoechsten Aufkommens schwarzer Pixel
            int mostPixels = 0;
            // Speichert den Anfang einer neuen, separierten Box
            int startBox = -1;
            // Anzahl schwarzer Pixel fuer jede Reihe
            final int[] blackPixels = new int[(bbx.y2 - bbx.y1) + 1];

            // Zunächst zahlen, wieviele schwarze Pixel in welcher Zeile
            // vorhanden sind
            for (int y = bbx.y1; y <= bbx.y2; ++y) {
                for (int x = bbx.x1; x <= bbx.x2; ++x) {
                    if (this.binaryImage.getPixel(x, y) == GrayImage.BLACK) {
                        blackPixels[y - bbx.y1]++;
                    }
                }
                // Wenn die schwarzen Pixel groesser sind als das bisherige
                // Maximum: neues Maximum
                if (blackPixels[y - bbx.y1] > mostPixels) {
                    mostPixels = blackPixels[y - bbx.y1];
                }
            }
            // Bounding Box erneut durchlaufen, nun zur Trennung
            for (int y = bbx.y1; y <= bbx.y2; ++y) {
                // Wenn noch keine neue Box angefangen wurde und das Aufkommen
                // schwarzer Pixel hoeher als der
                // Schwellwert
                // ist: Box starten
                if (blackPixels[y - bbx.y1] > mostPixels * SEPARATION_THRESHOLD
                        && startBox == -1) {
                    startBox = y;
                }
                // Falls eine Box angefangen wurde und der Schwellwert
                // unterschritten wird: Box beenden
                else if (blackPixels[y - bbx.y1] <= mostPixels
                        * SEPARATION_THRESHOLD
                        && startBox != -1) {
                    if (y - startBox >= MIN_HEIGHT
                            && (y + 3) - startBox >= averageGlyphHeight / 3
                            && (y + 3) - startBox <= averageGlyphHeight * 3) {
                        if (y + 3 < this.height) {
                            filteredBoxes.add(new RectangularRegion(bbx.x1,
                                    startBox, bbx.x2, y + 3));
                        } else {
                            filteredBoxes.add(new RectangularRegion(bbx.x1,
                                    startBox, bbx.x2, this.height - 1));
                        }
                        y += 2;
                        startBox = -1;
                    }
                }
                // Falls das Ende der Box erreicht ist und eine Box angefangen
                // wurde: Box abschliessen
                else if (y == bbx.y2 && startBox != -1
                        && y - startBox >= MIN_HEIGHT
                        && y - startBox >= averageGlyphHeight / 3
                        && y - startBox <= averageGlyphHeight * 3) {
                    filteredBoxes.add(new RectangularRegion(bbx.x1, startBox,
                            bbx.x2, y));
                }
            }
        }
        return filteredBoxes;
    }

    /**
     * Sorts the lines according to their y coordinate and shift the baslines to
     * find upper and lowerline afterwards
     * 
     * @param lines
     *            the lines to be sorted and shifted
     * @param averageGlyphHeight
     *            the average glyph height
     * @return the resulting ImageLines
     */
    private LinkedList<ImageLine> sortLinesForLineShift(
            final LinkedList<Line> lines, final double averageGlyphHeight) {
        Collections.sort(lines, new Comparator<Line>() {
            @Override
            public int compare(final Line o1, final Line o2) {
                if (o1.getStartY() < o2.getStartY()) {
                    return -1;
                } else if (o1.getStartY() == o2.getStartY()) {
                    if (o1.getStartX() < o2.getStartX()) {
                        return -1;
                    } else if (o1.getStartX() == o2.getStartX()) {
                        return 0;
                    } else {// if(o1.getStartX() > o2.getStartX()) {
                        return 1;
                    }
                } else {// if (o1.getStartY() > o2.getStartY()) {
                    return 1;
                }
            }
        });
        if (lines.size() == 0) {
            return new LinkedList<ImageLine>();
        } else {
            return this.shiftBaseline(lines, averageGlyphHeight);
        }

    }

    /**
     * Calculates the gradient of a line
     * 
     * @param line
     *            the line
     * @return the gradient
     */
    private double getGradient(final Line line) {
        return (line.getEndY() - line.getStartY())
                / (line.getEndX() - line.getStartX());
    }

    /**
     * Calculates the intersection with the y axis
     * 
     * @param line
     *            the line
     * @param m
     *            the gradient of the line
     * @return the intersection
     */
    private int getOffset(final Line line, final double m) {
        return (int) (line.getStartY() - m * line.getStartX());
    }

    /**
     * Gets the maximum y distance between the baseline and the predecessor or
     * successor line
     * 
     * @param start
     *            the baseline
     * @param end
     *            the end for the calculation
     * @return the maximum y distance
     */
    private int getMaxYDist(final Line start, final Line end) {
        return (int) Math.min(Math.abs(end.getStartY() - start.getStartY()),
                Math.abs(end.getEndY() - start.getEndY()));
    }

    /**
     * Gets the ImageLine for a given baseline
     * 
     * @param baseline
     *            the baseline
     * @param predecessor
     *            the predecessor baseline
     * @param successor
     *            the successor baseline
     * @return the ImageLine
     */
    private ImageLine getImageLine(final Line baseline, final Line predecessor,
            final Line successor) {
        final double m = this.getGradient(baseline);
        final int b = this.getOffset(baseline, m);

        final int startX = (int) baseline.getStartX();
        final int endX = (int) baseline.getEndX();

        final int previousBaseline = this.getMaxYDist(baseline, predecessor);
        final int minIndex = this.getTop(m, b, startX, endX, previousBaseline);
        final Line upperline = new Line(startX, m * startX + b - minIndex,
                endX, m * endX + b - minIndex);

        final int nextBaseline = this.getMaxYDist(baseline, successor);
        final int maxIndex = this.getBottom(m, b, startX, endX, nextBaseline);
        final Line lowerline = new Line(startX, m * startX + b + maxIndex,
                endX, m * endX + b + maxIndex);

        return new ImageLine(baseline, upperline, lowerline);
    }

    /**
     * Calculates the ImageLine for each baseline
     * 
     * @param lines
     *            the baselines
     * @param averageGlyphHeight
     *            the average glyph height
     * @return the calculated ImageLines
     */
    private LinkedList<ImageLine> shiftBaseline(final LinkedList<Line> lines,
            final double averageGlyphHeight) {
        final LinkedList<ImageLine> imageLines = new LinkedList<>();

        if (lines.size() == 1) {
            imageLines.add(this.getImageLine(lines.get(0), new Line(0, 0,
                    this.width, 0), new Line(0, this.height, this.width,
                    this.height)));
            return imageLines;
        }

        imageLines.add(this.getImageLine(lines.get(0), new Line(0, 0,
                this.width, 0), lines.get(1)));

        int i = 1;
        while (i < lines.size() - 1) {
            final Line predecessor = lines.get(i - 1);
            final Line baseline = lines.get(i);
            final Line successor = lines.get(i + 1);

            final double yDistPredessor = Math.abs(Math.min(
                    predecessor.getEndY() - baseline.getEndY(),
                    predecessor.getStartY() - baseline.getStartY()));

            final double yDistSuccesor = Math.abs(Math.min(successor.getEndY()
                    - baseline.getEndY(),
                    successor.getStartY() - baseline.getStartY()));

            if (yDistPredessor < averageGlyphHeight
                    && yDistSuccesor < averageGlyphHeight) {
                imageLines.add(this.getImageLine(baseline, new Line(0, 0,
                        this.width, 0), new Line(0, this.height, this.width,
                        this.height)));
                ++i;
            } else if (yDistPredessor < averageGlyphHeight) {
                imageLines.add(this.getImageLine(baseline, new Line(0, 0,
                        this.width, 0), successor));
                ++i;
            } else {
                imageLines.add(this.getImageLine(baseline, predecessor,
                        successor));
                ++i;
                break;
            }
        }

        while (i < lines.size() - 1) {
            Line predecessor = null;
            final Line baseline = lines.get(i);
            Line successor = null;

            int j = 1;
            double yDist = 0;
            do {
                predecessor = lines.get(i - j);
                ++j;
                yDist = Math.min(Math.abs(predecessor.getEndY()
                        - baseline.getEndY()), Math.abs(predecessor.getStartY()
                        - baseline.getStartY()));
            } while (i - j >= 0 && yDist < averageGlyphHeight * 1.0);

            j = 1;
            do {
                successor = lines.get(i + j);
                ++j;
                yDist = Math.min(
                        Math.abs(successor.getEndY() - baseline.getEndY()),
                        Math.abs(successor.getStartY() - baseline.getStartY()));
            } while (i + j < lines.size() && yDist < averageGlyphHeight * 1.0);

            imageLines.add(this.getImageLine(baseline, predecessor, successor));
            ++i;
        }

        imageLines.add(this.getImageLine(lines.get(lines.size() - 1), lines
                .get(lines.size() - 2), new Line(0, this.height, this.width,
                this.height)));

        return imageLines;
    }

    /**
     * Calculates the distance to the lowerLine for a given baseline
     * 
     * @param m
     *            the gradient of the baseline
     * @param pB
     *            the intersection with the y axis of the baseline
     * @param startX
     *            the x coordinate of the starting point of the baseline
     * @param endX
     *            the x coordinate of the end point of the baseline
     * @param maxHeight
     *            the maximum height
     * @return the distance of the lower line
     */
    private int getBottom(final double m, final int pB, final int startX,
            final int endX, final int maxHeight) {
        int b = pB;
        final int[] lineContacts = new int[maxHeight];

        int min = Integer.MAX_VALUE;
        int minIndex = -1;
        for (int j = 0; j < maxHeight; ++j, ++b) {
            for (int x = startX; x < endX; ++x) {
                final int y = (int) (m * x + b);
                final int index = y * this.width + x;
                if (this.binaryImage.getPixelInt(index) == GrayImage.BLACK) {
                    lineContacts[j]++;
                }
            }
            if (lineContacts[j] < min) {
                min = lineContacts[j];
                minIndex = j;
            }
        }
        return minIndex;
    }

    /**
     * Sorts out small lines which are contained in other lines.
     * 
     * @param extractedLines
     *            the found lines
     * @return the found lines filter by all lines which are completly contained
     *         in others
     */
    private LinkedList<ImageLine> sortOutSmallLinesContainedInOthers(
            final LinkedList<ImageLine> extractedLines) {
        final int numOfLines = extractedLines.size();
        final LinkedList<ImageLine> finalLines = new LinkedList<>();

        int i = 0;
        for (final ImageLine il : extractedLines) {
            boolean contained = false;
            for (int j = -RANGE_CHECK; j <= RANGE_CHECK; ++j) {
                if (i + j < 0 || numOfLines <= i + j || j == 0) {
                    continue;
                }
                if (extractedLines.get(i + j).contains(il)) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                finalLines.add(il);
            }
            ++i;
        }
        DiptychonLogger.info("Found {} lines", finalLines.size());
        return finalLines;
    }

    /**
     * Calculates the distance to the upperLine for a given baseline
     * 
     * @param m
     *            the gradient of the baseline
     * @param pB
     *            the intersection with the y axis of the baseline
     * @param startX
     *            the x coordinate of the starting point of the baseline
     * @param endX
     *            the x coordinate of the end point of the baseline
     * @param maxHeight
     *            the maximum height
     * @return the distance of the upperLine
     */
    private int getTop(final double m, final int pB, final int startX,
            final int endX, final int maxHeight) {
        int b = pB;
        final int[] lineContacts = new int[maxHeight];

        int min = Integer.MAX_VALUE;
        int minIndex = -1;
        for (int j = 0; j < maxHeight; ++j, --b) {
            for (int x = startX; x < endX; ++x) {
                final int y = (int) (m * x + b);
                final int index = y * this.width + x;
                if (index < 0) {
                    continue;
                }
                if (this.binaryImage.getPixelInt(index) == GrayImage.BLACK) {
                    lineContacts[j]++;
                }
            }
            if (lineContacts[j] < min) {
                min = lineContacts[j];
                minIndex = j;
            }
        }
        return minIndex;
    }

    /**
     * Sammelt Bounding Boxes zu Reihen zusammen, wenn die Nachbarn sich
     * vertikal ueberschneiden
     * 
     * @param boxes
     *            all boxes
     * @param maxHDistance
     *            the maximum allowed horizontal distance between two boxes to
     *            be united.
     * @return a list of rows which are generated by uniting boxes.
     */
    private LinkedList<Row> createRows(
            final LinkedList<RectangularRegion> boxes, final int maxHDistance) {
        Row bestFit;
        int maxMatches;
        final LinkedList<Row> rows = new LinkedList<>();
        // Durchlaufe alle Blobs
        for (final RectangularRegion bbx : boxes) {
            bestFit = null;
            maxMatches = 0;
            for (final Row row : rows) {
                RectangularRegion lastBbx;
                for (int i = 1; i <= 3; ++i) {
                    if (row.boxes.size() < i) {
                        break;
                    }
                    // Nehme die letzten Bounding Boxes jeder Reihe
                    lastBbx = row.boxes.get(row.boxes.size() - i);
                    // Wenn sich die Bbx vertikal ueberschneiden: Als
                    // potenzielle Row speichern
                    if (bbx.x1 - lastBbx.x2 <= maxHDistance
                            && ((lastBbx.y1 <= bbx.y2 && lastBbx.y1 >= bbx.y1) || (bbx.y1 <= lastBbx.y2 && bbx.y1 >= lastBbx.y1))) {
                        final int biggesty1 = Math.max(bbx.y1, lastBbx.y1);
                        final int smallesty2 = Math.min(bbx.y2, lastBbx.y2);
                        if (smallesty2 - biggesty1 > maxMatches
                                && (smallesty2 - biggesty1 >= (bbx.y2 - bbx.y1) / 2 || smallesty2
                                        - biggesty1 >= (lastBbx.y2 - lastBbx.y1) / 2)) {
                            maxMatches = smallesty2 - biggesty1;
                            bestFit = row;
                        }
                    }
                }
            }
            // Keine uebereinstimmung ->Erstelle neue Row
            if (bestFit == null) {
                final Row row = new Row();
                row.boxes.add(bbx);
                row.top = bbx.y1;
                row.bottom = bbx.y2;
                row.left = bbx.x1;
                row.right = bbx.x2;
                rows.add(row);
            } else {
                bestFit.boxes.add(bbx);
                if (bestFit.top > bbx.y1) {
                    bestFit.top = bbx.y1;
                }
                if (bestFit.bottom < bbx.y2) {
                    bestFit.bottom = bbx.y2;
                }
                if (bestFit.left > bbx.x1) {
                    bestFit.left = bbx.x1;
                }
                if (bestFit.right < bbx.x2) {
                    bestFit.right = bbx.x2;
                }
            }

        }
        return rows;
    }

    /** Enthaelt Informationen ueber eine Zeile */
    private class Row {
        /** obere Grenze */
        private int top;

        /** untere Grenze */
        private int bottom;

        /** linke Grenze */
        private int left;

        /** rechte Grenze */
        private int right;

        /** Boxen (Regionen), die dieser Zeile zugeordnet sind */
        private LinkedList<RectangularRegion> boxes = new LinkedList<RectangularRegion>();
    }

    /** Enthält Informationen ueber einen Pixel eines Bildes */
    private class Pixel implements Comparable<Pixel> {
        /** X-Koordinate */
        private int x;

        /** Y-Koordinate */
        private int y;

        /** Distanz zur Basislinie fuer die Bestimmung nach Caesar et al. */
        private double distance;

        /** Gewichtung fuer die Bestimmung der Basislinie nach Caesar et al. */
        private int weight = 1;

        /**
         * Erstellt ein neues Pixelobjekt
         * 
         * @param pX
         *            die X-Koordinate
         * @param pY
         *            die Y-Koordinate
         * @param pWeight
         *            die Gewichtung
         */
        public Pixel(final int pX, final int pY, final int pWeight) {
            this.x = pX;
            this.y = pY;
            this.weight = pWeight;
        }

        /**
         * Vergleicht Objekte auf Basis der Distanz zur Basislinie
         * 
         * @param o
         *            das zu vergleichende Pixel-Objekt
         * @return Information, ob der Pixel weiter entfernt oder naeher ist
         */
        @Override
        public int compareTo(final Pixel o) {
            if (this.distance < o.distance) {
                return -1;
            }
            if (this.distance == o.distance) {
                return 0;
            } else {
                return 1;
            }
        }
    }
}
