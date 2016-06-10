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
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;
import de.diptychon.DiptychonLogger;
import de.diptychon.models.data.Glyph;
import de.diptychon.models.misc.PartialGlyphShape;

/**
 * This class provides a glyph dialog to manipulate the extracted glyph. There
 * are three ways to modify the glyph (single pixel, line pixels and chunk
 * pixels).
 */
public class GlyphDialog extends A_Dialog {

    /**
     * Flag to know whether the dialog was closed or not. If closed listener are
     * useless
     */
    private boolean closed;

    /**
     * the fit size
     */
    private final double FIT_TO_SIZE = 250.d;

    /**
     * opacity of glyph
     */
    private final double GLYPH_OPACITY = 0.3;

    /**
     * the color of the glyph
     */
    private final Color GLYPH_COLOR = Color.ORANGERED;

    /**
     * imageview holds binarized image
     */
    @FXML
    // fx:id="binarized"
    private ImageView binarized;

    /**
     * imageview holds the grayscale image
     */
    @FXML
    // fx:id="grayscale"
    private ImageView grayscale;

    /**
     * imageview holds original image
     */
    @FXML
    // fx:id="original"
    private ImageView original;

    /**
     * transcript and the glyph
     */
    @FXML
    // fx:id="transcript"
    private TextField transcript;

    /**
     * group for original view
     */
    @FXML
    private Group originalGroup;

    /**
     * group for gray scaled view
     */
    @FXML
    private Group grayscaleGroup;

    /**
     * group for binarized view
     */
    @FXML
    private Group binarizedGroup;

    /**
     * toggle button for single pixel
     */
    @FXML
    private ToggleButton singlePixel;

    /**
     * toggle button for line pixels
     */
    @FXML
    private ToggleButton linePixels;

    /**
     * toggle button for chunk pixels
     */
    @FXML
    private ToggleButton chunkPixels;

    /**
     * toggle button for chunk pixels eras
     */
    @FXML
    private ToggleButton eraseToggle;

    /**
     * toggle button for line pixels
     */
    @FXML
    private Label idLabel;

    /**
     * toggle button for chunk pixels
     */
    @FXML
    private TextField idTextField;

    /**
     * combobox contains the number of pixels which the user wants to select
     */
    @FXML
    private ComboBox<String> numPixels;

    /**
     * toggle group to hold three toggle buttons (singlePixel, linePixels and
     * chunkPixels)
     */
    private ToggleGroup toggleGroup;

    /**
     * glyph contains the image
     */
    private Glyph glyph;

    /**
     * glyph contains the original image
     */
    private Glyph oldGlyph;

    /**
     * scale
     */
    private double scale;

    /**
     * path of line
     */
    private Path path;

    /**
     * if true a glyph will be updated when the dialog is closed by accept,
     * otherwise it will added as new glyph
     */
    private boolean edit;

    private String idOld;

    private String lineIdOld;

    /**
     * This method is used to assign images to three views
     * 
     * @param pGlyph
     *            object of glyph
     */
    public void setNewGlyph(final Glyph pGlyph) {
        this.edit = false;
        this.glyph = pGlyph;
        this.original.setImage(pGlyph.getImage());
        this.grayscale.setImage(pGlyph.getGrayImageAsFXImage());
        this.binarized.setImage(pGlyph.getBinarizedImageAsFXImage());
        final double scaleX = this.FIT_TO_SIZE / pGlyph.getWidth();
        final double scaleY = this.FIT_TO_SIZE / pGlyph.getHeight();
        this.scale = Math.min(scaleX, scaleY);
        this.original.toFront();
        this.glyph.intersectsBorder();
        this.transcript.setText(this.glyph.getGroupID());
        this.updateShapeToDraw();
    }

    /**
     * Sets a glyph in edit mode instead of extract mode
     * 
     * @param pGlyph
     *            the glyph
     */
    public void setGlyphToEdit(final Glyph pGlyph) {
        this.idLabel.setVisible(true);
        this.idTextField.setVisible(true);
        this.idOld = pGlyph.getID();
        if (pGlyph.getLineID() != null) {
            this.idTextField.setText(pGlyph.getLineID());
            this.lineIdOld = pGlyph.getLineID();
        } else {
            this.idTextField.setText("");
            this.lineIdOld = null;
        }
        this.edit = true;
        this.glyph = pGlyph;
        this.oldGlyph = pGlyph.copy();
        this.original.setImage(pGlyph.getImage());
        this.grayscale.setImage(pGlyph.getGrayImageAsFXImage());
        this.binarized.setImage(pGlyph.getBinarizedImageAsFXImage());
        final double scaleX = this.FIT_TO_SIZE / pGlyph.getWidth();
        final double scaleY = this.FIT_TO_SIZE / pGlyph.getHeight();
        this.scale = Math.min(scaleX, scaleY);
        this.original.toFront();

        this.transcript.setText(this.glyph.getGroupID());
        this.updateShapeToDraw();
    }

