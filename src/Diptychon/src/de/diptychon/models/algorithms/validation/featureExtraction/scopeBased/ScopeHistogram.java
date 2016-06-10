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

public class ScopeHistogram extends A_ScopeBasedFeature {

    private ScopeHistogram(final PolygonalCourse course) {
        this.computeFrequencies(course);
    }

    private void computeFrequencies(final PolygonalCourse course) {
        final double[] lengths = course.getRelativeLengths();
        final Course[] courses = course.getCourses();
        this.featureVector = new double[Scope.NUM_SCOPES];
        for (int index = 0; index < courses.length; index++) {
            this.featureVector[courses[index].getScope()] += lengths[index];
        }
    }

    /**
     * Factory class to create this feature
     */
    public static class Factory extends A_Feature.Factory {
        @Override
        public ScopeHistogram createFeature(final PolygonalCourse course) {
            return new ScopeHistogram(course);
        }
    }
}
