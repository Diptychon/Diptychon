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
package de.diptychon.models.algorithms.contourExtraction;

import java.util.ArrayList;

import org.tzi.qsd.geometry.Point;

import de.diptychon.models.misc.GrayImage;

/**
 * This class provides the functionality to trace the contours within a binary
 * image and counts the number of regions
 * <p>
 * It is performed according to the proposed algorithm in Digitale
 * Bildverarbeitung - Eine Einf&uuml;hrung mit ImageJ by Burger and Burge 2nd
 * edition
 */
public class ContourTracer {
    /**
     * The converted foreground value
     */
    private static final int FOREGROUND = 1;

    /**
     * The converted background value
     */
    private static final int BACKGROUND = 0;

    /**
     * The image
     */
    private final GrayImage image;

    /**
     * The width of the image
     */
    private final int width;

    /**
     * The height of the image
     */
    private final int height;

    /**
     * The label array
     */
    private int[][] labelArray;

    /**
     * The label array
     */
    private int labelCount = -1;

    private ArrayList<Float> perimeters = new ArrayList<Float>();

    /**
     * the pixel array
     */
    private int[][] pixelArray;

    private boolean getLabelsRunning = false;

    /**
     * Convenience enum to ease the calculation of the next neighbour in an
     * 8-neighborhood
     */
    private enum Direction {
        // CHECKSTYLE:OFF
        // 8-neighbourhood
        EAST(0, 1, 0),

        NORTH_EAST(1, 1, -1),

        NORTH(2, 0, -1),

        NORTH_WEST(3, -1, -1),

        WEST(4, -1, 0),

        SOUTH_WEST(5, -1, 1),

        SOUTH(6, 0, 1),

        SOUTH_EAST(7, 1, 1);
        // CHECKSTYLE:ON

        /**
         * The encoded direction
         */
        private final int dir;

        /**
         * The next step in x direction
         */
        private final int x;

        /**
         * The next step in y direction
         */
        private final int y;

        /**
         * Creates a new enum
         * 
         * @param d
         *            the direction encoding
         * @param pX
         *            The next step in x direction
         * @param pY
         *            The next step in y direction
         */
        Direction(final int d, final int pX, final int pY) {
            this.dir = d;
            this.x = pX;
            this.y = pY;
        }

        /**
         * Gets the next step in x direction
         * 
         * @return the next step in x direction
         */
        public int getDirX() {
            return this.x;
        }

        /**
         * Gets the next step in y direction
         * 
         * @return the next step in y direction
         */
        public int getDirY() {
            return this.y;
        }

        /**
         * Gets the corresponding direction as int
         * 
         * @return the corresponding direction as int
         */
        public int toInt() {
            return this.dir;
        }

        /**
         * Gets the enum to the direction
         * 
         * @param dir
         *            the direction
         * @return the enum
         */
        public static Direction getDirection(final int dir) {
            // 8-neighbourhood
            switch (dir) {
            case 0:
                return EAST;
            case 1:
                return NORTH_EAST;
            case 2:
                return NORTH;
            case 3:
                return NORTH_WEST;
            case 4:
                return WEST;
            case 5:
                return SOUTH_WEST;
            case 6:
                return SOUTH;
            case 7:
                return SOUTH_EAST;
            default:
                return null;
            }
        }
    }

    private enum Direction2 {
        // CHECKSTYLE:OFF
        // 4-neighbourhood
        EAST(0, 1, 0),

        NORTH(1, 0, -1),

        WEST(2, -1, 0),

        SOUTH(3, 0, 1);

        // CHECKSTYLE:ON

        /**
         * The encoded direction
         */
        private final int dir;

        /**
         * The next step in x direction
         */
        private final int x;

        /**
         * The next step in y direction
         */
        private final int y;

        /**
         * Creates a new enum
         * 
         * @param d
         *            the direction encoding
         * @param pX
         *            The next step in x direction
         * @param pY
         *            The next step in y direction
         */
        Direction2(final int d, final int pX, final int pY) {
            this.dir = d;
            this.x = pX;
            this.y = pY;
        }

