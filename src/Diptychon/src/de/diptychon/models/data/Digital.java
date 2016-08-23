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
package de.diptychon.models.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.geometry.Point2D;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import de.diptychon.DiptychonLogger;
import de.diptychon.controller.DocumentPanelController;
import de.diptychon.models.AbstractModel;
import de.diptychon.models.algorithms.validation.Moments;
import de.diptychon.models.algorithms.validation.PolygonComparison;
import de.diptychon.models.misc.AutoContrastOperation;
import de.diptychon.models.misc.ImageFileFilter;
import de.diptychon.models.misc.ImageUtils;
import de.diptychon.models.misc.PDFCreator;
import de.diptychon.models.representation.Transcript;

/**
 * This class is a model which inherites from the AbstractModel. It provides
 * multiple methods to interact with views and operate digital images and
 * glyphs.
 */
public class Digital extends AbstractModel implements Serializable {

    /**
     * SerialVersionUID to keep used data consistent.
     */
    private static final long serialVersionUID = 20130403;

    /**
     * Reference to the corresponding transcript
     */
    private final Transcript transcript;

    /**
     * The array which stores the information of the used document images
     */
    private DocumentImage[] documentImages;

    /**
     * The current view index in the array of documentImages
     */
    private int activeImage;

    /**
     * It is the path to the file. When concerning multiple files, it is the
     * path to the folder.
     */
    private String path;

    /**
     * The filename of the current project
     */
    private String saveFilename;

    /**
     * <code>True</code> if the binarized image is shown, <code>false</code>
     * otherwise
     */
    private transient boolean showBinarizedImage;

    /**
     * This method is used to construct and initialize the object - Digital
     */
    public Digital() {
        super();
        this.transcript = new Transcript(this);
        this.showBinarizedImage = false;
    }

    /**
     * Accepts a glyph and checks in which line it might be contained. If there
     * is no line, the glyph will be added without corresponding line
     * 
     * @param glyph
     *            the glyph
     */
    public void acceptGlyph(final Glyph glyph) {
        final String lineID = this.activeImage().acceptGlyph(glyph, true);
        if (!lineID.isEmpty()) {
            final ArrayList<Glyph> glyphs = new ArrayList<>(this.activeImage()
                    .getGlyphs(lineID));
            Collections.sort(glyphs);
            this.transcript.updateTranscription(lineID, glyphs);
            this.activeImage().removeGlyphsFromLine(lineID);
            this.activeImage().setGlyphs(lineID, glyphs);
        }
        this.showElements();
        this.notifyChange();
    }

    /**
     * Accepts a glyph and checks in which line it might be contained. If there
     * is no line, the glyph will be added without corresponding line
     * 
     * @param glyph
     *            the glyph
     */
    public void acceptOverlappingGlyph(final Glyph glyph) {
        final String lineID = this.activeImage().acceptOverlappingGlyph(glyph,
                true);
        if (!lineID.isEmpty()) {
            final ArrayList<Glyph> glyphs = new ArrayList<>(this.activeImage()
                    .getGlyphs(lineID));
            Collections.sort(glyphs);
            this.transcript.updateTranscription(lineID, glyphs);
            this.activeImage().removeGlyphsFromLine(lineID);
            this.activeImage().setGlyphs(lineID, glyphs);
        }
        this.showElements();
        this.notifyChange();
    }

    public void acceptOverlappingGlyph(final Object[] glyphAndLineID) {
        String lineID = (String) glyphAndLineID[1];
        if (this.activeImage().acceptOverlappingGlyph(
                (Glyph) glyphAndLineID[0], false, lineID)) {
            final ArrayList<Glyph> glyphs = new ArrayList<>(this.activeImage()
                    .getGlyphs(lineID));
            Collections.sort(glyphs);
            this.transcript.updateTranscription(lineID, glyphs);
            this.activeImage().removeGlyphsFromLine(lineID);
            this.activeImage().setGlyphs(lineID, glyphs);
            this.showElements();
            this.notifyChange();
        } else {
            ((Glyph) glyphAndLineID[0]).setLineID("badLine");
        }

    }

    /**
     * Accepts a glyph and checks in which line it might be contained. If there
     * is no line, the glyph will be added without corresponding line. In
     * contrast to acceptGlyph(), the view is not updated after accepting the
     * glyph
     * 
     * @param glyph
     *            the glyph
     */
    public void acceptGlyph2(final Glyph glyph) {
        final String lineID = this.activeImage().acceptGlyph(glyph, true);
        if (!lineID.isEmpty()) {
            final ArrayList<Glyph> glyphs = new ArrayList<>(this.activeImage()
                    .getGlyphs(lineID));
            Collections.sort(glyphs);
            this.transcript.updateTranscription(lineID, glyphs);
            this.activeImage().removeGlyphsFromLine(lineID);
            this.activeImage().setGlyphs(lineID, glyphs);
        }
    }

    /**
     * Accepts a fragment and checks in which line it might be contained. If
     * there is no line, the fragment will be added without corresponding line
     * 
     * @param glyph
     *            the fragment
     */
    public void acceptFragment(final Glyph glyph, final String lineID) {
        this.activeImage().acceptFragment(glyph, true, lineID);
        final ArrayList<Glyph> glyphs = new ArrayList<>(this.activeImage()
                .getGlyphs(lineID));
        Collections.sort(glyphs);
        this.transcript.updateTranscription(lineID, glyphs);
        this.activeImage().removeGlyphsFromLine(lineID);
        this.activeImage().setGlyphs(lineID, glyphs);
    }

    /**
     * Updates the glyph after user interaction
     * 
     * @param glyph
     *            the glyph
     */
    public void editGlyph(final Glyph glyph) {
        this.activeImage().editGlyph(glyph);
        this.showElements();
        this.notifyChange();
    }

    public void sortGlyphReset(final String ID) {
        if (this.activeImage().getGlyph(null, ID) == null) {
            return;
        }
        String lineID = this.activeImage().getGlyph(null, ID).getLineID();
        final ArrayList<Glyph> glyphs = new ArrayList<>(this.activeImage()
                .getGlyphs(lineID));
        this.activeImage().sortGlyphReset(ID);
        Collections.sort(glyphs);
        this.transcript.updateTranscription(lineID, glyphs);
        this.activeImage().removeGlyphsFromLine(lineID);
        this.activeImage().setGlyphs(lineID, glyphs);

        this.showElements();
        this.notifyChange();
    }

