package de.eidottermihi.rpicheck.activity;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import de.eidottermihi.rpicheck.R;
import de.eidottermihi.rpicheck.activity.helper.Validation;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;

public class NewRaspiAuthActivity extends SherlockActivity {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(NewRaspiAuthActivity.class);

	private Validation validator = new Validation();

	private Spinner spinnerAuth;

	private DeviceDbHelper deviceDb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_raspi_auth);
		// Show the Up button in the action bar.
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// assigning view elements to fields
		spinnerAuth = (Spinner) findViewById(R.id.spinnerAuthMethod);
		// init auth spinner
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.auth_methods,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinnerAuth.setAdapter(adapter);
		spinnerAuth.setOnItemSelectedListener(null);

		// init sql db
		deviceDb = new DeviceDbHelper(this);
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

	public void onSaveButtonClick(View view) {
		switch (view.getId()) {
		case R.id.new_raspi_continue_button:
			saveRaspi();
			break;
		}
	}

	private void saveRaspi() {
		// boolean validationSuccessful = validator.validatePiData(this,
		// editTextName, editTextHost, editTextUser, editTextPass,
		// editTextSshPortOpt, editTextSudoPw);
		// if (validationSuccessful) {
		// // getting credentials from textfields
		// final String name = editTextName.getText().toString().trim();
		// final String host = editTextHost.getText().toString().trim();
		// final String user = editTextUser.getText().toString().trim();
		// final String pass = editTextPass.getText().toString().trim();
		// final String sshPort = editTextSshPortOpt.getText().toString()
		// .trim();
		// final String description = editTextDescription.getText().toString()
		// .trim();
		// final String sudoPass = editTextSudoPw.getText().toString().trim();
		// addRaspiToDb(name, host, user, pass, sshPort, description, sudoPass);
		// Toast.makeText(this, R.string.new_pi_created, Toast.LENGTH_SHORT)
		// .show();
		// // back to main
		// NavUtils.navigateUpFromSameTask(this);
		// }
	}

	private void addRaspiToDb(String name, String host, String user,
			String pass, String sshPort, String description, String sudoPass) {
		// if sshPort is empty, use default port (22)
		if (StringUtils.isBlank(sshPort)) {
			sshPort = getText(R.string.default_ssh_port).toString();
		}
		if (StringUtils.isBlank(sudoPass)) {
			sudoPass = "";
		}
		deviceDb.create(name, host, user, pass, Integer.parseInt(sshPort),
				description, sudoPass);
	}

}
