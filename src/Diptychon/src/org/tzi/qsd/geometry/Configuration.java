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
package org.tzi.qsd.geometry;

import java.awt.geom.AffineTransform;
import java.util.Vector;

import de.diptychon.DiptychonLogger;

/**
 * A configuration groups a number of polygons.
 */
public class Configuration implements AffineTransformable, Cloneable {

    private final Vector<Polygon> configuration = new Vector<Polygon>();

    private Polygon[] polygons = null;

    /**
     * Constructs a configuration without polygons. Further polyons may be added
     * later with the
     * {@link de.diptychon.models.featureExtraction.qsd.geometry.Configuration#addPolygon(Polygon polygon)}
     * method.
     */
    public Configuration() {
    }

    /**
     * Constructs a configuration with one polygons. Further polyons may be
     * added later with the
     * {@link de.diptychon.models.featureExtraction.qsd.geometry.Configuration#addPolygon(Polygon polygon)}
     * method.
     * 
     * @param polygon
     *            the first polygon of this configuration
     */
    public Configuration(final Polygon polygon) {
        this.addPolygon(polygon);
    }

    /**
     * Adds the polygons of another configuration to this configuration.
     * 
     * @param other
     *            the configuration to be added
     */
    public void addConfiguration(final Configuration other) {
        for (final Polygon polygon : other.configuration) {
            this.addPolygon(polygon);
        }
    }

    /**
     * Adds a polygon to this configuration.
     * 
     * @param polygon
     *            the polygon to be added.
     */
    public void addPolygon(final Polygon polygon) {
        if (polygon != null) {
            this.configuration.add(polygon);
            this.polygons = null;
        }
    }

    /**
     * Removes a polygon from this configuration.
     * 
     * @param polygon
     *            the polygon to be removed.
     */
    public void removePolygon(final Polygon polygon) {
        this.configuration.remove(polygon);
        this.polygons = null;
    }

    /**
     * Returns the polygons of this configuration.
     * 
     * @return the polygons of this configuration
     */
    public Polygon[] getPolygons() {
        if (this.polygons == null) {
            this.polygons = this.configuration.toArray(new Polygon[0]);
        }
        return this.polygons;
    }

    @Override
    public void transform(final AffineTransform transform) {
        for (final Polygon polygon : this.configuration) {
            polygon.transform(transform);
        }
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            DiptychonLogger.error("{}", e);
        }
        return null;
    }
}
