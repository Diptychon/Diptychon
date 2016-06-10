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

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import de.diptychon.DiptychonLogger;

/**
 * Controller for the FindDialog
 */
public class FindDialog extends A_Dialog {

    /**
     * Search case sensitive?
     */
    @FXML
    private CheckBox caseSensitive;

    /**
     * Index of the currently shown search result
     */
    private IntegerProperty showIndex;

    /**
     * Number of search results
     */
    private IntegerProperty numOfResults;

    /**
     * Prompt textfield to input the string to search for
     */
    @FXML
    private TextField searchField;

    /**
     * Showing the index of the currently shown result and the number of results
     */
    @FXML
    private Label resultsLabel;

    /**
     * This is method is bound to the next button and shows the next result
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    public void preview(final ActionEvent event) {
        if (this.searchField.getLength() == 0) {
            this.showIndex.set(-3);
            this.resultsLabel.setText("No results found!");
            return;
        }
        if (this.showIndex.get() < this.numOfResults.get() - 1) {
            this.showIndex.set(this.showIndex.get() + 1);
        } else {
            this.showIndex.set(0);
        }
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
        if (this.searchField.getLength() == 0) {
            this.showIndex.set(-3);
            this.resultsLabel.setText("No results found!");
            return;
        }
        if (this.showIndex.get() > 0) {
            this.showIndex.set(this.showIndex.get() - 1);
        } else {
            this.showIndex.set(this.numOfResults.get() - 1);
        }
    }

    @Override
    public void cancel(final ActionEvent event) {
        this.showIndex.set(-2);
        this.closeDialog();
    }

    /**
     * Handles the input in the search textfield
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    public void handleInput(final KeyEvent event) {
        final KeyCombination combo = new KeyCodeCombination(KeyCode.ENTER);
        if (combo.match(event)) {
            return;
        } else {
            this.handleSearch();
        }
    }

    /**
     * Handles the search
     */
    private void handleSearch() {
        if (this.searchField.getLength() == 0) {
            this.showIndex.set(-3);
            this.resultsLabel.setText("No results found!");
            return;
        }
        this.showIndex.set(-3);
        if (this.caseSensitive.isSelected()) {
            final String searchFor = this.searchField.getText();
            DiptychonLogger.debug("Searching for \"{}\", case sensitive: true",
                    searchFor);
            this.documentPanelController.searchForWord(searchFor, true);
        } else {
            final String searchFor = this.searchField.getText();
            DiptychonLogger.debug("Searching for {}, case sensitive: false",
                    searchFor);
            this.documentPanelController.searchForWord(searchFor, false);
        }
    }

    @Override
    public void initialize(final URL fxmlFileLocation,
            final ResourceBundle resources) {
        assert this.root != null : "fx:id=\"root\" was not injected: check your FXML file 'binarizeDialog.fxml'.";

        this.showIndex = new SimpleIntegerProperty(-2);
        this.showIndex.addListener(new InvalidationListener() {

            @Override
            public void invalidated(final Observable observable) {
                if (FindDialog.this.numOfResults.get() != 0) {
                    FindDialog.this.resultsLabel.setText("Result "
                            + (FindDialog.this.showIndex.get() + 1) + "/"
                            + FindDialog.this.numOfResults.get());
                }
            }
        });

        this.numOfResults = new SimpleIntegerProperty(-1);
        this.numOfResults.addListener(new InvalidationListener() {

            @Override
            public void invalidated(final Observable observable) {
                if (FindDialog.this.numOfResults.get() == 0) {
                    FindDialog.this.resultsLabel.setText("No results found!");
                    FindDialog.this.showIndex.set(-3);
                }
            }
        });
    }

    /**
     * Binds the num of results to a property (up to now this property comes
     * from the DocumentPanel)
     * 
     * @param bindWith
     *            the property to bind
     */
    public void bindNumOfResults(final IntegerProperty bindWith) {
        this.numOfResults.bind(bindWith);
    }

    /**
     * Gets the property which save the index of the currently shown search
     * result
     * 
     * @return the property
     */
    public IntegerProperty getShowIndexProperty() {
        return this.showIndex;
    }

    /**
     * This method is called when the caseSensitiveSearch box was changed.
     * Search has to be redone
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void caseSensitiveChanged(final ActionEvent event) {
        this.handleSearch();
    }

}
