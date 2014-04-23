package de.eidottermihi.rpicheck.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.common.base.Strings;

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
	public static final int REQUEST_EDIT = 1;
	// Key for CommandBean when edit is requested
	public static final String CMD_KEY_EDIT = "cmdId";

	EditText nameEditText;
	EditText commandEditText;

	DeviceDbHelper db;

	Validation validation = new Validation();

	Long cmdId = null;

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

		if (getIntent().getExtras() != null
				&& getIntent().getExtras().getLong(CMD_KEY_EDIT, -1L) != -1) {
			// edit existing command!
			cmdId = getIntent().getExtras().getLong(CMD_KEY_EDIT);
			getSupportActionBar().setTitle(
					getString(R.string.activity_title_edit_command));
			CommandBean commandBean = db.readCommand(cmdId);
			nameEditText.setText(commandBean.getName());
			commandEditText.setText(commandBean.getCommand());
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_command_new, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			LOGGER.info("Cancelling new/edit command activity.");
			this.setResult(RESULT_CANCELED);
			this.finish();
			return true;
		case R.id.menu_save:
			saveCommand();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void saveCommand() {
		if (validation.validateNewCmdData(this, commandEditText)) {
			String name = nameEditText.getText().toString();
			String cmd = commandEditText.getText().toString().trim();
			if (Strings.isNullOrEmpty(name)) {
				name = cmd;
			}
			CommandBean bean = new CommandBean();
			bean.setName(name);
			bean.setCommand(cmd);
			bean.setShowOutput(true);
			if (cmdId == null) {
				db.create(bean);
			} else {
				bean.setId(cmdId);
				db.updateCommand(bean);
			}
			this.setResult(RESULT_OK);
			this.finish();
		}
	}

}
