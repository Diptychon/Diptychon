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

import de.diptychon.DiptychonLogger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

/**
 * Controller for the FindDialog
 */
public class ExportGlyphDialog extends A_Dialog {
    private static boolean lastSpecific = false;
    private static String lastGlyph = "";
    private static byte lastColordepth = 0;

    /**
     * Export only specific glyphs?
     */
    @FXML
    private CheckBox exportSpecific;

    /**
     * Prompt textfield to input the specific glyph string
     */
    @FXML
    private TextField searchField;

    @FXML
    private RadioButton greyscale;

    @FXML
    private RadioButton binary;

    /**
     * This is method is bound to the next button and shows the next result
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    public void preview(final ActionEvent event) {

    }

    /**
     * This is method is bound to the previous button and shows the previous
     * result
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @Override
    public void apply(final ActionEvent event) {
        saveSelections();
        
        // Get glyph if selected
        String glyph = null;
        if (lastSpecific) {
            glyph = lastGlyph;
        }
        
        DiptychonLogger.debug("Export glyphs [specific: {} {}] with depth {}", lastSpecific, lastGlyph, lastColordepth);
        
        // Run specific method
        if (lastColordepth == 0) {
            this.documentPanelController.exportGlyphsGrayscale(glyph);
        } else {
            this.documentPanelController.exportGlyphsBinary(glyph);
        }
        this.closeDialog();
    }

    @Override
    public void cancel(final ActionEvent event) {
        saveSelections();
        this.closeDialog();
    }

    private void saveSelections() {
        lastSpecific = exportSpecific.isSelected();
        lastGlyph = searchField.getText();
        lastColordepth = greyscale.isSelected() ? (byte) 0 : (byte) 1;
    }

    @Override
    public void initialize(final URL fxmlFileLocation,
            final ResourceBundle resources) {
        assert this.root != null : "fx:id=\"root\" was not injected: check your FXML file 'ExportGlyphDialog.fxml'.";
        assert this.exportSpecific != null : "fx:id=\"exportSpecific\" was not injected: check your FXML file 'ExportGlyphDialog.fxml'.";
        assert this.searchField != null : "fx:id=\"searchField\" was not injected: check your FXML file 'ExportGlyphDialog.fxml'.";
        assert this.greyscale != null : "fx:id=\"greyscale\" was not injected: check your FXML file 'ExportGlyphDialog.fxml'.";
        assert this.binary != null : "fx:id=\"binary\" was not injected: check your FXML file 'ExportGlyphDialog.fxml'.";

        setSpecificExport(lastSpecific);
        searchField.setText(lastGlyph);
        if (lastColordepth == 0) {
            greyscale.setSelected(true);
        } else {
            binary.setSelected(true);
        }
    }

    @FXML
    public void exportSpecificChanged(final ActionEvent event) {
        setSpecificExport(exportSpecific.isSelected());
    }

    private void setSpecificExport(boolean specific) {
        exportSpecific.setSelected(specific);
        searchField.setDisable(!specific);
    }
}
