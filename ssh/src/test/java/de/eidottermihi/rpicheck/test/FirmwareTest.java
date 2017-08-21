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

/**
 * @author Michael
 */
public class FirmwareTest extends AbstractMockedQueryTest {

    @Test
    public void firmware_hash() throws IOException, RaspiQueryException {
        String output = FileUtils.readFileToString(FileUtils
                .getFile("src/test/java/de/eidottermihi/rpicheck/test/vcgencmd_version.txt"));
        sessionMocker.withCommand("vcgencmd version",
                new CommandMocker().withResponse(output).mock());
        String firmwareVersion = raspiQuery.queryFirmwareVersion("vcgencmd");
        assertEquals("7789db48 (clean) (release)", firmwareVersion);
    }

    @Test
    public void firmware_old_format() throws IOException, RaspiQueryException {
        String output = FileUtils.readFileToString(FileUtils
                .getFile("src/test/java/de/eidottermihi/rpicheck/test/vcgencmd_version_old.txt"));
        sessionMocker.withCommand("vcgencmd version",
                new CommandMocker().withResponse(output).mock());
        String firmwareVersion = raspiQuery.queryFirmwareVersion("vcgencmd");
        assertEquals("362371 (release)", firmwareVersion);
    }

    @Test
    public void firmware_unkown_format() throws IOException, RaspiQueryException {
        String output = "2.0.1.RELEASE";
        sessionMocker.withCommand("vcgencmd version",
                new CommandMocker().withResponse(output).mock());
        String firmwareVersion = raspiQuery.queryFirmwareVersion("vcgencmd");
        assertEquals("n/a", firmwareVersion);
    }
}
