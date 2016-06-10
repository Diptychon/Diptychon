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
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.shape.Rectangle;
import de.diptychon.DiptychonLogger;

/**
 * Controller for the BinarizeDialog
 */
public class BinarizeDialog extends A_Dialog {

    /**
     * slider to set k
     */
    @FXML
    private Slider kSlider;

    @FXML
    private Label topLabel;

    @FXML
    private Label bottomLabel;

    /**
     * slider to set windowsize
     */
    @FXML
    private Slider windowSize;

    private Rectangle rect;

    /**
     * Generates a preview for the binarization with the set parameters
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void preview(final ActionEvent event) {
        DiptychonLogger.info("Preview button is pressed in Binarize Dialogue.");
        if (this.rect != null) {
            this.documentPanelController.binarizePreview(
                    (int) this.windowSize.getValue(), this.kSlider.getValue(),
                    this.rect);
        } else {
            this.documentPanelController.binarizePreview(
                    (int) this.windowSize.getValue(), this.kSlider.getValue());
        }
    }

    @Override
    public void apply(final ActionEvent event) {
        DiptychonLogger.info("Apply button is pressed in Binarize Diaglogue.");
        if (this.rect != null) {
            this.documentPanelController.binarizeMask(
                    (int) this.windowSize.getValue(), this.kSlider.getValue(),
                    this.rect);
        } else {
            this.documentPanelController.binarizeApply(
                    (int) this.windowSize.getValue(), this.kSlider.getValue());
        }
        this.closeDialog();
    }

    @Override
    public void cancel(final ActionEvent event) {
        DiptychonLogger.info("Cancel the binarize dialogue.");
        this.documentPanelController.cancelBinarization();
        this.closeDialog();
    }

    @Override
    public void initialize(final URL fxmlFileLocation,
            final ResourceBundle resources) {
        assert this.root != null : "fx:id=\"root\" was not injected: check your FXML file 'binarizeDialog.fxml'.";
        assert this.kSlider != null : "fx:id=\"k\" was not injected: check your FXML file 'binarizeDialog.fxml'.";
        assert this.windowSize != null : "fx:id=\"windowSize\" was not injected: "
                + "check your FXML file 'binarizeDialog.fxml'.";
    }

    /**
     * initializes the parameter
     * 
     * @param pK
     *            k
     * @param pWindowSize
     *            windowSize
     */
    public void initSlider(final double pK, final int pWindowSize) {
        NumberFormat n = NumberFormat.getInstance(Locale.ENGLISH);
        n.setMaximumFractionDigits(2);
        n.setMinimumFractionDigits(2);

        this.windowSize.setValue(pWindowSize);
        this.kSlider.setValue(pK);

        this.topLabel.setText((int) pWindowSize + "");
        this.bottomLabel.setText(n.format(pK) + "");

        this.kSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                NumberFormat n = NumberFormat.getInstance(Locale.ENGLISH);
                n.setMaximumFractionDigits(1);
                n.setMinimumFractionDigits(1);
                BinarizeDialog.this.bottomLabel.setText(n
                        .format(BinarizeDialog.this.kSlider.getValue()) + "");
            }
        });

        this.windowSize.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                BinarizeDialog.this.topLabel
                        .setText((int) BinarizeDialog.this.windowSize
                                .getValue() + "");
            }
        });
    }

    public void setMask(final Rectangle rect) {
        this.rect = rect;
    }
}
