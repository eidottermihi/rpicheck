package de.eidottermihi.rpicheck.fragment;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import de.eidottermihi.rpicheck.R;
import de.eidottermihi.rpicheck.activity.NewRaspiAuthActivity;
import de.eidottermihi.rpicheck.db.CommandBean;
import de.eidottermihi.rpicheck.db.RaspberryDeviceBean;
import de.eidottermihi.rpicheck.ssh.RaspiQuery;
import de.eidottermihi.rpicheck.ssh.RaspiQueryException;

public class RunCommandDialog extends DialogFragment {

	private boolean didRun = false;

	RaspberryDeviceBean device;
	CommandBean command;

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
		builder.setTitle("Running " + this.command.getName());
		builder.setIcon(R.drawable.device_access_accounts);
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
		Toast.makeText(this.getActivity(), "Running: " + command.getCommand(),
				Toast.LENGTH_SHORT).show();
		consoleOutput.setText("Connecting to " + device.getName() + "...");
		// get connection settings from shared preferences
		final String host = device.getHost();
		final String user = device.getUser();
		final String port = device.getPort() + "";
		final String sudoPass = device.getSudoPass();
		if (device.getAuthMethod().equals(
				NewRaspiAuthActivity.SPINNER_AUTH_METHODS[0])) {
			// ssh password
			final String pass = device.getPass();
			new SSHCommandTask().execute(host, user, pass, port, sudoPass,
					null, null, command.getCommand());
		} else if (device.getAuthMethod().equals(
				NewRaspiAuthActivity.SPINNER_AUTH_METHODS[1])) {
			// keyfile
			final String keyfilePath = device.getKeyfilePath();
			if (keyfilePath != null) {
				final File privateKey = new File(keyfilePath);
				if (privateKey.exists()) {
					new SSHCommandTask().execute(host, user, null, port,
							sudoPass, keyfilePath, null, command.getCommand());
				} else {
					Toast.makeText(this.getActivity(),
							"Cannot find keyfile at location: " + keyfilePath,
							Toast.LENGTH_LONG);
				}
			} else {
				Toast.makeText(this.getActivity(), "No keyfile specified!",
						Toast.LENGTH_LONG);
			}
		} else if (device.getAuthMethod().equals(
				NewRaspiAuthActivity.SPINNER_AUTH_METHODS[2])) {
			// keyfile and passphrase
			final String keyfilePath = device.getKeyfilePath();
			if (keyfilePath != null) {
				final File privateKey = new File(keyfilePath);
				if (privateKey.exists()) {
					if (!StringUtils.isBlank(device.getKeyfilePass())) {
						final String passphrase = device.getKeyfilePass();
						new SSHCommandTask().execute(host, user, null, port,
								sudoPass, keyfilePath, passphrase,
								command.getCommand());
					} else {
						// TODO ask for passphrase
						// final String dialogType =
						// type.equals(TYPE_REBOOT) ?
						// PassphraseDialog.SSH_SHUTDOWN
						// : PassphraseDialog.SSH_HALT;
						// final DialogFragment passphraseDialog = new
						// PassphraseDialog();
						// final Bundle args = new Bundle();
						// args.putString(PassphraseDialog.KEY_TYPE,
						// dialogType);
						// passphraseDialog.setArguments(args);
						// passphraseDialog.setCancelable(false);
						// passphraseDialog.show(getSupportFragmentManager(),
						// "passphrase");
					}
				} else {
					Toast.makeText(this.getActivity(),
							"Cannot find keyfile at location: " + keyfilePath,
							Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(this.getActivity(), "No keyfile specified!",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private static void putLine(String text) {
		consoleOutput.append("\n" + text);
	}

	private class SSHCommandTask extends AsyncTask<String, String, Boolean> {

		private RaspiQuery raspiQuery;

		@Override
		protected Boolean doInBackground(String... params) {
			// create and do query
			raspiQuery = new RaspiQuery((String) params[0], (String) params[1],
					Integer.parseInt(params[3]));
			final String pass = params[2];
			final String sudoPass = params[4];
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
				publishProgress("Connected.");
				String output = raspiQuery.run(command);
				publishProgress(output);
				raspiQuery.disconnect();
				publishProgress("Disconnected.");
			} catch (RaspiQueryException e) {
				publishProgress(e.getMessage());
				if (e.getCause() != null) {
					publishProgress("Reason: " + e.getCause().getMessage());
				}
				return false;
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
