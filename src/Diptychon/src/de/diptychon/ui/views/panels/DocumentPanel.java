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
package de.diptychon.ui.views.panels;

import java.beans.PropertyChangeEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

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
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ContextMenuBuilder;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItemBuilder;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import de.diptychon.DiptychonLogger;
import de.diptychon.DiptychonPreferences;
import de.diptychon.controller.DocumentPanelController;
import de.diptychon.models.data.Glyph;
import de.diptychon.models.data.ImageLine;
import de.diptychon.models.representation.Alphabet;
import de.diptychon.models.representation.PageLine;
import de.diptychon.ui.views.AbstractView;
import de.diptychon.ui.views.dialogs.A_Dialog;
import de.diptychon.ui.views.dialogs.AutoContrastAdjustDialog;
import de.diptychon.ui.views.dialogs.BinarizeDialog;
import de.diptychon.ui.views.dialogs.FindDialog;
import de.diptychon.ui.views.dialogs.FragEditingDialog;
import de.diptychon.ui.views.dialogs.GalleryDialog;
import de.diptychon.ui.views.dialogs.GlyphDialog;
import de.diptychon.ui.views.dialogs.GrayScaleDialog;
import de.diptychon.ui.views.dialogs.InformationDialog;
import de.diptychon.ui.views.dialogs.SplitLineDialog;
import de.diptychon.ui.views.dialogs.TemplateMatchingThresholdDialog;

/**
 * This class is used to display the DocumentImage on the one hand and the
 * Transcript on the other. Additionally this class provides all possible
 * interactions between the user and the data. At some point it might be useful
 * to split this class into three classes, since JavaFX allows nested
 * controllers since 2.2
 */
public class DocumentPanel extends AbstractView implements Initializable {
    /**
     * The minimum value ({@value} ) which is allowed for scaling the document
     * image
     */
    private static final int MIN_SCALE = 5;

    /**
     * The default value ({@value} ) which is used to scale the document image
     */
    private static final int DEFAULT_SCALE = 100;

    /**
     * The maximum value ({@value} ) which is allowed for scaling the document
     * image
     */
    private static final int MAX_SCALE = 1000;

    /**
     * The rubberband style
     */
    private static final String RUBBERBAND_STYLE = "-fx-background-color: aqua;"
            // set background to aqua
            + "-fx-border-width: 3;"
            // border width 3px
            + "-fx-border-color: blue;"
            + "-fx-border-style: dashed;"
            + "-fx-opacity: 0.25;";

    /**
     * The css style for the background of the transcript panel
     */
    private static final String TRANSCRIPT_BACKGROUND_STYLE = "-fx-background-image: url('"
            + DocumentPanel.class.getResource("/backgrounds/transcriptbg.png")
                    .toExternalForm()
            + "');"
            + "-fx-background-position: center center;"
            + "-fx-background-repeat: repeat;"
            + "-fx-border-width: 0; -fx-opacity: 1.0;";

    /**
     * The style for textlines displayed at the DocumentImage
     */
    private static final String INNER_LINE_STYLE = "-fx-fill: white;"
            + "-fx-stroke: black;" + "-fx-stroke-width: 2;"
            + "-fx-stroke-type: outside;" + "-fx-opacity: 0;";

    /**
     * The style for textlines displayed at the DocumentImage
     */
    private static final String INNER_LINE_DRAGGED = "-fx-fill: transparent;"
            + "-fx-stroke: black;" + "-fx-stroke-width: 2;"
            + "-fx-stroke-type: outside;" + "-fx-opacity: 0.5;";

    /**
     * The style for textlines displayed at the DocumentImage
     */
    private static final String LINE_STYLE = "-fx-fill: white;"
            + "-fx-stroke: white;" + "-fx-stroke-width: 5;"
            + "-fx-stroke-type: outside;" + "-fx-opacity: 0.2;";

    /**
     * The style for textlines displayed at the DocumentImage when mouse hovers
     * over
     */
    private static final String LINE_STYLE_MOUSE_INSIDE = "-fx-fill: white;"
            + "-fx-stroke: brown;" + "-fx-stroke-width: 7;"
            + "-fx-stroke-type: outside;" + "-fx-opacity: 0.2;";

    /**
     * The style for textlines displayed at the DocumentImage, when focused.
     */
    private static final String LINE_STYLE_HIGHLIGHT = "-fx-fill: white;"
            + "-fx-stroke: brown;" + "-fx-stroke-width: 5;"
            + "-fx-stroke-type: outside;" + "-fx-opacity: 0.4;";

    /**
     * The style for textlines displayed at the DocumentImage, when marked as
     * handwritten annotation
     */
    private static final String LINE_STYLE_ANNOTATION = "-fx-fill: white;"
            + "-fx-stroke: yellow;" + "-fx-stroke-width: 5;"
            + "-fx-stroke-type: outside;" + "-fx-opacity: 0.2;";

    /**
     * Style for the lines which are used to display the suggestion how lines
     * should be separated
     */
    private static final String SPLIT_LINE_STYLE = "-fx-fill: black;"
            + "-fx-stroke-width: 2;" + "-fx-stroke-type: outside;"
            + "-fx-opacity: 0.5;";

    /**
     * The style for textlines displayed at the Transcript; partitioned into two
     * parts since <code>-fx-font-family:</code> expects a parameter
     */
    private static final String TEXTFIELD_STYLE_1 = "-fx-background-color: linen, linen, linen;"
            + "-fx-background-image: url('"
            + DocumentPanel.class.getResource("/backgrounds/transcriptbg.png")
                    .toExternalForm() + "');" + "-fx-font-family: ";

    /**
     * The style for textlines displayed at the Transcript
     */
    private static final String TEXTFIELD_STYLE_2 = "-fx-font-size: ";

    /**
     * The style for textlines displayed at the Transcript, when focused
     */
    private static final String TEXTFIELD_FOCUS = "-fx-background-color: dodgerblue, dodgerblue, "
            + "-fx-control-inner-background;-fx-font-family: ";

    /**
     * The style for textlines displayed at the Transcript, when marked as
     * handwritten annotation
     */
    private static final String TEXTFIELD_ANNOTATION_BACKGROUND = "-fx-control-inner-background: yellow;";

    /**
     * List for fragments to be merged
     */
    private ArrayList<Glyph> fragMergeList = new ArrayList<Glyph>();

    /**
     * Color for the visualization of the merging process via mouse drag on
     * fragments
     */
    private Paint fragMergeColor;

    /**
     * StringPoperty for the amount of existing fragments while fragment editing
     */
    private StringProperty fragsLeft = new SimpleStringProperty();

    /**
     * Newly separated fragments created by the model after a separation request
     */
    private ArrayList<Glyph> separatedFrags;

    /**
     * BooleanProperty for activation of the fragment editing apply button
     */
    private BooleanProperty disableApply;

    /**
     * StringPoperty for the total fragments that must be created to finalize
     * the fragment editing process
     */
    private StringProperty fragsNeeded;

    /**
     * All current fragments
     */
    private ArrayList<Glyph> frags;

    /**
     * Respective fragment for the open context menu
     */
    private Glyph contextFrag;

    /**
     * Copy of the last editing step's fragments, used for undo functionality
     */
    private ArrayList<Glyph> lastFrags;

    /**
     * Undo functionality is only offered if this is true
     */
    private boolean undoable;

    /**
     * Respective line for the open context menu
     */
    private Rectangle contextLine;

    /**
     * Path used for the visualization of line separations while fragment
     * editing mode
     */
    private Path path;

    /**
     * Respective glyph for the open context menu
     */
    private Glyph contextGlyph;

    /**
     * True if diptychon is currently in fragment editing mode
     */
    private boolean fragmentsShowing;

    /**
     * Cropped Image created by the model after a getCroppedImage() request
     */
    private Image croppedImage;

    /**
     * Values für the scrollbars
     */
    private double scrollH, scrollV;

    /**
     * This is true if a textfield got the focus
     */
    private boolean textfieldFocused;

    /**
     * InvalidationListener for fragment editing mode, who is responsible for
     * updating the fragment counters and apply button of the dialog etc.
     */
    private InvalidationListener fragEditingListener;

    /**
     * general-purpose timer, should be terminated manually before the program
     * is finally exited
     */
    private Timer timer;

    /**
     * Convenience enum which ease the visualization of the result of template
     * matching
     */
    public enum TemplateMatchingIDColor {
        /**
         * The value of an accepted glyph
         */
        ACCEPT(1, Color.LIGHTGREEN),

        /**
         * The value of a denied glyph
         */
        DENY(0, Color.RED);

        /**
         * According to the ID one can infer whether a glyph is accepted or not
         */
        private final int ID;

        /**
         * The visual feedback for users, whether a glyph is accepted or not.
         */
        private final Color color;

        /**
         * Default constructor
         * 
         * @param pID
         *            Identifier whether a glyph is accepted or not.
         * @param pColor
         *            <code>GREEN</code> when a glyph is accepted,
         *            <code>RED</code> otherwise
         */
        TemplateMatchingIDColor(final int pID, final Color pColor) {
            this.ID = pID;
            this.color = pColor;
        }

        /**
         * Gets the id
         * 
         * @return the ID
         */
        public int getID() {
            return this.ID;
        }

        /**
         * Gets the color
         * 
         * @return The color
         */
        public Color getColor() {
            return this.color;
        }

        /**
         * Inverts an ID
         * 
         * @param pID
         *            the ID to be inverted
         * @return The inverted ID
         */
        public static int invertID(final int pID) {
            return 1 - pID;
        }

        /**
         * Gets the corresponding color to an ID
         * 
         * @param pID
         *            The ID one wants to get the color of
         * @return The corresponding color
         */
        public static Color getColor(final int pID) {
            if (pID == ACCEPT.getID()) {
                return ACCEPT.color;
            } else {
                return DENY.color;
            }

        }
    }

    /**
     * The Parent node of this panel
     */
    @FXML
    private Node root;

    /**
     * The ImageView used to display the DocumentImage
     */
    @FXML
    private ImageView documentImageView;

    /**
     * The background pane of the DocumentImage (enables the possibility to zoom
     * and let the scrollpane get noticed)
     */
    @FXML
    private AnchorPane anchorPaneImage;

    /**
     * The background pane of the Transcript (enables the possibility to zoom
     * and let the scrollpane get noticed)
     */
    @FXML
    private AnchorPane anchorPaneTranscript;

    /**
     * The DocumentImage group where all Nodes should be put into, since they
     * will be automatically zoomed in that case
     */
    @FXML
    private Group groupImage;

    /**
     * The Transcript group where all Nodes should be put into, since they will
     * be automatically zoomed in that case
     */
    @FXML
    private Group groupTranscript;

    /**
     * the region which is marked by user
     */
    @FXML
    private Region rubberBand;

    /**
     * This is used as a workaround to make it possible to zoom in/out of the
     * Transcript. It is necessary since the groupTranscript needs content to be
     * resized
     */
    @FXML
    private Region transcriptBackground;

    /**
     * A flag which indicates whether there is displayed a suggestion on how to
     * separate a line or not. If <code>true</code> several interactions will be
     * suppressed.
     */
    private boolean splitLinesShowing;

    /**
     * The controller which makes it possible to communicate with the models.
     */
    private DocumentPanelController documentPanelController;

    /**
     * Property used for scale determination.
     */
    private IntegerProperty scale;

    /**
     * Property used for scale determination.
     */
    private IntegerProperty squareSize;

    /**
     * Property used for scale determination.
     */
    private IntegerProperty fragSize;

    /**
     * Property used for scale determination.
     */
    private IntegerProperty noiseSize;

    /**
     * Property used for scale determination.
     */
    private IntegerProperty lineExtTop;

    /**
     * Property used for scale determination.
     */
    private IntegerProperty lineExtBottom;

    /**
     * Property used for scale determination.
     */
    private BooleanProperty onlyFocused;

    /**
     * Property used for scale determination.
     */
    private FloatProperty fragCount;

    private FloatProperty ccThreshold;

    /**
     * Property used for scale determination.
     */
    private BooleanProperty splitLine;

    /**
     * <code>true</code> if glyphs should be displayed <code>false</code>
     * otherwise
     */
    private BooleanProperty showGlyphs;

    /**
     * <code>true</code> if lines should be displayed <code>false</code>
     * otherwise
     */
    private BooleanProperty showLines;

    /**
     * <code>true</code> if scrollbars should be synchronized <code>false</code>
     * otherwise
     */
    private BooleanProperty syncScroll;

    /**
     * <code>true</code> if search for words should be performed
     * <code>false</code> otherwise
     */
    private BooleanProperty findWord;

    /**
     * Invalidationlistern which is bind to the MenuItem in MainFrame
     */
    private InvalidationListener findWordInvalidationListener;

    /**
     * showSearchResult
     */
    private IntegerProperty showSearchResult;

    /**
     * number of results
     */
    private IntegerProperty numOfResults;

    /**
     * fontsize
     */
    private IntegerProperty fontsize;

    /**
     * x coordinate which records the starting point of mouse drag
     */
    private double startX;

    /**
     * y coordinate which records the starting point of mouse drag
     */
    private double startY;

    /**
     * left scrollPane
     */
    @FXML
    private ScrollPane leftScroll;

    /**
     * right scrollPane
     */
    @FXML
    private ScrollPane rightScroll;

    /**
     * rubberband menu
     */
    private PopupMenu rubberBandMenu;

    /**
     * fragment menu
     */
    private PopupMenu fragMenu;

    /**
     * glyph menu
     */
    private PopupMenu glyphMenu;

    /**
     * glyph menu
     */
    private PopupMenu documentMenu;

    /**
     * glyph menu
     */
    private PopupMenu documentMenu2;

    /**
     * glyph menu
     */
    private PopupMenu documentMenu3;

    /**
     * glyph menu
     */
    private PopupMenu documentMenu4;

    /**
     * glyph menu
     */
    private PopupMenu lineMenu;

    /**
     * glyph menu
     */
    private PopupMenu fragLineMenu;

    /**
     * glyph menu
     */
    private PopupMenu fragLineMenu2;

    /**
     * rubberband drag
     */
    private boolean rubberbandDrag;

    /**
     * middle mouse button drag
     */
    private boolean middleMouseDrag;

    /**
     * textline drag
     */
    private boolean textlineDrag;

    private String RGFLineID;

    /**
     * font family
     */
    private StringProperty fontfamily;

