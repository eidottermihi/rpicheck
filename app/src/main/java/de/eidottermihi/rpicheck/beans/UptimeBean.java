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
package de.eidottermihi.rpicheck.beans;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class UptimeBean implements Serializable {
    private static final long serialVersionUID = 2L;

    private long secondsRunning;

    public UptimeBean(double uptimeFull) {
        this.secondsRunning = (long) uptimeFull;
    }

    public long getSecondsRunning() {
        return secondsRunning;
    }

    public void setSecondsRunning(long secondsRunning) {
        this.secondsRunning = secondsRunning;
    }

    public String getRunningPretty() {
        // current time - (secondsRunning * 1000) => start time
        PrettyTime pretty = new PrettyTime();
        return pretty.format(new Date(Calendar.getInstance().getTimeInMillis()
                - (secondsRunning * 1000)));
    }

}
