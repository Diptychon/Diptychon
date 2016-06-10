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
package de.diptychon.controller;

import java.io.File;
import java.util.ArrayList;

import javafx.beans.property.IntegerProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import de.diptychon.DiptychonPreferences;
import de.diptychon.models.data.Glyph;

/**
 * This class extends from AbstractController. It connects both model and view
 * together in order to establish the communication channels between them.
 */
public class DocumentPanelController extends AbstractController {
    private static final String ACCEPT_GLYPH = "acceptGlyph";

    private static final String ACCEPT_GLYPHS = "acceptGlyphs";

    public static final String ADD_TEXTLINE = "addTextline";

    private static final String APPLY_AUTO_CONTRAST = "applyAutoContrast";

    private static final String BINARIZE_APPLY = "binarizeApply";

    public static final String BINARIZE_MASK = "binarizeMask";

    private static final String BINARIZE_PREVIEW = "binarizePreview";

    private static final String BINARIZE_PREVIEW_MASK = "binarizePreviewMask";

    public static final String BINARIZED_BUTTON_PRESSED = "binarizedButtonPressed";

    public static final String BINARIZED_BUTTON_RELEASED = "binarizedButtonReleased";

    public static final String BROWSE_IMAGES = "browseImages";

    private static final String CANCEL_AUTO_CONTRAST = "cancelAutoContrast";

    public static final String CANCEL_BINARIZATION = "cancelBinarization";

    private static final String CANCEL_GRAY = "cancelGray";

    private static final String EDIT_GLYPH = "editGlyph";

    private static final String EXPORT_BINARY_IMAGE = "exportBinaryImage";

    private static final String EXPORT_GRAYSCALE_IMAGE = "exportGrayscaleImage";

    private static final String EXPORT_PDF_parentheses = "exportPDFwithparentheses";

    private static final String EXPORT_PDF_CLEAN = "exportPDFwithoutparentheses";

    private static final String EXPORT_TRANSCRIPT_AS_PLAIN_TEXT = "exportTranscriptAsPlainText";

    private static final String EXPORT_TRANSCRIPT_AS_RTF = "exportTranscriptAsRTF";

    private static final String EXPORT_TRANSCRIPT_AS_TEI = "exportTranscriptAsTEI";

    private static final String EXTRACT_GLYPH = "extractGlyph";

    private static final String EXTRACT_LINE = "extractLine";

    private static final String FIND_LINES = "findLines";

    private static final String GET_GLYPH = "getGlyph";

    private static final String SORT_GLYPH_LEFT = "sortGlyphLeft";

    private static final String SORT_GLYPH_RIGHT = "sortGlyphRight";

    private static final String SORT_GLYPH_RESET = "sortGlyphReset";

    private static final String GET_GLYPHS_AND_LINES = "getGlyphsAndLines";

    private static final String GET_HISTOGRAMM = "getHistogramm";

    private static final String GRAY_APPLY = "grayApply";

    private static final String GRAY_PREVIEW = "grayPreview";

    public static final String INIT_DIGITAL = "initDigital";

    public static final String INIT_SPLIT_SUGGESTION_DIALOG = "showLineSplitSuggestionDialog";

    public static final String LOAD_FROM_HDD_DIGITAL = "loadFromHDDDigital";

    private static final String LOAD_FROM_HDD_TRANSCRIPT = "loadFromHDDTranscript";

    private static final String MASK_IMAGE = "maskImage";

    private static final String MATCH_TEMPLATE = "matchTemplate";

    private static final String MATCH_TEMPLATE_SINGLE_LINE = "matchTemplateSingleLine";

    private static final String MATCH_ALL_TEMPLATES_SINGLE_LINE = "matchAllTemplatesSingleLine";

    public static final String OPEN_IMAGE = "openImage";

    public static final String OPEN_PROJECT = "openProject";

    private static final String OPEN_RECENTLY_USED = "openRecentlyUsed";

