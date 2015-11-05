/**
 * Copyright (C) 2015  RasPi Check Contributors
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

import org.junit.Assert;
import org.junit.Test;

import de.eidottermihi.rpicheck.ssh.beans.RaspiMemoryBean;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;
import de.eidottermihi.rpicheck.test.mocks.CommandMocker;

public class MemoryTest extends AbstractMockedQueryTest {

    @Test
    public void memory() throws RaspiQueryException {
        sessionMocker.withCommand(
                "free | sed -n 2,3p | tr -d '\\n' | sed 's/[[:space:]]\\+/,/g'",
                new CommandMocker().withResponse(
                        "Mem:,949328,586708,362620,0,302444,238064\n" +
                                "-/+,buffers/cache:,46200,903128").mock());
        RaspiMemoryBean memoryBean = raspiQuery.queryMemoryInformation();
        Assert.assertNotNull(memoryBean);
        Assert.assertEquals(949328L * 1000, memoryBean.getTotalMemory()
                .getBytes());
        Assert.assertEquals(903128L * 1000, memoryBean.getTotalFree()
                .getBytes());
        Assert.assertEquals(46200L * 1000, memoryBean.getTotalUsed()
                .getBytes());
        Assert.assertEquals(46200.0 / 949328.0, memoryBean.getPercentageUsed(), 0.001);
    }

    @Test
    public void memory_deutsch() throws RaspiQueryException {
        sessionMocker.withCommand(
                "free | sed -n 2,3p | tr -d '\\n' | sed 's/[[:space:]]\\+/,/g'",
                new CommandMocker().withResponse(
                        "Speicher:,949328,586708,362620,0,302444,238064\n" +
                                "-/+,Buffer/Cache:,46200,903128").mock());
        RaspiMemoryBean memoryBean = raspiQuery.queryMemoryInformation();
        Assert.assertNotNull(memoryBean);
        Assert.assertEquals(949328L * 1000, memoryBean.getTotalMemory()
                .getBytes());
        Assert.assertEquals(903128L * 1000, memoryBean.getTotalFree()
                .getBytes());
        Assert.assertEquals(46200L * 1000, memoryBean.getTotalUsed()
                .getBytes());
        Assert.assertEquals(46200.0 / 949328.0, memoryBean.getPercentageUsed(), 0.001);
    }

}
