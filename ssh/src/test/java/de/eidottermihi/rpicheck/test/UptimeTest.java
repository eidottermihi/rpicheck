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
