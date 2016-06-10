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
package de.diptychon.ui;

import java.beans.PropertyChangeEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import de.diptychon.DiptychonLogger;
import de.diptychon.DiptychonPreferences;
import de.diptychon.controller.DocumentPanelController;
import de.diptychon.models.data.Digital;
import de.diptychon.ui.views.AbstractView;
import de.diptychon.ui.views.dialogs.A_Dialog;
import de.diptychon.ui.views.dialogs.ConfirmationQuitDialog;
import de.diptychon.ui.views.dialogs.FontSettingsDialog;
import de.diptychon.ui.views.dialogs.FragSettingsDialog;
import de.diptychon.ui.views.dialogs.InformationDialog;
import de.diptychon.ui.views.dialogs.LineSettingsDialog;
import de.diptychon.ui.views.dialogs.SearchEngineDialog;
import de.diptychon.ui.views.dialogs.WizardDialogManager;

/**
 * The MainFrame of the user interace. Includes the menubar, toolbar, ...
 * <p>
 * Implements the Abstract Class {@link de.diptychon.ui.views.AbstractView
 * AbstractView} which enables it react on changes within the models
 */
public class MainFrame extends AbstractView implements Initializable {

    /**
     * The Prefix used in the title bar
     */
    private static final String TITLE_PREFIX = "Diptychon     ";

    /**
     * UI node
     */
    @FXML
    private TextField curPage;

    /**
     * Reference to the documentpanelcontroller
     */
    private DocumentPanelController documentPanelController;

    /**
     * Property which is <code>true</code> if the documentPanel should
     * initialize the word search, <code>false</code> otherwise
     */
    private BooleanProperty findWord;

    /**
     * UI node
     */
    @FXML
    private MenuItem miFind;

    /**
     * UI node
     */
    @FXML
    private MenuItem miSave;

    /**
     * UI node
     */
    @FXML
    private MenuItem miSaveAs;

    /**
     * UI node
     */
    @FXML
    private Button newProject;

    /**
     * UI node
     */
    @FXML
    private Button next;

    /**
     * UI node
     */
    @FXML
    private Label numOfPages;

    /**
     * UI node
     */
    @FXML
    private Button open;

    /**
     * UI node
     */
    @FXML
    private Menu openRecent;

    /**
     * UI node
     */
    @FXML
    private Button prev;

    /**
     * UI node
     */
    @FXML
    private Button zoomFit;

    /**
     * UI node
     */
    @FXML
    private Parent root;

    /**
     * UI node
     */
    @FXML
    private Button save;

    /**
     * UI node
     */
    @FXML
    private Button saveAs;

    /**
     * UI node
     */
    @FXML
    private ToggleButton showBinarizedImage;

    /**
     * UI node
     */
    @FXML
    private ToolBar toolbar;

    /**
     * UI node
     */
    @FXML
    private ProgressIndicator progressIndicator;

    /**
     * UI node
     */
    @FXML
    private ToggleButton showGlyphs;

    /**
     * UI node
     */
    @FXML
    private ToggleButton showLines;

    /**
     * UI node
     */
    @FXML
    private ToggleButton syncScroll;

    /**
     * UI node
     */
    @FXML
    private ToggleButton onlyFocused;

    /**
     * The title property
     */
    private StringProperty title;

    /**
     * UI node
     */
    public Region spacer;

    /**
     * UI node
     */
    public TextField rgfthres;

    /**
     * UI node
     */
    public TextField fragthres;

    /**
     * The current fontsize
     */
    private IntegerProperty fontSize;

    private IntegerProperty squareSize;

    private IntegerProperty lineExtTop;

    private IntegerProperty lineExtBottom;

    private BooleanProperty splitLine;

    private IntegerProperty fragSize;

    private IntegerProperty noiseSize;

    private FloatProperty fragCount;

    // private FloatProperty ccThreshold;

    /**
     * The current fontfamily
     */
    private StringProperty fontFamily;

    /**
     * UI node
     */
    @FXML
    private ImageView zoomDecrease;

    /**
     * UI node
     */
    @FXML
    private ImageView zoomIncrease;

    /**
     * UI node
     */
    @FXML
    private Slider zoomSlider;

