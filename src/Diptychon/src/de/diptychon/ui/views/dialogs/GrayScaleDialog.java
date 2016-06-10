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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import de.diptychon.DiptychonLogger;

/**
 * Controller for GrayScaleDialog
 */
public class GrayScaleDialog extends A_Dialog {

    /**
     * UI Control controls the red weight
     */
    @FXML
    private Slider redSlider;

    /**
     * UI Control controls the green weight
     */
    @FXML
    private Slider greenSlider;

    /**
     * UI Control controls the blue weight
     */
    @FXML
    private Slider blueSlider;

    /**
     * TODO: Unused
     */
    @FXML
    private ToggleButton unchangeRed;

    /**
     * TODO: Unused
     */
    @FXML
    private ToggleButton unchangeBlue;

    /**
     * TODO: Unused
     */
    @FXML
    private ToggleButton unchangeGreen;

    /**
     * Stores the previous red weight
     */
    private double oldR;

    /**
     * Stores the previous green weight
     */
    private double oldG;

    /**
     * Stores the previous blue weight
     */
    private double oldB;

    /**
     * Creates a GrayScale preview
     * 
     * @param event
     *            Propagated by JavaFX. Can be ignored
     */
    @FXML
    public void preview(final ActionEvent event) {
        DiptychonLogger.debug(
                "Grayscale parameters are: red {} green {} blue {}", this.oldR,
                this.oldG, this.oldB);
        DiptychonLogger.debug("Grayscale parameters sum is: {}", this.oldR
                + this.oldG + this.oldB);
        DiptychonLogger.info("Preview button is pressed in Gray Scale Dialog.");
        this.documentPanelController.grayPreview(this.redSlider.getValue(),
                this.greenSlider.getValue(), this.blueSlider.getValue());
    }

    /**
     * This method is used to apply gray scale to the current picture.
     * 
     * @event ActionEvent
     */
    @Override
    public void apply(final ActionEvent event) {
        DiptychonLogger.debug(
                "Grayscale parameters are: red {} green {} blue {}", this.oldR,
                this.oldG, this.oldB);
        DiptychonLogger.debug("Grayscale parameters sum is: {}", this.oldR
                + this.oldG + this.oldB);
        DiptychonLogger.info("Apply button is pressed in Gray Scale Dialog.");
        this.documentPanelController.grayApply(this.redSlider.getValue(),
                this.greenSlider.getValue(), this.blueSlider.getValue());
        this.closeDialog();
    }

    /**
     * This method is used to cancel the action of gray scale.
     * 
     * @event ActionEvent
     */
    @Override
    public void cancel(final ActionEvent event) {
        DiptychonLogger.info("Cancel button is pressed in Gray Scale Dialog.");
        this.documentPanelController.cancelGray();
        this.closeDialog();
    }

