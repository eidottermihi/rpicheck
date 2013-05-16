package de.eidottermihi.rpicheck.activity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.app.NavUtils;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.eidottermihi.rpicheck.R;

/**
 * Settings activity. Settings items are inflated from xml.
 * 
 * @author Michael
 * 
 */
public class SettingsActivity extends SherlockPreferenceActivity implements
		OnSharedPreferenceChangeListener {
	private static final String LOG_TAG = "SettingsActivity";

	/** Preference keys. */
	public static final String KEY_PREF_TEMPERATURE_SCALE = "pref_temperature_scala";
	public static final String KEY_PREF_QUERY_HIDE_ROOT_PROCESSES = "pref_query_hide_root";
	public static final String KEY_PREF_FREQUENCY_UNIT = "pref_frequency_unit";

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
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

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(KEY_PREF_FREQUENCY_UNIT)) {
			findPreference(key)
					.setSummary(
							sharedPreferences.getString(
									KEY_PREF_FREQUENCY_UNIT, "MHz"));

		}
		if (key.equals(KEY_PREF_TEMPERATURE_SCALE)) {
			findPreference(key).setSummary(
					sharedPreferences.getString(KEY_PREF_TEMPERATURE_SCALE,
							"°C"));
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
