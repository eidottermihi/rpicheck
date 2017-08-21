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
package de.eidottermihi.rpicheck.ssh.impl.queries;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.eidottermihi.rpicheck.ssh.GenericQuery;
import de.eidottermihi.rpicheck.ssh.beans.RaspiMemoryBean;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;

public class MemoryQuery extends GenericQuery<RaspiMemoryBean> {

    public static final String MEMORY_INFO_CMD = "cat /proc/meminfo | tr -s \" \"";
    public static final String MEMORY_UNKNOWN_OUPUT = "Memory information could not be queried. See the log for details.";
    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryQuery.class);
    private static final String KEY_TOTAL = "MemTotal:";
    private static final String KEY_AVAILABLE = "MemAvailable:";
    private static final String KEY_FREE = "MemFree:";
    private static final String KEY_BUFFERS = "Buffers:";
    private static final String KEY_CACHED = "Cached:";

    public MemoryQuery(SSHClient sshClient) {
        super(sshClient);
    }

    @Override
    public RaspiMemoryBean run() throws RaspiQueryException {
        LOGGER.info("Querying memory information...");
        try {
            Session session = getSSHClient().startSession();
            final Session.Command cmd = session.exec(MEMORY_INFO_CMD);
            cmd.join(30, TimeUnit.SECONDS);
            return this.formatMemoryInfo(IOUtils.readFully(cmd.getInputStream()).toString());
        } catch (IOException e) {
            throw RaspiQueryException.createTransportFailure(e);
        }
    }

    private RaspiMemoryBean formatMemoryInfo(String output) {
        final Map<String, Long> memoryData = new HashMap<String, Long>();
        String[] lines = output.split("[\r\n]+");
        for (String line : lines) {
            String[] paragraphs = line.split(" ");
            if (paragraphs.length > 1) {
                memoryData.put(paragraphs[0], Long.valueOf(paragraphs[1]));
            }
        }
        Long memTotal = memoryData.get(KEY_TOTAL);
        if (memTotal != null) {
            Long memAvailable = memoryData.get(KEY_AVAILABLE);
            if (memAvailable != null) {
                LOGGER.debug("Using MemAvailable for calculation of free memory.");
                return new RaspiMemoryBean(memTotal, memTotal - memAvailable);
            }
            // maybe Linux Kernel < 3.14
            // estimate "used": MemTotal - (MemFree + Buffers + Cached)
            // thats also how 'free' is doing it
            Long memFree = memoryData.get(KEY_FREE);
            Long memCached = memoryData.get(KEY_CACHED);
            Long memBuffers = memoryData.get(KEY_BUFFERS);
            if (memFree != null && memCached != null && memBuffers != null) {
                Long memUsed = memTotal - (memFree + memBuffers + memCached);
                LOGGER.debug("Using MemFree,Buffers and Cached for calculation of free memory.");
                return new RaspiMemoryBean(memTotal, memUsed);
            } else {
                return produceError(output);
            }
        } else {
            return produceError(output);
        }
    }

    private RaspiMemoryBean produceError(String output) {
        LOGGER.error("Expected a different output of command: {}", MEMORY_INFO_CMD);
        LOGGER.error("Output was : {}", output);
        return new RaspiMemoryBean(MEMORY_UNKNOWN_OUPUT);
    }
}
