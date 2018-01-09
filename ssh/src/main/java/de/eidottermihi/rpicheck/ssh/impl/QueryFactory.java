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
package de.eidottermihi.rpicheck.ssh.impl;

import net.schmizz.sshj.SSHClient;

import java.util.List;

import de.eidottermihi.rpicheck.ssh.ConnectionCheckingQuery;
import de.eidottermihi.rpicheck.ssh.LoadAveragePeriod;
import de.eidottermihi.rpicheck.ssh.Queries;
import de.eidottermihi.rpicheck.ssh.beans.NetworkInterfaceInformation;
import de.eidottermihi.rpicheck.ssh.beans.RaspiMemoryBean;
import de.eidottermihi.rpicheck.ssh.impl.queries.FirmwareQuery;
import de.eidottermihi.rpicheck.ssh.impl.queries.LoadAverageQuery;
import de.eidottermihi.rpicheck.ssh.impl.queries.MemoryQuery;
import de.eidottermihi.rpicheck.ssh.impl.queries.NetworkInformationQuery;
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

    public static final Queries<List<NetworkInterfaceInformation>> makeNetworkInformationQuery(SSHClient client) {
        return new ConnectionCheckingQuery<>(new NetworkInformationQuery(client));
    }
}
