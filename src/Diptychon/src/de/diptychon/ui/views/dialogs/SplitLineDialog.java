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

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import de.diptychon.DiptychonLogger;

/**
 * The controller for the SplitLineDialog
 */
public class SplitLineDialog extends A_Dialog {

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
     * The estimated character width slider
     */
    @FXML
    private Slider widthSlider;

    /**
     * The label to display the estimated character width
     */
    @FXML
    private Label characterWidth;

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
        this.characterWidth.textProperty().bind(
                this.widthSlider.valueProperty().asString("%.0f"));
        this.acceptanceProperty = new SimpleIntegerProperty(-1);
        this.widthSlider.valueProperty().addListener(
                new InvalidationListener() {

                    @Override
                    public void invalidated(final Observable observable) {
                        SplitLineDialog.this.acceptanceProperty
                                .set((int) SplitLineDialog.this.widthSlider
                                        .getValue());
                    }
                });
    }

    /**
     * Gets the estimated character width value
     * 
     * @return the estimated character width value
     */
    public DoubleProperty getCharacterWidthProperty() {
        return this.widthSlider.valueProperty();
    }

    /**
     * Gets the acceptance property
     * 
     * @return the acceptance property
     */
    public IntegerProperty getAcceptedProperty() {
        return this.acceptanceProperty;
    }

}
