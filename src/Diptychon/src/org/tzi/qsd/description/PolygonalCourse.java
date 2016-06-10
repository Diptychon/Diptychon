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
 *
 * Based on: Arne Schuldt
 */
package org.tzi.qsd.description;

import java.util.ArrayList;

import org.tzi.qsd.geometry.Configuration;
import org.tzi.qsd.geometry.Polygon;

import javafx.scene.shape.Line;

/**
 * The Polygonal Course containing the Courses of every segment of the polyon.
 * <p>
 * Since every Course object is a Scope, the Polygonal Course corresponds to the
 * Polygonal Scope for simple, closed polygons.
 */
public class PolygonalCourse {

    private static final int COLUMN_SPACING = 2;

    private final Course[] courses;

    private final BipartiteArrangement[][] matrix;

    private boolean open;

    private double[] relativeLenghts;

    /**
     * Constructs a PolygonalCourse for the provided Polygon.
     * 
     * @param polygon
     *            the polygon to be characterized by this PolygonalCourse
     */
    public PolygonalCourse(final Polygon polygon) {
        this.open = !polygon.isClosed();
        this.matrix = BipartiteArrangement.createDescription(polygon);
        this.courses = new Course[polygon.getLines().length];
        for (int index = 0; index < this.matrix.length; index++) {
            this.courses[index] = new Course(this.matrix[index]);
        }
    }

    public double[] getRelativeLengths() {
        return this.relativeLenghts;
    }

    public PolygonalCourse(final Configuration configuration) {
        this.open = true;
        final ArrayList<Line> allLines = new ArrayList<Line>();

        double length = 0.0;
        for (final Polygon polygon : configuration.getPolygons()) {
            this.open &= !polygon.isClosed();
            final Line[] lines = polygon.getLines();
            for (final Line line : lines) {
                allLines.add(line);
            }
            length += polygon.length();
        }
        Line[] lines = new Line[allLines.size()];
        lines = allLines.toArray(lines);
        this.matrix = BipartiteArrangement.createDescription(lines);
        this.courses = new Course[lines.length];
        for (int index = 0; index < this.matrix.length; index++) {
            this.courses[index] = new Course(this.matrix[index]);
        }

        this.relativeLenghts = new double[lines.length];
        for (int index = 0; index < lines.length; index++) {
            final Line line = lines[index];
            final double lineLength = Math.sqrt(Math.pow(line.getStartX()
                    - line.getEndX(), 2)
                    + Math.pow(line.getStartY() - line.getEndY(), 2));
            this.relativeLenghts[index] = lineLength / length;
        }

    }

    /**
     * Checks whether the characterized polygon is open or not.
     * 
     * @return true if the characterized polygon is open, false otherwise
     */
    public boolean isOpen() {
        return this.open;
    }

    /**
     * Returns the internal Scope matrix of this PolygonalCourse.
     * 
     * @return the internal Scope matrix
     */
    public BipartiteArrangement[][] getDescription() {
        return this.matrix;
    }

    /**
     * Returns the Courses of this PolygonalCourse.
     * 
     * @return the Courses
     */
    public Course[] getCourses() {
        return this.courses;
    }

    @Override
    public String toString() {
        final int firstColumnWidth = ("" + (this.matrix.length + 1)).length();
        final int[] columnWidths = new int[this.matrix.length];
        for (int col = 0; col < columnWidths.length; col++) {
            columnWidths[col] = this.calculateColumnWidth(col);
        }
        final StringBuffer course = new StringBuffer();
        course.append(this.pad("", firstColumnWidth + COLUMN_SPACING));
        for (int col = 0; col < this.matrix.length; col++) {
            course.append(this.pad("" + (col + 1), columnWidths[col]
                    + COLUMN_SPACING));
        }
        course.append("\n");
        for (int row = 0; row < this.matrix.length; row++) {
            course.append(this.pad("" + (row + 1), firstColumnWidth
                    + COLUMN_SPACING));
            for (int col = 0; col < this.matrix[row].length; col++) {
                course.append(this.pad(this.matrix[row][col].toString(),
                        columnWidths[col] + COLUMN_SPACING));
            }
            course.append("\n");
        }
        return course.toString();
    }

    private int calculateColumnWidth(final int col) {
        int columnWidth = ("" + (col + 1)).length();
        for (int row = 0; row < this.matrix.length; row++) {
            final int width = this.matrix[row][col].toString().length();
            columnWidth = Math.max(columnWidth, width);
        }
        return columnWidth;
    }

    private StringBuffer pad(final String string, final int length) {
        final StringBuffer result = new StringBuffer();
        result.append(string);
        for (int index = 0; index < length - string.length(); index++) {
            result.append(" ");
        }
        return result;
    }
}
