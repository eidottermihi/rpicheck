/**
 * Copyright (C) 2015  RasPi Check Contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package de.eidottermihi.rpicheck.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.eidottermihi.raspicheck.R;
import de.eidottermihi.rpicheck.activity.helper.Validation;

public class NewRaspiActivity extends SherlockActivity {
	public static final String PI_HOST = "PI_HOST";
	public static final String PI_NAME = "PI_NAME";
	public static final String PI_USER = "PI_USER";
	public static final String PI_DESC = "PI_DESC";
	public static final String PI_BUNDLE = "PI_BUNDLE";
	public static final int REQUEST_SAVE = 0;
	private static final Logger LOGGER = LoggerFactory
			.getLogger(NewRaspiActivity.class);
	private EditText editTextName;
	private EditText editTextHost;
	private EditText editTextUser;
	private EditText editTextDescription;

	private Validation validator = new Validation();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_raspi_new);
		// Show the Up button in the action bar.
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// assigning view elements to fields
		editTextName = (EditText) findViewById(R.id.edit_raspi_name_editText);
		editTextHost = (EditText) findViewById(R.id.edit_raspi_host_editText);
		editTextUser = (EditText) findViewById(R.id.edit_raspi_user_editText);
		editTextDescription = (EditText) findViewById(R.id.edit_raspi_desc_editText);
		// Show information text
		final View text = findViewById(R.id.new_raspi_text);
		text.setVisibility(View.VISIBLE);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_raspi_new, menu);
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
		case R.id.menu_continue:
			continueToAuthMethodActivity();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void continueToAuthMethodActivity() {
		boolean validationSuccessful = validator.validatePiCoreData(this,
				editTextName, editTextHost, editTextUser);
		if (validationSuccessful) {
			// getting credentials from textfields
			final String name = editTextName.getText().toString().trim();
			final String host = editTextHost.getText().toString().trim();
			final String user = editTextUser.getText().toString().trim();
			final String description = editTextDescription.getText().toString()
					.trim();
			// continue to auth activity
			final Intent i = new Intent(NewRaspiActivity.this,
					NewRaspiAuthActivity.class);
			final Bundle b = new Bundle();
			b.putString(PI_NAME, name);
			b.putString(PI_HOST, host);
			b.putString(PI_USER, user);
			b.putString(PI_DESC, description);
			i.putExtra(PI_BUNDLE, b);
			this.startActivityForResult(i, REQUEST_SAVE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_SAVE && resultCode == RESULT_OK) {
			// user saved the new raspi
			setResult(RESULT_OK);
			finish();
		}
	}

}
