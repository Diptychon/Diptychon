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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

/**
 * The controller for the SettingsDialog
 */
public class FragSettingsDialog extends A_Dialog {

    /**
     * The default square size
     */
    private static final int DEFAULT_SQUARE_SIZE = 2;

    /**
     * The default square size
     */
    private static final boolean DEFAULT_SPLIT_LINE = false;

    /**
     * The default square size
     */
    private static final int DEFAULT_FRAG_SIZE = 50;

    /**
     * The default square size
     */
    private static final int DEFAULT_NOISE_SIZE = 5;

    /**
     * The default square size
     */
    private static final float DEFAULT_FRAG_COUNT = (float) 1.4;

    /**
     * Shows all allowed square sizes
     */
    @FXML
    private Slider BurstSizeSlider;

    /**
     * Shows all allowed square sizes
     */
    @FXML
    private Slider FragNoiseSlider;

    /**
     * Shows all allowed square sizes
     */
    @FXML
    private CheckBox SplitLineCheckBox;

    /**
     * Shows all allowed square sizes
     */
    @FXML
    private Slider FragSizeSlider;

    /**
     * Shows all allowed square sizes
     */
    @FXML
    private Slider FragCountSlider;

    /**
     * Shows all allowed square sizes
     */
    @FXML
    private Label FragCountLabel;

    /**
     * Shows all allowed square sizes
     */
    @FXML
    private Label FragSizeLabel;

    /**
     * Shows all allowed square sizes
     */
    @FXML
    private Label FragNoiseLabel;

    /**
     * Shows all allowed square sizes
     */
    @FXML
    private Label BurstSizeLabel;

    /**
     * The square size setting before it was changed
     */
    private int prevSquareSize;

    /**
     * The split line setting before it was changed
     */
    private boolean prevSplitLine;

    /**
     * The split line setting before it was changed
     */
    private int prevFragSize;

    /**
     * The split line setting before it was changed
     */
    private int prevNoiseSize;

    /**
     * The split line setting before it was changed
     */
    private float prevFragCount;

    /**
     * the current square size
     */
    private IntegerProperty squareSize;

    /**
     * the current square size
     */
    private BooleanProperty splitLine;

    /**
     * the current square size
     */
    private IntegerProperty fragSize;

    /**
     * the current square size
     */
    private IntegerProperty noiseSize;

    /**
     * the current square size
     */
    private FloatProperty fragCount;

    @Override
    public void apply(final ActionEvent event) {
        this.squareSize.set((int) this.BurstSizeSlider.getValue());
        this.splitLine.setValue(this.SplitLineCheckBox.isSelected());
        this.fragSize.set((int) (this.FragSizeSlider.getValue()));
        this.noiseSize.set((int) (this.FragNoiseSlider.getValue()));
        this.fragCount.set((float) (this.FragCountSlider.getValue()));
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
        this.BurstSizeSlider.setValue(DEFAULT_SQUARE_SIZE);
        this.SplitLineCheckBox.setSelected(DEFAULT_SPLIT_LINE);
        this.FragSizeSlider.setValue(DEFAULT_FRAG_SIZE);
        this.FragNoiseSlider.setValue(DEFAULT_NOISE_SIZE);
        this.FragCountSlider.setValue(DEFAULT_FRAG_COUNT);
    }

    @Override
    public void cancel(final ActionEvent event) {
        this.BurstSizeSlider.setValue(this.prevSquareSize);
        this.SplitLineCheckBox.setSelected(this.prevSplitLine);
        this.FragSizeSlider.setValue(this.prevFragSize);
        this.FragCountSlider.setValue(this.prevFragCount);
        this.FragNoiseSlider.setValue(this.prevNoiseSize);
        this.closeDialog();
    }

    @Override
    public void initialize(final URL fxmlFileLocation,
            final ResourceBundle resources) {
        assert this.root != null : "fx:id=\"root\" was not injected: check your FXML file 'binarizeDialog.fxml'.";
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
    public void initBoxes(final IntegerProperty squareSizeProperty,
            final BooleanProperty splitLineProperty,
            final IntegerProperty fragSizeProperty,
            final FloatProperty fragCountProperty,
            final IntegerProperty fragNoiseProperty) {

        this.prevSquareSize = squareSizeProperty.get();
        this.prevSplitLine = splitLineProperty.get();
        this.prevFragSize = fragSizeProperty.get();
        this.prevFragCount = fragCountProperty.get();
        this.BurstSizeSlider.setValue(squareSizeProperty.get());
        this.SplitLineCheckBox.setSelected(splitLineProperty.get());
        this.FragSizeSlider.setValue(fragSizeProperty.get());
        this.FragCountSlider.setValue(fragCountProperty.get());
        this.FragNoiseSlider.setValue(fragNoiseProperty.get());
        this.squareSize = squareSizeProperty;
        this.FragSizeLabel.setText((int) fragSizeProperty.get() + "");
        NumberFormat n = NumberFormat.getInstance(Locale.ENGLISH);
        n.setMaximumFractionDigits(1);
        n.setMinimumFractionDigits(1);
        this.FragCountLabel.setText(n.format(fragCountProperty.get()) + "");
        this.BurstSizeLabel.setText((int) squareSizeProperty.get() + "");
        this.FragNoiseLabel.setText((int) fragNoiseProperty.get() + "");
        this.splitLine = splitLineProperty;
        this.fragSize = fragSizeProperty;
        this.noiseSize = fragNoiseProperty;
        this.fragCount = fragCountProperty;

        this.BurstSizeSlider.valueProperty().addListener(
                new InvalidationListener() {
                    @Override
                    public void invalidated(final Observable observable) {
                        FragSettingsDialog.this.BurstSizeLabel
                                .setText((int) FragSettingsDialog.this.BurstSizeSlider
                                        .getValue() + "");
                    }
                });

        this.FragNoiseSlider.valueProperty().addListener(
                new InvalidationListener() {
                    @Override
                    public void invalidated(final Observable observable) {
                        FragSettingsDialog.this.FragNoiseLabel
                                .setText((int) FragSettingsDialog.this.FragNoiseSlider
                                        .getValue() + "");
                    }
                });

        this.FragSizeSlider.valueProperty().addListener(
                new InvalidationListener() {
                    @Override
                    public void invalidated(final Observable observable) {
                        FragSettingsDialog.this.FragSizeLabel
                                .setText((int) FragSettingsDialog.this.FragSizeSlider
                                        .getValue() + "");
                    }
                });

        this.FragCountSlider.valueProperty().addListener(
                new InvalidationListener() {
                    @Override
                    public void invalidated(final Observable observable) {
                        NumberFormat n = NumberFormat
                                .getInstance(Locale.ENGLISH);
                        n.setMaximumFractionDigits(1);
                        n.setMinimumFractionDigits(1);
                        FragSettingsDialog.this.FragCountLabel.setText(n
                                .format(FragSettingsDialog.this.FragCountSlider
                                        .getValue())
                                + "");
                    }
                });
    }

}
