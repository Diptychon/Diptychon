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
 * This class represents a DummyYesNoFunctionality which simply means it does
 * nothing
 */
public class DummyYesNoFunctionality extends A_YesNoFunctionality {

    /**
     * Creates new DummyYesNoFunctionality
     * 
     * @param dcp
     *            reference to a DocumentPanelController
     */
    public DummyYesNoFunctionality(final DocumentPanelController dcp) {
        super(dcp);
    }

    @Override
    public void accept() {
    }

    @Override
    public void deny() {
    }
}
