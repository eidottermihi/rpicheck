package de.eidottermihi.rpicheck.activity;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;

import de.eidottermihi.rpicheck.R;
import de.eidottermihi.rpicheck.activity.helper.Validation;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;

public class NewRaspiAuthActivity extends SherlockActivity implements
		OnItemSelectedListener {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(NewRaspiAuthActivity.class);

	public static final String[] SPINNER_AUTH_METHODS = { "password", "keys",
			"keysWithPassword" };

	private static final int REQUEST_LOAD = 0;

	private Validation validator = new Validation();

	private Spinner spinnerAuth;

	private DeviceDbHelper deviceDb;

	private RelativeLayout relLaySshPass;
	private RelativeLayout relLayKeyfile;
	private EditText editTextSshPass;
	private EditText editTextKeyfilePass;
	private Button buttonKeyfile;
	private TextView textKeyPass;
	private EditText editTextSshPort;
	private EditText editTextSudoPw;

	private String keyfilePath;

	private String host;
	private String desc;
	private String user;
	private String name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_raspi_auth);
		// Show the Up button in the action bar.
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// assigning view elements to fields
		spinnerAuth = (Spinner) findViewById(R.id.spinnerAuthMethod);
		relLaySshPass = (RelativeLayout) findViewById(R.id.rel_pw);
		relLayKeyfile = (RelativeLayout) findViewById(R.id.rel_key);
		editTextSshPass = (EditText) findViewById(R.id.editText_ssh_password);
		editTextKeyfilePass = (EditText) findViewById(R.id.editTextKeyPw);
		buttonKeyfile = (Button) findViewById(R.id.buttonKeyfile);
		textKeyPass = (TextView) findViewById(R.id.text_key_pw);
		editTextSshPort = (EditText) findViewById(R.id.edit_raspi_ssh_port_editText);
		editTextSudoPw = (EditText) findViewById(R.id.edit_raspi_sudoPass_editText);
		// show default option for auth method = ssh password
		this.switchAuthMethodsInView(SPINNER_AUTH_METHODS[0]);
		// init auth spinner
		final ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(this, R.array.auth_methods,
						android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinnerAuth.setAdapter(adapter);
		spinnerAuth.setOnItemSelectedListener(this);

		// init sql db
		deviceDb = new DeviceDbHelper(this);

		// get data from previous screen (name/host/user...), already validated
		final Bundle piData = this.getIntent().getExtras()
				.getBundle(NewRaspiActivity.PI_BUNDLE);
		host = piData.getString(NewRaspiActivity.PI_HOST);
		name = piData.getString(NewRaspiActivity.PI_NAME);
		user = piData.getString(NewRaspiActivity.PI_USER);
		desc = piData.getString(NewRaspiActivity.PI_DESC);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onButtonClick(View view) {
		switch (view.getId()) {
		case R.id.new_raspi_save_button:
			saveRaspi();
			break;
		case R.id.buttonKeyfile:
			openKeyfile();
			break;
		default:
			break;
		}
	}

	private void openKeyfile() {
		final Intent intent = new Intent(getBaseContext(), FileDialog.class);
		intent.putExtra(FileDialog.START_PATH, Environment
				.getExternalStorageDirectory().getPath());

		// can user select directories or not
		intent.putExtra(FileDialog.CAN_SELECT_DIR, false);
		// user can only open existing files
		intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);

		// alternatively you can set file filter
		// intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "png" });
		this.startActivityForResult(intent, REQUEST_LOAD);
	}

	private void saveRaspi() {
		// get auth method
		final String selectedAuthMethod = SPINNER_AUTH_METHODS[spinnerAuth
				.getSelectedItemPosition()];
		final String sudoPass = editTextSudoPw.getText().toString().trim();
		final String sshPort = editTextSshPort.getText().toString().trim();
		boolean saveSuccessful = false;
		if (selectedAuthMethod.equals(SPINNER_AUTH_METHODS[0])) {
			// ssh password (cannot be empty)
			if (validator.checkNonOptionalTextField(editTextSshPass,
					getString(R.string.validation_msg_password))) {
				final String sshPass = editTextSshPass.getText().toString()
						.trim();
				addRaspiToDb(name, host, user, selectedAuthMethod, sshPort,
						desc, sudoPass, sshPass, null, null);
				saveSuccessful = true;
			}
		} else if (selectedAuthMethod.equals(SPINNER_AUTH_METHODS[1])) {
			// keyfile must be selected
			if (keyfilePath != null && new File(keyfilePath).exists()) {
				addRaspiToDb(name, host, user, selectedAuthMethod, sshPort,
						desc, sudoPass, null, null, keyfilePath);
				saveSuccessful = true;
			} else {
				Toast.makeText(this, getText(R.string.no_keyfile_present),
						Toast.LENGTH_LONG);
			}
		} else if (selectedAuthMethod.equals(SPINNER_AUTH_METHODS[2])) {
			// keyfile and key password
			if (keyfilePath != null && new File(keyfilePath).exists()) {
				if (validator.checkNonOptionalTextField(editTextKeyfilePass,
						getString(R.string.raspi_key_password_empty))) {
					final String keyfilePass = editTextKeyfilePass.getText()
							.toString().trim();
					addRaspiToDb(name, host, user, selectedAuthMethod, sshPort,
							desc, sudoPass, null, keyfilePass, keyfilePath);
					saveSuccessful = true;
				}
			} else {
				Toast.makeText(this, getText(R.string.no_keyfile_present),
						Toast.LENGTH_LONG);
			}
			saveSuccessful = true;
		}
		if (saveSuccessful) {
			Toast.makeText(this, R.string.new_pi_created, Toast.LENGTH_SHORT)
					.show();
			// start main activity
			final Intent i = new Intent(NewRaspiAuthActivity.this,
					MainActivity.class);
			this.startActivity(i);
		}

	}

	private void addRaspiToDb(String name, String host, String user,
			String authMethod, String sshPort, String description,
			String sudoPass, String sshPass, String keyPass, String keyPath) {
		// if sshPort is empty, use default port (22)
		if (StringUtils.isBlank(sshPort)) {
			sshPort = getText(R.string.default_ssh_port).toString();
		}
		if (StringUtils.isBlank(sudoPass)) {
			sudoPass = "";
		}
		deviceDb.create(name, host, user, sshPass, Integer.parseInt(sshPort),
				description, sudoPass, authMethod, keyPath, keyPass);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		final String selectedAuthMethod = SPINNER_AUTH_METHODS[pos];
		LOGGER.debug("Auth method selected: {}", selectedAuthMethod);
		this.switchAuthMethodsInView(selectedAuthMethod);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}

	private void switchAuthMethodsInView(String method) {
		if (method.equals(SPINNER_AUTH_METHODS[0])) {
			// show only ssh password
			relLaySshPass.setVisibility(View.VISIBLE);
			relLayKeyfile.setVisibility(View.GONE);
		} else if (method.equals(SPINNER_AUTH_METHODS[1])) {
			// show key file button
			relLaySshPass.setVisibility(View.GONE);
			relLayKeyfile.setVisibility(View.VISIBLE);
			textKeyPass.setVisibility(View.GONE);
			editTextKeyfilePass.setVisibility(View.GONE);
		} else {
			// show key file button and passphrase field
			relLaySshPass.setVisibility(View.GONE);
			relLayKeyfile.setVisibility(View.VISIBLE);
			textKeyPass.setVisibility(View.VISIBLE);
			editTextKeyfilePass.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_LOAD) {
				final String filePath = data
						.getStringExtra(FileDialog.RESULT_PATH);
				LOGGER.debug("Path of selected keyfile: {}", filePath);
				this.keyfilePath = filePath;
				buttonKeyfile.setText(filePath);
			}
		} else if (resultCode == Activity.RESULT_CANCELED) {
			LOGGER.warn("No file selected...");
		}
	}

}