    @SuppressWarnings("unchecked")
    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(final URL fxmlFileLocation,
            final ResourceBundle resources) {
        assert this.documentImageView != null : "fx:id=\"documentImageView\" was not injected: "
                + "check your FXML file 'DocumentPanel.fxml'.";
        assert this.anchorPaneImage != null : "fx:id=\"group\" was not injected: check your "
                + "FXML file 'DocumentPanel.fxml'.";
        assert this.root != null : "fx:id=\"root\" was not injected: check your FXML file 'DocumentPanel.fxml'.";

        this.transcriptBackground.setStyle(TRANSCRIPT_BACKGROUND_STYLE);
        this.rubberBand.setStyle(RUBBERBAND_STYLE);
        this.resetRubberband();
        this.startX = 0;
        this.startY = 0;

        this.addScaleInvalidationListener();
        this.addInvalidationListeners();
        this.addFindWordInvalidationListener();
        this.addFontInvalidationListener();
        this.squareSize = new SimpleIntegerProperty(2);
        this.splitLine = new SimpleBooleanProperty(false);
        this.fragSize = new SimpleIntegerProperty(50);
        this.noiseSize = new SimpleIntegerProperty(5);
        this.fragCount = new SimpleFloatProperty((float) 1.4);
        this.fragsNeeded = new SimpleStringProperty("0");
        this.lineExtTop = new SimpleIntegerProperty(10);
        this.lineExtBottom = new SimpleIntegerProperty(10);
        this.addScrollInvalidationListener(this.leftScroll, this.rightScroll);
        this.addScrollInvalidationListener(this.rightScroll, this.leftScroll);
        this.timer = new Timer();
        this.path = new Path();
        this.path.setId("PATH");

        this.rubberBandMenu = new PopupMenu();

        this.rubberBandMenu
                .addAll(new String[] { "buttons/glyph", "buttons/extract",
                        "buttons/contrast", "buttons/line", "buttons/binarize" },
                        new EventHandler[] { this.getExtractGlyphHandler(),
                                this.getMaskImageHandler(),
                                this.getAutoContastAdjustHandler(),
                                this.getExtractLineHandler(),
                                this.getBinarizeMaskHandler() });

        this.rubberbandDrag = true;

        this.fragMenu = new PopupMenu();

        this.fragMenu.addAll(
                new String[] { "buttons/burst", "buttons/remove" },
                new EventHandler[] { this.getBurstFragmentHandler(),
                        this.getRemoveFragmentHandler() });

        this.glyphMenu = new PopupMenu();

        this.glyphMenu
                .addAll(new String[] { "buttons/moveleft", "buttons/moveright",
                        "buttons/editglyph", "buttons/removeglyph",
                        "buttons/gallery" },
                        new EventHandler[] { this.getMoveLeftHandler(),
                                this.getMoveRightHandler(),
                                this.getEditGlyphHandler(),
                                this.getRemoveGlyphHandler(),
                                this.getGalleryHandler() });

        this.documentMenu = new PopupMenu();

        this.documentMenu.addAll(
                new String[] { "buttons/binarize", "buttons/greyscale",
                        "buttons/contrast", "buttons/glyphnoise",
                        "buttons/textlines" },
                new EventHandler[] { this.getBinarizeHandler(),
                        this.getGreyscaleHandler(), this.getContrastHandler(),
                        this.getGlyphNoiseHandler(),
                        this.getFindTextlinesHandler() });

        this.documentMenu2 = new PopupMenu();

        this.documentMenu2.addAll(
                new String[] { "buttons/binarize", "buttons/greyscale",
                        "buttons/contrast", "buttons/glyphnoise" },
                new EventHandler[] { this.getBinarizeHandler(),
                        this.getGreyscaleHandler(), this.getContrastHandler(),
                        this.getGlyphNoiseHandler() });

        this.documentMenu3 = new PopupMenu();

        this.documentMenu3
                .addAll(new String[] { "buttons/binarize", "buttons/greyscale",
                        "buttons/contrast" },
                        new EventHandler[] { this.getBinarizeHandler(),
                                this.getGreyscaleHandler(),
                                this.getContrastHandler() });

        this.documentMenu4 = new PopupMenu();

        this.documentMenu4.addAll(
                new String[] { "buttons/binarize", "buttons/greyscale",
                        "buttons/contrast", "buttons/textlines" },
                new EventHandler[] { this.getBinarizeHandler(),
                        this.getGreyscaleHandler(), this.getContrastHandler(),
                        this.getFindTextlinesHandler() });

        this.fragLineMenu = new PopupMenu();

        this.fragLineMenu.addAll(
                new String[] { "buttons/fragnoise", "buttons/borderfrags" },
                new EventHandler[] { this.getFragNoiseHandler(),
                        this.getRemoveBorderFragsHandler() });

        this.fragLineMenu2 = new PopupMenu();

        this.fragLineMenu2.addAll(
                new String[] { "buttons/fragnoise", "buttons/undo",
                        "buttons/borderfrags" },
                new EventHandler[] { this.getFragNoiseHandler(),
                        this.getUndoHandler(),
                        this.getRemoveBorderFragsHandler() });

        this.lineMenu = new PopupMenu();

        this.lineMenu.addAll(
                new String[] { "buttons/transcribe", "buttons/fragmentate",
                        "buttons/annotation", "buttons/remove" },
                new EventHandler[] { this.getTranscribeHandler(),
                        this.getFragmentateHandler(),
                        this.getAnnotationHandler(),
                        this.getRemoveLineHandler() });

        this.root.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(final KeyEvent evt) {
                if (!DocumentPanel.this.textfieldFocused) {
                    KeyCombination combo = new KeyCodeCombination(KeyCode.LEFT);
                    ScrollPane scrollPane = (ScrollPane) DocumentPanel.this.anchorPaneImage
                            .getParent().getParent().getParent();
                    if (combo.match(evt)) {
                        scrollPane.setHvalue(scrollPane.getHvalue() - 0.01);
                    }
                    combo = new KeyCodeCombination(KeyCode.RIGHT);
                    if (combo.match(evt)) {
                        scrollPane.setHvalue(scrollPane.getHvalue() + 0.01);
                    }
                    combo = new KeyCodeCombination(KeyCode.UP);
                    if (combo.match(evt)) {
                        scrollPane.setVvalue(scrollPane.getVvalue() - 0.01);
                    }
                    combo = new KeyCodeCombination(KeyCode.DOWN);
                    if (combo.match(evt)) {
                        scrollPane.setVvalue(scrollPane.getVvalue() + 0.01);
                    }
                    combo = new KeyCodeCombination(KeyCode.EQUALS);
                    if (combo.match(evt)) {
                        DocumentPanel.this.scale
                                .setValue(DocumentPanel.this.scale.getValue() + 10);
                    }
                    combo = new KeyCodeCombination(KeyCode.MINUS);
                    if (combo.match(evt)) {
                        DocumentPanel.this.scale
                                .setValue(DocumentPanel.this.scale.getValue() - 10);
                    }
                }
            }
        });
    }

    public void removeFragNoise() {
        ArrayList<Glyph> fragsToRemove = new ArrayList<Glyph>();
        for (Glyph f : this.frags) {
            if (f.getSize() < this.noiseSize.getValue()) {
                fragsToRemove.add(f);
            }
        }
        this.frags.removeAll(fragsToRemove);
        this.showFragments(this.frags);
    }

    /**
     * Adds FindWord functionality. When the corresponding MenuItem in MainFrame
     * is clicked, the invalidationLister will take notice and show the Dialog
     */
    private void addFindWordInvalidationListener() {
        this.findWord = new SimpleBooleanProperty(false);
        this.showSearchResult = new SimpleIntegerProperty(-2);
        this.numOfResults = new SimpleIntegerProperty(-1);
        this.findWord.addListener(new InvalidationListener() {

            @Override
            public void invalidated(final Observable observable) {
                if (DocumentPanel.this.findWord.get()) {
                    DocumentPanel.this.findWord.set(false);
                    final FindDialog findDialog = (FindDialog) new A_Dialog.Factory()
                            .createDialog(FindDialog.class.getCanonicalName(),
                                    DocumentPanel.this.documentPanelController);

                    findDialog
                            .bindNumOfResults(DocumentPanel.this.numOfResults);

                    DocumentPanel.this.showSearchResult
                            .bindBidirectional(findDialog
                                    .getShowIndexProperty());

                    final Rectangle2D bounds = Screen.getPrimary()
                            .getVisualBounds();

                    findDialog.showNormalDialog(
                            DocumentPanel.this.root.getScene().getWindow(),
                            (int) ((bounds.getWidth() - bounds.getMinX()) - Math
                                    .max(400, DocumentPanel.this.rightScroll
                                            .getWidth() / 2 + 200)),
                            (int) ((bounds.getHeight() - bounds.getMinY()) / 2 - 120));
                }
            }
        });
    }

    /**
     * This method is used to show the context menu for processing images. such
     * as binarize image find text line, adjust contrast and make gray scale
     * image.
     * 
     * @param event
     *            contextmenusevent
     */
    @FXML
    public void showDocumentImageContextMenu(final ContextMenuEvent event) {
        boolean foundtext = false;
        final ObservableList<Node> children = this.groupTranscript
                .getChildren();
        for (final Node child : children) {
            if (child instanceof TextField) {
                TextField tf = (TextField) child;
                if (tf.getText().length() > 0) {
                    foundtext = true;
                }
            }
        }
        if (foundtext) {
            if (this.getGlyphCount() > 0) {
                DocumentPanel.this.documentMenu2.showMenu(
                        DocumentPanel.this.root.getScene().getWindow(),
                        event.getScreenX(), event.getScreenY());
            } else {
                DocumentPanel.this.documentMenu3.showMenu(
                        DocumentPanel.this.root.getScene().getWindow(),
                        event.getScreenX(), event.getScreenY());
            }
        } else {
            if (this.getGlyphCount() > 0) {
                DocumentPanel.this.documentMenu.showMenu(
                        DocumentPanel.this.root.getScene().getWindow(),
                        event.getScreenX(), event.getScreenY());
            } else {
                DocumentPanel.this.documentMenu4.showMenu(
                        DocumentPanel.this.root.getScene().getWindow(),
                        event.getScreenX(), event.getScreenY());
            }
        }
    }

    /**
     * This method is used to add scale into invalidation listener.
     */
    private void addScaleInvalidationListener() {
        this.scale = new SimpleIntegerProperty(DEFAULT_SCALE);
        this.scale.addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                final int curScale = DocumentPanel.this.scale.get();
                if (curScale < MIN_SCALE) {
                    DocumentPanel.this.scale.set(MIN_SCALE);
                } else if (curScale > MAX_SCALE) {
                    DocumentPanel.this.scale.set(MAX_SCALE);
                } else {
                    final float factor = curScale / 100.f;

                    DocumentPanel.this.resize(
                            DocumentPanel.this.anchorPaneImage,
                            DocumentPanel.this.groupImage, factor);
                    DocumentPanel.this.resize(
                            DocumentPanel.this.anchorPaneTranscript,
                            DocumentPanel.this.groupTranscript, factor);

                }
            }
        });
    }

    /**
     * This method is used to add font changes into invalidationlistener.
     */
    private void addFontInvalidationListener() {
        this.fontsize = new SimpleIntegerProperty(24);
        this.fontsize.addListener(new InvalidationListener() {

            @Override
            public void invalidated(final Observable observable) {
                DocumentPanel.this.resizeSetFontAndPositionPageLines();
            }
        });
        this.fontfamily = new SimpleStringProperty("Junicode");
        this.fontfamily.addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                DocumentPanel.this.resizeSetFontAndPositionPageLines();
            }
        });
    }

    /**
     * This method is used to resize the font and position of page lines in the
     * current page.
     */
    private void resizeSetFontAndPositionPageLines() {
        final ObservableList<Node> children = this.groupTranscript
                .getChildren();
        for (final Node child : children) {
            if (child instanceof TextField) {
                child.setStyle(TEXTFIELD_STYLE_1 + this.fontfamily.get() + ";"
                        + TEXTFIELD_STYLE_2 + this.fontsize.get() + "pt;");

                final TextField textfield = (TextField) child;

                final double height = this.calcTextfieldHeight();

                final int offset = (int) ((textfield.getPrefHeight() - height) / 2);

                textfield.setLayoutY(textfield.getLayoutY() + offset);
                textfield.setPrefHeight(height);
                textfield.setPrefWidth(this.calcTextfieldWidth(
                        (int) textfield.getLayoutX(),
                        (int) textfield.getMaxWidth(), textfield.getLength()));
            }
        }
    }

    /**
     * This method is used to calculate the height of textfield.
     * 
     * @return height of textfield
     */
    private double calcTextfieldHeight() {
        return 0.04 * this.fontsize.get() * this.fontsize.get() + 20;
    }

    /**
     * This method is used to calculate the width of textfield
     * 
     * @param posX
     *            X position
     * @param curWidth
     *            current width
     * @param contentLength
     *            length of content
     * @return the width of textfield
     */
    private double calcTextfieldWidth(final int posX, final int curWidth,
            final int contentLength) {
        if (contentLength <= 0) {
            return curWidth * 0.6;
        } else {
            final double widthRestriction = (this.transcriptBackground
                    .getPrefWidth() - posX - 5);
            return Math.min(widthRestriction, -(this.fontsize.get() ^ 2) + 0.5
                    * contentLength * this.fontsize.get() + 50);
        }
    }

    /**
     * This method is used to attach showglyphs with invalidationlisteners.
     */
    private void addInvalidationListeners() {
        this.showGlyphs = new SimpleBooleanProperty(true);
        this.showGlyphs.addListener(new InvalidationListener() {

            @Override
            public void invalidated(final Observable observable) {
                if (DocumentPanel.this.showGlyphs.get()) {
                    DocumentPanel.this.documentPanelController
                            .getGlyphsAndLines();
                    if (DocumentPanel.this.fragmentsShowing) {
                        DocumentPanel.this
                                .showFragments(DocumentPanel.this.frags);
                    }
                } else {
                    DocumentPanel.this.showGlyphs(null);
                    if (DocumentPanel.this.fragmentsShowing) {
                        DocumentPanel.this
                                .showFragments(DocumentPanel.this.frags);
                    }
                }
            }
        });
        this.onlyFocused = new SimpleBooleanProperty(true);
        this.onlyFocused.addListener(new InvalidationListener() {

            @Override
            public void invalidated(final Observable observable) {
                if (DocumentPanel.this.showGlyphs.get()) {
                    DocumentPanel.this.documentPanelController
                            .getGlyphsAndLines();
                    if (DocumentPanel.this.fragmentsShowing) {
                        DocumentPanel.this
                                .showFragments(DocumentPanel.this.frags);
                    }
                } else {
                    DocumentPanel.this.showGlyphs(null);
                    if (DocumentPanel.this.fragmentsShowing) {
                        DocumentPanel.this
                                .showFragments(DocumentPanel.this.frags);
                    }
                }
            }
        });
        this.showLines = new SimpleBooleanProperty(true);
        this.showLines.addListener(new InvalidationListener() {

            @Override
            public void invalidated(final Observable observable) {
                if (DocumentPanel.this.showLines.get()) {
                    DocumentPanel.this.documentPanelController
                            .getGlyphsAndLines();
                } else {
                    DocumentPanel.this.showDocumentImageLines(null);
                }
            }
        });
        this.syncScroll = new SimpleBooleanProperty(true);
        this.syncScroll.addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                if (DocumentPanel.this.syncScroll.get()) {
                    DocumentPanel.this.syncScrollbars(
                            DocumentPanel.this.leftScroll,
                            DocumentPanel.this.rightScroll);
                }
            }
        });
    }

    /**
     * This method is used to resize the anchor pane. This is the only way to
     * make the scrollpane take notice of zooming in/out. Otherwise the
     * scrollBars will not be adapted.
     * 
     * @param anchorPane
     *            The anchorPane to be resized (transcript or imageview)
     * @param group
     *            The corresponding group
     * @param factor
     *            The scale factor
     */
    private void resize(final AnchorPane anchorPane, final Group group,
            final float factor) {
        group.getTransforms().clear();

        double paneWidth = anchorPane.getParent().getParent()
                .getBoundsInParent().getWidth();
        double paneHeight = anchorPane.getParent().getParent()
                .getBoundsInParent().getHeight();
        double groupWidth = group.getBoundsInParent().getWidth();
        double groupHeight = group.getBoundsInParent().getHeight();
        double scrollbarVPos = ((ScrollPane) anchorPane.getParent().getParent()
                .getParent()).getVvalue();
        double scrollbarHPos = ((ScrollPane) anchorPane.getParent().getParent()
                .getParent()).getHvalue();

        this.scrollH = scrollbarHPos;
        this.scrollV = scrollbarVPos;

        double pivotX = paneWidth / 2 + groupWidth * scrollbarHPos - paneWidth
                * scrollbarHPos;
        double pivotY = paneHeight / 2 + groupHeight * scrollbarVPos
                - paneHeight * scrollbarVPos;

        if (pivotX < 0) {
            pivotX = 0;
        }
        if (pivotY < 0) {
            pivotY = 0;
        }

        /*
         * System.out.println("GROUPWIDTH: " + groupWidth);
         * System.out.println("GROUPHEIGHT: " + groupHeight);
         * System.out.println("ANCHORPANEWIDTH: " + paneWidth);
         * System.out.println("ANCHORPANEHEIGHT: " + paneHeight);
         * System.out.println("PIVOTX: " + pivotX);
         * System.out.println("PIVOTY: " + pivotY);
         * System.out.println("HVALUE: " +
         * ((ScrollPane)anchorPane.getParent().getParent
         * ().getParent()).getHvalue()); System.out.println("VVALUE: " +
         * ((ScrollPane
         * )anchorPane.getParent().getParent().getParent()).getVvalue());
         * System.out.println("HMAX: " +
         * ((ScrollPane)anchorPane.getParent().getParent
         * ().getParent()).getHmax()); System.out.println("VMAX: " +
         * ((ScrollPane
         * )anchorPane.getParent().getParent().getParent()).getVmax());
         */

        double width = group.getBoundsInParent().getWidth();
        double height = group.getBoundsInParent().getHeight();

        group.getTransforms().add(new Scale(factor, factor, 0, 0));

        width = group.getBoundsInParent().getWidth();
        height = group.getBoundsInParent().getHeight();

        anchorPane.setPrefWidth(width);
        anchorPane.setPrefHeight(height);

        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {

                ((ScrollPane) anchorPane.getParent().getParent().getParent())
                        .setHvalue(DocumentPanel.this.scrollH);
                ((ScrollPane) anchorPane.getParent().getParent().getParent())
                        .setVvalue(DocumentPanel.this.scrollV);
            }

        }, 25);
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (((ScrollPane) anchorPane.getParent().getParent()
                        .getParent()).getHvalue() != DocumentPanel.this.scrollH
                        || ((ScrollPane) anchorPane.getParent().getParent()
                                .getParent()).getVvalue() != DocumentPanel.this.scrollV) {
                    ((ScrollPane) anchorPane.getParent().getParent()
                            .getParent()).setHvalue(DocumentPanel.this.scrollH);
                    ((ScrollPane) anchorPane.getParent().getParent()
                            .getParent()).setVvalue(DocumentPanel.this.scrollV);
                }
            }
        }, 250);
    }

    /**
     * This method is used to resize the anchor pane. This is the only way to
     * make the scrollpane take notice of zooming in/out. Otherwise the
     * scrollBars will not be adapted.
     * 
     * @param anchorPane
     *            The anchorPane to be resized (transcript or imageview)
     * @param group
     *            The corresponding group
     * @param factor
     *            The scale factor
     */
    private void resizeToLine(final AnchorPane anchorPane, final Group group,
            final String lineID) {
        this.showLines.set(true);
        double paneWidth = anchorPane.getParent().getParent()
                .getBoundsInParent().getWidth();
        double paneHeight = anchorPane.getParent().getParent()
                .getBoundsInParent().getHeight();

        Node node = group.lookup("#" + lineID);

        final ScrollPane sp = ((ScrollPane) anchorPane.getParent().getParent()
                .getParent());
        double factor = paneHeight / (node.getBoundsInLocal().getHeight() * 2);

        group.getTransforms().clear();

        this.scale.setValue(100);
        this.scale.setValue(factor * 100);

        final double newWidth = group.getBoundsInParent().getWidth();
        final double newHeight = group.getBoundsInParent().getHeight();

        anchorPane.setPrefWidth(newWidth);
        anchorPane.setPrefHeight(newHeight);

        double groupWidth = group.getBoundsInParent().getWidth();
        double groupHeight = group.getBoundsInParent().getHeight();

        double minX = node.getBoundsInLocal().getMinX() * factor;
        double minY = node.getBoundsInLocal().getMinY() * factor;

        double pivotX = minX;
        double pivotY = minY + node.getLayoutBounds().getHeight() * 2;

        this.scrollV = pivotY / groupHeight - (paneHeight / 2) / groupHeight
                + (paneHeight / groupHeight) * (pivotY / groupHeight);
        this.scrollH = pivotX / groupWidth - (paneWidth / 2) / groupWidth
                + (paneWidth / groupWidth) * (pivotX / groupWidth);

        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                sp.setVvalue(DocumentPanel.this.scrollV);
                sp.setHvalue(DocumentPanel.this.scrollH);
            }

        }, 100);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                DocumentPanel.this.timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (((ScrollPane) anchorPane.getParent().getParent()
                                .getParent()).getHvalue() != DocumentPanel.this.scrollH
                                || ((ScrollPane) anchorPane.getParent()
                                        .getParent().getParent()).getVvalue() != DocumentPanel.this.scrollV) {
                            ((ScrollPane) anchorPane.getParent().getParent()
                                    .getParent())
                                    .setHvalue(DocumentPanel.this.scrollH);
                        }
                        ((ScrollPane) anchorPane.getParent().getParent()
                                .getParent())
                                .setVvalue(DocumentPanel.this.scrollV);
                    }
                }, 1000);
            }
        });
    }

    /**
     * This method is used to set the starting point for the rubberband.
     * 
     * @param event
     *            mouse event
     */
    @FXML
    private void mousePressed(final MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            this.startX = event.getSceneX();
            this.startY = event.getSceneY();
            this.rubberbandDrag = true;
        }
    }

    /**
     * This method is used to detect whether the mouse is dragged or not.
     * According to that, the rubberband will be displayed or updated.
     * 
     * @param event
     *            mouse event
     */
    @FXML
    private void mouseDragged(final MouseEvent event) {
        if (event.getButton().equals(MouseButton.MIDDLE)) {
            this.middleMouseDrag = true;
        }
        if (!event.getButton().equals(MouseButton.PRIMARY)
                || !this.rubberbandDrag) {
            return;
        }
        final Point2D point = this.documentImageView.localToScene(
                this.documentImageView.getLayoutX(),
                this.documentImageView.getLayoutY());
        double width = event.getSceneX() - this.startX;
        double height = event.getSceneY() - this.startY;

        double x = this.startX - point.getX();
        double y = this.startY - point.getY();

        // clip width/height according to lower bound
        width = x + width < 0 ? -x : width;
        height = y + height < 0 ? -y : height;

        // change x/y according to direction of width/height
        x = width >= 0 ? x : x + width;
        y = height >= 0 ? y : y + height;

        width = Math.abs(width);
        height = Math.abs(height);

        final double curWidth = this.documentImageView.getFitWidth()
                * (this.scale.doubleValue() / 100.f);
        final double curHeight = this.documentImageView.getFitHeight()
                * (this.scale.doubleValue() / 100.f);

        // clip width/height according to image size
        width = x + width > curWidth ? curWidth - x : width;
        height = y + height > curHeight ? curHeight - y : height;

        final float scaleFactor = this.scale.floatValue() / 100.f;
        this.updateRubberBandPosAndSize((int) (x / scaleFactor),
                (int) (y / scaleFactor), (int) (width / scaleFactor),
                (int) (height / scaleFactor));

        event.consume();
    }

    /**
     * This method is used to detect whether the mouse is released or not.
     * 
     * @param event
     *            mouse event
     */
    @FXML
    private void mouseReleased(final MouseEvent event) {
        this.rubberbandDrag = false;
    }

    /**
     * This method is used to update the position and size of rubberband.
     * 
     * @param x
     *            position x
     * @param y
     *            position y
     * @param pWidth
     *            the width of rubberband
     * @param pHeight
     *            the height of rubberband
     */
    private void updateRubberBandPosAndSize(final int x, final int y,
            final int pWidth, final int pHeight) {
        this.rubberBand.setLayoutX(x);
        this.rubberBand.setLayoutY(y);
        this.rubberBand.setPrefWidth(pWidth);
        this.rubberBand.setPrefHeight(pHeight);
        this.rubberBand.toFront();
        this.rubberBand.setVisible(true);
    }

    /**
     * This method is used to reset rubberband if the mouse is clicked outside
     * of the rubberband.
     * 
     * @param event
     *            mouse event
     */
    @FXML
    private void mouseClicked(final MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            if (Math.abs(this.startX - event.getSceneX())
                    + Math.abs(this.startY - event.getSceneY()) < 2) {
                this.resetRubberband();
            }
        } else if (event.getButton().equals(MouseButton.MIDDLE)) {
            if (this.middleMouseDrag) {
                this.middleMouseDrag = false;
                return;
            }
            Point2D point = this.groupImage.sceneToLocal(event.getSceneX(),
                    event.getSceneY());
            final double pivotX = point.getX() * this.scale.getValue() / 100;
            final double pivotY = point.getY() * this.scale.getValue() / 100;

            double paneWidth = this.anchorPaneImage.getParent().getParent()
                    .getBoundsInParent().getWidth();
            double paneHeight = this.anchorPaneImage.getParent().getParent()
                    .getBoundsInParent().getHeight();
            double groupWidth = this.groupImage.getBoundsInParent().getWidth();
            double groupHeight = this.groupImage.getBoundsInParent()
                    .getHeight();

            this.leftScroll.setVvalue(pivotY / groupHeight - (paneHeight / 2)
                    / groupHeight + (paneHeight / groupHeight)
                    * (pivotY / groupHeight));
            this.leftScroll.setHvalue(pivotX / groupWidth - (paneWidth / 2)
                    / groupWidth + (paneWidth / groupWidth)
                    * (pivotX / groupWidth));

            this.scale.set(250);
        }
    }

    /**
     * This method is used to show the contextmenu of rubberband.
     * 
     * @param event
     *            mouse event
     */
    @FXML
    private void rubberBandClicked(final MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            final double screenX = event.getScreenX();
            final double screenY = event.getScreenY();
            this.rubberBandMenu.showMenu(this.root.getScene().getWindow(),
                    screenX, screenY);
        }
    }

    /**
     * This method is used to set the rubberband by default value 0.
     */
    private void resetRubberband() {
        this.rubberBand.setPrefWidth(0);
        this.rubberBand.setPrefHeight(0);
        this.rubberBand.setVisible(false);
    }

    /**
     * Gets the scale property
     * 
     * @return scale
     */
    public IntegerProperty getScaleProperty() {
        return this.scale;
    }

    /**
     * Gets the square fragment size property
     * 
     * @return square fragment size property
     */
    public IntegerProperty getSquareSizeProperty() {
        return this.squareSize;
    }

    /**
     * Gets the split line property
     * 
     * @return split line property
     */
    public IntegerProperty getFragSizeProperty() {
        return this.fragSize;
    }

    /**
     * Gets the split line property
     * 
     * @return split line property
     */
    public FloatProperty getFragCountProperty() {
        return this.fragCount;
    }

    /**
     * Gets the split line property
     * 
     * @return split line property
     */
    public IntegerProperty getNoiseSizeProperty() {
        return this.noiseSize;
    }

    /**
     * Gets the split line property
     * 
     * @return split line property
     */
    public IntegerProperty getLineExtTopProperty() {
        return this.lineExtTop;
    }

    /**
     * Gets the split line property
     * 
     * @return split line property
     */
    public IntegerProperty getLineExtBottomProperty() {
        return this.lineExtBottom;
    }

    /**
     * Gets the split line property
     * 
     * @return split line property
     */
    public BooleanProperty getOnlyFocusedProperty() {
        return this.onlyFocused;
    }

    /**
     * Gets the split line property
     * 
     * @return split line property
     */
    public BooleanProperty getSplitLineProperty() {
        return this.splitLine;
    }

    /**
     * Gets the property of showglyph.
     * 
     * @return property of showglyph
     */
    public BooleanProperty getShowGlyphsProperty() {
        return this.showGlyphs;
    }

    /**
     * Gets the property of showlines
     * 
     * @return property of showlines
     */
    public BooleanProperty getShowLinesProperty() {
        return this.showLines;
    }

    /**
     * Gets property of Fontsize.
     * 
     * @return property of fontsize
     */
    public IntegerProperty getFontsizeProperty() {
        return this.fontsize;
    }

    /**
     * Gets the property of Findword
     * 
     * @return property of findword
     */
    public BooleanProperty getFindWordProperty() {
        return this.findWord;
    }

    /**
     * Gets the root node
     * 
     * @return root
     */
    public Node getView() {
        return this.root;
    }

    /**
     * Sets the documentpanelcontroller
     * 
     * @param pDocumentPanelController
     *            document panel controller
     */
    public void setController(
            final DocumentPanelController pDocumentPanelController) {
        this.documentPanelController = pDocumentPanelController;
    }

    /**
     * This method is used to sort glyphs their position on the document image
     * 
     * @param allGlyphs
     *            all the glyphs
     */
    private void sortGlyphs(final ArrayList<Glyph> allGlyphs) {
        Collections.sort(allGlyphs, new Comparator<Glyph>() {
            @Override
            public int compare(final Glyph g1, final Glyph g2) {
                final String[] splitID1 = g1.getID().split("_");
                final String[] splitID2 = g2.getID().split("_");

                if (splitID1.length == splitID2.length) {
                    if (splitID1.length == 6) {
                        final int colID1 = Integer.valueOf(splitID1[3]);
                        final int colID2 = Integer.valueOf(splitID2[3]);
                        if (colID1 < colID2) {
                            return -1;
                        } else if (colID1 > colID2) {
                            return 1;
                        }
                    }
                    final int gID1 = Integer
                            .valueOf(splitID1[splitID1.length - 1]);
                    final int gID2 = Integer
                            .valueOf(splitID2[splitID2.length - 1]);
                    if (gID1 < gID2) {
                        return -1;
                    } else if (gID1 == gID2) {
                        return 0;
                    } else {
                        return 1;
                    }
                } else if (splitID1.length < splitID2.length) {
                    return 1;
                } else if (splitID1.length > splitID2.length) {
                    return -1;
                }

                return 0;
            }
        });
    }

    /**
     * This method is used to clear the glyph with prefix.
     */
    private void clearGlyphs() {
        this.clearChildrensWithIDPrefix(Shape.class, Glyph.ID_PREFIX,
                Alphabet.COLUMN_PREFIX);
    }

    /**
     * This method is used to clear the glyph with prefix.
     */
    private void clearFragments() {
        this.clearChildrensWithIDPrefix(Shape.class, "F_", "BLABLÖ");
    }

    /**
     * This method is used to show glyphs.
     * 
     * @param allGlyphs
     *            arraylist contains all glyphs
     */
    private void showGlyphs(final ArrayList<Glyph> allGlyphs) {
        this.clearGlyphs();
        if (!this.showGlyphs.get()) {
            return;
        }
        final ObservableList<Node> children = this.groupImage.getChildren();
        int i = 0;
        this.sortGlyphs(allGlyphs);
        for (final Glyph g : allGlyphs) {
            if (this.onlyFocused.get()) {
                Node nodeTranscript = DocumentPanel.this.groupTranscript
                        .lookup("#" + g.getLineID());
                if (!(g.getLineID() == null || g.getLineID().isEmpty())) {
                    if (nodeTranscript == null || !nodeTranscript.isFocused()) {
                        continue;
                    }
                }
            }
            if (g.isSpace) {
                continue;
            }
            Shape tmp = null;
            if (g.getLineID() == null || g.getLineID().isEmpty()) {
                tmp = g.getShapeToDraw(Color.RED);
            } else if (i % 2 == 0) {
                tmp = g.getShapeToDraw(Color.DARKGOLDENROD);
            } else {
                tmp = g.getShapeToDraw(Color.STEELBLUE);
            }
            ++i;
            final Shape glyphShape = tmp;

            if (glyphShape == null) {
                continue;
            }

            Tooltip.install(
                    (Node) glyphShape,
                    new Tooltip("Char: " + g.getGroupID() + "      ID: "
                            + g.getID() + " "));
            glyphShape
                    .setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                        @Override
                        public void handle(final ContextMenuEvent event) {
                            try {
                                DocumentPanel.this.contextGlyph = g;
                                final double screenX = event.getScreenX();
                                final double screenY = event.getScreenY();
                                DocumentPanel.this.glyphMenu.showMenu(
                                        DocumentPanel.this.root.getScene()
                                                .getWindow(), screenX, screenY);
                            } catch (final NumberFormatException e) {
                                DiptychonLogger
                                        .error("NumberFormatException in DocumentPanel. "
                                                + glyphShape.getId()
                                                + " cannot be casted to integer!");
                            }
                        }
                    });
            children.add(glyphShape);
        }
        if (this.splitLinesShowing) {
            this.splitLinesToFront();
        }
    }

    /**
     * This method is used to show glyphs.
     * 
     * @param allFragments
     *            arraylist contains all glyphs
     */
    private void showFragments(final ArrayList<Glyph> allFragments) {
        this.frags = allFragments;

        this.fragmentsShowing = true;
        this.clearFragments();
        Collections.sort(allFragments);
        if (allFragments.size() == Integer.valueOf(this.fragsNeeded.getValue())) {
            this.disableApply.set(false);
        } else {
            this.disableApply.set(true);
        }
        this.fragsLeft.set(Integer.toString(allFragments.size()));
        final ObservableList<Node> children = this.groupImage.getChildren();
        int i = 0;

        for (final Glyph g : allFragments) {
            Shape tmp = null;
            if (i % 4 == 0) {
                tmp = g.getShapeToDraw(Color.DARKGOLDENROD);
            } else if (i % 4 == 1) {
                tmp = g.getShapeToDraw(Color.DARKRED);
            } else if (i % 4 == 2) {
                tmp = g.getShapeToDraw(Color.DARKGREEN);
            } else {
                tmp = g.getShapeToDraw(Color.STEELBLUE);
            }
            /*
             * } else { if (i % 10 == 0) { tmp =
             * g.getShapeToDraw(Color.DARKRED); } else if (i % 10 == 1) { tmp =
             * g.getShapeToDraw(Color.DARKGOLDENROD); } else if (i % 10 == 2) {
             * tmp = g.getShapeToDraw(Color.DARKGREEN); } else if (i % 10 == 3)
             * { tmp = g.getShapeToDraw(Color.STEELBLUE); } else if (i % 10 ==
             * 4) { tmp = g.getShapeToDraw(Color.DARKSALMON); } else if (i % 10
             * == 5) { tmp = g.getShapeToDraw(Color.DARKKHAKI); } else if (i %
             * 10 == 6) { tmp = g.getShapeToDraw(Color.DARKOLIVEGREEN); } else
             * if (i % 10 == 7) { tmp = g.getShapeToDraw(Color.DARKORCHID); }
             * else if (i % 10 == 8) { tmp =
             * g.getShapeToDraw(Color.DARKSLATEBLUE); } else { tmp =
             * g.getShapeToDraw(Color.DARKMAGENTA); } }
             */

            ++i;
            final Shape glyphShape = tmp;

            if (glyphShape == null) {
                continue;
            }

            children.add(glyphShape);

            glyphShape
                    .setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                        @Override
                        public void handle(final ContextMenuEvent event) {
                            try {
                                DocumentPanel.this.contextFrag = g;
                                final double screenX = event.getScreenX();
                                final double screenY = event.getScreenY();
                                DocumentPanel.this.fragMenu.showMenu(
                                        DocumentPanel.this.root.getScene()
                                                .getWindow(), screenX, screenY);
                            } catch (final NumberFormatException e) {
                                DiptychonLogger
                                        .error("NumberFormatException in DocumentPanel. "
                                                + glyphShape.getId()
                                                + " cannot be casted to integer!");
                            }
                        }
                    });

            glyphShape.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent event) {
                    if (event.getButton().equals(MouseButton.SECONDARY)
                            || g.isSquareFragment) {
                        return;
                    }
                    DocumentPanel.this.lastFrags = new ArrayList<Glyph>();
                    for (Glyph f : DocumentPanel.this.frags) {
                        DocumentPanel.this.lastFrags.add(f.copy());
                        if (f == g) {
                            DocumentPanel.this.lastFrags
                                    .get(DocumentPanel.this.lastFrags.size() - 1).isFormerGlyph = false;
                        }
                    }
                    DocumentPanel.this.undoable = true;

                    DocumentPanel.this.documentPanelController.separateGlyph(g);

                    ArrayList<Glyph> newFrags = allFragments;
                    newFrags.remove(g);
                    if (g.isFormerGlyph) {
                        DocumentPanel.this.documentPanelController
                                .removeGlyphFragment(g.formerID);
                    }
                    newFrags.addAll(DocumentPanel.this.separatedFrags);
                    DocumentPanel.this.showFragments(newFrags);

                }
            });
            glyphShape.setOnMouseDragEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent event) {
                    glyphShape.setFill(DocumentPanel.this.fragMergeColor);
                    DocumentPanel.this.fragMergeList.add(g);
                    glyphShape.setOnMouseDragEntered(null);
                }
            });
            glyphShape.setOnDragDetected(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent event) {
                    glyphShape.startFullDrag();
                    DocumentPanel.this.fragMergeList.clear();
                    DocumentPanel.this.fragMergeColor = Color.GREENYELLOW;
                    glyphShape.setFill(DocumentPanel.this.fragMergeColor);
                    DocumentPanel.this.fragMergeList.add(g);
                    glyphShape.setOnMouseDragEntered(null);
                    glyphShape.setOnMouseClicked(null);
                }
            });
            DocumentPanel.this.getView().setOnMouseDragReleased(
                    new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(final MouseEvent event) {
                            DocumentPanel.this.lastFrags = new ArrayList<Glyph>();
                            for (Glyph f : DocumentPanel.this.frags) {
                                DocumentPanel.this.lastFrags.add(f.copy());
                                if (DocumentPanel.this.fragMergeList
                                        .contains(f)) {
                                    DocumentPanel.this.lastFrags
                                            .get(DocumentPanel.this.lastFrags
                                                    .size() - 1).isFormerGlyph = false;
                                }
                            }

                            DocumentPanel.this.undoable = true;

                            ArrayList<Glyph> newFrags = allFragments;
                            newFrags.removeAll(DocumentPanel.this.fragMergeList);
                            for (Glyph f : DocumentPanel.this.fragMergeList) {
                                if (f.isFormerGlyph) {
                                    DocumentPanel.this.documentPanelController
                                            .removeGlyphFragment(f.formerID);
                                }
                            }

                            for (int i = 1; i < DocumentPanel.this.fragMergeList
                                    .size(); i++) {
                                BoundingBox bb = DocumentPanel.this.fragMergeList
                                        .get(0).getBoundingBox();
                                BoundingBox bb2 = DocumentPanel.this.fragMergeList
                                        .get(i).getBoundingBox();

                                double minX, maxX, minY, maxY, minX2, maxX2, minY2, maxY2;

                                minX = bb.getMinX();
                                maxX = bb.getMaxX();
                                minY = bb.getMinY();
                                maxY = bb.getMaxY();

                                minX2 = bb2.getMinX();
                                maxX2 = bb2.getMaxX();
                                minY2 = bb2.getMinY();
                                maxY2 = bb2.getMaxY();

                                double newMinX, newMaxX, newMinY, newMaxY;

                                newMinX = Math.min(minX, minX2);
                                newMaxX = Math.max(maxX, maxX2);
                                newMinY = Math.min(minY, minY2);
                                newMaxY = Math.max(maxY, maxY2);

                                int newWidth = (int) (newMaxX - newMinX);
                                int newHeight = (int) (newMaxY - newMinY);

                                Rectangle bounds = new Rectangle(newMinX,
                                        newMinY, newWidth, newHeight);

                                DocumentPanel.this.documentPanelController
                                        .getCroppedImage(bounds);
                                DocumentPanel.this.fragMergeList
                                        .get(0)
                                        .merge(DocumentPanel.this.fragMergeList
                                                .get(i),
                                                DocumentPanel.this.croppedImage);
                            }
                            ++DiptychonPreferences.glyphMergingCounter;
                            DocumentPanel.this.fragMergeList.get(0).isFormerGlyph = false;
                            DocumentPanel.this.fragMergeList.get(0).isSquareFragment = false;
                            newFrags.add(DocumentPanel.this.fragMergeList
                                    .get(0));
                            DocumentPanel.this.showFragments(newFrags);
                        }
                    });
        }
        if (this.splitLinesShowing) {
            this.splitLinesToFront();
        }
    }

    /**
     * This method is used to show image lines in document. Additionally all
     * listener/functionality are/is added here.
     * 
     * @param lines
     *            collection of imagelines
     */
    private void showDocumentImageLines(final Collection<ImageLine> lines) {
        final ObservableList<Node> children = this.groupImage.getChildren();
        this.clearChildrensWithIDPrefix(Rectangle.class, ImageLine.ID_PREFIX);
        if (!this.showLines.get()) {
            return;
        }
        for (final ImageLine line : lines) {
            final Rectangle bounds = line.getBounds();
            bounds.focusTraversableProperty().set(true);
            final Rectangle innerbounds = line.getBounds();
            bounds.setId(line.getID());
            Tooltip.install((Node) bounds, new Tooltip(line.getID()));
            innerbounds.setId(line.getID());
            if (line.isHandwrittenAnnotation()) {
                bounds.setStyle(LINE_STYLE_ANNOTATION);
            } else {
                bounds.setStyle(LINE_STYLE);
            }
            innerbounds.setStyle(INNER_LINE_STYLE);
            children.add(bounds);
            bounds.setOnMouseEntered(new EventHandler<MouseEvent>() {

                @Override
                public void handle(final MouseEvent event) {
                    if (DocumentPanel.this.splitLinesShowing
                            || DocumentPanel.this.fragmentsShowing) {
                        return;
                    }
                    if (!bounds.getStyle().equals(LINE_STYLE_HIGHLIGHT)) {
                        bounds.setStyle(LINE_STYLE);
                    }
                }

            });
            bounds.setOnMouseMoved(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent event) {
                    final String id = bounds.getId();
                    Node nodeTranscript = DocumentPanel.this.groupTranscript
                            .lookup("#" + id);
                    if (nodeTranscript == null) {
                        nodeTranscript = DocumentPanel.this.groupTranscript
                                .lookup("#" + id + "_anno");
                    }
                    if (DocumentPanel.this.splitLinesShowing
                            || DocumentPanel.this.fragmentsShowing
                            || !nodeTranscript.isFocused()) {
                        return;
                    }
                    final Point2D point = DocumentPanel.this.documentImageView
                            .localToScene(DocumentPanel.this.documentImageView
                                    .getLayoutX(),
                                    DocumentPanel.this.documentImageView
                                            .getLayoutY());
                    final float scaleFactor = DocumentPanel.this.scale.get() / 100.f;
                    final int x = (int) ((event.getSceneX() - point.getX()) / scaleFactor);
                    final int y = (int) ((event.getSceneY() - point.getY()) / scaleFactor);
                    final int rectX1 = (int) bounds.getX();
                    final int rectY1 = (int) bounds.getY();
                    final int rectX2 = (int) bounds.getWidth() + rectX1;
                    final int rectY2 = (int) bounds.getHeight() + rectY1;

                    if (x > rectX1 + 5 && y > rectY1 + 5 && x < rectX2
                            && y < rectY2) {
                        if (!textlineDrag) {
                            DocumentPanel.this.getView().setCursor(
                                    Cursor.DEFAULT);

                            if (nodeTranscript != null) {
                                if (nodeTranscript.isFocused()) {
                                    bounds.setStyle(LINE_STYLE_HIGHLIGHT);
                                }
                            }
                        }
                    }
                    if (x <= rectX1 + 4) {
                        DocumentPanel.this.getView().setCursor(Cursor.W_RESIZE);
                        bounds.setStyle(LINE_STYLE_MOUSE_INSIDE);
                    } else if (y <= rectY1 + 4) {
                        DocumentPanel.this.getView().setCursor(Cursor.N_RESIZE);
                        bounds.setStyle(LINE_STYLE_MOUSE_INSIDE);
                    } else if (rectX2 - 3 < x && x <= rectX2 + 4) {
                        DocumentPanel.this.getView().setCursor(Cursor.E_RESIZE);
                        bounds.setStyle(LINE_STYLE_MOUSE_INSIDE);
                    } else if (rectY2 - 3 <= y && y <= rectY2 + 4) {
                        DocumentPanel.this.getView().setCursor(Cursor.S_RESIZE);
                        bounds.setStyle(LINE_STYLE_MOUSE_INSIDE);
                    } else if (!bounds.getStyle().equals(LINE_STYLE_HIGHLIGHT)) {
                        bounds.setStyle(LINE_STYLE);
                    }
                }
            });
            bounds.setOnDragDetected(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent event) {

                    children.add(innerbounds);

                    if (!children.contains(DocumentPanel.this.path)) {
                        children.add(DocumentPanel.this.path);
                    }

                    if (DocumentPanel.this.fragmentsShowing) {
                        DocumentPanel.this.path.getElements().clear();
                        DocumentPanel.this.path.getElements().add(
                                new MoveTo(event.getX(), event.getY()));
                        DocumentPanel.this.path.getElements().add(
                                new LineTo(event.getX(), event.getY()));
                    }
                }
            });
            bounds.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent event) {
                    if (DocumentPanel.this.fragmentsShowing) {

                        if (DocumentPanel.this.path.getElements().size() == 2) {
                            final LineTo lt = (LineTo) DocumentPanel.this.path
                                    .getElements().get(1);
                            lt.setX(event.getX());
                            lt.setY(event.getY());
                            DocumentPanel.this.path.toFront();
                        }
                    }
                    if (DocumentPanel.this.splitLinesShowing
                            || DocumentPanel.this.fragmentsShowing) {
                        return;
                    }
                    final Point2D point = DocumentPanel.this.documentImageView
                            .localToScene(DocumentPanel.this.documentImageView
                                    .getLayoutX(),
                                    DocumentPanel.this.documentImageView
                                            .getLayoutY());
                    final float scaleFactor = DocumentPanel.this.scale.get() / 100.f;
                    final int x = (int) ((event.getSceneX() - point.getX()) / scaleFactor);
                    final int y = (int) ((event.getSceneY() - point.getY()) / scaleFactor);

                    if (DocumentPanel.this.getView().getCursor() != null) {
                        if (DocumentPanel.this.getView().getCursor()
                                .equals(Cursor.N_RESIZE)) {
                            final double oldY = bounds.getY();
                            if (y < oldY + bounds.getHeight()) {
                                innerbounds.setStyle(INNER_LINE_DRAGGED);
                                bounds.setY(y);
                                innerbounds.setY(y);
                                bounds.setHeight(bounds.getHeight() + oldY - y);
                                innerbounds.setHeight(innerbounds.getHeight()
                                        + oldY - y);
                                DocumentPanel.this.textlineDrag = true;
                            }
                        } else if (DocumentPanel.this.getView().getCursor()
                                .equals(Cursor.S_RESIZE)) {
                            final double newHeight = y - bounds.getY();
                            if (newHeight > 0) {
                                innerbounds.setStyle(INNER_LINE_DRAGGED);
                                innerbounds.setHeight(newHeight);
                                bounds.setHeight(newHeight);
                                DocumentPanel.this.textlineDrag = true;
                            }
                        } else if (DocumentPanel.this.getView().getCursor()
                                .equals(Cursor.W_RESIZE)) {
                            final double oldX = bounds.getX();
                            if (x < oldX + bounds.getWidth()) {
                                innerbounds.setStyle(INNER_LINE_DRAGGED);
                                bounds.setX(x);
                                innerbounds.setX(x);
                                bounds.setWidth(bounds.getWidth() + oldX - x);
                                innerbounds.setWidth(innerbounds.getWidth()
                                        + oldX - x);
                                DocumentPanel.this.textlineDrag = true;
                            }
                        } else if (DocumentPanel.this.getView().getCursor()
                                .equals(Cursor.E_RESIZE)) {
                            final double newWidth = x - bounds.getX();
                            if (newWidth > 0) {
                                innerbounds.setStyle(INNER_LINE_DRAGGED);
                                bounds.setWidth(newWidth);
                                innerbounds.setWidth(newWidth);
                                DocumentPanel.this.textlineDrag = true;
                            }
                        }
                    }
                }
            });
            bounds.setOnMouseReleased(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent event) {
                    if (DocumentPanel.this.fragmentsShowing
                            && DocumentPanel.this.path.getElements().size() != 0) {
                        final int startX = (int) ((MoveTo) (DocumentPanel.this.path
                                .getElements().get(0))).getX();
                        final int startY = (int) ((MoveTo) (DocumentPanel.this.path
                                .getElements().get(0))).getY();
                        final int endX = (int) ((LineTo) (DocumentPanel.this.path
                                .getElements()).get(1)).getX();
                        final int endY = (int) ((LineTo) (DocumentPanel.this.path
                                .getElements()).get(1)).getY();
                        DiptychonLogger
                                .info("Using line split region from ({}/{}) to ({}/{})",
                                        startX, startY, endX, endY);

                        final int sx = startX < endX ? startX : endX;
                        final int ex = startX < endX ? endX : startX;
                        int x = sx;

                        final int sy = startY < endY ? startY : endY;
                        final int ey = startY < endY ? endY : startY;
                        int y = sy;

                        final double k = (double) (endY - startY)
                                / (double) ((endX - startX));
                        final int b = (int) (startY - k * startX);

                        ArrayList<ArrayList<Point2D>> sepLine = new ArrayList<ArrayList<Point2D>>();
                        ArrayList<Glyph> frags = new ArrayList<Glyph>();

                        for (int i = children.size() - 1; i >= 0; i--) {
                            x = sx;
                            y = sy;
                            final Node child = children.get(i);
                            if (child.getId() != null) {
                                if (child.getId().contains("F_")) {
                                    if (Shape.class.isInstance(child)) {
                                        ArrayList<Point2D> points = new ArrayList<Point2D>();
                                        if (endX == startX) {
                                            while (y <= ey) {
                                                if (((Shape) child)
                                                        .contains(((Shape) child)
                                                                .sceneToLocal(bounds
                                                                        .localToScene(
                                                                                x,
                                                                                y)))) {
                                                    // System.out.println("FRAGMENT "
                                                    // + child.getId() +
                                                    // " GESCHNITTEN");
                                                    points.add(((Shape) child)
                                                            .sceneToLocal(bounds
                                                                    .localToScene(
                                                                            x,
                                                                            y)));
                                                    for (Glyph f : DocumentPanel.this.frags) {
                                                        if (f.getID().equals(
                                                                child.getId())) {
                                                            if (!frags
                                                                    .contains(f)) {
                                                                frags.add(f);
                                                            }
                                                        }
                                                    }
                                                }
                                                y++;
                                            }
                                        } else {
                                            while (x <= ex) {
                                                y = (int) (k * x + b);
                                                if (((Shape) child)
                                                        .contains(((Shape) child)
                                                                .sceneToLocal(bounds
                                                                        .localToScene(
                                                                                x,
                                                                                y)))) {
                                                    // System.out.println("FRAGMENT "
                                                    // + child.getId() +
                                                    // " GESCHNITTEN");
                                                    points.add(((Shape) child)
                                                            .sceneToLocal(bounds
                                                                    .localToScene(
                                                                            x,
                                                                            y)));
                                                    for (Glyph f : DocumentPanel.this.frags) {
                                                        if (f.getID().equals(
                                                                child.getId())) {
                                                            if (!frags
                                                                    .contains(f)) {
                                                                frags.add(f);
                                                            }
                                                        }
                                                    }
                                                }

                                                x++;
                                            }
                                            y = sy;
                                            while (y <= ey) {
                                                x = (int) ((y - b) / k);
                                                if (((Shape) child)
                                                        .contains(((Shape) child)
                                                                .sceneToLocal(bounds
                                                                        .localToScene(
                                                                                x,
                                                                                y)))) {
                                                    // System.out.println("FRAGMENT "
                                                    // + child.getId() +
                                                    // " GESCHNITTEN");
                                                    points.add(((Shape) child)
                                                            .sceneToLocal(bounds
                                                                    .localToScene(
                                                                            x,
                                                                            y)));
                                                    for (Glyph f : DocumentPanel.this.frags) {
                                                        if (f.getID().equals(
                                                                child.getId())) {
                                                            if (!frags
                                                                    .contains(f)) {
                                                                frags.add(f);
                                                            }
                                                        }
                                                    }
                                                }

                                                y++;

                                            }
                                        }
                                        if (points.size() != 0) {
                                            sepLine.add(points);
                                        }
                                    }
                                }
                            }
                        }

                        DocumentPanel.this.lastFrags = new ArrayList<Glyph>();
                        for (Glyph f : DocumentPanel.this.frags) {
                            DocumentPanel.this.lastFrags.add(f.copy());
                            if (frags.contains(f)) {
                                DocumentPanel.this.lastFrags
                                        .get(DocumentPanel.this.lastFrags
                                                .size() - 1).isFormerGlyph = false;
                            }
                        }
                        DocumentPanel.this.undoable = true;

                        int i = 0;
                        ArrayList<Glyph> newFrags = DocumentPanel.this.frags;
                        /*
                         * if (event.getButton().equals(MouseButton.SECONDARY)
                         * || g.isSquareFragment) { return; }
                         * DocumentPanel.this.lastFrags = new
                         * ArrayList<Glyph>(); for (Glyph f :
                         * DocumentPanel.this.frags) {
                         * DocumentPanel.this.lastFrags.add(f.copy()); if (f ==
                         * g) {
                         * DocumentPanel.this.lastFrags.get(DocumentPanel.this
                         * .lastFrags.size()-1).isFormerGlyph = false; } }
                         * DocumentPanel.this.undoable = true;
                         * 
                         * DocumentPanel.this.documentPanelController.separateGlyph
                         * (g);
                         * 
                         * ArrayList<Glyph> newFrags = allFragments;
                         * newFrags.remove(g); if (g.isFormerGlyph) {
                         * DocumentPanel
                         * .this.documentPanelController.removeGlyphFragment
                         * (g.formerID); }
                         * newFrags.addAll(DocumentPanel.this.separatedFrags);
                         * DocumentPanel.this.showFragments(newFrags);
                         */
                        for (Glyph f : frags) {
                            DocumentPanel.this.documentPanelController
                                    .separateGlyph(f, sepLine.get(i));
                            newFrags.remove(f);
                            if (f.isFormerGlyph) {
                                // System.out.println("FORMER GLYPH REMOVED");
                                DocumentPanel.this.documentPanelController
                                        .removeGlyphFragment(f.formerID);
                            }

                            newFrags.addAll(DocumentPanel.this.separatedFrags);

                            DocumentPanel.this.showFragments(newFrags);
                            i++;
                        }

                        DocumentPanel.this.path.getElements().clear();
                    }
                    final String id = bounds.getId();
                    children.remove(innerbounds);
                    if (DocumentPanel.this.splitLinesShowing
                            || DocumentPanel.this.fragmentsShowing) {
                        return;
                    }

                    if (DocumentPanel.this.textlineDrag) {
                        DocumentPanel.this.documentPanelController
                                .updateTextlineSize(bounds);
                        DocumentPanel.this.textlineDrag = false;
                        // GEHT NICHT, JAVAFX BUG?
                        // bounds.toFront();
                        // bounds.requestFocus();
                        final Node nodeTranscript = DocumentPanel.this.groupTranscript
                                .lookup("#" + id);
                        ((TextField) nodeTranscript).setEditable(true);
                        ((TextField) nodeTranscript).requestFocus();
                        DocumentPanel.this.timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                ((TextField) nodeTranscript).selectAll();
                            }

                        }, 200);
                    }
                }
            });
            bounds.setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent event) {
                    if (DocumentPanel.this.splitLinesShowing
                            || DocumentPanel.this.fragmentsShowing) {
                        return;
                    }
                    if (!textlineDrag) {
                        final String id = bounds.getId();

                        final Node nodeTranscript = DocumentPanel.this.groupTranscript
                                .lookup("#" + id);
                        if (nodeTranscript != null) {
                            if (!nodeTranscript.isFocused()) {
                                bounds.setStyle(LINE_STYLE);
                            } else {
                                bounds.setStyle(LINE_STYLE_HIGHLIGHT);
                            }
                        }
                        DocumentPanel.this.getView().setCursor(Cursor.DEFAULT);
                    }
                }
            });
            bounds.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent event) {
                    if (event.getButton().equals(MouseButton.MIDDLE)) {
                        Point2D point = DocumentPanel.this.groupImage
                                .sceneToLocal(event.getSceneX(),
                                        event.getSceneY());
                        final double pivotX = point.getX()
                                * DocumentPanel.this.scale.getValue() / 100;
                        final double pivotY = point.getY()
                                * DocumentPanel.this.scale.getValue() / 100;

                        double paneWidth = DocumentPanel.this.anchorPaneImage
                                .getParent().getParent().getBoundsInParent()
                                .getWidth();
                        double paneHeight = DocumentPanel.this.anchorPaneImage
                                .getParent().getParent().getBoundsInParent()
                                .getHeight();
                        double groupWidth = DocumentPanel.this.groupImage
                                .getBoundsInParent().getWidth();
                        double groupHeight = DocumentPanel.this.groupImage
                                .getBoundsInParent().getHeight();

                        DocumentPanel.this.leftScroll.setVvalue(pivotY
                                / groupHeight - (paneHeight / 2) / groupHeight
                                + (paneHeight / groupHeight)
                                * (pivotY / groupHeight));
                        DocumentPanel.this.leftScroll.setHvalue(pivotX
                                / groupWidth - (paneWidth / 2) / groupWidth
                                + (paneWidth / groupWidth)
                                * (pivotX / groupWidth));

                        DocumentPanel.this.scale.set(250);
                        return;
                    } else if (DocumentPanel.this.splitLinesShowing
                            || DocumentPanel.this.fragmentsShowing) {
                        return;
                    }

                    final String id = bounds.getId();

                    Node nodeTranscript = DocumentPanel.this.groupTranscript
                            .lookup("#" + id);
                    if (nodeTranscript == null) {
                        nodeTranscript = DocumentPanel.this.groupTranscript
                                .lookup("#" + id + "_anno");
                    }
                    nodeTranscript.requestFocus();
                    bounds.toFront();
                    DocumentPanel.this.documentPanelController.showGlyphs();
                }

            });
            bounds.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                @Override
                public void handle(final ContextMenuEvent event) {
                    DocumentPanel.this.contextLine = bounds;
                    if (DocumentPanel.this.splitLinesShowing) {
                        return;
                    } else if (DocumentPanel.this.fragmentsShowing) {
                        if (bounds.getId().equals(DocumentPanel.this.RGFLineID)) {
                            if (DocumentPanel.this.undoable) {
                                DocumentPanel.this.fragLineMenu2.showMenu(
                                        DocumentPanel.this.root.getScene()
                                                .getWindow(), event
                                                .getScreenX(), event
                                                .getScreenY());
                            } else {
                                DocumentPanel.this.fragLineMenu.showMenu(
                                        DocumentPanel.this.root.getScene()
                                                .getWindow(), event
                                                .getScreenX(), event
                                                .getScreenY());
                            }
                        }
                    } else {
                        DocumentPanel.this.lineMenu.showMenu(
                                DocumentPanel.this.root.getScene().getWindow(),
                                event.getScreenX(), event.getScreenY());

                    }
                }
            });
        }
        if (this.splitLinesShowing) {
            this.splitLinesToFront();
        }
    }

    /**
     * This method is used to clear the child nodes with prefix id.
     * 
     * @param class1
     *            class template
     * @param prefix
     *            prefix id
     * @param <T>
     *            The class the node is supposed to be instance of
     */
    private <T> void clearChildrensWithIDPrefix(final Class<T> class1,
            final String... prefix) {
        final ObservableList<Node> children = this.groupImage.getChildren();
        for (int i = children.size() - 1; i >= 0; --i) {
            final Node child = children.get(i);
            for (final String s : prefix) {
                if (child.getId().contains(s)) {
                    if (class1.isInstance(child)) {
                        children.remove(i);
                    }
                }
            }
        }
    }

    private int getGlyphCount() {
        final ObservableList<Node> children = this.groupImage.getChildren();
        int count = 0;
        for (int i = children.size() - 1; i >= 0; --i) {
            final Node child = children.get(i);

            if (child.getId().contains(Glyph.ID_PREFIX)
                    || child.getId().contains(Alphabet.COLUMN_PREFIX)) {
                if (Shape.class.isInstance(child)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * This method is used to remove the child nodes with prefix id and returns
     * them.
     * 
     * @param prefix
     *            prefix id
     * @param class1
     *            class template
     * @param <T>
     *            The class the node is supposed to be instance of
     * @return removed child nodes
     */
    @SuppressWarnings("unchecked")
    private <T> ArrayList<T> getAndClearChildrensWithIDPrefix(
            final String prefix, final Class<T> class1) {
        final ObservableList<Node> children = this.groupImage.getChildren();
        final ArrayList<T> nodes = new ArrayList<>();
        for (int i = children.size() - 1; i >= 0; --i) {
            final Node child = children.get(i);
            if (child.getId().startsWith(prefix)) {
                nodes.add((T) child);
                children.remove(i);
            }
        }
        return nodes;
    }

    /**
     * This method is used to show the glyphs by template matching.
     * 
     * @param templateMatchingThreshold
     *            threshold of template matching
     * @param templateMatchingGlyphs
     *            pattern glyphs of template matching
     */
    private void showTemplateMatchingGlyphs(
            final int templateMatchingThreshold,
            final ArrayList<Glyph>[] templateMatchingGlyphs) {
        // System.out.println("DocumentPanel: showTemplateMatchingGlyphs");
        final ObservableList<Node> children = this.groupImage.getChildren();
        children.clear();
        children.add(this.documentImageView);
        children.add(this.rubberBand);

        for (int i = templateMatchingThreshold; i < templateMatchingGlyphs.length; ++i) {
            final ArrayList<Glyph> glyphs = templateMatchingGlyphs[i];
            for (final Glyph glyph : glyphs) {
                final Shape glyphShape = glyph
                        .getShapeToDraw(TemplateMatchingIDColor.ACCEPT
                                .getColor());
                if (glyph.getIDWithoutPrefix() != TemplateMatchingIDColor.ACCEPT
                        .getID()) {
                    glyphShape.setFill(TemplateMatchingIDColor.DENY.getColor());
                }

                children.add(glyphShape);
                glyphShape.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(final MouseEvent event) {
                        final int id = TemplateMatchingIDColor.invertID(glyph
                                .getIDWithoutPrefix());
                        glyph.setID(id);
                        glyphShape.setFill(TemplateMatchingIDColor.getColor(id));
                    }
                });
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void modelPropertyChange(final PropertyChangeEvent evt) {
        final String name = evt.getPropertyName();
        if (name.equals(DocumentPanelController.OPEN_IMAGE)) {
            this.setImageView((Image) evt.getNewValue());
        } else if (name.equals(DocumentPanelController.RELOAD_IMAGE)) {
            final Image image = (Image) evt.getNewValue();
            if (this.documentImageView.getImage().getWidth() != image
                    .getWidth()) {
                this.setImageView(image);
            } else {
                this.documentImageView.setImage(image);
            }
        } else if (name.equals(DocumentPanelController.UPDATE_GLYPH_ANNO)) {
            final Object[] elements = (Object[]) evt.getNewValue();
            ArrayList<Glyph> allGlyphs = (ArrayList<Glyph>) elements[0];
            for (final Glyph g : allGlyphs) {
                Node nodeTranscript = DocumentPanel.this.groupTranscript
                        .lookup("#" + g.getLineID() + "_anno");
                if (nodeTranscript != null) {
                    g.isAnnotation = true;
                }

            }
        }

        else if (name.equals(DocumentPanelController.SHOW_GLYPH_EDITOR_NEW)) {
            this.resetRubberband();
            final Glyph g = (Glyph) evt.getNewValue();
            final GlyphDialog glyphDialog = (GlyphDialog) new A_Dialog.Factory()
                    .createDialog(GlyphDialog.class.getName(),
                            this.documentPanelController);
            glyphDialog.setNewGlyph(g);
            glyphDialog.showModalDialog(this.root.getScene().getWindow(), -1,
                    -1);
        } else if (name.equals(DocumentPanelController.SET_CROPPED_IMAGE)) {
            this.croppedImage = (Image) evt.getNewValue();
        } else if (name.equals(DocumentPanelController.ZOOM_FIT)) {
            double paneHeight = this.anchorPaneImage.getParent()
                    .getBoundsInParent().getHeight();

            this.scale.setValue(paneHeight
                    / this.groupImage.getBoundsInLocal().getHeight() * 100);
        } else if (name.equals(DocumentPanelController.CANCEL_TIMER)) {
            this.timer.cancel();
        } else if (name.equals(DocumentPanelController.SHOW_GLYPH_EDITOR)) {
            this.resetRubberband();
            final Glyph g = (Glyph) evt.getNewValue();
            final GlyphDialog glyphDialog = (GlyphDialog) new A_Dialog.Factory()
                    .createDialog(GlyphDialog.class.getName(),
                            this.documentPanelController);
            glyphDialog.setGlyphToEdit(g);
            glyphDialog.showModalDialog(this.root.getScene().getWindow(), -1,
                    -1);
        } else if (name.equals(DocumentPanelController.SHOW_BINARIZE_DIALOG)) {
            final Object[] values = (Object[]) evt.getNewValue();
            final BinarizeDialog bd = (BinarizeDialog) new A_Dialog.Factory()
                    .createDialog(BinarizeDialog.class.getName(),
                            this.documentPanelController);
            bd.initSlider((double) values[0], (int) values[1]);
            bd.showNormalDialog(this.root.getScene().getWindow(), -1, -1);
        } else if (name
                .equals(DocumentPanelController.BINARIZED_BUTTON_PRESSED)) {
            this.resetRubberband();
            DocumentPanel.this.documentPanelController.getGlyphsAndLines();
        } else if (name
                .equals(DocumentPanelController.SHOW_BINARIZE_DIALOG_MASK)) {
            final Object[] values = (Object[]) evt.getNewValue();
            final BinarizeDialog bd = (BinarizeDialog) new A_Dialog.Factory()
                    .createDialog(BinarizeDialog.class.getName(),
                            this.documentPanelController);
            bd.initSlider((double) values[0], (int) values[1]);
            bd.setMask(new Rectangle(
                    DocumentPanel.this.rubberBand.getLayoutX(),
                    DocumentPanel.this.rubberBand.getLayoutY(),
                    DocumentPanel.this.rubberBand.getWidth(),
                    DocumentPanel.this.rubberBand.getHeight()));
            bd.showNormalDialog(this.root.getScene().getWindow(), -1, -1);
        } else if (name.equals(DocumentPanelController.SHOW_GRAY_SCALE_DIALOG)) {
            final double[] values = (double[]) evt.getNewValue();
            final GrayScaleDialog gd = (GrayScaleDialog) new A_Dialog.Factory()
                    .createDialog(GrayScaleDialog.class.getName(),
                            this.documentPanelController);
            gd.initSlider(values);
            gd.showNormalDialog(this.root.getScene().getWindow(), -1, -1);
        } else if (name.equals(DocumentPanelController.SHOW_GALLERY_DIALOG)) {
            final ArrayList<Glyph> glyphs = (ArrayList<Glyph>) evt
                    .getNewValue();
            this.sortGlyphs(glyphs);
            final GalleryDialog ggd = (GalleryDialog) new A_Dialog.Factory()
                    .createDialog("GalleryDialog", this.documentPanelController);
            ggd.initGlyphs(glyphs, this.showSearchResult);
            ggd.showNormalDialog(this.root.getScene().getWindow(), -1, -1);
        } else if (name
                .equals(DocumentPanelController.SHOW_TEMPLATE_MATCHING_GLYPHS)) {
            // System.out.println("DocumentPanel:modelPropertyChange 4a");
            this.resetRubberband();
            final ArrayList<Glyph>[] templateMatchingGlyphs = (ArrayList<Glyph>[]) evt
                    .getNewValue();
            final TemplateMatchingThresholdDialog templateMatchingThresholdDialog = (TemplateMatchingThresholdDialog) new A_Dialog.Factory()
                    .createDialog(
                            TemplateMatchingThresholdDialog.class.getName(),
                            this.documentPanelController);
            final IntegerProperty templateMatchingThreshold = new SimpleIntegerProperty(
                    TemplateMatchingThresholdDialog.MIN);
            templateMatchingThreshold.bind(templateMatchingThresholdDialog
                    .getAccuracyProperty());
            this.addTemplateMatchingTresholdInvalidationListener(
                    templateMatchingThreshold, templateMatchingGlyphs);
            final IntegerProperty templateMatchingAcceptance = new SimpleIntegerProperty(
                    2);
            templateMatchingAcceptance.bind(templateMatchingThresholdDialog
                    .getAcceptanceProperty());
            this.addTemplateMatchingAcceptanceInvalidationListener(
                    templateMatchingAcceptance, templateMatchingThreshold,
                    templateMatchingGlyphs);
            this.showTemplateMatchingGlyphs(templateMatchingThreshold.get(),
                    templateMatchingGlyphs);
            templateMatchingThresholdDialog.showNormalDialog(this.root
                    .getScene().getWindow(), -1, -1);
            // System.out.println("DocumentPanel:modelPropertyChange 4b");
        }
        // else if
        // (name.equals(DocumentPanelController.SHOW_TEMPLATE_MATCHING_GLYPHS_SINGLE_LINE))
        // {
        // final ArrayList<Glyph> templateMatchingGlyphs = (ArrayList<Glyph>)
        // evt.getNewValue();
        // /*
        // * if (templateMatchingGlyphs != null) {
        // this.showTemplateMatchingGlyphsSingleLine(templateMatchingGlyphs);
        // * }
        // */
        // this.documentPanelController.acceptGlyphs(templateMatchingGlyphs);
        // }
        else if (name.equals(DocumentPanelController.SHOW_ELEMENTS)) {
            // System.out.println("DocumentPanel:modelPropertyChange 1");
            this.resetRubberband();
            final Object[] elements = (Object[]) evt.getNewValue();
            this.showDocumentImageLines((Collection<ImageLine>) elements[0]);
            this.showGlyphs((ArrayList<Glyph>) elements[1]);
        } else if (name.equals(DocumentPanelController.SHOW_GLYPHS)) {
            // System.out.println("DocumentPanel:modelPropertyChange 2a");
            this.resetRubberband();
            final Object[] elements = (Object[]) evt.getNewValue();
            this.showGlyphs((ArrayList<Glyph>) elements[0]);
            // System.out.println("DocumentPanel:modelPropertyChange 2b");
        } else if (name.equals(DocumentPanelController.SHOW_FRAGMENTS)) {
            this.resetRubberband();
            final Object[] elements = (Object[]) evt.getNewValue();
            this.showDocumentImageLines((Collection<ImageLine>) elements[0]);
            this.showFragments((ArrayList<Glyph>) elements[1]);
        } else if (name.equals(DocumentPanelController.SHOW_SEPARATED_FRAGS)) {
            final Object[] elements = (Object[]) evt.getNewValue();
            this.separatedFrags = ((ArrayList<Glyph>) elements[0]);
        } else if (name.equals(DocumentPanelController.SHOW_PAGE_LINES)) {
            this.resetRubberband();
            this.showDocumentPageLines((Collection<PageLine>) evt.getNewValue());
        } else if (name.equals(DocumentPanelController.ADD_TEXTLINE)) {
            this.addTextline((Rectangle) evt.getNewValue(), false);
        } else if (name.equals(DocumentPanelController.UPDATE_DOCUMENT_PAGE)) {
            this.showDocumentPageLines((Collection<PageLine>) evt.getNewValue());
        } else if (name.equals(DocumentPanelController.SET_TRANSCRIPTION)) {
            final Object[] transcriptionLineID = (Object[]) evt.getNewValue();
            final String[] transcription = (String[]) transcriptionLineID[0];
            final String lineID = (String) transcriptionLineID[1];
            final TextField tf = (TextField) this.groupTranscript.lookup("#"
                    + lineID);
            if (tf == null) {
                return;
            }
            tf.setText("");
            for (final String s : transcription) {
                tf.appendText(s);
                tf.setEditable(false);
            }
            final double prefLength = this.calcTextfieldWidth(
                    (int) tf.getLayoutX(), (int) tf.getMaxWidth(),
                    tf.getLength());
            if (tf.getPrefWidth() < prefLength) {
                tf.setPrefWidth(prefLength);
                final String id = tf.getId();
                final Line lowerLine;
                if (id.endsWith("_anno")) {
                    lowerLine = (Line) this.groupTranscript.lookup("#L"
                            + id.subSequence(0, id.length() - 5));
                } else {
                    lowerLine = (Line) this.groupTranscript.lookup("#L" + id);
                }
                lowerLine.setEndX(lowerLine.getStartX() + prefLength);
            }
        } else if (name
                .equals(DocumentPanelController.INIT_SPLIT_SUGGESTION_DIALOG)) {
            final Object[] splitsAndId = (Object[]) evt.getNewValue();
            final Line[] splits = (Line[]) splitsAndId[0];
            this.RGFLineID = (String) splitsAndId[1];
            if (!this.splitLine.getValue()) {
                DocumentPanel.this.resizeToLine(
                        DocumentPanel.this.anchorPaneImage,
                        DocumentPanel.this.groupImage, (String) splitsAndId[1]);
                final ArrayList<Line> newSplits = new ArrayList<Line>();
                newSplits.add(splits[splits.length - 1]);
                newSplits.add(splits[0]);

                final FragEditingDialog fed = (FragEditingDialog) new A_Dialog.Factory()
                        .createDialog("FragEditingDialog",
                                DocumentPanel.this.documentPanelController);

                DocumentPanel.this.fragsLeft = new SimpleStringProperty("0");
                DocumentPanel.this.fragsLeft.bindBidirectional(fed
                        .getFragsLeftProperty());
                DocumentPanel.this.disableApply = new SimpleBooleanProperty(
                        false);
                DocumentPanel.this.disableApply.bindBidirectional(fed
                        .getApplyDisableProperty());

                DocumentPanel.this.documentPanelController
                        .splitImageLineApply2(newSplits,
                                (String) splitsAndId[1],
                                this.fragSize.getValue(),
                                this.fragCount.getValue(),
                                this.noiseSize.getValue());
                DocumentPanel.this.splitLinesShowing = false;

                DocumentPanel.this.addFragEditingAcceptanceListener(
                        fed.getAcceptedProperty(), (String) splitsAndId[1]);

                final Node node = DocumentPanel.this.groupTranscript.lookup("#"
                        + (String) splitsAndId[1]);
                DocumentPanel.this.fragEditingListener = new InvalidationListener() {
                    @Override
                    public void invalidated(final Observable observable) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Point2D point = ((TextField) node)
                                        .getInputMethodRequests()
                                        .getTextLocation(
                                                ((TextField) node)
                                                        .getCaretPosition());
                                double width = DocumentPanel.this.root
                                        .getScene().getWindow().getWidth();
                                int fontsize = DocumentPanel.this.fontsize
                                        .get();
                                int scale = DocumentPanel.this.scale.get();
                                double rightscroll = DocumentPanel.this.rightScroll
                                        .getHvalue();
                                if (point.getX() + fontsize * (scale / 99) > width) {
                                    DocumentPanel.this.rightScroll
                                            .setHvalue(rightscroll
                                                    + (fontsize * (scale / 100)
                                                            + 50 - (scale / 5))
                                                    / width);
                                }
                            }
                        });

                        String text = ((TextField) node).getText();
                        text = text.replaceAll(" ", "");
                        text = text.replaceAll("\\$([\\s\\S]*?)\\$", "x");
                        DocumentPanel.this.fragsNeeded.setValue(text.length()
                                + "");
                        if (DocumentPanel.this.frags.size() == Integer
                                .valueOf(DocumentPanel.this.fragsNeeded
                                        .getValue())) {
                            DocumentPanel.this.disableApply.set(false);

                        } else {
                            DocumentPanel.this.disableApply.set(true);
                        }
                    }
                };
                ((TextField) node).textProperty().addListener(
                        DocumentPanel.this.fragEditingListener);
                String text = ((TextField) node).getText();
                text = text.replaceAll(" ", "");
                text = text.replaceAll("\\$([\\s\\S]*?)\\$", "x");
                fed.getCharsProperty().bindBidirectional(this.fragsNeeded);
                DocumentPanel.this.fragsNeeded.setValue(text.length() + "");

                final Rectangle2D bounds = Screen.getPrimary()
                        .getVisualBounds();

                fed.showNormalDialog(DocumentPanel.this.root.getScene()
                        .getWindow(), (int) (bounds.getWidth() - 400),
                        (int) (bounds.getHeight() - bounds.getMinY() - 250));

            } else {

                if (splits == null) {
                    final InformationDialog id = new InformationDialog.Factory()
                            .createDialogWithExclamationMark(
                                    "Could not find possible solution. Try bigger width!",
                                    Pos.CENTER, 250);
                    id.showDialog(this.root.getScene().getWindow(), -1, -1);
                } else {
                    this.showSplitLinesRect(splits);
                }

                final SplitLineDialog sld = (SplitLineDialog) new A_Dialog.Factory()
                        .createDialog("SplitLineDialog",
                                this.documentPanelController);
                this.addSplitLineAcceptanceListener(sld.getAcceptedProperty(),
                        (String) splitsAndId[1]);

                final Rectangle2D bounds = Screen.getPrimary()
                        .getVisualBounds();

                sld.showNormalDialog(
                        this.root.getScene().getWindow(),
                        (int) ((bounds.getWidth() - bounds.getMinX()) / 2 - 225),
                        (int) (bounds.getHeight() - bounds.getMinY() - 250));

            }
        } else if (name
                .equals(DocumentPanelController.UPDATE_SPLIT_LINE_SUGGESTION_DIALOG)) {
            this.clearChildrensWithIDPrefix(Rectangle.class, "Split_");

            final Object[] splitsAndId = (Object[]) evt.getNewValue();
            final Line[] splits = (Line[]) splitsAndId[0];
            if (splits == null) {
                final InformationDialog id = new InformationDialog.Factory()
                        .createDialogWithExclamationMark(
                                "Could not find possible solution. Try smaller width!",
                                Pos.CENTER_RIGHT, 250);
                id.showDialog(this.root.getScene().getWindow(), -1, -1);
            } else {
                this.showSplitLinesRect(splits);
            }
        } else if (name.equals(DocumentPanelController.SEARCH_FOR_WORD)) {
            final ArrayList<Glyph[]> foundWords = (ArrayList<Glyph[]>) evt
                    .getNewValue();
            this.addShowResultInvalidationListener(foundWords);
            this.numOfResults.set(foundWords.size());
            this.showSearchResult.set(0);
        }
    }

    /**
     * This method is used to attach the result with invalidation listeners.
     * 
     * @param foundWords
     *            arraylist of glyph
     */
    private void addShowResultInvalidationListener(
            final ArrayList<Glyph[]> foundWords) {
        if (this.findWordInvalidationListener != null) {
            this.showSearchResult
                    .removeListener(this.findWordInvalidationListener);
        }
        this.findWordInvalidationListener = new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                final int index = DocumentPanel.this.showSearchResult.get();
                if (index == -2) {
                    final ObservableList<Node> children = DocumentPanel.this.groupImage
                            .getChildren();
                    children.clear();
                    children.add(DocumentPanel.this.documentImageView);
                    children.add(DocumentPanel.this.rubberBand);
                    DocumentPanel.this.documentPanelController
                            .getGlyphsAndLines();
                    return;
                }
                if (index == -3) {
                    final ObservableList<Node> children = DocumentPanel.this.groupImage
                            .getChildren();
                    children.clear();
                    children.add(DocumentPanel.this.documentImageView);
                    children.add(DocumentPanel.this.rubberBand);
                } else if (index < 0 || foundWords.size() == 0
                        || index >= foundWords.size()) {
                    return;
                } else {
                    DocumentPanel.this.showSearchResults(foundWords.get(index));
                }
            }
        };
        this.showSearchResult.addListener(this.findWordInvalidationListener);
    }

    /**
     * This method is used to show search results.
     * 
     * @param word
     *            a set of glyphs
     */
    private void showSearchResults(final Glyph[] word) {
        final ObservableList<Node> children = this.groupImage.getChildren();
        children.clear();
        children.add(this.documentImageView);
        children.add(this.rubberBand);
        int tmpID = 0;
        final Color fill = Color.ORANGE;

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;

        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (final Glyph g : word) {
            final Shape glyphShape = g.getShapeToDraw(fill);
            glyphShape.setId("SearchResult_" + tmpID);
            children.add(glyphShape);
            final Node node = this.groupImage.lookup("#SearchResult_" + tmpID);

            final int xMin = (int) node.getBoundsInParent().getMinX();
            final int yMin = (int) node.getBoundsInParent().getMinY();

            final int xMax = (int) node.getBoundsInParent().getMaxX();
            final int yMax = (int) node.getBoundsInParent().getMaxY();

            minX = Math.min(xMin, minX);
            minY = Math.min(yMin, minY);

            maxX = Math.max(xMax, maxX);
            maxY = Math.max(yMax, maxY);

            ++tmpID;
        }

        final int pivotX = (minX + (maxX - minX) / 2) * this.scale.getValue()
                / 100;
        final int pivotY = (minY + (maxY - minY) / 2) * this.scale.getValue()
                / 100;

        double paneWidth = this.anchorPaneImage.getParent().getParent()
                .getBoundsInParent().getWidth();
        double paneHeight = this.anchorPaneImage.getParent().getParent()
                .getBoundsInParent().getHeight();
        double groupWidth = this.groupImage.getBoundsInParent().getWidth();
        double groupHeight = this.groupImage.getBoundsInParent().getHeight();

        this.leftScroll.setVvalue(pivotY / groupHeight - (paneHeight / 2)
                / groupHeight + (paneHeight / groupHeight)
                * (pivotY / groupHeight));
        this.leftScroll
                .setHvalue(pivotX / groupWidth - (paneWidth / 2) / groupWidth
                        + (paneWidth / groupWidth) * (pivotX / groupWidth));
    }

    /**
     * This method is used to attach the fragment editing acceptance with
     * invalidation listeners.
     * 
     * @param accepted
     *            property of accepted
     */
    private void addFragEditingAcceptanceListener(
            final IntegerProperty accepted, final String lineID) {
        final Node node = DocumentPanel.this.groupTranscript.lookup("#"
                + lineID);
        ((TextField) node).setEditable(true);
        final Node node2 = DocumentPanel.this.groupImage.lookup("#"
                + this.RGFLineID);
        node2.toFront();
        this.showFragments(this.frags);
        accepted.addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                final Node node = DocumentPanel.this.groupTranscript.lookup("#"
                        + lineID);
                ((TextField) node).textProperty().removeListener(
                        DocumentPanel.this.fragEditingListener);
                if (accepted.get() == 1) {
                    DocumentPanel.this.undoable = false;
                    DocumentPanel.this.fragmentsShowing = false;
                    DocumentPanel.this.clearFragments();

                    String text = ((TextField) node).getText();

                    ArrayList<String> trans = new ArrayList<String>();
                    for (int i = 0; i < text.length(); i++) {
                        if (String.valueOf(text.charAt(i)).equals("$")) {
                            trans.add(text.substring(i + 1,
                                    text.indexOf("$", i + 1)));
                            i = text.indexOf("$", i + 1);
                        } else {
                            trans.add(String.valueOf(text.charAt(i)));
                        }
                    }

                    DocumentPanel.this.documentPanelController
                            .removeSpacesFromLine(lineID);

                    ArrayList<int[]> spaces = new ArrayList<int[]>();

                    for (int i = 0; i < trans.size(); i++) {
                        if (trans.get(i).equals(" ")) {
                            Glyph lFrag = DocumentPanel.this.frags.get(i - 1
                                    - spaces.size());
                            Glyph rFrag = DocumentPanel.this.frags.get(i
                                    - spaces.size());

                            int spaceX = (lFrag.getLayoutX() + lFrag.getWidth() + rFrag
                                    .getLayoutX()) / 2;
                            int spaceY = (lFrag.getLayoutY()
                                    + rFrag.getLayoutY() + rFrag.getHeight()) / 2;

                            int[] spaceCoords = { spaceX, spaceY };

                            spaces.add(spaceCoords);

                        } else {
                            DocumentPanel.this.frags.get(i - spaces.size())
                                    .setGroupID(trans.get(i));
                        }
                    }
                    DocumentPanel.this.documentPanelController.extractSpaces(
                            spaces, lineID);

                    ArrayList<Glyph> fragsToRemove = new ArrayList<Glyph>();

                    for (Glyph g : frags) {
                        if (g.isFormerGlyph) {
                            g.setID(g.formerID);
                            fragsToRemove.add(g);
                        }
                    }

                    frags.removeAll(fragsToRemove);

                    DocumentPanel.this.documentPanelController.acceptFragments(
                            DocumentPanel.this.frags, lineID);
                    ((TextField) node).setEditable(false);
                } else if (accepted.get() == 0) {
                    DocumentPanel.this.fragmentsShowing = false;
                    DocumentPanel.this.clearFragments();
                    DocumentPanel.this.documentPanelController
                            .removeLineEncodingFromAlphabet(lineID);
                    DocumentPanel.this.documentPanelController.showElements();
                    ((TextField) node).setEditable(true);
                }
            }
        });

    }

    /**
     * This method is used to attach the splitline acceptance with invalidation
     * listeners.
     * 
     * @param accepted
     *            property of accepted
     * @param lineID
     *            line ID
     */
    private void addSplitLineAcceptanceListener(final IntegerProperty accepted,
            final String lineID) {
        this.splitLinesShowing = true;
        accepted.addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                if (accepted.get() >= 5) {
                    DocumentPanel.this.documentPanelController
                            .splitImageLinePreview2(lineID, accepted.get());
                } else if (accepted.get() == 1) {
                    DocumentPanel.this.resizeToLine(
                            DocumentPanel.this.anchorPaneImage,
                            DocumentPanel.this.groupImage, lineID);

                    final ArrayList<Rectangle> splitsTmp = DocumentPanel.this
                            .getAndClearChildrensWithIDPrefix("Split",
                                    Rectangle.class);

                    final ArrayList<Line> splits = new ArrayList<>(splitsTmp
                            .size());

                    for (final Rectangle rect : splitsTmp) {
                        final Line l = new Line(rect.getX() + 1, rect.getY(),
                                rect.getX() + 1, rect.getY() + rect.getHeight());
                        l.setId(rect.getId());
                        splits.add(l);
                    }
                    final FragEditingDialog fed = (FragEditingDialog) new A_Dialog.Factory()
                            .createDialog("FragEditingDialog",
                                    DocumentPanel.this.documentPanelController);

                    DocumentPanel.this.fragsLeft = new SimpleStringProperty("0");
                    DocumentPanel.this.fragsLeft.bindBidirectional(fed
                            .getFragsLeftProperty());
                    DocumentPanel.this.disableApply = new SimpleBooleanProperty(
                            false);
                    DocumentPanel.this.disableApply.bindBidirectional(fed
                            .getApplyDisableProperty());

                    DocumentPanel.this.documentPanelController
                            .splitImageLineApply2(splits, lineID,
                                    DocumentPanel.this.fragSize.getValue(),
                                    DocumentPanel.this.fragCount.getValue(),
                                    DocumentPanel.this.noiseSize.getValue());
                    DocumentPanel.this.splitLinesShowing = false;

                    DocumentPanel.this.addFragEditingAcceptanceListener(
                            fed.getAcceptedProperty(), lineID);

                    final Node node = DocumentPanel.this.groupTranscript
                            .lookup("#" + lineID);

                    DocumentPanel.this.fragEditingListener = new InvalidationListener() {
                        @Override
                        public void invalidated(final Observable observable) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    Point2D point = ((TextField) node)
                                            .getInputMethodRequests()
                                            .getTextLocation(
                                                    ((TextField) node)
                                                            .getCaretPosition());
                                    double width = DocumentPanel.this.root
                                            .getScene().getWindow().getWidth();
                                    int fontsize = DocumentPanel.this.fontsize
                                            .get();
                                    int scale = DocumentPanel.this.scale.get();
                                    double rightscroll = DocumentPanel.this.rightScroll
                                            .getHvalue();
                                    if (point.getX() + fontsize * (scale / 99) > width) {
                                        DocumentPanel.this.rightScroll.setHvalue(rightscroll
                                                + Math.abs(((point.getX() - width) + 50)
                                                        / width));
                                    }
                                }
                            });
                            String text = ((TextField) node).getText();
                            text = text.replaceAll(" ", "");
                            text = text.replaceAll("\\$([\\s\\S]*?)\\$", "x");
                            DocumentPanel.this.fragsNeeded.setValue(text
                                    .length() + "");
                            if (DocumentPanel.this.frags.size() == Integer
                                    .valueOf(DocumentPanel.this.fragsNeeded
                                            .getValue())) {
                                DocumentPanel.this.disableApply.set(false);

                            } else {
                                DocumentPanel.this.disableApply.set(true);
                            }
                        }
                    };
                    ((TextField) node).textProperty().addListener(
                            DocumentPanel.this.fragEditingListener);
                    String text = ((TextField) node).getText();
                    text = text.replaceAll(" ", "");
                    text = text.replaceAll("\\$([\\s\\S]*?)\\$", "x");
                    fed.getCharsProperty().bindBidirectional(
                            DocumentPanel.this.fragsNeeded);
                    DocumentPanel.this.fragsNeeded.setValue(text.length() + "");
                    fed.getCharsProperty().setValue(
                            Integer.toString(text.length()));

                    final Rectangle2D bounds = Screen.getPrimary()
                            .getVisualBounds();

                    fed.showNormalDialog(DocumentPanel.this.root.getScene()
                            .getWindow(), (int) ((bounds.getWidth() - bounds
                            .getMinX()) / 2 - 225), (int) (bounds.getHeight()
                            - bounds.getMinY() - 350));

                } else {
                    DocumentPanel.this.clearChildrensWithIDPrefix(
                            Rectangle.class, "Split_");
                    DocumentPanel.this.splitLinesShowing = false;
                }
            }
        });
    }

    /**
     * This method is used to put children with split lines to front.
     */
    private void splitLinesToFront() {
        final ObservableList<Node> children = this.groupImage.getChildren();

        for (int i = children.size() - 1; i > 0; --i) {
            final Node child = children.get(i);
            if (child.getId().startsWith("Split_")) {
                child.toFront();
            }
        }
    }

    /**
     * This method is used to show split line rectangle.
     * 
     * @param splits
     *            lines of splits
     */
    private void showSplitLinesRect(final Line[] splits) {
        final ObservableList<Node> children = this.groupImage.getChildren();
        for (int i = 0; i < splits.length; ++i) {
            final Line l = splits[i];
            final Rectangle r = new Rectangle(l.getStartX() - 1, l.getStartY(),
                    3, l.getEndY() - l.getStartY());
            r.setStyle(SPLIT_LINE_STYLE);
            r.setStroke(l.getStroke());
            r.setId("Split_" + i);
            final double _startX = l.getStartX();
            r.setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent event) {
                    DocumentPanel.this.root.setCursor(Cursor.OPEN_HAND);
                }
            });
            r.setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent event) {
                    DocumentPanel.this.root.setCursor(Cursor.DEFAULT);
                }
            });
            r.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(final MouseEvent event) {
                    DocumentPanel.this.root.setCursor(Cursor.CLOSED_HAND);
                    final Point2D point = DocumentPanel.this.documentImageView
                            .localToScene(DocumentPanel.this.documentImageView
                                    .getLayoutX(),
                                    DocumentPanel.this.documentImageView
                                            .getLayoutY());
                    final float scaleFactor = DocumentPanel.this.scale.get() / 100.f;
                    final int leftX = (int) ((event.getSceneX() - point.getX()) / scaleFactor);
                    final int rightX = (int) ((event.getSceneX() - point.getX() + 3) / scaleFactor);
                    final double[] leftRight = DocumentPanel.this
                            .getLeftRightBound(r.getId());
                    double leftBound = leftRight[0];
                    double rightBound = leftRight[1];
                    if (leftBound == -1) {
                        leftBound = _startX - 1;
                    }
                    if (rightBound == -1) {
                        rightBound = _startX + 1;
                    }

                    if (leftBound <= leftX && rightX <= rightBound) {
                        r.setX(leftX);
                    }
                }
            });
            r.toFront();
            children.add(r);
        }
    }

    /**
     * This method is used to get left and right bound of the node with ID sid.
     * 
     * @param sId
     *            The id of the node
     * @return the left and right bound of the node
     */
    private double[] getLeftRightBound(final String sId) {
        final double[] leftRight = new double[] { -1, -1 };
        final String[] idSplit = sId.split("_");
        final int id = Integer.valueOf(idSplit[idSplit.length - 1]);
        final ObservableList<Node> children = this.groupImage.getChildren();
        for (final Node child : children) {
            final String cId = child.getId();
            if (cId.startsWith("Split")) {
                final String[] tmp = cId.split("_");
                final int tmpID = Integer.valueOf(tmp[tmp.length - 1]);
                if (id - tmpID == 1) {
                    final Rectangle rect = (Rectangle) child;
                    leftRight[0] = rect.getX() + rect.getWidth() + 2;
                }
                if (id - tmpID == -1) {
                    final Rectangle rect = (Rectangle) child;
                    leftRight[1] = rect.getX() - 2;
                }
            }
        }
        return leftRight;
    }

    /**
     * This method is used to show document page lines.
     * 
     * @param pageLines
     *            collection of pagelines
     */
    private void showDocumentPageLines(final Collection<PageLine> pageLines) {
        final ObservableList<Node> children = this.groupTranscript
                .getChildren();
        children.clear();
        children.add(this.transcriptBackground);

        for (final PageLine pageLine : pageLines) {
            final Rectangle rectangle = pageLine.getBounds();
            boolean annotation = false;
            if (pageLine.isHandwrittenAnnotation()) {
                annotation = true;
            }
            rectangle.setId(pageLine.getID());
            this.addTextline(rectangle, annotation);
        }
        this.lowerLinesToFront();
    }

    /**
     * Convenience method to bring all lower lines of a textline to front
     */
    private void lowerLinesToFront() {
        final ObservableList<Node> children = this.groupTranscript
                .getChildren();
        for (int i = children.size() - 1; i >= 0; --i) {
            final Node child = children.get(i);
            if (child instanceof Line) {
                child.toFront();
            }
        }
    }

    /**
     * This method is used to adds a textfield to the transcript view
     * 
     * @param rectangle
     *            The position of the textfield
     * @param annotation
     *            <code>true</code> if the textfield is a handwritten
     *            annotation, <code>false</code> otherwise
     */
    private void addTextline(final Rectangle rectangle, final boolean annotation) {
        final double height = this.calcTextfieldHeight();
        final double width = this.calcTextfieldWidth(
                (int) rectangle.getLayoutX(), (int) rectangle.getWidth(), -1);

        final int offsetY = (int) ((rectangle.getHeight() - height) / 2);

        final Line lowerLine = new Line(rectangle.getX() * 0.65,
                rectangle.getY() + +offsetY + height, rectangle.getX() * 0.65
                        + width, rectangle.getY() + offsetY + height);

        lowerLine.setId("L" + rectangle.getId());
        final TextField textfield = new TextField();
        textfield.setId(rectangle.getId());
        textfield.setLayoutX(rectangle.getX() * 0.65);
        textfield.setLayoutY(rectangle.getY() + offsetY);

        textfield.setPrefWidth(width);
        textfield.setPrefHeight(height);

        textfield.setMinWidth(Control.USE_PREF_SIZE);
        textfield.setMaxWidth(rectangle.getWidth());

        textfield.setMinHeight(Control.USE_PREF_SIZE);
        textfield.setMaxHeight(Control.USE_PREF_SIZE);

        textfield.setFocusTraversable(true);

        if (annotation) {
            textfield.setStyle("-fx-font-family:" + this.fontfamily.get() + ";"
                    + TEXTFIELD_STYLE_2 + this.fontsize.get() + "pt;"
                    + TEXTFIELD_ANNOTATION_BACKGROUND);
            textfield.setId(textfield.getId() + "_anno");
        } else {
            textfield.setStyle(TEXTFIELD_STYLE_1 + this.fontfamily.get() + ";"
                    + TEXTFIELD_STYLE_2 + this.fontsize.get() + "pt;");
        }
        textfield.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(final KeyEvent evt) {
                final KeyCombination combo = new KeyCodeCombination(
                        KeyCode.ENTER);
                if (combo.match(evt) && !DocumentPanel.this.fragmentsShowing) {

                    String id = textfield.getId();
                    if (id.endsWith("_anno")) {
                        id = id.substring(0, id.length() - 5);
                    }
                    String text = textfield.getText();
                    if (!text.isEmpty()) {
                        while (text.contains("  ")) {
                            text = text.replace("  ", " ");
                        }
                        while (String.valueOf(text.charAt(text.length() - 1))
                                .equals(" ")) {
                            text = text.substring(0, text.length() - 1);
                            if (text.length() == 0) {
                                break;
                            }
                        }

                        DocumentPanel.this.documentPanelController.transcribe(
                                text, id, false);
                    } else {
                        DocumentPanel.this.documentPanelController
                                .removeLineEncodingFromAlphabet(id);
                    }

                    final String[] split = id.split("_");
                    int lineID = Integer.valueOf(split[split.length - 1]) + 1;
                    // size - 2 because of the child workaroundRegion
                    if (lineID == DocumentPanel.this.groupTranscript
                            .getChildren().size() - 2) {
                        lineID = 0;
                    }
                    final String newID = ImageLine.ID_PREFIX + lineID;

                    Node nodeTranscript = DocumentPanel.this.groupTranscript
                            .lookup("#" + newID);
                    int i = 0;
                    while (nodeTranscript == null) {
                        if (i == 10000) {
                            lineID = 0;
                        }
                        lineID++;
                        nodeTranscript = DocumentPanel.this.groupTranscript
                                .lookup("#" + ImageLine.ID_PREFIX + (lineID));
                        if (nodeTranscript != null) {
                            break;
                        }
                        i++;
                    }

                    nodeTranscript.toFront();
                    nodeTranscript.requestFocus();
                    final Bounds nodeBound = nodeTranscript
                            .localToScene(nodeTranscript.getBoundsInLocal());
                    final Bounds scrollBound = DocumentPanel.this.rightScroll
                            .localToScene(DocumentPanel.this.rightScroll
                                    .getBoundsInLocal());
                    final double vValue = DocumentPanel.this.rightScroll
                            .getVvalue();
                    final double height = DocumentPanel.this.transcriptBackground
                            .getHeight();
                    final double h = scrollBound.getHeight();
                    if (nodeBound.getMaxY() >= scrollBound.getMaxY()) {
                        if (height > h) {
                            final double d = height - h;
                            final double dy = nodeBound.getMaxY()
                                    - scrollBound.getMaxY();
                            DocumentPanel.this.rightScroll
                                    .setVvalue(((dy + h / 2) / d) + vValue);
                        }
                    } else if (nodeBound.getMaxY() <= scrollBound.getMinY()) {
                        DocumentPanel.this.rightScroll.setVvalue(0);
                    }
                } else {
                    final double prefLength = DocumentPanel.this
                            .calcTextfieldWidth((int) textfield.getLayoutX(),
                                    (int) textfield.getMaxWidth(),
                                    textfield.getLength());
                    if (textfield.getPrefWidth() < prefLength) {
                        textfield.setPrefWidth(prefLength);
                        final String id = textfield.getId();
                        final Line lowerLine;
                        if (id.endsWith("_anno")) {
                            lowerLine = (Line) DocumentPanel.this.groupTranscript.lookup("#L"
                                    + id.subSequence(0, id.length() - 5));
                        } else {
                            lowerLine = (Line) DocumentPanel.this.groupTranscript
                                    .lookup("#L" + id);
                        }
                        lowerLine.setEndX(lowerLine.getStartX() + prefLength);
                    }
                }
            }
        });
        textfield.focusedProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                if (textfield.focusedProperty().get()
                        && !DocumentPanel.this.fragmentsShowing) {
                    DocumentPanel.this.textfieldFocused = true;
                    textfield.toFront();
                    textfield.setStyle(TEXTFIELD_FOCUS
                            + DocumentPanel.this.fontfamily.get() + ";"
                            + TEXTFIELD_STYLE_2
                            + DocumentPanel.this.fontsize.get());
                    final Node node = DocumentPanel.this.groupImage.lookup("#"
                            + textfield.getId());
                    if (node == null) {
                        return;
                    }
                    node.setStyle(LINE_STYLE_HIGHLIGHT);
                    node.toFront();
                    // System.out.println("DocumentPanel:modelPropertyChange 3a");
                    if (DocumentPanel.this.showGlyphs.getValue()) {
                        DocumentPanel.this.documentPanelController.showGlyphs();
                    }
                    // System.out.println("DocumentPanel:modelPropertyChange 3b");
                    if (DocumentPanel.this.fragmentsShowing) {
                        DocumentPanel.this
                                .showFragments(DocumentPanel.this.frags);
                    }
                    // System.out.println("DocumentPanel:modelPropertyChange 3c");
                } else {
                    // System.out.println("DocumentPanel:modelPropertyChange 3d");
                    DocumentPanel.this.textfieldFocused = false;
                    final Node imageLine;
                    final String id = textfield.getId();
                    if (id.endsWith("_anno")) {
                        imageLine = DocumentPanel.this.groupImage.lookup("#"
                                + id.substring(0, id.length() - 5));
                    } else {
                        imageLine = DocumentPanel.this.groupImage.lookup("#"
                                + id);
                    }
                    if (imageLine != null) {

                        if (id.endsWith("_anno")) {
                            textfield.setStyle("-fx-font-family:"
                                    + DocumentPanel.this.fontfamily.get() + ";"
                                    + TEXTFIELD_STYLE_2
                                    + DocumentPanel.this.fontsize.get() + "pt;"
                                    + TEXTFIELD_ANNOTATION_BACKGROUND);
                            imageLine.setStyle(LINE_STYLE_ANNOTATION);
                        } else {
                            imageLine.setStyle(LINE_STYLE);
                            textfield.setStyle(TEXTFIELD_STYLE_1
                                    + DocumentPanel.this.fontfamily.get() + ";"
                                    + TEXTFIELD_STYLE_2
                                    + DocumentPanel.this.fontsize.get());
                        }
                    }
                    DocumentPanel.this.lowerLinesToFront();
                }
                // System.out.println("DocumentPanel:modelPropertyChange 3 END");
            }
        });
        textfield
                .setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                    @Override
                    public void handle(final ContextMenuEvent event) {
                        DocumentPanel.this.getLineContextMenu(textfield, event);
                    }
                });
        this.groupTranscript.getChildren().add(textfield);
        this.groupTranscript.getChildren().add(lowerLine);

    }

    /**
     * This method is used to get line context menus.
     * 
     * @param node
     *            parent node
     * @param event
     *            context menu event
     */
    private void getLineContextMenu(final Node node,
            final ContextMenuEvent event) {
        final String menuItem;
        final String textfieldStyle;
        final String imagelineStyle;
        if (node.getId().endsWith("_anno")) {
            menuItem = "Unset handwritten Annotation";
            if (node.isFocused()) {
                textfieldStyle = TEXTFIELD_FOCUS + this.fontfamily.get() + ";"
                        + TEXTFIELD_STYLE_2 + this.fontsize.get() + "pt;";
                imagelineStyle = LINE_STYLE_HIGHLIGHT;
            } else {
                textfieldStyle = TEXTFIELD_STYLE_1 + this.fontfamily.get()
                        + ";" + TEXTFIELD_STYLE_2 + this.fontsize.get() + "pt;";
                imagelineStyle = LINE_STYLE;
            }
        } else {
            menuItem = "Set to handwritten Annotation";
            textfieldStyle = "-fx-font-family: " + this.fontfamily.get() + ";"
                    + TEXTFIELD_STYLE_2 + this.fontsize.get() + "pt;"
                    + TEXTFIELD_ANNOTATION_BACKGROUND;
            imagelineStyle = LINE_STYLE_ANNOTATION;
        }
        final ContextMenu cm = ContextMenuBuilder
                .create()
                .items(MenuItemBuilder.create().text("Remove textline")
                        .onAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(final ActionEvent event) {
                                DiptychonLogger.info("Removing textline");
                                final String id = node.getId();
                                if (id.endsWith("_anno")) {
                                    DocumentPanel.this.documentPanelController
                                            .removeTextline(id.substring(0,
                                                    id.length() - 5));
                                } else {
                                    DocumentPanel.this.documentPanelController
                                            .removeTextline(id);
                                }
                                DocumentPanel.this.removeTextlines(id);
                            }
                        }).build(),
                        MenuItemBuilder.create().text("Transcribe")
                                .onAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(final ActionEvent event) {
                                        DiptychonLogger
                                                .info("Running Template Matching for single line");
                                        final String id = node.getId();
                                        if (id.endsWith("_anno")) {
                                            DocumentPanel.this.documentPanelController
                                                    .matchAllTemplatesSingleLine(id.substring(
                                                            0, id.length() - 5));
                                        } else {
                                            DocumentPanel.this.documentPanelController
                                                    .matchAllTemplatesSingleLine(id);
                                        }
                                    }
                                }).build(),
                        MenuItemBuilder.create().text("Fragmentate")
                                .onAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(final ActionEvent event) {
                                        boolean setSplitLine = false;
                                        if (DocumentPanel.this.splitLine
                                                .getValue()) {
                                            DocumentPanel.this.splitLine
                                                    .setValue(false);
                                            setSplitLine = true;
                                        }
                                        DiptychonLogger
                                                .info("Starting Fragment Editing");
                                        final String id = node.getId();
                                        final Node textfield = DocumentPanel.this.groupTranscript
                                                .lookup("#" + (String) id);
                                        String text = ((TextField) textfield)
                                                .getText();
                                        if (!text.isEmpty()) {
                                            while (text.contains("  ")) {
                                                text = text.replace("  ", " ");
                                            }
                                            while (String
                                                    .valueOf(
                                                            text.charAt(text
                                                                    .length() - 1))
                                                    .equals(" ")) {
                                                text = text.substring(0,
                                                        text.length() - 1);
                                                if (text.length() == 0) {
                                                    break;
                                                }
                                            }
                                        }
                                        ((TextField) textfield).setText(text);

                                        boolean textWasEmpty = false;
                                        if (((TextField) textfield).getText()
                                                .length() == 0) {
                                            textWasEmpty = true;
                                            ((TextField) textfield)
                                                    .setText("_");
                                        }
                                        if (id.endsWith("_anno")) {
                                            DocumentPanel.this.documentPanelController
                                                    .transcribe(
                                                            ((TextField) textfield)
                                                                    .getText(),
                                                            id.substring(
                                                                    0,
                                                                    id.length() - 5),
                                                            true);
                                        } else {
                                            DocumentPanel.this.documentPanelController
                                                    .transcribe(
                                                            ((TextField) textfield)
                                                                    .getText(),
                                                            id, true);
                                        }
                                        if (textWasEmpty) {
                                            ((TextField) textfield).setText("");
                                        }
                                        if (setSplitLine) {
                                            DocumentPanel.this.splitLine
                                                    .setValue(true);
                                        }
                                    }
                                }).build(),
                        MenuItemBuilder.create().text(menuItem)
                                .onAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(final ActionEvent event) {
                                        final String id = node.getId();
                                        final Node nodeTranscript = DocumentPanel.this.groupTranscript
                                                .lookup("#" + id);
                                        final Node nodeImage;
                                        if (id.endsWith("_anno")) {
                                            DocumentPanel.this.documentPanelController.setAnnotation(id
                                                    .substring(0,
                                                            id.length() - 5));

                                            nodeTranscript.setId(id.substring(
                                                    0, id.length() - 5));

                                            nodeImage = DocumentPanel.this.groupImage.lookup("#"
                                                    + id.substring(0,
                                                            id.length() - 5));

                                        } else {
                                            DocumentPanel.this.documentPanelController
                                                    .setAnnotation(id);

                                            nodeTranscript.setId(id + "_anno");

                                            nodeImage = DocumentPanel.this.groupImage
                                                    .lookup("#" + id);

                                        }
                                        nodeTranscript.setStyle(textfieldStyle);

                                        DiptychonLogger.info(menuItem
                                                + "Line (ID: {})", id);

                                        if (nodeImage != null) {
                                            nodeImage.setStyle(imagelineStyle);
                                        }

                                    }
                                }).build()).build();
        cm.show(DocumentPanel.this.root.getScene().getWindow(),
                event.getScreenX(), event.getScreenY() - 76);
        event.consume();

    }

    /**
     * This method is used to remove text lines.
     * 
     * @param id
     *            id of removed textline
     */
    private void removeTextlines(final String id) {
        final Node nodeTranscript = this.groupImage.lookup("#" + id);
        if (nodeTranscript != null) {
            this.groupImage.getChildren().remove(nodeTranscript);
        }
        final Node nodeImage = this.groupTranscript.lookup("#" + id);
        if (nodeImage != null) {
            this.groupTranscript.getChildren().remove(nodeImage);
        }
    }

    /**
     * This method is used to set image view.
     * 
     * @param image
     *            image
     */
    private void setImageView(final Image image) {
        this.scale.set(DEFAULT_SCALE);
        final ObservableList<Node> children = this.groupImage.getChildren();
        children.clear();
        children.add(this.documentImageView);
        children.add(this.rubberBand);

        this.documentImageView.setFitWidth(image.getWidth());
        this.documentImageView.setFitHeight(image.getHeight());
        this.documentImageView.setImage(image);

        final double newWidth = image.getWidth() * 0.65;
        final double newHeight = image.getHeight();

        this.transcriptBackground.setPrefWidth(newWidth);
        this.transcriptBackground.setPrefHeight(newHeight);
        this.resetRubberband();
    }

    /**
     * This method is used to add threshold of template matching into
     * invalidation listener.
     * 
     * @param templateMatchingThreshold
     *            threshhold of template matching
     * @param templateMatchingGlyphs
     *            arraylist of glyphs
     */
    private void addTemplateMatchingTresholdInvalidationListener(
            final IntegerProperty templateMatchingThreshold,
            final ArrayList<Glyph>[] templateMatchingGlyphs) {
        templateMatchingThreshold.addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                final int curThreshold = templateMatchingThreshold.get();
                if (curThreshold > TemplateMatchingThresholdDialog.MAX) {
                    templateMatchingThreshold.set(MAX_SCALE);
                } else {
                    DocumentPanel.this.showTemplateMatchingGlyphs(
                            templateMatchingThreshold.get(),
                            templateMatchingGlyphs);
                }
            }
        });
    }

    /**
     * This method is used to add template matching acceptance into invalidation
     * listener.
     * 
     * @param templateMatchingAcceptance
     *            acceptance of template matching
     * @param templateMatchingThreshold
     *            threshold of template matching
     * @param templateMatchingGlyphs
     *            glyphs of template matching
     */
    private void addTemplateMatchingAcceptanceInvalidationListener(
            final IntegerProperty templateMatchingAcceptance,
            final IntegerProperty templateMatchingThreshold,
            final ArrayList<Glyph>[] templateMatchingGlyphs) {
        templateMatchingAcceptance.addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                if (templateMatchingAcceptance.get() == 1) {
                    DiptychonLogger.info("Accept Glyphs");
                    DocumentPanel.this.acceptGlyphs(templateMatchingThreshold,
                            templateMatchingGlyphs);
                } else if (templateMatchingAcceptance.get() == 0) {
                    final ObservableList<Node> children = DocumentPanel.this.groupImage
                            .getChildren();
                    children.clear();
                    children.add(DocumentPanel.this.documentImageView);
                    children.add(DocumentPanel.this.rubberBand);

                    DocumentPanel.this.documentPanelController
                            .getGlyphsAndLines();
                }
            }
        });
    }

    /**
     * This method is used to accept glyphs according to template matching
     * threshold
     * 
     * @param templateMatchingThreshold
     *            threshold of template matching
     * @param templateMatchingGlyphs
     *            arraylist of glyphs
     */
    private void acceptGlyphs(final IntegerProperty templateMatchingThreshold,
            final ArrayList<Glyph>[] templateMatchingGlyphs) {
        final ArrayList<Glyph> acceptedGlyphs = new ArrayList<>();
        for (int i = templateMatchingThreshold.get(); i < templateMatchingGlyphs.length; ++i) {
            final ArrayList<Glyph> glyphs = templateMatchingGlyphs[i];
            for (final Glyph glyph : glyphs) {
                if (glyph.getIDWithoutPrefix() == TemplateMatchingIDColor.ACCEPT
                        .getID()) {
                    acceptedGlyphs.add(glyph);
                }
            }
        }
        this.documentPanelController.acceptGlyphs(acceptedGlyphs);
    }

    /**
     * This method is used to scroll the page.
     * 
     * @param evt
     *            scroll event
     */
    @FXML
    public void handleMouseScroll(final ScrollEvent evt) {
        if (evt.isControlDown()) {
            final double delta = evt.getDeltaY();
            final int scaleStepSize = 5;
            int curScale = this.scale.get();
            if (delta > 0.d) {
                curScale += scaleStepSize;
            } else {
                curScale -= scaleStepSize;
            }
            this.scale.set(curScale);
        }
    }

    /**
     * This method is used to get extract glyph handler.
     * 
     * @return extract glyph handler
     */
    private EventHandler<ActionEvent> getExtractGlyphHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                DiptychonLogger.info("Execute context menu extract glyph...");
                DocumentPanel.this.documentPanelController
                        .extractRectangle(new Rectangle(
                                DocumentPanel.this.rubberBand.getLayoutX(),
                                DocumentPanel.this.rubberBand.getLayoutY(),
                                DocumentPanel.this.rubberBand.getWidth(),
                                DocumentPanel.this.rubberBand.getHeight()));
            }
        };
    }

    private EventHandler<ActionEvent> getBurstFragmentHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                DocumentPanel.this.lastFrags = new ArrayList<Glyph>();
                for (Glyph f : DocumentPanel.this.frags) {
                    DocumentPanel.this.lastFrags.add(f.copy());
                }
                DocumentPanel.this.undoable = true;

                DocumentPanel.this.documentPanelController.squareFragGlyph(
                        DocumentPanel.this.contextFrag,
                        (Integer) DocumentPanel.this.squareSize.getValue());
                ArrayList<Glyph> newFrags = DocumentPanel.this.frags;
                newFrags.remove(DocumentPanel.this.contextFrag);
                newFrags.addAll(DocumentPanel.this.separatedFrags);
                DocumentPanel.this.showFragments(newFrags);
            }
        };
    }

    private EventHandler<ActionEvent> getMoveLeftHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                DiptychonLogger.info("Moving glyph left in sorting...");
                DocumentPanel.this.documentPanelController
                        .sortGlyphLeft(DocumentPanel.this.contextGlyph.getID());
            }
        };
    }

    private EventHandler<ActionEvent> getMoveRightHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                DiptychonLogger.info("Moving glyph right in sorting...");
                DocumentPanel.this.documentPanelController
                        .sortGlyphRight(DocumentPanel.this.contextGlyph.getID());
            }
        };
    }

    private EventHandler<ActionEvent> getEditGlyphHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                DiptychonLogger.info("Show glyph editor dialog "
                        + "for glyph (ID: {}).",
                        DocumentPanel.this.contextGlyph.getID());
                DocumentPanel.this.documentPanelController
                        .getGlyph(DocumentPanel.this.contextGlyph.getID());
            }
        };
    }

    // CURRENTLY NOT USED
    private EventHandler<ActionEvent> getResetPositionHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                DiptychonLogger.info("Resetting glyph position in sorting...");
                DocumentPanel.this.documentPanelController
                        .sortGlyphReset(DocumentPanel.this.contextGlyph.getID());
            }
        };
    }

    // CURRENTLY NOT USED
    private EventHandler<ActionEvent> getFindSimilarHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                DiptychonLogger.info("match similar glyphs...");
                DocumentPanel.this.documentPanelController
                        .matchTemplate(DocumentPanel.this.contextGlyph.getID());
            }
        };
    }

    private EventHandler<ActionEvent> getRemoveGlyphHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                DiptychonLogger.info("Removing Glyph (ID: {}).",
                        DocumentPanel.this.contextGlyph.getID());
                DocumentPanel.this.documentPanelController
                        .removeGlyph(DocumentPanel.this.contextGlyph.getID());
            }
        };
    }

    private EventHandler<ActionEvent> getRemoveFragmentHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                DocumentPanel.this.lastFrags = new ArrayList<Glyph>();
                for (Glyph f : DocumentPanel.this.frags) {
                    DocumentPanel.this.lastFrags.add(f.copy());
                }
                DocumentPanel.this.undoable = true;

                ArrayList<Glyph> newFrags = DocumentPanel.this.frags;
                DocumentPanel.this.undoable = true;
                newFrags.remove(DocumentPanel.this.contextFrag);
                DocumentPanel.this.showFragments(newFrags);
            }
        };
    }

    private EventHandler<ActionEvent> getBinarizeHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                DocumentPanel.this.documentPanelController.showBinarizeDialog();
                DiptychonLogger.info("Binarizing Image");
            }
        };
    }

    private EventHandler<ActionEvent> getBinarizeMaskHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                DocumentPanel.this.documentPanelController
                        .showBinarizeDialogMask();
                DiptychonLogger.info("Binarizing Local Image Part");
            }
        };
    }

    private EventHandler<ActionEvent> getFragNoiseHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                DocumentPanel.this.removeFragNoise();
            }
        };
    }

    private EventHandler<ActionEvent> getRemoveBorderFragsHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                // System.out.println("ENTFERNE RANDFRAGMENTE");
                DocumentPanel.this.lastFrags = new ArrayList<Glyph>();
                for (Glyph f : DocumentPanel.this.frags) {
                    DocumentPanel.this.lastFrags.add(f.copy());
                }
                DocumentPanel.this.undoable = true;
                ArrayList<Glyph> fragsToRemove = new ArrayList<Glyph>();
                Rectangle bounds = DocumentPanel.this.contextLine;
                for (Glyph f : DocumentPanel.this.frags) {
                    // System.out.println(bounds.getLayoutBounds().getMinY());
                    Shape shape = f.getShapeToDraw(Color.PINK);
                    if (shape == null) {
                        continue;
                    }
                    // System.out.println(shape.getLayoutY());
                    if (shape.getLayoutY() - bounds.getLayoutBounds().getMinY() < 6
                            || bounds.getLayoutBounds().getMaxY()
                                    - (shape.getLayoutY() + shape
                                            .getBoundsInLocal().getHeight()) < 6) {
                        if (shape.getBoundsInLocal().getHeight() < bounds
                                .getLayoutBounds().getHeight() / 2) {
                            fragsToRemove.add(f);
                        }
                    }
                }
                DocumentPanel.this.frags.removeAll(fragsToRemove);
                DocumentPanel.this.showFragments(DocumentPanel.this.frags);
            }
        };
    }

    private EventHandler<ActionEvent> getUndoHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                ArrayList<Glyph> newFrags = new ArrayList<Glyph>();
                for (Glyph f : DocumentPanel.this.lastFrags) {
                    newFrags.add(f.copy());
                }
                DocumentPanel.this.undoable = false;
                DocumentPanel.this.showFragments(newFrags);
            }
        };
    }

    private EventHandler<ActionEvent> getContrastHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                DocumentPanel.this.executeContractAdjustment(new Rectangle(0,
                        0, DocumentPanel.this.documentImageView.getImage()
                                .getWidth(),
                        DocumentPanel.this.documentImageView.getImage()
                                .getHeight()));
            }
        };
    }

    private EventHandler<ActionEvent> getGreyscaleHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                DiptychonLogger.info("Create grayscale image");
                DocumentPanel.this.documentPanelController
                        .showGrayScaleDialog();
            }
        };
    }

    private EventHandler<ActionEvent> getGlyphNoiseHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                DiptychonLogger.info("Remove Glyph Noise");
                DocumentPanel.this.documentPanelController.removeNoise();
            }
        };
    }

    private EventHandler<ActionEvent> getFindTextlinesHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                DiptychonLogger.info("Find textlines");
                DocumentPanel.this.documentPanelController.findLines(
                        DocumentPanel.this.lineExtTop.get(),
                        DocumentPanel.this.lineExtBottom.get());
            }
        };
    }

    private EventHandler<ActionEvent> getGalleryHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                DiptychonLogger.info("Open Glyph Gallery");
                DocumentPanel.this.documentPanelController
                        .showGalleryDialog(DocumentPanel.this.contextGlyph
                                .getGroupID());
            }
        };
    }

    private EventHandler<ActionEvent> getRemoveLineHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                DiptychonLogger.info("Removing textline");
                final String id = DocumentPanel.this.contextLine.getId();
                if (id.endsWith("_anno")) {
                    DocumentPanel.this.documentPanelController
                            .removeTextline(id.substring(0, id.length() - 5));
                } else {
                    DocumentPanel.this.documentPanelController
                            .removeTextline(id);
                }
                DocumentPanel.this.removeTextlines(id);
            }
        };
    }

    private EventHandler<ActionEvent> getFragmentateHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                boolean setSplitLine = false;
                if (DocumentPanel.this.splitLine.getValue()) {
                    DocumentPanel.this.splitLine.setValue(false);
                    setSplitLine = true;
                }
                DiptychonLogger.info("Starting Fragment Editing");
                final String id = DocumentPanel.this.contextLine.getId();
                final Node textfield = DocumentPanel.this.groupTranscript
                        .lookup("#" + (String) id);
                String text = ((TextField) textfield).getText();
                if (!text.isEmpty()) {
                    while (text.contains("  ")) {
                        text = text.replace("  ", " ");
                    }
                    while (String.valueOf(text.charAt(text.length() - 1))
                            .equals(" ")) {
                        text = text.substring(0, text.length() - 1);
                        if (text.length() == 0) {
                            break;
                        }
                    }
                }
                ((TextField) textfield).setText(text);

                boolean textWasEmpty = false;
                if (((TextField) textfield).getText().length() == 0) {
                    textWasEmpty = true;
                    ((TextField) textfield).setText("_");
                }
                if (id.endsWith("_anno")) {
                    DocumentPanel.this.documentPanelController.transcribe(
                            ((TextField) textfield).getText(),
                            id.substring(0, id.length() - 5), true);
                } else {
                    DocumentPanel.this.documentPanelController.transcribe(
                            ((TextField) textfield).getText(), id, true);
                }
                if (textWasEmpty) {
                    ((TextField) textfield).setText("");
                }
                if (setSplitLine) {
                    DocumentPanel.this.splitLine.setValue(true);
                }
            }

        };
    }

    private EventHandler<ActionEvent> getTranscribeHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                DiptychonLogger
                        .info("Running Template Matching for single line");
                final String id = DocumentPanel.this.contextLine.getId();
                if (id.endsWith("_anno")) {
                    DocumentPanel.this.documentPanelController
                            .matchAllTemplatesSingleLine(id.substring(0,
                                    id.length() - 5));
                } else {
                    DocumentPanel.this.documentPanelController
                            .matchAllTemplatesSingleLine(id);
                }
            }
        };
    }

    private EventHandler<ActionEvent> getAnnotationHandler() {

        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                final String menuItem;
                final String textfieldStyle;
                final String imagelineStyle;
                if (DocumentPanel.this.contextLine.getId().endsWith("_anno")) {
                    menuItem = "Unset handwritten Annotation";
                    if (DocumentPanel.this.contextLine.isFocused()) {
                        textfieldStyle = TEXTFIELD_FOCUS
                                + DocumentPanel.this.fontfamily.get() + ";"
                                + TEXTFIELD_STYLE_2
                                + DocumentPanel.this.fontsize.get() + "pt;";
                        imagelineStyle = LINE_STYLE_HIGHLIGHT;
                    } else {
                        textfieldStyle = TEXTFIELD_STYLE_1
                                + DocumentPanel.this.fontfamily.get() + ";"
                                + TEXTFIELD_STYLE_2
                                + DocumentPanel.this.fontsize.get() + "pt;";
                        imagelineStyle = LINE_STYLE;
                    }
                } else {
                    menuItem = "Set to handwritten Annotation";
                    textfieldStyle = "-fx-font-family: "
                            + DocumentPanel.this.fontfamily.get() + ";"
                            + TEXTFIELD_STYLE_2
                            + DocumentPanel.this.fontsize.get() + "pt;"
                            + TEXTFIELD_ANNOTATION_BACKGROUND;
                    imagelineStyle = LINE_STYLE_ANNOTATION;
                }

                final String id = DocumentPanel.this.contextLine.getId();
                final Node nodeTranscript = DocumentPanel.this.groupTranscript
                        .lookup("#" + id);
                final Node nodeImage;
                if (id.endsWith("_anno")) {
                    DocumentPanel.this.documentPanelController.setAnnotation(id
                            .substring(0, id.length() - 5));

                    nodeTranscript.setId(id.substring(0, id.length() - 5));

                    nodeImage = DocumentPanel.this.groupImage.lookup("#"
                            + id.substring(0, id.length() - 5));

                } else {
                    DocumentPanel.this.documentPanelController
                            .setAnnotation(id);

                    nodeTranscript.setId(id + "_anno");

                    nodeImage = DocumentPanel.this.groupImage.lookup("#" + id);

                }
                nodeTranscript.setStyle(textfieldStyle);

                DiptychonLogger.info(menuItem + "Line (ID: {})", id);

                if (nodeImage != null) {
                    nodeImage.setStyle(imagelineStyle);
                }
            }
        };
    }

    /**
     * This method is used to get mask image handler.
     * 
     * @return mask image handler
     */
    private EventHandler<ActionEvent> getMaskImageHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                if (DocumentPanel.this.rubberBand.getWidth() > 0
                        && DocumentPanel.this.rubberBand.getHeight() > 0) {
                    DiptychonLogger.info("Execute context menu mask image...");
                    DocumentPanel.this.documentPanelController
                            .maskImage(new Rectangle(
                                    DocumentPanel.this.rubberBand.getLayoutX(),
                                    DocumentPanel.this.rubberBand.getLayoutY(),
                                    DocumentPanel.this.rubberBand.getWidth(),
                                    DocumentPanel.this.rubberBand.getHeight()));
                }
            }
        };
    }

    /**
     * This method is used to get auto contrast adjust handler
     * 
     * @return handler
     */
    private EventHandler<ActionEvent> getAutoContastAdjustHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                if (DocumentPanel.this.rubberBand.getWidth() > 0
                        && DocumentPanel.this.rubberBand.getHeight() > 0) {
                    DocumentPanel.this.executeContractAdjustment(new Rectangle(
                            DocumentPanel.this.rubberBand.getLayoutX(),
                            DocumentPanel.this.rubberBand.getLayoutY(),
                            DocumentPanel.this.rubberBand.getWidth(),
                            DocumentPanel.this.rubberBand.getHeight()));
                }
            }
        };
    }

    /**
     * This method is used to get extract line handler
     * 
     * @return handler of extract line
     */
    private EventHandler<ActionEvent> getExtractLineHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                if (DocumentPanel.this.rubberBand.getWidth() > 0
                        && DocumentPanel.this.rubberBand.getHeight() > 0) {
                    DocumentPanel.this.documentPanelController
                            .extractLine(new Rectangle(
                                    DocumentPanel.this.rubberBand.getLayoutX(),
                                    DocumentPanel.this.rubberBand.getLayoutY(),
                                    DocumentPanel.this.rubberBand.getWidth(),
                                    DocumentPanel.this.rubberBand.getHeight()));
                }
            }
        };
    }

    /**
     * This method is sued to execute the command of contrast adjustment
     * 
     * @param imageRectangle
     *            the size of image
     */
    private void executeContractAdjustment(final Rectangle imageRectangle) {
        DiptychonLogger.info("Execute contrast adjustment...");
        this.resetRubberband();

        final AutoContrastAdjustDialog autoContrastDialog = (AutoContrastAdjustDialog) new A_Dialog.Factory()
                .createDialog(AutoContrastAdjustDialog.class.toString(),
                        this.documentPanelController);

        final int[] histogramm = new int[256];
        autoContrastDialog.init(imageRectangle, histogramm);
        this.documentPanelController.getHistogramm(
                autoContrastDialog.getInvalidationTrigger(), imageRectangle,
                histogramm);
        autoContrastDialog.showNormalDialog(this.root.getScene().getWindow(),
                -1, -1);
    }

    /**
     * This method is used to add scroll panes into invalidation listener to
     * detect the movements of them.
     * 
     * @param first
     *            first scroll pane
     * @param second
     *            second scroll pane
     */
    private void addScrollInvalidationListener(final ScrollPane first,
            final ScrollPane second) {
        first.hvalueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                if (DocumentPanel.this.syncScroll.get()) {
                    DocumentPanel.this.syncScrollbars(first, second);
                }
            }

        });
        first.vvalueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(final Observable observable) {
                if (DocumentPanel.this.syncScroll.get()) {
                    DocumentPanel.this.syncScrollbars(first, second);
                }
            }

        });
    }

    /**
     * This method is used to synchronize first scroll pane with second scroll
     * pane.
     * 
     * @param first
     *            first scroll pane
     * @param second
     *            second scroll pane
     */
    private void syncScrollbars(final ScrollPane first, final ScrollPane second) {
        second.setHvalue(first.getHvalue());
        second.setVvalue(first.getVvalue());
    }

    /**
     * This method is used to get SynScrollProperty
     * 
     * @return property of synScroll
     */
    public BooleanProperty getSyncScrollProperty() {
        return this.syncScroll;
    }

    /**
     * This method is used to get property of font family.
     * 
     * @return property
     */
    public StringProperty getFontfamilyProperty() {
        return this.fontfamily;
    }

    public static int countOccurrences(String text, String character) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (String.valueOf(text.charAt(i)).equals(character)) {
                count++;
            }
        }
        return count;
    }

    /**
     * @return threshold property
     */
    public FloatProperty getCCThresholdProperty() {
        return ccThreshold;
    }
}