    private static final String PREVIEW_AUTO_CONTRAST = "previewAutoContrast";

    public static final String PROGRESS_CHANGED = "progressChanged";

    public static final String GET_RGF_THRESHOLD = "getRGFThreshold";

    public static final String GET_FRAG_THRESHOLD = "getFragThreshold";

    public static final String RELOAD_IMAGE = "reloadImage";

    private static final String REMOVE_ENCODING = "removeLineEncodingFromAlphabet";

    private static final String REMOVE_GLYPH = "removeGlyph";

    private static final String REMOVE_GLYPH_FRAGMENT = "removeGlyphFragment";

    private static final String REMOVE_NOISE = "removeNoise";

    private static final String REMOVE_TEXTLINE = "removeTextline";

    private static final String SAVE = "save";

    public static final String ZOOM_FIT = "zoomFit";

    public static final String CANCEL_TIMER = "cancelTimer";

    public static final String UPDATE_GLYPH_ANNO = "updateGlyphAnno";

    public static final String SEARCH_FOR_WORD = "searchForWord";

    public static final String SEARCH_ENGINE_PREFERENCES_DIALOG = "SearchEnginePreferenceDialog";

    public static final String HIGHLIGHT_GLYPHS = "highlightGlyphs";

    public static final String SET_DEFAULT_TOGGLE_STATUS = "setDefaultToggleStatus";

    private static final String SET_HANDWRITTEN_ANNOTATION = "setHandwrittenAnnotation";

    public static final String SET_TRANSCRIPTION = "setTranscription";

    public static final String SHOW_BINARIZE_DIALOG = "showBinarizeDialog";

    public static final String SHOW_BINARIZE_DIALOG_MASK = "showBinarizeDialogMask";

    private static final String SHOW_BINARIZED_IMAGE = "showBinarizedImage";

    public static final String SHOW_ELEMENTS = "showElements";

    public static final String EXPORT_STATISTIC = "exportStatistic";

    public static final String EXPORT_GLYPHS_GRAYSCALE = "exportGlyphsGrayscale";

    public static final String EXPORT_GLYPHS_BINARY = "exportGlyphsBinary";

    public static final String SHOW_GLYPHS = "showGlyphs";

    public static final String CALC_STATS = "calcStats";

    public static final String SHOW_FRAGMENTS = "showFragments";

    public static final String SHOW_GLYPH_EDITOR = "showGlyphEditor";

    public static final String SHOW_GLYPH_EDITOR_NEW = "showGlyphEditorNew";

    public static final String SHOW_GRAY_SCALE_DIALOG = "showGrayScaleDialog";

    public static final String SHOW_GALLERY_DIALOG = "showGalleryDialog";

    public static final String SHOW_IMAGE_NAME = "showImageName";

    public static final String SHOW_PAGE_LINES = "showPageLines";

    public static final String SHOW_TEMPLATE_MATCHING_GLYPHS = "showTemplateMatchingGlyphs";

    public static final String SHOW_TEMPLATE_MATCHING_GLYPHS_SINGLE_LINE = "showTemplateMatchingGlyphsSingleLine";

    private static final String SPLIT_IMAGE_LINE_APPLY = "splitImageLineApply";

    private static final String SPLIT_IMAGE_LINE_APPLY2 = "splitImageLineApply2";

    private static final String SPLIT_IMAGE_LINE_PREVIEW = "splitImageLinePreview";

    private static final String SPLIT_IMAGE_LINE_PREVIEW2 = "splitImageLinePreview2";

    public static final String STATUS_CHANGED = "statusChanged";

    private static final String TRANSCRIBE = "transcribe";

    public static final String UNKNOWN_SAVE_FILE = "unknownSaveFile";

    public static final String UPDATE_DOCUMENT_PAGE = "updateDocumentPage";

    public static final String UPDATE_RECENTLY_USED = "updateRecentlyUsed";

    public static final String UPDATE_SPLIT_LINE_SUGGESTION_DIALOG = "updateSplitSuggestionDialog";

