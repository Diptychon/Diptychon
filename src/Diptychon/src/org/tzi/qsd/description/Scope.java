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

import java.awt.geom.AffineTransform;

import org.tzi.qsd.geometry.Point;

import de.diptychon.models.algorithms.validation.featureExtraction.scopeBased.PolygonalExtent.Extent;

/**
 * According to Gottfried (2005) and Schuldt (2005) Bipartite Arrangements (BA),
 * Tripartite Line Tracks (TLT), and Courses can be represented by their Scope.
 * The Scope is the set of atomic BA_6 relations indicating the position of a
 * relation or course relative to an intrinsic reference system, namely the
 * Orientation Grid.
 * <p>
 * Therefore the Scope is the class underlying the BA, TLT and Course classes.
 * <p>
 * This class is especially designed for use with simple, closed polygons, since
 * many subsequent classes require this restriction.
 */

public class Scope implements Comparable<Scope> {

    /**
     * Represents the back left BA relation B<sub>l</sub>
     */
    public static final int B_l = 1 << 0;

    /**
     * Represents the during left BA relation D<sub>l</sub>
     */
    public static final int D_l = 1 << 1;

    /**
     * Represents the front left BA relation F<sub>l</sub>
     */
    public static final int F_l = 1 << 2;

    /**
     * Represents the front right BA relation F<sub>r</sub>
     */
    public static final int F_r = 1 << 3;

    /**
     * Represents the during right BA relation D<sub>r</sub>
     */
    public static final int D_r = 1 << 4;

    /**
     * Represents the back right BA relation B<sub>r</sub>
     */
    public static final int B_r = 1 << 5;

    /**
     * Represents the set of atomic BA relations
     */
    public static final int[] ATOMIC = { B_l, D_l, F_l, F_r, D_r, B_r };

    public static final int LEFT = B_l | D_l | F_l;

    public static final int FRONT = F_l | F_r;

    public static final int RIGHT = F_r | D_r | B_r;

    public static final int BACK = B_r | B_l;

    public static final int NUM_SCOPES = (int) Math.pow(2, ATOMIC.length);

    public static final int MAX_EXTENT = ATOMIC.length;

    protected final int scope;

    private Extent extent = null;

    private int firstAtomicRelation = -1;

    /**
     * Constructs an empty Scope.
     */
    public Scope() {
        this.scope = 0;
    }

    /**
     * Constructs a Scope from a given bit vector representation.
     * 
     * @param scope
     *            bit vector representation for the scope to be constructed
     */
    public Scope(final int scope) {
        this.scope = scope;
    }

