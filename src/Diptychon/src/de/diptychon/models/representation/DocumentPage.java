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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import javafx.scene.shape.Rectangle;
import de.diptychon.models.data.ImageLine;

/**
 * This class represents one Page of a transcript and provides the necessary
 * functionality for it.
 */
public class DocumentPage implements Serializable {

    /**
     * SerialVersionUID to keep used data consistent.
     */
    private static final long serialVersionUID = 20121107;

    /**
     * Stores all lines of a Page with String ID as key and Line as value
     */
    private final HashMap<String, PageLine> lines;

    /**
     * Default constructor, initializes the line HashMap
     */
    public DocumentPage() {
        this.lines = new HashMap<>();
    }

    /**
     * Adds a new line with a specific ID to this page
     * 
     * @param id
     *            the id of the line
     * @param rectangle
     *            the Position and size of the line
     */
    public void addLine(final String id, final Rectangle rectangle) {
        final PageLine pageLine = new PageLine(id, rectangle);
        this.lines.put(id, pageLine);
    }

    /**
     * Checks whether a line with ID <code>lineID</code> exists on this page.
     * 
     * @param lineID
     *            the id of the line to check
     * @return <code>true</code> if the line exists, <code>false</code>
     *         otherwise
     */
    public boolean lineExists(final String lineID) {
        return this.lines.get(lineID) != null;
    }

    /**
     * Gets all lines of this page as ArrayList; sorted by y coordinate
     * 
     * @return The sorted list of lines
     */
    public ArrayList<PageLine> getLines() {
        final ArrayList<PageLine> allLines = new ArrayList<>(
                this.lines.values());
        Collections.sort(allLines);
        return allLines;
    }

    /**
     * Removes all lines from this page
     */
    public void removeLines() {
        this.lines.clear();
    }

    /**
     * Transcribes a specific line of this page
     * 
     * @param encoding
     *            the encoding
     * @param lineID
     *            the id of the line
     * @return <code>true</code> if the transcription was successful,
     *         <code>false</code> otherwise
     */
    public boolean transcribe(final String[] encoding, final String lineID) {
        final PageLine pageLine = this.lines.get(lineID);
        if (pageLine == null) {
            return false;
        }
        pageLine.transcribe(encoding);
        return true;
    }

    /**
     * Adds a Collection of lines to this page
     * 
     * @param imageLines
     *            the collection of lines
     */
    public void addLines(final Collection<ImageLine> imageLines) {
        for (final ImageLine il : imageLines) {
            final PageLine pageLine = new PageLine(il.getID(), il.getBounds());
            this.lines.put(il.getID(), pageLine);
        }
    }

    /**
     * Removes a specific line of this page and returns its encoded
     * transcription
     * 
     * @param id
     *            the id of the line
     * @return the encoded transcription
     */
    public String[] removeTextline(final String id) {
        final String[] encoding = this.lines.get(id).getEncoding();
        this.lines.remove(id);
        return encoding;
    }

    /**
     * Updates the position and size of a line. The rectangle must contain the
     * id
     * 
     * @param rectangle
     *            the new position and size
     */
    public void updateTextlineSize(final Rectangle rectangle) {
        final PageLine pg = this.lines.get(rectangle.getId());
        pg.updateSize(rectangle);
    }

    /**
     * Gets the encoded transcription of a line.
     * 
     * @param lineID
     *            the id of the line
     * @return the encoded transcription
     */
    public String[] getLineEncoding(final String lineID) {
        return this.lines.get(lineID).getEncoding();
    }

    /**
     * Sets a line to handwritten annotation (if it was not before, otherwise it
     * is unset)
     * 
     * @param lineID
     *            the id of the line
     */
    public void setHandwrittenAnnotation(final String lineID) {
        this.lines.get(lineID).setHandwrittenAnnotation();
    }

    /**
     * Removes the transcription of a line
     * 
     * @param lineID
     *            the id of the line
     */
    public void clearTranscription(final String lineID) {
        this.lines.get(lineID).clearTranscription();
    }

    /**
     * Gets the line with id <code>lineID</code>
     * 
     * @param lineID
     *            the id of the line
     * @return the line with id <code>lineID</code>
     */
    public PageLine getLine(final String lineID) {
        return this.lines.get(lineID);
    }
}
