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
import de.eidottermihi.rpicheck.ssh.LoadAveragePeriod;
import de.eidottermihi.rpicheck.ssh.Queries;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;

/**
 * Queries load average via /proc/loadavg and /proc/stat. /proc/stat is used to
 * determine number of cpu cores.
 *
 * @author Michael
 */
public class LoadAverageQuery extends GenericQuery<Double> implements
        Queries<Double> {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(LoadAverageQuery.class);

    private static final String LOAD_AVG_CMD = "cat /proc/loadavg; cat /proc/stat | grep cpu | wc -l";

    private LoadAveragePeriod period;

    public LoadAverageQuery(SSHClient sshClient, LoadAveragePeriod period) {
        super(sshClient);
        this.period = period;
    }

    @Override
    public Double run() throws RaspiQueryException {
        LOGGER.info("Querying load average for time period {}", this.period);
        Session session;
        try {
            session = getSSHClient().startSession();
            session.allocateDefaultPTY();
            final Command cmd = session.exec(LOAD_AVG_CMD);
            cmd.join(30, TimeUnit.SECONDS);
            cmd.close();
            final String output = IOUtils.readFully(cmd.getInputStream())
                    .toString();
            return this.parseLoadAverage(output, this.period);
        } catch (IOException e) {
            throw RaspiQueryException.createTransportFailure(e);
        }
    }

    private double parseLoadAverage(String output, LoadAveragePeriod timePeriod) {
        String[] lines = output.split("\n");
        Double loadAvg = null;
        for (String line : lines) {
            LOGGER.debug("Checking line: {}", line);
            final String[] split = line.split(" ");
            if (split.length == 5) {
                try {
                    switch (timePeriod) {
                        case ONE_MINUTE:
                            loadAvg = Double.parseDouble(split[0]);
                            break;
                        case FIVE_MINUTES:
                            loadAvg = Double.parseDouble(split[1]);
                            break;
                        case FIFTEEN_MINUTES:
                            loadAvg = Double.parseDouble(split[2]);
                            break;
                        default:
                            throw new RuntimeException("Unknown LoadAveragePeriod!");
                    }
                    // got load average, continue with next line
                    continue;
                } catch (NumberFormatException e) {
                    LOGGER.debug("Skipping line: {}", line);
                }
            }
            if (split.length == 1 && loadAvg != null) {
                // core count line
                try {
                    Integer coreCount = Integer.parseInt(split[0].trim()) - 1;
                    return Math.min(1.0D, loadAvg / coreCount);
                } catch (NumberFormatException e) {
                    LOGGER.debug("Skipping line: {}", line);
                }
            }
            LOGGER.debug("Skipping line: {}", line);
        }
        LOGGER.error("Expected a different output of command: {}", LOAD_AVG_CMD);
        LOGGER.error("Actual output was: {}", output);
        return 0D;
    }

}
