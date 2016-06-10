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

import java.io.File;

/**
 * This class is used to filter the image files which extends from
 * javax.swing.filechooser.FlieFilter.
 */
public class ImageFileFilter extends javax.swing.filechooser.FileFilter
        implements java.io.FileFilter {

    /**
     * gif format
     */
    private static final String GIF = "gif";

    /**
     * jpg format
     */
    private static final String JPG = "jpg";

    /**
     * jpeg format
     */
    private static final String JPEG = "jpeg";

    /**
     * png format
     */
    private static final String PNG = "png";

    /**
     * tif format
     */
    private static final String TIF = "tif";

    /**
     * tiff format
     */
    private static final String TIFF = "tiff";

    /**
     * This method is used to get the extension of a file
     * 
     * @param f
     *            file f
     * @return the extension
     */
    private String getExtension(final File f) {
        String extension = null;
        final String name = f.getName();
        final int i = name.lastIndexOf('.');

        if (i > 0 && i < name.length() - 1) {
            extension = name.substring(i + 1).toLowerCase();
        }
        return extension;
    }

    @Override
    public boolean accept(final File f) {
        if (f.isDirectory()) {
            return true;
        }

        final String extension = this.getExtension(f);
        if (extension != null) {
            return extension.equals(GIF) || extension.equals(JPG)
                    || extension.equals(JPEG) || extension.equals(PNG)
                    || extension.equals(TIF) || extension.equals(TIFF);
        }

        return false;
    }

    @Override
    public String getDescription() {
        return "*." + GIF + "; " + "*." + JPG + "; " + "*." + JPEG + "; "
                + "*." + PNG + "; " + "*." + TIF + "; " + "*." + TIFF + "; ";
    }

}