    private static final String UPDATE_TEXTLINE_SIZE = "updateTextlineSize";

    private static final String SET_RGF_THRESHOLD = "setRGFThreshold";

    private static final String SET_FRAG_THRESHOLD = "setFragThreshold";

    private static final String SEPARATE_GLYPH = "separateGlyph";

    private static final String SQUARE_FRAG_GLYPH = "squareFragGlyph";

    private static final String ACCEPT_FRAGMENTS = "acceptFragments";

    private static final String ACCEPT_OVERLAPPING_GLYPH = "acceptOverlappingGlyph";

    private static final String EXTRACT_SPACES = "extractSpaces";

    private static final String REMOVE_SPACES_FROM_LINE = "removeSpacesFromLine";

    private static final String GET_CROPPED_IMAGE = "getCroppedImage";

    public static final String MAINFRAME_SET_RGF_THRESHOLD = "mainframeSetRGFThreshold";

    public static final String SHOW_SEPARATED_FRAGS = "showSeparatedFrags";

    public static final String SET_CROPPED_IMAGE = "setCroppedImage";

    /**
     * Accepts a glyph
     * 
     * @param glyph
     *            the glyph
     */
    public void acceptGlyph(final Glyph glyph) {
        this.setModelProperty(ACCEPT_GLYPH, glyph);
    }

    /**
     * Accepts a list of glyphs (used after template matching)
     * 
     * @param glyphs
     *            the list of glyphs
     */
    public void acceptGlyphs(final ArrayList<Glyph> glyphs) {
        this.setModelProperty(ACCEPT_GLYPHS, glyphs);
    }

    /**
     * Applies an autocontrast operation
     * 
     * @param imageRectangle
     *            The position and size of the area where this operation was
     *            applied to
     * @param saturationLeft
     *            The saturation on the left side of this histogram
     * @param saturationRight
     *            The saturation on the right side of this histogram
     * @param min
     *            The minimum pixel intensity
     * @param max
     *            The maximum pixel intensity
     * @param histogramm
     *            The histogram of the area where this operation is applied to
     */
    public void applyAutoContrast(final Rectangle imageRectangle,
            final double saturationLeft, final double saturationRight,
            final int min, final int max, final int[] histogramm) {
        this.setModelProperty(APPLY_AUTO_CONTRAST, new Object[] {
                imageRectangle, saturationLeft, saturationRight, min, max,
                histogramm });
    }

    /**
     * This method is used to binarize the image by clicking apply button
     * 
     * @param windowSize
     *            the size of window
     * @param k
     *            k value
     */
    public void binarizeApply(final int windowSize, final double k) {
        this.setModelProperty(BINARIZE_APPLY, new Object[] { windowSize, k });
    }

    /**
     * This method is used to binarize the image
     * 
     * @param windowSize
     *            the size of window
     * @param k
     *            k value
     * @param rect
     *            The position and size of the area where this operation was
     *            applied to
     */
    public void binarizeMask(final int windowSize, final double k,
            Rectangle rect) {
        this.setModelProperty(BINARIZE_MASK,
                new Object[] { windowSize, k, rect });
    }

    /**
     * This method is used to binarize the image as preview
     * 
     * @param windowSize
     *            the size of window
     * @param k
     *            k value
     */
    public void binarizePreview(final int windowSize, final double k) {
        this.setModelProperty(BINARIZE_PREVIEW, new Object[] { windowSize, k });
    }

    /**
     * This method is used to binarize the image as preview
     * 
     * @param windowSize
     *            the size of window
     * @param k
     *            k value
     * @param rect
     *            The position and size of the area where this operation was
     *            applied to
     */
    public void binarizePreview(final int windowSize, final double k,
            Rectangle rect) {
        this.setModelProperty(BINARIZE_PREVIEW_MASK, new Object[] { windowSize,
                k, rect });
    }

