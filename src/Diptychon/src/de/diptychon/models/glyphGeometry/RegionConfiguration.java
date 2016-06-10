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
package de.diptychon.models.glyphGeometry;

import java.util.ArrayList;
import java.util.LinkedList;

import org.tzi.qsd.geometry.Point;

import de.diptychon.models.misc.GrayImage;
import de.diptychon.models.misc.ImageUtils;

/**
 * Represents a configuration of regions
 */
public class RegionConfiguration {

    public static int instanceCounter = 1000;
    public static final int UNDEFINED = -1;

    private ArrayList<Region> regions; // the regions of this configuration
    private int area; // sum of all areas
    private Point centre; // the centre of mass of all regions
    private GrayImage origImg; // this is extracted from origImg
    private int halfWidth; // of origImg
    private int halfHeight;
    private int meanDistance; // of each point to the centre

    private CentreQuadrants quadrants; // four quadrants around middle of image

    public RegionConfiguration(ArrayList<Region> regions, GrayImage origImg) {
        this.regions = regions;
        this.origImg = origImg;
        this.halfWidth = origImg.getWidth() / 2;
        this.halfHeight = origImg.getHeight() / 2;
        this.meanDistance = UNDEFINED;

        if (regions.size() > 0) {
            // compute the centre over the areas of all regions
            int xSum = 0;
            int ySum = 0;
            int numOfPoints = 0;
            for (Region r : regions) {
                xSum = xSum + r.getSumXCoords();
                ySum = ySum + r.getSumYCoords();
                numOfPoints = numOfPoints + r.getArea();
            }
            area = numOfPoints;
            centre = new Point(xSum / area, ySum / area);
            quadrants = new CentreQuadrants();
        } else {
            area = 0;
            centre = new Point(UNDEFINED, UNDEFINED);
            quadrants = null;
        }
    }

    public void saveImg(String fileName) {
        // int top = quadrants.sizeTopLeft()+quadrants.sizeTopRight();
        // int bot = quadrants.sizeBotLeft()+quadrants.sizeBotRight();
        // String s = " top"+top+" bot"+bot;
        // if (top > 0 && bot > 0)
        ImageUtils.writeGrayscaleImage("CC\\" + (instanceCounter++) + "-"
                + fileName + ".png", origImg);
    }

    public boolean isEmpty() {
        return regions.size() == 0;
    }

    public ArrayList<Region> getAll() {
        return regions;
    }

    public int getHeight() {
        int highest = 0;
        for (Region r : regions)
            if (r.getHeight() > highest)
                highest = r.getHeight();
        return highest;
    }

    /**
     * number of included regions
     */
    public int getNumber() {
        return regions.size();
    }

    public int getNumOfHolesLarger(int k) {
        int numOfHoles = 0;
        System.out.println("aa0");
        for (Region r : regions)
            numOfHoles += r.getNumOfHolesLarger(k);

        return numOfHoles;
    }

    public int getArea() {
        return area;
    }

    public Point getCentre() {
        return centre;
    }

    public int meanDistanceFromCentre() {
        if (meanDistance == UNDEFINED) {
            int d = 0;
            int cx = (int) origImg.getWidth() / 2;
            int cy = (int) origImg.getHeight() / 2;
            for (int x = 0; x < origImg.getWidth(); x++)
                for (int y = 0; y < origImg.getHeight(); y++)
                    if (origImg.getPixel(x, y) == GrayImage.BLACK)
                        // d = d + (int)(Math.sqrt(Math.pow(x-cx,2) +
                        // Math.pow(y-cy,2)));
                        d = d
                                + (int) Math.pow(
                                        Math.abs(x - cx) + Math.abs(y - cy), 2);
            meanDistance = d / getArea();
            saveImg("" + meanDistanceFromCentre());
            return meanDistance;
        } else
            return meanDistance;
    }

    public boolean equalPointDistribution(RegionConfiguration other) {
        boolean result = quadrants.equalOrder(other.quadrants);
        return result;
    }

    public boolean topSimilarBottomPart(RegionConfiguration r) {
        return (quadrants.topPartLargerBottomPart() && r.quadrants
                .topPartLargerBottomPart())
                || (quadrants.bottomPartLargerTopPart() && r.quadrants
                        .bottomPartLargerTopPart())
                || (!quadrants.topPartLargerBottomPart()
                        && !r.quadrants.topPartLargerBottomPart()
                        && !quadrants.bottomPartLargerTopPart() && !r.quadrants
                            .bottomPartLargerTopPart());
    }

    /**
     * returns the region with the minimal y-coordinate of its centre
     */
    public Region getTop() {
        Region top = regions.get(0);
        for (Region r : regions)
            if (r.getCentre().getY() < top.getCentre().getY())
                top = r;

        return top;
    }

