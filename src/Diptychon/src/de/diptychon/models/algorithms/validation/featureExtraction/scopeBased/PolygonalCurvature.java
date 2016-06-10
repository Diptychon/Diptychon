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

import org.tzi.qsd.description.Course;
import org.tzi.qsd.description.PolygonalCourse;
import org.tzi.qsd.description.Scope;

import de.diptychon.models.algorithms.validation.featureExtraction.A_Feature;
import de.diptychon.models.algorithms.validation.featureExtraction.scopeBased.PolygonalExtent.Extent;

/**
 * This class represents the Curvature of a course as suggested by Gottfried
 * (2005) and redefined on the basis of scope by Schuldt (2005). The version at
 * hand is again redefined on the basis of the scope with six basic relations.
 * Hence, it distinguishes from the definitions published by Schuldt (2005) and
 * Gottfried (2007) in that the new definition is much simpler.
 * <p>
 * The PolygonalCurvature is the weighted average and maximum of the curvatures
 * of all courses of a polygon as described in Schuldt (2005).
 */
public class PolygonalCurvature extends A_ScopeBasedFeature {
    /**
     * Constructs the polygonal curvature for a given polygonal course.
     * 
     * @param polygonalCourse
     *            the polygonal course to be examined regarding its polygonal
     *            curvature
     */
    public PolygonalCurvature(final PolygonalCourse polygonalCourse) {
        final Course[] courses = polygonalCourse.getCourses();
        final double[] curvatures = new double[courses.length];
        for (int index = 0; index < courses.length; index++) {
            final Curvature curvature = new Curvature(courses[index]);
            curvatures[index] = 1 - 1.0 / curvature.getDescription();
            final double description = curvature.getDescription();
            if (description != 0) {
                curvatures[index] = 1 - 1.0 / description;
            } else {
                curvatures[index] = 0;
            }
        }
        this.featureVector = new double[1];
        this.featureVector[0] = this.calculateAverage(curvatures);
    }

    public static class Curvature {

        private final int description;

        /**
         * Constructs the curvature for a given course.
         * 
         * @param course
         *            the course to be examined regarding its curvature
         */
        public Curvature(final Course course) {
            int curvature = 0;
            final Scope[] relations = course.getRelations();
            for (int index = 0; index < relations.length; index++) {
                final Scope scope = relations[index];
                final int extent = new Extent(scope).getDescription();
                curvature += Math.max(0, extent - 1);
            }
            this.description = curvature;
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
        public PolygonalCurvature createFeature(final PolygonalCourse course) {
            return new PolygonalCurvature(course);
        }
    }
}
