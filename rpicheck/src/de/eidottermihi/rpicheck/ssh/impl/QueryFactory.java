package de.eidottermihi.rpicheck.ssh.impl;

import net.schmizz.sshj.SSHClient;
import de.eidottermihi.rpicheck.ssh.ConnectionCheckingQuery;
import de.eidottermihi.rpicheck.ssh.Queries;
import de.eidottermihi.rpicheck.ssh.impl.queries.SerialNoQuery;
import de.eidottermihi.rpicheck.ssh.impl.queries.UptimeQuery;

public final class QueryFactory {

	public static final Queries<String> makeSerialNoQuery(SSHClient client) {
		return new ConnectionCheckingQuery<>(new SerialNoQuery(client));
	}

	public static final Queries<Double> makeUptimeQuery(SSHClient client) {
		return new ConnectionCheckingQuery<>(new UptimeQuery(client));
	}
}
