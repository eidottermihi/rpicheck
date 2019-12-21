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
