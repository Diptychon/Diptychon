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

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import de.diptychon.DiptychonPreferences;

/**
 * This class is File Selector class which handles something like project name,
 * saving path and files.
 */
public class WizardFileSelector extends WizardDialog {

    /**
     * checkBox control which is a flag for finding text check.
     */
    @FXML
    public CheckBox findTextCheck;

    /**
     * RaidoButton control which means file selection
     */
    @FXML
    private RadioButton filesRadio;

    /**
     * RadioButton control which means directory selection
     */
    @FXML
    private RadioButton DirRadio;

    /**
     * Button control which is file selection.
     */
    @FXML
    private Button openFilesBtn;

    /**
     * Button control which is directory selection.
     */
    @FXML
    private Button openDirBtn;

    /**
     * TextField control which is for project name
     */
    @FXML
    private TextField projectNameEdt;

    /**
     * This member is a list to hold the files.
     */
    private List<File> fileList;

    /**
     * It is directory.
     */
    private File file;

    /**
     * It is the path for saving project.
     */
    private File savePath;

    /**
     * TextField control which is for saving path.
     */
    @FXML
    private TextField savedPathEdt;

    /**
     * Label control which is for error notification.
     */
    @FXML
    private Label hint_Label;

    @Override
    protected void apply(final ActionEvent event) {
        
    }

    @Override
    @FXML
    protected void cancel(final ActionEvent event) {
        this.setNextState(emDialogType.emCancel);
        this.hideDialog();
    }

    /**
     * This method is used to go to next step in wizard dialogs. But before
     * going to next session, the path of files must be checked.
     * 
     * @event ActionEvent
     */
    @Override
    @FXML
    protected void next(final ActionEvent event) {

        if (!this.projectNameEdt.getText().isEmpty()
                && !this.savedPathEdt.getText().isEmpty()
                && this.fileList != null && !this.fileList.isEmpty()) {
            this.hint_Label.setVisible(false);
            this.setNextState(this.getNextType());
            this.hideDialog();
        } else {
            this.hint_Label.setVisible(true);
        }
    }

    @Override
    protected void previous(final ActionEvent event) {
        
    }

    @Override
    protected void finish(final ActionEvent event) {
        
    }

    /**
     * This method is used to initialize file selector wizard dialog.
     */
    @Override
    public void initialize(final URL fxmlFileLocation,
            final ResourceBundle resources) {
        this.savedPathEdt.setText(DiptychonPreferences.getLastShownDirectory());
    }

    /**
     * This method is used to select files.
     * 
     * @param event
     *            ActionEvent
     */
    @FXML
    private void openFiles(final ActionEvent event) {
        final FileChooser fileChooser = new FileChooser();
        final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Image files (*.jpg, *.jpeg, *.tiff, *.tif, *.png, *.bmp)",
                "*.jpg", "*.jpeg", "*.tiff", "*.tif", "*.png", "*.bmp");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File(DiptychonPreferences
                .getLastShownDirectory()));

        this.fileList = fileChooser.showOpenMultipleDialog(this.root.getScene()
                .getWindow());
        DiptychonPreferences.updateLastShownDirectory(fileList);
    }

    /**
     * This method is used to select directory.
     * 
     * @param event
     *            ActionEvent
     */
    @FXML
    private void openDirectory(final ActionEvent event) {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(DiptychonPreferences
                .getLastShownDirectory()));
        this.file = directoryChooser.showDialog(null);
        DiptychonPreferences.updateLastShownDirectory(file);
    }

    /**
     * This method is used to enable file radio and disable directory radio.
     * 
     * @param event
     *            ActionEvent
     */
    @FXML
    private void clickFileRadio(final ActionEvent event) {
        this.openDirBtn.setDisable(true);
        this.openFilesBtn.setDisable(false);
    }

    /**
     * This method is used to enable directory radio and disable file radio.
     * 
     * @param event
     *            ActionEvent
     */
    @FXML
    private void clickDirRadio(final ActionEvent event) {
        this.openDirBtn.setDisable(false);
        this.openFilesBtn.setDisable(true);
    }

    /**
     * This method is used to get files from multi-files or directory.
     * 
     * @return array of files
     */
    public File[] getFiles() {
        File[] files = null;
        if (this.filesRadio.isSelected() && this.fileList != null) {
            files = new File[this.fileList.size()];
            files = this.fileList.toArray(files);
        } else if (this.DirRadio.isSelected() && this.file != null) {
            files = new File[1];
            files[0] = this.file;
        } else {
            files = null;
        }
        return files;
    }

    /**
     * This method is used to get project name.
     * 
     * @return project name
     */
    public String getProjectName() {
        return this.projectNameEdt.getText();
    }

    /**
     * This method is used to get the save path.
     * 
     * @param event
     *            ActionEvent
     */
    @FXML
    public void handleSavePath(final ActionEvent event) {
        final DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setInitialDirectory(new File(DiptychonPreferences
                .getLastShownDirectory()));
        this.savePath = dirChooser.showDialog(this.root.getScene().getWindow());
        DiptychonPreferences.updateLastShownDirectory(this.savePath);
        if (this.savePath != null) {
            this.savedPathEdt.clear();
            this.savedPathEdt.setText(this.savePath.getAbsolutePath());
        }
    }

    /**
     * This method is used to get save path.
     * 
     * @return save path
     */
    public String getSavedPath() {
        if (this.savePath == null) {
            return "";
        }
        return this.savePath.getAbsolutePath();
    }
}
