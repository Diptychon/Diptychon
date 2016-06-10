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
package de.diptychon.models.representation;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javafx.application.Platform;
import javafx.scene.shape.Rectangle;
import de.diptychon.DiptychonLogger;
import de.diptychon.controller.DocumentPanelController;
import de.diptychon.models.AbstractModel;
import de.diptychon.models.data.Digital;
import de.diptychon.models.data.Glyph;
import de.diptychon.models.data.ImageLine;
import de.diptychon.models.misc.TranscriptExporter;

/**
 * The model representing the transcription of a digital
 */
public class Transcript extends AbstractModel implements Serializable {

    /**
     * The prefix for page IDs
     */
    private static final String PAGE_ID_PREFIX = "P_";

    /**
     * SerialVersionUID to keep used data consistent.
     */
    private static final long serialVersionUID = 202121129;

    /**
     * All DocumentPages which the transcript consists of
     */
    private DocumentPage[] documentPages;

    /**
     * The alphabet of this transcript
     */
    private Alphabet alphabet;

    /**
     * the current page which is shown to the user
     */
    private int activePage;

    /**
     * Reference to the digital
     */
    private transient Digital digital;

    /**
     * Default Constructor.
     * 
     * @param pDigital
     *            reference to the Digital
     */
    public Transcript(final Digital pDigital) {
        super();
        this.digital = pDigital;
    }

    /**
     * Initializes a transcript with the given number of pages
     * 
     * @param numberOfPages
     *            the number of pages the transcript should consist of
     */
    public void initTranscript(final int numberOfPages) {
        this.documentPages = new DocumentPage[numberOfPages];
        for (int i = 0; i < numberOfPages; ++i) {
            this.documentPages[i] = new DocumentPage();
        }
        this.alphabet = new Alphabet();
        this.activePage = 0;
    }

    /**
     * Gets the number of the current page
     * 
     * @return the current page
     */
    private DocumentPage activePage() {
        return this.documentPages[this.activePage];
    }

    /**
     * Adds a new line to the current page
     * 
     * @param lineID
     *            the id of the line
     * @param rectangle
     *            the position and size of the line to be added
     */
    public void addLine(final String lineID, final Rectangle rectangle) {
        rectangle.setId(lineID);
        this.activePage().addLine(lineID, rectangle);
        this.firePropertyChange(DocumentPanelController.ADD_TEXTLINE, null,
                rectangle);
    }

    /**
     * Transcribes a specific line on the current page
     * 
     * @param lineIDTranscriptionSplit
     *            the id of the line which should be transcribed and the
     *            transcription
     */
    public void transcribe(final Object[] lineIDTranscriptionSplit) {
        boolean forceSplit = (boolean) lineIDTranscriptionSplit[2];
        final String lineID = (String) lineIDTranscriptionSplit[1];
        boolean splitLine = true;

        if (this.activePage().lineExists(lineID)) {
            final ArrayList<String> transcription = this.alphabet
                    .partitionTextline((String) lineIDTranscriptionSplit[0]);

            final String[] formerEncoding = this.activePage().getLineEncoding(
                    lineID);
            String[] formerDecoding = new String[] { "" };
            if (transcription.size() == formerEncoding.length && !forceSplit) {
                splitLine = false;
                formerDecoding = this.alphabet.decodeTranscription(
                        formerEncoding, '$', '$');
            }

            this.activePage().clearTranscription(lineID);
            this.alphabet.removeEncoding(formerEncoding);

            final String[] encoding = this.alphabet.encodeTranscription(
                    transcription, PAGE_ID_PREFIX + this.activePage, lineID);
            this.activePage().transcribe(encoding, lineID);
            // this.alphabet.printAlphabetAndCount();
            final String[] decoded = this.alphabet.decodeTranscription(
                    encoding, '$', '$');

            if (!splitLine) {
                int index = 0;
                if (formerDecoding[index] != null) {
                    for (final String s : formerDecoding) {
                        if (s.equals(" ") && !decoded[index].equals(" ")) {
                            splitLine = true;
                            break;
                        }
                        ++index;
                    }
                } else {
                    splitLine = true;
                }
            }

            this.firePropertyChange(DocumentPanelController.SET_TRANSCRIPTION,
                    null, new Object[] { decoded, lineID });

            if (splitLine) {
                this.digital.splitImageLine(decoded, encoding, lineID);
            } else {
                this.digital.updateGlyphIDs(decoded, encoding, lineID);
            }
        }
    }

