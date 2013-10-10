package de.eidottermihi.rpicheck.activity.helper;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

/**
 * @author Michael
 * 
 */
public class LoggingHelper {
	private static final Level STD_LEVEL_RASPITOOLS = Level.INFO;
	private static final Level STD_LEVEL_RPICHECK = Level.INFO;

	/**
	 * Changes the logging level of rpicheck and raspitools.
	 * 
	 * @param debugEnabled
	 *            if debug is enabled
	 */
	public static void changeLogger(boolean debugEnabled) {
		ch.qos.logback.classic.Logger rpicheckLogger = (ch.qos.logback.classic.Logger) LoggerFactory
				.getLogger("de.eidottermihi.rpicheck");
		ch.qos.logback.classic.Logger raspitoolsLogger = (ch.qos.logback.classic.Logger) LoggerFactory
				.getLogger("de.eidottermihi.raspitools");
		if (debugEnabled) {
			rpicheckLogger.setLevel(Level.DEBUG);
			raspitoolsLogger.setLevel(Level.DEBUG);
		} else {
			rpicheckLogger.setLevel(STD_LEVEL_RPICHECK);
			raspitoolsLogger.setLevel(STD_LEVEL_RASPITOOLS);
		}
	}

}
