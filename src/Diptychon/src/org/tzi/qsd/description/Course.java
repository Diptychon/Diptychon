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
 * Based on: Arne Schuldt
 */
package org.tzi.qsd.description;

/**
 * The Course of a polygon relative to one of its segments. This class is used
 * by the Polygonal Course class. It can be regarded as a Scope. Furthermore it
 * provides a method returning its relations and another one returning a
 * condensed version of its relations. The latter one does not include
 * Indetermined relations.
 * <p>
 * This class is especially designed for use with simple, closed polygons, since
 * many subsequent classes require this restriction.
 */
public class Course extends Scope {

    private final BipartiteArrangement[] relations;

    /**
     * Constructs a Course from an array of Scope relations as provided from
     * PolygonalCourse
     * 
     * @param relations
     *            the Scope relations characterizing this Course
     */
    public Course(final BipartiteArrangement[] relations) {
        super(relations);
        this.relations = relations;
    }

    /**
     * Returns the relations.
     * 
     * @return the relations
     */
    public BipartiteArrangement[] getRelations() {
        return this.relations;
    }
}
