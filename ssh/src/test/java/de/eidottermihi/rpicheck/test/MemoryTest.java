/**
 * MIT License
 *
 * Copyright (c) 2019  RasPi Check Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
        Assert.assertEquals(64700.0 / 949328.0, memoryBean.getMemoryPercentageUsed(), 0.001);
        Assert.assertEquals(102396.0 * 1024, memoryBean.getSwapMemory().getBytes(), 0.001);
        Assert.assertEquals(102392.0 * 1024, memoryBean.getSwapFree().getBytes(), 0.001);
        Assert.assertEquals(4.0 * 1024, memoryBean.getSwapUsed().getBytes(), 0.001);
        Assert.assertEquals(4.0 / 102396.0, memoryBean.getSwapPercentageUsed(), 0.001 );
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
        Assert.assertEquals(51304.0 / 949328.0, memoryBean.getMemoryPercentageUsed(), 0.001);
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
