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

public class SingleCrossPlus implements ReferenceSystem {

    private static final double DEFAULT_EPSILON = 1E-5;

    private static final double PERIOD = 2 * Math.PI;

    private static final int NUM_SINGULAR_POSITIONS = 4;

    private static final int NUM_GENERAL_POSITIONS = 4;

    private static final int NUM_POSITIONS = NUM_SINGULAR_POSITIONS
            + NUM_GENERAL_POSITIONS;

    private final Line reference;

    private final double epsilon;

    /**
     * Constructs the single cross+ (with singulariy precision) induced by the
     * reference line which is defined by a given start and end point.
     * 
     * @param start
     *            the start point of the line inducing this reference system
     * @param end
     *            the end point of the line inducing this reference system
     */
    public SingleCrossPlus(final Point start, final Point end) {
        this(new Line(start.x, start.y, end.x, end.y));
    }

    /**
     * Constructs the single cross+ (with singulariy precision) induced by the
     * reference line which is defined by a given start and end point.
     * 
     * @param start
     *            the start point of the line inducing this reference system
     * @param end
     *            the end point of the line inducing this reference system
     * @param e
     *            the precision for determining singular positions
     */
    public SingleCrossPlus(final Point start, final Point end, final double e) {
        this(new Line(start.x, start.y, end.x, end.y), e);
    }

    /**
     * Constructs the single cross+ (with singulariy precision) induced by the
     * reference line.
     * 
     * @param reference
     *            the reference line inducing this reference system
     */
    public SingleCrossPlus(final Line reference) {
        this(reference, DEFAULT_EPSILON);
    }

    /**
     * Constructs the single cross+ (with singulariy precision) induced by the
     * reference line.
     * 
     * @param r
     *            the reference line inducing this reference system
     * @param e
     *            the precision for determining singular positions
     */
    public SingleCrossPlus(final Line r, final double e) {
        this.reference = r;
        this.epsilon = e;
    }

    @Override
    public int position(final Point point) {
        final double angle = this.turnAngle(point) / PERIOD * 4;
        final int discrete = (int) Math.round(angle);
        if (Math.abs(discrete - angle) < this.epsilon) {
            return (discrete * 2) % NUM_POSITIONS;
        }
        return ((int) angle * 2 + 1) % NUM_POSITIONS;
    }

    @Override
    public int numPositions() {
        return NUM_POSITIONS;
    }

    @Override
    public Line getReference() {
        return this.reference;
    }

    private double turnAngle(final Point point) {
        final Line primary = new Line(this.reference.getEndX(),
                this.reference.getEndY(), point.x, point.y);
        return (PERIOD + this.lineAngle(this.reference) - this
                .lineAngle(primary)) % PERIOD;
    }

    private double lineAngle(final Line line) {
        final double width = line.getEndX() - line.getStartX();
        final double height = line.getEndY() - line.getStartY();
        final double diameter = Math.sqrt(Math.pow(
                line.getStartX() - line.getEndX(), 2)
                + Math.pow(line.getStartY() - line.getEndY(), 2));
        final double sin = Math.asin(-height / diameter);
        final double cos = Math.acos(width / diameter);
        if (Math.abs(sin - cos) < 1E-5) {
            return sin;
        } else if (Math.abs(sin + cos) < 1E-5) {
            return 2 * Math.PI - cos;
        }
        return Math.PI - sin;
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
        public SingleCrossPlus createReferenceSystem(final Point start,
                final Point end) {
            return new SingleCrossPlus(this.opposite ? end : start,
                    this.opposite ? start : end);
        }
    }
}
