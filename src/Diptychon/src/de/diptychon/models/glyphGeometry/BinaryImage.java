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
import java.util.Collections;
import java.util.Stack;

import org.tzi.qsd.geometry.Point;

import de.diptychon.models.algorithms.contourExtraction.Contour;
import de.diptychon.models.algorithms.contourExtraction.ContourSet;
import de.diptychon.models.algorithms.contourExtraction.ContourTracer;
import de.diptychon.models.misc.GrayImage;
import de.diptychon.models.misc.ImageUtils;

/**
 * Specialisation of a GrayImage: representation is the same, specific methods
 * for binary images are added. This class is mainly used to extract glyph
 * regions from binary images, which are represented by the class Region.
 */
public class BinaryImage extends GrayImage {

    private static final long serialVersionUID = 20150417;
    private static int instanceCounter = 10000;
    public transient static final byte FOREGROUND = BLACK;
    public transient static final byte BACKGROUND = -1;
    public transient static final int UNDEFINED_INT = -1;

    private GrayImage labelImage = null;
    private ArrayList<Region> regions = null;
    private RegionConfiguration regionConfig = null;

    /**
     * Constructor
     */
    public BinaryImage(byte[] croppedPart, int pWidth, int pHeight) {
        super(croppedPart, pWidth, pHeight);
        instanceCounter++;
    }

    public GrayImage getAsGrayImage() {
        return new GrayImage(this.getPixels(), this.getWidth(),
                this.getHeight());
    }

    public void saveOnHDD() {
        // ImageUtils.writeGrayscaleImage("CC\\BildausschnittBinaer-"+instanceCounter+".png",
        // labelImage);

        // ImageUtils.writeGrayscaleImage("CC\\BildausschnittBinaer-"+instanceCounter+".png",
        // this);
    }

    public RegionConfiguration getRegionConfiguration() {
        if (regionConfig == null)
            regionConfig = new RegionConfiguration(regions, getAsGrayImage());
        if (regionConfig.isEmpty())
            return null;
        else
            return regionConfig;
    }

    /**
     * each BACKGROUND pixel is changed to a FOREGROUND pixel if at least one
     * pixel within the 8-connected neighborhood is a FOREGROUND pixel
     * 
     * the border of the image is neglected
     */
    private void dilatation() {
        GrayImage copy = new GrayImage(this.getPixelCloned(), getWidth(),
                getHeight());
        for (int y = 1; y < copy.getHeight() - 1; y++)
            for (int x = 1; x < copy.getWidth() - 1; x++)
                if (copy.getPixel(x, y) == BACKGROUND)
                    if (copy.getPixel(x - 1, y - 1) == FOREGROUND
                            || copy.getPixel(x, y - 1) == FOREGROUND
                            || copy.getPixel(x + 1, y - 1) == FOREGROUND
                            || copy.getPixel(x - 1, y) == FOREGROUND
                            || copy.getPixel(x + 1, y) == FOREGROUND
                            || copy.getPixel(x - 1, y + 1) == FOREGROUND
                            || copy.getPixel(x, y + 1) == FOREGROUND
                            || copy.getPixel(x + 1, y + 1) == FOREGROUND) {
                        this.setToGrey(x, y, FOREGROUND);
                    }
    }

    /**
     * each FOREGROUND pixel remains a FOREGROUND pixel if the 8-connected
     * neighborhood does not contain any BACKGROUND pixel
     * 
     * the border of the image is neglected
     */
    private void erosion() {
        GrayImage copy = new GrayImage(this.getPixelCloned(), getWidth(),
                getHeight());
        for (int y = 1; y < copy.getHeight() - 1; y++)
            for (int x = 1; x < copy.getWidth() - 1; x++)
                if (copy.getPixel(x, y) == FOREGROUND)
                    if (copy.getPixel(x - 1, y - 1) == BACKGROUND
                            || copy.getPixel(x, y - 1) == BACKGROUND
                            || copy.getPixel(x + 1, y - 1) == BACKGROUND
                            || copy.getPixel(x - 1, y) == BACKGROUND
                            || copy.getPixel(x + 1, y) == BACKGROUND
                            || copy.getPixel(x - 1, y + 1) == BACKGROUND
                            || copy.getPixel(x, y + 1) == BACKGROUND
                            || copy.getPixel(x + 1, y + 1) == BACKGROUND) {
                        this.setToGrey(x, y, BACKGROUND);
                    }
    }

