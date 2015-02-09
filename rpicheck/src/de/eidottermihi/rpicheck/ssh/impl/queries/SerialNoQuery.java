package de.eidottermihi.rpicheck.ssh.impl.queries;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.eidottermihi.rpicheck.ssh.GenericQuery;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;

public class SerialNoQuery extends GenericQuery<String> {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SerialNoQuery.class);
	private static final String CAT_PROC_CPUINFO_GREP_SERIAL = "cat /proc/cpuinfo | grep Serial";
	private static final String N_A = "n/a";

	public SerialNoQuery(SSHClient sshClient) {
		super(sshClient);
	}

	@Override
	public String run() throws RaspiQueryException {
		LOGGER.info("Querying serial number...");
		try {
			Session session = getSSHClient().startSession();
			final Command cmd = session.exec(CAT_PROC_CPUINFO_GREP_SERIAL);
			cmd.join(30, TimeUnit.SECONDS);
			String output = IOUtils.readFully(cmd.getInputStream()).toString();
			return this.formatCpuSerial(output);
		} catch (IOException e) {
			throw RaspiQueryException.createTransportFailure(e);
		}
	}

	private String formatCpuSerial(String output) {
		final String[] split = output.trim().split(":");
		if (split.length >= 2) {
			final String cpuSerial = split[1].trim();
			return cpuSerial;
		} else {
			LOGGER.error(
					"Could not query cpu serial number. Expected another output of '{}'.",
					CAT_PROC_CPUINFO_GREP_SERIAL);
			LOGGER.error("Output of '{}': \n{}", CAT_PROC_CPUINFO_GREP_SERIAL,
					output);
			return N_A;
		}
	}

}
