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

/**
 * Interface which provides basic functions for implement a reference system
 */
public interface ReferenceSystem {

    /**
     * Determines the position of a point within to this reference system.
     * 
     * @param point
     *            the point to be characterised
     * @return the position of the point within to this reference system
     */
    int position(final Point point);

    /**
     * Returns the number of positions this reference system can distinguish.
     * 
     * @return the number of positions this reference system can distinguish
     */
    int numPositions();

    /**
     * Returns the line on which this reference system is induced.
     * 
     * @return the line on which this reference system is induced
     */
    Line getReference();

    /**
     * Factory interface
     */
    public interface Factory {
        /**
         * Creates a new ReferenceSystem
         * 
         * @param start
         *            start point of the reference system
         * @param end
         *            end point of the reference system
         * @return the reference system
         */
        ReferenceSystem createReferenceSystem(final Point start, final Point end);
    }
}