        /**
         * Gets the next step in x direction
         * 
         * @return the next step in x direction
         */
        public int getDirX() {
            return this.x;
        }

        /**
         * Gets the next step in y direction
         * 
         * @return the next step in y direction
         */
        public int getDirY() {
            return this.y;
        }

        /**
         * Gets the corresponding direction as int
         * 
         * @return the corresponding direction as int
         */
        public int toInt() {
            return this.dir;
        }

        /**
         * Gets the enum to the direction
         * 
         * @param dir
         *            the direction
         * @return the enum
         */
        public static Direction2 getDirection(final int dir) {
            // 8-neighbourhood
            switch (dir) {
            case 0:
                return EAST;
            case 1:
                return NORTH;
            case 2:
                return WEST;
            case 3:
                return SOUTH;
            default:
                return null;
            }
        }
    }

    /**
     * Creates a new ContourTracer
     * 
     * @param pImage
     *            the binary image
     */
    public ContourTracer(final GrayImage pImage) {
        this.image = pImage;
        this.width = pImage.getWidth();
        this.height = pImage.getHeight();

        // image = new Dilatation(image,
        // MorphologicalOperation.MASK_OCTAGON).getImage();
        // image = new Erosion(image,
        // MorphologicalOperation.MASK_OCTAGON).getImage();

        this.makeAuxArrays();
    }

    /**
     * Creates a new ContourTracer
     * 
     * @param pixels
     *            the pixels of the image
     * @param width
     *            the width of the image
     * @param height
     *            the height of the image
     */
    public ContourTracer(final int[][] pixels, final int width, final int height) {
        this.image = null;
        this.width = width;
        this.height = height;

        this.pixelArray = new int[this.height + 2][this.width + 2];
        this.labelArray = new int[this.height + 2][this.width + 2];

        for (int y = 0; y < this.height; ++y) {
            for (int x = 0; x < this.width; ++x) {
                if (pixels[y][x] == FOREGROUND) {
                    this.pixelArray[y + 1][x + 1] = FOREGROUND;
                }
            }
        }
    }

    /**
     * Creates a new ContourTracer
     * 
     * @param pixels
     *            the pixels of the image
     * @param labels
     *            the labels for the image
     * @param labelCount
     *            the number of the labels
     * @param width
     *            the width of the image
     * @param height
     *            the height of the image
     */
    public ContourTracer(final int[][] pixels, final int[][] labels,
            final int labelCount, final int width, final int height) {
        this.image = null;
        this.width = width;
        this.height = height;

        this.pixelArray = new int[this.height + 2][this.width + 2];
        this.labelArray = new int[this.height + 2][this.width + 2];

        for (int y = 0; y < this.height; ++y) {
            for (int x = 0; x < this.width; ++x) {
                this.labelArray[y + 1][x + 1] = labels[y][x];
                if (pixels[y][x] == FOREGROUND) {
                    this.pixelArray[y + 1][x + 1] = FOREGROUND;
                }
            }
        }
    }

    /**
     * Creates a new ContourTracer
     * 
     * @param pixels
     *            the pixels of the image
     * @param pWidth
     *            the width of the image
     * @param pHeight
     *            the height of the image
     */
    public ContourTracer(final byte[] pixels, final int pWidth,
            final int pHeight) {
        this.image = new GrayImage(pixels, pWidth, pHeight);
        this.width = pWidth;
        this.height = pHeight;

        this.makeAuxArrays();
    }

    /**
     * Creates convenience arrays which ease up the contour tracing and region
     * labeling
     */
    void makeAuxArrays() {
        this.pixelArray = new int[this.height + 2][this.width + 2];
        this.labelArray = new int[this.height + 2][this.width + 2];

        for (int y = 0; y < this.height; ++y) {
            final int offset = y * this.width;
            for (int x = 0; x < this.width; ++x) {
                final int index = offset + x;
                if (this.image.getPixelInt(index) == GrayImage.BLACK) {
                    this.pixelArray[y + 1][x + 1] = FOREGROUND;
                }
            }
        }
    }

