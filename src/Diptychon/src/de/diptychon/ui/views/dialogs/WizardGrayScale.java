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

/**
 * This class is to handle gray scale.
 */
public class WizardGrayScale extends WizardDialog {
    /**
     * slider control for red
     */
    @FXML
    public Slider redSlider;

    /**
     * slider control for green
     */
    @FXML
    public Slider greenSlider;

    /**
     * slider control for blue
     */
    @FXML
    public Slider blueSlider;

    /**
     * toggle button for unchanged red
     */
    @FXML
    private ToggleButton unchangeRed;

    /**
     * toggle button for unchanged blue
     */
    @FXML
    private ToggleButton unchangeBlue;

    /**
     * toggle button for unchanged green
     */
    @FXML
    private ToggleButton unchangeGreen;

    /**
     * value of red
     */
    private double oldR;

    /**
     * value of green
     */
    private double oldG;

    /**
     * value of blue
     */
    private double oldB;

    /**
     * This method is used to cancel the action of wizard dialogs.
     * 
     * @event ActionEvent
     */
    @Override
    @FXML
    protected void cancel(final ActionEvent event) {
        this.setNextState(emDialogType.emCancel);
        this.hideDialog();
    }

    /**
     * It is used to be as next function in wizard dialog.
     * 
     * @param event
     *            It is an action event parameter.
     */
    @Override
    @FXML
    protected void next(final ActionEvent event) {
        this.setNextState(this.getNextType());
        this.hideDialog();
    }

    /**
     * It is used to be as previous function in wizard dialog.
     * 
     * @param event
     *            It is an action event parameter.
     */
    @Override
    @FXML
    protected void previous(final ActionEvent event) {
        this.setNextState(this.getPreviousType());
        this.hideDialog();
    }

    /**
     * It is used to be as finish function in wizard dialog.
     * 
     * @param event
     *            It is an action event parameter.
     */
    @Override
    @FXML
    protected void finish(final ActionEvent event) {
        
    }

    /**
     * This method is used to initialize gray scale wizard dialog.
     * 
     * @fxmlFileLocation path of fxml file
     * @resources resources for dialog
     * 
     */
    @Override
    public void initialize(final URL fxmlFileLocation,
            final ResourceBundle resources) {
        assert this.root != null : "fx:id=\"root\" was not injected: check your FXML file 'binarizeDialog.fxml'.";
        assert this.redSlider != null : "fx:id=\"red\" was not injected: check your FXML file 'binarizeDialog.fxml'.";
        assert this.greenSlider != null : "fx:id=\"green\" was not injected: check your FXML "
                + "file 'binarizeDialog.fxml'.";
        assert this.blueSlider != null : "fx:id=\"blue\" was not injected: check your FXML file 'binarizeDialog.fxml'.";

        this.oldR = (double) Math.round(this.redSlider.getValue() * 1000) / 1000;
        this.oldG = (double) Math.round(this.greenSlider.getValue() * 1000) / 1000;
        this.oldB = (double) Math.round(this.blueSlider.getValue() * 1000) / 1000;

        this.redSlider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(
                            final ObservableValue<? extends Number> ov,
                            final Number old_val, final Number new_val) {
                        WizardGrayScale.this.updatePreview((double) Math
                                .round(new_val.doubleValue() * 1000) / 1000,
                                WizardGrayScale.this.oldG,
                                WizardGrayScale.this.oldB);
                    }
                });

        this.greenSlider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(
                            final ObservableValue<? extends Number> ov,
                            final Number old_val, final Number new_val) {
                        WizardGrayScale.this.updatePreview(
                                WizardGrayScale.this.oldR,
                                (double) Math.round(new_val.doubleValue() * 1000) / 1000,
                                WizardGrayScale.this.oldB);
                    }
                });

        this.blueSlider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(
                            final ObservableValue<? extends Number> ov,
                            final Number old_val, final Number new_val) {
                        WizardGrayScale.this.updatePreview(
                                WizardGrayScale.this.oldR,
                                WizardGrayScale.this.oldG,
                                (double) Math.round(new_val.doubleValue() * 1000) / 1000);
                    }
                });

    }

    /**
     * This method is used to update preview.
     * 
     * @param rVal
     *            red value
     * @param gVal
     *            green value
     * @param bVal
     *            blue value
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
}