    /**
     * each FOREGROUND pixel remains a FOREGROUND pixel if there is at least one
     * BACKGROUND pixel in the 8-neighbourhood
     * 
     * the border of the image is neglected
     */
    public GrayImage extractContourPoints() {
        GrayImage copy = new GrayImage(this.getPixelCloned(), getWidth(),
                getHeight());
        for (int y = 1; y < copy.getHeight() - 1; y++)
            for (int x = 1; x < copy.getWidth() - 1; x++)
                if (getPixel(x, y) == FOREGROUND)
                    if (getPixel(x - 1, y - 1) == BACKGROUND
                            || getPixel(x, y - 1) == BACKGROUND
                            || getPixel(x + 1, y - 1) == BACKGROUND
                            || getPixel(x - 1, y) == BACKGROUND
                            || getPixel(x + 1, y) == BACKGROUND
                            || getPixel(x - 1, y + 1) == BACKGROUND
                            || getPixel(x, y + 1) == BACKGROUND
                            || getPixel(x + 1, y + 1) == BACKGROUND)
                        copy.setToGrey(x, y, FOREGROUND);
                    else
                        copy.setToGrey(x, y, BACKGROUND);
        return copy;
    }

    public void closing() {
        dilatation();
        erosion();
    }

    public void opening() {
        erosion();
        dilatation();
    }

    /**
     * label connected regions within a copy of the image called labelImage and
     * extract all regions
     */
    public void labelConnectedComponents() {
        labelImage = new GrayImage(this.getPixelCloned(), getWidth(),
                getHeight());
        int nextLabel = 2;
        regions = new ArrayList<Region>();

        for (int y = 0; y < labelImage.getHeight(); y++)
            for (int x = 0; x < labelImage.getWidth(); x++)
                if (labelImage.getPixel(x, y) == FOREGROUND) {
                    int[][] newRegion = new int[getHeight() + 2][getWidth() + 2];
                    int[] regionFeatures = flooding(new Point(x, y), nextLabel,
                            newRegion);
                    if (regionFeatures[0] > 1) // if size of region > one Pixel
                        regions.add(new Region(regionFeatures, new Point(x, y),
                                getContours(newRegion)));
                    nextLabel = nextLabel + 1;
                }
        // determineRunCodesOfLargestRegion();
    }

    /**
     * label a connected region which contains the pixel 'seed' simultaneously,
     * determine - size of that region - the extreme value coordinates for three
     * foreground points: left, right, top, bottom - center of gravity - and
     * fill the empty array newRegion with the points of that region
     */
    private int[] flooding(Point seed, int label, int[][] newRegion) {
        int size = 0; // index 0 of returned array
        int leftX = getWidth(); // index 1
        int leftY = UNDEFINED_INT; // index 2
        int rightX = 0; // index 3
        int rightY = UNDEFINED_INT; // index 4
        int topX = UNDEFINED_INT; // index 5
        int topY = getHeight(); // index 6
        int bottomX = UNDEFINED_INT; // index 7
        int bottomY = 0; // index 8
        int sumXCoords = 0; // index 9
        int sumYCoords = 0; // index 10
        int centreX = 0; // index 11
        int centreY = 0; // index 12

        Stack<Point> s = new Stack<Point>();
        s.push(seed);
        while (!s.empty()) {
            Point p = s.pop();
            int x = (int) p.getX();
            int y = (int) p.getY();
            if (x >= 0 && x < labelImage.getWidth() && y >= 0
                    && y < labelImage.getHeight()) {
                if (labelImage.getPixel(x, y) == FOREGROUND) {
                    s.push(new Point(x - 1, y - 1));
                    s.push(new Point(x - 1, y));
                    s.push(new Point(x - 1, y + 1));
                    s.push(new Point(x, y - 1));

                    labelImage.setToGrey(x, y, label);
                    newRegion[y][x] = 1;
                    size++;
                    if (x < leftX) {
                        leftX = x;
                        leftY = y;
                    }
                    if (x > rightX) {
                        rightX = x;
                        rightY = y;
                    }
                    if (y < topY) {
                        topX = x;
                        topY = y;
                    }
                    if (y > bottomY) {
                        bottomX = x;
                        bottomY = y;
                    }
                    sumXCoords = sumXCoords + x;
                    sumYCoords = sumYCoords + y;

                    s.push(new Point(x, y + 1));
                    s.push(new Point(x + 1, y - 1));
                    s.push(new Point(x + 1, y));
                    s.push(new Point(x + 1, y + 1));
                }
            }
        }

        centreX = sumXCoords / size;
        centreY = sumYCoords / size;
        return new int[] { size, leftX, leftY, rightX, rightY, topX, topY,
                bottomX, bottomY, sumXCoords, sumYCoords, centreX, centreY };
    }

