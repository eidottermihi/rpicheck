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
package de.eidottermihi.rpicheck.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.eidottermihi.raspicheck.R;
import de.eidottermihi.rpicheck.activity.helper.LoggingHelper;
import de.eidottermihi.rpicheck.adapter.DeviceSpinnerAdapter;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;
import de.eidottermihi.rpicheck.db.RaspberryDeviceBean;
import io.freefair.android.injection.annotation.InjectView;
import io.freefair.android.injection.annotation.XmlLayout;
import io.freefair.android.injection.annotation.XmlMenu;
import io.freefair.android.injection.app.InjectionAppCompatActivity;


/**
 * The configuration screen for the {@link de.eidottermihi.rpicheck.widget.OverclockingWidget OverclockingWidget} AppWidget.
 */
@XmlLayout(R.layout.overclocking_widget_configure)
@XmlMenu(R.menu.activity_overclocking_widget_configure)
public class OverclockingWidgetConfigureActivity extends InjectionAppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static final String ACTION_WIDGET_UPDATE_ONE = "updateOneWidget";
    public static final String PREF_SHOW_TEMP_SUFFIX = "_temp";
    public static final String PREF_SHOW_ARM_SUFFIX = "_arm";
    public static final String PREF_SHOW_LOAD_SUFFIX = "_load";
    public static final String PREF_SHOW_MEMORY_SUFFIX = "_memory";
    private static final Logger LOGGER = LoggerFactory.getLogger(OverclockingWidgetConfigureActivity.class);
    private static final String PREFS_NAME = "de.eidottermihi.rpicheck.widget.OverclockingWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    private static final String PREF_UPDATE_ONLY_ON_WIFI = "_onlywifi";
    private static final String URI_SCHEME = "raspicheck";

    private static final String PREF_UPDATE_INTERVAL_SUFFIX = "_interval";
    private static final String UPDATE_YES = "yes";
    private static final String UPDATE_WIFI = "wifi";
    private static final String UPDATE_NO = "no";
    // corresponds to array/widget_auto_update
    private static final String[] autoUpdate = {UPDATE_YES, UPDATE_WIFI, UPDATE_NO};

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    @InjectView(R.id.widgetPiSpinner)
    private Spinner widgetPiSpinner;
    @InjectView(R.id.textEditUpdateInterval)
    private EditText textEditUpdateInterval;
    @InjectView(R.id.widgetUpdateSpinner)
    private Spinner widgetUpdateSpinner;
    @InjectView(R.id.widgetUpdateIntervalSpinner)
    private Spinner widgetUpdateIntervalSpinner;
    @InjectView(R.id.linLayoutCustomUpdateInterval)
    private TextInputLayout linLayoutCustomInterval;
    @InjectView(R.id.checkBoxArm)
    private CheckBox checkBoxArm;
    @InjectView(R.id.checkBoxLoad)
    private CheckBox checkBoxLoad;
    @InjectView(R.id.checkBoxTemp)
    private CheckBox checkBoxTemp;
    @InjectView(R.id.checkBoxRam)
    private CheckBox checkBoxRam;
    @InjectView(R.id.linLayoutUpdateInterval)
    private LinearLayout linLayoutUpdateInterval;

    private DeviceDbHelper deviceDbHelper;

    private int[] updateIntervalsMinutes;

    public OverclockingWidgetConfigureActivity() {
        super();
    }


    /**
     * @param context
     * @param appWidgetId
     * @param deviceId    ID of the chosen device
     */
    static void saveChosenDevicePref(Context context, int appWidgetId, Long deviceId, int updateInterval, boolean onlyOnWifi,
                                     boolean showTemp, boolean showArm, boolean showLoad, boolean showMemory) {
        LOGGER.info("Saving new OverclockingWidget. Settings - Update interval: {} - Only on Wifi: {}" +
                " - TEMP: {} - ARM: {} - LOAD: {} - RAM: {}", updateInterval, onlyOnWifi, showTemp, showArm, showLoad, showMemory);
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putLong(PREF_PREFIX_KEY + appWidgetId, deviceId);
        prefs.putInt(

                prefKey(PREF_UPDATE_INTERVAL_SUFFIX, appWidgetId), updateInterval

        );
        prefs.putBoolean(

                prefKey(PREF_SHOW_TEMP_SUFFIX, appWidgetId), showTemp

        );
        prefs.putBoolean(

                prefKey(PREF_SHOW_ARM_SUFFIX, appWidgetId), showArm

        );
        prefs.putBoolean(

                prefKey(PREF_SHOW_LOAD_SUFFIX, appWidgetId), showLoad

        );
        prefs.putBoolean(

                prefKey(PREF_SHOW_MEMORY_SUFFIX, appWidgetId), showMemory

        );
        prefs.putBoolean(

                prefKey(PREF_UPDATE_ONLY_ON_WIFI, appWidgetId), onlyOnWifi

        );
        prefs.apply();
    }

    static String prefKey(String key, int appWidgetId) {
        return PREF_PREFIX_KEY + appWidgetId + key;
    }

    static boolean loadShowStatus(Context context, String key, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(prefKey(key, appWidgetId), true);
    }

    static Long loadDeviceId(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Long deviceId = prefs.getLong(PREF_PREFIX_KEY + appWidgetId, 0);
        if (deviceId != 0) {
            return deviceId;
        } else {
            return null;
        }
    }

    static boolean loadOnlyOnWifi(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(prefKey(PREF_UPDATE_ONLY_ON_WIFI, appWidgetId), false);
    }

    static int loadUpdateInterval(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getInt(prefKey(PREF_UPDATE_INTERVAL_SUFFIX, appWidgetId), 5);
    }

    /**
     * @param appWidgetId the app widget id
     * @param context     the context
     * @return if this widget has associated preferences (it it fully configured then)
     */
    static boolean isInitiated(int appWidgetId, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.contains(PREF_PREFIX_KEY + appWidgetId);
    }

    static void deleteDevicePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.remove(prefKey(PREF_UPDATE_INTERVAL_SUFFIX, appWidgetId));
        prefs.remove(prefKey(PREF_SHOW_LOAD_SUFFIX, appWidgetId));
        prefs.remove(prefKey(PREF_SHOW_ARM_SUFFIX, appWidgetId));
        prefs.remove(prefKey(PREF_SHOW_TEMP_SUFFIX, appWidgetId));
        prefs.remove(prefKey(PREF_UPDATE_ONLY_ON_WIFI, appWidgetId));
        prefs.apply();
    }

    static void settingScheduledAlarm(Context context, int appWidgetId) {
        LOGGER.debug("Check if Widget[ID={}] needs a new Alarm.", appWidgetId);
        if (isInitiated(appWidgetId, context)) {
            int updateIntervalInMinutes = OverclockingWidgetConfigureActivity.loadUpdateInterval(context, appWidgetId);
            if (updateIntervalInMinutes > 0) {
                long updateIntervalMillis = updateIntervalInMinutes * 60 * 1000;
                // Setting alarm via AlarmManager
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                PendingIntent pintent = getServicePendingIntent(context, appWidgetId, getPendingIntentUri(appWidgetId), ACTION_WIDGET_UPDATE_ONE);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + updateIntervalMillis, updateIntervalMillis, pintent);
                LOGGER.debug("Added alarm for periodic updates of Widget[ID={}], update interval: {} ms.", appWidgetId, updateIntervalMillis);
            } else {
                LOGGER.debug("No periodic updates for Widget[ID={}].", appWidgetId);
            }
        } else {
            LOGGER.debug("Widget[ID={}] not fully initiated, no alarm needed.", appWidgetId);
        }
    }


    public static void removeAlarm(Context c, int appWidgetId) {
        LOGGER.debug("Removing alarm for Widget[ID={}].", appWidgetId);
        final Uri intentUri = getPendingIntentUri(appWidgetId);
        final PendingIntent pendingIntent = getServicePendingIntent(c, appWidgetId, intentUri, ACTION_WIDGET_UPDATE_ONE);
        final AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    protected static PendingIntent getServicePendingIntent(Context context, int appWidgetId, Uri uri, String action) {
        final Intent intent = new Intent(context, WidgetUpdateService.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(uri);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    protected static Uri getPendingIntentUri(int appWidgetId) {
        return Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/"), String.valueOf(appWidgetId));
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // init logging
        LoggingHelper.initLogging(OverclockingWidgetConfigureActivity.this);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        this.getSupportActionBar().setTitle(getString(R.string.widget_configure_title));
        deviceDbHelper = new DeviceDbHelper(this);
        final int deviceCount = initSpinners();
        if (deviceCount == 0) {
            // show Toast to add a device first
            Toast.makeText(this, getString(R.string.widget_add_no_device), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        linLayoutCustomInterval.setVisibility(View.GONE);
    }

    /**
     * @return the device count
     */
    private int initSpinners() {
        // Device Spinner
        final DeviceSpinnerAdapter deviceSpinnerAdapter = new DeviceSpinnerAdapter(OverclockingWidgetConfigureActivity.this, deviceDbHelper.getFullDeviceCursor(), true);
        widgetPiSpinner.setAdapter(deviceSpinnerAdapter);
        // Auto update
        final ArrayAdapter<CharSequence> autoUpdateAdapter = ArrayAdapter.createFromResource(OverclockingWidgetConfigureActivity.this, R.array.widget_auto_updates, android.R.layout.simple_spinner_item);
        autoUpdateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        widgetUpdateSpinner.setAdapter(autoUpdateAdapter);
        widgetUpdateSpinner.setOnItemSelectedListener(this);
        // Update interval
        this.updateIntervalsMinutes = this.getResources().getIntArray(R.array.widget_update_intervals_values);
        final ArrayAdapter<CharSequence> updateIntervalAdapter = ArrayAdapter.createFromResource(OverclockingWidgetConfigureActivity.this, R.array.widget_update_intervals, android.R.layout.simple_spinner_item);
        updateIntervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        widgetUpdateIntervalSpinner.setAdapter(updateIntervalAdapter);
        widgetUpdateIntervalSpinner.setOnItemSelectedListener(this);
        return deviceSpinnerAdapter.getCount();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                final Context context = OverclockingWidgetConfigureActivity.this;

                long selectedItemId = widgetPiSpinner.getSelectedItemId();
                LOGGER.info("Selected Device - Item ID = {}", selectedItemId);
                RaspberryDeviceBean deviceBean = deviceDbHelper.read(selectedItemId);
                if (deviceBean.usesAuthentificationMethod(RaspberryDeviceBean.AUTH_PUBLIC_KEY_WITH_PASSWORD) && deviceBean.getKeyfilePass() == null) {
                    Toast.makeText(context, getString(R.string.widget_key_pass_error), Toast.LENGTH_LONG).show();
                    return super.onOptionsItemSelected(item);
                }
                int updateIntervalInMinutes = 0;
                boolean onlyOnWifi = false;
                if (!autoUpdate[widgetUpdateSpinner.getSelectedItemPosition()].equals(UPDATE_NO)) {
                    if (updateIntervalsMinutes[widgetUpdateIntervalSpinner.getSelectedItemPosition()] == -1) {
                        String s = textEditUpdateInterval.getText().toString().trim();
                        if (Strings.isNullOrEmpty(s)) {
                            textEditUpdateInterval.setError(getString(R.string.widget_update_interval_error));
                            return super.onOptionsItemSelected(item);
                        }
                        updateIntervalInMinutes = Integer.parseInt(s);
                        if (updateIntervalInMinutes == 0) {
                            textEditUpdateInterval.setError(getString(R.string.widget_update_interval_zero));
                            return super.onOptionsItemSelected(item);
                        }
                    } else {
                        updateIntervalInMinutes = updateIntervalsMinutes[widgetUpdateIntervalSpinner.getSelectedItemPosition()];
                    }
                }
                if (autoUpdate[widgetUpdateSpinner.getSelectedItemPosition()].equals(UPDATE_WIFI)) {
                    onlyOnWifi = true;
                }
                // save Device ID in prefs
                saveChosenDevicePref(context, mAppWidgetId, selectedItemId, updateIntervalInMinutes, onlyOnWifi,
                        checkBoxTemp.isChecked(), checkBoxArm.isChecked(), checkBoxLoad.isChecked(), checkBoxRam.isChecked());

                settingScheduledAlarm(context, mAppWidgetId);

                // It is the responsibility of the configuration activity to update the app widget
                OverclockingWidget.updateAppWidget(context, mAppWidgetId, deviceDbHelper, false);

                // Make sure we pass back the original appWidgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        final int updateId = widgetUpdateSpinner.getId();
        final int updateIntervalId = widgetUpdateIntervalSpinner.getId();
        if (parent.getId() == updateId) {
            LOGGER.debug("Update-Spinner: Item pos {}, id {} selected.", position, id);
            if (autoUpdate[position].equals(UPDATE_NO)) {
                linLayoutUpdateInterval.setVisibility(View.GONE);
            } else {
                linLayoutUpdateInterval.setVisibility(View.VISIBLE);
            }
        } else if (parent.getId() == updateIntervalId) {
            LOGGER.debug("Interval-Spinner: Item pos {}, id {} selected.", position, id);
            int updateIntervalInMinutes = updateIntervalsMinutes[position];
            if (updateIntervalInMinutes == -1) {
                // custom time interval
                linLayoutCustomInterval.setVisibility(View.VISIBLE);
            } else {
                linLayoutCustomInterval.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // nothing to do
    }
}



