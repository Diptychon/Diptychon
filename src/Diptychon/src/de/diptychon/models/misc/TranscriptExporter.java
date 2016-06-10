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
package de.diptychon.models.misc;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import de.diptychon.models.representation.Alphabet;
import de.diptychon.models.representation.DocumentPage;
import de.diptychon.models.representation.PageLine;
import de.diptychon.models.representation.Transcript;

/**
 * For Information about the RTF specification see
 * http://www.biblioscape.com/rtf15_spec.htm
 */
public class TranscriptExporter {

    /**
     * RTF Header
     */
    private static final String RTF_HEADER = "{\\rtf1\\ansi\\ansicpg1252\\deff0\\"
            + "deflang1031{\\fonttbl{\\f0\\fnil";

    /**
     * RTF Metadata
     */
    private static final String RTF_GENERATOR = "{\\*\\generator Diptychon RTF Exporter v0.1.20121130;}";

    /**
     * RTF Parameter like Font, Fontsize etc
     */
    private static final String RTF_PARAMETER = "\\viewkind4\\uc1\\pard\\sa200\\sl240\\slmult1\\f0\\fs24";

    /**
     * RTF Fileending
     */
    private static final String RTF_CLOSER = "}";

    /**
     * RTF Linebreak
     */
    private static final String RTF_LINE_BREAK = "\\par ";

    /**
     * RTF Tabulator
     */
    private static final String RTF_TAB = "\\tab ";

    /**
     * RTF Italic start
     */
    private static final String RTF_ITALIC_START = "\\i ";

    /**
     * RTF italic end
     */
    private static final String RTF_ITALIC_END = "\\i0 ";

    /**
     * RTF new page
     */
    private static final String RTF_PAGE_END = "\\page ";

    private static final String TEI_XMLNS = "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\"> ";

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    /**
     * Write file to
     */
    private FileWriter outFile;

    private PrintWriter out;

    public void exportAsTEI(final String filename, final Transcript transcript,
            final String sourceName) throws IOException {
        this.out = new PrintWriter(filename);

        out.println(XML_HEADER);
        out.println(TEI_XMLNS);
        out.println("<teiHeader>");
        out.println("<fileDesc>");
        out.println("<titleStmt>");
        out.println("<title>Diptychon TEI Export File</title>");
        out.println("</titleStmt>");
        out.println("<sourceDesc>");
        out.println("<p>" + sourceName + "</p>");
        out.println("</sourceDesc>");
        out.println("</fileDesc>");
        out.println("</teiHeader>");
        out.println("<text>");
        out.println("<body>");

        final DocumentPage[] documentPages = transcript.getDocumentPages();
        final Alphabet alphabet = transcript.getAlphabet();
        int pageCounter = 1;
        for (final DocumentPage dp : documentPages) {
            out.println("<p>");
            ++pageCounter;
            final Collection<PageLine> pageLines = dp.getLines();
            if (pageLines.isEmpty()) {
                continue;
            }

            final ArrayList<PageLine> convertedAndSorted = new ArrayList<>(
                    pageLines);
            Collections.sort(convertedAndSorted);

            int i = 1;
            for (final PageLine pl : pageLines) {
                final String[] decoding = alphabet.decodeTranscription(
                        pl.getEncoding(), '(', ')');
                if (!pl.isHandwrittenAnnotation()) {
                    if (!pl.isEmpty()) {
                        this.writelnTEI(decoding);
                    } else {
                        this.out.println("<lb/>");
                    }

                    ++i;
                } else {
                    this.out.print("<note place=\"foot\">");
                    this.writeTEI(decoding);
                    this.out.println("</note><lb/>");
                }
            }
            out.println("</p>");
            if (i + 1 < documentPages.length) {
                out.println("<pb xml:id=\"p" + i + 1 + "\"/>");
            }
        }

        out.println("</body>");
        out.println("</text>");
        out.println("</TEI>");
        this.out.close();
    }

