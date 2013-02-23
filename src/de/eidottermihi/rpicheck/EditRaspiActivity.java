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

public class EditRaspiActivity extends SherlockActivity {

	private static final String LOG_TAG = "EditRaspiActivity";
	private EditText editTextName;
	private EditText editTextHost;
	private EditText editTextUser;
	private EditText editTextPass;
	private EditText editTextSshPortOpt;
	private EditText editTextDescription;

	private DeviceDbHelper deviceDb;
	private RaspberryDeviceBean deviceBean;

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
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onSaveButtonClick(View view) {
		switch (view.getId()) {
		case R.id.new_raspi_save_button:
			updateRaspi();
			break;
		}
	}

	private void updateRaspi() {
		// getting credentials from textfields
		String name = editTextName.getText().toString().trim();
		String host = editTextHost.getText().toString().trim();
		String user = editTextUser.getText().toString().trim();
		String pass = editTextPass.getText().toString().trim();
		String sshPort = editTextSshPortOpt.getText().toString().trim();
		String description = editTextDescription.getText().toString().trim();
		Log.d(LOG_TAG, "Update raspi :" + name + "/" + host + "/" + user + "/"
				+ pass + "/" + sshPort);

		if (StringUtils.isBlank(name) || StringUtils.isBlank(host)
				|| StringUtils.isBlank(user) || StringUtils.isBlank(pass)) {
			Toast.makeText(this,
					getText(R.string.new_raspi_minimum),
					Toast.LENGTH_LONG).show();
		} else {
			updateRaspiInDb(name, host, user, pass, sshPort, description);
			// back to main
			NavUtils.navigateUpFromSameTask(this);
		}
	}

	private void updateRaspiInDb(String name, String host, String user,
			String pass, String sshPort, String description) {
		// if sshPort is empty, use default port (22)
		if (StringUtils.isBlank(sshPort)) {
			sshPort = getText(R.string.default_ssh_port).toString();
		}
		deviceBean.setName(name);
		deviceBean.setHost(host);
		deviceBean.setUser(user);
		deviceBean.setPass(pass);
		deviceBean.setPort(Integer.parseInt(sshPort));
		deviceBean.setDescription(description);
		deviceDb.update(deviceBean);
	}

}
