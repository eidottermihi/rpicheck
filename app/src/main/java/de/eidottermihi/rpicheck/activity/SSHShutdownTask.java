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
package de.eidottermihi.rpicheck.activity;

import android.os.AsyncTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import de.eidottermihi.rpicheck.activity.helper.Constants;
import de.eidottermihi.rpicheck.beans.ShutdownResult;
import de.eidottermihi.rpicheck.ssh.IQueryService;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQuery;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;

/**
 * @author Michael
 */
public class SSHShutdownTask extends AsyncTask<String, Integer, ShutdownResult> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SSHShutdownTask.class);

    private IQueryService queryService;

    private AsyncShutdownUpdate delegate;

    public SSHShutdownTask(AsyncShutdownUpdate delegate) {
        super();
        this.delegate = delegate;
    }

    /**
     * Send the reboot or halt command.
     */
    @Override
    protected ShutdownResult doInBackground(String... params) {
        queryService = new RaspiQuery((String) params[0], (String) params[1],
                Integer.parseInt(params[3]));
        final String pass = params[2];
        final String sudoPass = params[4];
        final String type = params[5];
        final String keyfile = params[6];
        final String keypass = params[7];
        final ShutdownResult result = new ShutdownResult();
        result.setType(type);
        try {
            if (keyfile != null) {
                File f = new File(keyfile);
                if (keypass == null) {
                    // connect with private key only
                    queryService.connectWithPubKeyAuth(f.getPath());
                } else {
                    // connect with key and passphrase
                    queryService.connectWithPubKeyAuthAndPassphrase(
                            f.getPath(), keypass);
                }
            } else {
                queryService.connect(pass);
            }
            if (type.equals(Constants.TYPE_REBOOT)) {
                queryService.sendRebootSignal(sudoPass);
            } else if (type.equals(Constants.TYPE_HALT)) {
                queryService.sendHaltSignal(sudoPass);
            }
            return result;
        } catch (RaspiQueryException e) {
            LOGGER.error(e.getMessage(), e);
            result.setExcpetion(e);
            return result;
        } finally {
            try {
                queryService.disconnect();
            } catch (RaspiQueryException e) {
                LOGGER.debug("Error closing the ssh client.", e);
            }
        }
    }

    @Override
    protected void onPostExecute(ShutdownResult result) {
        delegate.onShutdownFinished(result);
    }

}