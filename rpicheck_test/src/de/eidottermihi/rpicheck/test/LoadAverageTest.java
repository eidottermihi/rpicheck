package de.eidottermihi.rpicheck.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import de.eidottermihi.rpicheck.ssh.LoadAveragePeriod;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;
import de.eidottermihi.rpicheck.test.mocks.CommandMocker;

public class LoadAverageTest extends AbstractMockedQueryTest {

	@Test
	public void load_avg() throws IOException, RaspiQueryException {
		String output = FileUtils.readFileToString(FileUtils
				.getFile("src/de/eidottermihi/rpicheck/test/proc_loadavg.txt"));
		sessionMocker.withCommand("cat /proc/loadavg", new CommandMocker()
				.withResponse(output).mock());
		double queryLoadAverage = raspiQuery
				.queryLoadAverage(LoadAveragePeriod.FIVE_MINUTES);
		assertEquals(0.58D, queryLoadAverage, 0.001D);
	}

	@Test
	public void load_avg_fifteen_minutes() throws IOException,
			RaspiQueryException {
		String output = FileUtils.readFileToString(FileUtils
				.getFile("src/de/eidottermihi/rpicheck/test/proc_loadavg.txt"));
		sessionMocker.withCommand("cat /proc/loadavg", new CommandMocker()
				.withResponse(output).mock());
		double queryLoadAverage = raspiQuery
				.queryLoadAverage(LoadAveragePeriod.FIFTEEN_MINUTES);
		assertEquals(0.53D, queryLoadAverage, 0.001D);
	}

	@Test
	public void load_avg_copyright_header() throws IOException,
			RaspiQueryException {
		String output = FileUtils
				.readFileToString(FileUtils
						.getFile("src/de/eidottermihi/rpicheck/test/proc_loadavg_with_copyright.txt"));
		sessionMocker.withCommand("cat /proc/loadavg", new CommandMocker()
				.withResponse(output).mock());
		double queryLoadAverage = raspiQuery
				.queryLoadAverage(LoadAveragePeriod.FIVE_MINUTES);
		assertEquals(0.58D, queryLoadAverage, 0.001D);
	}

}
