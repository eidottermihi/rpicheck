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
package de.eidottermihi.rpicheck.test.mocks;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;

import org.mockito.Mockito;

/**
 * Mocker for {@link SSHClient}
 *
 * @author Michael
 */
public class SSHClientMocker {

    private SSHClient client = Mockito.mock(SSHClient.class);

    public SSHClientMocker setAuthed(boolean isAuthed) {
        Mockito.when(client.isAuthenticated()).thenReturn(isAuthed);
        return this;
    }

    public SSHClientMocker setConnected(boolean isConnected) {
        Mockito.when(client.isConnected()).thenReturn(isConnected);
        return this;
    }

    public SSHClientMocker withSession(Session session) {
        try {
            Mockito.when(client.startSession()).thenReturn(session);
        } catch (ConnectionException e) {
        } catch (TransportException e) {
        }
        return this;
    }

    public SSHClient mock() {
        return this.client;
    }

}