    /**
     * Browses to the next or previous image
     * 
     * @param next
     *            <code>true</code> if next image should be shown,
     *            <code>false</code> otherwise
     */
    public void browseImages(final boolean next) {
        this.setModelProperty(BROWSE_IMAGES, next);
    }

    /**
     * Cancels an autocontrast operation
     */
    public void cancelAutoContrast() {
        this.setModelProperty(CANCEL_AUTO_CONTRAST);
    }

    /**
     * This method is used to cancel the binarization
     */
    public void cancelBinarization() {
        this.setModelProperty(CANCEL_BINARIZATION);
    }

    /**
     * This method is used to cancel the process of gray image
     */
    public void cancelGray() {
        this.setModelProperty(CANCEL_GRAY);
    }

    /**
     * Updates a glyph
     * 
     * @param glyph
     *            the glyph
     */
    public void editGlyph(final Glyph glyph) {
        this.setModelProperty(EDIT_GLYPH, glyph);
    }

    /**
     * Exports the transcript as plaintext to filename
     * 
     * @param filename
     *            the filename
     */
    public void exportAsPlainText(final String filename) {
        this.setModelProperty(EXPORT_TRANSCRIPT_AS_PLAIN_TEXT, filename);
    }

    /**
     * Exports the transcript as rtf to filename
     * 
     * @param filename
     *            the filename
     * @param fontname
     *            the font
     */
    public void exportAsRichTextFormat(final String filename,
            final String fontname) {
        this.setModelProperty(EXPORT_TRANSCRIPT_AS_RTF, new Object[] {
                filename, fontname });
    }

    /**
     * Exports the transcript as tei to filename
     * 
     * @param filename
     *            the filename
     * @param source
     *            the source
     */
    public void exportAsTEI(final String filename, final String source) {
        this.setModelProperty(EXPORT_TRANSCRIPT_AS_TEI, new Object[] {
                filename, source });
    }

    /**
     * Exports the binary image to filename
     * 
     * @param filename
     *            the filename
     */
    public void exportBinaryImage(final String filename) {
        this.setModelProperty(EXPORT_BINARY_IMAGE, filename);
    }

    /**
     * Exports the grayscale image to filename
     * 
     * @param filename
     *            the filename
     */
    public void exportGrayscaleImage(final String filename) {
        this.setModelProperty(EXPORT_GRAYSCALE_IMAGE, filename);
    }

    /**
     * Exports the document to filename
     * 
     * @param filename
     *            the filename
     */
    public void exportPDFwithparentheses(final String filename) {
        this.setModelProperty(EXPORT_PDF_parentheses, filename);
    }

    /**
     * Exports the document to filename
     * 
     * @param filename
     *            the filename
     */
    public void exportPDFwithoutparentheses(final String filename) {
        this.setModelProperty(EXPORT_PDF_CLEAN, filename);
    }

    /**
     * Extracts a line at position and size rectangle
     * 
     * @param rectangle
     *            the position and size of the line
     */
    public void extractLine(final Rectangle rectangle) {
        this.setModelProperty(EXTRACT_LINE, rectangle);
    }

    /**
     * This method is used to extract rectangle.
     * 
     * @param rect
     *            the region of rectangle
     */
    public void extractRectangle(final Rectangle rect) {
        this.setModelProperty(EXTRACT_GLYPH, rect);
    }

    /**
     * Forces the model to find lines at the current documentimage
     */
    public void findLines(final int top, final int bottom) {
        this.setModelProperty(FIND_LINES, new Object[] { top, bottom });
    }

    /**
     * Gets the glyph with id ID
     * 
     * @param ID
     *            the id of the glyph
     */
    public void getGlyph(final String ID) {
        this.setModelProperty(GET_GLYPH, ID);
    }

    public void sortGlyphLeft(final String ID) {
        this.setModelProperty(SORT_GLYPH_LEFT, ID);
    }

    public void sortGlyphRight(final String ID) {
        this.setModelProperty(SORT_GLYPH_RIGHT, ID);
    }

