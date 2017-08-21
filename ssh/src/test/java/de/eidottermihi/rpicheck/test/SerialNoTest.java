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
