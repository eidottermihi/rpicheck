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
import de.eidottermihi.rpicheck.db.RaspberryDeviceBean;

public class EditRaspiActivity extends SherlockActivity {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(EditRaspiActivity.class);
	private EditText editTextName;
	private EditText editTextHost;
	private EditText editTextUser;
	private EditText editTextPass;
	private EditText editTextSshPortOpt;
	private EditText editTextDescription;
	private EditText editTextSudoPass;

	private DeviceDbHelper deviceDb;
	private RaspberryDeviceBean deviceBean;

	private Validation validator = new Validation();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_raspi);
		// Show the Up button in the action bar.
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// assigning view elements to fields
		editTextName = (EditText) findViewById(R.id.edit_raspi_name_editText);
		editTextHost = (EditText) findViewById(R.id.edit_raspi_host_editText);
		editTextUser = (EditText) findViewById(R.id.edit_raspi_user_editText);
		editTextPass = (EditText) findViewById(R.id.edit_raspi_pass_editText);
		editTextSshPortOpt = (EditText) findViewById(R.id.edit_raspi_ssh_port_editText);
		editTextDescription = (EditText) findViewById(R.id.edit_raspi_desc_editText);
		editTextSudoPass = (EditText) findViewById(R.id.edit_raspi_sudoPass_editText);

		// init sql db
		deviceDb = new DeviceDbHelper(this);

		// read device information
		int deviceId = this.getIntent().getExtras()
				.getInt(MainActivity.EXTRA_DEVICE_ID);
		deviceBean = deviceDb.read(deviceId);

		// fill fields from device bean
		editTextName.setText(deviceBean.getName());
		editTextHost.setText(deviceBean.getHost());
		editTextUser.setText(deviceBean.getUser());
		editTextPass.setText(deviceBean.getPass());
		editTextSshPortOpt.setText(deviceBean.getPort() + "");
		editTextDescription.setText(deviceBean.getDescription());
		editTextSudoPass.setText(deviceBean.getSudoPass());

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
			updateRaspi();
			break;
		}
	}

	private void updateRaspi() {
		boolean validationSuccessful = validator.validatePiData(this,
				editTextName, editTextHost, editTextUser, editTextPass,
				editTextSshPortOpt, editTextSudoPass);
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
			final String sudoPass = editTextSudoPass.getText().toString()
					.trim();
			updateRaspiInDb(name, host, user, pass, sshPort, description,
					sudoPass);
			Toast.makeText(this, R.string.update_successful, Toast.LENGTH_SHORT)
					.show();
			// back to main
			NavUtils.navigateUpFromSameTask(this);
		}
	}

	private void updateRaspiInDb(String name, String host, String user,
			String pass, String sshPort, String description, String sudoPass) {
		// if sshPort is empty, use default port (22)
		if (StringUtils.isBlank(sshPort)) {
			sshPort = getText(R.string.default_ssh_port).toString();
		}
		// if sudoPass is null use empty pass
		if (StringUtils.isBlank(sudoPass)) {
			sudoPass = "";
		}
		deviceBean.setName(name);
		deviceBean.setHost(host);
		deviceBean.setUser(user);
		deviceBean.setPass(pass);
		deviceBean.setPort(Integer.parseInt(sshPort));
		deviceBean.setDescription(description);
		deviceBean.setSudoPass(sudoPass);
		deviceDb.update(deviceBean);
	}

}
