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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import de.diptychon.DiptychonLogger;

/**
 * Implements the controller for the TemplateMatchingThresholdDialog.fxml
 */
public class TemplateMatchingThresholdDialog extends A_Dialog {

    /**
     * Represents the lowest threshold allowed for template matching ({@value}
     * ). A threshold lower than this, does not make any sense, since it would
     * lead to an enormous amount of possible matches
     */
    public static final int MIN = 25;

    /**
     * The threshold ({@value} ) which is used to filter out possible matches by
     * width. In contrast the threshold for the height is determined by the
     * standard deviation
     */
    public static final int MAX = 255;

    /**
     * Property value to check whether the result was accepted or not. This is a
     * convenience variable to enable the <code>DocumentPanel</code> to react on
     * acceptance changes
     */
    private IntegerProperty acceptanceProperty;

    /**
     * The accuracy slider which changes the threshold for displaying the result
     * of template matching.
     */
    @FXML
    private Slider accuracy;

    /**
     * The value of the accuracy slider. For debug purpose.
     */
    @FXML
    private Label accuracyValue;

    @Override
    public void apply(final ActionEvent event) {
        DiptychonLogger.info("TemplateMatching threshold Apply.");
        this.acceptanceProperty.set(1);
        this.closeDialog();
    }

    @Override
    public void cancel(final ActionEvent event) {
        DiptychonLogger.info("TemplateMatching threshold Cancel.");
        this.acceptanceProperty.set(0);
        this.closeDialog();
    }

    @Override
    public void initialize(final URL fxmlFileLocation,
            final ResourceBundle resources) {
        assert this.accuracy != null : "fx:id=\"accuracy\" was not injected: check your "
                + "FXML file 'TemplateMatchingThresholdDialog.fxml'.";
        assert this.root != null : "fx:id=\"root\" was not injected: check your"
                + " FXML file 'TemplateMatchingThresholdDialog.fxml'.";
        this.accuracyValue.textProperty().bind(
                this.accuracy.valueProperty().asString("%.0f"));
        this.acceptanceProperty = new SimpleIntegerProperty(2);
    }

    /**
     * Accuracy getter method.
     * 
     * @return The accuracy used for template matching
     */
    public DoubleProperty getAccuracyProperty() {
        return this.accuracy.valueProperty();
    }

    /**
     * Acceptance getter method
     * 
     * @return an integer property which says whether a result is accepted or
     *         not. BooleanProperty cannot be used here, since we need 3 values:
     *         initialized, accepted, denied.
     */
    public IntegerProperty getAcceptanceProperty() {
        return this.acceptanceProperty;
    }
}
