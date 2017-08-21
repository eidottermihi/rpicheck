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

import com.google.common.base.Splitter;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.eidottermihi.rpicheck.ssh.GenericQuery;
import de.eidottermihi.rpicheck.ssh.Queries;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;

public class UptimeQuery extends GenericQuery<Double> implements
        Queries<Double> {

    public UptimeQuery(SSHClient sshClient) {
        super(sshClient);
    }

    private static final String UPTIME_CMD = "cat /proc/uptime";
    private static final Logger LOGGER = LoggerFactory
            .getLogger(UptimeQuery.class);

    @Override
    public Double run() throws RaspiQueryException {
        LOGGER.info("Querying uptime...");
        try {
            final Session session = getSSHClient().startSession();
            final Command cmd = session.exec(UPTIME_CMD);
            cmd.join(30, TimeUnit.SECONDS);
            final String output = IOUtils.readFully(cmd.getInputStream())
                    .toString();
            return this.formatUptime(output);
        } catch (IOException e) {
            throw RaspiQueryException.createTransportFailure(e);
        }
    }

    private Double formatUptime(String output) {
        final Iterable<String> lines = Splitter.on("\n").split(output);
        for (String line : lines) {
            List<String> split = Splitter.on(" ").splitToList(line);
            if (split.size() == 2) {
                try {
                    return Double.parseDouble(split.get(0));
                } catch (NumberFormatException e) {
                    LOGGER.debug("Skipping line: {}", line);
                }
            } else {
                LOGGER.debug("Skipping line: {}", line);
            }
        }
        LOGGER.error("Expected a different output of command: {}", UPTIME_CMD);
        LOGGER.error("Actual output was: {}", output);
        return 0D;
    }

}
