package de.eidottermihi.rpicheck.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.eidottermihi.rpicheck.R;
import de.eidottermihi.rpicheck.db.CommandBean;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;
import de.eidottermihi.rpicheck.db.RaspberryDeviceBean;
import de.eidottermihi.rpicheck.fragment.RunCommandDialog;

public class CustomCommandActivity extends SherlockFragmentActivity implements
		OnItemClickListener, OnItemLongClickListener {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CustomCommandActivity.class);

	private RaspberryDeviceBean currentDevice;

	private ListView commandListView;

	private DeviceDbHelper deviceDb = new DeviceDbHelper(this);

	private Cursor fullCommandCursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_commands);
		this.commandListView = (ListView) findViewById(R.id.commandListView);

		// Show the Up button in the action bar.
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		Bundle extras = this.getIntent().getExtras();
		if (extras != null && extras.get("pi") != null) {
			LOGGER.debug("onCreate: get currentDevice out of intent.");
			currentDevice = (RaspberryDeviceBean) extras.get("pi");
		} else if (savedInstanceState.getSerializable("pi") != null) {
			LOGGER.debug("onCreate: get currentDevice out of savedInstanceState.");
			currentDevice = (RaspberryDeviceBean) savedInstanceState
					.getSerializable("pi");
		}
		if (currentDevice != null) {
			LOGGER.debug("Setting activity title for device.");
			getSupportActionBar().setTitle(currentDevice.getName());
			LOGGER.debug("Initializing ListView");
			this.initListView(currentDevice);
		} else {
			LOGGER.debug("No current device! Setting no title");
		}

	}

	/**
	 * Init ListView with commands.
	 * 
	 * @param pi
	 */
	private void initListView(RaspberryDeviceBean pi) {
		fullCommandCursor = deviceDb.getFullCommandCursor();
		CommandAdapter commandsAdapter = new CommandAdapter(this,
				fullCommandCursor, CursorAdapter.FLAG_AUTO_REQUERY);
		commandListView.setAdapter(commandsAdapter);
		commandListView.setOnItemClickListener(this);
		commandListView.setOnItemLongClickListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_command, menu);
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
		case R.id.menu_new_command:
			// init intent
			Intent newCommandIntent = new Intent(CustomCommandActivity.this,
					NewCommandActivity.class);
			newCommandIntent.putExtras(this.getIntent().getExtras());
			this.startActivityForResult(newCommandIntent,
					NewCommandActivity.REQUEST_NEW);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == NewCommandActivity.REQUEST_NEW
				&& resultCode == RESULT_OK) {
			// new cmd saved, update...
			Toast.makeText(this, "New command saved!", Toast.LENGTH_SHORT)
					.show();
			initListView(currentDevice);
		}
		LOGGER.debug("Finished new command activity.");
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (currentDevice != null) {
			LOGGER.debug("Writing currentDevice in outState.");
			outState.putSerializable("pi", currentDevice);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int itemPos,
			long itemId) {
		ConnectivityManager connMgr = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			LOGGER.debug(
					"Command pos {} clicked. Item _id = {}. Starting run dialog.",
					itemPos, itemId);
			DialogFragment runCommandDialog = new RunCommandDialog();
			Bundle args = new Bundle();
			args.putSerializable("pi", currentDevice);
			CommandBean command = deviceDb.readCommand(itemId);
			args.putSerializable("cmd", command);
			runCommandDialog.setArguments(args);
			runCommandDialog.show(getSupportFragmentManager(), "runCommand");
		} else {
			Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		LOGGER.debug("Command pos {} long clicked. Item _id = {}.");
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (deviceDb != null) {
			deviceDb.close();
		}
	}

}
