package de.eidottermihi.rpicheck.activity;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import de.eidottermihi.rpicheck.R;
import de.eidottermihi.rpicheck.activity.helper.Validation;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;

public class NewRaspiActivity extends SherlockActivity {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(NewRaspiActivity.class);
	private EditText editTextName;
	private EditText editTextHost;
	private EditText editTextUser;
	private EditText editTextPass;
	private EditText editTextSshPortOpt;
	private EditText editTextDescription;
	private EditText editTextSudoPw;

	private Validation validator = new Validation();

	private DeviceDbHelper deviceDb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_raspi);
		// Show the Up button in the action bar.
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// assigning view elements to fields
		// assigning view elements to fields
		editTextName = (EditText) findViewById(R.id.edit_raspi_name_editText);
		editTextHost = (EditText) findViewById(R.id.edit_raspi_host_editText);
		editTextUser = (EditText) findViewById(R.id.edit_raspi_user_editText);
		editTextPass = (EditText) findViewById(R.id.edit_raspi_pass_editText);
		editTextSshPortOpt = (EditText) findViewById(R.id.edit_raspi_ssh_port_editText);
		editTextDescription = (EditText) findViewById(R.id.edit_raspi_desc_editText);
		editTextSudoPw = (EditText) findViewById(R.id.edit_raspi_sudoPass_editText);
		// Show information text
		final View text = findViewById(R.id.new_raspi_text);
		text.setVisibility(View.VISIBLE);

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
		case R.id.new_raspi_save_button:
			saveRaspi();
			break;
		}
	}

	private void saveRaspi() {
		boolean validationSuccessful = validator.validatePiData(this,
				editTextName, editTextHost, editTextUser, editTextPass,
				editTextSshPortOpt, editTextSudoPw);
		if (validationSuccessful) {
			// getting credentials from textfields
			final String name = editTextName.getText().toString().trim();
			final String host = editTextHost.getText().toString().trim();
			final String user = editTextUser.getText().toString().trim();
			final String pass = editTextPass.getText().toString().trim();
			final String sshPort = editTextSshPortOpt.getText().toString()
					.trim();
			final String description = editTextDescription.getText().toString()
					.trim();
			final String sudoPass = editTextSudoPw.getText().toString().trim();
			addRaspiToDb(name, host, user, pass, sshPort, description, sudoPass);
			Toast.makeText(this, R.string.new_pi_created, Toast.LENGTH_SHORT)
					.show();
			// back to main
			NavUtils.navigateUpFromSameTask(this);
		}
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
