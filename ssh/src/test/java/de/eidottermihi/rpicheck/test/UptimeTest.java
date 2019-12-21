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

import org.junit.Test;

import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;
import de.eidottermihi.rpicheck.test.mocks.CommandMocker;

import static org.junit.Assert.assertEquals;

public class UptimeTest extends AbstractMockedQueryTest {

    @Test
    public void uptime() throws RaspiQueryException {
        sessionMocker.withCommand("cat /proc/uptime", new CommandMocker()
                .withResponse("20256.17 80516.80").mock());
        assertEquals(20256.17D, raspiQuery.queryUptime(), 0.0001D);
    }

    @Test
    public void uptime_copyright_header() throws RaspiQueryException {
        sessionMocker
                .withCommand(
                        "cat /proc/uptime",
                        new CommandMocker()
                                .withResponse(
                                        "Copyright (C) stuff right here\n\nbogus line\n\n With multiple line breaks\nAnd other weird stuff in a proc file\n20256.17 80516.80")
                                .mock());
        assertEquals(20256.17D, raspiQuery.queryUptime(), 0.0001D);
    }

    @Test
    public void uptime_unexpected_output() throws RaspiQueryException {
        sessionMocker
                .withCommand(
                        "cat /proc/uptime",
                        new CommandMocker()
                                .withResponse(
                                        "Copyright (C) stuff right here\n\nbogus line\n\n With multiple line breaks\nAnd other weird stuff in a proc file\n")
                                .mock());
        assertEquals(0.0D, raspiQuery.queryUptime(), 0.0001D);
    }

}