    public void sortGlyphReset(final String ID) {
        this.setModelProperty(SORT_GLYPH_RESET, ID);
    }

    /**
     * Gets all glyphs and lines of the current image
     */
    public void getGlyphsAndLines() {
        this.setModelProperty(GET_GLYPHS_AND_LINES);
    }

    /**
     * Gets the histogram of the specified rectangular region
     * 
     * @param invalidationTrigger
     *            used to notify the view about changes within the rectangular
     *            region
     * @param rect
     *            the specified rectangular region
     * @param histogramm
     *            the histogramm
     */
    public void getHistogramm(final IntegerProperty invalidationTrigger,
            final Rectangle rect, final int[] histogramm) {
        this.setModelProperty(GET_HISTOGRAMM, new Object[] {
                invalidationTrigger, rect, histogramm });
    }

    /**
     * This method is used to gray the image by clicking the apply button
     * 
     * @param redWeight
     *            red weight
     * @param greenWeight
     *            green weight
     * @param blueWeight
     *            blue weight
     */
    public void grayApply(final double redWeight, final double greenWeight,
            final double blueWeight) {
        this.setModelProperty(GRAY_APPLY, new Object[] { redWeight,
                greenWeight, blueWeight });
    }

    /**
     * This method is used to preview the gray image according to the given RGB
     * weights
     * 
     * @param redWeight
     *            red weight
     * @param greenWeight
     *            green weight
     * @param blueWeight
     *            blue weight
     */
    public void grayPreview(final double redWeight, final double greenWeight,
            final double blueWeight) {
        this.setModelProperty(GRAY_PREVIEW, new Object[] { redWeight,
                greenWeight, blueWeight });
    }

    /**
     * This method is used to initialize digital
     * 
     * @param files
     *            image files
     */
    public void initDigital(final File[] files) {
        this.setModelProperty(INIT_DIGITAL, files);
    }

    /**
     * Forces the model to load data from hdd
     */
    public void loadFromHDD() {
        this.setModelProperty(LOAD_FROM_HDD_DIGITAL);
        this.setModelProperty(LOAD_FROM_HDD_TRANSCRIPT);
    }

    /**
     * This method is used to make the image
     * 
     * @param mask
     *            the region of mask
     */
    public void maskImage(final Rectangle mask) {
        this.setModelProperty(MASK_IMAGE, mask);
    }

    /**
     * Performs template matching with the glyph with id ID
     * 
     * @param ID
     *            the id of the glyph
     */
    public void matchTemplate(final String ID) {
        this.setModelProperty(MATCH_TEMPLATE, ID);
    }

    /**
     * Performs template matching with the glyph with id ID for a single line
     * 
     * @param ID
     *            the id of the glyph
     * @param lineID
     *            the id of the line
     */
    public void matchTemplateSingleLine(final String lineID) {
        this.setModelProperty(MATCH_TEMPLATE_SINGLE_LINE, lineID);
    }

    /**
     * Performs template matching with the glyph with id ID for a single line
     * 
     * @param ID
     *            the id of the glyph
     * @param lineID
     *            the id of the line
     */
    public void matchAllTemplatesSingleLine(final String lineID) {
        this.setModelProperty(MATCH_ALL_TEMPLATES_SINGLE_LINE, lineID);
    }

    /**
     * This method is used to open an image
     * 
     * @param image
     *            index of the images
     */
    public void openImage(final int image) {
        this.setModelProperty(OPEN_IMAGE, image);
    }

    /**
     * Opens the project with name filename
     * 
     * @param filename
     *            the name of the project
     */
    public void openProject(final String filename) {
        System.out.println("DocPanelController: openProj. : filename: "
                + filename);
        this.setModelProperty(OPEN_PROJECT, filename);
    }

    /**
     * Opens a recently used project and updates the list of recently used
     * projects
     * 
     * @param filename
     *            the name of the project
     */
    public void openRecentlyUsed(final String filename) {
        System.out.println("DocPanController: openRectentlyUsed filname: "
                + filename);
        this.setModelProperty(OPEN_RECENTLY_USED, filename);
    }

