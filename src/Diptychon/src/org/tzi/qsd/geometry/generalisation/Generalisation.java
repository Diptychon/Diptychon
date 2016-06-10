/*
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
package org.tzi.qsd.geometry.generalisation;

import org.tzi.qsd.geometry.Configuration;
import org.tzi.qsd.geometry.Point;
import org.tzi.qsd.geometry.Polygon;
import org.tzi.qsd.moments.Centroid;

/**
 * An abstract class representing generalisations of polygons. Examples for a
 * generalisation is
 * {@link de.diptychon.models.featureExtraction.qsd.geometry.generalisation.Approximation}
 * .
 */
public abstract class Generalisation {

    /**
     * Generalises a configuration of polygons.
     * 
     * @param configuration
     *            the configuration to be generalised
     * @return the generalised configuration
     */
    public Configuration generalise(final Configuration configuration) {
        final Configuration generalised = new Configuration();
        for (final Polygon polygon : configuration.getPolygons()) {
            generalised.addConfiguration(this.generalise(polygon));
        }
        return generalised;
    }

    /**
     * Generalises a polygon. The concrete result depends on the actual
     * implementation. The result is configuration of polygons as some
     * generalisations might result in multiple polygons.
     * 
     * @param polygon
     *            the polygon to be generalised
     * @return the generalised configuration
     */
    public abstract Configuration generalise(final Polygon polygon);

    protected static int findMostDistantPoint(final Polygon polygon) {
        final Point center = new Centroid(polygon);
        final Point[] points = polygon.getPoints();
        int mostDistantPoint = 0;
        double maximumDistance = -1;
        for (int index = 0; index < points.length; index++) {
            final Point point = points[index];
            final double distance = center.distance(point);
            if (maximumDistance == -1 || maximumDistance < distance) {
                maximumDistance = distance;
                mostDistantPoint = index;
            }
        }
        return mostDistantPoint;
    }

    protected static Polygon shift(final Polygon polygon, final int start) {
        final int length = polygon.getPoints().length;
        if (!polygon.isClosed() || length < 2) {
            return polygon;
        }
        final int end = (length + start - 1) % length;
        final Polygon shiftedPolygon = polygon.getPolyline(start, end);
        shiftedPolygon.close();
        return shiftedPolygon;
    }
}