    protected Scope(final Scope[] relations) {
        Scope scope = new Scope();
        for (int index = 0; index < relations.length; index++) {
            if (relations[index] != null) {
                scope = scope.createUnion(relations[index]);
            }
        }
        this.scope = scope.scope;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Scope)) {
            return false;
        }
        final Scope other = (Scope) o;
        return this.scope == other.scope;
    }

    @Override
    public int hashCode() {
        return this.getScope();
    }

    @Override
    public int compareTo(final Scope other) {
        if (this.scope == other.scope) {
            return 0;
        }
        if (this.getFirstAtomicRelation() != other.getFirstAtomicRelation()) {
            return (this.getFirstAtomicRelation() < other
                    .getFirstAtomicRelation()) ? -1 : 1;
        }
        return (this.getExtent().getDescription() < other.getExtent()
                .getDescription()) ? -1 : 1;
    }

    /**
     * Returns the scope of the object described by this object.
     * 
     * @return the scope of the object
     */
    public int getScope() {
        return this.scope;
    }

    public boolean isGapless() {
        final int newExtent = this.getExtent().getDescription();
        if (newExtent == 0 || newExtent == MAX_EXTENT) {
            return true;
        }
        int index = 0;
        while (!contains(this.scope, index % MAX_EXTENT)) {
            index++;
        }
        while (contains(this.scope, index % MAX_EXTENT)) {
            index++;
        }
        int count = 0;
        while (!contains(this.scope, index % MAX_EXTENT)) {
            index++;
            count++;
        }
        return MAX_EXTENT - count == newExtent;
    }

    /**
     * Checks whether this scope can be realized by a simple closed Polygon, as
     * defined in Schuldt (2005)
     * 
     * @return true if this scope can be realized by a simple closed polygon,
     *         false otherwise
     */
    public boolean isRealizableBySimpleClosedPolygon() {
        return this.isGapless() && contains(this.scope, 1);
    }

    /**
     * Determines the position of this scope in the visualization with center in
     * origin and radius one.
     * 
     * @return the position of this scope in a signature visualization
     */
    public Point getSignaturePosition() {
        final int newExtent = this.getExtent().getDescription();
        final Point position = new Point((MAX_EXTENT - newExtent)
                / (double) Scope.MAX_EXTENT, 0);
        final int newFirstAtomicRelation = this.getFirstAtomicRelation();
        final double angle = 2
                * Math.PI
                * (/* 21 */11 - (newFirstAtomicRelation + newExtent / 2.0 - 0.5))
                / MAX_EXTENT;
        final AffineTransform rotation = new AffineTransform();
        rotation.rotate(angle);
        position.transform(rotation);
        return position;
    }

    /**
     * Determines the position of this scope in the visualization with the
     * provided center and radius.
     * 
     * @return the position of this scope in a signature visualization
     */
    public Point getSignaturePosition(final Point center, final double radius) {
        final Point position = this.getSignaturePosition();
        final AffineTransform scale = new AffineTransform();
        scale.scale(radius, radius);
        position.transform(scale);
        final AffineTransform translation = new AffineTransform();
        translation.translate(center.getX(), center.getY());
        position.transform(translation);
        return position;
    }

    /**
     * Returns the Extent object of this Scope. This is a short and more
     * convenient variant for new Extent(scope).
     * 
     * @return the Extent of this Scope
     */
    public Extent getExtent() {
        if (this.extent == null) {
            this.extent = new Extent(this);
        }
        return this.extent;
    }

    /**
     * Finds the first atomic relation of this scope. Attention: According to
     * the numbering of the Orientation Grid's sectors in Schuldt (2005) the
     * search runs clockwise.
     * 
     * @return the first atomic relation's index (from 1 to 6)
     */
    public int getFirstAtomicRelation() {
        if (this.firstAtomicRelation != -1) {
            return this.firstAtomicRelation;
        }
        final int newExtent = this.getExtent().getDescription();
        if (newExtent == MAX_EXTENT) {
            return 1;
        }
        int nonMemberIndex = -1;
        for (int index = 0; index < MAX_EXTENT; index++) {
            if (!contains(this.scope, index)) {
                nonMemberIndex = index;
                break;
            }
        }
        if (nonMemberIndex == -1) {
            return -1;
        }
        for (int index = nonMemberIndex + 1; index < MAX_EXTENT
                + nonMemberIndex; index++) {
            if (contains(this.scope, index % MAX_EXTENT)) {
                this.firstAtomicRelation = index % MAX_EXTENT + 1;
                break;
            }
        }
        return this.firstAtomicRelation;
    }

    @Override
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        for (int index = 0; index < MAX_EXTENT; index++) {
            if (contains(this.scope, index)) {
                buffer.append("1");
            } else {
                buffer.append("0");
            }
        }
        return buffer.toString();
    }

    /**
     * Returns a new Scope describing the union of this Scope and another Scope.
     * 
     * @param other
     *            another Scope
     * @return the union Scope
     */
    public Scope createUnion(final Scope other) {
        return new Scope(this.scope | other.scope);
    }

    /**
     * Returns a new Scope describing the intersection of this Scope and another
     * Scope.
     * 
     * @param other
     *            another Scope
     * @return the intersection Scope
     */
    public Scope createIntersection(final Scope other) {
        final int description = this.scope & other.scope;
        return new Scope(description);
    }

    /**
     * Returns a new Scope describing the complement of this Scope.
     * 
     * @return the complement of this Scope
     */
    public Scope createComplement() {
        int complement = 0;
        for (int index = 0; index < MAX_EXTENT; index++) {
            if (!contains(this.scope, index)) {
                complement |= ATOMIC[index];
            }
        }
        return new Scope(complement);
    }

    /**
     * Creates the inverse of the Scope.
     * 
     * @return the inverse of the Scope
     */
    public Scope createInverse() {
        final int firstBits = (this.scope & LEFT) << (ATOMIC.length / 2);
        final int lastBits = (this.scope & RIGHT) >> (ATOMIC.length / 2);
        return new Scope(firstBits | lastBits);
    }

    /**
     * Creates the flipped version of the Scope.
     * 
     * @return the flipped version of the Scope
     */
    public Scope createFlipped() {
        int flipped = 0;
        for (int index = 0; index < MAX_EXTENT; index++) {
            if (contains(this.scope, index)) {
                final int flippedIndex = (MAX_EXTENT + MAX_EXTENT / 3 - index)
                        % MAX_EXTENT;
                flipped |= ATOMIC[flippedIndex];
            }
        }
        return new Scope(flipped);
    }

    private static boolean contains(final int reference, final int index) {
        final int comparison = (1 << ((index + MAX_EXTENT) % MAX_EXTENT));
        return (reference & comparison) != 0;
    }
}
