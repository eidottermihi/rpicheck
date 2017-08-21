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
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;

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
public class FirmwareQuery extends GenericQuery<String> implements Queries<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FirmwareQuery.class);

    private static final int SHORTENED_HASH_LENGTH = 8;
    private static final String BLANK = " ";

    private String vcgencmdPath;

    /**
     * @param sshClient    an SSHClient
     * @param vcgencmdPath path to 'vcgencmd'
     */
    public FirmwareQuery(SSHClient sshClient, String vcgencmdPath) {
        super(sshClient);
        this.vcgencmdPath = vcgencmdPath;
    }

    @Override
    public String run() throws RaspiQueryException {
        LOGGER.debug("Querying firmware version, vcgencmd path={}", this.vcgencmdPath);
        try {
            Session session = getSSHClient().startSession();
            String cmdString = vcgencmdPath + " version";
            final Session.Command cmd = session.exec(cmdString);
            cmd.join(30, TimeUnit.SECONDS);
            String output = IOUtils.readFully(cmd.getInputStream())
                    .toString();
            final String result = this.parseFirmwareVersion(output);
            LOGGER.debug("Firmware version: {}", result);
            return result;
        } catch (IOException e) {
            throw RaspiQueryException.createTransportFailure(e);
        }
    }

    /**
     * Parses the output of "vcgendcmd version".
     *
     * @param output the output
     * @return the version string
     */
    private String parseFirmwareVersion(final String output) {
        final String[] splitted = output.split("\n");
        if (splitted.length >= 3) {
            if (splitted[2].startsWith("version ")) {
                return checkAndFormatVersionHash(splitted[2].replace("version ", ""));
                //return splitted[2].replace("version ", "");
            } else {
                return splitted[2];
            }
        } else {
            LOGGER.error("Could not parse firmware. Maybe the output of 'vcgencmd version' changed.");
            LOGGER.debug("Output of 'vcgencmd version': \n{}", output);
            return "n/a";
        }
    }

    private String checkAndFormatVersionHash(String versionString) {
        StringBuilder sb = new StringBuilder();
        String[] splitted = versionString.split(BLANK);
        if (splitted.length == 3) {
            String hash = splitted[0];
            if (hash.length() > SHORTENED_HASH_LENGTH) {
                sb.append(hash.substring(0, SHORTENED_HASH_LENGTH));
            } else {
                sb.append(hash);
            }
            return sb.append(BLANK).append(splitted[1]).append(BLANK).append(splitted[2]).toString();
        }
        return versionString;
    }
}
