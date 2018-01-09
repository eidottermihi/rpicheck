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