    /**
     * Determine the run length codes of all rows of the largest region.
     */
    public void determineRunCodesOfLargestRegion() {
        confineToLargestRegions(1);
        int[] numberOfAllChanges = new int[getHeight()];

        for (int y = 0; y < labelImage.getHeight(); y++) {
            int x = 0;
            while (x < labelImage.getWidth() && getPixel(x, y) != FOREGROUND) {
                x++;
            }
            if (x == labelImage.getWidth()) {
                numberOfAllChanges[y] = 0;
            } else {
                int lastPixel = this.getPixel(x, y);
                while (x < labelImage.getWidth()) {
                    if (getPixel(x, y) != lastPixel)
                        numberOfAllChanges[y]++;
                    lastPixel = this.getPixel(x, y);
                    x++;
                }
                if (lastPixel != FOREGROUND)
                    numberOfAllChanges[y]--;
            }
        }
        String s = "";
        for (int y = 0; y < labelImage.getHeight(); y++)
            s = s + "-" + numberOfAllChanges[y];
        ImageUtils.writeGrayscaleImage("CC\\" + instanceCounter
                + "-AnzahlWechsel-" + s + ".png", this);
    }

    /**
     * Removes all regions from the image but the largest regions
     */
    public void confineToLargestRegions(int largestRegions) {
        Collections.sort(regions);
        if (regions.size() > largestRegions)
            for (int i = 0; i < regions.size() - largestRegions; i++)
                removeRegion(regions.get(i).getAPoint());
    }

    /**
     * Removes all regions which are less than pSize
     */
    public void removeRegionsWhichAreLessThan(int pSize) {
        // System.out.println("BinaryImage: removeRegionsWhichAreLessThan "+pSize+" Pixel");
        for (Region r : regions)
            if (r.getArea() < pSize) {
                // System.out.println("Zu löschen: "+ r.getArea() +" Pixel");
                removeRegion(r.getAPoint());
            } else
                ;
        // System.out.println("Zu groß: "+ r.getArea() +" Pixel");
    }

