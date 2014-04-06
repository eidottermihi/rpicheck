package de.eidottermihi.rpicheck.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.eidottermihi.rpicheck.R;
import de.eidottermihi.rpicheck.db.RaspberryDeviceBean;

public class CustomCommandActivity extends SherlockActivity {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CustomCommandActivity.class);

	private RaspberryDeviceBean currentDevice;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_commands);
		// Show the Up button in the action bar.
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		Bundle extras = this.getIntent().getExtras();
		if (extras.get("pi") != null) {
			currentDevice = (RaspberryDeviceBean) extras.get("pi");
			getSupportActionBar().setTitle(currentDevice.getName());
		}

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
		LOGGER.info("onActivityResult()");
		if (requestCode == NewCommandActivity.REQUEST_NEW
				&& resultCode == RESULT_OK) {
			// new cmd saved, update...
			Toast.makeText(this, "New command saved!", Toast.LENGTH_SHORT)
					.show();
		}
		LOGGER.info("Finished new command activity.");
		currentDevice = (RaspberryDeviceBean) data.getExtras().get("pi");
		getSupportActionBar().setTitle(currentDevice.getName());
		super.onActivityResult(requestCode, resultCode, data);
	}

}
