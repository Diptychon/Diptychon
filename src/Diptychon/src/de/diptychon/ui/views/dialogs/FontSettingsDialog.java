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
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.text.Font;

/**
 * The controller for the SettingsDialog
 */
public class FontSettingsDialog extends A_Dialog {

    /**
     * The default font
     */
    private static final String DEFAULT_FONT = "Junicode";

    /**
     * The default font size
     */
    private static final int DEFAULT_FONT_SIZE = 24;

    /**
     * The default square size
     */
    private static final int DEFAULT_SQUARE_SIZE = 2;

    /**
     * Shows all font families
     */
    @FXML
    private ComboBox<String> fontfamily;

    /**
     * Shows all allowed font sizes
     */
    @FXML
    private ComboBox<String> fontsize;

    /**
     * The font setting before it was changed
     */
    private String prevFont;

    /**
     * The font size setting before it was changed
     */
    private int prevSize;

    /**
     * the current font
     */
    private StringProperty font;

    /**
     * the current font size
     */
    private IntegerProperty size;

    @Override
    public void apply(final ActionEvent event) {
        this.font.set(this.fontfamily.getValue());
        this.size.set(Integer.valueOf(this.fontsize.getValue()));
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
        if (this.fontfamily.getItems().contains(DEFAULT_FONT)) {
            this.fontfamily.setValue(DEFAULT_FONT);
        } else {
            final ObservableList<String> items = this.fontfamily.getItems();
            if (items.size() >= 1) {
                this.fontfamily.setValue(items.get(0));
            }
        }
        this.fontsize.setValue(DEFAULT_FONT_SIZE + "");
    }

    @Override
    public void cancel(final ActionEvent event) {
        this.fontfamily.setValue(this.prevFont);
        this.fontsize.setValue(this.prevSize + "");
        this.closeDialog();
    }

    @Override
    public void initialize(final URL fxmlFileLocation,
            final ResourceBundle resources) {
        assert this.root != null : "fx:id=\"root\" was not injected: check your FXML file 'binarizeDialog.fxml'.";

        final List<String> fonts = Font.getFontNames();
        for (final String s : fonts) {
            // Check if font can be successfully loaded
            final Font newfont = Font.font(s, 24);
            if (newfont.getName().equals(s)) {
                this.fontfamily.getItems().add(s);
            }
        }

        if (this.fontfamily.getItems().contains("Junicode")) {
            this.fontfamily.setValue("Junicode");
        } else {
            this.fontfamily.setValue(Font.getDefault().getName());
        }
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
    public void initBoxes(final StringProperty fontProperty,
            final IntegerProperty sizeProperty) {
        if (this.fontfamily.getItems().contains(fontProperty.get())) {
            this.prevFont = fontProperty.get();
            this.fontfamily.setValue(fontProperty.get());
        } else {
            final ObservableList<String> items = this.fontfamily.getItems();
            if (items.size() >= 1) {
                this.fontfamily.setValue(items.get(0));
                this.prevFont = items.get(0);
            }

        }
        this.prevSize = sizeProperty.get();
        this.fontsize.setValue(sizeProperty.get() + "");
        this.font = fontProperty;
        this.size = sizeProperty;
    }

}
