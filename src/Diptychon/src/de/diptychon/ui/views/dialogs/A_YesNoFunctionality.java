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
package de.diptychon.ui.views.dialogs;

import de.diptychon.controller.DocumentPanelController;

/**
 * Abstract class that must be implemented when creating a
 * yesNoConfirmationDialog
 */
public abstract class A_YesNoFunctionality {
    /**
     * Reference to a documentPanelController
     */
    protected final DocumentPanelController documentPanelController;

    /**
     * Creates a new functionality
     * 
     * @param dcp
     *            Reference to a documentPanelController
     */
    public A_YesNoFunctionality(final DocumentPanelController dcp) {
        this.documentPanelController = dcp;
    }

    /**
     * Implement accept functionality
     */
    public abstract void accept();

    /**
     * Implement deny functionality
     */
    public abstract void deny();

}