    /**
     * Removes a region that contains the point 'start', that is, whiten the
     * pixels of this region
     */
    private void removeRegion(Point start) {
        Stack<Point> s = new Stack<Point>();
        s.push(start);
        while (!s.empty()) {
            Point p = s.pop();
            int x = (int) p.getX();
            int y = (int) p.getY();
            if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
                if (getPixel(x, y) == FOREGROUND) {
                    s.push(new Point(x - 1, y - 1));
                    s.push(new Point(x - 1, y));
                    s.push(new Point(x - 1, y + 1));
                    s.push(new Point(x, y - 1));
                    whitenPixel(x, y);
                    s.push(new Point(x, y + 1));
                    s.push(new Point(x + 1, y - 1));
                    s.push(new Point(x + 1, y));
                    s.push(new Point(x + 1, y + 1));
                }
            }
        }
    }

    /**
     * Returns the number of holes of all regions
     */
    public int getNumOfHoles() {
        int numOfHoles = 0;
        for (Region r : regions)
            numOfHoles = numOfHoles + r.getNumOfHoles();

        return numOfHoles;
    }

    /**
     * Determine the number of holes of a single region and return their
     * contours (start at index 1 ..) as well as the outer contour of this
     * region (index 0)
     * 
     * @param precondition
     *            : the array 'region' contains only one single connected region
     *            which size is > 1 pixel
     */
    private ArrayList<Contour> getContours(int[][] region) {
        ContourTracer ct = new ContourTracer(region, getWidth(), getHeight());
        ContourSet cs = ct.getContours();

        ArrayList<Contour> outerContourANDcontoursOfHoles = new ArrayList<Contour>();
        // there is only one (see precondition)
        outerContourANDcontoursOfHoles.add(cs.getOuterContours().get(0));
        for (Contour c : cs.getInnerContours())
            outerContourANDcontoursOfHoles.add(c);

        return outerContourANDcontoursOfHoles;
    }

    /**
     * Whiten all pixels of 'this' image whose x-coordinates are beyond the
     * x-coordinates of the parameter image pImg; this is tested in each row y
     * separately; keep pixels whose x-coordinates are around the centre of
     * subimage (far from left/right-borders)
     * 
     * RESULT: positive: the glyphs do overlap less strong with adjacent glyphs
     * negative: it leaves too large parts of the glyphs incorrectly undetected
     * 
     * idea: afterwards: reassign all pixels at the borders, left and right,
     * which pertain to connected components in original image, below a certain
     * threshold (rather small)
     * 
     * @param precondition
     *            pImg needs to have the same dimension as 'this'
     */
    public void keepForegroundPixelsWhichMatch(GrayImage pImg) {
        int xMin = Integer.MAX_VALUE;
        int xMax = 0;
        int xMinP = Integer.MAX_VALUE;
        int xMaxP = 0;

        for (int y = 0; y < this.getHeight(); y++) {
            for (int x = 0; x < this.getWidth(); x++) {
                if (getPixel(x, y) == FOREGROUND) {
                    if (x < xMin)
                        xMin = x;
                    if (x > xMax)
                        xMax = x;
                }
                if (pImg.getPixel(x, y) == FOREGROUND) {
                    if (x < xMinP)
                        xMinP = x;
                    if (x > xMaxP)
                        xMaxP = x;
                }
            }
            if (xMin == xMinP && xMax == xMaxP)
                ;// System.out.println("Region: keepForegroundPixelsWhichMatch: GLEICH in Zeile "+y);
            else {
                byte BORDER_DIST = 2;
                // System.out.println("Region: keepForegroundPixelsWhichMatch: UNGLEICH in Zeile "+y);
                for (int x = xMin; x <= xMax; x++)
                    if ((x < xMinP || x > xMaxP)
                            && ((x < BORDER_DIST) || (this.getWidth() - x < BORDER_DIST)))
                        whitenPixel(x, y);
            }
        }
    }

    /**
     * Whiten all pixels of 'this' image which are in the first few and last
     * columns RESULT: it shows that a few more glyphs can be detected if other
     * glyphs are cut out more strictly
     */
    public void whitenBorderColumns(int k) {
        if (k < this.getWidth() / 2) {
            for (int x = 0; x < k; x++)
                for (int y = 0; y < this.getHeight(); y++)
                    whitenPixel(x, y);

            for (int x = this.getWidth() - 1; x > this.getWidth() - k; x--)
                for (int y = 0; y < this.getHeight(); y++)
                    whitenPixel(x, y);
        }
    }

    public int[] matches(GrayImage pPattern) {
        int commonalities = 0;
        int differences = 0;

        // BinaryImage testImg = new BinaryImage(this.getPixelCloned(),
        // getWidth(), getHeight());
        // testImg.erosion();
        // testImg.labelConnectedComponents();

        // BinaryImage pattern = new BinaryImage(pPattern.getPixelCloned(),
        // getWidth(), getHeight());
        // pattern.erosion();
        // pattern.labelConnectedComponents();

        // RegionConfiguration r = testImg.getRegionConfiguration();
        // for (int y = 0; y < testImg.getHeight(); y++)
        // testImg.setToGrey((int)r.getCentre().getX(), y, 128);
        // for (int x = 0; x < testImg.getWidth(); x++)
        // testImg.setToGrey(x, (int)r.getCentre().getY(), 128);

        // RegionConfiguration p = pattern.getRegionConfiguration();
        // for (int y = 0; y < pattern.getHeight(); y++)
        // pattern.setToGrey((int)p.getCentre().getX(), y, 128);
        // for (int x = 0; x < pattern.getWidth(); x++)
        // pattern.setToGrey(x, (int)p.getCentre().getY(), 128);

        byte[] candidate = this.getPixels();
        byte[] pattern = pPattern.getPixels();
        int size = getWidth() * getHeight();
        for (int i = 0; i < size; i++)
            if (candidate[i] == FOREGROUND && pattern[i] == FOREGROUND)
                commonalities++;
            // testImg.setToGrey(x, y, FOREGROUND);
            // pattern.setToGrey(x, y, FOREGROUND);
            else if ((candidate[i] == FOREGROUND && pattern[i] == BACKGROUND)
                    || (candidate[i] == BACKGROUND && pattern[i] == FOREGROUND))
                differences++;
        // testImg.setToGrey(x, y, 140);
        // pattern.setToGrey(x, y, 140);

        // ImageUtils.writeGrayscaleImage("CC\\Bild-"+
        // instanceCounter+"-test.png", testImg);
        //
        // ImageUtils.writeGrayscaleImage("CC\\Bild-"+
        // instanceCounter+"-patt.png", pattern);

        // return commonalities > coverRatio * differences;
        return new int[] { commonalities, differences };
    }
}