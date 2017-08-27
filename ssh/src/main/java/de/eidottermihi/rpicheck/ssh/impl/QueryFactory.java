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
package de.eidottermihi.rpicheck.ssh.impl;

import net.schmizz.sshj.SSHClient;

import de.eidottermihi.rpicheck.ssh.ConnectionCheckingQuery;
import de.eidottermihi.rpicheck.ssh.LoadAveragePeriod;
import de.eidottermihi.rpicheck.ssh.Queries;
import de.eidottermihi.rpicheck.ssh.beans.RaspiMemoryBean;
import de.eidottermihi.rpicheck.ssh.impl.queries.FirmwareQuery;
import de.eidottermihi.rpicheck.ssh.impl.queries.LoadAverageQuery;
import de.eidottermihi.rpicheck.ssh.impl.queries.MemoryQuery;
import de.eidottermihi.rpicheck.ssh.impl.queries.SerialNoQuery;
import de.eidottermihi.rpicheck.ssh.impl.queries.SystemtimeQuery;
import de.eidottermihi.rpicheck.ssh.impl.queries.UptimeQuery;

public final class QueryFactory {

    public static final Queries<String> makeSerialNoQuery(SSHClient client) {
        return new ConnectionCheckingQuery<>(new SerialNoQuery(client));
    }

    public static final Queries<Double> makeUptimeQuery(SSHClient client) {
        return new ConnectionCheckingQuery<>(new UptimeQuery(client));
    }

    public static final Queries<Double> makeLoadAvgQuery(SSHClient client,
                                                         LoadAveragePeriod period) {
        return new ConnectionCheckingQuery<>(new LoadAverageQuery(client,
                period));
    }

    public static final Queries<RaspiMemoryBean> makeMemoryQuery(SSHClient client) {
        return new ConnectionCheckingQuery<>(new MemoryQuery(client));
    }

    public static final Queries<String> makeFirmwareQuery(SSHClient client, String vcgencmdPath) {
        return new ConnectionCheckingQuery<>(new FirmwareQuery(client, vcgencmdPath));
    }

    public static final Queries<String> makeSystemTimeQuery(SSHClient client) {
        return new ConnectionCheckingQuery<>(new SystemtimeQuery(client));
    }
}
