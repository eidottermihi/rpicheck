/**
 * MIT License
 *
 * Copyright (c) 2018  RasPi Check Contributors
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
import de.eidottermihi.rpicheck.ssh.beans.MemoryBean;
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
    private static final String SWAP_TOTAL = "SwapTotal:";
    private static final String SWAP_FREE = "SwapFree:";

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
        final Map<String, Long> memoryData = parseData(output);

        // memory
        Long memTotal = memoryData.get(KEY_TOTAL);
        Long memUsed = null;
        String error = null;
        if (memTotal != null) {
            Long memAvailable = memoryData.get(KEY_AVAILABLE);
            if (memAvailable != null) {
                LOGGER.debug("Using MemAvailable for calculation of free memory.");
                memUsed = memTotal - memAvailable;
            } else {
                // maybe Linux Kernel < 3.14
                // estimate "used": MemTotal - (MemFree + Buffers + Cached)
                // thats also how 'free' is doing it
                Long memFree = memoryData.get(KEY_FREE);
                Long memCached = memoryData.get(KEY_CACHED);
                Long memBuffers = memoryData.get(KEY_BUFFERS);
                if (memFree != null && memCached != null && memBuffers != null) {
                    memUsed = memTotal - (memFree + memBuffers + memCached);
                    LOGGER.debug("Using MemFree,Buffers and Cached for calculation of free memory.");
                } else {
                    error = produceError(output);
                }
            }
        } else {
            error = produceError(output);
        }
        // swap
        Long swapTotal = memoryData.get(SWAP_TOTAL);
        Long swapUsed = null;
        if(swapTotal != null){
            Long swapFree = memoryData.get(SWAP_FREE);
            if(swapFree != null){
                swapUsed = swapTotal - swapFree;
            } else {
                error = produceError(MEMORY_UNKNOWN_OUPUT);
            }
        } else {
            error = produceError(MEMORY_UNKNOWN_OUPUT);
        }
        if(error == null) {
            return new RaspiMemoryBean(memTotal, memUsed, swapTotal, swapUsed);
        } else {
            return new RaspiMemoryBean(error);
        }

    }

    private Map<String, Long> parseData(String output) {
        final Map<String, Long> memoryData = new HashMap<String, Long>();
        String[] lines = output.split("[\r\n]+");
        for (String line : lines) {
            String[] paragraphs = line.split(" ");
            if (paragraphs.length > 1) {
                memoryData.put(paragraphs[0], Long.valueOf(paragraphs[1]));
            }
        }
        return memoryData;
    }

    private String produceError(String output) {
        LOGGER.error("Expected a different output of command: {}", MEMORY_INFO_CMD);
        LOGGER.error("Output was : {}", output);
        return MEMORY_UNKNOWN_OUPUT;
    }
}
