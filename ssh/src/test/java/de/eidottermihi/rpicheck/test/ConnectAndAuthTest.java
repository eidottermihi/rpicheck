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

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.eidottermihi.rpicheck.ssh.impl.RaspiQuery;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;
import de.eidottermihi.rpicheck.test.mocks.TestingRaspiQuery;

public class ConnectAndAuthTest {

    private RaspiQuery raspiQuery;
    private SSHClient sshClient;

    @Before
    public void init() {
        sshClient = Mockito.mock(SSHClient.class);
        raspiQuery = new TestingRaspiQuery("localhost", "admin", 22, sshClient);
    }

    @Test
    public void connect_ok() throws RaspiQueryException {
        raspiQuery.connect("123");
    }

    @Test(expected = RaspiQueryException.class)
    public void connect_auth_failure() throws RaspiQueryException,
            UserAuthException, TransportException {
        Mockito.doThrow(UserAuthException.class).when(sshClient)
                .authPassword(Mockito.anyString(), Mockito.anyString());
        raspiQuery.connect("wrong_pw");
    }

    @Test(expected = IllegalStateException.class)
    public void query_no_connection() throws RaspiQueryException {
        Mockito.when(sshClient.isConnected()).thenReturn(false);
        raspiQuery.queryVcgencmd();
    }

    @Test(expected = IllegalStateException.class)
    public void query_no_auth() throws RaspiQueryException {
        Mockito.when(sshClient.isConnected()).thenReturn(true);
        Mockito.when(sshClient.isAuthenticated()).thenReturn(false);
        raspiQuery.queryVcgencmd();
    }

}