    /**
     * Forces a preview of the current autocontrast operation
     * 
     * @param imageRectangle
     *            The position and size of the area where this operation was
     *            applied to
     * @param saturationLeft
     *            The saturation on the left side of this histogram
     * @param saturationRight
     *            The saturation on the right side of this histogram
     * @param min
     *            The minimum pixel intensity
     * @param max
     *            The maximum pixel intensity
     * @param simple
     *            Property, used to notify the view about changes within the
     *            rectangular region
     * @param histogramm
     *            The histogram of the area where this operation is applied to
     */
    public void previewAutoContrast(final Rectangle imageRectangle,
            final double saturationLeft, final double saturationRight,
            final int min, final int max, final IntegerProperty simple,
            final int[] histogramm) {
        this.setModelProperty(PREVIEW_AUTO_CONTRAST, new Object[] {
                imageRectangle, saturationLeft, saturationRight, min, max,
                simple, histogramm });
    }

    /**
     * Removes the specified glyph from the current page
     * 
     * @param ID
     *            the id of the glyph
     */
    public void removeGlyph(final String ID) {
        this.setModelProperty(REMOVE_GLYPH, ID);
    }

    public void removeGlyphFragment(final String ID) {
        this.setModelProperty(REMOVE_GLYPH_FRAGMENT, ID);
    }

    /**
     * Removes the encoding of the line with id lineID from the current page and
     * the alphabet
     * 
     * @param lineID
     *            the id of the line
     */
    public void removeLineEncodingFromAlphabet(final String lineID) {
        this.setModelProperty(REMOVE_ENCODING, lineID);
    }

    /**
     * Removes the line with id id from the current page
     * 
     * @param id
     *            the id of the line
     */
    public void removeTextline(final String id) {
        this.setModelProperty(REMOVE_TEXTLINE, id);
    }

    /**
     * Saves the project at the current filename (if specified, save(final
     * String filename) will be called automatically otherwise)
     */
    public void save() {
        System.out.println("DocPanelController: save() ");
        this.setModelProperty(SAVE);
    }

    public void zoomFit() {
        this.setModelProperty(ZOOM_FIT);
    }

    public void cancelTimer() {
        this.setModelProperty(CANCEL_TIMER);
    }

    /**
     * Saves the project to filename
     * 
     * @param filename
     *            the filename
     */
    public void save(final String filename) {
        System.out.println("DocPanelController: save(String filename)");
        this.setModelProperty(SAVE, filename);
    }

    /**
     * Makes the model to search for a specified string in the transcription
     * 
     * @param toSearch
     *            the string to search for
     * @param caseSensitive
     *            <code>true</code> if case sensitive search, <code>false</code>
     *            otherwise
     */
    public void searchForWord(final String toSearch, final boolean caseSensitive) {
        this.setModelProperty(SEARCH_FOR_WORD, new Object[] { toSearch,
                caseSensitive });
    }

    /**
     * Makes the model to use the search engine
     * 
     * @param search
     *            parameters for the search engine
     */
    public void SearchEnginePreferenceDialog(final String searchPreferences) {
        this.setModelProperty(SEARCH_ENGINE_PREFERENCES_DIALOG,
                new Object[] { searchPreferences });
    }

    public void highlightGlyphs(final Glyph[] highlights) {
        this.setModelProperty(HIGHLIGHT_GLYPHS, highlights);
    }

    /**
     * Marks a line as handwritten annotation (if was not, will be unset
     * otherwise )
     * 
     * @param id
     *            the id of the line
     */
    public void setAnnotation(final String id) {
        this.setModelProperty(SET_HANDWRITTEN_ANNOTATION, id);
    }

    /**
     * Sets the toggle buttons to default (true)
     */
    public void setDefaultStatus() {
        this.setModelProperty(SET_DEFAULT_TOGGLE_STATUS, null);
    }

