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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.xml.XmpSerializer;

import de.diptychon.DiptychonLogger;
import de.diptychon.models.data.DocumentImage;
import de.diptychon.models.data.Glyph;
import de.diptychon.models.representation.DocumentPage;
import de.diptychon.models.representation.PageLine;
import de.diptychon.models.representation.Transcript;

/**
 * Class to create a PDF-file containing the document as image and the
 * transcript as searchable, invisible text
 */
public class PDFCreator {
    // TODO user feedback in case of success and failure

    // glyph method getIDWithoutPrefix does not work
    // (java.lang.NumberFormatException)
    // bounding boxes not set properly for large glyphs?

    private PDDocument doc;
    private PDFont font;
    private boolean parentheses;
    private ArrayList<Integer> heightsOfWords;

    /**
     * Constructor creating a new document
     */
    public PDFCreator() {
        doc = new PDDocument();
    }

    /**
     * create a PDF-file containing the document as image and the transcript as
     * searchable, invisible text
     * 
     * @param file
     *            name under which the file should be saved
     * @param documentImages
     *            array of the document images
     * @param imagePath
     *            the path of the image
     * @param transcript
     *            the transcript to get the document pages
     * @param parentheses
     *            whether or not parentheses should be set for abbreviations
     */
    public static void createPDF(String file, DocumentImage[] documentImages,
            String imagePath, Transcript transcript, boolean parentheses) {
        PDFCreator pdfCreator = new PDFCreator();
        DiptychonLogger.info("Creating PDF-file as " + file);
        pdfCreator.parentheses = parentheses;
        try {
            InputStream fontStream = PDFCreator.class
                    .getResourceAsStream("/pdfExport/ArialMT.ttf");
            pdfCreator.font = PDTrueTypeFont
                    .loadTTF(pdfCreator.doc, fontStream);

            final DocumentPage[] documentPages = transcript.getDocumentPages();// one
                                                                               // page
                                                                               // per
                                                                               // image?
            for (int i = 0; i < documentImages.length; i++) {
                pdfCreator.addPage(documentImages[i], imagePath,
                        documentPages[i]);
            }
            pdfCreator.makePDFAConform();
            pdfCreator.doc.save(file);
            DiptychonLogger.info("Saved PDF-file" + file);
        } catch (Exception e) {
            // TODO: handle exception
            DiptychonLogger.error("{}", e);
        } finally {
            if (pdfCreator.doc != null) {
                try {
                    pdfCreator.doc.close();
                } catch (IOException e) {
                    DiptychonLogger.error("{}", e);
                }
            }
        }
    }

    /**
     * adds a Page with content to the PDF file
     * 
     * @param documentImage
     *            DocumentImage which maintains the image for this page
     * @param imagePath
     *            the path of the image
     * @param dp
     *            the DocumentPage containing the lines for this page
     * @throws Exception
     */
    private void addPage(DocumentImage documentImage, String imagePath,
            DocumentPage dp) throws Exception {
        heightsOfWords = new ArrayList<Integer>();

        Image fxImage = documentImage.load(imagePath, false);
        PDXObjectImage image = null;
        BufferedImage bi = SwingFXUtils.fromFXImage(fxImage, null);
        image = new PDJpeg(doc, bi);

        PDPage page = new PDPage(new PDRectangle(image.getWidth(),
                image.getHeight()));
        doc.addPage(page);

        final Collection<PageLine> pageLines = dp.getLines();

        PDPageContentStream contentStream = new PDPageContentStream(doc, page);
        for (PageLine pl : pageLines) {
            ArrayList<Glyph> glyphs = documentImage.getGlyphs(pl.getID());
            if (!glyphs.isEmpty()) {
                sortGlyphList(glyphs);
                calculateAndDrawStrings(glyphs, contentStream,
                        image.getHeight());
            }
        }
        // Dokumentbild ins PDF kopieren (Text wird damit verdeckt)
        contentStream.drawXObject(image, 0, 0, page.getMediaBox().getWidth(),
                page.getMediaBox().getHeight());
        contentStream.close();

        // Statistik fuer Worthoehen berechnen
        int sumOfHeights = 0;
        for (Integer h : heightsOfWords) {
            sumOfHeights += h;
        }
        int meanHeight = sumOfHeights / heightsOfWords.size();
        System.out.println("Durchschnittshoehe = " + meanHeight);
        double stdvHeight = 0.0;
        for (Integer h : heightsOfWords) {
            stdvHeight = stdvHeight + Math.abs(h - meanHeight);
        }
        stdvHeight = stdvHeight / heightsOfWords.size();
        System.out.println("Varianz = " + stdvHeight);
    }

