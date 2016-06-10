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
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import de.diptychon.DiptychonLogger;

/**
 * The controller for the SplitLineDialog
 */
public class FragEditingDialog extends A_Dialog {

    /**
     * The minimum character width
     */
    public static final int MIN = 5;

    /**
     * The maximum character width
     */
    public static final int MAX = 50;

    /**
     * The acceptance property
     */
    private IntegerProperty acceptanceProperty;

    /**
     * The label to display the amount of fragments left
     */
    @FXML
    private Label fragsLeft;

    /**
     * The label to display the amount of fragments needed
     */
    @FXML
    private Label chars;

    /**
     * The apply button
     */
    @FXML
    private Button applyButton;

    @Override
    public void apply(final ActionEvent event) {
        DiptychonLogger.info("Split Line Character Width Apply.");
        this.acceptanceProperty.set(1);
        this.closeDialog();
    }

    @Override
    public void cancel(final ActionEvent event) {
        DiptychonLogger.info("Split Line Character Width Cancel.");
        this.acceptanceProperty.set(0);
        this.closeDialog();
    }

    @Override
    public void initialize(final URL fxmlFileLocation,
            final ResourceBundle resources) {
        this.acceptanceProperty = new SimpleIntegerProperty(-1);
    }

    /**
     * Gets the acceptance property
     * 
     * @return the acceptance property
     */
    public IntegerProperty getAcceptedProperty() {
        return this.acceptanceProperty;
    }

    /**
     * Gets the acceptance property
     * 
     * @return the acceptance property
     */
    public StringProperty getFragsLeftProperty() {
        return this.fragsLeft.textProperty();
    }

    /**
     * Gets the acceptance property
     * 
     * @return the acceptance property
     */
    public StringProperty getCharsProperty() {
        return this.chars.textProperty();
    }

    /**
     * Gets the acceptance property
     * 
     * @return the acceptance property
     */
    public BooleanProperty getApplyDisableProperty() {
        return this.applyButton.disableProperty();
    }

}
