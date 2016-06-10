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
package de.diptychon.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * This class provides base level functionality for all models, including a
 * support for a property change mechanism (using the PropertyChangeSupport
 * class), as well as a convenience method that other objects can use to reset
 * model state.
 */
public abstract class AbstractModel {

    /**
     * Convenience class that allow others to observe changes to the model
     * properties
     */
    private final PropertyChangeSupport propertyChangeSupport;

    /**
     * Default constructor. Instantiates the PropertyChangeSupport class.
     */
    public AbstractModel() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Adds a property change listener to the observer list.
     * 
     * @param l
     *            The property change listener
     */
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        this.propertyChangeSupport.addPropertyChangeListener(l);
    }

    /**
     * Removes a property change listener from the observer list.
     * 
     * @param l
     *            The property change listener
     */
    public void removePropertyChangeListener(final PropertyChangeListener l) {
        this.propertyChangeSupport.removePropertyChangeListener(l);
    }

    /**
     * Fires an event to all registered listeners informing them that a property
     * in this model has changed.
     * 
     * @param propertyName
     *            The name of the property
     * @param oldValue
     *            The previous value of the property before the change
     * @param newValue
     *            The new property value after the change
     */
    public void firePropertyChange(final String propertyName,
            final Object oldValue, final Object newValue) {
        this.propertyChangeSupport.firePropertyChange(propertyName, oldValue,
                newValue);
    }
}