    /**
     * After loading from HDD this methods gets all lines and the transcription
     */
    public void loadFromHDDTranscript() {
        final Collection<PageLine> lines = this.activePage().getLines();
        this.firePropertyChange(DocumentPanelController.UPDATE_DOCUMENT_PAGE,
                null, lines);
        this.getPageTranscription();
    }

    /**
     * Sets the current page
     * 
     * @param activeImage
     *            the current page
     */
    public void setActivePage(final int activeImage) {
        this.activePage = activeImage;
        this.firePropertyChange(DocumentPanelController.UPDATE_DOCUMENT_PAGE,
                null, this.activePage().getLines());
        this.getPageTranscription();
    }

    /**
     * Adds a collection of lines to the current page
     * 
     * @param imageLines
     *            the collection of lines
     */
    public void addLines(final Collection<ImageLine> imageLines) {
        this.activePage().addLines(imageLines);
        this.firePropertyChange(DocumentPanelController.SHOW_PAGE_LINES, null,
                this.activePage().getLines());
    }

    /**
     * Removes all lines from the current page
     */
    public void removeLines() {
        this.activePage().removeLines();
        this.firePropertyChange(DocumentPanelController.SHOW_PAGE_LINES, null,
                this.activePage().getLines());
    }

    /**
     * Sets the reference to the digital
     * 
     * @param pDigital
     *            the reference to the digital
     */
    public void setDigital(final Digital pDigital) {
        this.digital = pDigital;
    }

    /**
     * Marks a line as handwritten annotation
     * 
     * @param lineID
     *            the id of the line which will be marked as handwritten
     *            annotation
     */
    public void setHandwrittenAnnotationTranscript(final String lineID) {
        this.activePage().setHandwrittenAnnotation(lineID);
    }

    /**
     * Removes a line and its transcription from the current page
     * 
     * @param id
     *            the id of the line to be removed
     */
    public void removePageTextline(final String id) {
        final String[] encoding = this.activePage().removeTextline(id);
        this.alphabet.removeEncoding(encoding);
        this.firePropertyChange(DocumentPanelController.SHOW_PAGE_LINES, null,
                this.activePage().getLines());
        this.getPageTranscription();
    }

    /**
     * Gets the transcription of the current page
     */
    private void getPageTranscription() {
        final Collection<PageLine> lines = this.activePage().getLines();
        for (final PageLine line : lines) {
            final String[] encoding = line.getEncoding();
            if (encoding != null
                    && (encoding.length >= 1 && !encoding[0].isEmpty())) {
                final String[] decodedLine = this.alphabet.decodeTranscription(
                        encoding, '$', '$');
                final String lineID = line.getID();
                this.firePropertyChange(
                        DocumentPanelController.SET_TRANSCRIPTION, null,
                        new Object[] { decodedLine, lineID });
            }
        }
    }

    /**
     * Updates the size of a line
     * 
     * @param rectangle
     *            the new size of a line (also consists of the line id)
     */
    public void updatePageTextlineSize(final Rectangle rectangle) {
        this.activePage().updateTextlineSize(rectangle);
        this.firePropertyChange(DocumentPanelController.SHOW_PAGE_LINES, null,
                this.activePage().getLines());
        this.getPageTranscription();
    }

