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

import org.junit.Assert;
import org.junit.Test;

import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;
import de.eidottermihi.rpicheck.test.mocks.CommandMocker;

public class SerialNoTest extends AbstractMockedQueryTest {
    @Test
    public void serial() throws RaspiQueryException {
        sessionMocker.withCommand(
                "cat /proc/cpuinfo | grep Serial",
                new CommandMocker().withResponse(
                        "Serial          : 0000000041e5647d").mock());
        Assert.assertEquals("0000000041e5647d", raspiQuery.queryCpuSerial());
    }

    @Test
    public void serial_crap() throws RaspiQueryException {
        sessionMocker.withCommand("cat /proc/cpuinfo | grep Serial",
                new CommandMocker().withResponse("No valid entry").mock());
        Assert.assertEquals("n/a", raspiQuery.queryCpuSerial());
    }
}
