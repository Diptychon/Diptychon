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
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.shape.Rectangle;
import de.diptychon.DiptychonLogger;

/**
 * Controller for the AutoContrastAdjustDialog
 */
public class AutoContrastAdjustDialog extends A_Dialog {

    /**
     * The chart to visualize the histogram
     */
    @FXML
    private BarChart<String, Double> barChart;

    /**
     * the slider to set the minimum intensity
     */
    @FXML
    private Slider minSlider;

    /**
     * the slider to set the maximum intensity
     */
    @FXML
    private Slider maxSlider;

    /**
     * textfield to input the saturation
     */
    @FXML
    private TextField saturationLeft;

    /**
     * textfield to input the saturation
     */
    @FXML
    private TextField saturationRight;

    /**
     * the invalidation property which is invoked when this histogram changes
     */
    private IntegerProperty trigger;

    /**
     * the histogram
     */
    private int[] histogramm;

    /**
     * the rectangular region for the contrast operation
     */
    private Rectangle imageRectangle;

    /**
     * Convenience enum (I like enums....)
     */
    private enum Saturation {
        LEFT, RIGHT;
    }

    /**
     * Initializes this dialog
     * 
     * @param rect
     *            the rectangular region for the contrast operation
     * @param hist
     *            the histogram
     */
    public void init(final Rectangle rect, final int[] hist) {
        this.imageRectangle = rect;
        this.histogramm = hist;
        this.trigger = new SimpleIntegerProperty(1);
        this.trigger.addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                AutoContrastAdjustDialog.this.updateAutoContrastInfo();
            }
        });
        this.initHistogram();
    }

    /**
     * Gets the invalidation property which is invoked when this histogram
     * changes
     * 
     * @return the invalidation property
     */
    public IntegerProperty getInvalidationTrigger() {
        return this.trigger;
    }

    /**
     * Generates a preview for the autocontrast operation with the set
     * parameters
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void preview(final ActionEvent event) {
        this.documentPanelController.previewAutoContrast(this.imageRectangle,
                this.getSaturation(Saturation.LEFT),
                this.getSaturation(Saturation.RIGHT),
                (int) this.minSlider.getValue(),
                (int) this.maxSlider.getValue(), this.trigger, this.histogramm);
    }

    @FXML
    @Override
    protected void apply(final ActionEvent event) {
        this.documentPanelController.applyAutoContrast(this.imageRectangle,
                this.getSaturation(Saturation.LEFT),
                this.getSaturation(Saturation.RIGHT),
                (int) this.minSlider.getValue(),
                (int) this.maxSlider.getValue(), this.histogramm);
        this.closeDialog();
    }

    /**
     * Gets the desired saturation
     * 
     * @param saturation
     *            high or low?
     * @return the value
     */
    private double getSaturation(final Saturation saturation) {
        double satVal = 0.d;
        String satText = "0.0";
        switch (saturation) {
        case LEFT:
            satText = this.saturationLeft.getText();
            break;
        case RIGHT:
            satText = this.saturationRight.getText();
            break;
        default:
            return 0.d;
        }
        satText = satText.replace(',', '.');
        try {
            satVal = Double.valueOf(satText);
        } catch (final NumberFormatException e) {
            DiptychonLogger.error("{}", e);
            return 0.d;
        }
        return satVal / 100.d;
    }

    @FXML
    @Override
    protected void cancel(final ActionEvent event) {
        this.documentPanelController.cancelAutoContrast();
        this.closeDialog();
    }

    @Override
    public void initialize(final URL fxmlFileLocation,
            final ResourceBundle resources) {
        this.minSlider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(
                            final ObservableValue<? extends Number> ov,
                            final Number old_val, final Number new_val) {
                        AutoContrastAdjustDialog.this.updateSlider(new_val
                                .intValue());
                    }
                });
        this.maxSlider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(
                            final ObservableValue<? extends Number> ov,
                            final Number old_val, final Number new_val) {
                        AutoContrastAdjustDialog.this.updateSlider(new_val
                                .intValue());
                    }
                });
    }

    /**
     * Updates the slider of this dialog
     * 
     * @param value
     *            the value to set
     */
    private void updateSlider(final int value) {
        if (value > this.maxSlider.getValue()) {
            this.maxSlider.setValue(value);
        } else if (value < this.minSlider.getValue()) {
            this.minSlider.setValue(value);
        }
    }

    /**
     * initializes the histogramm and its chart
     */
    private void initHistogram() {
        final XYChart.Series<String, Double> seriesLog = new XYChart.Series<>();
        int index = 0;
        for (final int value : this.histogramm) {
            double log = value;
            if (value != 0) {
                log = Math.log(value);
            }
            seriesLog.getData().add(
                    new XYChart.Data<String, Double>(index + "", log));
            ++index;
        }
        this.barChart.getData().add(seriesLog);
    }

    /**
     * Updates the chart which shows the histogram
     */
    public void updateAutoContrastInfo() {
        this.barChart.getData().clear();
        this.initHistogram();
    }
}
