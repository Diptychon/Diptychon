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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;

/**
 * This class is about wizard dialog of binarizing.
 */
public class WizardBinarizing extends WizardDialog {

    /**
     * slider control of windowSize
     */
    @FXML
    private Slider wsSlider;

    /**
     * slider control of k
     */
    @FXML
    private Slider skSlider;

    /**
     * This method is used to cancel the action of current wizard dialogs.
     * 
     * @event ActionEvent
     */
    @Override
    @FXML
    protected void cancel(final ActionEvent event) {
        this.setNextState(emDialogType.emCancel);
        this.hideDialog();
    }

    @Override
    @FXML
    protected void next(final ActionEvent event) {

    }

    /**
     * This method is used to jump back to previous dialog.
     * 
     * @event ActionEvent
     */
    @Override
    @FXML
    protected void previous(final ActionEvent event) {
        this.setNextState(this.getPreviousType());
        this.hideDialog();
    }

    /**
     * This method is used to complete the wizard dialogs.
     * 
     * @event ActionEvent
     */
    @Override
    @FXML
    protected void finish(final ActionEvent event) {
        this.setNextState(this.getNextType());
        this.hideDialog();

    }

    /**
     * gets the slider value
     * 
     * @return the slider value
     */
    public double getWsValue() {
        return this.wsSlider.getValue();
    }

    /**
     * Gets the slider value
     * 
     * @return the slider value
     */
    public double getSkValue() {
        return this.skSlider.getValue();
    }

    @Override
    public void initialize(final URL fxmlFileLocation,
            final ResourceBundle resources) {
        // TODO Auto-generated method stub

    }

}
