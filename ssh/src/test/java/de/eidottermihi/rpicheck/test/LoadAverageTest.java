/**
 * MIT License
 *
 * Copyright (c) 2018  RasPi Check Contributors
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
package de.eidottermihi.rpicheck.test;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.IOException;

import de.eidottermihi.rpicheck.ssh.LoadAveragePeriod;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;
import de.eidottermihi.rpicheck.test.mocks.CommandMocker;

import static org.junit.Assert.assertEquals;

public class LoadAverageTest extends AbstractMockedQueryTest {

    private static final String COMMAND = "cat /proc/loadavg; cat /proc/stat | grep cpu | wc -l";

    @Test
    public void load_avg() throws IOException, RaspiQueryException {
        String output = FileUtils.readFileToString(FileUtils
                .getFile("src/test/java/de/eidottermihi/rpicheck/test/proc_loadavg.txt"));
        sessionMocker.withCommand(COMMAND,
                new CommandMocker().withResponse(output).mock());
        double queryLoadAverage = raspiQuery
                .queryLoadAverage(LoadAveragePeriod.FIVE_MINUTES);
        assertEquals(0.58D, queryLoadAverage, 0.001D);
    }

    @Test
    public void load_avg_fifteen_minutes() throws IOException,
            RaspiQueryException {
        String output = FileUtils.readFileToString(FileUtils
                .getFile("src/test/java/de/eidottermihi/rpicheck/test/proc_loadavg.txt"));
        sessionMocker.withCommand(COMMAND,
                new CommandMocker().withResponse(output).mock());
        double queryLoadAverage = raspiQuery
                .queryLoadAverage(LoadAveragePeriod.FIFTEEN_MINUTES);
        assertEquals(0.53D, queryLoadAverage, 0.001D);
    }

    @Test
    public void load_avg_one_minute() throws IOException, RaspiQueryException {
        String output = FileUtils.readFileToString(FileUtils
                .getFile("src/test/java/de/eidottermihi/rpicheck/test/proc_loadavg.txt"));
        sessionMocker.withCommand(COMMAND,
                new CommandMocker().withResponse(output).mock());
        double queryLoadAverage = raspiQuery
                .queryLoadAverage(LoadAveragePeriod.ONE_MINUTE);
        assertEquals(0.60D, queryLoadAverage, 0.001D);
    }

    @Test
    public void load_avg_copyright_header() throws IOException,
            RaspiQueryException {
        String output = FileUtils
                .readFileToString(FileUtils
                        .getFile("src/test/java/de/eidottermihi/rpicheck/test/proc_loadavg_with_copyright.txt"));
        sessionMocker.withCommand(COMMAND,
                new CommandMocker().withResponse(output).mock());
        double queryLoadAverage = raspiQuery
                .queryLoadAverage(LoadAveragePeriod.FIVE_MINUTES);
        assertEquals(0.58D, queryLoadAverage, 0.001D);
    }

    @Test
    public void load_avg_pi2_one() throws IOException, RaspiQueryException {
        String output = FileUtils
                .readFileToString(FileUtils
                        .getFile("src/test/java/de/eidottermihi/rpicheck/test/proc_loadavg_pi2.txt"));
        sessionMocker.withCommand(COMMAND,
                new CommandMocker().withResponse(output).mock());
        assertEquals(1.00D,
                raspiQuery.queryLoadAverage(LoadAveragePeriod.ONE_MINUTE),
                0.001D);
    }

    @Test
    public void load_avg_pi2_five() throws IOException, RaspiQueryException {
        String output = FileUtils
                .readFileToString(FileUtils
                        .getFile("src/test/java/de/eidottermihi/rpicheck/test/proc_loadavg_pi2.txt"));
        sessionMocker.withCommand(COMMAND,
                new CommandMocker().withResponse(output).mock());
        assertEquals(0.6475D,
                raspiQuery.queryLoadAverage(LoadAveragePeriod.FIVE_MINUTES),
                0.001D);
    }

    @Test
    public void load_avg_pi2_fifteen() throws IOException, RaspiQueryException {
        String output = FileUtils
                .readFileToString(FileUtils
                        .getFile("src/test/java/de/eidottermihi/rpicheck/test/proc_loadavg_pi2.txt"));
        sessionMocker.withCommand(COMMAND,
                new CommandMocker().withResponse(output).mock());
        assertEquals(0.3125D,
                raspiQuery.queryLoadAverage(LoadAveragePeriod.FIFTEEN_MINUTES),
                0.001D);
    }

}