    /**
     * Exports a transcript to a given filename as rtf
     * 
     * @param filename
     *            the filename
     * @param transcript
     *            the transcript
     * @param fontname
     *            the font of the rtf
     * @throws IOException
     *             thrown when file could not be found, created or is not
     *             writable
     */
    public void exportAsRTF(final String filename, final Transcript transcript,
            final String fontname) throws IOException {

        this.outFile = new FileWriter(filename);
        this.outFile.write(RTF_HEADER + "\\fcharset0 " + fontname + ";}}"
                + System.lineSeparator() + RTF_GENERATOR + RTF_PARAMETER
                + System.lineSeparator());

        final DocumentPage[] documentPages = transcript.getDocumentPages();
        final Alphabet alphabet = transcript.getAlphabet();
        int pageCounter = 1;
        for (final DocumentPage dp : documentPages) {
            this.outFile.write("Page " + pageCounter + RTF_LINE_BREAK
                    + System.lineSeparator());
            ++pageCounter;
            final Collection<PageLine> pageLines = dp.getLines();
            if (pageLines.isEmpty()) {
                continue;
            }

            final ArrayList<PageLine> convertedAndSorted = new ArrayList<>(
                    pageLines);
            Collections.sort(convertedAndSorted);

            int i = 1;
            this.outFile.write("1");
            for (final PageLine pl : pageLines) {
                final String[] decoding = alphabet.decodeTranscription(
                        pl.getEncoding(), '(', ')');
                if (!pl.isHandwrittenAnnotation()) {
                    if (i % 5 == 0) {
                        this.outFile.write(i + "");
                    }
                    if (!pl.isEmpty()) {
                        this.writelnRTF(decoding);
                    } else {
                        this.outFile.write(RTF_LINE_BREAK
                                + System.lineSeparator());
                    }

                    ++i;
                } else {
                    this.outFile.write(RTF_ITALIC_START);
                    this.writelnRTF(decoding);
                    this.outFile.write(RTF_ITALIC_END);
                }
            }
            this.outFile.write(RTF_LINE_BREAK + RTF_PAGE_END
                    + System.lineSeparator());
        }

        this.outFile.write(RTF_CLOSER);
        this.outFile.flush();
        this.outFile.close();

    }

    /**
     * Writes a single line as rtf
     * 
     * @param line
     *            The line to write
     * @throws IOException
     *             thrown when file is not writable or could not be found
     */
    private void writelnRTF(final String[] line) throws IOException {
        this.outFile.write(RTF_TAB);
        for (final String s : line) {
            for (int i = 0; i < s.length(); ++i) {
                this.outFile.write("\\u"
                        + String.format("%04d", s.codePointAt(i)) + "?");
            }
        }
        this.outFile.write(RTF_LINE_BREAK + System.lineSeparator());
    }

    private void writelnTEI(final String[] line) throws IOException {
        for (final String s : line) {

            for (int i = 0; i < s.length(); ++i) {
                this.out.print(s.charAt(i));
            }

        }
        this.out.println("<lb/>");
    }

    private void writeTEI(final String[] line) throws IOException {
        for (final String s : line) {

            for (int i = 0; i < s.length(); ++i) {
                this.out.print(s.charAt(i));
            }

        }
    }

    /**
     * Exports a transcript to a given filename as plain text
     * 
     * @param filename
     *            the filename
     * @param transcript
     *            the transcript
     * @throws IOException
     *             thrown when file could not be found, created or is not
     *             writable
     */
    public void exportAsPlainText(final String filename,
            final Transcript transcript) throws IOException {

        this.outFile = new FileWriter(filename);
        final DocumentPage[] documentPages = transcript.getDocumentPages();
        final Alphabet alphabet = transcript.getAlphabet();
        int pageCounter = 1;
        for (final DocumentPage dp : documentPages) {
            this.outFile.write("Page " + pageCounter + System.lineSeparator());
            ++pageCounter;
            final Collection<PageLine> pageLines = dp.getLines();
            if (pageLines.isEmpty()) {
                continue;
            }
            final ArrayList<PageLine> convertedAndSorted = new ArrayList<>(
                    pageLines);
            Collections.sort(convertedAndSorted);
            for (final PageLine pl : pageLines) {
                final String[] decoding = alphabet.decodeTranscription(
                        pl.getEncoding(), '(', ')');
                if (!pl.isEmpty()) {
                    this.writelnPlainText(decoding);
                } else {
                    this.outFile.write(System.lineSeparator());
                }
            }
            this.outFile.write(System.lineSeparator());
        }
        this.outFile.flush();
        this.outFile.close();

    }

    /**
     * Writes a single line as plaintext
     * 
     * @param line
     *            The line to write
     * @throws IOException
     *             thrown when file is not writable or could not be found
     */
    private void writelnPlainText(final String[] line) throws IOException {
        for (final String s : line) {
            this.outFile.write(s);
        }
        this.outFile.write(System.lineSeparator());
    }
}
