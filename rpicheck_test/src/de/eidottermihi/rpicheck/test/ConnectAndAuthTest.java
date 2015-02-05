package de.eidottermihi.rpicheck.test;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.eidottermihi.rpicheck.ssh.impl.RaspiQuery;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;

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
