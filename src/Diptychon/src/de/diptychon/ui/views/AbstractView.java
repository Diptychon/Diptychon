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
package de.diptychon.ui.views;

import java.beans.PropertyChangeEvent;

/**
 * Base Class for all UI Node which need to be able to react on changes of any
 * model
 */
public abstract class AbstractView {

    /**
     * Default Constructor
     */
    public AbstractView() {
        super();
    }

    /**
     * Called by the controller when it needs to pass along a property change
     * from a model. The standard method is empty, because not all views have to
     * react on property changes of a model
     * 
     * @param evt
     *            The property change event from the model
     */

    public abstract void modelPropertyChange(final PropertyChangeEvent evt);
}
