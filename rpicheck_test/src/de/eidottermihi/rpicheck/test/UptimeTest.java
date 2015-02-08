package de.eidottermihi.rpicheck.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;
import de.eidottermihi.rpicheck.test.mocks.CommandMocker;

public class UptimeTest extends AbstractMockedQueryTest {

	@Test
	public void uptime() throws RaspiQueryException {
		sessionMocker.withCommand("cat /proc/uptime", new CommandMocker()
				.withResponse("20256.17 80516.80").mock());
		assertEquals(20256.17D, raspiQuery.queryUptime(), 0.0001D);
	}

	@Test
	public void uptime_copyright_header() throws RaspiQueryException {
		sessionMocker
				.withCommand(
						"cat /proc/uptime",
						new CommandMocker()
								.withResponse(
										"Copyright (C) stuff right here\n\n With multiple line breaks\nAnd other weird stuff in a proc file\n20256.17 80516.80")
								.mock());
		assertEquals(20256.17D, raspiQuery.queryUptime(), 0.0001D);
	}

}