    /**
     * Exports the transcript as rtf
     * 
     * @param fileFontname
     *            the file and fontname
     */
    public void exportTranscriptAsRTF(final Object[] fileFontname) {
        final String filename = (String) fileFontname[0];
        final String fontname = (String) fileFontname[1];
        final TranscriptExporter te = new TranscriptExporter();
        try {
            te.exportAsRTF(filename, this, fontname);
        } catch (final IOException e) {
            DiptychonLogger.error("{}", e);
        }
    }

    public void exportTranscriptAsTEI(final Object[] fileSource) {
        final TranscriptExporter te = new TranscriptExporter();
        try {
            te.exportAsTEI((String) fileSource[0], this, (String) fileSource[1]);
        } catch (final IOException e) {
            DiptychonLogger.error("{}", e);
        }
    }

    /**
     * Exports the transcript as plaintext
     * 
     * @param filename
     *            the filename
     */
    public void exportTranscriptAsPlainText(final String filename) {
        final TranscriptExporter te = new TranscriptExporter();
        try {
            te.exportAsPlainText(filename, this);
        } catch (final IOException e) {
            DiptychonLogger.error("{}", e);
        }
    }

    /**
     * Gets the transcription (decoded and encoded) of a specific line of the
     * current page
     * 
     * @param lineID
     *            the id of the line
     * @return the decoded and encoded transcription
     */
    public String[][] getLineTranscription(final String lineID) {
        final String[] encoding = this.activePage().getLineEncoding(lineID);
        if (encoding != null && (encoding.length >= 1 && encoding[0] != null)) {
            final String[] decodedLine = this.alphabet.decodeTranscription(
                    encoding, '$', '$');
            return new String[][] { decodedLine, encoding };
        }
        return new String[][] { {} };
    }

    /**
     * Gets all document pages
     * 
     * @return all document pages
     */
    public DocumentPage[] getDocumentPages() {
        return this.documentPages;
    }

    /**
     * Gets the alphabet of this transcription
     * 
     * @return the alphabet
     */
    public Alphabet getAlphabet() {
        return this.alphabet;
    }

    /**
     * Removes a the transcription of a specific line from the alphabet
     * 
     * @param lineID
     *            the id of the line
     */
    public void removeLineEncodingFromAlphabet(final String lineID) {
        final String[] encoding = this.activePage().getLineEncoding(lineID);
        this.activePage().clearTranscription(lineID);
        this.alphabet.removeEncoding(encoding);
        this.digital.removeGlyphsFromLine(lineID);
        this.firePropertyChange(DocumentPanelController.SET_TRANSCRIPTION,
                null, new Object[] { new String[] { "" }, lineID });
    }

