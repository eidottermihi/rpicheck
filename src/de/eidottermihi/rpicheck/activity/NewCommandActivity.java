package de.eidottermihi.rpicheck.activity;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import de.eidottermihi.rpicheck.R;
import de.eidottermihi.rpicheck.activity.helper.Validation;
import de.eidottermihi.rpicheck.db.CommandBean;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;

public class NewCommandActivity extends SherlockActivity {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(NewCommandActivity.class);

	// Requestcode for new command
	public static final int REQUEST_NEW = 0;
	// Requestcode for edit command
	public static final int REQUEST_EDIT = 0;
	// Key for CommandBean when edit is requested
	public static final String CMD_KEY_EDIT = "cmd";

	EditText nameEditText;
	EditText commandEditText;
	CheckBox showOutputCheckbox;

	DeviceDbHelper db;

	Validation validation = new Validation();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_command);
		// Show the Up button in the action bar.
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(true);

		db = new DeviceDbHelper(this);

		nameEditText = (EditText) findViewById(R.id.new_cmd_name_editText);
		commandEditText = (EditText) findViewById(R.id.new_cmd_command_editText);
		showOutputCheckbox = (CheckBox) findViewById(R.id.show_output_cb);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			LOGGER.info("Cancelling new command activity.");
			this.setResult(RESULT_CANCELED);
			this.finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onSaveButtonClick(View view) {
		if (validation.validateNewCmdData(this, commandEditText)) {
			String name = nameEditText.getText().toString().trim();
			String cmd = nameEditText.getText().toString().trim();
			boolean showOutput = showOutputCheckbox.isChecked();
			if (StringUtils.isBlank(name)) {
				name = cmd;
			}
			CommandBean bean = new CommandBean();
			bean.setName(name);
			bean.setCommand(cmd);
			bean.setShowOutput(showOutput);
			db.create(bean);
			this.setResult(RESULT_OK);
			this.finish();
		}
	}

}
