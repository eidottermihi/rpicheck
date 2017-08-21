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
