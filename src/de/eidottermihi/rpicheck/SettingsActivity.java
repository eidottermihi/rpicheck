package de.eidottermihi.rpicheck;

import java.util.Map;
import java.util.Map.Entry;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class SettingsActivity extends SherlockPreferenceActivity implements
		OnSharedPreferenceChangeListener {
	private static final String LOG_TAG = "SettingsActivity";
	public static final String KEY_PREF_TEMPERATURE_SCALE = "pref_temperature_scala";

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(LOG_TAG, "Activity launched.");
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		// ancestral navigation
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_settings, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// home button is pressed
			NavUtils.navigateUpFromSameTask(this);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d(LOG_TAG, "Shared preferences: Key[" + key + "] changed.");
		Map<String, ?> all = sharedPreferences.getAll();
		for (Entry<String, ?> entry : all.entrySet()) {
			Log.d(LOG_TAG, entry.getKey() + "="
					+ (Object) entry.getValue().toString());
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

}
