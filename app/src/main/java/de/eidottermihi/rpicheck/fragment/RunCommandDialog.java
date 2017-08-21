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
package de.eidottermihi.rpicheck.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import de.eidottermihi.raspicheck.R;
import de.eidottermihi.rpicheck.db.CommandBean;
import de.eidottermihi.rpicheck.db.RaspberryDeviceBean;
import de.eidottermihi.rpicheck.ssh.IQueryService;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQuery;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;

/**
 * @author Michael
 */
public class RunCommandDialog extends DialogFragment {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunCommandDialog.class);

    private boolean didRun = false;

    RaspberryDeviceBean device;
    CommandBean command;
    String passphrase;
    static TextView consoleOutput;

    // Need handler for callbacks to the UI thread
    final Handler mHandler = new Handler();

    // Create runnable for posting
    final Runnable mRunFinished = new Runnable() {
        public void run() {
            // gets called from AsyncTask when task is finished
        }
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity());
        this.device = (RaspberryDeviceBean) this.getArguments()
                .getSerializable("pi");
        this.command = (CommandBean) this.getArguments().getSerializable("cmd");
        if (this.getArguments().getString("passphrase") != null) {
            this.passphrase = this.getArguments().getString("passphrase");
        }

        builder.setTitle(getString(R.string.run_cmd_dialog_title,
                this.command.getName()));

        // fetching the theme-dependent icon
        TypedValue icon = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(R.attr.ic_dialog_run,
                icon, true)) {
            builder.setIcon(icon.resourceId);
        }

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // just closing the dialog
            }
        });
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_command_run, null);
        builder.setView(view);
        consoleOutput = (TextView) view.findViewById(R.id.runCommandOutput);
        consoleOutput.setMovementMethod(new ScrollingMovementMethod());
        if (savedInstanceState != null) {
            this.didRun = savedInstanceState.getBoolean("didRun", false);
        }
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.didRun == false) {
            // run command
            this.runCommand();
            this.didRun = true;
        }
    }

    private void runCommand() {
        consoleOutput
                .setText("Connecting to host " + device.getHost() + " ...");
        // get connection settings from shared preferences
        final String host = device.getHost();
        final String user = device.getUser();
        final String port = device.getPort() + "";
        final String sudoPass = device.getSudoPass();
        if (device.usesAuthentificationMethod(RaspberryDeviceBean.AUTH_PASSWORD)) {
            // ssh password
            putLine("Authenticating with password ...");
            final String pass = device.getPass();
            new SSHCommandTask().execute(host, user, pass, port, sudoPass,
                    null, null, command.getCommand());
        } else if (device.usesAuthentificationMethod(RaspberryDeviceBean.AUTH_PUBLIC_KEY)) {
            putLine("Authenticating with private key ...");
            // keyfile
            final String keyfilePath = device.getKeyfilePath();
            if (keyfilePath != null) {
                final File privateKey = new File(keyfilePath);
                if (privateKey.exists()) {
                    new SSHCommandTask().execute(host, user, null, port,
                            sudoPass, keyfilePath, null, command.getCommand());
                } else {
                    putLine("ERROR - No keyfile was found on path " + keyfilePath);
                }
            } else {
                putLine("ERROR - No keyfile was specified.");
            }
        } else if (device.usesAuthentificationMethod(RaspberryDeviceBean.AUTH_PUBLIC_KEY_WITH_PASSWORD)) {
            putLine("Authenticating with private key and passphrase ...");
            // keyfile and passphrase
            final String keyfilePath = device.getKeyfilePath();
            if (keyfilePath != null) {
                final File privateKey = new File(keyfilePath);
                if (privateKey.exists()) {
                    if (!Strings.isNullOrEmpty(this.passphrase)) {
                        new SSHCommandTask().execute(host, user, null, port,
                                sudoPass, keyfilePath, this.passphrase,
                                command.getCommand());
                    } else {
                        putLine("ERROR - No passphrase specified.");
                    }
                } else {
                    putLine("ERROR - Cannot find keyfile at location: "
                            + keyfilePath);
                }
            } else {
                putLine("ERROR - No keyfile was specified.");
            }
        }
    }

    private static void putLine(String text) {
        consoleOutput.append("\n" + text);
        consoleOutput.post(new Runnable() {

            @Override
            public void run() {
                final Layout layout = consoleOutput.getLayout();
                if (layout != null) {
                    int scrollDelta = layout.getLineBottom(consoleOutput
                            .getLineCount() - 1)
                            - consoleOutput.getScrollY()
                            - consoleOutput.getHeight();
                    if (scrollDelta > 0) {
                        consoleOutput.scrollBy(0, scrollDelta);
                    }
                }

            }
        });
    }

    private class SSHCommandTask extends AsyncTask<String, String, Boolean> {

        private IQueryService raspiQuery;

        @Override
        protected Boolean doInBackground(String... params) {
            // create and do query
            raspiQuery = new RaspiQuery((String) params[0], (String) params[1],
                    Integer.parseInt(params[3]));
            final String pass = params[2];
//			final String sudoPass = params[4];
            final String privateKeyPath = params[5];
            final String privateKeyPass = params[6];
            final String command = params[7];
            try {
                if (privateKeyPath != null) {
                    File f = new File(privateKeyPath);
                    if (privateKeyPass == null) {
                        // connect with private key only
                        raspiQuery.connectWithPubKeyAuth(f.getPath());
                    } else {
                        // connect with key and passphrase
                        raspiQuery.connectWithPubKeyAuthAndPassphrase(
                                f.getPath(), privateKeyPass);
                    }
                } else {
                    raspiQuery.connect(pass);
                }
                publishProgress("Connection established.");
                String output = raspiQuery.run(command);
                publishProgress(output);
                publishProgress("Connection closed.");
            } catch (RaspiQueryException e) {
                publishProgress("ERROR - " + e.getMessage());
                if (e.getCause() != null) {
                    publishProgress("Reason: " + e.getCause().getMessage());
                }
                return false;
            } finally {
                try {
                    raspiQuery.disconnect();
                } catch (RaspiQueryException e) {
                    LOGGER.debug("Error closing the ssh client.", e);
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // inform handler
            mHandler.post(mRunFinished);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            final String feedback = values[0];
            putLine(feedback);
            super.onProgressUpdate(values);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("didRun", this.didRun);

    }

}
