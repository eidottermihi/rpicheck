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
 *
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
