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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Extension of the apache log4j logger to be able to log to stdout and a file
 * at the same time
 */
public final class DiptychonLogger

{
    /**
     * Logger to console
     */
    private static Logger stdLogger;

    /**
     * Logger to file
     */
    private static Logger fileLogger;

    /**
     * Empty Default Constructor
     */
    private DiptychonLogger() {

    }

    /**
     * Initializes the Logger
     */
    public static void createDiptychonLogger() {
        stdLogger = LogManager.getLogger("Console");
        fileLogger = LogManager.getLogger("File");
    }

    /**
     * Logs a message with parameters at the <code>ERROR</code> level.
     * 
     * @param message
     *            the message to log.
     * @param params
     *            parameters to the message.
     */
    public static void error(final String message, final Object... params) {
        stdLogger.error(message, params);
        fileLogger.error(message, params);
    }

    /**
     * Logs a message with parameters at the <code>TRACE</code> level.
     * 
     * @param message
     *            the message to log.
     * @param params
     *            parameters to the message.
     */
    public static void trace(final String message, final Object... params) {
        stdLogger.trace(message, params);
        fileLogger.trace(message, params);
    }

    /**
     * Logs a message with parameters at the <code>WARN</code> level.
     * 
     * @param message
     *            the message to log.
     * @param params
     *            parameters to the message.
     */
    public static void warn(final String message, final Object... params) {
        stdLogger.warn(message, params);
        fileLogger.warn(message, params);
    }

    /**
     * Logs a message with parameters at the <code>DEBUG</code> level.
     * 
     * @param message
     *            the message to log.
     * @param params
     *            parameters to the message.
     */
    public static void debug(final String message, final Object... params) {
        stdLogger.debug(message, params);
        fileLogger.debug(message, params);
    }

    /**
     * Logs a message with parameters at the <code>FATAL</code> level.
     * 
     * @param message
     *            the message to log.
     * @param params
     *            parameters to the message.
     */
    public static void fatal(final String message, final Object... params) {
        stdLogger.fatal(message, params);
        fileLogger.fatal(message, params);
    }

    /**
     * Logs a message with parameters at the <code>INFO</code> level.
     * 
     * @param message
     *            the message to log.
     * @param params
     *            parameters to the message.
     */
    public static void info(final String message, final Object... params) {
        stdLogger.info(message, params);
        fileLogger.info(message, params);
    }
}
