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

import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.transport.TransportException;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import de.eidottermihi.rpicheck.ssh.beans.VcgencmdBean;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;
import de.eidottermihi.rpicheck.test.mocks.CommandMocker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class VcgencmdTest extends AbstractMockedQueryTest {

    @Test
    public void vcgencmd_in_path() throws RaspiQueryException,
            ConnectionException, TransportException {
        String vcgencmdPath = "vcgencmd";
        sessionMocker.withCommand(vcgencmdPath, new CommandMocker()
                .withResponse("vcgencmd version 1.2.3.4").mock());
        sessionMocker.withCommand(vcgencmdPath + " measure_clock arm",
                new CommandMocker().withResponse("frequency(45)=840090000")
                        .mock());
        sessionMocker.withCommand(vcgencmdPath + " measure_clock core",
                new CommandMocker().withResponse("frequency(1)=320000000")
                        .mock());
        sessionMocker.withCommand(vcgencmdPath + " measure_volts core",
                new CommandMocker().withResponse("volt=1.200V").mock());
        sessionMocker.withCommand(vcgencmdPath + " measure_temp",
                new CommandMocker().withResponse("temp=41.2'C").mock());
        sessionMocker
                .withCommand(
                        vcgencmdPath + " version",
                        new CommandMocker()
                                .withResponse(
                                        "Dec 29 2014 14:23:10\nCopyright (c) 2012 Broadcom\nversion d3c15a3b57203798ff811c40ea65174834267d48 (clean) (release)")
                                .mock());
        VcgencmdBean vcgencmd = raspiQuery.queryVcgencmd();
        assertNotNull(vcgencmd);
        assertThat(vcgencmd.getArmFrequency(), CoreMatchers.is(840090000L));
        assertThat(vcgencmd.getCoreFrequency(), CoreMatchers.is(320000000L));
        assertEquals(1.200, vcgencmd.getCoreVolts(), 0.00001);
        assertEquals(41.2, vcgencmd.getCpuTemperature(), 0.00001);
    }

}