    public void sortGlyphLeft(final String ID) {
        if (this.activeImage().getGlyph(null, ID) == null) {
            return;
        }
        String lineID = this.activeImage().getGlyph(null, ID).getLineID();
        final ArrayList<Glyph> glyphs = new ArrayList<>(this.activeImage()
                .getGlyphs(lineID));
        this.activeImage().sortGlyphLeft(ID, lineID);
        Collections.sort(glyphs);
        this.transcript.updateTranscription(lineID, glyphs);
        this.activeImage().removeGlyphsFromLine(lineID);
        this.activeImage().setGlyphs(lineID, glyphs);

        this.showElements();
        this.notifyChange();
    }

    public void sortGlyphRight(final String ID) {
        if (this.activeImage().getGlyph(null, ID) == null) {
            return;
        }
        String lineID = this.activeImage().getGlyph(null, ID).getLineID();
        final ArrayList<Glyph> glyphs = new ArrayList<>(this.activeImage()
                .getGlyphs(lineID));
        this.activeImage().sortGlyphRight(ID, lineID);
        Collections.sort(glyphs);
        this.transcript.updateTranscription(lineID, glyphs);
        this.activeImage().removeGlyphsFromLine(lineID);
        this.activeImage().setGlyphs(lineID, glyphs);

        this.showElements();
        this.notifyChange();
    }

    /**
     * This method is used to extract spaces through given coordinates
     * 
     * @param spaceCoords
     *            the space coordinates
     * @return
     */
    @SuppressWarnings("unchecked")
    public void extractSpaces(final Object[] spacesAndLineID) {
        this.acceptFragments(new Object[] {
                this.activeImage().extractSpaces(
                        (ArrayList<int[]>) spacesAndLineID[0]),
                spacesAndLineID[1] });
    }

    /**
     * This method is used to remove spaces from a given line
     * 
     * @param lineID
     *            the line ID
     * @return
     */
    public void removeSpacesFromLine(final String lineID) {
        this.activeImage().removeSpacesFromLine(lineID);
    }

    /**
     * Removes glyphs with a very small surface
     */
    public void removeNoise() {
        final ArrayList<Glyph> glyphs = new ArrayList<>(this.activeImage()
                .getAllGlyphs());
        for (Glyph g : glyphs) {
            if (g.isSpace)
                return;
            if (g.getWidth() * g.getHeight() < (float) (this.activeImage()
                    .getGrayImage().getWidth()
                    * this.activeImage().getGrayImage().getHeight() * 0.000004)) {
                final String lineID = this.activeImage().removeGlyph(g.getID());
                if (!lineID.isEmpty()) {
                    final ArrayList<Glyph> lglyphs = new ArrayList<>(this
                            .activeImage().getGlyphs(lineID));
                    Collections.sort(lglyphs);
                    this.transcript.updateTranscription(lineID, lglyphs);
                    this.activeImage().removeGlyphsFromLine(lineID);
                    this.activeImage().setGlyphs(lineID, lglyphs);
                }
            }
        }
        this.showElements();
        this.notifyChange();
    }

    /**
     * Removes the glyph with id id
     * 
     * @param id
     *            the id of the glyph
     */
    public void removeGlyph(final String id) {
        final String lineID = this.activeImage().removeGlyph(id);
        if (!lineID.isEmpty()) {
            final ArrayList<Glyph> glyphs = new ArrayList<>(this.activeImage()
                    .getGlyphs(lineID));
            Collections.sort(glyphs);
            this.transcript.updateTranscription(lineID, glyphs);
            this.activeImage().removeGlyphsFromLine(lineID);
            this.activeImage().setGlyphs(lineID, glyphs);
        }
        this.showElements();
        this.notifyChange();
    }

    /**
     * Removes a fragment that is a former glyph with id id
     * 
     * @param id
     *            the id of the glyph
     */
    public void removeGlyphFragment(final String id) {
        this.activeImage().removeGlyph(id);
        this.showElements();
        this.notifyChange();
    }