    /**
     * Gets the labels
     * 
     * @return the label array
     */
    public int[][] getLabels() {
        int label = 0; // current label
        // this.labelCount = 0;
        this.getLabelsRunning = true;

        for (int y = 1; y < this.pixelArray.length - 1; y++) {
            label = 0; // no label
            for (int x = 1; x < this.pixelArray[y].length - 1; x++) {
                if (this.pixelArray[y][x] == FOREGROUND) {
                    if (label != 0) { // keep using same label
                        this.labelArray[y][x] = label;
                    } else {
                        label = this.labelArray[y][x];
                        if (label == 0) { // unlabeled - new outer contour
                            label = ++this.labelCount;
                            final OuterContour oc = this.traceOuterContour2(x,
                                    y);
                            if (oc.getLength() > 1) {
                                this.labelArray[y][x] = label;
                            }
                        }
                    }
                } else { // BACKGROUND pixel
                    if (label != 0) {
                        if (this.labelArray[y][x] == 0) { // unlabeled - new
                                                          // inner contour
                            final InnerContour ic = this.traceInnerContour(
                                    x - 1, y);
                        }
                        label = 0;
                    }
                }
            }
        }
        this.getLabelsRunning = false;
        return this.labelArray;
    }

    /**
     * Gets the contours
     * 
     * @return the contours
     */
    public int getLabelCount() {
        if (this.labelCount == (-1)) {
            this.getLabels();
        }
        return this.labelCount;
    }

    /**
     * Gets the contours
     * 
     * @return the contours
     */
    public ContourSet getContours() {
        final ContourSet cs = new ContourSet();
        int label = 0; // current label

        for (int y = 1; y < this.pixelArray.length - 1; y++) {
            label = 0; // no label
            for (int x = 1; x < this.pixelArray[y].length - 1; x++) {
                if (this.pixelArray[y][x] == FOREGROUND) {
                    if (label != 0) { // keep using same label
                        this.labelArray[y][x] = label;
                    } else {
                        label = this.labelArray[y][x];
                        if (label == 0) { // unlabeled - new outer contour
                            label = 1;
                            final OuterContour oc = this
                                    .traceOuterContour(x, y);
                            if (oc.getLength() > 1) {
                                cs.addContour(oc);
                                this.labelArray[y][x] = label;
                            } else {
                                this.perimeters
                                        .remove(this.perimeters.size() - 1);
                            }
                        }
                    }
                } else { // BACKGROUND pixel
                    if (label != 0) {
                        if (this.labelArray[y][x] == 0) { // unlabeled - new
                                                          // inner contour
                            final InnerContour ic = this.traceInnerContour(
                                    x - 1, y);
                            if (ic.getLength() > 1) {
                                cs.addContour(ic);
                            }
                        }
                        label = 0;
                    }
                }
            }
        }
        return cs;
    }

    /**
     * Traces the OuterContour from a given starting point
     * 
     * @param xStart
     *            the x coordinate of the starting point
     * @param yStart
     *            the y coordinate of the starting point
     * @return the OuterContour
     */
    private OuterContour traceOuterContour(final int xStart, final int yStart) {
        return (OuterContour) this.traceContour(xStart, yStart, Direction.WEST);
    }

    /**
     * Traces the OuterContour from a given starting point
     * 
     * @param xStart
     *            the x coordinate of the starting point
     * @param yStart
     *            the y coordinate of the starting point
     * @return the OuterContour
     */
    private OuterContour traceOuterContour2(final int xStart, final int yStart) {
        return (OuterContour) this.traceContour2(xStart, yStart,
                Direction2.WEST);
    }

    /**
     * Traces the InnerContour from a given starting point
     * 
     * @param xStart
     *            the x coordinate of the starting point
     * @param yStart
     *            the y coordinate of the starting point
     * @return the InnerContour
     */
    private InnerContour traceInnerContour(final int xStart, final int yStart) {
        return (InnerContour) this.traceContour(xStart, yStart, Direction.EAST);
    }

