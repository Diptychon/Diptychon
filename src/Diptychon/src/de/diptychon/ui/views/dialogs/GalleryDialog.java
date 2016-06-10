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

import javafx.beans.property.IntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import de.diptychon.models.data.Glyph;

/**
 * This class provides a glyph dialog to manipulate the extracted glyph. There
 * are three ways to modify the glyph (single pixel, line pixels and chunk
 * pixels).
 */
public class GalleryDialog extends A_Dialog {
    /**
     * group for gallery view
     */
    @FXML
    private VBox galleryVBox;

    @FXML
    private AnchorPane gallery;

    @FXML
    private ImageView ivl;

    @FXML
    private ImageView ivr;

    @FXML
    private Label character;

    @FXML
    private Label freq;

    private IntegerProperty showIndex;

    private Glyph[] highlights;

    @Override
    /**
     * This method is called by the FXMLLoader when initialization is complete.
     * It initializes the toggleGroup and combobox numPixels.
     */
    public void initialize(final URL fxmlFileLocation,
            final ResourceBundle resources) {
    }

    @Override
    public void apply(final ActionEvent event) {
        this.showIndex.set(-2);
        this.closeDialog();
    }

    @Override
    public void cancel(final ActionEvent event) {
        this.showIndex.set(-2);
        this.closeDialog();
    }

