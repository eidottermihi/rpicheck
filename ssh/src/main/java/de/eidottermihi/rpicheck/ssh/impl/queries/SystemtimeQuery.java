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
import java.util.concurrent.TimeUnit;

import de.eidottermihi.rpicheck.ssh.GenericQuery;
import de.eidottermihi.rpicheck.ssh.Queries;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;

/**
 * @author Michael
 */
public class SystemtimeQuery extends GenericQuery<String> implements Queries<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FirmwareQuery.class);

    public SystemtimeQuery(SSHClient sshClient) {
        super(sshClient);
    }


    @Override
    public String run() throws RaspiQueryException {
        LOGGER.debug("Querying system time via 'date --rfc-2822'.");
        try {
            Session session = getSSHClient().startSession();
            String cmdString = "date --rfc-2822";
            final Session.Command cmd = session.exec(cmdString);
            cmd.join(30, TimeUnit.SECONDS);
            String output = IOUtils.readFully(cmd.getInputStream())
                    .toString();
            final String result = output.trim();
            LOGGER.debug("System time: {}", result);
            return result;
        } catch (IOException e) {
            throw RaspiQueryException.createTransportFailure(e);
        }
    }
}
