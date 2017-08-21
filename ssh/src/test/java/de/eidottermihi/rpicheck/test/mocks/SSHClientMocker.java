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
