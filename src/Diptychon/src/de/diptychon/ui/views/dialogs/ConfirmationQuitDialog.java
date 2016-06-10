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

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Controller for the ConfirmationQuitDialog
 */
public class ConfirmationQuitDialog extends A_Dialog {

    /**
     * <code>true</code> if the user wants to exit <code>false otherwise</code>
     */
    private BooleanProperty exit;

    @Override
    @FXML
    protected void apply(final ActionEvent event) {
        this.closeDialog();
        this.documentPanelController.save();
        this.exit.set(true);
    }

    /**
     * Discards changes and exit
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void discard(final ActionEvent event) {
        this.closeDialog();
        this.exit.set(true);
    }

    @Override
    @FXML
    protected void cancel(final ActionEvent event) {
        this.closeDialog();
        this.exit.set(false);
    }

    /**
     * Gets the exit property
     * 
     * @return the exit property
     */
    public BooleanProperty getExitProperty() {
        return this.exit;
    }

    @Override
    public void initialize(final URL fxmlFileLocation,
            final ResourceBundle resources) {
        assert this.root != null : "fx:id=\"root\" was not injected: check your FXML file 'ConfirmationDialog.fxml'.";
        this.exit = new SimpleBooleanProperty(false);
    }

}