    public void initGlyphs(final ArrayList<Glyph> glyphs,
            final IntegerProperty showIndex) {
        this.highlights = new Glyph[2];
        this.showIndex = showIndex;
        double maxHeight = 0;
        double curHeight = 0;
        this.freq.setText(glyphs.size() + "");
        this.character.setText(glyphs.get(0).getGroupID());
        for (final Glyph g : glyphs) {
            if (g.isSpace) {
                continue;
            }
            if (g.getImage().getHeight() > maxHeight) {
                maxHeight = g.getImage().getHeight();
            }
            if (curHeight + g.getImage().getHeight() > gallery.getPrefHeight()) {
                this.gallery
                        .setPrefHeight(curHeight + g.getImage().getHeight());
            }
            ImageView iv = new ImageView(g.getImage());
            if (this.gallery.getChildren().size() > 0) {
                ImageView iv2 = (ImageView) this.gallery.getChildren().get(
                        this.gallery.getChildren().size() - 1);
                if (iv2.getLayoutX() + iv2.getBoundsInLocal().getWidth()
                        + iv.getBoundsInLocal().getWidth() + 5 > this.gallery
                            .getPrefWidth()) {
                    iv.setLayoutY(iv2.getLayoutY() + maxHeight + 5);
                    iv.setLayoutX(1);
                    curHeight += maxHeight + 5;
                    maxHeight = 0;
                    if (curHeight > this.gallery.getPrefHeight()) {
                        this.gallery.setPrefHeight(curHeight);
                    }
                } else {
                    iv.setLayoutX(iv2.getLayoutX()
                            + iv2.getBoundsInLocal().getWidth() + 5);
                    iv.setLayoutY(curHeight);
                }
            } else {
                iv.setLayoutX(1);
            }
            iv.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent event) {
                    if (event.getButton().equals(MouseButton.PRIMARY)) {
                        ivl.setImage(g.getImage());
                        GalleryDialog.this.highlights[0] = g;
                        if (GalleryDialog.this.highlights[1] == null) {
                            GalleryDialog.this.documentPanelController
                                    .highlightGlyphs(new Glyph[] { g });
                        } else {
                            GalleryDialog.this.documentPanelController
                                    .highlightGlyphs(highlights);
                        }
                        GalleryDialog.this.showIndex.set(-1);
                        GalleryDialog.this.showIndex.set(0);
                        ivl.setViewport(new Rectangle2D(0, 0, g.getImage()
                                .getWidth(), g.getImage().getHeight()));
                        ivl.setOnScroll(new EventHandler<ScrollEvent>() {

                            @Override
                            public void handle(final ScrollEvent event) {
                                if (event.getDeltaY() > 0) {
                                    ivl.setViewport(new Rectangle2D(ivl
                                            .getViewport().getMinX(), ivl
                                            .getViewport().getMinY(), ivl
                                            .getViewport().getWidth() + 1, ivl
                                            .getViewport().getHeight() + 1));
                                } else {
                                    if (ivl.getViewport().getWidth() - 1 > 0
                                            && ivl.getViewport().getHeight() - 1 > 0
                                            && ivl.getViewport().getWidth() > g
                                                    .getImage().getWidth()
                                            && ivl.getViewport().getHeight() > g
                                                    .getImage().getHeight()) {
                                        ivl.setViewport(new Rectangle2D(
                                                ivl.getViewport().getMinX(),
                                                ivl.getViewport().getMinY(),
                                                ivl.getViewport().getWidth() - 1,
                                                ivl.getViewport().getHeight() - 1));
                                    }
                                }
                            }
                        });
                        ivl.setOnMouseClicked(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(final MouseEvent event) {
                                if (event.getButton().equals(
                                        MouseButton.SECONDARY)) {
                                    ivl.setViewport(new Rectangle2D(ivl
                                            .getViewport().getMinX(), ivl
                                            .getViewport().getMinY(), ivl
                                            .getViewport().getWidth() + 1, ivl
                                            .getViewport().getHeight() + 1));
                                } else if (event.getButton().equals(
                                        MouseButton.PRIMARY)) {
                                    if (ivl.getViewport().getWidth() - 1 > 0
                                            && ivl.getViewport().getHeight() - 1 > 0
                                            && ivl.getViewport().getWidth() > g
                                                    .getImage().getWidth()
                                            && ivl.getViewport().getHeight() > g
                                                    .getImage().getHeight()) {
                                        ivl.setViewport(new Rectangle2D(
                                                ivl.getViewport().getMinX(),
                                                ivl.getViewport().getMinY(),
                                                ivl.getViewport().getWidth() - 1,
                                                ivl.getViewport().getHeight() - 1));
                                    }
                                }
                            }
                        });
                    } else if (event.getButton().equals(MouseButton.SECONDARY)) {
                        ivr.setImage(g.getImage());
                        GalleryDialog.this.highlights[1] = g;
                        if (GalleryDialog.this.highlights[0] == null) {
                            GalleryDialog.this.documentPanelController
                                    .highlightGlyphs(new Glyph[] { g });
                        } else {
                            GalleryDialog.this.documentPanelController
                                    .highlightGlyphs(highlights);
                        }
                        GalleryDialog.this.showIndex.set(-1);
                        GalleryDialog.this.showIndex.set(0);
                        ivr.setViewport(new Rectangle2D(0, 0, g.getImage()
                                .getWidth(), g.getImage().getHeight()));
                        ivr.setOnScroll(new EventHandler<ScrollEvent>() {

                            @Override
                            public void handle(final ScrollEvent event) {
                                if (event.getDeltaY() > 0) {
                                    ivr.setViewport(new Rectangle2D(ivr
                                            .getViewport().getMinX(), ivr
                                            .getViewport().getMinY(), ivr
                                            .getViewport().getWidth() + 1, ivr
                                            .getViewport().getHeight() + 1));
                                } else {
                                    if (ivr.getViewport().getWidth() - 1 > 0
                                            && ivr.getViewport().getHeight() - 1 > 0
                                            && ivr.getViewport().getWidth() > g
                                                    .getImage().getWidth()
                                            && ivr.getViewport().getHeight() > g
                                                    .getImage().getHeight()) {
                                        ivr.setViewport(new Rectangle2D(
                                                ivr.getViewport().getMinX(),
                                                ivr.getViewport().getMinY(),
                                                ivr.getViewport().getWidth() - 1,
                                                ivr.getViewport().getHeight() - 1));
                                    }
                                }
                            }
                        });
                        ivr.setOnMouseClicked(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(final MouseEvent event) {
                                if (event.getButton().equals(
                                        MouseButton.SECONDARY)) {
                                    ivr.setViewport(new Rectangle2D(ivr
                                            .getViewport().getMinX(), ivr
                                            .getViewport().getMinY(), ivr
                                            .getViewport().getWidth() + 1, ivr
                                            .getViewport().getHeight() + 1));
                                } else if (event.getButton().equals(
                                        MouseButton.PRIMARY)) {
                                    if (ivr.getViewport().getWidth() - 1 > 0
                                            && ivr.getViewport().getHeight() - 1 > 0
                                            && ivr.getViewport().getWidth() > g
                                                    .getImage().getWidth()
                                            && ivr.getViewport().getHeight() > g
                                                    .getImage().getHeight()) {
                                        ivr.setViewport(new Rectangle2D(
                                                ivr.getViewport().getMinX(),
                                                ivr.getViewport().getMinY(),
                                                ivr.getViewport().getWidth() - 1,
                                                ivr.getViewport().getHeight() - 1));
                                    }
                                }
                            }
                        });

                    }
                }
            });

            final Rectangle bounds = new Rectangle(iv.getLayoutX() - 1,
                    iv.getLayoutY() - 1, iv.getBoundsInLocal().getWidth() + 1,
                    iv.getBoundsInLocal().getHeight() + 1);
            bounds.setStyle("-fx-fill: transparent;" + "-fx-stroke: Black;"
                    + "-fx-stroke-width: 1;" + "-fx-stroke-type: outside;"
                    + "-fx-opacity: 0.5;");

            this.gallery.getChildren().add(bounds);
            this.gallery.getChildren().add(iv);

        }
    }

}
