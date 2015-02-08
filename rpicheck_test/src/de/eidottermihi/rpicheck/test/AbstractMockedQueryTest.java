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
