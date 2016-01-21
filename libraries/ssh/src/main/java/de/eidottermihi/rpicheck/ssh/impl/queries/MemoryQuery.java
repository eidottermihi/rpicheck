/**
 * Copyright (C) 2016  RasPi Check Contributors
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
import java.util.concurrent.TimeUnit;

import de.eidottermihi.rpicheck.ssh.GenericQuery;
import de.eidottermihi.rpicheck.ssh.beans.RaspiMemoryBean;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;

public class MemoryQuery extends GenericQuery<RaspiMemoryBean> {

    private static final String MEMORY_INFO_CMD = "free | sed -n 2,3p | tr -d '\\n' | sed 's/[[:space:]]\\+/,/g'";
    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryQuery.class);

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
            return this.formatMemoryInfo(IOUtils.readFully(
                    cmd.getInputStream()).toString());
        } catch (IOException e) {
            throw RaspiQueryException.createTransportFailure(e);
        }
    }

    private RaspiMemoryBean formatMemoryInfo(String output) {
        final String[] split = output.split(",");
        if (split.length >= 3) {
            final long total = Long.parseLong(split[1]);
            final long used = Long.parseLong(split[8]);
            return new RaspiMemoryBean(total, used);
        } else {
            LOGGER.error("Expected a different output of command: {}",
                    MEMORY_INFO_CMD);
            LOGGER.error("Output was : {}", output);
            return new RaspiMemoryBean(
                    "Memory information could not be queried. See the log for details.");
        }
    }
}
