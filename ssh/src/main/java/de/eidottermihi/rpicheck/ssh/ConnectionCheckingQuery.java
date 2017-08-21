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
package de.eidottermihi.rpicheck.ssh;

import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;

public class ConnectionCheckingQuery<R extends Object> implements Queries<R> {

    private GenericQuery<R> delegate;

    public ConnectionCheckingQuery(GenericQuery<R> query) {
        this.delegate = query;
    }

    @Override
    public R run() throws RaspiQueryException {
        if (delegate.getSSHClient() == null) {
            throw new IllegalStateException(
                    "You must establish a connection first.");
        }
        if (!delegate.getSSHClient().isConnected()
                || !delegate.getSSHClient().isAuthenticated()) {
            throw new IllegalStateException(
                    "You must establish a connection first.");
        }
        return delegate.run();
    }
}
