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

import org.junit.Before;

import de.eidottermihi.rpicheck.ssh.impl.RaspiQuery;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;
import de.eidottermihi.rpicheck.test.mocks.SSHClientMocker;
import de.eidottermihi.rpicheck.test.mocks.SessionMocker;
import de.eidottermihi.rpicheck.test.mocks.TestingRaspiQuery;

public abstract class AbstractMockedQueryTest {
    protected RaspiQuery raspiQuery;
    protected SSHClientMocker clientMocker;
    protected SessionMocker sessionMocker;

    @Before
    public void init() throws RaspiQueryException {
        sessionMocker = new SessionMocker();
        clientMocker = new SSHClientMocker().setAuthed(true).setConnected(true)
                .withSession(sessionMocker.mock());
        raspiQuery = new TestingRaspiQuery("host", "user", 22,
                clientMocker.mock());
        raspiQuery.connect("123");
    }
}
