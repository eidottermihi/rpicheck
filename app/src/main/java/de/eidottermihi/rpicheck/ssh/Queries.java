package de.eidottermihi.rpicheck.ssh;

import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;

public interface Queries<R> {
	/**
	 * @return result of the Query
	 * @throws RaspiQueryException
	 *             when the query failed
	 */
	R run() throws RaspiQueryException;
}
