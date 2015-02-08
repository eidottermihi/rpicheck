package de.eidottermihi.rpicheck.test.mocks;

import net.schmizz.sshj.SSHClient;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQuery;

/**
 * Testing subclass (allows to specify SSHClient in use)
 * 
 * @author Michael
 *
 */
public class TestingRaspiQuery extends RaspiQuery {

	private SSHClient testingSSHClient;

	public TestingRaspiQuery(String host, String user, Integer port,
			SSHClient testingSSHClient) {
		super(host, user, port);
		this.testingSSHClient = testingSSHClient;
	}

	@Override
	public SSHClient newAndroidSSHClient() {
		return testingSSHClient;
	}

}