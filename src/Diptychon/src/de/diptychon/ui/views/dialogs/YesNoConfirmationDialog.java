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

import java.awt.Point;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import de.diptychon.DiptychonLogger;

/**
 * Base class for YesNo Confirmation Dialogs
 */
public class YesNoConfirmationDialog implements Initializable {

    /**
     * The root node
     */
    @FXML
    private Node root;

    /**
     * The label for the text to be shown
     */
    @FXML
    private Label infoText;

    /**
     * Property which is used to notify the owner window about the state of this
     * dialog
     */
    private IntegerProperty accepted;

    /**
     * Functionality which is used to react on the acceptance status
     */
    private A_YesNoFunctionality yesNoFunctionality;

    /**
     * Gets the root node
     * 
     * @return the root node
     */
    public Node getView() {
        return this.root;
    }

    /**
     * Sets the yesNoFunctionality
     * 
     * @param pYesNoFunctionality
     *            the yesNoFunctionality
     */
    private void setYesNoFunctionality(
            final A_YesNoFunctionality pYesNoFunctionality) {
        this.yesNoFunctionality = pYesNoFunctionality;
    }

    /**
     * Sets the text of the label
     * 
     * @param text
     *            the text
     */
    private void setInfoText(final String text) {
        this.infoText.setText(text);
    }

    /**
     * Closes the dialog
     */
    private void closeDialog() {
        this.root.toBack();
        this.root.getScene().getWindow().hide();
    }

    /**
     * This method is invoked when the user clicks the accept button.
     */
    @FXML
    private void accept() {
        this.yesNoFunctionality.accept();
        this.accepted.set(1);
        this.closeDialog();
    };

    /**
     * This method is invoked when the user clicks the deny button.
     */
    @FXML
    private void deny() {
        this.yesNoFunctionality.deny();
        this.accepted.set(-1);
        this.closeDialog();
    };

    @Override
    public void initialize(final URL fxmlFileLocation,
            final ResourceBundle resources) {
        this.accepted = new SimpleIntegerProperty(0);
    };

    /**
     * Gets the acceptance property
     * 
     * @return the acceptance property
     */
    public IntegerProperty getAcceptedProperty() {
        return this.accepted;
    }

    /**
     * Shows the dialog as modal dialog, i.e. it will block the rest of the
     * application.
     * 
     * @param owner
     *            The owner Window of this dialog
     * @param x
     *            The <code>x</code> position on the screen the dialog should be
     *            displayed at. -1 if it should be centered
     * @param y
     *            The <code>y</code> position on the screen the dialog should be
     *            displayed at. -1 if it should be centered
     */
    public void showModalDialog(final Window owner, final int x, final int y) {
        this.showDialog(owner, Modality.APPLICATION_MODAL, x, y);
    }

    /**
     * Used to show the current dialog. Depending on the <code>modality</code>
     * the application is blocked until the dialog is closed or it is still
     * possible to use the application
     * 
     * @param owner
     *            The owner Window of this dialog
     * @param modality
     *            <code>Modality.APPLICATION_MODAL</code> or
     *            <code>Modality.WINDOW_MODAL</code> if the application should
     *            be blocked <code>Modality.NONE</code> else
     * @param x
     *            The <code>x</code> position on the screen the dialog should be
     *            displayed at. -1 if it should be centered
     * @param y
     *            The <code>y</code> position on the screen the dialog should be
     *            displayed at. -1 if it should be centered
     */
    private void showDialog(final Window owner, final Modality modality,
            final int x, final int y) {
        // final Popup popup = PopupBuilder.create().content(node).build();
        final Stage popup = new Stage(StageStyle.TRANSPARENT);
        popup.initOwner(owner);
        popup.initModality(modality);
        final Scene scene = new Scene((Parent) this.root);
        scene.setFill(null);
        popup.setScene(scene);

        final Point dragDelta = new Point();
        this.root.getScene().setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                if (event.getY() < 60) {
                    YesNoConfirmationDialog.this.root.getScene().setCursor(
                            Cursor.MOVE);
                    dragDelta.x = (int) (popup.getX() - event.getScreenX());
                    dragDelta.y = (int) (popup.getY() - event.getScreenY());
                    event.consume();
                }
            }
        });
        this.root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                if (event.getY() < 60) {
                    popup.getScene().setCursor(Cursor.MOVE);
                    popup.setX(event.getScreenX() + dragDelta.x);
                    popup.setY(event.getScreenY() + dragDelta.y);
                    event.consume();
                }
            }
        });

        this.root.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                popup.getScene().setCursor(Cursor.DEFAULT);
            }
        });

        if (x != -1 && y != -1) {
            popup.setX(x);
            popup.setY(y);
        }
        if (modality.equals(Modality.NONE)) {
            popup.show();
        } else {
            popup.showAndWait();
        }
    }

    /**
     * Factory class which is used to create all Dialogs that extends
     * <code>YesNoConfirmationDialog</code>.
     */
    public static class Factory {
        /**
         * Creates a new dialog
         * 
         * @param pYesNoFunctionality
         *            the desired functionality
         * @param pInfoText
         *            the text to be shown
         * @return the new dialog
         */
        public YesNoConfirmationDialog createDialog(
                final A_YesNoFunctionality pYesNoFunctionality,
                final String pInfoText) {
            YesNoConfirmationDialog yesNoConfirmationDialog = null;
            try {
                final FXMLLoader loader = new FXMLLoader();
                loader.setLocation(new URL("file:/"));
                loader.load(YesNoConfirmationDialog.class
                        .getResourceAsStream("/fxml/"
                                + "YesNoConfirmationDialog" + ".fxml"));
                DiptychonLogger.info("Load dialogFrame: {}", ("/fxml/"
                        + "YesNoConfirmationDialog" + ".fxml"));
                yesNoConfirmationDialog = (YesNoConfirmationDialog) loader
                        .getController();
                yesNoConfirmationDialog
                        .setYesNoFunctionality(pYesNoFunctionality);
                yesNoConfirmationDialog.setInfoText(pInfoText);
            } catch (final IOException e) {
                DiptychonLogger.error("Unable to load {}.fxml",
                        YesNoConfirmationDialog.class.toString());
                DiptychonLogger.error("{}", e);
            }
            return yesNoConfirmationDialog;
        }
    }
}