    /**
     * Shows the dialog to adapt the binarizing parameters
     */
    public void showBinarizeDialog() {
        this.setModelProperty(SHOW_BINARIZE_DIALOG);
    }

    /**
     * Shows the dialog to adapt the binarizing parameters
     */
    public void showBinarizeDialogMask() {
        this.setModelProperty(SHOW_BINARIZE_DIALOG_MASK);
    }

    /**
     * shows the binarized image
     */
    public void showBinarizedImage() {
        this.setModelProperty(SHOW_BINARIZED_IMAGE);
    }

    /**
     * Shows the dialog to adapt the grayscale parameters
     */
    public void showGrayScaleDialog() {
        this.setModelProperty(SHOW_GRAY_SCALE_DIALOG);
    }

    /**
     * Shows the glyph gallery dialog
     */
    public void showGalleryDialog(final String character) {
        this.setModelProperty(SHOW_GALLERY_DIALOG, character);
    }

    /**
     * Applies the split line suggestion
     * 
     * @param splits
     *            the lines to split at
     * @param lineID
     *            the id of the line
     */
    public void splitImageLineApply(final ArrayList<Line> splits,
            final String lineID) {
        this.setModelProperty(SPLIT_IMAGE_LINE_APPLY, new Object[] { splits,
                lineID });
    }

    /**
     * Applies the split line suggestion
     * 
     * @param splits
     *            the lines to split at
     * @param lineID
     *            the id of the line
     */
    public void splitImageLineApply2(final ArrayList<Line> splits,
            final String lineID, final int fragSize, final float fragCount,
            final int noiseThres) {
        this.setModelProperty(SPLIT_IMAGE_LINE_APPLY2, new Object[] { splits,
                lineID, fragSize, fragCount, noiseThres });
    }

    /**
     * Forces a preview of the split line suggestion
     * 
     * @param lineID
     *            the id of the line
     * @param width
     *            the estimated average character width
     */
    public void splitImageLinePreview(final String lineID, final int width) {
        this.setModelProperty(SPLIT_IMAGE_LINE_PREVIEW, new Object[] { lineID,
                width });
    }

    /**
     * Forces a preview of the split line suggestion
     * 
     * @param lineID
     *            the id of the line
     * @param width
     *            the estimated average character width
     */
    public void splitImageLinePreview2(final String lineID, final int width) {
        this.setModelProperty(SPLIT_IMAGE_LINE_PREVIEW2, new Object[] { lineID,
                width });
    }

    /**
     * Transcribes the specified line
     * 
     * @param text
     *            the transcribed text
     * @param lineID
     *            the id of the line to transcribe
     */
    public void transcribe(final String text, final String lineID,
            final boolean forceSplit) {
        this.setModelProperty(TRANSCRIBE, new Object[] { text, lineID,
                forceSplit });
    }

    /**
     * Updates the position and size (bounds) of a line
     * 
     * @param bounds
     *            the new bounds
     */
    public void updateTextlineSize(final Rectangle bounds) {
        this.setModelProperty(UPDATE_TEXTLINE_SIZE, bounds);
    }

    /**
     * Removes glyphs with very small surface
     */
    public void removeNoise() {
        this.setModelProperty(REMOVE_NOISE);
    }

    public void setRGFThreshold(final Float thres) {
        // System.out.println("setRGFThreshold");
        this.setModelProperty(SET_RGF_THRESHOLD, thres);

    }

    public void setFragThreshold(final Float thres) {
        // System.out.println("setFragThreshold");
        this.setModelProperty(SET_FRAG_THRESHOLD, thres);
    }

    public void separateGlyph(final Glyph glyph) {
        ++DiptychonPreferences.glyphSeparateByRGFCounter;
        this.setModelProperty(SEPARATE_GLYPH, glyph);
    }

    public void separateGlyph(final Glyph glyph,
            final ArrayList<Point2D> sepLine) {
        ++DiptychonPreferences.glyphSeparateByLineCounter;
        this.setModelProperty(SEPARATE_GLYPH, new Object[] { glyph, sepLine });
    }

