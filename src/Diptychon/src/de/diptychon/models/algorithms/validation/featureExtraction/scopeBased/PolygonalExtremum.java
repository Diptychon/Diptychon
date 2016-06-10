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

/**
 * This class represents the Extremum of a course as suggested by Gottfried
 * (2005) and redefined on the basis of scope by Schuldt (2005).
 * <p>
 * PolygonalExtremum is the weighted average and maximum of the extremum of all
 * courses of a Polygon as described in Schuldt (2005).
 */
public class PolygonalExtremum extends A_ScopeBasedFeature {

    /**
     * Constructs the polygonal extremum for a given polygonal course.
     * 
     * @param polygonalCourse
     *            the polygonal course to be examined regarding its polygonal
     *            extremum
     */
    public PolygonalExtremum(final PolygonalCourse polygonalCourse) {
        final Course[] courses = polygonalCourse.getCourses();
        final double[] extrema = new double[courses.length];
        for (int index = 0; index < courses.length; index++) {
            final Extremum extremum = new Extremum(courses[index]);
            extrema[index] = extremum.getDescription();
        }
        this.featureVector = new double[1];
        this.featureVector[0] = this.calculateAverage(extrema);
    }

    public static class Extremum {
        private static final Scope[] EXTREMA = { new Scope(Scope.LEFT),
                new Scope(Scope.FRONT), new Scope(Scope.RIGHT),
                new Scope(Scope.BACK) };

        private final int description;

        /**
         * Constructs the extremum for a given course.
         * 
         * @param course
         *            the course to be examined regarding its extremum
         */
        public Extremum(final Course course) {
            for (final Scope extremum : EXTREMA) {
                final Scope union = course.createUnion(extremum);
                if (union.getScope() == extremum.getScope()) {
                    this.description = 1;
                    return;
                }
            }
            this.description = 0;
        }

        /**
         * Returns the description of this extremum.
         * 
         * @return the description of this extremum
         */
        public int getDescription() {
            return this.description;
        }
    }

    /**
     * Factory class to create this feature
     */
    public static class Factory extends A_Feature.Factory {
        @Override
        public PolygonalExtremum createFeature(final PolygonalCourse course) {
            return new PolygonalExtremum(course);
        }
    }
}
