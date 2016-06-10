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
import org.tzi.qsd.description.Course;
import org.tzi.qsd.description.PolygonalCourse;
import org.tzi.qsd.description.Scope;

import de.diptychon.models.algorithms.validation.featureExtraction.A_Feature;

public class PolygonalEnclosedness extends A_ScopeBasedFeature {

    public PolygonalEnclosedness(final PolygonalCourse polygonalCourse) {
        final Course[] courses = polygonalCourse.getCourses();
        final double[] enclosed = new double[courses.length];
        for (int index = 0; index < courses.length; index++) {
            final Enclosedness enclosedness = new Enclosedness(courses[index]);
            enclosed[index] = enclosedness.getDescription();
        }
        this.featureVector = new double[1];
        this.featureVector[0] = this.calculateAverage(enclosed);
    }

    public static class Enclosedness {
        private final int description;

        public Enclosedness(final Course course) {
            final Scope scope = new Scope(BipartiteArrangement.ALL);
            if (course.getScope() == scope.getScope()) {
                this.description = 1;
                return;
            }
            this.description = 0;
        }

        public int getDescription() {
            return this.description;
        }
    }

    /**
     * Factory class to create this feature
     */
    public static class Factory extends A_Feature.Factory {
        @Override
        public PolygonalEnclosedness createFeature(final PolygonalCourse course) {
            return new PolygonalEnclosedness(course);
        }
    }
}
