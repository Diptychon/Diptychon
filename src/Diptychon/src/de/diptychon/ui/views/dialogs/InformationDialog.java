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

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import de.diptychon.DiptychonLogger;

/**
 * Default Information Dialog. Can be set to arbitrary text informations
 */
public class InformationDialog implements Initializable {

    /**
     * The root node
     */
    @FXML
    protected Node root;

    /**
     * The infoText to show
     */
    @FXML
    protected Label infoText;

    /**
     * Gets the root node
     * 
     * @return the root node
     */
    public Node getView() {
        return this.root;
    }

    /**
     * Sets the text to show
     * 
     * @param text
     *            the text
     * @param alignment
     *            the desired alignment
     * @param width
     *            the desired dialog width
     */
    private void setInfoText(final String text, final Pos alignment,
            final int width) {
        this.infoText.setPrefWidth(width);
        this.infoText.setMinWidth(Control.USE_PREF_SIZE);
        this.infoText.setMaxWidth(Control.USE_PREF_SIZE);
        this.infoText.setAlignment(alignment);
        this.infoText.setText(text);
    }

    /**
     * Closes the dialog
     */
    @FXML
    private void accept() {
        this.root.toBack();
        this.root.getScene().getWindow().hide();
    };

    @Override
    public void initialize(final URL fxmlFileLocation,
            final ResourceBundle resources) {
    };

    /**
     * Used to show the current dialog. Depending on the <code>modality</code>
     * the application is blocked until the dialog is closed or it is still
     * possible to use the application
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
    public void showDialog(final Window owner, final int x, final int y) {
        final Stage popup = new Stage(StageStyle.TRANSPARENT);
        popup.initOwner(owner);
        popup.initModality(Modality.APPLICATION_MODAL);
        final Scene scene = new Scene((Parent) this.root);
        scene.setFill(null);
        popup.setScene(scene);

        final Point dragDelta = new Point();
        this.root.getScene().setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(final MouseEvent event) {
                if (event.getY() < 60) {
                    InformationDialog.this.root.getScene().setCursor(
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
        popup.showAndWait();
    }

    /**
     * Factory class which is used to create all Dialogs that extends
     * <code>A_Dialog</code>.
     */
    public static class Factory {
        /**
         * Creates a new information dialog without ExclamationMark (e.g. About
         * Dialog)
         * 
         * @param pInfoText
         *            the text
         * @param alignment
         *            the desired alignment
         * @param width
         *            the desired dialog width
         * @return The created Dialog
         */
        public InformationDialog createDialogWithoutExclamationMark(
                final String pInfoText, final Pos alignment, final int width) {
            return this.createDialog("InformationDialogWithoutExclamationMark",
                    pInfoText, alignment, width);
        }

        /**
         * Creates a new information dialog without ExclamationMark (e.g. About
         * Dialog)
         * 
         * @param pInfoText
         *            the text
         * @param alignment
         *            the desired alignment
         * @param width
         *            the desired dialog width
         * @return The created Dialog
         */
        public InformationDialog createDialogWithExclamationMark(
                final String pInfoText, final Pos alignment, final int width) {
            return this.createDialog("InformationDialog", pInfoText, alignment,
                    width);
        }

        /**
         * Creates a new information dialog without ExclamationMark (e.g. About
         * Dialog)
         * 
         * @param type
         *            the name of the dialog
         * @param pInfoText
         *            the text
         * @param alignment
         *            the desired alignment
         * @param width
         *            the desired dialog width
         * @return The created Dialog
         */
        private InformationDialog createDialog(final String type,
                final String pInfoText, final Pos alignment, final int width) {
            InformationDialog informationDialog = null;
            try {
                final FXMLLoader loader = new FXMLLoader();
                loader.setLocation(new URL("file:/"));
                loader.load(InformationDialog.class
                        .getResourceAsStream("/fxml/" + type + ".fxml"));
                DiptychonLogger.info("Load dialogFrame: {}",
                        ("/fxml/" + type + ".fxml"));
                informationDialog = (InformationDialog) loader.getController();
                informationDialog.setInfoText(pInfoText, alignment, width);
            } catch (final IOException e) {
                DiptychonLogger.error("Unable to load {}.fxml",
                        InformationDialog.class.toString());
                DiptychonLogger.error("{}", e);
            }
            return informationDialog;
        }
    }
}
