/**
 * MIT License
 *
 * Copyright (c) 2019  RasPi Check Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
