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
package de.diptychon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * The DiptychonPreferences. All Preferences are stored using the Preferences
 * Property of Java
 * 
 * @see java.util.prefs.Preferences
 */
public final class DiptychonPreferences {
    /**
     * counter for glyph merging
     */
    public static int glyphMergingCounter = 0;

    /**
     * counter for glyph separation through RGF
     */
    public static int glyphSeparateByRGFCounter = 0;

    /**
     * counter for glyph separation
     */
    public static int glyphSeparateByLineCounter = 0;

    /**
     * prefix for properties
     */
    private static final String DIPTYCHON = "diptychon.";

    /**
     * path to property where the path to the recently used projects are stored
     */
    private static final String RECENTLY_USED = DIPTYCHON + "recently.used.";

    /**
     * Used to configure how many recently used projects should be kept for fast
     * access via "open recently used"
     */
    private static final int RECENTLY_USED_MEMORY = 5;

    /**
     * path to property where the path to the last shown directory in a
     * FileChooser is stores
     */
    public static final String LAST_SHOWN_DIRECTORY = DIPTYCHON
            + "last.shown.directory";

    /**
     * Pointer to the root element of the diptychon properties.
     */
    private static java.util.prefs.Preferences root;

    /**
     * diptychon home folder property
     */
    public static final String DIPTYCHON_HOME = DIPTYCHON + "home";

    /**
     * Empty Default Constructor
     */
    private DiptychonPreferences() {

    }

    /**
     * For debug purpose only; used to print all properties to stdout
     */
    private static void display() {
        try {
            final String[] keys = root.keys();
            for (final String key : keys) {
                System.out.println(key + ": " + root.get(key, "---"));
            }
        } catch (final BackingStoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the Property for the parameter string
     * 
     * @param what
     *            the parameter
     * @return the property
     */
    public static String get(final String what) {
        DiptychonLogger.debug("Getting preference: {}", what);
        switch (what) {
        case DIPTYCHON_HOME:
            String diptychonDir = root.get(DIPTYCHON_HOME, "");
            if (diptychonDir.equals("")) {
                diptychonDir = System.getProperty("user.home");
                diptychonDir += File.separator + "Diptychon";
                root.put(DIPTYCHON_HOME, diptychonDir);
            }
            if (!(new File(diptychonDir).exists())) {
                new File(diptychonDir).mkdir();
            }
            return diptychonDir;
        default:
            return root.get(what, null);
        }
    }

    /**
     * Return the project which was used last
     * 
     * @return the project which was used last
     */
    public static String getLastUsed() {
        return getRecentlyUsed().get(0);
    }

    /**
     * Returns the list of the x recently used projects
     * 
     * @return a list of the recently used projects
     */
    public static List<String> getRecentlyUsed() {
        DiptychonLogger.debug("Getting property: recently.used.");
        final List<String> recentlyUsed = new ArrayList<>(5);
        for (int i = 0; i < RECENTLY_USED_MEMORY; ++i) {
            final String key = RECENTLY_USED + i;
            final String filename = root.get(key, "");
            if (!(new File(filename).exists())) {
                root.put(key, "");
            } else {
                recentlyUsed.add(filename);
            }
        }
        return recentlyUsed;
    }

    /**
     * Returns the pathname of the Directory that was visible in the FileChooser
     * while opened or saved the last file.
     * 
     * @return The abstract pathname to a directory
     */
    public static String getLastShownDirectory() {
        String lastShown = get(LAST_SHOWN_DIRECTORY);
        DiptychonLogger.debug(lastShown);
        if (lastShown == null || lastShown.isEmpty() || !new File(lastShown).exists()) {
            return get(DIPTYCHON_HOME);
        }
        return lastShown;
    }

    /**
     * Updates the last shown directory property by saving the parent of the
     * given file. Does nothing if {@code file} is {@code null}.
     * 
     * @param file
     *            The file that was selected for the last open/save-operation
     */
    public static void updateLastShownDirectory(File file) {
        if (file != null) {
            DiptychonPreferences.set(DiptychonPreferences.LAST_SHOWN_DIRECTORY,
                    file.getParent());
        }
    }

    /**
     * Updates the last shown directory property by saving the parent of the
     * first file in the given list. Does nothing if {@code files} is
     * {@code null} or empty.
     * 
     * @param files
     *            The files that were selected for the last open/save-operation
     */
    public static void updateLastShownDirectory(List<File> files) {
        if (files != null && !files.isEmpty()) {
            updateLastShownDirectory(files.get(0));
        }
    }

    /**
     * Initially reading the properties
     */
    static void readElements() {
        DiptychonLogger.debug("Reading preferences");
        root = Preferences.userRoot().node("/de/diptychon");

        // for debug purpose: Comment in to clear preferences entries
        // try
        // {
        // root.clear();
        // }
        // catch (BackingStoreException e)
        // {
        // e.printStackTrace();
        // }
    }

    /**
     * Sets the value of <code>property</code> to <code>what</code>
     * 
     * @param property
     *            the key
     * @param value
     *            the what
     */
    public static void set(final String property, final String value) {
        DiptychonLogger.debug("Setting property {} to {}", property, value);
        final String replacedValue = value.replaceAll("\\\\", "\\/");
        root.put(property, replacedValue);
    }

    /**
     * When a project was opened or saved the recently used projects has to be
     * updated, which is done by this method
     * 
     * @param filename
     *            the filename of the current project
     * @return the updated list of recently used projects
     */
    public static List<String> updateRecentlyUsed(final String filename) {
        final List<String> recentlyUsed = new ArrayList<>(RECENTLY_USED_MEMORY);
        for (int i = 0; i < RECENTLY_USED_MEMORY; ++i) {
            String key = RECENTLY_USED + (i);
            final String value = root.get(key, "");
            if (!value.equals(filename)) {
                key = RECENTLY_USED + i;
                recentlyUsed.add(value);
            }
        }
        root.put(RECENTLY_USED + "0", filename);
        final int end = Math.min(recentlyUsed.size(), RECENTLY_USED_MEMORY);
        for (int i = 1; i < end; ++i) {
            root.put(RECENTLY_USED + i, recentlyUsed.get(i - 1));
        }
        for (int i = end; i < RECENTLY_USED_MEMORY; ++i) {
            root.put(RECENTLY_USED + i, "");
        }
        return getRecentlyUsed();
    }

}
