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
package org.tzi.qsd.description.reference;

import org.tzi.qsd.geometry.Point;

import javafx.scene.shape.Line;

public class DoubleCross implements ReferenceSystem {

    private static final int NUM_POSITIONS = 6;

    private static final int[][] MAPPING = { { -1, 0, 0, -1 }, { 3, -1, 1, 2 },
            { 3, 4, -1, 3 }, { -1, 5, 0, -1 } };

    private final SingleCross front;

    private final SingleCross back;

    /**
     * Constructs the double cross induced by the reference line which is
     * defined by a given start and end point.
     * 
     * @param start
     *            the start point of the line inducing this reference system
     * @param end
     *            the end point of the line inducing this reference system
     */
    public DoubleCross(final Point start, final Point end) {
        this.front = new SingleCross(start, end);
        this.back = new SingleCross(end, start);
    }

    /**
     * Constructs the double cross induced by the reference line.
     * 
     * @param reference
     *            the reference line inducing this reference system
     */
    public DoubleCross(final Line reference) {
        this(new Point(reference.getStartX(), reference.getStartY()),
                new Point(reference.getEndX(), reference.getEndY()));
    }

    /**
     * Determines the position of a point within to this reference system.
     * 
     * @param x
     *            the x coordinate of the point to be characterised
     * @param y
     *            the y coordinate of the point to be characterised
     * @return the position of the point within to this reference system
     */
    public int position(final int x, final int y) {
        return this.position(new Point(x, y));
    }

    @Override
    public int position(final Point point) {
        return MAPPING[this.front.position(point)][this.back.position(point)];
    }

    @Override
    public int numPositions() {
        return NUM_POSITIONS;
    }

    @Override
    public Line getReference() {
        return this.front.getReference();
    }

    /**
     * {@inheritdoc}
     */
    public static class Factory implements ReferenceSystem.Factory {

        private final boolean opposite;

        public Factory() {
            this(false);
        }

        public Factory(final boolean opposite) {
            this.opposite = opposite;
        }

        @Override
        public DoubleCross createReferenceSystem(final Point start,
                final Point end) {
            return new DoubleCross(this.opposite ? end : start,
                    this.opposite ? start : end);
        }
    }
}
