package de.eidottermihi.rpicheck;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

// TODO check if hostname/username/password are blank/empty string (app crashes if so)
public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	private static final String LOG_TAG = "SettingsActivity";
	public static final String KEY_PREF_HOSTNAME = "pref_hostname";
	public static final String KEY_PREF_USERNAME = "pref_username";
	public static final String KEY_PREF_PASSWORD = "pref_password";

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(LOG_TAG, "Activity launched.");
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		// preferences were visited, set preferencesShown to true
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		prefs.edit().putBoolean(MainActivity.KEY_PREFERENCES_SHOWN, true)
				.commit();

		// code from
		// http://stackoverflow.com/questions/5905240/showing-preference-screen-first-time-app-is-run-and-related-questions

		// check if prefs have value, if not then set summary to tooltip text
		Preference prefHost = findPreference(KEY_PREF_HOSTNAME);
		if (prefs.getString(KEY_PREF_HOSTNAME, null) == null) {
			prefHost.setSummary(R.string.tooltip_hostname);
		} else {
			prefHost.setSummary(prefs.getString(KEY_PREF_HOSTNAME, null));
		}
		Preference prefUser = findPreference(KEY_PREF_USERNAME);
		if (prefs.getString(KEY_PREF_USERNAME, null) == null) {
			prefUser.setSummary(R.string.tooltip_username);
		} else {
			prefUser.setSummary(prefs.getString(KEY_PREF_USERNAME, null));
		}
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d(LOG_TAG, "Shared preferences: Key[" + key + "] changed.");
		Preference pref = findPreference(key);
		if (key.equals(KEY_PREF_HOSTNAME)) {
			// set summary of hostname preference to user's hostname
			pref.setSummary(sharedPreferences.getString(key, null));
		} else if (key.equals(KEY_PREF_USERNAME)) {
			// set summary of username preference to user-specified username
			pref.setSummary(sharedPreferences.getString(key, null));
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
