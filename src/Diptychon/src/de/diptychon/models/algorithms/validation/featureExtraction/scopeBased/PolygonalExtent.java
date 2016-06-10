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
 * The Extent counts the number of BA12 relations a scope consists of. For all
 * BA6 relations the return value is 1; BA23 objects may be represented by up to
 * four BA6 relations. The bit-wise internal representation of BA corresponds to
 * the scope of the relation. Therefore the number of relations represents the
 * extent of the relation's scope.
 * <p>
 * The PolygonalExtent is the weighted average and maximum of the extents of all
 * courses of a polygon as described in Schuldt (2005).
 */
public class PolygonalExtent extends A_ScopeBasedFeature {

    /**
     * Constructs the polygonal extent for a given polygonal course.
     * 
     * @param polygonalCourse
     *            the polygonal course to be examined regarding its polygonal
     *            extent
     */
    public PolygonalExtent(final PolygonalCourse polygonalCourse) {
        final Course[] courses = polygonalCourse.getCourses();
        final double[] extents = new double[courses.length];
        for (int index = 0; index < courses.length; index++) {
            final Extent extent = new Extent(courses[index]);
            extents[index] = (double) extent.getDescription()
                    / Scope.MAX_EXTENT;
        }
        this.featureVector = new double[1];
        this.featureVector[0] = this.calculateAverage(extents);
    }

    public static class Extent {
        private final int description;

        /**
         * Constructs the extent for a given scope.
         * 
         * @param scope
         *            the scope to be examined regarding its extent
         */
        public Extent(final Scope scope) {
            final int ba6 = scope.getScope();
            int extent = 0;
            for (int bit = Scope.B_l; bit <= Scope.B_r; bit = bit << 1) {
                if ((bit & ba6) != 0) {
                    extent++;
                }
            }
            this.description = extent;
        }

        /**
         * Returns the description of this extent.
         * 
         * @return the description of this extent
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
        public PolygonalExtent createFeature(final PolygonalCourse course) {
            return new PolygonalExtent(course);
        }
    }
}
