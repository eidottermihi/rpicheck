/**
 * Copyright (C) 2017  RasPi Check Contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package de.eidottermihi.rpicheck.test;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import de.eidottermihi.rpicheck.ssh.beans.RaspiMemoryBean;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;
import de.eidottermihi.rpicheck.ssh.impl.queries.MemoryQuery;
import de.eidottermihi.rpicheck.test.mocks.CommandMocker;

public class MemoryTest extends AbstractMockedQueryTest {

    @Test
    public void memory() throws RaspiQueryException, IOException {
        String output = FileUtils.readFileToString(FileUtils
                .getFile("src/test/java/de/eidottermihi/rpicheck/test/proc_meminfo.txt"));
        sessionMocker.withCommand(
                MemoryQuery.MEMORY_INFO_CMD,
                new CommandMocker().withResponse(output).mock());
        RaspiMemoryBean memoryBean = raspiQuery.queryMemoryInformation();
        Assert.assertNotNull(memoryBean);
        Assert.assertEquals(949328L * 1024, memoryBean.getTotalMemory()
                .getBytes());
        Assert.assertEquals(884628L * 1024, memoryBean.getTotalFree()
                .getBytes());
        Assert.assertEquals(64700L * 1024, memoryBean.getTotalUsed()
                .getBytes());
        Assert.assertEquals(64700.0 / 949328.0, memoryBean.getPercentageUsed(), 0.001);
    }

    @Test
    public void memory_old_linux_kernel() throws RaspiQueryException, IOException {
        String output = FileUtils.readFileToString(FileUtils
                .getFile("src/test/java/de/eidottermihi/rpicheck/test/proc_meminfo_old.txt"));
        sessionMocker.withCommand(
                MemoryQuery.MEMORY_INFO_CMD,
                new CommandMocker().withResponse(output).mock());
        RaspiMemoryBean memoryBean = raspiQuery.queryMemoryInformation();
        Assert.assertNotNull(memoryBean);
        Assert.assertEquals(949328L * 1024, memoryBean.getTotalMemory()
                .getBytes());
        Assert.assertEquals(898024L * 1024, memoryBean.getTotalFree()
                .getBytes());
        Assert.assertEquals(51304L * 1024, memoryBean.getTotalUsed()
                .getBytes());
        Assert.assertEquals(51304.0 / 949328.0, memoryBean.getPercentageUsed(), 0.001);
    }

    @Test
    public void memory_unknown_ouput() throws RaspiQueryException, IOException {
        String output = FileUtils.readFileToString(FileUtils
                .getFile("src/test/java/de/eidottermihi/rpicheck/test/proc_meminfo_error.txt"));
        sessionMocker.withCommand(
                MemoryQuery.MEMORY_INFO_CMD,
                new CommandMocker().withResponse(output).mock());
        RaspiMemoryBean memoryBean = raspiQuery.queryMemoryInformation();
        Assert.assertNotNull(memoryBean);
        Assert.assertEquals(memoryBean.getErrorMessage(), MemoryQuery.MEMORY_UNKNOWN_OUPUT);
    }

}
