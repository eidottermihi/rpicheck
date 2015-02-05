package de.eidottermihi.rpicheck.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.TransportException;

import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.eidottermihi.rpicheck.beans.VcgencmdBean;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQuery;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;

public class VcgencmdTest {

	private RaspiQuery raspiQuery;
	private SSHClient sshClient;
	private Session mockedSession;

	@Before
	public void init() throws RaspiQueryException, ConnectionException,
			TransportException {
		sshClient = Mockito.mock(SSHClient.class);
		Mockito.when(sshClient.isConnected()).thenReturn(true);
		Mockito.when(sshClient.isAuthenticated()).thenReturn(true);
		mockedSession = Mockito.mock(Session.class);
		Mockito.when(sshClient.startSession()).thenReturn(mockedSession);
		raspiQuery = new TestingRaspiQuery("localhost", "admin", 22, sshClient);
		raspiQuery.connect("123");
	}

	@Test
	public void vcgencmd_in_path() throws RaspiQueryException,
			ConnectionException, TransportException {
		String vcgencmdPath = "vcgencmd";
		mockCommand(mockedSession, vcgencmdPath, "vcgencmd version 1.2.3.4");
		mockCommand(mockedSession, vcgencmdPath + " measure_clock arm",
				"frequency(45)=840090000");
		mockCommand(mockedSession, vcgencmdPath + " measure_clock core",
				"frequency(1)=320000000");
		mockCommand(mockedSession, vcgencmdPath + " measure_volts core",
				"volt=1.200V");
		mockCommand(mockedSession, vcgencmdPath + " measure_temp",
				"temp=41.2'C");
		mockCommand(
				mockedSession,
				vcgencmdPath + " version",
				"Dec 29 2014 14:23:10\nCopyright (c) 2012 Broadcom\nversion d3c15a3b57203798ff811c40ea65174834267d48 (clean) (release)");
		VcgencmdBean vcgencmd = raspiQuery.queryVcgencmd();
		assertNotNull(vcgencmd);
		assertThat(vcgencmd.getArmFrequency(), CoreMatchers.is(840090000L));
		assertThat(vcgencmd.getCoreFrequency(), CoreMatchers.is(320000000L));
		assertEquals(1.200, vcgencmd.getCoreVolts(), 0.00001);
		assertEquals(41.2, vcgencmd.getCpuTemperature(), 0.00001);
	}

	private void mockCommand(Session session, String cmd,
			String inputStreamContent) throws ConnectionException,
			TransportException {
		Command mockedCmd = Mockito.mock(Command.class);
		Mockito.when(session.exec(Mockito.eq(cmd))).thenReturn(mockedCmd);
		Mockito.when(mockedCmd.getInputStream()).thenReturn(
				IOUtils.toInputStream(inputStreamContent));
	}
}