    /**
     * Traces the InnerContour from a given starting point
     * 
     * @param xStart
     *            the x coordinate of the starting point
     * @param yStart
     *            the y coordinate of the starting point
     * @return the InnerContour
     */
    private InnerContour traceInnerContour2(final int xStart, final int yStart) {
        return (InnerContour) this.traceContour2(xStart, yStart,
                Direction2.EAST);
    }

    /**
     * Traces the contour from a given starting point in a given direction
     * 
     * @param xStart
     *            the x coordinate of the starting point
     * @param yStart
     *            the y coordinate of the starting point
     * @param dir
     *            the direction
     * @return the contour
     */
    private Contour traceContour(final int xStart, final int yStart,
            final Direction dir) {
        final Contour cont = dir.equals(Direction.WEST) ? new OuterContour()
                : new InnerContour();
        int xSuccessor, ySuccessor; // successor of starting point (xS,yS)
        int xPrevious, yPrevious; // P = previous contour point
        int xCurrent, yCurrent; // C = current contour point
        if (dir.equals(Direction.WEST)) {
            this.perimeters.add((float) 1);
        }
        Point p = new Point(xStart, yStart);
        Direction dirNext = this.findNextPoint(p, dir);
        cont.addPoint(p);
        xPrevious = xStart;
        yPrevious = yStart;
        xCurrent = (int) p.getX();
        xSuccessor = xCurrent;
        yCurrent = (int) p.getY();
        ySuccessor = yCurrent;

        // true if isolated pixel
        boolean done = (xStart == xSuccessor && yStart == ySuccessor);

        while (!done) {
            this.labelArray[yCurrent][xCurrent] = this.labelCount;
            p = new Point(xCurrent, yCurrent);
            if (dir.equals(Direction.WEST)) {
                switch (dirNext.toInt()) {
                case 0:
                    this.perimeters
                            .set(this.perimeters.size() - 1, this.perimeters
                                    .get(this.perimeters.size() - 1) + 1);
                    break;
                case 1:
                    this.perimeters.set(this.perimeters.size() - 1,
                            this.perimeters.get(this.perimeters.size() - 1)
                                    + (float) Math.sqrt(2));
                    break;
                case 2:
                    this.perimeters
                            .set(this.perimeters.size() - 1, this.perimeters
                                    .get(this.perimeters.size() - 1) + 1);
                    break;
                case 3:
                    this.perimeters.set(this.perimeters.size() - 1,
                            this.perimeters.get(this.perimeters.size() - 1)
                                    + (float) Math.sqrt(2));
                    break;
                case 4:
                    this.perimeters
                            .set(this.perimeters.size() - 1, this.perimeters
                                    .get(this.perimeters.size() - 1) + 1);
                    break;
                case 5:
                    this.perimeters.set(this.perimeters.size() - 1,
                            this.perimeters.get(this.perimeters.size() - 1)
                                    + (float) Math.sqrt(2));
                    break;
                case 6:
                    this.perimeters
                            .set(this.perimeters.size() - 1, this.perimeters
                                    .get(this.perimeters.size() - 1) + 1);
                    break;
                case 7:
                    this.perimeters.set(this.perimeters.size() - 1,
                            this.perimeters.get(this.perimeters.size() - 1)
                                    + (float) Math.sqrt(2));
                    break;
                default:
                    break;
                }
            }
            // 8-neighbourhood
            final Direction dirSearch = Direction
                    .getDirection((dirNext.toInt() + 6) % 8);
            dirNext = this.findNextPoint(p, dirSearch);
            xPrevious = xCurrent;
            yPrevious = yCurrent;
            xCurrent = (int) p.getX();
            yCurrent = (int) p.getY();
            done = (xPrevious == xStart && yPrevious == yStart
                    && xCurrent == xSuccessor && yCurrent == ySuccessor);
            if (!done) {
                cont.addPoint(p);
            }
        }
        return cont;
    }

