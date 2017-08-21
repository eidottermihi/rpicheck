/**
 * Copyright (C) 2017  RasPi Check Contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package de.eidottermihi.rpicheck.activity.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.slf4j.LoggerFactory;

import java.io.File;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import de.eidottermihi.rpicheck.activity.SettingsActivity;

/**
 * @author Michael
 */
public class LoggingHelper {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LoggingHelper.class);

    /**
     * Initialize and configure the logback logging.
     * Java configuration is used to rely on {@link Context#getExternalFilesDir(String)} to provide a storage directory.
     * This must be called on every "entry-point" to this app, e.g. Widget Configuration screen or MainActivity.
     *
     * @param context application context
     */
    public static void initLogging(Context context) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isDebugLogging = sharedPrefs.getBoolean(SettingsActivity.KEY_PREF_DEBUG_LOGGING, false);
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.reset();
        // qualify Logger to disambiguate from org.slf4j.Logger
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.TRACE);

        PatternLayoutEncoder encoder1 = new PatternLayoutEncoder();
        encoder1.setContext(lc);
        encoder1.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        encoder1.start();

        // setup LogcatAppender
        PatternLayoutEncoder encoder2 = new PatternLayoutEncoder();
        encoder2.setContext(lc);
        encoder2.setPattern("[%thread] %msg%n");
        encoder2.start();

        LogcatAppender logcatAppender = new LogcatAppender();
        logcatAppender.setContext(lc);
        logcatAppender.setEncoder(encoder2);
        logcatAppender.start();

        // Log-Level for RaspiCheck
        ch.qos.logback.classic.Logger rpicheckLogger = (ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger("de.eidottermihi.rpicheck");
        rpicheckLogger.setLevel(isDebugLogging ? Level.DEBUG : Level.INFO);
        ch.qos.logback.classic.Logger sshjLogger = (ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger("net.schmizz");
        sshjLogger.setLevel(Level.WARN);

        // setup FileAppender
        String logPath = getLogfilePath(context);
        if (logPath != null) {
            FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
            fileAppender.setContext(lc);
            fileAppender.setFile(logPath);
            fileAppender.setEncoder(encoder1);
            fileAppender.start();
            rpicheckLogger.addAppender(fileAppender);
            sshjLogger.addAppender(fileAppender);
        }
        root.addAppender(logcatAppender);
        LOGGER.debug("Logging was configured, debug logging enabled: {}", isDebugLogging);
    }

    /**
     * @param context
     * @return Path to the Logfile or null if its currently not available
     */
    public static String getLogfilePath(Context context) {
        File externalFilesDir = context.getExternalFilesDir(null);
        if (externalFilesDir != null) {
            return externalFilesDir.getAbsolutePath() + "/" + Constants.LOG_NAME;
        }
        return null;
    }

}
