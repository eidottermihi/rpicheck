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
