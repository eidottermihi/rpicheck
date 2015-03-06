/**
 * Copyright (C) 2015  RasPi Check Contributors
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

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

/**
 * @author Michael
 * 
 */
public class LoggingHelper {
	private static final Level STD_LEVEL_RPICHECK = Level.INFO;

	/**
	 * Changes the logging level of rpicheck.
	 * 
	 * @param debugEnabled
	 *            if debug is enabled
	 */
	public static void changeLogger(boolean debugEnabled) {
		ch.qos.logback.classic.Logger rpicheckLogger = (ch.qos.logback.classic.Logger) LoggerFactory
				.getLogger("de.eidottermihi.rpicheck");
		if (debugEnabled) {
			rpicheckLogger.setLevel(Level.DEBUG);
		} else {
			rpicheckLogger.setLevel(STD_LEVEL_RPICHECK);
		}
	}

}