    /**
     * This method binds properties to properties of other views, bars dialog
     * etc to simplify the communication between different UI controls
     * 
     * @param scale
     *            the scale integer property of the DocumentPanel
     * @param pShowGlyphs
     *            a boolean property which is true if glyphs should be
     *            displayed, false otherwise
     * @param pShowLines
     *            a boolean property which is true if lines should be displayed,
     *            false otherwise
     * @param fontsize
     *            the fontsize integer property of the DocumentPanel
     * @param pFontfamily
     *            the fontFamily string property of the DocumentPanel
     * @param findWordProperty
     *            a boolean property which is true if lines should be displayed
     *            and false otherwise
     * @param pSyncScroll
     *            a boolean property which is true if the scrollbars of the
     *            image view and the transcript view should be synchronized,
     *            false otherwise
     */
    public void bindProperties(final IntegerProperty scale,
            final BooleanProperty pShowGlyphs,
            final BooleanProperty pShowLines, final IntegerProperty fontsize,
            final StringProperty pFontfamily,
            final BooleanProperty findWordProperty,
            final BooleanProperty pSyncScroll,
            final IntegerProperty pSquareSize,
            final BooleanProperty pSplitLine, final IntegerProperty pFragSize,
            final FloatProperty pFragCount, final IntegerProperty pNoiseSize,
            final IntegerProperty pLineExtTop,
            final IntegerProperty pLineExtBottom,
            final BooleanProperty pOnlyFocused)
            // , final FloatProperty  pCCThreshold)
    {
        this.zoomSlider.valueProperty().bindBidirectional(scale);
        this.showGlyphs.selectedProperty().bindBidirectional(pShowGlyphs);
        this.showLines.selectedProperty().bindBidirectional(pShowLines);
        this.syncScroll.selectedProperty().bindBidirectional(pSyncScroll);
        this.onlyFocused.selectedProperty().bindBidirectional(pOnlyFocused);
        this.findWord.addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                if (MainFrame.this.findWord.get()) {
                    // changing this value forces the DocumentPanel to open the
                    // FindWordDialog
                    findWordProperty.set(true);
                    MainFrame.this.findWord.set(false);
                }
            }
        });
        this.fontSize.addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                fontsize.set(MainFrame.this.fontSize.getValue());
            }
        });
        this.fontFamily.addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                pFontfamily.set(MainFrame.this.fontFamily.getValue());
            }
        });
        this.squareSize.addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                pSquareSize.set(MainFrame.this.squareSize.getValue());
            }
        });
        this.splitLine.addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                pSplitLine.set(MainFrame.this.splitLine.getValue());
            }
        });
        this.fragSize.addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                pFragSize.set(MainFrame.this.fragSize.getValue());
            }
        });
        this.noiseSize.addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                pNoiseSize.set(MainFrame.this.noiseSize.getValue());
            }
        });
        this.fragCount.addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                pFragCount.set(MainFrame.this.fragCount.getValue());
            }
        });
        this.lineExtTop.addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                pLineExtTop.set(MainFrame.this.lineExtTop.getValue());
            }
        });
        this.lineExtBottom.addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                pLineExtBottom.set(MainFrame.this.lineExtBottom.getValue());
            }
        });
        // ccThreshold.addListener(new InvalidationListener()
        // {
        // @Override
        // public void invalidated(final Observable observable)
        // {
        // pCCThreshold.set(MainFrame.this.ccThreshold.getValue());
        // }
        // });
    }

    /**
     * Binds the titleProperty of the stage (only accessible in
     * DiptychonFX.java) to the path of the current file
     * 
     * @param titleProperty
     *            the titleProperty of the stage of diptychon
     */
    public void bindTitleProperty(final StringProperty titleProperty) {
        titleProperty.bind(this.title);
    }

    /**
     * Creates a FileSaveDialog with given file extension filters
     * 
     * @param extFilterDescription
     *            The description of the file extension (e.g.
     *            <code>Portable Network Graphic *.png</code>)
     * @param sExtFilter
     *            The extension filter
     * @param extension
     *            The file extension, which is necessary to be able to check
     *            whether the user input a filename with or without extension
     * @return the chosen file
     */
    private String fileSaveDialog(final String extFilterDescription,
            final String sExtFilter, final String extension) {
        final FileChooser fileChooser = new FileChooser();
        final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                extFilterDescription, sExtFilter);
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File(DiptychonPreferences
                .getLastShownDirectory()));

        final File file = fileChooser.showSaveDialog(this.root.getScene()
                .getWindow());
        DiptychonPreferences.updateLastShownDirectory(file);

        if (file != null) {
            String filename = file.getAbsolutePath();
            // if the filename is without extension, is has to be added
            if (!filename.endsWith(extension)) {
                filename += extension;
            }
            return filename;
        }
        return null;
    }

    /**
     * Gets the Parent Node of this Panel. Necessary to be able to include this
     * panel into the stage.
     * 
     * @return The Parent Node of this panel.
     */
    public Parent getView() {
        return this.root;
    }

    /**
     * Opens the About Information Dialog
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleAboutAction(final ActionEvent event) {
        DiptychonLogger.info("About clicked");
        final String txt = "The Diptychon System, Version 20130618\n\n"
                +

                "Diptychon is funded by the DFG,\nDeutsche Forschungsgemeinschaft\n"
                + "Reference numbers GO 2023/4-1 and LA 3007/1-1\n\n\n"
                +

                "Cooperating institutions\n\n"
                +

                "Universität Bremen\n"
                + "Fachbereich Mathematik und Informatik\n"
                + "Technologie-Zentrum Informatik (TZI)\n\n"
                +

                "Berlin-Brandenburgische Akademie der Wissenschaften\n"
                + "Monumenta Germaniae Historica\n\n"
                +

                "Humboldt-Universität zu Berlin\n"
                + "Institut für Geschichtswissenschaften\n"
                + "Lehrstuhl für Mittelalterliche Geschichte und Landesgeschichte\n";

        final InformationDialog id = new InformationDialog.Factory()
                .createDialogWithoutExclamationMark(txt, Pos.TOP_LEFT, 400);
        id.showDialog(this.root.getScene().getWindow(), -1, -1);
    }

    @FXML
    private void handleExportStatistic(final ActionEvent event) {
        this.documentPanelController.exportStatistic();
    }

    @FXML
    private void handleExportGlyphsBinary(final ActionEvent event) {
        this.documentPanelController.exportGlyphsBinary();
    }

    @FXML
    private void handleExportGlyphsGrayscale(final ActionEvent event) {
        this.documentPanelController.exportGlyphsGrayscale();
    }

    @FXML
    private void handleMergeStatistics(final ActionEvent event)
            throws IOException {
        final FileChooser fileChooser = new FileChooser();
        final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Textfiles (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File(DiptychonPreferences
                .getLastShownDirectory()));

        final List<File> file = fileChooser.showOpenMultipleDialog(this.root
                .getScene().getWindow());
        DiptychonPreferences.updateLastShownDirectory(file);

        if (file != null && file.size() > 1) {
            DiptychonLogger.info("Merging Stastistics Files...");
            File[] files = new File[file.size()];
            files = file.toArray(files);

            ArrayList<Integer> TTotal = new ArrayList<Integer>();
            ArrayList<Float> TMSpace = new ArrayList<Float>();
            ArrayList<Float> TWidth = new ArrayList<Float>();
            ArrayList<Float> THeight = new ArrayList<Float>();
            ArrayList<Float> TMSDBL = new ArrayList<Float>();
            ArrayList<Float> TMSDBR = new ArrayList<Float>();
            ArrayList<Float> TMChars = new ArrayList<Float>();
            ArrayList<Float> TMSlant = new ArrayList<Float>();
            ArrayList<Integer> TSDir = new ArrayList<Integer>();
            ArrayList<Integer> WTotal = new ArrayList<Integer>();
            ArrayList<Float> WMSpace = new ArrayList<Float>();
            ArrayList<Float> WMChars = new ArrayList<Float>();

            ArrayList<Integer> ATotal = new ArrayList<Integer>();
            ArrayList<Integer> AIsolated = new ArrayList<Integer>();
            ArrayList<Integer> AWEnding = new ArrayList<Integer>();
            ArrayList<Integer> AWStarting = new ArrayList<Integer>();
            ArrayList<Integer> AWContaining = new ArrayList<Integer>();

            ArrayList<Integer> GTotal = new ArrayList<Integer>();
            ArrayList<String> glyphs = new ArrayList<String>();
            ArrayList<ArrayList<String[]>> GAttributes = new ArrayList<ArrayList<String[]>>();

            int TTotalNew = 0;
            float TMSpaceNew = 0;
            float TWidthNew = 0;
            float THeightNew = 0;
            float TMSDBLNew = 0;
            float TMSDBRNew = 0;
            float TMCharsNew = 0;
            float TMSlantNew = 0;
            int TSDirNew = 0;

            int WTotalNew = 0;
            float WMSpaceNew = 0;
            float WMCharsNew = 0;

            int ATotalNew = 0;
            int AIsolatedNew = 0;
            int AWEndingNew = 0;
            int AWStartingNew = 0;
            int AWContainingNew = 0;

            int GTotalNew = 0;
            ArrayList<Float[]> GAttributesNew = new ArrayList<Float[]>();

            String line;

            for (int i = 0; i < files.length; i++) {
                FileReader fr = new FileReader(files[i]);
                BufferedReader br = new BufferedReader(fr);

                line = br.readLine();
                if (line.startsWith("Merged")) {
                    br.readLine();
                    line = br.readLine();
                }
                TTotal.add(Integer.parseInt(line.substring(line.indexOf(":") + 2)));
                line = br.readLine();
                TMSpace.add(Float.valueOf(line.substring(line.indexOf(":") + 2)));
                line = br.readLine();
                TWidth.add(Float.valueOf(line.substring(line.indexOf(":") + 2)));
                line = br.readLine();
                THeight.add(Float.valueOf(line.substring(line.indexOf(":") + 2)));
                line = br.readLine();
                TMSDBL.add(Float.valueOf(line.substring(line.indexOf(":") + 2)));
                line = br.readLine();
                TMSDBR.add(Float.valueOf(line.substring(line.indexOf(":") + 2)));
                line = br.readLine();
                TMChars.add(Float.valueOf(line.substring(line.indexOf(":") + 2)));
                line = br.readLine();
                TMSlant.add(Float.valueOf(line.substring(line.indexOf(":") + 2)));
                line = br.readLine();
                TSDir.add(Integer.parseInt(line.substring(line.indexOf("|") + 2)));

                br.readLine();

                line = br.readLine();
                WTotal.add(Integer.parseInt(line.substring(line.indexOf(":") + 2)));
                line = br.readLine();
                WMSpace.add(Float.valueOf(line.substring(line.indexOf(":") + 2)));
                line = br.readLine();
                WMChars.add(Float.valueOf(line.substring(line.indexOf(":") + 2)));

                br.readLine();

                line = br.readLine();
                ATotal.add(Integer.parseInt(line.substring(line.indexOf(":") + 2)));
                line = br.readLine();
                AIsolated
                        .add(Integer.parseInt(line.substring(line.indexOf(":") + 2)));
                line = br.readLine();
                AWEnding.add(Integer.parseInt(line.substring(line.indexOf(":") + 2)));
                line = br.readLine();
                AWStarting.add(Integer.parseInt(line.substring(line
                        .indexOf(":") + 2)));
                line = br.readLine();
                AWContaining.add(Integer.parseInt(line.substring(line
                        .indexOf(":") + 2)));

                br.readLine();

                line = br.readLine();
                GTotal.add(Integer.parseInt(line.substring(line.indexOf(":") + 2)));
                br.readLine();
                line = br.readLine();

                while (line != null) {
                    String[] values = line.split(" \\| ");
                    if (Collections.frequency(glyphs, values[0]) > 0) {
                        GAttributes.get(glyphs.indexOf(values[0])).add(values);
                    } else {
                        glyphs.add(values[0]);
                        ArrayList<String[]> vals = new ArrayList<String[]>();
                        vals.add(values);
                        GAttributes.add(vals);
                    }
                    line = br.readLine();
                }
                br.close();

            }

            for (int i = 0; i < files.length; i++) {
                TTotalNew += TTotal.get(i);
                WTotalNew += WTotal.get(i);
                ATotalNew += ATotal.get(i);
                AIsolatedNew += AIsolated.get(i);
                AWEndingNew += AWEnding.get(i);
                AWStartingNew += AWStarting.get(i);
                AWContainingNew += AWContaining.get(i);
                GTotalNew += GTotal.get(i);
                TSDirNew += TSDir.get(i);
            }
            for (int i = 0; i < files.length; i++) {
                float factor = TTotal.get(i) / (float) TTotalNew;
                TMSpaceNew += factor * TMSpace.get(i);
                TWidthNew += factor * TWidth.get(i);
                THeightNew += factor * THeight.get(i);
                TMSDBLNew += factor * TMSDBL.get(i);
                TMSDBRNew += factor * TMSDBR.get(i);
                TMCharsNew += factor * TMChars.get(i);
                TMSlantNew += factor * TMSlant.get(i);

                factor = WTotal.get(i) / (float) WTotalNew;
                WMSpaceNew += factor * WMSpace.get(i);
                WMCharsNew += factor * WMChars.get(i);
            }
            for (int i = 0; i < glyphs.size(); i++) {
                Float[] values = new Float[] { (float) 0, (float) 0, (float) 0,
                        (float) 0 };
                for (int j = 0; j < GAttributes.get(i).size(); j++) {
                    values[3] += Float.valueOf(GAttributes.get(i).get(j)[4]);
                }
                for (int j = 0; j < GAttributes.get(i).size(); j++) {
                    float factor = (Float.valueOf(GAttributes.get(i).get(j)[4]) / values[3]);
                    values[0] += factor
                            * Float.valueOf(GAttributes.get(i).get(j)[1]);
                    values[1] += factor
                            * Float.valueOf(GAttributes.get(i).get(j)[2]);
                    values[2] += factor
                            * Float.valueOf(GAttributes.get(i).get(j)[3]);
                }
                GAttributesNew.add(values);
            }

            try {
                String filename = file.get(0).getParent() + File.separator
                        + "Merged Statistics";
                String newFilename = "";
                File f = new File(filename + ".txt");
                int postfix = 1;

                while (f.exists()) {
                    postfix++;
                    newFilename = filename + " " + postfix;
                    f = new File(newFilename + ".txt");
                }

                if (!newFilename.equals("")) {
                    filename = newFilename;
                }

                PrintWriter out = new PrintWriter(filename + ".txt");

                out.print("Merged Statistics: " + files[0].getName());
                for (int i = 1; i < files.length; i++) {
                    out.print(", " + files[i].getName());
                }
                out.println();
                out.println();
                out.println("Total textlines: " + TTotalNew);
                out.println("Mean space between textlines: " + TMSpaceNew);
                out.println("Mean textline width" + ": " + TWidthNew);
                out.println("Mean textline height" + ": " + THeightNew);
                out.println("Mean space between textline and document border left"
                        + ": " + TMSDBLNew);
                out.println("Mean space between textline and document border right"
                        + ": " + TMSDBRNew);
                out.println("Mean amount of characters in textline" + ": "
                        + TMCharsNew);
                out.println("Mean textline slant" + ": " + TMSlantNew);
                if (TSDirNew > 0) {
                    out.println("Dominating slant direction: Ascending right | "
                            + TSDirNew);
                } else if (TSDirNew < 0) {
                    out.println("Dominating slant direction: Falling right | "
                            + TSDirNew);
                } else {
                    out.println("Dominating slant direction: Balanced | "
                            + TSDirNew);
                }

                out.println();

                out.println("Total words: " + WTotalNew);
                out.println("Mean space between words: " + WMSpaceNew);
                out.println("Mean amount of characters in word: " + WMCharsNew);

                out.println();

                out.println("Total abbreviations: " + ATotalNew);
                out.println("Isolated abbreviations: " + AIsolatedNew);
                out.println("Words ending with abbreviation: " + AWEndingNew);
                out.println("Words starting with abbreviation: "
                        + AWStartingNew);
                out.println("Words containing abbreviation: " + AWContainingNew);

                out.println();

                out.println("Total glyphs: " + GTotalNew);
                out.println("Character | Width | Height | Area | Frequency");
                for (int i = 0; i < glyphs.size(); i++) {
                    out.println(glyphs.get(i) + " | "
                            + GAttributesNew.get(i)[0] + " | "
                            + GAttributesNew.get(i)[1] + " | "
                            + GAttributesNew.get(i)[2] + " | "
                            + (int) (float) GAttributesNew.get(i)[3]);
                }

                out.close();
            } catch (FileNotFoundException e) {
                DiptychonLogger.error("{}", e);
            }

        }
    }

    /**
     * Invoked when the user pressed <code>next</code>
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleBrowseNext(final ActionEvent event) {
        DiptychonLogger.info("Browse Next Page.");
        this.documentPanelController.browseImages(true);
    }

    /**
     * Invoked when the user pressed <code>previous</code>
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleBrowsePrevious(final ActionEvent event) {
        DiptychonLogger.info("Browse Previous Page.");
        this.documentPanelController.browseImages(false);
    }

    /**
     * Invoked when the user pressed <code>exit</code>
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    public void handleExitAction(final ActionEvent event) {
        DiptychonLogger.info("Exit the application.");
        this.documentPanelController.cancelTimer();
        if (!this.save.isDisabled()) {
            final ConfirmationQuitDialog cd = (ConfirmationQuitDialog) new ConfirmationQuitDialog.Factory()
                    .createDialog("ConfirmationQuitDialog",
                            this.documentPanelController);
            final BooleanProperty exit = new SimpleBooleanProperty();
            exit.bindBidirectional(cd.getExitProperty());
            exit.addListener(new InvalidationListener() {
                @Override
                public void invalidated(final Observable observable) {
                    if (exit.get()) {
                        Platform.exit();
                    }
                }
            });
            cd.showModalDialog(this.root.getScene().getWindow(), -1, -1);
        } else {
            Platform.exit();
        }
    }

    /**
     * Invoked when the user pressed <code>Export Binary</code>
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleExportBinaryImage(final ActionEvent event) {
        System.out.println(this.title.get());
        final String filename = this.fileSaveDialog("Image files (*.png)",
                "*.png", ".png");
        if (filename != null) {
            this.documentPanelController.exportGrayscaleImage(filename);
        }
    }

    /**
     * Invoked when the user pressed <code>Export Greyscale</code>
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleExportGrayScale(final ActionEvent event) {
        final String filename = this.fileSaveDialog("Image files (*.png)",
                "*.png", ".png");
        if (filename != null) {
            this.documentPanelController.exportBinaryImage(filename);
        }
    }

    /**
     * Invoked when the user pressed <code>Export PDF</code>
     *
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleExportPDFwithparentheses(final ActionEvent event) {
        System.out.println(this.title.get());
        final String filename = this.fileSaveDialog("PDF files (*.pdf)",
                "*.pdf", ".pdf");
        if (filename != null) {
            this.documentPanelController.exportPDFwithparentheses(filename);
        }
    }

    /**
     * Invoked when the user pressed <code>Export PDF</code>
     *
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleExportPDFwithoutparentheses(final ActionEvent event) {
        System.out.println(this.title.get());
        final String filename = this.fileSaveDialog("PDF files (*.pdf)",
                "*.pdf", ".pdf");
        if (filename != null) {
            this.documentPanelController.exportPDFwithoutparentheses(filename);
        }
    }

    /**
     * Invoked when the user pressed <code>Export as plain text</code>
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleExportTranscriptPlainText(final ActionEvent event) {
        final String filename = this.fileSaveDialog("Plain Text(*.txt)",
                "*.txt", ".txt");
        if (filename != null) {
            this.documentPanelController.exportAsPlainText(filename);
        }
    }

    /**
     * Invoked when the user pressed <code>Export as rtf</code>
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleExportTranscriptRTF(final ActionEvent event) {
        final String filename = this.fileSaveDialog("Rich Text Format (*.rtf)",
                "*.rtf", ".rtf");
        if (filename != null) {
            this.documentPanelController.exportAsRichTextFormat(filename,
                    this.fontFamily.getValue());
        }
    }

    @FXML
    private void handleExportTranscriptTEI(final ActionEvent event) {
        final String filename = this.fileSaveDialog(
                "Text Encoding Initiative (*.xml)", "*.xml", ".xml");
        if (filename != null) {
            this.documentPanelController
                    .exportAsTEI(filename, this.title.get());
        }
    }

    /**
     * Invoked when the user pressed <code>Find</code>
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleFind(final ActionEvent event) {
        this.findWord.set(true);
    }

    /**
     * Invoked when the user pressed <code>New Project</code>
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleNewProjectAction(final ActionEvent event) {
        final FileChooser fileChooser = new FileChooser();
        final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Image files (*.jpg, *.jpeg, *.tiff, *.tif, *.png, *.bmp)",
                "*.jpg", "*.jpeg", "*.tiff", "*.tif", "*.png", "*.bmp");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File(DiptychonPreferences
                .getLastShownDirectory()));

        final List<File> file = fileChooser.showOpenMultipleDialog(this.root
                .getScene().getWindow());
        DiptychonPreferences.updateLastShownDirectory(file);

        if (file != null) {
            this.resetStatus();
            DiptychonLogger.info("Creating new Project...");
            File[] files = new File[file.size()];
            files = file.toArray(files);
            this.documentPanelController.initDigital(files);
            this.save.setDisable(false);
            this.miSave.setDisable(false);
            this.miFind.setDisable(false);
        }
    }

    /**
     * Invoked when the user pressed <code>Open</code>
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleOpenAction(final ActionEvent event) {
        final FileChooser fileChooser = new FileChooser();
        final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Diptychon Save File", "*.dsf");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File(DiptychonPreferences
                .getLastShownDirectory()));

        final File file = fileChooser.showOpenDialog(this.root.getScene()
                .getWindow());
        DiptychonPreferences.updateLastShownDirectory(file);

        if (file != null && file.exists()) {
            DiptychonLogger.info("Loading Project {}", file.getAbsolutePath());
            this.documentPanelController.openProject(file.getAbsolutePath());
            this.save.setDisable(true);
            this.miSave.setDisable(true);
            this.miFind.setDisable(false);
        }
    }

    /**
     * Invoked when the user enters a page number which he wants to see
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handlePageInput(final ActionEvent event) {
        try {
            final int pageNumber = Integer.valueOf(this.curPage.getText()) - 1;
            if (pageNumber < 0
                    || Integer.valueOf(this.numOfPages.getText().substring(2)) <= pageNumber) {
                throw new IllegalArgumentException();
            }
            DiptychonLogger.info("Handle user input and go to page {}.",
                    pageNumber + 1);
            this.documentPanelController.openImage(pageNumber);
            this.root.requestFocus();
        } catch (final IllegalArgumentException e) {
            DiptychonLogger.error("There is no page: {}",
                    this.curPage.getText());
        }
    }

    /**
     * Invoked when the user pressed <code>save</code>
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleSaveAction(final ActionEvent event) {
        DiptychonLogger.info("Saving file...");
        this.miSave.setDisable(true);
        this.save.setDisable(true);
        this.documentPanelController.save();
    }

    /**
     * Invoked when the user pressed <code>save as</code>
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleSaveAsAction(final ActionEvent event) {
        final FileChooser fileChooser = new FileChooser();
        final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Diptychon Save File", "*.dsf");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File(DiptychonPreferences
                .getLastShownDirectory()));

        final File file = fileChooser.showSaveDialog(this.root.getScene()
                .getWindow());
        DiptychonPreferences.updateLastShownDirectory(file);

        if (file != null) {
            String filename = file.getAbsolutePath();
            if (!filename.endsWith(".dsf")) {
                filename += ".dsf";
            }
            DiptychonLogger.info("Save file as {}", filename);
            this.documentPanelController.save(filename);
            final String project = file.getName() + ".dsf";
            final String path = file.getAbsolutePath();
            this.title.set(TITLE_PREFIX + project + "     " + path);

            System.out.println("Dateiname: " + file.getName());
        } else if (event == null) {
            this.miSave.setDisable(false);
            this.save.setDisable(false);
        }
    }

    /**
     * Invoked when the user wants to open the font settings menu
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleFontSettingsMenu(final ActionEvent event) {
        final FontSettingsDialog sd = (FontSettingsDialog) new A_Dialog.Factory()
                .createDialog("FontSettingsDialog",
                        this.documentPanelController);
        sd.initBoxes(this.fontFamily, this.fontSize);
        sd.showModalDialog(this.root.getScene().getWindow(), -1, -1);
    }

    /**
     * Invoked when the user wants to open the fragmentation settings menu
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleFragSettingsMenu(final ActionEvent event) {
        final FragSettingsDialog sd = (FragSettingsDialog) new A_Dialog.Factory()
                .createDialog("FragSettingsDialog",
                        this.documentPanelController);
        sd.initBoxes(this.squareSize, this.splitLine, this.fragSize,
                this.fragCount, this.noiseSize);
        sd.showModalDialog(this.root.getScene().getWindow(), -1, -1);
    }

    /**
     * Invoked when the user wants to open the search engine preferences
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleSearchEnginePrefsMenu(final ActionEvent event) {
        SearchEngineDialog sed = (SearchEngineDialog) new A_Dialog.Factory()
                .createDialog("SearchEnginePreferenceDialog",
                        documentPanelController);
        // sed.initBoxes(ccThreshold);
        sed.showModalDialog(this.root.getScene().getWindow(), -1, -1);
    }

    /**
     * Invoked when the user wants to open the line detection settings menu
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleLineSettingsMenu(final ActionEvent event) {
        final LineSettingsDialog ld = (LineSettingsDialog) new A_Dialog.Factory()
                .createDialog("LineSettingsDialog",
                        this.documentPanelController);
        ld.initBoxes(this.lineExtTop, this.lineExtBottom);
        ld.showModalDialog(this.root.getScene().getWindow(), -1, -1);
    }

    /**
     * Invoked when the user pressed <code>show binarized</code>
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleShowBinarized(final ActionEvent event) {
        this.documentPanelController.showBinarizedImage();
    }

    /**
     * Invoked when the user wants to create a new project by using the wizard
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleWizard(final ActionEvent event) {
        final WizardDialogManager wdm = new WizardDialogManager();
        wdm.intial(this.documentPanelController, this.root.getScene()
                .getWindow());
        wdm.showWizardDialogs();
    }

    /**
     * Invoked when the user pressed <code>zooom in</code>
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleZoomIn(final MouseEvent event) {
        if (!this.zoomSlider.isDisabled()) {
            this.zoomSlider.setValue(this.zoomSlider.getValue() + 10);
        }
    }

    @FXML
    private void handleZoomFit(final ActionEvent event) {
        this.documentPanelController.zoomFit();
    }

    @FXML
    private void handleCalcStats(final ActionEvent event) {
        this.documentPanelController.calcStats();
    }

    /**
     * Invoked when the user pressed <code>zooom out</code>
     * 
     * @param event
     *            Propagated by JavaFX but can be ignored
     */
    @FXML
    private void handleZoomOut(final MouseEvent event) {
        if (!this.zoomSlider.isDisabled()) {
            this.zoomSlider.setValue(this.zoomSlider.getValue() - 10);
        }
    }

    @Override
    public void initialize(final URL fxmlFileLocation,
            final ResourceBundle resources) {
        assert this.curPage != null : "fx:id=\"curPage\" was not injected: check your FXML file 'MainFrame.fxml'.";
        assert this.next != null : "fx:id=\"next\" was not injected: check your FXML file 'MainFrame.fxml'.";
        assert this.numOfPages != null : "fx:id=\"numOfPages\" was not injected: check your FXML file "
                + "'MainFrame.fxml'.";
        assert this.open != null : "fx:id=\"open\" was not injected: check your FXML file 'MainFrame.fxml'.";
        assert this.prev != null : "fx:id=\"prev\" was not injected: check your FXML file 'MainFrame.fxml'.";
        assert this.root != null : "fx:id=\"root\" was not injected: check your FXML file 'MainFrame.fxml'.";
        assert this.save != null : "fx:id=\"save\" was not injected: check your FXML file 'MainFrame.fxml'.";
        assert this.saveAs != null : "fx:id=\"saveAs\" was not injected: check your FXML file 'MainFrame.fxml'.";
        assert this.zoomDecrease != null : "fx:id=\"zoomDecrease\" was not injected: check your FXML "
                + "file 'MainFrame.fxml'.";
        assert this.zoomIncrease != null : "fx:id=\"zoomIncrease\" was not injected: check your FXML "
                + "file 'MainFrame.fxml'.";
        assert this.zoomFit != null : "fx:id=\"zoomFit\" was not injected: check your FXML "
                + "file 'MainFrame.fxml'.";
        assert this.zoomSlider != null : "fx:id=\"zoomSlider\" was not injected: check your FXML file"
                + " 'MainFrame.fxml'.";
        this.findWord = new SimpleBooleanProperty(false);
        this.initUIControls();
        this.title = new SimpleStringProperty(TITLE_PREFIX);
        this.fontFamily = new SimpleStringProperty();
        this.fontFamily.set("Junicode");
        this.fontSize = new SimpleIntegerProperty();
        this.fontSize.set(16);
        this.squareSize = new SimpleIntegerProperty();
        this.squareSize.set(2);
        this.splitLine = new SimpleBooleanProperty();
        this.splitLine.set(false);
        this.fragSize = new SimpleIntegerProperty();
        this.fragSize.set(50);
        this.noiseSize = new SimpleIntegerProperty();
        this.noiseSize.set(5);
        this.fragCount = new SimpleFloatProperty();
        this.fragCount.set((float) 1.4);
        this.lineExtTop = new SimpleIntegerProperty();
        this.lineExtTop.set(10);
        this.lineExtBottom = new SimpleIntegerProperty();
        this.lineExtBottom.set(10);
        // this.ccThreshold = new SimpleFloatProperty();
        // this.ccThreshold.set((float) 0.7);
        HBox.setHgrow(this.spacer, Priority.ALWAYS);
        // System.out.println(this.toolbar.getPrefWidth() -
        // this.progressIndicator.getPrefWidth());
        // this.progressIndicator.setTranslateX(this.toolbar.getPrefWidth() -
        // this.progressIndicator.getPrefWidth());
    }

    /**
     * initialize the information of pages
     * 
     * @param pages
     *            the number of pages
     */
    private void initPageInfo(final int pages) {
        this.numOfPages.setText("/ " + pages);
        if (pages > 1) {
            this.prev.setDisable(false);
            this.next.setDisable(false);
            this.curPage.setEditable(true);
        }
    }

    /**
     * Initializes all ui controls, i.e. disabling several buttons etc
     */
    private void initUIControls() {
        this.zoomSlider.setDisable(true);
        this.save.setDisable(true);
        this.saveAs.setDisable(true);
        this.miSave.setDisable(true);
        this.miSaveAs.setDisable(true);
        this.miFind.setDisable(true);
        this.prev.setDisable(true);
        this.next.setDisable(true);
        this.curPage.setEditable(false);
        this.showBinarizedImage.setDisable(true);
    }

    @Override
    public void modelPropertyChange(final PropertyChangeEvent evt) {
        final String name = evt.getPropertyName();
        if (name.equals(DocumentPanelController.INIT_DIGITAL)) {
            this.saveAs.setDisable(false);
            this.miSaveAs.setDisable(false);
            this.zoomSlider.setDisable(false);
            this.showBinarizedImage.setDisable(false);
            this.initPageInfo((int) evt.getNewValue());
            this.updatePageInfo(1);
        } else if (name.equals(DocumentPanelController.BROWSE_IMAGES)) {
            this.updatePageInfo((int) evt.getNewValue());
        } else if (name.equals(DocumentPanelController.SHOW_IMAGE_NAME)) {
            final Object[] values = (Object[]) evt.getNewValue();
            final String path = (String) values[0];
            final String project;
            if (values[1] == null) {
                project = "Unnamed Project";
            } else {
                project = ((String) values[1]).substring(((String) values[1])
                        .lastIndexOf(File.separator) + 1);
            }
            this.title.set(TITLE_PREFIX + project + "     " + path);
        } else if (name.equals(DocumentPanelController.OPEN_PROJECT)) {
            this.documentPanelController.unregisterAllModels();
            final Digital digital = (Digital) evt.getNewValue();
            this.documentPanelController.registerModel(digital);
            this.documentPanelController.registerModel(digital.getTranscript());
            this.documentPanelController.loadFromHDD();
        } else if (name.equals(DocumentPanelController.UNKNOWN_SAVE_FILE)) {
            System.out
                    .println("MainFraime: modelPropertyChange: if UNKNOWN_SAVE_FILE ");
            this.handleSaveAsAction(null);
        } else if (name.equals(DocumentPanelController.STATUS_CHANGED)) {
            this.save.setDisable(false);
            this.miSave.setDisable(false);
        } else if (name.equals(DocumentPanelController.UPDATE_RECENTLY_USED)) {
            this.setRecentlyUsed((String) evt.getNewValue());
        } else if (name.equals(DocumentPanelController.SHOW_BINARIZE_DIALOG)) {
            this.showBinarizedImage.setSelected(true);
        } else if (name.equals(DocumentPanelController.CANCEL_BINARIZATION)) {
            this.showBinarizedImage.setSelected(false);
        } else if (name.equals(DocumentPanelController.PROGRESS_CHANGED)) {
            if ((double) evt.getNewValue() == 100) {
                this.progressIndicator.setProgress((double) 0);
                this.progressIndicator.setVisible(false);
            } else {
                this.progressIndicator.setVisible(true);
                if (!((double) evt.getNewValue() < this.progressIndicator
                        .getProgress())) {
                    this.progressIndicator.setProgress((double) evt
                            .getNewValue());
                }
            }
        } else if (name.equals(DocumentPanelController.GET_RGF_THRESHOLD)) {
            final float bla;
            // System.out.println("GET_RGF_THRESHOLD");
            if (this.rgfthres.getText().equals("")) {
                bla = 0;
            } else {
                bla = Float.parseFloat(this.rgfthres.getText());
            }
            this.documentPanelController.setRGFThreshold(bla);
        } else if (name.equals(DocumentPanelController.GET_FRAG_THRESHOLD)) {
            final float bla;
            // System.out.println("GET_FRAG_THRESHOLD");
            if (this.fragthres.getText().equals("")) {
                bla = 0;
            } else {
                bla = Float.parseFloat(this.fragthres.getText());
            }
            this.documentPanelController.setFragThreshold(bla);
        } else if (name.equals(DocumentPanelController.LOAD_FROM_HDD_DIGITAL)) {
            this.saveAs.setDisable(false);
            this.zoomSlider.setDisable(false);
            this.miSaveAs.setDisable(false);
            this.showBinarizedImage.setDisable(false);
            final int[] pageInfo = (int[]) evt.getNewValue();
            this.initPageInfo(pageInfo[0]);
            this.updatePageInfo(pageInfo[1]);
        } else if (name
                .equals(DocumentPanelController.BINARIZED_BUTTON_PRESSED)) {
            this.showBinarizedImage.setSelected(true);
        }
    }

    /**
     * Opens the last used project
     */
    public void openRecentlyUsed() {
        final List<String> recentlyUsed = DiptychonPreferences
                .getRecentlyUsed();
        this.setRecentlyUsed(recentlyUsed);
        if (recentlyUsed.size() > 0) {
            this.documentPanelController.openRecentlyUsed(recentlyUsed.get(0));
            this.miFind.setDisable(false);
        }
    }

    /**
     * Sets the reference to the documentpanelcontroller
     * 
     * @param pDocumentPanelController
     *            the reference to the documentPanelController
     */
    public void setController(
            final DocumentPanelController pDocumentPanelController) {
        this.documentPanelController = pDocumentPanelController;
    }

    /**
     * Updates the list of recently used projects
     * 
     * @param recentlyUsed
     *            the list of recently used projects
     */
    private void setRecentlyUsed(final List<String> recentlyUsed) {
        this.openRecent.getItems().clear();
        for (final String s : recentlyUsed) {
            if (s.equals("")) {
                break;
            } else if (!(new File(s).exists())) {
                continue;
            } else {
                final MenuItem mi = new MenuItem(s);
                mi.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        MainFrame.this.documentPanelController.openProject(s);
                    }
                });
                this.openRecent.getItems().add(mi);
            }
        }
        if (this.openRecent.getItems().size() == 0) {
            this.openRecent.setDisable(true);
        }
    }

    /**
     * Updates the list of recently used projects
     * 
     * @param filename
     *            the filename of the last used project
     */
    private void setRecentlyUsed(final String filename) {
        this.openRecent.getItems().clear();
        final List<String> recentlyUsed = DiptychonPreferences
                .updateRecentlyUsed(filename);
        this.setRecentlyUsed(recentlyUsed);
    }

    /**
     * update page infomation
     * 
     * @param pCurPage
     *            index of current page
     */
    private void updatePageInfo(final int pCurPage) {
        DiptychonLogger.info("Update page info, Current page is {}.", pCurPage);
        this.curPage.setText("" + pCurPage);
    }

    /**
     * Resets all ui controls
     */
    private void resetStatus() {
        this.showBinarizedImage.setSelected(false);
        this.showGlyphs.setSelected(true);
        this.showLines.setSelected(true);
        this.syncScroll.setSelected(true);
        this.onlyFocused.setSelected(true);
        this.documentPanelController.setDefaultStatus();
    }
}
