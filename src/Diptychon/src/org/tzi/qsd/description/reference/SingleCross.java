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

public class SingleCross implements ReferenceSystem {

    private static final double DEFAULT_EPSILON = 0;

    private static final int NUM_POSITIONS = 4;

    private static final int[] MAPPING = { 0, 0, 1, 1, 2, 2, 3, 3 };

    private final SingleCrossPlus cross;

    /**
     * Constructs the single cross induced by the reference line which is
     * defined by a given start and end point.
     * 
     * @param start
     *            the start point of the line inducing this reference system
     * @param end
     *            the end point of the line inducing this reference system
     */
    public SingleCross(final Point start, final Point end) {
        this(new Line(start.x, start.y, end.x, end.y));
    }

    /**
     * Constructs the single cross induced by the reference line.
     * 
     * @param reference
     *            the reference line inducing this reference system
     */
    public SingleCross(final Line reference) {
        this.cross = new SingleCrossPlus(reference, DEFAULT_EPSILON);
    }

    @Override
    public int position(final Point point) {
        return MAPPING[this.cross.position(point)];
    }

    @Override
    public int numPositions() {
        return NUM_POSITIONS;
    }

    @Override
    public Line getReference() {
        return this.cross.getReference();
    }

    public static class Factory implements ReferenceSystem.Factory {

        private final boolean opposite;

        public Factory() {
            this(false);
        }

        public Factory(final boolean opposite) {
            this.opposite = opposite;
        }

        @Override
        public SingleCross createReferenceSystem(final Point start,
                final Point end) {
            return new SingleCross(this.opposite ? end : start,
                    this.opposite ? start : end);
        }
    }
}
