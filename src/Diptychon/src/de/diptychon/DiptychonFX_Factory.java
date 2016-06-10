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
package de.diptychon;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import de.diptychon.controller.DocumentPanelController;
import de.diptychon.models.data.Digital;
import de.diptychon.ui.MainFrame;
import de.diptychon.ui.views.panels.DocumentPanel;

/**
 * This class is used to initialize the UI and controller
 */
public class DiptychonFX_Factory {
    /**
     * This method is used to initialize the main frame by loading the fxml
     * file.
     * 
     * @return the main frame
     */
    public MainFrame initDiptychon() {
        MainFrame mainFrame = null;
        final FXMLLoader loader = new FXMLLoader();
        try {
            loader.setLocation(new URL("file:/"));
            loader.load(this.getClass().getResourceAsStream(
                    "/fxml/MainFrame.fxml"));
        } catch (final IOException e) {
            DiptychonLogger.error("Unable to load MainFrame.fxml");
            DiptychonLogger.error("{}", e);
        }
        DiptychonLogger.info("Load file {}", loader.getLocation().toString()
                + "/fxml/MainFrame.fxml");
        mainFrame = (MainFrame) loader.getController();

        final DocumentPanel documentPanel = this.getDocumentImagePanel();

        ((BorderPane) mainFrame.getView()).setCenter(documentPanel.getView());

        this.initController(mainFrame, documentPanel);

        return mainFrame;
    }

    /**
     * Creates and gets the DocumentPanel
     * 
     * @return The DocumentPanel
     */
    private DocumentPanel getDocumentImagePanel() {
        DocumentPanel documentPanel = null;
        try {
            final FXMLLoader loader = new FXMLLoader();
            loader.setLocation(new URL("file:/"));
            loader.load(this.getClass().getResourceAsStream(
                    "/fxml/DocumentPanel.fxml"));
            DiptychonLogger.info("Load file {}", loader.getLocation()
                    .toString() + "/fxml/DocumentPanel.fxml");
            documentPanel = (DocumentPanel) loader.getController();
        } catch (final IOException e) {
            DiptychonLogger.error("Unable to load DocumentPanel.fxml");
            DiptychonLogger.error("{}", e);
        }
        return documentPanel;
    }

    /**
     * This method is used to initialize the controller which means that all
     * views and models are registered within the main controller
     * 
     * @param pMainFrame
     *            the mainframe object
     * @param pDocumentPanel
     *            document panel object
     */
    private void initController(final MainFrame pMainFrame,
            final DocumentPanel pDocumentPanel) {
        DiptychonLogger.info("Init controllers");
        final DocumentPanelController documentPanelController = new DocumentPanelController();
        final Digital digital = new Digital();
        documentPanelController.registerModel(digital);
        documentPanelController.registerModel(digital.getTranscript());
        documentPanelController.registerView(pMainFrame);
        documentPanelController.registerView(pDocumentPanel);
        pDocumentPanel.setController(documentPanelController);
        pMainFrame.setController(documentPanelController);
        pMainFrame.bindProperties(pDocumentPanel.getScaleProperty(),
                pDocumentPanel.getShowGlyphsProperty(),
                pDocumentPanel.getShowLinesProperty(),
                pDocumentPanel.getFontsizeProperty(),
                pDocumentPanel.getFontfamilyProperty(),
                pDocumentPanel.getFindWordProperty(),
                pDocumentPanel.getSyncScrollProperty(),
                pDocumentPanel.getSquareSizeProperty(),
                pDocumentPanel.getSplitLineProperty(),
                pDocumentPanel.getFragSizeProperty(),
                pDocumentPanel.getFragCountProperty(),
                pDocumentPanel.getNoiseSizeProperty(),
                pDocumentPanel.getLineExtTopProperty(),
                pDocumentPanel.getLineExtBottomProperty(),
                pDocumentPanel.getOnlyFocusedProperty());// ,
        // pDocumentPanel.getCCThresholdProperty());
    }
}
