package de.eidottermihi.rpicheck.ssh;

import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;

public class ConnectionCheckingQuery<R extends Object> implements Queries<R> {

	private GenericQuery<R> delegate;

	public ConnectionCheckingQuery(GenericQuery<R> query) {
		this.delegate = query;
	}

	@Override
	public R run() throws RaspiQueryException {
		if (delegate.getSSHClient() == null) {
			throw new IllegalStateException(
					"You must establish a connection first.");
		}
		if (!delegate.getSSHClient().isConnected()
				|| !delegate.getSSHClient().isAuthenticated()) {
			throw new IllegalStateException(
					"You must establish a connection first.");
		}
		return delegate.run();
	}
}
