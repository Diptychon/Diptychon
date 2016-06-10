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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This class represents the alphabet of a project. Each new Glyph must be added
 * here. This class also encodes and decodes transcribed lines.
 */
public class Alphabet implements Serializable {

    /**
     * Prefix for column encoding.
     */
    public static final String COLUMN_PREFIX = "COL_";

    /**
     * SerialVersionUID to keep used data consistent.
     */
    private static final long serialVersionUID = 20121107;

    /**
     * Used to check whether a user dissolved an abbreviation or not.
     */
    private static final char ABBREVIATION_DELIMITER = '$';

    // /**
    // * Supposed to be used to check whether a user transcribed a ligature or
    // not.
    // */
    // private static final char LIGATURE_DELIMITER = '§';

    /**
     * The internal representation of the alphabet. Each unique character is
     * used as a key, and its position is used as value. i.e. if there are
     * several 'a's there is one entry 'a' and several values which marks the
     * position of each 'a'
     */
    private final HashMap<String, ArrayList<String>> alphabet;

    /**
     * Default constructor.
     */
    public Alphabet() {
        this.alphabet = new HashMap<>();
    }

    /**
     * Returns the character or abbreviation at <code>lineIdColumn</code>.
     * 
     * @param lineIdColumn
     *            The position a character is supposed to be.
     * @param abbreviationOpener
     *            The opener for abbreviation (used only if the character is not
     *            a single character but an abbreviation)
     * @param abbreviationCloser
     *            The closer for abbreviation (used only if the character is not
     *            a single character but an abbreviation)
     * @return The element at position <code>lineIdColumn</code>
     */
    public String getElementAt(final String lineIdColumn,
            final char abbreviationOpener, final char abbreviationCloser) {
        final Set<Entry<String, ArrayList<String>>> entries = this.alphabet
                .entrySet();
        for (final Map.Entry<String, ArrayList<String>> entry : entries) {
            if (entry.getValue().contains(lineIdColumn)) {
                final String key = entry.getKey();
                if (key.length() > 1) {
                    if (abbreviationOpener == ABBREVIATION_DELIMITER
                            && abbreviationCloser == ABBREVIATION_DELIMITER) {
                        return abbreviationOpener + key + abbreviationCloser;
                    } else {
                        final StringBuffer decoded = new StringBuffer(
                                key.subSequence(0, 1));
                        if (abbreviationOpener != '\0') {
                            decoded.append(abbreviationOpener);
                        }
                        decoded.append(key.subSequence(1, key.length()));
                        if (abbreviationCloser != '\0') {
                            decoded.append(abbreviationCloser);
                        }
                        return decoded.toString();
                    }
                } else {
                    return key;
                }
            }
        }
        return "";
    }

    /**
     * Partitions a transcribed line into a list of single elements (single
     * characters and abbreviations)
     * 
     * @param text
     *            The transcribed line.
     * @return the partitioned line as ArrayList
     */
    public ArrayList<String> partitionTextline(final String text) {
        final ArrayList<String> transcription = new ArrayList<>(text.length());
        final char[] charText = text.toCharArray();

        StringBuffer abbreviation = new StringBuffer();
        boolean abbreviationFound = false;
        for (final char c : charText) {
            if (c != ABBREVIATION_DELIMITER) {
                if (!abbreviationFound) {
                    transcription.add(c + "");
                } else {
                    abbreviation.append(c);
                }
            } else {
                if (abbreviationFound) {
                    transcription.add(abbreviation.toString());
                    abbreviationFound = false;
                } else {
                    abbreviation = new StringBuffer();
                    abbreviationFound = true;
                }
            }
        }
        return transcription;
    }

    /**
     * Encodes a transcribed, partitioned line as follows
     * <code>P_X_L_X_COL_X</code>
     * 
     * @param transcription
     *            The transcribed and partioned text line.
     * @param pageID
     *            The ID of the page the line corresponds to.
     * @param lineID
     *            The ID of the text line.
     * @return An array with the encoded line.
     */
    public String[] encodeTranscription(final ArrayList<String> transcription,
            final String pageID, final String lineID) {
        final String[] encoding = new String[transcription.size()];
        int index = 1;
        for (final String element : transcription) {
            final String lineIDColumn = pageID + "_" + lineID + "_"
                    + COLUMN_PREFIX + index;
            this.addElement(element, lineIDColumn);
            encoding[index - 1] = lineIDColumn;
            ++index;
        }
        return encoding;
    }

    /**
     * Decodes an encoded text line
     * 
     * @param encoding
     *            The encoding which should be decoded
     * @param abbreviationOpener
     *            The opener for abbreviation (used only if the character is not
     *            a single character but an abbreviation)
     * @param abbreviationCloser
     *            The closer for abbreviation (used only if the character is not
     *            a single character but an abbreviation)
     * @return The decoded text line as Array
     */
    public String[] decodeTranscription(final String[] encoding,
            final char abbreviationOpener, final char abbreviationCloser) {
        final String[] decodedLine = new String[encoding.length];
        for (final String element : encoding) {
            if (!element.isEmpty()) {
                final String[] columnAsString = element.split(COLUMN_PREFIX);
                decodedLine[Integer
                        .valueOf(columnAsString[columnAsString.length - 1]) - 1] = this
                        .getElementAt(element, abbreviationOpener,
                                abbreviationCloser);
            }
        }
        return decodedLine;
    }

    /**
     * Adds an element to the alphabet
     * 
     * @param element
     *            The element to be added
     * @param lineIdColumn
     *            The ID of the line and the column at which the element is
     *            located at.
     */
    public void addElement(final String element, final String lineIdColumn) {
        ArrayList<String> positions = this.alphabet.get(element);
        if (positions == null) {
            positions = new ArrayList<>();
            this.alphabet.put(element, positions);
        }
        positions.add(lineIdColumn);
    }

    /**
     * Removes an Array of elements from the alphabet
     * 
     * @param encoding
     *            The element to be removed
     */
    public void removeEncoding(final String[] encoding) {
        final Set<Entry<String, ArrayList<String>>> entries = this.alphabet
                .entrySet();
        for (final String element : encoding) {
            if (!element.isEmpty()) {
                for (final Map.Entry<String, ArrayList<String>> entry : entries) {
                    final ArrayList<String> elements = entry.getValue();
                    if (elements.remove(element)) {
                        if (elements.size() == 0) {
                            this.alphabet.remove(entry.getKey());
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Removes the encoding of a whole line from the alphabet
     * 
     * @param lineId
     *            The ID of the line which should be removed from the alphabet
     */
    public void removeEncodingOfLine(final String lineId) {
        final Iterator<Entry<String, ArrayList<String>>> it = this.alphabet
                .entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<String, ArrayList<String>> entry = it.next();

            final ArrayList<String> codes = entry.getValue();

            for (int i = codes.size() - 1; i >= 0; --i) {
                if (codes.get(i).contains(lineId)) {
                    codes.remove(i);
                    if (codes.size() == 0) {
                        it.remove();
                    }
                }
            }
        }
    }

    /**
     * Debug method to print the alphabet to std out.
     */
    public void printAlphabetAndCount() {
        final Set<Entry<String, ArrayList<String>>> entries = this.alphabet
                .entrySet();
        System.out.println("-----Alphabet-----");
        for (final Map.Entry<String, ArrayList<String>> entry : entries) {
            final ArrayList<String> codes = entry.getValue();
            System.out.print(entry.getKey() + ": " + entry.getValue().size()
                    + " ");
            for (final String s : codes) {
                System.out.print(s + " ");
            }
            System.out.println();
        }
        System.out.println("------------------");
    }
}
