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

import org.tzi.qsd.geometry.Point;

import de.diptychon.models.algorithms.contourExtraction.Contour;
import de.diptychon.models.misc.GrayImage;
import de.diptychon.models.misc.ImageUtils;

/**
 * Represents a single connected region with an arbitrary shape; coordinates
 * relate to the image from which this region is extracted;
 */
public class Region implements Comparable<Object> {
    private static int instanceCounter = 10000;

    private int area; // number of Points of this region (only foreground points
                      // count)
    private Point aPoint; // an arbitrary point lying within this region
    private int width; // difference between right most and left most pixel
    private int height; // difference between bottom most and top most pixel
    private int sumXCoords; // summ of all x coordinates
    private int sumYCoords; // summ of all y coordinates
    private Point centre; // the center of gravity
    private Point left; // left most point
    private Point right; // right most point
    private Point top; // top most point
    private Point bottom; // bottom most point
    private Contour contour; // boundary
    private int perimeter; // length of boundary
    private ArrayList<Hole> holes; // the holes of this region

    /**
     * Constructor
     */
    public Region(int[] regionFeatures, Point pPoint,
            ArrayList<Contour> pContours) {
        area = regionFeatures[0];
        width = regionFeatures[3] - regionFeatures[1];
        height = regionFeatures[8] - regionFeatures[6];
        left = new Point(regionFeatures[1], regionFeatures[2]);
        right = new Point(regionFeatures[2], regionFeatures[4]);
        top = new Point(regionFeatures[5], regionFeatures[6]);
        bottom = new Point(regionFeatures[7], regionFeatures[8]);
        sumXCoords = regionFeatures[9];
        sumYCoords = regionFeatures[10];
        centre = new Point(regionFeatures[11], regionFeatures[12]);
        aPoint = pPoint;
        contour = pContours.get(0); // outer contour has index 0
        perimeter = contour.getLength();
        if (pContours.size() > 1) // if there is at least one hole
        {
            holes = new ArrayList<Hole>();
            for (int i = 1; i < pContours.size(); i++)
                holes.add(new Hole(pContours.get(i)));
        } else
            holes = null;
    }

    public void saveContour(int regionNr) {
        double maxWidth = 0;
        double maxHeight = 0;
        for (Point p : contour.getPoints()) {
            if (p.getX() > maxWidth)
                maxWidth = p.getX();
            if (p.getY() > maxHeight)
                maxHeight = p.getY();
        }
        byte[] contourPoints = new byte[(int) ((maxWidth + 1) * (maxHeight + 1))];
        for (Point p : contour.getPoints()) {
            int x = (int) p.getX();
            int y = (int) p.getY();
            contourPoints[y * (int) maxWidth + x] = GrayImage.WHITE;
        }
        GrayImage saveContour = new GrayImage(contourPoints, (int) maxWidth,
                (int) maxHeight);
        saveContour.invertImage();
        ImageUtils.writeGrayscaleImage("CC\\" + (instanceCounter++) + "-"
                + regionNr + "Circularity " + getCircularity() + ".png",
                saveContour);
    }

    public int getArea() {
        return area;
    }

    public Point getAPoint() {
        return aPoint;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getSumXCoords() {
        return sumXCoords;
    }

    public int getSumYCoords() {
        return sumYCoords;
    }

    public Point getCentre() {
        return centre;
    }

    public int getLeftMostXCoord() {
        return (int) left.getX();
    }

    public int getTopMostYCoord() {
        return (int) top.getY();
    }

    public int getPerimeter() {
        return perimeter;
    }

    public double getCircularity() {
        return 4 * Math.PI * (getArea() / (Math.pow(getPerimeter(), 2)));
    }

    public int getNumOfHoles() {
        if (holes != null)
            return holes.size();
        else
            return 0;
    }

    public int getNumOfHolesLarger(int k) {
        if (holes != null) {
            int numOfHolesLargerK = 0;
            for (Hole h : holes)
                if (h.getPerimeter() >= k)
                    numOfHolesLargerK++;
            return numOfHolesLargerK;
        } else
            return 0;
    }

    public String getSizesOfHoles() {
        String s = "";
        if (holes != null)
            for (Hole h : holes)
                s = s + "-Groesse-" + h.getPerimeter();

        return s;
    }

    @Override
    public int compareTo(Object o) {
        return this.getArea() - ((Region) o).getArea();
    }

    /**
     * Inner class: Represents a single connected hole with an arbitrary shape
     */

    public class Hole {
        private Contour contourOfHole;
        private int perimeter;

        public Hole(Contour pContourOfHole) {
            contourOfHole = pContourOfHole;
            perimeter = contourOfHole.getLength();
        }

        public int getPerimeter() {
            return perimeter;
        }
    }

    /**
     * Inner class: Represents for each row of the region whether it contains -
     * only figure points, - only background points, - a change of figure to
     * background points, - a change of background to figure points, - two
     * changes from figure to background to figure, - ... - a number of features
     * based on these run codes
     */

    public class HorizontalRunCode {
        public HorizontalRunCode() {

        }

        public int getPerimeter() {
            return perimeter;
        }
    }

}