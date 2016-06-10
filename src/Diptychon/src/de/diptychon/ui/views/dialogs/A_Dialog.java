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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import de.diptychon.DiptychonLogger;
import de.diptychon.controller.DocumentPanelController;

/**
 * An abstract class which provides basic functionality for a dialog.
 */
public abstract class A_Dialog implements Initializable {

    /**
     * The Parent Node of this Dialog.
     */
    @FXML
    protected Node root;

    /**
     * The reference to a <code>DocumentPanelController</code> which is needed
     * to communicate with the models.
     */
    protected DocumentPanelController documentPanelController;

    /**
     * Gets the Parent Node of this Dialog. Necessary to be able to include this
     * panel into the stage.
     * 
     * @return The Parent Node of this panel.
     */
    public Node getView() {
        return this.root;
    }

    /**
     * Sets the <code>DocumentPanelController</code>.
     * 
     * @param pDocumentPanelController
     *            The reference to a <code>DocumentPanelController</code>
     */
    private void setDocumentPanelController(
            final DocumentPanelController pDocumentPanelController) {
        this.documentPanelController = pDocumentPanelController;
    }

    /**
     * Closes the dialog. i.e. it is moved to the background and hidden. If
     * there are any listener registered, those should be unregistered to avoid
     * strange side effects.
     */
    protected void closeDialog() {
        this.root.toBack();
        this.root.getScene().getWindow().hide();
    }

    /**
     * This method is invoked when the user clicks the apply button.
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    protected abstract void apply(ActionEvent event);

    /**
     * This method is invoked when the user clicks the cancel button.
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    protected abstract void cancel(ActionEvent event);

    @Override
    public abstract void initialize(URL fxmlFileLocation,
            ResourceBundle resources);

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
     * Shows the dialog as none modal dialog. i.e. the application will not be
     * blocked.
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
    public void showNormalDialog(final Window owner, final int x, final int y) {
        this.showDialog(owner, Modality.NONE, x, y);
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
                    A_Dialog.this.root.getScene().setCursor(Cursor.MOVE);
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
     * <code>A_Dialog</code>.
     */
    public static class Factory {
        /**
         * Creates a new dialog
         * 
         * @param dialogName
         *            the filename of the corresponding fxml file
         * @param documentPanelController
         *            The reference to a DocumentPanelController
         * @return The created Dialog
         */
        public A_Dialog createDialog(final String dialogName,
                final DocumentPanelController documentPanelController) {
            final String[] split = dialogName.split("\\.");
            A_Dialog a_Dialog = null;
            try {
                final FXMLLoader loader = new FXMLLoader();
                loader.setLocation(new URL("file:/"));
                loader.load(A_Dialog.class.getResourceAsStream("/fxml/"
                        + split[split.length - 1] + ".fxml"));
                DiptychonLogger.info("Load dialogFrame: {}", ("/fxml/"
                        + split[split.length - 1] + ".fxml"));
                a_Dialog = (A_Dialog) loader.getController();
                a_Dialog.setDocumentPanelController(documentPanelController);
            } catch (final IOException e) {
                DiptychonLogger.error("Unable to load {}.fxml",
                        split[split.length - 1]);
                DiptychonLogger.error("{}", e);
            }
            return a_Dialog;
        }
    }
}
