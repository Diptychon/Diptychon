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
package de.diptychon.models.algorithms.validation.featureExtraction.scopeBased;

import de.diptychon.models.algorithms.validation.featureExtraction.A_Feature;

/**
 * Feature which are based on the scope should extent this class.
 */
public class A_ScopeBasedFeature extends A_Feature {

    /**
     * Calculates the average of a scope based feature.
     * 
     * @param value
     *            All extracted values
     * @return the average.
     */
    protected double calculateAverage(final double[] value) {
        double average = 0;
        for (int index = 0; index < value.length; index++) {
            average += value[index];
        }
        if (value.length == 0) {
            return average;
        }
        return average / value.length;
    }
}
