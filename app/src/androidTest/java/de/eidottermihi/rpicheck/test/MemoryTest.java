package de.eidottermihi.rpicheck.test;

import org.junit.Assert;
import org.junit.Test;

import de.eidottermihi.rpicheck.beans.RaspiMemoryBean;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;
import de.eidottermihi.rpicheck.test.mocks.CommandMocker;

public class MemoryTest extends AbstractMockedQueryTest {

	@Test
	public void memory() throws RaspiQueryException {
		sessionMocker.withCommand(
				"free | egrep 'Mem' | sed 's/[[:space:]]\\+/,/g'",
				new CommandMocker().withResponse(
						"Mem:,762420,275136,487284,9864,89188,121464").mock());
		RaspiMemoryBean memoryBean = raspiQuery.queryMemoryInformation();
		Assert.assertNotNull(memoryBean);
		Assert.assertEquals(762420L * 1000, memoryBean.getTotalMemory()
				.getBytes());
		Assert.assertEquals(487284L * 1000, memoryBean.getTotalFree()
				.getBytes());
		Assert.assertEquals(275136L * 1000, memoryBean.getTotalUsed()
				.getBytes());
	}

}