    /**
     * writes a pageline word by word at the correct glyph position
     * 
     * @param glyphs
     *            an array containing the glyphs of the current pageline
     * @param contentStream
     *            the stream to write on
     * @param pageHeight
     *            the height of the page
     * @throws IOException
     *             thrown when addStringToPDF fails
     */
    void calculateAndDrawStrings(ArrayList<Glyph> glyphs,
            PDPageContentStream contentStream, int pageHeight)
            throws IOException {
        String decodedString = "";
        int glyphCounter = 0;
        int currentGlyphCount = -1;
        double currentMaxY = 0;
        int currentMinY = Integer.MAX_VALUE;
        for (glyphCounter = 0; glyphCounter < glyphs.size(); glyphCounter++) {
            Glyph currentGlyph = glyphs.get(glyphCounter);
            String id = currentGlyph.getGroupID();
            if (id.length() > 1 && parentheses)// set parentheses around first
                                               // char if more than one
                                               // character per glyph
            {
                id = String.format("%c(%s)", id.charAt(0), id.substring(1));
            }
            decodedString += id;
            currentGlyphCount++;
            if (!currentGlyph.isSpace) // get boundaries for current word
            {
                currentMinY = currentGlyph.getLayoutY() < currentMinY ? currentGlyph
                        .getLayoutY() : currentMinY;
                currentMaxY = currentGlyph.getBoundingBox().getMaxY() > currentMaxY ? currentGlyph
                        .getBoundingBox().getMaxY() : currentMaxY;
                double wortHoehe = currentMaxY - currentMinY;
                heightsOfWords.add((int) wortHoehe);
                System.out.println("Worthöhe = " + wortHoehe);
            }
            if (currentGlyph.isSpace)// write current word and reset for next
                                     // word
            {
                Glyph startGlyph = glyphs.get(glyphCounter - currentGlyphCount);
                Glyph endGlyph = glyphs.get(glyphCounter - 1);
                addStringToPDF(contentStream, startGlyph, endGlyph,
                        decodedString, pageHeight, (int) currentMaxY
                                - currentMinY, (int) currentMaxY);
                decodedString = "";
                currentGlyphCount = 0;
                currentMaxY = 0;
                currentMinY = Integer.MAX_VALUE;
            }
        }

        if (!decodedString.isEmpty())// write last word
        {
            Glyph startGlyph = glyphs.get(glyphCounter - currentGlyphCount - 1);
            Glyph endGlyph = glyphs.get(glyphCounter - 1);
            addStringToPDF(contentStream, startGlyph, endGlyph, decodedString,
                    pageHeight, (int) currentMaxY - currentMinY,
                    (int) currentMaxY);
        }
    }

    /**
     * writes one word to the PDPageContentStream
     * 
     * @param contentStream
     *            the stream to write on
     * @param startGlyph
     *            the first glyph of the word
     * @param endGlyph
     *            the last glyph of the word
     * @param decodedString
     *            the string containing the word
     * @param pageHeight
     *            the height of the page
     * @param fontSize
     *            the size of the font
     * @param maxY
     *            the y-baseline to write the word on
     * @throws IOException
     *             thrown when some output operation fails
     */
    void addStringToPDF(PDPageContentStream contentStream, Glyph startGlyph,
            Glyph endGlyph, String decodedString, int pageHeight, int fontSize,
            int maxY) throws IOException {
        int startX = startGlyph.getForcedLayoutX() != -1 ? startGlyph
                .getForcedLayoutX() : startGlyph.getLayoutX();
        float startY = pageHeight - maxY;
        double widthInFont = fontSize * font.getStringWidth(decodedString)
                / 1000;
        double widthInGlyphs = endGlyph.getBoundingBox().getMaxX()
                - startGlyph.getBoundingBox().getMinX();

        String resultString = new String(decodedString.getBytes(), "ISO-8859-1");// somehow
                                                                                 // some
                                                                                 // lines
                                                                                 // have
                                                                                 // different
                                                                                 // text
                                                                                 // representation..

        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.moveTextPositionByAmount(startX, startY);
        contentStream.appendRawCommands(String.format("%d Tz\n",
                (int) (widthInGlyphs / widthInFont * 100)));
        contentStream.drawString(resultString);
        contentStream.endText();
    }

    /**
     * makes the PDF conform to the PDF/A-3b standard
     * 
     * @throws Exception
     *             thrown when either the conformance or the colorprofile can
     *             not be set or when xmp fails
     */
    void makePDFAConform() throws Exception {
        PDDocumentCatalog cat = doc.getDocumentCatalog();
        PDMetadata metadata = new PDMetadata(doc);
        cat.setMetadata(metadata);
        XMPMetadata xmp = XMPMetadata.createXMPMetadata();
        PDFAIdentificationSchema pdfaid = xmp
                .createAndAddPFAIdentificationSchema();
        pdfaid.setConformance("B");
        pdfaid.setPart(3);
        pdfaid.setAboutAsSimple("");
        XmpSerializer serializer = new XmpSerializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(xmp, baos, true);
        metadata.importXMPMetadata(baos.toByteArray());
        InputStream colorProfile = PDFCreator.class
                .getResourceAsStream("/pdfExport/sRGB Color Space Profile.icm");
        PDOutputIntent oi = new PDOutputIntent(doc, colorProfile);
        oi.setInfo("sRGB IEC61966-2.1");
        oi.setOutputCondition("sRGB IEC61966-2.1");
        oi.setOutputConditionIdentifier("sRGB IEC61966-2.1");
        oi.setRegistryName("http://www.color.org");
        cat.addOutputIntent(oi);
    }

    /**
     * sorts the glyphs according to their index in their page line
     * 
     * @param glyphs
     *            list containing the glyphs
     */
    void sortGlyphList(ArrayList<Glyph> glyphs) {
        Collections.sort(glyphs, new Comparator<Glyph>() {
            @Override
            public int compare(Glyph g1, Glyph g2) {
                String delimiter = "COL_";
                String id1 = g1.getID();
                int pos1 = Integer.parseInt(id1.substring(id1
                        .indexOf(delimiter) + delimiter.length()));
                String id2 = g2.getID();
                int pos2 = Integer.parseInt(id2.substring(id2
                        .indexOf(delimiter) + delimiter.length()));
                return pos1 - pos2;
            }
        });
    }
}
