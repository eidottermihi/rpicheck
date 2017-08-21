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
package de.eidottermihi.rpicheck.test.mocks;

import net.schmizz.sshj.SSHClient;

import de.eidottermihi.rpicheck.ssh.impl.RaspiQuery;

/**
 * Testing subclass (allows to specify SSHClient in use)
 *
 * @author Michael
 */
public class TestingRaspiQuery extends RaspiQuery {

    private SSHClient testingSSHClient;

    public TestingRaspiQuery(String host, String user, Integer port,
                             SSHClient testingSSHClient) {
        super(host, user, port);
        this.testingSSHClient = testingSSHClient;
    }

    @Override
    public SSHClient newAndroidSSHClient() {
        return testingSSHClient;
    }

}