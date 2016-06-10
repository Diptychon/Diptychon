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
 * Based on: Workinggroup Qualitative Shape Description (University Bremen)
 */
package org.tzi.qsd.description;

import java.awt.geom.Line2D;
import java.util.ArrayList;

import org.tzi.qsd.description.reference.DoubleCross;
import org.tzi.qsd.geometry.Configuration;
import org.tzi.qsd.geometry.Point;
import org.tzi.qsd.geometry.Polygon;

import javafx.scene.shape.Line;

/**
 * A Bipartite Arrangement (BA) is the global orientation of two line segments.
 * It can be determined by applying an orientation grid.
 */
public class BipartiteArrangement extends Scope {

    private static final int[] MAPPING = { 3, 4, 5, 0, 1, 2 };

    /**
     * Represents the identity BA relation ID
     */
    private static final int ID = 0;

    /**
     * Represents the back overlaps left BA relation BO<sub>l</sub>
     */
    private static final int BO_l = B_l | D_l;

    /**
     * Represents the front overlaps left BA relation FO<sub>l</sub>
     */
    private static final int FO_l = D_l | F_l;

    /**
     * Represents the front middle BA relation F<sub>m</sub>
     */
    private static final int F_m = F_l | F_r;

    /**
     * Represents the front overlaps right BA relation FO<sub>r</sub>
     */
    private static final int FO_r = F_r | D_r;

    /**
     * Represents the back overlaps right BA relation BO<sub>r</sub>
     */
    private static final int BO_r = D_r | B_r;

    /**
     * Represents the back middle BA relation B<sub>m</sub>
     */
    private static final int B_m = B_r | B_l;

    /**
     * Represents the back overlaps middle left BA relation BO<sub>ml</sub>
     */
    private static final int BO_ml = B_r | B_l | D_l;

    /**
     * Represents the back contains left BA relation BC<sub>l</sub>
     */
    private static final int BC_l = B_r | B_l | D_l | F_l;

    /**
     * Represents the contains left BA relation C<sub>l</sub>
     */
    private static final int C_l = B_l | D_l | F_l;

    /**
     * Represents the front contains left BA relation FC<sub>l</sub>
     */
    private static final int FC_l = B_l | D_l | F_l | F_r;

    /**
     * Represents the front overlaps middle left BA relation BO<sub>ml</sub>
     */
    private static final int FO_ml = D_l | F_l | F_r;

    /**
     * Represents the front overlaps middle right BA relation BO<sub>mr</sub>
     */
    private static final int FO_mr = F_l | F_r | D_r;

    /**
     * Represents the front contains right BA relation FC<sub>r</sub>
     */
    private static final int FC_r = F_l | F_r | D_r | B_r;

    /**
     * Represents the contains right BA relation C<sub>r</sub>
     */
    private static final int C_r = F_r | D_r | B_r;

    /**
     * Represents the back contains right BA relation BC<sub>r</sub>
     */
    private static final int BC_r = F_r | D_r | B_r | B_l;

    /**
     * Represents the back overlaps middle right BA relation BO<sub>mr</sub>
     */
    private static final int BO_mr = D_r | B_r | B_l;

    /**
     * Represents all BA relations (Universal Scope)
     */
    public static final int ALL = LEFT | RIGHT;

    /**
     * Represents the BA<sub>23</sub> relations
     */
    private static final int[] BA_23 = { ID, B_l, BO_l, D_l, FO_l, F_l, F_m,
            F_r, FO_r, D_r, BO_r, B_r, B_m, BO_ml, BC_l, C_l, FC_l, FO_ml,
            FO_mr, FC_r, C_r, BC_r, BO_mr };

    /**
     * Represents the BA<sub>23</sub> relations as Strings
     */
    private static final String[] BA_23_STRINGS = { "ID", "B_l", "BO_l", "D_l",
            "FO_l", "F_l", "F_m", "F_r", "FO_r", "D_r", "BO_r", "B_r", "B_m",
            "BO_ml", "BC_l", "C_l", "FC_l", "FO_ml", "FO_mr", "FC_r", "C_r",
            "BC_r", "BO_mr" };

    private static final int C_INDETERMINED = ALL;

    private static final int[][] BIPARTITE_ARRANGEMENTS = new int[][] {
            { B_l, BO_l, C_l, C_INDETERMINED, BO_mr, B_m },
            { BO_l, D_l, FO_l, FO_ml, ID, BO_ml },
            { C_l, FO_l, F_l, F_m, FO_mr, C_INDETERMINED },
            { C_INDETERMINED, FO_ml, F_m, F_r, FO_r, C_r },
            { BO_mr, ID, FO_mr, FO_r, D_r, BO_r },
            { B_m, BO_ml, C_INDETERMINED, C_r, BO_r, B_r } };

    private static final int[] START_POINT_IDENTITY = new int[] { B_l, D_l,
            FO_l, FO_r, D_r, B_r };

    private static final int[] END_POINT_IDENTITY = new int[] { BO_l, D_l, F_l,
            F_r, D_r, BO_r };

    /**
     * Constructs a BA object defined by the provided description.
     * 
     * @param description
     *            the description of this BA object
     */
    public BipartiteArrangement(final int description) {
        super(description);
    }

