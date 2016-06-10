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
package de.diptychon.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.diptychon.DiptychonLogger;
import de.diptychon.models.AbstractModel;
import de.diptychon.ui.views.AbstractView;

/**
 * This class is the abstract implementation of a controller. Each UI component
 * which should be able to communicate with models, should be registered at a
 * controller which provides the desired functionality
 */
public abstract class AbstractController implements PropertyChangeListener {

    /**
     * list of registered views
     */
    private final List<AbstractView> registeredViews;

    /**
     * list of registered models
     */
    private final List<AbstractModel> registeredModels;

    /**
     * Default Contructor.
     */
    public AbstractController() {
        this.registeredViews = new ArrayList<>();
        this.registeredModels = new ArrayList<>();
    }

    /**
     * This method is used to register models at the controller
     * 
     * @param model
     *            model object
     */
    public void registerModel(final AbstractModel model) {
        this.registeredModels.add(model);
        model.addPropertyChangeListener(this);
    }

    /**
     * Removes all registered models from the controller.
     */
    public void unregisterAllModels() {
        this.registeredModels.clear();
    }

    /**
     * This method is used to register the view into the controller.
     * 
     * @param view
     *            view object
     */
    public void registerView(final AbstractView view) {
        this.registeredViews.add(view);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        for (final AbstractView view : this.registeredViews) {
            view.modelPropertyChange(evt);
        }
    }

    /**
     * Used to set a model property without parameters, e.g. invert boolean
     * properties.
     * 
     * @param propertyName
     *            property name
     */
    protected void setModelProperty(final String propertyName) {
        this.setModelProperty(propertyName, null);
    }

    /**
     * This method is used to set the property of a model by reflection
     * 
     * @param propertyName
     *            property name
     * @param newValue
     *            the value of property name
     */
    protected void setModelProperty(final String propertyName,
            final Object newValue) {

        for (final AbstractModel model : this.registeredModels) {
            Method method;
            if (newValue == null) {
                try {
                    method = model.getClass().getMethod(propertyName);
                    method.invoke(model);
                    return;
                } catch (final NoSuchMethodException e) {
                    /*
                     * Empty because not every model will have a specific
                     * method. So this Exception can and has to be ignored
                     */
                } catch (final SecurityException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException e) {
                    DiptychonLogger.error("{}", e);
                }
            } else {
                Class<?> c = newValue.getClass();
                while (c != null) {
                    try {
                        method = model.getClass().getMethod(propertyName,
                                new Class[] { c });
                        method.invoke(model, newValue);
                        return;
                    } catch (final NoSuchMethodException e) {
                        final Class<?>[] interfaces = c.getInterfaces();
                        for (final Class<?> inter : interfaces) {
                            try {
                                method = model.getClass().getMethod(
                                        propertyName, new Class[] { inter });
                                method.invoke(model, newValue);
                                return;

                            } catch (final NoSuchMethodException e1) {
                                /*
                                 * Empty because not every model will have a
                                 * specific method. So this Exception can and
                                 * has to be ignored
                                 */
                            } catch (final SecurityException
                                    | IllegalAccessException
                                    | IllegalArgumentException
                                    | InvocationTargetException e1) {
                                DiptychonLogger.error("{}", e);
                            }

                        }
                    } catch (final SecurityException | IllegalAccessException
                            | IllegalArgumentException
                            | InvocationTargetException e) {
                        DiptychonLogger.error("{}", e);
                    }
                    c = c.getSuperclass();
                }
            }
        }
    }
}