    /**
     * inner, element class
     *
     * Represents the distribution of foreground pixel within the four quadrants
     * around the middle of the glyph / candidate image.
     */

    private class CentreQuadrants {

        private int sizeTopRight = UNDEFINED;
        private int sizeTopLeft = UNDEFINED;
        private int sizeBotLeft = UNDEFINED;
        private int sizeBotRight = UNDEFINED;
        private int topRight = 0;
        private int topLeft = 1;
        private int botLeft = 2;
        private int botRight = 3;

        private LinkedList<Quadrant> order;

        public CentreQuadrants() {
            order = new LinkedList<Quadrant>();
            order.add(new Quadrant(topRight, sizeTopRight()));

            if (sizeTopLeft() > sizeTopRight())
                order.add(new Quadrant(topLeft, sizeTopLeft()));
            else
                order.addFirst(new Quadrant(topLeft, sizeTopLeft()));

            int i = 0;
            while (i < order.size()
                    && order.get(i).getNumOfPoints() < sizeBotLeft())
                i++;
            order.add(i, new Quadrant(botLeft, sizeBotLeft()));

            i = 0;
            while (i < order.size()
                    && order.get(i).getNumOfPoints() < sizeBotRight())
                i++;
            order.add(i, new Quadrant(botRight, sizeBotRight()));

            // System.out.println("\n");
            // for ( Quadrant q: order )
            // System.out.println("Quadrant x hat "+q.getNumOfPoints()+
            // " Punkte");
        }

        public boolean equalOrder(CentreQuadrants other) {
            return order.get(0).getPosition() == other.order.get(0)
                    .getPosition()
                    && order.get(1).getPosition() == other.order.get(1)
                            .getPosition()
                    && order.get(2).getPosition() == other.order.get(2)
                            .getPosition()
                    && order.get(3).getPosition() == other.order.get(3)
                            .getPosition();
        }

        public boolean topPartLargerBottomPart() {
            int topPart = 0;
            int botPart = 0;
            for (Quadrant q : order) {
                if (q.getPosition() == topLeft || q.getPosition() == topRight)
                    topPart = topPart + q.getNumOfPoints();
                else
                    botPart = botPart + q.getNumOfPoints();
            }
            return topPart > botPart + (botPart * 0.10);
        }

        public boolean bottomPartLargerTopPart() {
            int topPart = 0;
            int botPart = 0;
            for (Quadrant q : order) {
                if (q.getPosition() == topLeft || q.getPosition() == topRight)
                    topPart = topPart + q.getNumOfPoints();
                else
                    botPart = botPart + q.getNumOfPoints();
            }
            return botPart > topPart + (topPart * 0.10);
        }

        public int sizeTopRight() {
            if (sizeTopRight == UNDEFINED) {
                sizeTopRight = 0;
                for (int x = halfWidth; x < origImg.getWidth(); x++)
                    for (int y = 0; y < halfHeight; y++)
                        if (origImg.getPixel(x, y) == GrayImage.BLACK)
                            sizeTopRight++;
            }
            return sizeTopRight;
        }

        public int sizeTopLeft() {
            if (sizeTopLeft == UNDEFINED) {
                sizeTopLeft = 0;
                for (int x = 0; x < halfWidth; x++)
                    for (int y = 0; y < halfHeight; y++)
                        if (origImg.getPixel(x, y) == GrayImage.BLACK)
                            sizeTopLeft++;
            }
            return sizeTopLeft;
        }

        public int sizeBotLeft() {
            if (sizeBotLeft == UNDEFINED) {
                sizeBotLeft = 0;
                for (int x = 0; x < halfWidth; x++)
                    for (int y = halfHeight; y < origImg.getHeight(); y++)
                        if (origImg.getPixel(x, y) == GrayImage.BLACK)
                            sizeBotLeft++;
            }
            return sizeBotLeft;
        }

        public int sizeBotRight() {
            if (sizeBotRight == UNDEFINED) {
                sizeBotRight = 0;
                for (int x = halfWidth; x < origImg.getWidth(); x++)
                    for (int y = halfHeight; y < origImg.getHeight(); y++)
                        if (origImg.getPixel(x, y) == GrayImage.BLACK)
                            sizeBotRight++;
            }
            return sizeBotRight;
        }

        /**
         * inner, element class
         *
         * Represents one quadrant.
         */
        private class Quadrant {
            private final int position;
            private final int numOfPoints;

            public Quadrant(int position, int numOfPoints) {
                this.position = position;
                this.numOfPoints = numOfPoints;
            }

            public int getPosition() {
                return position;
            }

            public int getNumOfPoints() {
                return numOfPoints;
            }
        }
    }
}