    /**
     * Searches for a String within the transcription of the current page
     * 
     * @param toSearch
     *            the string to search for
     * @param caseSensitive
     *            <code>true</code> if the search must be case sensitive,
     *            <code>false</code> otherwise
     * @return the result of the search
     */
    public ArrayList<String[]> searchForWord(String toSearch,
            final boolean caseSensitive) {
        final ArrayList<String[]> results = new ArrayList<>();
        final Collection<PageLine> activePageLines = this.activePage()
                .getLines();
        if (!caseSensitive) {
            toSearch = toSearch.toLowerCase();
        }
        for (final PageLine pl : activePageLines) {
            final String[] encoding = pl.getEncoding();
            final String[] decoded = this.alphabet.decodeTranscription(
                    encoding, '\0', '\0');

            for (int i = 0; i < decoded.length; ++i) {
                String compare = decoded[i];
                if (compare == null) {
                    continue;
                }
                if (!caseSensitive) {
                    compare = compare.toLowerCase();
                }
                if (compare.length() > 1 && !compare.contains(toSearch)) {
                    boolean contains = false;
                    for (int j = 0; j < toSearch.length(); ++j) {
                        final String tmp = toSearch.substring(0,
                                toSearch.length() - j);
                        if (compare.contains(tmp)) {
                            final int lastIndex = compare.lastIndexOf(tmp);
                            compare = compare.substring(lastIndex, lastIndex
                                    + tmp.length());
                            contains = true;
                            break;
                        }
                    }
                    if (!contains) {
                        for (int j = toSearch.length() - 2; j > 0; --j) {
                            final String tmp = toSearch.substring(
                                    toSearch.length() - j,
                                    toSearch.length() - 1);
                            if (compare.contains(tmp)) {
                                final int lastIndex = compare.lastIndexOf(tmp);
                                compare = compare.substring(lastIndex,
                                        compare.length());
                                contains = true;
                                break;
                            }
                        }
                    }
                } else {
                    if (compare.contains(toSearch)) {
                        final int lastIndex = compare.lastIndexOf(toSearch);
                        compare = compare.substring(lastIndex);
                    }
                }
                if (toSearch.startsWith(compare)
                        || compare.startsWith(toSearch)) {
                    final int startAt = i;
                    final StringBuffer tmp;
                    int elementCounter = compare.equals(" ") ? 0 : 1;
                    int spaceCounter = elementCounter != 0 ? 0 : 1;

                    if (compare.length() < toSearch.length()) {
                        tmp = new StringBuffer(compare);
                        while (tmp.length() < toSearch.length()
                                && i < decoded.length - 1) {
                            ++i;
                            compare = decoded[i];
                            if (!caseSensitive) {
                                compare = compare.toLowerCase();
                            }

                            final String toAdd;
                            if (tmp.length() + compare.length() <= toSearch
                                    .length()) {
                                toAdd = compare;
                                tmp.append(toAdd);
                            } else {
                                toAdd = compare.substring(0, toSearch.length()
                                        - tmp.length());
                                tmp.append(toAdd);
                            }
                            if (toAdd.equals(" ")) {
                                ++spaceCounter;
                            } else {
                                ++elementCounter;
                            }
                            if (!toSearch.startsWith(tmp.toString())) {
                                break;
                            }
                        }
                    } else {
                        tmp = new StringBuffer(compare.substring(0,
                                toSearch.length()));
                    }

                    if (toSearch.equals(tmp.toString())) {
                        final String[] res = new String[elementCounter];
                        int replacedSpaces = 0;
                        for (int j = startAt; j < startAt + elementCounter
                                + spaceCounter; ++j) {
                            if (!decoded[j].equals(" ")) {
                                res[j - startAt - replacedSpaces] = encoding[j];
                            } else {
                                ++replacedSpaces;
                            }
                        }
                        results.add(res);
                    } else {
                        i = startAt;
                    }
                }
            }
        }
        return results;
    }

    /**
     * Updates the transcription of a specific line and their corresponding
     * glyphs (actually only their ids
     * 
     * @param lineID
     *            the id of the line
     * @param glyphs
     *            the glyphs which need an update
     * @return the updated glyphs
     */
    public ArrayList<Glyph> updateTranscription(final String lineID,
            final ArrayList<Glyph> glyphs) {
        final PageLine pl = this.activePage().getLine(lineID);
        this.alphabet.removeEncoding(pl.getEncoding());
        pl.clearTranscription();
        int i = 1;
        for (final Glyph g : glyphs) {
            // System.out.println("************** Glyph g = "+g.getGroupID()+" getLayoutX() = "+g.getLayoutX());
            g.setID(PAGE_ID_PREFIX + this.activePage + "_" + lineID + "_"
                    + Alphabet.COLUMN_PREFIX + i);
            this.alphabet.addElement(g.getGroupID(), g.getID());
            pl.addCharacterToEncoding(g.getID());
            ++i;
        }
        final String[] decoded = this.alphabet.decodeTranscription(
                pl.getEncoding(), '$', '$');

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Transcript.this.firePropertyChange(
                        DocumentPanelController.SET_TRANSCRIPTION, null,
                        new Object[] { decoded, lineID });
            }
        });
        return glyphs;
    }
}
