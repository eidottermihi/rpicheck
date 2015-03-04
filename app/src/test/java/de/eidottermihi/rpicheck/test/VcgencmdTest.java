package de.eidottermihi.rpicheck.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.transport.TransportException;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import de.eidottermihi.rpicheck.beans.VcgencmdBean;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;
import de.eidottermihi.rpicheck.test.mocks.CommandMocker;

public class VcgencmdTest extends AbstractMockedQueryTest {

	@Test
	public void vcgencmd_in_path() throws RaspiQueryException,
			ConnectionException, TransportException {
		String vcgencmdPath = "vcgencmd";
		sessionMocker.withCommand(vcgencmdPath, new CommandMocker()
				.withResponse("vcgencmd version 1.2.3.4").mock());
		sessionMocker.withCommand(vcgencmdPath + " measure_clock arm",
				new CommandMocker().withResponse("frequency(45)=840090000")
						.mock());
		sessionMocker.withCommand(vcgencmdPath + " measure_clock core",
				new CommandMocker().withResponse("frequency(1)=320000000")
						.mock());
		sessionMocker.withCommand(vcgencmdPath + " measure_volts core",
				new CommandMocker().withResponse("volt=1.200V").mock());
		sessionMocker.withCommand(vcgencmdPath + " measure_temp",
				new CommandMocker().withResponse("temp=41.2'C").mock());
		sessionMocker
				.withCommand(
						vcgencmdPath + " version",
						new CommandMocker()
								.withResponse(
										"Dec 29 2014 14:23:10\nCopyright (c) 2012 Broadcom\nversion d3c15a3b57203798ff811c40ea65174834267d48 (clean) (release)")
								.mock());
		VcgencmdBean vcgencmd = raspiQuery.queryVcgencmd();
		assertNotNull(vcgencmd);
		assertThat(vcgencmd.getArmFrequency(), CoreMatchers.is(840090000L));
		assertThat(vcgencmd.getCoreFrequency(), CoreMatchers.is(320000000L));
		assertEquals(1.200, vcgencmd.getCoreVolts(), 0.00001);
		assertEquals(41.2, vcgencmd.getCpuTemperature(), 0.00001);
	}

}
