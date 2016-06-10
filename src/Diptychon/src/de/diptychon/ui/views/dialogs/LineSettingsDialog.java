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
import javafx.beans.property.IntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

/**
 * The controller for the SettingsDialog
 */
public class LineSettingsDialog extends A_Dialog {

    /**
     * The default font size
     */
    private static final int DEFAULT_TOP = 10;

    /**
     * The default square size
     */
    private static final int DEFAULT_BOTTOM = 10;

    /**
     * Shows all font families
     */
    @FXML
    private Slider topSlider;

    /**
     * Shows all allowed font sizes
     */
    @FXML
    private Slider bottomSlider;

    /**
     * The font setting before it was changed
     */
    private int prevTop;

    /**
     * The font size setting before it was changed
     */
    private int prevBottom;

    /**
     * the current font
     */
    private IntegerProperty top;

    /**
     * the current font size
     */
    private IntegerProperty bottom;

    /**
     * the current font size
     */
    @FXML
    private Label topLabel;

    /**
     * the current font size
     */
    @FXML
    private Label bottomLabel;

    @Override
    public void apply(final ActionEvent event) {
        this.top.set((int) this.topSlider.getValue());
        this.bottom.set((int) bottomSlider.getValue());
        this.closeDialog();
    }

    /**
     * Sets font family and font size to default
     * 
     * @param event
     *            Propagated by JavaFX. Can be ignored
     */
    @FXML
    private void setDefault(final ActionEvent event) {
        this.topSlider.setValue(DEFAULT_TOP);
        this.bottomSlider.setValue(DEFAULT_BOTTOM);
    }

    @Override
    public void cancel(final ActionEvent event) {
        this.top.setValue(this.prevTop);
        this.bottom.setValue(this.prevBottom);
        this.closeDialog();
    }

    @Override
    public void initialize(final URL fxmlFileLocation,
            final ResourceBundle resources) {
        assert this.root != null : "fx:id=\"root\" was not injected: check your FXML file 'FindLineDialog.fxml'.";

    }

    /**
     * Initializes the comboboxes to display the available fonts and the allowed
     * sizes
     * 
     * @param fontProperty
     *            the font property bound to the DocumentPanel
     * @param sizeProperty
     *            the font size property bound to the DocumentPanel
     */
    public void initBoxes(final IntegerProperty lineExtTopProperty,
            final IntegerProperty lineExtBottomProperty) {
        this.prevTop = lineExtTopProperty.get();
        this.prevBottom = lineExtBottomProperty.get();
        this.topSlider.setValue(lineExtTopProperty.get());
        this.bottomSlider.setValue(lineExtBottomProperty.get());
        this.top = lineExtTopProperty;
        this.bottom = lineExtBottomProperty;
        this.topLabel.setText(lineExtTopProperty.get() + "");
        this.bottomLabel.setText(lineExtBottomProperty.get() + "");

        this.topSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                LineSettingsDialog.this.topLabel
                        .setText((int) LineSettingsDialog.this.topSlider
                                .getValue() + "");
            }
        });

        this.bottomSlider.valueProperty().addListener(
                new InvalidationListener() {
                    @Override
                    public void invalidated(final Observable observable) {
                        LineSettingsDialog.this.bottomLabel
                                .setText((int) LineSettingsDialog.this.bottomSlider
                                        .getValue() + "");
                    }
                });
    }

}
