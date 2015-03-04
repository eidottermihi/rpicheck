package de.eidottermihi.rpicheck.test.mocks;

import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.TransportException;

import org.mockito.Mockito;

/**
 * Mocker for {@link Session}
 * 
 * @author Michael
 *
 */
public class SessionMocker {
	private Session session = Mockito.mock(Session.class);

	public SessionMocker withCommand(String cmdString, Command command) {
		try {
			Mockito.when(session.exec(Mockito.eq(cmdString))).thenReturn(
					command);
		} catch (ConnectionException e) {
		} catch (TransportException e) {
		}
		return this;
	}

	public Session mock() {
		return this.session;
	}
}
