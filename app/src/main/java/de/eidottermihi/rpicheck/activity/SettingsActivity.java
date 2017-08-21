/**
 * Copyright (C) 2017  RasPi Check Contributors
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

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import de.eidottermihi.raspicheck.BuildConfig;
import de.eidottermihi.raspicheck.R;
import de.eidottermihi.rpicheck.activity.helper.LoggingHelper;
import io.freefair.android.preference.AppCompatPreferenceActivity;
import sheetrock.panda.changelog.ChangeLog;

/**
 * Settings activity. Settings items are inflated from xml.
 *
 * @author Michael
 */
public class SettingsActivity extends AppCompatPreferenceActivity implements
        OnSharedPreferenceChangeListener, OnPreferenceClickListener {

    /**
     * Preference keys.
     */
    public static final String KEY_PREF_TEMPERATURE_SCALE = "pref_temperature_scala";
    public static final String KEY_PREF_QUERY_HIDE_ROOT_PROCESSES = "pref_query_hide_root";
    public static final String KEY_PREF_FREQUENCY_UNIT = "pref_frequency_unit";
    public static final String KEY_PREF_DEBUG_LOGGING = "pref_debug_log";

    private static final String KEY_PREF_LOG = "pref_log";
    private static final String KEY_PREF_CHANGELOG = "pref_changelog";
    private static final String KEY_PREF_LOAD_AVG_PERIOD = "pref_load_avg";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SettingsActivity.class);

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        // adding preference listener to log / changelog
        Preference prefLog = findPreference(KEY_PREF_LOG);
        prefLog.setOnPreferenceClickListener(this);
        Preference prefChangelog = findPreference(KEY_PREF_CHANGELOG);
        prefChangelog.setOnPreferenceClickListener(this);

        findPreference("pref_app_version").setSummary(BuildConfig.VERSION_NAME);

        // init summary texts to reflect users choice
        this.initSummaries();
    }

    private void initSummaries() {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        initSummary(prefs, KEY_PREF_TEMPERATURE_SCALE);
        initSummary(prefs, KEY_PREF_FREQUENCY_UNIT);
    }

    @SuppressWarnings("deprecation")
    private void initSummary(SharedPreferences prefs, String prefKey) {
        final Preference pref = findPreference(prefKey);
        final String prefValue = prefs.getString(prefKey, null);
        if (prefValue != null) {
            pref.setSummary(prefValue);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(KEY_PREF_FREQUENCY_UNIT)) {
            initSummary(sharedPreferences, KEY_PREF_FREQUENCY_UNIT);
        }
        if (key.equals(KEY_PREF_TEMPERATURE_SCALE)) {
            initSummary(sharedPreferences, KEY_PREF_TEMPERATURE_SCALE);
        }
        if (key.equals(KEY_PREF_DEBUG_LOGGING)) {
            boolean debugEnabled = sharedPreferences.getBoolean(key, false);
            if (debugEnabled) {
                LOGGER.warn("Enabling debug logging. Be warned that the log file can get huge because of this.");
            } else {
                LOGGER.info("Disabled debug logging.");
            }
            LoggingHelper.initLogging(this);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        boolean clickHandled = false;
        if (preference.getKey().equals(KEY_PREF_LOG)) {
            LOGGER.trace("View log was clicked.");
            clickHandled = true;
            String logfilePath = LoggingHelper.getLogfilePath(this);
            if (logfilePath != null) {
                File log = new File(logfilePath);
                if (log.exists()) {
                    final Intent intent = new Intent();
                    intent.setDataAndType(Uri.fromFile(log), "text/plain");
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Log file does not exist.",
                            Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Log file does not exist.",
                        Toast.LENGTH_LONG).show();
            }
        } else if (preference.getKey().equals(KEY_PREF_CHANGELOG)) {
            LOGGER.trace("View changelog was clicked.");
            clickHandled = true;
            ChangeLog cl = new ChangeLog(this);
            cl.getFullLogDialog().show();
        }
        return clickHandled;
    }

}
