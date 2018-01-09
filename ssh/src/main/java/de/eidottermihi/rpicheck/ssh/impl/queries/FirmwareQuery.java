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
