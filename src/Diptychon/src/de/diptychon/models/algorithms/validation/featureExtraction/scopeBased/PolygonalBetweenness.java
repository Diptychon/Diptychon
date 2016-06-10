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

import org.tzi.qsd.description.BipartiteArrangement;
import org.tzi.qsd.description.PolygonalCourse;
import org.tzi.qsd.description.Scope;

import de.diptychon.models.algorithms.validation.featureExtraction.A_Feature;

public class PolygonalBetweenness extends A_ScopeBasedFeature {

    public PolygonalBetweenness(final PolygonalCourse polygonalCourse) {
        final BipartiteArrangement[][] matrix = polygonalCourse
                .getDescription();
        int between = 0;
        int total = 0;
        for (int first = 0; first < matrix.length; first++) {
            for (int second = 0; second < matrix.length; second++) {
                for (int third = 0; third < matrix.length; third++) {
                    if (first == second || second == third || third == first) {
                        continue;
                    }
                    final Betweenness betweenness = new Betweenness(
                            matrix[first][third], matrix[first][second],
                            matrix[third][first], matrix[third][second]);
                    between += betweenness.getDescription();
                    total++;
                }
            }
        }
        this.featureVector = new double[1];
        this.featureVector[0] = between / (double) total;
    }

    public static class Betweenness {
        private final int description;

        public Betweenness(final Scope first, final Scope second,
                final Scope third, final Scope fourth) {
            final int firstExtent = this.intersect(first, second);
            final int thirdExtent = this.intersect(third, fourth);
            this.description = (firstExtent > 0 && thirdExtent > 0) ? 1 : 0;
        }

        public int getDescription() {
            return this.description;
        }

        private int intersect(final Scope first, final Scope second) {
            return first.createIntersection(second).getExtent()
                    .getDescription();
        }
    }

    /**
     * Factory class to create this feature
     */
    public static class Factory extends A_Feature.Factory {
        @Override
        public PolygonalBetweenness createFeature(final PolygonalCourse course) {
            return new PolygonalBetweenness(course);
        }
    }
}