    /**
     * Accepts a list of glyphs and adds them to corresponding lines, if exists
     * 
     * @param glyphs
     *            the list of glyphs
     */
    public void acceptGlyphs(final ArrayList<Glyph> glyphs) {
        for (final Glyph g : glyphs) {
            this.acceptGlyph2(g);
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Digital.this.showElements();
                Digital.this.notifyChange();
            }
        });
    }

    /**
     * Accepts a list of fragments and adds them to corresponding lines, if
     * exists
     * 
     * @param glyphs
     *            the list of fragments
     */
    @SuppressWarnings("unchecked")
    public void acceptFragments(final Object[] fragmentsAndLineID) {
        for (final Glyph g : (ArrayList<Glyph>) fragmentsAndLineID[0]) {
            this.acceptFragment(g, (String) fragmentsAndLineID[1]);
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Digital.this.showElements();
                Digital.this.notifyChange();
            }
        });
    }

    /**
     * Gets the current documentimage
     * 
     * @return the current documentimage
     */
    private DocumentImage activeImage() {
        return this.documentImages[this.activeImage];
    }

    /**
     * Forces to show the binarized image instead of the grayscale (default)
     */
    public void showBinarizedImage() {
        this.showBinarizedImage = !this.showBinarizedImage;
        this.firePropertyChange(DocumentPanelController.RELOAD_IMAGE, null,
                this.activeImage().showBinarizedImage(this.showBinarizedImage));
    }

    public void showBinarizedImageForced() {
        this.showBinarizedImage = true;
        this.firePropertyChange(DocumentPanelController.RELOAD_IMAGE, null,
                this.activeImage().showBinarizedImage(this.showBinarizedImage));
    }

    public void separateGlyph(final Glyph glyph) {
        this.showSeparatedFrags(this.activeImage().separateGlyph(glyph));
    }

    @SuppressWarnings("unchecked")
    public void separateGlyph(final Object[] glyphAndSepLine) {
        this.showSeparatedFrags(this.activeImage().separateGlyph(
                (Glyph) glyphAndSepLine[0],
                (ArrayList<Point2D>) glyphAndSepLine[1]));
    }

    public void squareFragGlyph(final Object[] glyphAndSize) {
        this.showSeparatedFrags(this.activeImage().squareFragGlyph(
                (Glyph) glyphAndSize[0], (Integer) glyphAndSize[1]));
    }

    /**
     * Sends all elements (lines and glyphs) to the views
     */
    public void showElements() {
        this.firePropertyChange(DocumentPanelController.SHOW_ELEMENTS, null,
                new Object[] { this.activeImage().getLines(),
                        this.activeImage().getAllGlyphs() });
    }

    public void showGlyphs() {
        this.firePropertyChange(DocumentPanelController.SHOW_GLYPHS, null,
                new Object[] { this.activeImage().getAllGlyphs() });
    }

    /**
     * Sends all elements (lines and glyphs) to the views
     * 
     * @param fragments
     */
    private void showFragments(ArrayList<Glyph> fragments) {
        this.showElements();
        this.firePropertyChange(DocumentPanelController.SHOW_FRAGMENTS, null,
                new Object[] { this.activeImage().getLines(), fragments });
    }

    public void calcStats() {
        this.updateGlyphStats();
        this.updateWordStats();
        this.updateLineStats();
        this.updateDocumentStats();
    }

    /**
     * Sends all elements (lines and glyphs) to the views
     */
    private void showSeparatedFrags(ArrayList<Glyph> fragments) {
        this.firePropertyChange(DocumentPanelController.SHOW_SEPARATED_FRAGS,
                null, new Object[] { fragments });
    }

    /**
     * Applies an autocontrast operation to the image
     * 
     * @param autoContrastParameters
     *            the parameters of an autocontrast operation
     * @see de.diptychon.models.misc.AutoContrastOperation
     */
    public void applyAutoContrast(final Object[] autoContrastParameters) {

        final AutoContrastOperation aco = new AutoContrastOperation(
                (Rectangle) autoContrastParameters[0],
                (double) autoContrastParameters[1],
                (double) autoContrastParameters[2],
                (int) autoContrastParameters[3],
                (int) autoContrastParameters[4],
                (int[]) autoContrastParameters[5]);
        this.activeImage().addAutoContrastOperationToHistory(aco);
        this.activeImage().autoContrastImage(aco);
        this.showCurrentDocumentImage();
        this.notifyChange();
    }

    /**
     * Searches for a string within the transcript of the current page/image
     * 
     * @param searchWordCaseSensitve
     *            the string to search for and a flag whether to perform the
     *            search case sensitive or not
     */
    public void searchForWord(final Object[] searchWordCaseSensitve) {
        final ArrayList<String[]> results = this.transcript.searchForWord(
                (String) searchWordCaseSensitve[0],
                (boolean) searchWordCaseSensitve[1]);
        this.firePropertyChange(DocumentPanelController.SEARCH_FOR_WORD, null,
                this.activeImage().getSearchedGlyphs(results));
    }

    public void highlightGlyphs(final Glyph[] higlights) {
        final ArrayList<Glyph[]> show = new ArrayList<Glyph[]>();
        show.add(higlights);
        this.firePropertyChange(DocumentPanelController.SEARCH_FOR_WORD, null,
                show);
    }

    /**
     * This method is used to binarize the image.
     * 
     * @param binarizeParameters
     *            Windows size value and K value
     */
    public void binarizeApply(final Object[] binarizeParameters) {
        this.activeImage().binarize((int) binarizeParameters[0],
                (double) binarizeParameters[1]);
        this.showBinarizedImage = true;
        this.showCurrentDocumentImage();
        this.notifyChange();
        this.firePropertyChange(
                DocumentPanelController.BINARIZED_BUTTON_PRESSED, null, null);
    }

    public void binarizeMask(final Object[] binarizeParameters) {
        this.activeImage().binarize((int) binarizeParameters[0],
                (double) binarizeParameters[1],
                (Rectangle) binarizeParameters[2]);
        this.showBinarizedImage = true;
        this.showCurrentDocumentImage();
        this.notifyChange();
        this.firePropertyChange(
                DocumentPanelController.BINARIZED_BUTTON_PRESSED, null, null);

    }

    /**
     * This method is used to preview the binarized image.
     * 
     * @param binarizeParameters
     *            Windows size value and k value
     */
    public void binarizePreview(final Object[] binarizeParameters) {
        this.activeImage().binarize((int) binarizeParameters[0],
                (double) binarizeParameters[1]);
        this.showBinarizedImage = true;
        this.firePropertyChange(DocumentPanelController.RELOAD_IMAGE, null,
                this.activeImage().getBinarizedAsFXImage());
    }

    public void binarizePreviewMask(final Object[] binarizeParameters) {
        this.activeImage().binarize((int) binarizeParameters[0],
                (double) binarizeParameters[1],
                (Rectangle) binarizeParameters[2]);
        this.showBinarizedImage = true;
        this.firePropertyChange(DocumentPanelController.RELOAD_IMAGE, null,
                this.activeImage().getBinarizedAsFXImage());
    }

    /**
     * Sends the binarized image to the view and forces the binarization dialog
     * to open
     */
    public void showBinarizeDialog() {
        this.activeImage().backupBinaryImage();
        this.firePropertyChange(DocumentPanelController.RELOAD_IMAGE, null,
                this.activeImage().getBinarizedAsFXImage());
        this.firePropertyChange(DocumentPanelController.SHOW_BINARIZE_DIALOG,
                null, this.activeImage().getBinarizeParameters());

    }

    public void showBinarizeDialogMask() {
        this.activeImage().backupBinaryImage();
        this.firePropertyChange(DocumentPanelController.RELOAD_IMAGE, null,
                this.activeImage().getBinarizedAsFXImage());
        this.firePropertyChange(
                DocumentPanelController.SHOW_BINARIZE_DIALOG_MASK, null, this
                        .activeImage().getBinarizeParameters());

    }

    /**
     * Sends the binarized image to the view and forces the grayscale dialog to
     * open
     */
    public void showGrayScaleDialog() {
        this.firePropertyChange(DocumentPanelController.RELOAD_IMAGE, null,
                this.activeImage().getGrayImageAsFXImage());
        this.firePropertyChange(DocumentPanelController.SHOW_GRAY_SCALE_DIALOG,
                null, this.activeImage().getGrayParameters());
    }

    /**
     * Sends the glyphs to the view and forces the glyph gallery dialog to open
     */
    public void showGalleryDialog(final String character) {
        ArrayList<Glyph> glyphs = new ArrayList<Glyph>();
        for (Glyph g : this.activeImage().getAllGlyphs()) {
            if (g.getGroupID().equals(character)) {
                glyphs.add(g);
            }
        }
        this.firePropertyChange(DocumentPanelController.SHOW_GALLERY_DIALOG,
                null, glyphs);
    }

    /**
     * Splits an imageline by using the information of the transcription
     * 
     * @param decoding
     *            the transcription
     * @param encoding
     *            the encoded transcription
     * @param lineID
     *            the id of the line
     */
    public void splitImageLine(final String[] decoding,
            final String[] encoding, final String lineID) {
        final Line[] splits = this.activeImage().splitImageLine(decoding,
                encoding, lineID);
        this.firePropertyChange(
                DocumentPanelController.INIT_SPLIT_SUGGESTION_DIALOG, null,
                new Object[] { splits, lineID });
    }

    /**
     * Applies a split line suggestion
     * 
     * @param splitsAndLineID
     *            the splitlines and the line id
     */
    @SuppressWarnings("unchecked")
    public void splitImageLineApply(final Object[] splitsAndLineID) {
        final String lineID = (String) splitsAndLineID[1];
        final String[][] decodingEncoding = this.transcript
                .getLineTranscription(lineID);
        this.activeImage().splitImageLineApply(decodingEncoding[0],
                decodingEncoding[1], (ArrayList<Line>) splitsAndLineID[0],
                lineID);
        this.showElements();
        this.updateGlyphStats();
        this.notifyChange();
    }

    /**
     * Applies a split line suggestion
     * 
     * @param splitsAndLineID
     *            the splitlines and the line id
     */
    @SuppressWarnings("unchecked")
    public void splitImageLineApply2(
            final Object[] splitsAndLineIDAndFragSizeAndFragCountAndNoiseThres) {
        final String lineID = (String) splitsAndLineIDAndFragSizeAndFragCountAndNoiseThres[1];
        String[][] decodingEncoding = this.transcript
                .getLineTranscription(lineID);
        if (decodingEncoding.length < 2) {
            decodingEncoding = new String[2][1];
        }

        this.showFragments(this
                .activeImage()
                .splitImageLineApply2(
                        decodingEncoding[0],
                        decodingEncoding[1],
                        (ArrayList<Line>) splitsAndLineIDAndFragSizeAndFragCountAndNoiseThres[0],
                        lineID,
                        (Integer) splitsAndLineIDAndFragSizeAndFragCountAndNoiseThres[2],
                        (Float) splitsAndLineIDAndFragSizeAndFragCountAndNoiseThres[3],
                        (Integer) splitsAndLineIDAndFragSizeAndFragCountAndNoiseThres[4]));
        this.notifyChange();
    }

    /**
     * Previews a split line suggestion
     * 
     * @param splitsAndLineID
     *            the splitlines and the line id
     */
    public void splitImageLinePreview(final Object[] splitsAndLineID) {
        final String lineID = (String) splitsAndLineID[0];
        final String[][] decodingEncoding = this.transcript
                .getLineTranscription(lineID);

        final Line[] splits = this.activeImage().splitImageLinePreview(
                decodingEncoding[0], decodingEncoding[1], lineID,
                (int) splitsAndLineID[1]);
        this.firePropertyChange(
                DocumentPanelController.UPDATE_SPLIT_LINE_SUGGESTION_DIALOG,
                null, new Object[] { splits, lineID });
    }

    /**
     * Previews a split line suggestion
     * 
     * @param splitsAndLineID
     *            the splitlines and the line id
     */
    public void splitImageLinePreview2(final Object[] splitsAndLineID) {
        final String lineID = (String) splitsAndLineID[0];
        final String[][] decodingEncoding = this.transcript
                .getLineTranscription(lineID);

        final Line[] splits = this.activeImage().splitImageLinePreview2(
                decodingEncoding[0], decodingEncoding[1], lineID,
                (int) splitsAndLineID[1]);
        this.firePropertyChange(
                DocumentPanelController.UPDATE_SPLIT_LINE_SUGGESTION_DIALOG,
                null, new Object[] { splits, lineID });
    }

    /**
     * This method is used to open the next image or previous image depending
     * boolean next and notify the changes to the view
     * 
     * @param next
     *            Boolean vaule true means next page or previous page
     */
    public void browseImages(final Boolean next) {
        this.activeImage().unload();
        if (next) {
            this.activeImage = this.activeImage == this.documentImages.length - 1 ? 0
                    : ++this.activeImage;

        } else {
            this.activeImage = this.activeImage == 0 ? this.documentImages.length - 1
                    : --this.activeImage;

        }
        this.openImage(this.activeImage);
        // activeImage+1 is necessary to display the correct image number
        // the visual representation does not start with 0 but 1
        this.firePropertyChange(DocumentPanelController.BROWSE_IMAGES, null,
                this.activeImage + 1);
        this.notifyChange();
    }

    /**
     * Cancels an autocontrast operation
     */
    public void cancelAutoContrast() {
        this.activeImage().resetToGray();
        this.activeImage().reapplyAutocontrast();
        this.showCurrentDocumentImage();
    }

    /**
     * This method is used to cancel the binarization.
     */
    public void cancelBinarization() {
        this.activeImage().cancelBinarization();
        this.showBinarizedImageForced();
    }

    /**
     * This method is used to cancel the gray scale operation.
     */
    public void cancelGray() {
        this.activeImage().cancelGray();
        this.showCurrentDocumentImage();
    }

    /**
     * This method is used to extract the glyph which is marked.
     * 
     * @param mask
     *            mask is the region which is marked.
     */
    public void extractGlyph(final Rectangle mask) {
        final Glyph g = this.activeImage().extractGlyph(mask);
        this.firePropertyChange(DocumentPanelController.SHOW_GLYPH_EDITOR_NEW,
                null, g);
    }

    /**
     * Extracts a line at the specified rectangular region
     * 
     * @param rectangle
     *            the rectangular region
     */
    public void extractLine(final Rectangle rectangle) {
        final ImageLine il = this.activeImage().addLine(rectangle);
        this.transcript.addLine(il.getID(), rectangle);
        final ArrayList<Glyph> glyphs = new ArrayList<>(this.activeImage()
                .getGlyphs(rectangle.getId()));
        Collections.sort(glyphs);
        this.transcript.updateTranscription(rectangle.getId(), glyphs);
        this.showElements();
        this.notifyChange();
    }

    /**
     * Perfroms an automatic line extraction
     */
    public void findLines(final Object[] topAndBottom) {
        final Collection<ImageLine> imageLines = this.activeImage().findLines(
                (int) topAndBottom[0], (int) topAndBottom[1]);
        this.transcript.addLines(imageLines);
        this.showElements();
        this.notifyChange();
    }

    /**
     * Exports the binarized image to filename
     * 
     * @param filename
     *            the filename
     */
    public void exportBinaryImage(final String filename) {
        ImageUtils.writeGrayscaleImage(filename, this.activeImage()
                .getGrayImage());
    }

    /**
     * Exports the grayscale image to filename
     * 
     * @param filename
     *            the filename
     */
    public void exportGrayscaleImage(final String filename) {
        ImageUtils.writeGrayscaleImage(filename, this.activeImage()
                .getBinarizedImage());
    }

    /**
     * exports PDF with parentheses for abbreviations
     *
     * @param filename
     *            the filename
     */
    public void exportPDFwithoutparentheses(final String filename) {
        // System.out.println(this.path);
        PDFCreator.createPDF(filename, this.documentImages, this.path,
                transcript, false);
    }

    /**
     * exports PDF with parentheses for abbreviations
     *
     * @param filename
     *            the filename
     */
    public void exportPDFwithparentheses(final String filename) {
        PDFCreator.createPDF(filename, this.documentImages, this.path,
                transcript, true);
    }

    /**
     * Sends the glyph with id ID to the view
     * 
     * @param ID
     *            the id
     */
    public void getGlyph(final String ID) {
        this.firePropertyChange(DocumentPanelController.SHOW_GLYPH_EDITOR,
                null, this.activeImage().getGlyph(null, ID));

        if (this.activeImage().getGlyph(null, ID) != null) {

            String lineID = this.activeImage().getGlyph(null, ID).getLineID();
            if (lineID == null) {
                return;
            }
            final ArrayList<Glyph> glyphs = new ArrayList<>(this.activeImage()
                    .getGlyphs(lineID));
            Collections.sort(glyphs);
            this.transcript.updateTranscription(lineID, glyphs);
            this.activeImage().removeGlyphsFromLine(lineID);
            this.activeImage().setGlyphs(lineID, glyphs);

            this.showElements();
            this.notifyChange();
        }
    }

    /**
     * Sends all elements (lines and glyphs) to the views
     */
    public void getGlyphsAndLines() {
        this.firePropertyChange(DocumentPanelController.SHOW_ELEMENTS, null,
                new Object[] { this.activeImage().getLines(),
                        this.activeImage().getAllGlyphs() });
    }

    /**
     * Gets the histogramm
     */
    public void getHistogramm(final Object[] autoContrastParameters) {
        final Rectangle imageRectangle = (Rectangle) autoContrastParameters[1];

        int[] histogramm = (int[]) autoContrastParameters[2];
        histogramm = this.activeImage().getHistogramInRectangle(imageRectangle,
                histogramm);

        final IntegerProperty simple = (IntegerProperty) autoContrastParameters[0];
        simple.set(simple.get() + 1);
    }

    /**
     * This method is used to extract absolute file path from the array of file
     * objects and extract the names of each file in array
     * 
     * @param files
     *            Array of file objects which contain the essential information
     *            of files
     * @return the file names for each file in the array
     */
    private String[] getPaths(final File[] files) {
        if (files.length > 0) {
            this.path = files[0].getParentFile().getAbsolutePath();
        }
        final ArrayList<String> tmpPaths = new ArrayList<String>(files.length);
        for (final File f : files) {
            if (!f.isDirectory()) {
                tmpPaths.add(f.getName());
            }
        }
        return tmpPaths.toArray(new String[tmpPaths.size()]);
    }

    /**
     * This method is used to change gray scale of the image.
     * 
     * @param grayParameters
     *            red value, green value and blue value.
     */
    public void grayApply(final Object[] grayParameters) {
        this.activeImage().gray((double) grayParameters[0],
                (double) grayParameters[1], (double) grayParameters[2]);
        this.activeImage().clearAutoContrastOperationHistory();
        this.activeImage().updateBinaryImage();
        this.showCurrentDocumentImage();
        this.notifyChange();
    }

    /**
     * Gets the current transcript
     * 
     * @return the transcript
     */
    public Transcript getTranscript() {
        return this.transcript;
    }

    public void getCroppedImage(final Rectangle bounds) {
        this.firePropertyChange(DocumentPanelController.SET_CROPPED_IMAGE,
                null, this.activeImage().getCroppedImage(bounds));
    }

    /**
     * This method is used to preview the gray scale of the image.
     * 
     * @param grayParameters
     *            red value, green value and blue value.
     */
    public void grayPreview(final Object[] grayParameters) {
        this.activeImage().gray((double) grayParameters[0],
                (double) grayParameters[1], (double) grayParameters[2]);
        this.firePropertyChange(DocumentPanelController.RELOAD_IMAGE, null,
                this.activeImage().getGrayImageAsFXImage());
    }

    /**
     * This method is used to save the passing image file paths and open the
     * image first file. It supports multiple files at the same time. But only
     * one file is activated each time. It also gives the feedbacks to view via
     * controller.
     * 
     * @param files
     *            It is the array of file objects which hold the essential
     *            information of files
     */
    public void initDigital(final File[] files) {
        if (files == null) {
            return;
        }
        this.documentImages = null;
        this.activeImage = 0;
        this.path = null;
        this.saveFilename = null;
        String[] paths;
        if (files.length == 1) { // files is a file or a directory
            final File file = files[0];
            if (file.isDirectory()) {
                // accept only image file within the directory
                paths = this.getPaths(file.listFiles(new ImageFileFilter()));
            } else {
                // ImageFileFilter is not needed here, because single images are
                // filtered by the FileChooser
                this.path = file.getParentFile().getAbsolutePath();
                paths = new String[] { file.getName() };
            }
        } else { // multiple files selected
            paths = this.getPaths(files);
        }
        if (paths.length > 0) {
            DiptychonLogger.info("Adding file(s):");
            this.documentImages = new DocumentImage[paths.length];
            int i = 0;
            for (final String p : paths) {
                DiptychonLogger.info(files[i].getAbsolutePath());
                this.documentImages[i] = new DocumentImage(p);
                ++i;
            }
            this.path += File.separator;
            this.transcript.initTranscript(this.documentImages.length);
            this.openImage(0);
            this.firePropertyChange(DocumentPanelController.INIT_DIGITAL, null,
                    this.documentImages.length);
        }
        this.notifyChange();
    }

    /**
     * Loads a digital and the corresponding data from hdd
     */
    public void loadFromHDDDigital() {
        System.out.println("Digital: loadFromHDDDigital(): this.path ="
                + this.path);
        System.out.println("saveFilename = " + saveFilename);

        final javafx.scene.image.Image documentImage = this.activeImage().load(
                this.path, this.showBinarizedImage);
        this.firePropertyChange(DocumentPanelController.OPEN_IMAGE, null,
                documentImage);
        this.firePropertyChange(DocumentPanelController.SHOW_IMAGE_NAME, null,
                new Object[] { this.path + this.activeImage().getName(),
                        this.saveFilename });
        this.firePropertyChange(DocumentPanelController.LOAD_FROM_HDD_DIGITAL,
                null, new int[] { this.documentImages.length,
                        this.activeImage + 1 });
        this.showElements();
        this.transcript.setDigital(this);
    }

    /**
     * This method is used to mask the image
     * 
     * @param mask
     *            mask is the region which is marked.
     */
    public void maskImage(final Rectangle mask) {
        final ArrayList<String> removedLineIDs = this.activeImage().maskImage(
                mask);
        for (final String s : removedLineIDs) {
            this.transcript.removePageTextline(s);
        }
        this.transcript.addLines(this.activeImage().getLines());
        this.showCurrentDocumentImage();
        this.notifyChange();
    }

    /**
     * Überarbeitet: jworch, Björn Gottfried 2. Juni 2015 Performs template
     * matching
     * 
     * @param lineID
     *            of the line in which glyphs are searched for
     */
    public void matchAllTemplatesSingleLine(final String lineID) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Digital.this.acceptGlyphs(activeImage()
                        .matchAllTemplatesSingleLine(lineID,
                                Digital.this.getTranscript()));
                System.out
                        .println("********** EXIT automatic Transcription **********");
            }
        }).start();
    }

    /**
     * Convenience method to load data from filename from hdd
     * 
     * @param filename
     *            the filename
     */
    private void openFileHelper(final String filename) {
        System.out.println("Digital 0: openFileHelper: filename " + filename);
        System.out.println("saveFilename = " + saveFilename);
        File dsfFile = new File(filename);
        String dsfFilePath = dsfFile.getParentFile().getAbsolutePath();
        System.out.println("Path alt = " + this.path);
        this.path = dsfFilePath;
        this.path += File.separator;
        System.out.println("Path new = " + this.path);

        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new BufferedInputStream(
                    new GZIPInputStream(new FileInputStream(filename))));
            final Digital digital = (Digital) ois.readObject();
            digital.path = this.path;
            digital.saveFilename = dsfFile.getName();
            this.firePropertyChange(DocumentPanelController.OPEN_PROJECT, null,
                    digital);
            ois.close();
        } catch (final IOException | ClassNotFoundException e) {
            DiptychonLogger.error("{}", e);
        }
    }

    /**
     * Updates the position and size of a line
     * 
     * @param rectangle
     *            the new position and size
     */
    public void updateTextlineSize(final Rectangle rectangle) {
        if (this.activeImage().updateTextlineSize(rectangle)) {
            final ArrayList<Glyph> glyphs = new ArrayList<>(this.activeImage()
                    .getGlyphs(rectangle.getId()));
            Collections.sort(glyphs);
            this.transcript.updateTranscription(rectangle.getId(), glyphs);
        }
        this.transcript.updatePageTextlineSize(rectangle);
        this.showElements();
        this.notifyChange();
    }

    /**
     * This method is used to load a new page according to the imageNumber
     * index.
     * 
     * @param imageNumber
     *            It stands for the index in the documentImages array.
     */
    public void openImage(final Integer imageNumber) {
        this.activeImage = imageNumber;
        this.transcript.setActivePage(this.activeImage);
        final javafx.scene.image.Image documentImage = this.activeImage().load(
                this.path, this.showBinarizedImage);
        this.firePropertyChange(DocumentPanelController.OPEN_IMAGE, null,
                documentImage);
        this.firePropertyChange(DocumentPanelController.SHOW_IMAGE_NAME, null,
                new Object[] { this.path + this.activeImage().getName(),
                        this.saveFilename });
        this.showElements();
    }

    /**
     * Opens the project with filename
     * 
     * @param filename
     *            the filename
     */
    public void openProject(final String filename) {
        // TODO Sicherungsmassnahme: Bild nicht vorhanden, manuell suchen
        this.firePropertyChange(DocumentPanelController.UPDATE_RECENTLY_USED,
                null, filename);
        this.openFileHelper(filename);
        System.out.println("Digital: openProject: filename = " + filename);
    }

    /**
     * Opens a recently used project and upates the list of recently used
     * projects
     * 
     * @param filename
     *            the filename
     */
    public void openRecentlyUsed(final String filename) {
        System.out.println("Digital: openRecentlyUsed: filename=" + filename);
        this.openFileHelper(filename);
    }

    /**
     * Previews an autocontrast operation
     * 
     * @param autoContrastParameters
     *            the parameters for an autocontrastoperation
     */
    public void previewAutoContrast(final Object[] autoContrastParameters) {
        int[] histogramm = (int[]) autoContrastParameters[6];
        final Rectangle imageRectangle = (Rectangle) autoContrastParameters[0];

        final AutoContrastOperation aco = new AutoContrastOperation(
                imageRectangle, (double) autoContrastParameters[1],
                (double) autoContrastParameters[2],
                (int) autoContrastParameters[3],
                (int) autoContrastParameters[4], histogramm);

        this.activeImage().autoContrastImage(aco);

        this.firePropertyChange(DocumentPanelController.RELOAD_IMAGE, null,
                this.activeImage().getGrayImageAsFXImage());

        histogramm = this.activeImage().getHistogramInRectangle(imageRectangle,
                histogramm);
        final IntegerProperty simple = (IntegerProperty) autoContrastParameters[5];
        simple.set(simple.get() + 1);
    }

    /**
     * Removes the line with id id from the current page/image
     * 
     * @param id
     *            the id of the line
     */
    public void removeTextline(final String id) {
        this.activeImage().removeTextline(id);
        this.transcript.removePageTextline(id);
        this.showElements();
        this.notifyChange();
    }

    /**
     * Saves the current project if a name has been specified
     */
    public void save() {
        if (this.saveFilename == null
                || !(new File(this.saveFilename).exists())) {
            System.out.println("Datei 1: " + this.saveFilename);
            this.firePropertyChange(DocumentPanelController.UNKNOWN_SAVE_FILE,
                    null, null);
        } else {
            DiptychonLogger.info("... as {}", this.saveFilename);
            // durch dokumentbilder iterieren, nach this.saveFilename kopieren
            // this.path = this.saveFilename ohne dateiname
            // name in documentimage wirklich name oder path?
            System.out.println("Datei 2: " + this.saveFilename);
            this.save(this.saveFilename);
        }
    }

    public void zoomFit() {
        this.firePropertyChange(DocumentPanelController.ZOOM_FIT, null, null);
    }

    public void cancelTimer() {
        this.firePropertyChange(DocumentPanelController.CANCEL_TIMER, null,
                null);
    }

    /**
     * Exports the binary images of all glyphs
     */
    public void exportGlyphsBinary() throws IOException {
        exportGlyphsBinary(null);
    }

    /**
     * Exports the binary images of all glyphs that matches the given string.
     */
    public void exportGlyphsBinary(String specificGlyph) throws IOException {
        this.firePropertyChange(DocumentPanelController.UPDATE_GLYPH_ANNO,
                null, new Object[] { this.activeImage().getAllGlyphs() });

        String filename = this.path + File.separator + "Glyphs"
                + File.separator + "Binary" + File.separator;
        String filename2 = this.path + File.separator + "Glyphs"
                + File.separator + "Binary" + File.separator
                + "Glyph Properties.txt";
        if (this.saveFilename != null) {
            System.out.println(this.saveFilename);
            filename = this.saveFilename.substring(0,
                    this.saveFilename.length() - 4)
                    + " Glyphs" + File.separator + "Binary" + File.separator;
            filename2 = this.saveFilename.substring(0,
                    this.saveFilename.length() - 4)
                    + " Glyphs"
                    + File.separator
                    + "Binary"
                    + File.separator
                    + "Glyph Properties.txt";
        }

        File dir = new File(filename);
        File dir2 = new File(dir.getParent());
        dir2.mkdir();
        dir.mkdir();
        PrintWriter out = new PrintWriter(filename2);
        System.out.println("Save binary glyphs: "
                + new File(filename).getAbsolutePath());

        out.print("GlyphID Character PositionX PositionY Width Height Area NumOfComponents Perimeter NumOfHoles Circularity Extent Extremum Curvature Betweenness");
        for (int p = 0; p <= Moments.getP(); p++) {
            for (int q = 0; q <= Moments.getQ(); q++) {
                out.print(" Moment(p=" + p + ",q=" + q + ")");
            }
        }
        out.println();
        ArrayList<Glyph> glyphs = this.activeImage().getAllGlyphs();

        Collections.sort(glyphs, new Comparator<Glyph>() {
            @Override
            public int compare(final Glyph g1, final Glyph g2) {
                return g1.getGroupID().compareTo(g2.getGroupID());
            }
        });

        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(
                filename + "Glyphs.zip"));
        for (Glyph g : glyphs) {
            if (!g.isSpace) {
                if (specificGlyph == null || specificGlyph.equals(g.getGroupID())) {
                    zout.putNextEntry(new ZipEntry(g.getID() + ".png"));
                    ImageUtils.writeGrayscaleImage(zout, g.getBinarizedImage());
                    zout.closeEntry();
                    final PolygonComparison features = new PolygonComparison(
                            g.getBinarizedImagePixels(), g.getWidth(),
                            g.getHeight());
                    final Moments moments = new Moments(
                            g.getBinarizedImagePixels(), g.getWidth(),
                            g.getHeight());
                    double[] moms = moments.getMoments();
    
                    // float divisor =
                    // ((float)features.getPerimeter()*(float)features.getPerimeter());
                    // if (divisor == 0) { divisor = (float) 0.01; }
    
                    // Oft inkorrekte Berechnung (warum?) des Flächeninhaltes über
                    // das von PolygonComparison gelieferte größte äußere Polygon:
                    // final float area = features.getArea();
    
                    final float area = g.getSize();
                    final float perimeter = features.getPerimeter();
                    final float circularity = (float) ((4 * Math.PI * area) / Math
                            .pow(perimeter, 2));
                    out.print(g.getID() + " " + g.getGroupID() + " "
                            + g.getLayoutX() + " " + g.getLayoutY() + " "
                            + g.getWidth() + " " + g.getHeight() + " "
                            + g.getSize() + " " + features.getNumOfPolygons() + " "
                            + features.getPerimeter() + " "
                            + features.getNumOfInnerPolygons() + " " + circularity
                            + " " + features.getExtent() + " "
                            + features.getExtremum() + " "
                            + features.getCurvature() + " "
                            + features.getBetweenness());
                    for (int i = 0; i < moms.length; i++) {
                        out.print(" " + moms[i]);
                    }
                    out.println();
                }
            }
        }
        out.close();
        zout.close();
    }

    /**
     * Exports the gray images of all glyphs
     */
    public void exportGlyphsGrayscale() throws IOException {
        exportGlyphsGrayscale(null);
    }

    /**
     * Exports the gray images of all glyphs that matches the given string.
     */
    public void exportGlyphsGrayscale(String specificGlyph) throws IOException {
        this.firePropertyChange(DocumentPanelController.UPDATE_GLYPH_ANNO,
                null, new Object[] { this.activeImage().getAllGlyphs() });

        String filename = this.path + File.separator + "Glyphs"
                + File.separator + "Greyscale" + File.separator;
        if (this.saveFilename != null) {
            System.out.println(this.saveFilename);
            filename = this.saveFilename.substring(0,
                    this.saveFilename.length() - 4)
                    + " Glyphs" + File.separator + "Greyscale" + File.separator;
        }

        File dir = new File(filename);
        File dir2 = new File(dir.getParent());
        dir2.mkdir();
        dir.mkdir();
        System.out.println(filename);

        ArrayList<Glyph> glyphs = this.activeImage().getAllGlyphs();

        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(
                filename + "Glyphs.zip"));
        for (Glyph g : glyphs) {
            if (!g.isSpace) {
                if (specificGlyph == null || specificGlyph.equals(g.getGroupID())) {
                    zout.putNextEntry(new ZipEntry(g.getID() + ".png"));
                    ImageUtils.writeGrayscaleImage(zout, g.getGrayImage());
                    zout.closeEntry();
                }
            }
        }
        zout.close();
    }

    /**
     * Exports the document statistics
     */
    public void exportStatistic() throws IOException {
        this.firePropertyChange(DocumentPanelController.UPDATE_GLYPH_ANNO,
                null, new Object[] { this.activeImage().getAllGlyphs() });
        try {
            String filename = this.path + "Document Statistics";
            if (this.saveFilename != null) {
                System.out.println("save-file-name: " + this.saveFilename);
                int fileExtension = 4;
                filename = this.path
                        + this.saveFilename.substring(0,
                                this.saveFilename.length() - fileExtension)
                        + " Statistics";
                System.out.println("savefilename: " + filename);
            }

            PrintWriter out = new PrintWriter(filename + ".txt");

            this.activeImage().updateDocumentStats();
            this.activeImage().updateGlyphStats();
            this.activeImage().updateLineStats();
            this.activeImage().updateWordStats();

            ArrayList<Float> docStats = this.activeImage().getDocumentStats();
            ArrayList<Float> lineStats = this.activeImage().getLineStats();
            ArrayList<Float> wordStats = this.activeImage().getWordStats();
            HashMap<String, ArrayList<Float>> glyphStats = this.activeImage()
                    .getGlyphStats();

            out.println("Total textlines: " + (int) (float) docStats.get(0));
            out.println("Mean space between textlines: " + docStats.get(2));
            out.println("Mean textline width" + ": " + lineStats.get(0));
            out.println("Mean textline height" + ": " + lineStats.get(1));
            out.println("Mean space between textline and document border left"
                    + ": " + lineStats.get(2));
            out.println("Mean space between textline and document border right"
                    + ": " + lineStats.get(3));
            out.println("Mean amount of characters in textline" + ": "
                    + lineStats.get(4));
            out.println("Mean textline slant" + ": " + lineStats.get(5));
            if (lineStats.get(6) > 0) {
                out.println("Dominating slant direction: Ascending right | "
                        + (int) (float) lineStats.get(6));
            } else if (lineStats.get(6) < 0) {
                out.println("Dominating slant direction: Falling right | "
                        + (int) (float) lineStats.get(6));
            } else {
                out.println("Dominating slant direction: Balanced | "
                        + (int) (float) lineStats.get(6));
            }

            out.println();

            out.println("Total words: " + (int) (float) wordStats.get(2));
            out.println("Mean space between words: " + wordStats.get(0));
            out.println("Mean amount of characters in word: "
                    + wordStats.get(1));

            out.println();

            float abbcount = docStats.get(5) + docStats.get(6)
                    + docStats.get(7) + docStats.get(8);
            out.println("Total abbreviations: " + (int) abbcount);
            out.println("Isolated abbreviations: "
                    + (int) (float) docStats.get(5));
            out.println("Words ending with abbreviation: "
                    + (int) (float) docStats.get(6));
            out.println("Words starting with abbreviation: "
                    + (int) (float) docStats.get(7));
            out.println("Words containing abbreviation: "
                    + (int) (float) docStats.get(8));

            out.println();

            out.println("Total glyphs: " + (int) (float) docStats.get(1));
            out.println("Character | Width | Height | Area | Frequency");
            for (final String GID : glyphStats.keySet()) {
                out.println(GID + " | "
                        + (int) (float) glyphStats.get(GID).get(0) + " | "
                        + (int) (float) glyphStats.get(GID).get(1) + " | "
                        + (int) (float) glyphStats.get(GID).get(2) + " | "
                        + (int) (float) glyphStats.get(GID).get(3));
            }

            out.close();
        } catch (FileNotFoundException e) {
            DiptychonLogger.error("end of statistics error: ", e);
        }
    }

    /**
     * Saves the current project to filename
     * 
     * @param filename
     *            the filename
     */
    public void save(final String filename) {
        System.out.println();
        System.out.println("Digital: save(final String filename): " + filename);
        System.out.println("DocumentImage Filename: "
                + this.documentImages[0].getName());
        System.out.println("DocumentImage Path: " + path);
        System.out.println();

        File dsfFile = new File(filename);
        System.out.println("dsfFile: " + dsfFile);
        String dsfFilePath = dsfFile.getParentFile().getAbsolutePath();
        System.out.println("dsfFilePath: " + dsfFilePath);

        for (int i = 0; i < this.documentImages.length; i++) {
            String imageFileName = this.documentImages[i].getName();
            File dsfFilePathImageName = new File(dsfFilePath,
                    this.documentImages[i].getName());
            System.out.println("neu: " + dsfFilePathImageName.getPath());
            System.out.println();
            Path source = Paths.get(path + imageFileName);
            Path destination = Paths.get(dsfFilePathImageName.getPath());

            try {
                StandardCopyOption[] opts = {
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES };
                Files.copy(source, destination, opts);
                System.out.println("Files.copy ok");
            } catch (IOException e1) {
                e1.printStackTrace();
                System.out.println("Files.copy failed");
            }
        }

        this.saveFilename = filename;
        if (!path.endsWith(File.separator)) {
            this.path += File.separator;
        }
        System.out.println("Neuer Bildpfad: " + path);

        ObjectOutputStream oos = null;
        try {
            DiptychonLogger.debug("Creating file outputstream...");
            oos = new ObjectOutputStream(new BufferedOutputStream(
                    new GZIPOutputStream(new FileOutputStream(filename))));
            oos.writeObject(this);
            oos.flush();
            oos.close();
            DiptychonLogger.debug("Outputstream closed.");
        } catch (final IOException e) {
            DiptychonLogger.error("{}", e);
        }
        this.firePropertyChange(DocumentPanelController.UPDATE_RECENTLY_USED,
                null, this.saveFilename);
    }

    /**
     * This method is used to show the current document image.
     */
    private void showCurrentDocumentImage() {
        final javafx.scene.image.Image documentImage = this.activeImage().load(
                this.path, this.showBinarizedImage);
        this.firePropertyChange(DocumentPanelController.RELOAD_IMAGE, null,
                documentImage);
        this.showElements();
    }

    /**
     * Notifies the views about a change in the model, which enables the save
     * button
     */
    private void notifyChange() {
        this.firePropertyChange(DocumentPanelController.STATUS_CHANGED, null,
                null);
    }

    /**
     * Marks a line as handwritten annotation if it was not, unset otherwise
     * 
     * @param lineID
     *            the id of the line
     */
    public void setHandwrittenAnnotation(final String lineID) {
        this.activeImage().setHandwrittenAnnotation(lineID);
        this.transcript.setHandwrittenAnnotationTranscript(lineID);
        this.notifyChange();
    }

    /**
     * Removes all glyphs from the specified line
     * 
     * @param lineID
     *            the id of the line
     */
    public void removeGlyphsFromLine(final String lineID) {
        this.activeImage().removeGlyphsFromLine(lineID);
        this.showElements();
        this.notifyChange();
    }

    /**
     * Updates the glyph ids when the transcription of a line was changed
     * 
     * @param decoded
     *            the transcription
     * @param encoding
     *            the encoded transcription
     * @param lineID
     *            the id of the line
     */
    public void updateGlyphIDs(final String[] decoded, final String[] encoding,
            final String lineID) {
        this.activeImage().updateGlyphIDs(decoded, encoding, lineID);
        this.notifyChange();
    }

    public void updateGlyphStats() {
        this.activeImage().updateGlyphStats();
    }

    public void updateDocumentStats() {
        this.activeImage().updateDocumentStats();
    }

    public void updateLineStats() {
        this.activeImage().updateLineStats();
    }

    public void updateWordStats() {
        this.activeImage().updateWordStats();
    }

    /**
     * sets the default status
     */
    public void setDefaultToggleStatus() {
        this.showBinarizedImage = false;
    }
}
