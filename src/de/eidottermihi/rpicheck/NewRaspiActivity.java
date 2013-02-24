package de.eidottermihi.rpicheck;

import org.apache.commons.lang3.StringUtils;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class NewRaspiActivity extends SherlockActivity {

	private static final String LOG_TAG = "NewRaspiActivity";
	private EditText editTextName;
	private EditText editTextHost;
	private EditText editTextUser;
	private EditText editTextPass;
	private EditText editTextSshPortOpt;
	private EditText editTextDescription;
	private EditText editTextSudoPw;

	private DeviceDbHelper deviceDb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_raspi);
		// Show the Up button in the action bar.
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// assigning view elements to fields
		editTextName = (EditText) findViewById(R.id.raspi_name_editText);
		editTextHost = (EditText) findViewById(R.id.raspi_host_editText);
		editTextUser = (EditText) findViewById(R.id.raspi_user_editText);
		editTextPass = (EditText) findViewById(R.id.raspi_pass_editText);
		editTextSshPortOpt = (EditText) findViewById(R.id.raspi_ssh_port_editText);
		editTextDescription = (EditText) findViewById(R.id.raspi_desc_editText);
		editTextSudoPw = (EditText) findViewById(R.id.raspi_sudoPass_editText);

		// init sql db
		deviceDb = new DeviceDbHelper(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.activity_new_raspi, menu);
		return super.onCreateOptionsMenu(menu);
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
			return true;
		case R.id.new_raspi_save_button:
			saveRaspi();
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
		// getting credentials from textfields
		final String name = editTextName.getText().toString().trim();
		final String host = editTextHost.getText().toString().trim();
		final String user = editTextUser.getText().toString().trim();
		final String pass = editTextPass.getText().toString().trim();
		final String sshPort = editTextSshPortOpt.getText().toString().trim();
		final String description = editTextDescription.getText().toString().trim();
		final String sudoPass = editTextSudoPw.getText().toString().trim();
		Log.d(LOG_TAG, "New raspi :" + name + "/" + host + "/" + user + "/"
				+ pass + "/" + sshPort + "/" + sudoPass);

		if (StringUtils.isBlank(name) || StringUtils.isBlank(host)
				|| StringUtils.isBlank(user) || StringUtils.isBlank(pass) || StringUtils.isBlank(sudoPass)) {
			Toast.makeText(this,
					R.string.new_raspi_minimum,
					Toast.LENGTH_LONG).show();
		} else {
			addRaspiToDb(name, host, user, pass, sshPort, description, sudoPass);
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
		deviceDb.create(name, host, user, pass, Integer.parseInt(sshPort),
				description, sudoPass);
	}

}