    @Override
    public String toString() {
        for (int index = 0; index < BA_23.length; index++) {
            if (this.scope == BA_23[index]) {
                return BA_23_STRINGS[index];
            }
        }
        return "";
    }

    /**
     * Determines the Bipartite Arrangement (BA) of the reference line and the
     * provided primary line.
     * 
     * @param reference
     *            the line that induces the orientation grid
     * @param primary
     *            the line which's position is to be determined relative to the
     *            primary line
     * @return the BA describing the global orientation
     */
    public static BipartiteArrangement createDescription(final Line reference,
            final Line primary) {
        return createDescription(new DoubleCross(reference), primary);
    }

    /**
     * Creates the BA description for the provided polygon.
     * 
     * @param polygon
     *            the polygon to be described
     * @return the BA description of this polygon
     */
    public static BipartiteArrangement[][] createDescription(
            final Polygon polygon) {
        final Line[] lines = polygon.getLines();
        return BipartiteArrangement.createDescription(lines);
    }

    public static BipartiteArrangement[][] createDescription(
            final Configuration configuration) {
        final ArrayList<Line> allLines = new ArrayList<Line>();
        for (final Polygon polygon : configuration.getPolygons()) {
            final Line[] lines = polygon.getLines();
            for (final Line line : lines) {
                allLines.add(line);
            }
        }
        Line[] lines = new Line[allLines.size()];
        lines = allLines.toArray(lines);
        return BipartiteArrangement.createDescription(lines);
    }

    public static BipartiteArrangement[][] createDescription(final Line[] lines) {
        final BipartiteArrangement[][] bas = new BipartiteArrangement[lines.length][lines.length];
        for (int row = 0; row < lines.length; row++) {
            final Line reference = lines[row];
            final DoubleCross cross = new DoubleCross(reference);
            for (int col = 0; col < lines.length; col++) {
                final Line primary = lines[col];
                bas[row][col] = createDescription(cross, primary);
            }
        }
        return bas;
    }

    private static BipartiteArrangement createDescription(
            final DoubleCross cross, final Line primary) {
        final Line reference = cross.getReference();
        if (Line2D.linesIntersect(reference.getStartX(), reference.getStartY(),
                reference.getEndX(), reference.getEndX(), primary.getStartX(),
                primary.getStartY(), primary.getEndX(), primary.getEndX())
                && !checkOneCommonPoint(reference, primary)) {
            return new BipartiteArrangement(ID);
        }
        if (checkOneCommonPoint(reference, primary)) {
            return new BipartiteArrangement(createCommonPointDescription(cross,
                    primary));
        }
        final int first = MAPPING[cross.position(new Point(primary.getStartX(),
                primary.getStartY()))];
        final int second = MAPPING[cross.position(new Point(primary.getEndX(),
                primary.getEndY()))];
        final int description = BIPARTITE_ARRANGEMENTS[first][second];
        if (description == C_INDETERMINED) {
            final DoubleCross doubleCross = new DoubleCross(primary);
            final int sector = MAPPING[doubleCross.position(new Point(reference
                    .getStartX(), reference.getStartY()))];
            final boolean left = (ATOMIC[sector] == D_r);
            switch (ATOMIC[first]) {
            case B_l:
                return new BipartiteArrangement(left ? FC_l : BC_r);
            case F_l:
                return new BipartiteArrangement(left ? FC_r : BC_l);
            case F_r:
                return new BipartiteArrangement(left ? BC_r : FC_l);
            case B_r:
                return new BipartiteArrangement(left ? BC_l : FC_r);
            }
        }
        return new BipartiteArrangement(description);
    }

    private static boolean checkOneCommonPoint(final Line first,
            final Line second) {
        final boolean startPointIntersection = (first.getStartX() == second
                .getStartX() && first.getStartY() == second.getStartY())
                || (first.getStartX() == second.getEndX() && first.getStartY() == second
                        .getEndY());
        final boolean endPointIntersection = (first.getEndX() == second
                .getStartX() && first.getEndY() == second.getStartY())
                || (first.getEndX() == second.getEndX() && first.getEndY() == second
                        .getEndY());
        return startPointIntersection ^ endPointIntersection;
    }

    private static int createCommonPointDescription(final DoubleCross cross,
            final Line primary) {
        final Line reference = cross.getReference();
        if (reference.getStartX() == primary.getStartX()
                && reference.getStartY() == primary.getStartY()) {
            final int position = MAPPING[cross.position(new Point(primary
                    .getEndX(), primary.getEndY()))];
            return START_POINT_IDENTITY[position];
        } else if (reference.getStartX() == primary.getEndX()
                && reference.getStartY() == primary.getEndY()) {
            final int position = MAPPING[cross.position(new Point(primary
                    .getStartX(), primary.getStartY()))];
            return START_POINT_IDENTITY[position];
        } else if (reference.getEndX() == primary.getStartX()
                && reference.getEndY() == primary.getStartY()) {
            final int position = MAPPING[cross.position(new Point(primary
                    .getEndX(), primary.getEndY()))];
            return END_POINT_IDENTITY[position];
        } else {
            final int position = MAPPING[cross.position(new Point(primary
                    .getStartX(), primary.getStartY()))];
            return END_POINT_IDENTITY[position];
        }
    }
}