    public void squareFragGlyph(final Glyph glyph, final int squareSize) {
        // System.out.println("squareFragGlyph");
        this.setModelProperty(SQUARE_FRAG_GLYPH, new Object[] { glyph,
                squareSize });
    }

    public void acceptFragments(final ArrayList<Glyph> fragments,
            final String lineID) {
        System.out.println("acceptFragments");
        this.setModelProperty(ACCEPT_FRAGMENTS, new Object[] { fragments,
                lineID });
        System.out.println();
        System.out.println("------------------------------------");
        System.out.println("separation by RGF  "
                + DiptychonPreferences.glyphSeparateByRGFCounter);
        System.out.println("Line separation    "
                + DiptychonPreferences.glyphSeparateByLineCounter);
        System.out.println("Merging            "
                + DiptychonPreferences.glyphMergingCounter);
        System.out.println("Anzahl Glyphen     " + fragments.size());
        System.out.println("");
        double alleInteraktionen = (DiptychonPreferences.glyphSeparateByRGFCounter
                + DiptychonPreferences.glyphSeparateByLineCounter + DiptychonPreferences.glyphMergingCounter)
                / (1.0 * fragments.size());
        double nurTrennungen = (DiptychonPreferences.glyphSeparateByRGFCounter + DiptychonPreferences.glyphSeparateByLineCounter)
                / (1.0 * fragments.size());
        System.out.println("Alle Interaktionen " + alleInteraktionen);
        System.out.println("Nur Trennungen     " + nurTrennungen);
        System.out.println("------------------------------------");
        DiptychonPreferences.glyphMergingCounter = 0;
        DiptychonPreferences.glyphSeparateByRGFCounter = 0;
        DiptychonPreferences.glyphSeparateByLineCounter = 0;
    }

    public void acceptOverlappingGlyph(Glyph glyph) {
        // System.out.println("acceptFragments");
        this.setModelProperty(ACCEPT_OVERLAPPING_GLYPH, glyph);
    }

    public void acceptOverlappingGlyph(Glyph glyph, final String lineID) {
        // System.out.println("acceptFragments");
        this.setModelProperty(ACCEPT_OVERLAPPING_GLYPH, new Object[] { glyph,
                lineID });
    }

    public void extractSpaces(final ArrayList<int[]> spaceCoords,
            final String lineID) {
        // System.out.println("extractSpaces");
        this.setModelProperty(EXTRACT_SPACES, new Object[] { spaceCoords,
                lineID });
    }

    public void removeSpacesFromLine(final String lineID) {
        // System.out.println("extractSpaces");
        this.setModelProperty(REMOVE_SPACES_FROM_LINE, lineID);
    }

    public void getCroppedImage(final Rectangle bounds) {
        // System.out.println("getCroppedImage");
        this.setModelProperty(GET_CROPPED_IMAGE, bounds);
    }

    public void showElements() {
        // System.out.println("showElements");
        this.setModelProperty(SHOW_ELEMENTS);
    }

    public void exportStatistic() {
        this.setModelProperty(EXPORT_STATISTIC);
        System.out.println("exportStatistic()");
        DiptychonPreferences.glyphMergingCounter = 0;
        DiptychonPreferences.glyphSeparateByRGFCounter = 0;
        DiptychonPreferences.glyphSeparateByLineCounter = 0;
    }

    public void exportGlyphsBinary() {
        this.setModelProperty(EXPORT_GLYPHS_BINARY);
    }

    public void exportGlyphsGrayscale() {
        this.setModelProperty(EXPORT_GLYPHS_GRAYSCALE);
    }

    public void showGlyphs() {
        // System.out.println("showGlyphs");
        this.setModelProperty(SHOW_GLYPHS);
    }

    public void calcStats() {
        System.out.println("calcStats");
        this.setModelProperty(CALC_STATS);
    }
}
