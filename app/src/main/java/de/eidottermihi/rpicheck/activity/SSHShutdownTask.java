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