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
import net.schmizz.sshj.connection.channel.direct.Session.Command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.eidottermihi.rpicheck.ssh.GenericQuery;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;

public class SerialNoQuery extends GenericQuery<String> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SerialNoQuery.class);
    private static final String CAT_PROC_CPUINFO_GREP_SERIAL = "cat /proc/cpuinfo | grep Serial";
    private static final String N_A = "n/a";

    public SerialNoQuery(SSHClient sshClient) {
        super(sshClient);
    }

    @Override
    public String run() throws RaspiQueryException {
        LOGGER.info("Querying serial number...");
        try {
            Session session = getSSHClient().startSession();
            final Command cmd = session.exec(CAT_PROC_CPUINFO_GREP_SERIAL);
            cmd.join(30, TimeUnit.SECONDS);
            String output = IOUtils.readFully(cmd.getInputStream()).toString();
            return this.formatCpuSerial(output);
        } catch (IOException e) {
            throw RaspiQueryException.createTransportFailure(e);
        }
    }

    private String formatCpuSerial(String output) {
        final String[] split = output.trim().split(":");
        if (split.length >= 2) {
            final String cpuSerial = split[1].trim();
            return cpuSerial;
        } else {
            LOGGER.error(
                    "Could not query cpu serial number. Expected another output of '{}'.",
                    CAT_PROC_CPUINFO_GREP_SERIAL);
            LOGGER.error("Output of '{}': \n{}", CAT_PROC_CPUINFO_GREP_SERIAL,
                    output);
            return N_A;
        }
    }

}