    /**
     * This method is used to set scale to the containers
     * 
     * @param pScale
     *            the value of scale
     */
    private void scale(final double pScale) {
        this.originalGroup.setScaleX(pScale);
        this.originalGroup.setScaleY(pScale);
        this.grayscaleGroup.setScaleX(pScale);
        this.grayscaleGroup.setScaleY(pScale);
        this.binarizedGroup.setScaleX(pScale);
        this.binarizedGroup.setScaleY(pScale);
        final double translateY = (this.originalGroup.getBoundsInParent()
                .getHeight() - this.originalGroup.getBoundsInLocal()
                .getHeight()) / 2.;
        this.originalGroup.setTranslateY(translateY);
        this.grayscaleGroup.setTranslateY(translateY);
        this.binarizedGroup.setTranslateY(translateY);
    }

    /**
     * This method is used to attach the mouse clicked event to the glyph shape.
     * 
     * @param glyphShape
     *            the glyph shape which is going to be attached.
     */
    private void addShapeOnMouseClicked(final Shape glyphShape) {
        glyphShape.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(final MouseEvent event) {
                if (!GlyphDialog.this.closed
                        && event.getButton().equals(MouseButton.PRIMARY)) {
                    GlyphDialog.this.handleMouseClick(event);
                    event.consume();
                }
            }
        });
    }

    /**
     * This method is called by the FXMLLoader when initialization is complete.
     * It initializes the toggleGroup and combobox numPixels.
     */
    @Override
    public void initialize(final URL fxmlFileLocation,
            final ResourceBundle resources) {
        assert this.binarized != null : "fx:id=\"binarized\" was not injected: check "
                + "your FXML file 'GlyphEditor.fxml'.";
        assert this.grayscale != null : "fx:id=\"grayscale\" was not injected: check "
                + "your FXML file 'GlyphEditor.fxml'.";
        assert this.original != null : "fx:id=\"original\" was not injected: check your FXML file 'GlyphEditor.fxml'.";
        assert this.transcript != null : "fx:id=\"transcript\" was not injected: check "
                + "your FXML file 'GlyphEditor.fxml'.";

        this.toggleGroup = new ToggleGroup();
        this.toggleGroup.getToggles().add(this.singlePixel);
        this.toggleGroup.getToggles().add(this.linePixels);
        this.toggleGroup.getToggles().add(this.chunkPixels);

        this.path = new Path();
        this.numPixels.setValue("9");
        this.numPixels.setDisable(true);
        this.closed = false;
    }

    /**
     * This method is used to handle the mouse click event.
     * 
     * @param event
     *            mouse event.
     */
    @FXML
    private void handleMouseClick(final MouseEvent event) {
        this.mouseClick(event);
    }

    /**
     * This method is used to handle the mouse click on toggle. Only when the
     * chunk pixel button is pressed, combobox numPixel is activated, otherwise,
     * it is deactivated.
     * 
     * @param event
     *            mouse event
     */
    @FXML
    private void handleMouseClickGoggle(final MouseEvent event) {
        if (this.toggleGroup.getSelectedToggle() == this.chunkPixels) {
            this.numPixels.setDisable(false);
        } else {
            this.numPixels.setDisable(true);
        }
    }

    /**
     * This method is used to handle the mouse click. If the singlePixels button
     * is activated, one click to draw a pixel. If the chunkPixels button is
     * activated, one click to draw pixels according to the selected value from
     * combobox.
     * 
     * @param event
     *            mouse event
     */
    private void mouseClick(final MouseEvent event) {
        final int x = (int) (event.getX());
        final int y = (int) (event.getY());
        if (!this.closed && event.getButton().equals(MouseButton.PRIMARY)
                && this.toggleGroup.getSelectedToggle() == this.singlePixel) {
            DiptychonLogger.info("Single Pixel is clicked on glyph.");
            this.invertPixels(x, y);
            this.glyph.updateShape(false);
            event.consume();
        } else if (!this.closed
                && event.getButton().equals(MouseButton.PRIMARY)
                && this.toggleGroup.getSelectedToggle() == this.chunkPixels) {
            final String value = this.numPixels.getValue();
            DiptychonLogger.info("Chunk Pixel({} pixels) is clicked on glyph.",
                    value);
            if (this.eraseToggle.isSelected()) {
                this.whitenPixels(x, y);
            } else {
                this.blackenPixels(x, y);
            }
            switch (value) {
            case "2": {
                if ((x + 1) < this.glyph.getWidth()) {
                    if (this.eraseToggle.isSelected()) {
                        this.whitenPixels(x + 1, y);
                    } else {
                        this.blackenPixels(x + 1, y);
                    }
                }
                break;
            }
            case "4": {
                if ((x + 1) < this.glyph.getWidth()) {
                    this.invertPixels(x + 1, y);
                    if ((y + 1) < this.glyph.getHeight()) {
                        if (this.eraseToggle.isSelected()) {
                            this.whitenPixels(x + 1, y + 1);
                        } else {
                            this.blackenPixels(x + 1, y + 1);
                        }
                    }
                }
                if ((y + 1) < this.glyph.getHeight()) {
                    if (this.eraseToggle.isSelected()) {
                        this.whitenPixels(x, y + 1);
                    } else {
                        this.blackenPixels(x, y + 1);
                    }
                }
                break;
            }
            case "6": {
                if ((x + 1) < this.glyph.getWidth()) {
                    if (this.eraseToggle.isSelected()) {
                        this.whitenPixels(x + 1, y);
                    } else {
                        this.blackenPixels(x + 1, y);
                    }

                    if ((y + 1) < this.glyph.getHeight()) {
                        if (this.eraseToggle.isSelected()) {
                            this.whitenPixels(x + 1, y + 1);
                        } else {
                            this.blackenPixels(x + 1, y + 1);
                        }
                    }
                    if ((y + 2) < this.glyph.getHeight()) {
                        if (this.eraseToggle.isSelected()) {
                            this.whitenPixels(x + 1, y + 2);
                        } else {
                            this.blackenPixels(x + 1, y + 2);
                        }
                    }
                }
                if ((y + 1) < this.glyph.getHeight()) {
                    if (this.eraseToggle.isSelected()) {
                        this.whitenPixels(x, y + 1);
                    } else {
                        this.blackenPixels(x, y + 1);
                    }
                }
                if ((y + 2) < this.glyph.getHeight()) {
                    if (this.eraseToggle.isSelected()) {
                        this.whitenPixels(x, y + 2);
                    } else {
                        this.blackenPixels(x, y + 2);
                    }
                }

                break;
            }
            case "9": {
                if ((x + 1) < this.glyph.getWidth()) {
                    if (this.eraseToggle.isSelected()) {
                        this.whitenPixels(x + 1, y);
                    } else {
                        this.blackenPixels(x + 1, y);
                    }
                    if ((y + 1) < this.glyph.getHeight()) {
                        if (this.eraseToggle.isSelected()) {
                            this.whitenPixels(x + 1, y + 1);
                        } else {
                            this.blackenPixels(x + 1, y + 1);
                        }
                    }
                    if ((y + 2) < this.glyph.getHeight()) {
                        if (this.eraseToggle.isSelected()) {
                            this.whitenPixels(x + 1, y + 2);
                        } else {
                            this.blackenPixels(x + 1, y + 2);
                        }
                    }

                }
                if ((x + 2) < this.glyph.getWidth()) {
                    if (this.eraseToggle.isSelected()) {
                        this.whitenPixels(x + 2, y);
                    } else {
                        this.blackenPixels(x + 2, y);
                    }
                    if ((y + 1) < this.glyph.getHeight()) {
                        if (this.eraseToggle.isSelected()) {
                            this.whitenPixels(x + 2, y + 1);
                        } else {
                            this.blackenPixels(x + 2, y + 1);
                        }
                    }
                    if ((y + 2) < this.glyph.getHeight()) {
                        if (this.eraseToggle.isSelected()) {
                            this.whitenPixels(x + 2, y + 2);
                        } else {
                            this.blackenPixels(x + 2, y + 2);
                        }
                    }
                }
                if ((y + 1) < this.glyph.getHeight()) {
                    if (this.eraseToggle.isSelected()) {
                        this.whitenPixels(x, y + 1);
                    } else {
                        this.blackenPixels(x, y + 1);
                    }
                }
                if ((y + 2) < this.glyph.getHeight()) {
                    if (this.eraseToggle.isSelected()) {
                        this.whitenPixels(x, y + 2);
                    } else {
                        this.blackenPixels(x, y + 2);
                    }
                }

                break;
            }
            default:
                break;
            }
            this.glyph.updateShape(false);
        } else {
            this.glyph.invertActiveShape(x, y);
            this.binarized.setImage(this.glyph.getBinarizedImageAsFXImage());
            event.consume();
        }
        this.updateShapeToDraw();
    }

    /**
     * Inverts the pixel at positon (x,y)
     * 
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     */
    private void invertPixels(final int x, final int y) {
        this.glyph.invertPixel(x, y);
        this.binarized.setImage(this.glyph.getBinarizedImageAsFXImage());
    }

    /**
     * Sets the pixel at positon (x,y) to white
     * 
     * @param x
     *            the x coordinate
     * @param y
     *            the y coordinate
     */
    private void whitenPixels(final int x, final int y) {
        this.glyph.whitenPixel(x, y);
        this.binarized.setImage(this.glyph.getBinarizedImageAsFXImage());
    }

    private void blackenPixels(final int x, final int y) {
        this.glyph.blackenPixel(x, y);
        this.binarized.setImage(this.glyph.getBinarizedImageAsFXImage());
    }

    /**
     * This method is used to update the shape and attach the mouse click
     * handler to each shape.
     */
    private void updateShapeToDraw() {
        final ObservableList<Node> children = this.grayscaleGroup.getChildren();
        for (int i = children.size() - 1; i > 0; --i) {
            children.remove(i);
        }
        final ArrayList<PartialGlyphShape> glyphShape = this.glyph.getShape();
        for (int i = 0; i < glyphShape.size(); ++i) {
            if (glyphShape.get(i).isActive()) {
                final Shape s = glyphShape.get(i).getShape();
                s.setOpacity(this.GLYPH_OPACITY);
                s.setFill(this.GLYPH_COLOR);
                this.addShapeOnMouseClicked(s);
                children.add(s);
            }
        }
        this.scale(this.scale);
    }

    @Override
    public void apply(final ActionEvent event) {
        if (!this.idTextField.getText().equals(this.lineIdOld) && this.edit) {
            this.documentPanelController.acceptOverlappingGlyph(this.glyph,
                    this.idTextField.getText());
            if (this.glyph.getLineID().equals("badLine")) {
                System.out.println("Warnung: Ungültige LineID");
                this.idLabel.setStyle("-fx-text-fill: red;");
                this.idTextField.requestFocus();
                this.glyph.setLineID(this.lineIdOld);
                return;
            }
            this.documentPanelController.removeGlyph(this.idOld);

            this.glyph.setLineID(this.idTextField.getText());
        }
        final String text = this.transcript.getText();
        if (text == null || text.isEmpty()) {
            final InformationDialog id = new InformationDialog.Factory()
                    .createDialogWithExclamationMark(
                            "Please type in transcription!", Pos.CENTER, 200);
            id.showDialog(this.root.getScene().getWindow(), -1, -1);
            return;
        }

        this.grayscaleGroup.getChildren().clear();
        this.glyph.removeInactive();
        if (!this.glyph.getShape().isEmpty()) {
            this.glyph.setGroupID(text);
            if (this.edit) {
                DiptychonLogger.info("Edit Glyph.");
                this.glyph.isCreatedByEditor = true;
                this.documentPanelController.editGlyph(this.glyph);
            } else {
                DiptychonLogger.info("Accept new Glyph.");
                this.glyph.isCreatedByEditor = true;
                this.documentPanelController.acceptOverlappingGlyph(this.glyph);
            }
            this.closed = true;
            this.closeDialog();
        }
    }

    @Override
    public void cancel(final ActionEvent event) {
        DiptychonLogger.info("Cancel the Glyph dialog.");
        if (this.edit) {
            this.glyph.setBinarizedImage(this.oldGlyph.getBinarizedImage());
            this.glyph.updateShape(false);
        }
        this.closed = true;
        this.closeDialog();
    }

    /**
     * This method is used to handle the mouse press event. It is activated only
     * when linePixels button is selected. It stores the start points of a line.
     * 
     * @param ev
     *            mouse event
     */
    @FXML
    private void handleMousePressed(final MouseEvent ev) {
        if (this.toggleGroup.getSelectedToggle() != this.linePixels) {
            return;
        }

        if (((Node) ev.getSource()).getId().contentEquals("binarized")) {
            if (this.grayscaleGroup.getChildren().contains(this.path)) {
                this.grayscaleGroup.getChildren().remove(this.path);
            }

            if (!this.binarizedGroup.getChildren().contains(this.path)) {
                this.binarizedGroup.getChildren().add(this.path);
            }
            this.path.toFront();
        } else if (((Node) ev.getSource()).getId().contentEquals("grayscale")) {
            if (this.binarizedGroup.getChildren().contains(this.path)) {
                this.binarizedGroup.getChildren().remove(this.path);
            }

            if (!this.grayscaleGroup.getChildren().contains(this.path)) {
                this.grayscaleGroup.getChildren().add(this.path);
            }
            this.path.toFront();
        }

        if (this.path.getElements().size() != 0) {
            this.path.getElements().clear();
        }
        this.path.getElements().add(new MoveTo(ev.getX(), ev.getY()));

    }

    /**
     * This method is used to handle the mouse dragged event. It draws the line
     * according the moving mouse.
     * 
     * @param ev
     *            mouse event
     */
    @FXML
    private void handleMouseDragged(final MouseEvent ev) {
        if (this.toggleGroup.getSelectedToggle() != this.linePixels) {
            return;
        }
        if (this.path.getElements().size() == 1) {
            this.path.getElements().add(new LineTo(ev.getX(), ev.getY()));
        } else if (this.path.getElements().size() == 2) {
            if (ev.getX() <= this.glyph.getWidth() && ev.getX() >= 0
                    && ev.getY() <= this.glyph.getHeight() && ev.getY() >= 0) {
                final LineTo lt = (LineTo) this.path.getElements().get(1);
                lt.setX(ev.getX());
                lt.setY(ev.getY());

            }

        }

    }

    /**
     * This method is used to handle the mouse release event. It gets the start
     * point and end point of a line and calcute the line. It turns the black
     * pixels into white in the line.
     * 
     * @param ev
     *            mouse event
     */
    @FXML
    private void handleMouseReleased(final MouseEvent ev) {
        if (this.toggleGroup.getSelectedToggle() != this.linePixels) {
            return;
        }
        if (this.path.getElements().size() == 2) {
            final int startX = (int) ((MoveTo) (this.path.getElements().get(0)))
                    .getX();
            final int startY = (int) ((MoveTo) (this.path.getElements().get(0)))
                    .getY();
            final int endX = (int) ((LineTo) (this.path.getElements()).get(1))
                    .getX();
            final int endY = (int) ((LineTo) (this.path.getElements()).get(1))
                    .getY();
            DiptychonLogger.info(
                    "Using line split region from ({}/{}) to ({}/{})", startX,
                    startY, endX, endY);

            final int sx = startX < endX ? startX : endX;
            final int ex = startX < endX ? endX : startX;
            int x = sx;
            if (endY == startY) {
                final int y = startY;
                while (x <= ex) {
                    this.glyph.whitenPixel(x, y);
                    ++x;
                }
            } else if (endX == startX) {
                final int sy = startY < endY ? startY : endY;
                final int ey = startY < endY ? endY : startY;
                int y = sy;
                while (y <= ey) {
                    this.glyph.whitenPixel(x, y);
                    ++y;
                }
            } else if ((endY - startY) != 0) {
                final double k = (double) (endY - startY)
                        / (double) ((endX - startX));// *scaleT
                final int b = (int) (startY - k * startX);// *scaleT

                // k *= scaleT;
                while (x <= ex) {
                    final int y = (int) (k * x + b);
                    if (Math.abs(y) >= this.glyph.getHeight()
                            || x >= this.glyph.getWidth() || y < 0) {
                        DiptychonLogger.error("error X: {}, Y: {}", x, y);
                        ++x;
                        continue;
                    }
                    this.glyph.whitenPixel(x, y);
                    if (x > 0) {
                        this.glyph.whitenPixel(x - 1, y);
                    }
                    if (x < this.glyph.getWidth() - 1) {
                        this.glyph.whitenPixel(x + 1, y);
                    }
                    if (y < this.glyph.getHeight() - 1) {
                        this.glyph.whitenPixel(x, y + 1);
                    }
                    if (y > 0) {
                        this.glyph.whitenPixel(x, y - 1);
                    }
                    ++x;
                }
            }
            this.binarized.setImage(this.glyph.getBinarizedImageAsFXImage());
            this.glyph.updateShape(false);
            this.updateShapeToDraw();
        }
        if (this.path.getElements().size() > 0) {
            this.path.getElements().clear();
        }
    }

}