    /**
     * Traces the contour from a given starting point in a given direction
     * 
     * @param xStart
     *            the x coordinate of the starting point
     * @param yStart
     *            the y coordinate of the starting point
     * @param dir
     *            the direction
     * @return the contour
     */
    private Contour traceContour2(final int xStart, final int yStart,
            final Direction2 dir) {
        final Contour cont = dir.equals(Direction2.WEST) ? new OuterContour()
                : new InnerContour();
        int xSuccessor, ySuccessor; // successor of starting point (xS,yS)
        int xPrevious, yPrevious; // P = previous contour point
        int xCurrent, yCurrent; // C = current contour point
        Point p = new Point(xStart, yStart);
        Direction2 dirNext = this.findNextPoint2(p, dir);
        cont.addPoint(p);
        xPrevious = xStart;
        yPrevious = yStart;
        xCurrent = (int) p.getX();
        xSuccessor = xCurrent;
        yCurrent = (int) p.getY();
        ySuccessor = yCurrent;

        // true if isolated pixel
        boolean done = (xStart == xSuccessor && yStart == ySuccessor);

        while (!done) {
            this.labelArray[yCurrent][xCurrent] = this.labelCount;
            p = new Point(xCurrent, yCurrent);
            // 4-neighbourhood
            final Direction2 dirSearch = Direction2.getDirection((dirNext
                    .toInt() + 3) % 4);
            dirNext = this.findNextPoint2(p, dirSearch);
            xPrevious = xCurrent;
            yPrevious = yCurrent;
            xCurrent = (int) p.getX();
            yCurrent = (int) p.getY();
            done = (xPrevious == xStart && yPrevious == yStart
                    && xCurrent == xSuccessor && yCurrent == ySuccessor);
            if (!done) {
                cont.addPoint(p);
            }
        }
        return cont;
    }

    /**
     * Gets the next foreground pixel starting at point p in direction dir
     * 
     * @param p
     *            the starting point
     * @param dir
     *            the initial direction
     * @return the next direction
     */
    // CHECKSTYLE:OFF
    private Direction findNextPoint(final Point p, Direction dir)// CHECKSTYLE:ON
    {
        for (int i = 0; i < Direction.values().length; ++i) {
            final int x = (int) p.getX() + dir.getDirX();
            final int y = (int) p.getY() + dir.getDirY();
            if (this.pixelArray[y][x] == BACKGROUND) {
                if (!this.getLabelsRunning) {
                    this.labelArray[y][x] = -1;
                }
                // else { this.labelArray[y][x] = 0; }
                // 8-neighbourhood
                // CHECKSTYLE:OFF
                dir = Direction.getDirection((dir.toInt() + 1) % 8);
                // CHECKSTYLE:ON
            } else {
                p.x = x;
                p.y = y;
                break;
            }
        }
        return dir;
    }

    /**
     * Gets the next foreground pixel starting at point p in direction dir
     * 
     * @param p
     *            the starting point
     * @param dir
     *            the initial direction
     * @return the next direction
     */
    // CHECKSTYLE:OFF
    private Direction2 findNextPoint2(final Point p, Direction2 dir)// CHECKSTYLE:ON
    {
        for (int i = 0; i < Direction2.values().length; ++i) {
            final int x = (int) p.getX() + dir.getDirX();
            final int y = (int) p.getY() + dir.getDirY();
            if (this.pixelArray[y][x] == BACKGROUND) {
                if (!this.getLabelsRunning) {
                    this.labelArray[y][x] = -1;
                }
                // else { this.labelArray[y][x] = 0; }
                // 4-neighbourhood
                // CHECKSTYLE:OFF
                dir = Direction2.getDirection((dir.toInt() + 1) % 4);
                // CHECKSTYLE:ON
            } else {
                p.x = x;
                p.y = y;
                break;
            }
        }
        return dir;
    }

    public ArrayList<Float> getPerimeters() {
        return perimeters;
    }
}
