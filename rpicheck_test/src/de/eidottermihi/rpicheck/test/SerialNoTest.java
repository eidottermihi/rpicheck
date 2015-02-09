package de.eidottermihi.rpicheck.test;

import org.junit.Assert;
import org.junit.Test;

import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;
import de.eidottermihi.rpicheck.test.mocks.CommandMocker;

public class SerialNoTest extends AbstractMockedQueryTest {
	@Test
	public void serial() throws RaspiQueryException {
		sessionMocker.withCommand(
				"cat /proc/cpuinfo | grep Serial",
				new CommandMocker().withResponse(
						"Serial          : 0000000041e5647d").mock());
		Assert.assertEquals("0000000041e5647d", raspiQuery.queryCpuSerial());
	}

	@Test
	public void serial_crap() throws RaspiQueryException {
		sessionMocker.withCommand("cat /proc/cpuinfo | grep Serial",
				new CommandMocker().withResponse("No valid entry").mock());
		Assert.assertEquals("n/a", raspiQuery.queryCpuSerial());
	}
}