    /**
     * This method is used to initialize the gray scale dialog and set event
     * handlers.
     * 
     * @fxmlFileLocation file path of gray scale fxml file @ recources recource
     *                   of gray scale dialog
     */
    @Override
    public void initialize(final URL fxmlFileLocation,
            final ResourceBundle resources) {
        assert this.root != null : "fx:id=\"root\" was not injected: check your FXML file 'GrayScaleDialog.fxml'.";
        assert this.redSlider != null : "fx:id=\"red\" was not injected: check your FXML file 'GrayScaleDialog.fxml'.";
        assert this.greenSlider != null : "fx:id=\"green\" was not injected: check your FXML "
                + "file 'GrayScaleDialog.fxml'.";
        assert this.blueSlider != null : "fx:id=\"blue\" was not injected: check your FXML file "
                + "'GrayScaleDialog.fxml'.";

        this.oldR = (double) Math.round(this.redSlider.getValue() * 1000) / 1000;
        this.oldG = (double) Math.round(this.greenSlider.getValue() * 1000) / 1000;
        this.oldB = (double) Math.round(this.blueSlider.getValue() * 1000) / 1000;

        this.redSlider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(
                            final ObservableValue<? extends Number> ov,
                            final Number old_val, final Number new_val) {
                        GrayScaleDialog.this.updatePreview((double) Math
                                .round(new_val.doubleValue() * 1000) / 1000,
                                GrayScaleDialog.this.oldG,
                                GrayScaleDialog.this.oldB);
                    }
                });

        this.greenSlider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(
                            final ObservableValue<? extends Number> ov,
                            final Number old_val, final Number new_val) {
                        GrayScaleDialog.this.updatePreview(
                                GrayScaleDialog.this.oldR,
                                (double) Math.round(new_val.doubleValue() * 1000) / 1000,
                                GrayScaleDialog.this.oldB);
                    }
                });

        this.blueSlider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(
                            final ObservableValue<? extends Number> ov,
                            final Number old_val, final Number new_val) {
                        GrayScaleDialog.this.updatePreview(
                                GrayScaleDialog.this.oldR,
                                GrayScaleDialog.this.oldG,
                                (double) Math.round(new_val.doubleValue() * 1000) / 1000);
                    }
                });
    }

    /**
     * Updates the grayscale preview
     * 
     * @param rVal
     *            the red weight
     * @param gVal
     *            the green weight
     * @param bVal
     *            the blue weight
     */
    private void updatePreview(final double rVal, final double gVal,
            final double bVal) {
        if (rVal != this.oldR) {
            final double diff = rVal - this.oldR;
            if (diff > 0) {
                if (this.oldG <= 0) {
                    this.oldB -= diff;
                    this.oldG = 0;
                } else if (this.oldB <= 0) {
                    this.oldG -= diff;
                    this.oldB = 0;
                } else {
                    final double add = diff / 2;
                    this.oldG -= add;
                    this.oldB -= add;
                }
            } else {
                if (this.oldG >= 1) {
                    this.oldB += -diff;
                    this.oldG = 1;
                } else if (this.oldB >= 1) {
                    this.oldG += -diff;
                    this.oldB = 1;
                } else {
                    final double add = -diff / 2;
                    this.oldG += add;
                    this.oldB += add;
                }
            }

            this.oldR = rVal;

            this.oldR = (double) Math.round(this.oldR * 1000) / 1000;
            this.oldG = (double) Math.round(this.oldG * 1000) / 1000;
            this.oldB = (double) Math.round(this.oldB * 1000) / 1000;

            final double weightingSum = this.oldR + this.oldG + this.oldB;
            if (weightingSum != 1.d) {
                this.oldR += 1.0 - (weightingSum);
            }

        } else if (gVal != this.oldG) {
            final double diff = gVal - this.oldG;
            if (diff > 0) {
                if (this.oldR <= 0) {
                    this.oldB -= diff;
                    this.oldR = 0;
                } else if (this.oldB <= 0) {
                    this.oldR -= diff;
                    this.oldB = 0;
                } else {
                    final double add = diff / 2;
                    this.oldR -= add;
                    this.oldB -= add;
                }
            } else {
                if (this.oldR >= 1) {
                    this.oldB += -diff;
                    this.oldR = 1;
                } else if (this.oldB >= 1) {
                    this.oldR += -diff;
                    this.oldB = 1;
                } else {
                    final double add = -diff / 2;
                    this.oldR += add;
                    this.oldB += add;
                }
            }

            this.oldG = gVal;

            this.oldR = (double) Math.round(this.oldR * 1000) / 1000;
            this.oldG = (double) Math.round(this.oldG * 1000) / 1000;
            this.oldB = (double) Math.round(this.oldB * 1000) / 1000;

            final double weightingSum = this.oldR + this.oldG + this.oldB;
            if (weightingSum != 1.d) {
                this.oldG += 1.0 - (weightingSum);
            }
        } else if (bVal != this.oldB) {
            final double diff = bVal - this.oldB;
            if (diff > 0) {
                if (this.oldR <= 0) {
                    this.oldG -= diff;
                    this.oldR = 0;
                } else if (this.oldG <= 0) {
                    this.oldR -= diff;
                    this.oldG = 0;
                } else {
                    final double add = diff / 2;
                    this.oldR -= add;
                    this.oldG -= add;
                }
            } else {
                if (this.oldR >= 1) {
                    this.oldG += -diff;
                    this.oldR = 1;
                } else if (this.oldG >= 1) {
                    this.oldR += -diff;
                    this.oldG = 1;
                } else {
                    final double add = -diff / 2;
                    this.oldR += add;
                    this.oldG += add;
                }
            }

            this.oldB = bVal;

            this.oldR = (double) Math.round(this.oldR * 1000) / 1000;
            this.oldG = (double) Math.round(this.oldG * 1000) / 1000;
            this.oldB = (double) Math.round(this.oldB * 1000) / 1000;

            final double weightingSum = this.oldR + this.oldG + this.oldB;
            if (weightingSum != 1.d) {
                this.oldB += 1.0 - (weightingSum);
            }

        }

        this.redSlider.setValue(this.oldR);
        this.greenSlider.setValue(this.oldG);
        this.blueSlider.setValue(this.oldB);
    }

    /**
     * Initializes the slider with specified values
     * 
     * @param values
     *            the initial weights
     */
    public void initSlider(final double[] values) {
        this.redSlider.setValue(values[0]);
        this.greenSlider.setValue(values[1]);
        this.blueSlider.setValue(values[2]);
    }
}
