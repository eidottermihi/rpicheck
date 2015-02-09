package de.eidottermihi.rpicheck.ssh;

import net.schmizz.sshj.SSHClient;

public abstract class GenericQuery<R extends Object> implements Queries<R> {

	private SSHClient sshClient;

	public GenericQuery(SSHClient sshClient) {
		this.sshClient = sshClient;
	}

	public SSHClient getSSHClient() {
		return sshClient;
	}

}
