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
