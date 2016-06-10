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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * This class is inherited from A_Dialog and it is used to created wizard
 * dialog. In this class many methods are abstact method and they will be
 * implemented by spuer-class.
 */
public abstract class WizardDialog extends A_Dialog {

    /**
     * This is used to be types of dialogs.
     */
    public enum emDialogType {
        emFileChooser, emGrayScale, emBinarizing, emTextLines, emStart, emFinish, emCancel;
    }

    /**
     * It is the own type of current dialog.
     */
    protected emDialogType ownType;

    /**
     * It is the type of previous dialog.
     */
    protected emDialogType previousType;

    /**
     * It is the type of next dialog.
     */
    protected emDialogType nextType;

    /**
     * It means the dialog after current dialog when the wizard dialogs are
     * running
     */
    protected emDialogType nextState;

    /**
     * It is the stage and in this case it is the dialog.
     */
    protected Stage stage;

    /**
     * This method is used to initialize the dialog.
     * 
     * @param win
     *            It is the owner dialog.
     */
    public void initial(final Window win) {
        this.stage = new Stage(StageStyle.TRANSPARENT);
        this.stage.initModality(Modality.WINDOW_MODAL);
        this.stage.initOwner(win);
        final Scene scene = new Scene((Parent) this.root);
        scene.setFill(null);
        this.stage.setScene(scene);
    }

    @Override
    protected void apply(final ActionEvent event) {

    }

    @Override
    @FXML
    protected abstract void cancel(ActionEvent event);

    /**
     * It is used to be as next function in wizard dialog.
     * 
     * @param event
     *            It is an action event parameter.
     */
    @FXML
    protected abstract void next(ActionEvent event);

    /**
     * It is used to be as previous function in wizard dialog.
     * 
     * @param event
     *            It is an action event parameter.
     */
    @FXML
    protected abstract void previous(ActionEvent event);

    /**
     * It is used to be as finish function in wizard dialog.
     * 
     * @param event
     *            It is an action event parameter.
     */
    @FXML
    protected abstract void finish(ActionEvent event);

    /**
     * This method is used to get its own type.
     * 
     * @return return emDialogType
     */
    protected emDialogType getOwnType() {
        return this.ownType;
    }

    /**
     * This method is used to set its own type.
     * 
     * @param em
     *            emDialogType
     */
    protected void setOwnType(final emDialogType em) {
        this.ownType = em;
    }

    /**
     * This method is used to get the type of next dialog.
     * 
     * @return return emDialogType
     */
    protected emDialogType getNextType() {
        return this.nextType;
    }

    /**
     * This method is used to set the type of next dialog.
     * 
     * @param em
     *            emDialogType
     */
    protected void setNextType(final emDialogType em) {
        this.nextType = em;
    }

    /**
     * This method is used to get the type of previous dialog.
     * 
     * @return emDialogType
     */
    protected emDialogType getPreviousType() {
        return this.previousType;
    }

    /**
     * This method is used to set the type of previous dialog.
     * 
     * @param em
     *            emDialogType
     */
    protected void setPreviousType(final emDialogType em) {
        this.previousType = em;
    }

    /**
     * This method is used to show dialog.
     */
    public void showDialog() {
        this.stage.showAndWait();
    }

    /**
     * This method is used to hide dialog.
     */
    public void hideDialog() {
        this.stage.hide();
    }

    /**
     * This method is to get type of the next step when finishing current
     * dialog.
     * 
     * @return emDialogType
     */
    public emDialogType getNextState() {
        return this.nextState;
    }

    /**
     * This method is to set the type of the next step when finishing current
     * dialog.
     * 
     * @param emType
     *            the next wizard type
     */
    public void setNextState(final emDialogType emType) {
        this.nextState = emType;
    }

    /**
     * This method is used to set the title of dialog.
     * 
     * @param title
     *            name
     */
    public void setTitle(final String title) {
        this.stage.setTitle(title);
    }
}
