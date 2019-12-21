/**
 * MIT License
 *
 * Copyright (c) 2019  RasPi Check Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.eidottermihi.rpicheck.widget;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.eidottermihi.rpicheck.activity.SettingsActivity;
import de.eidottermihi.rpicheck.activity.helper.FormatHelper;
import de.eidottermihi.rpicheck.activity.helper.LoggingHelper;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;
import de.eidottermihi.rpicheck.db.RaspberryDeviceBean;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link OverclockingWidgetConfigureActivity OverclockingWidgetConfigureActivity}
 */
public class OverclockingWidget extends AppWidgetProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(OverclockingWidget.class);
    private static final String ACTION_WIDGET_UPDATE_ONE_MANUAL = "updateOneWidgetManual";

    private DeviceDbHelper deviceDb;


    static void updateAppWidget(Context context, final int appWidgetId, DeviceDbHelper deviceDb, boolean triggeredByAlarm) {
        LOGGER.debug("Starting refresh of Widget[ID={}].", appWidgetId);
        Long deviceId = OverclockingWidgetConfigureActivity.loadDeviceId(context, appWidgetId);
        LOGGER.debug("Refreshing widget for device[ID={}]", deviceId);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String preferredTempScale = sharedPreferences.getString(SettingsActivity.KEY_PREF_TEMPERATURE_SCALE, FormatHelper.SCALE_CELSIUS);
        final boolean useFahrenheit = preferredTempScale.equals(FormatHelper.SCALE_FAHRENHEIT);
        LOGGER.debug("Using temperature scale: {}", preferredTempScale);
        if (deviceId != null) {
            // get update interval
            final boolean showTemp = OverclockingWidgetConfigureActivity.loadShowStatus(context, OverclockingWidgetConfigureActivity.PREF_SHOW_TEMP_SUFFIX, appWidgetId);
            final boolean showArm = OverclockingWidgetConfigureActivity.loadShowStatus(context, OverclockingWidgetConfigureActivity.PREF_SHOW_ARM_SUFFIX, appWidgetId);
            final boolean showLoad = OverclockingWidgetConfigureActivity.loadShowStatus(context, OverclockingWidgetConfigureActivity.PREF_SHOW_LOAD_SUFFIX, appWidgetId);
            final boolean showMemory = OverclockingWidgetConfigureActivity.loadShowStatus(context, OverclockingWidgetConfigureActivity.PREF_SHOW_MEMORY_SUFFIX, appWidgetId);
            final boolean onlyOnWlan = OverclockingWidgetConfigureActivity.loadOnlyOnWifi(context, appWidgetId);
            RaspberryDeviceBean deviceBean = deviceDb.read(deviceId);
            if (deviceBean == null) {
                // device has been deleted
                LOGGER.debug("Device has been deleted, showing alternate view with message.");
                OverclockingWidgetView.initRemovedView(context, appWidgetId);
                return;
            }
            if (deviceBean.usesAuthentificationMethod(RaspberryDeviceBean.AUTH_PUBLIC_KEY) || deviceBean.usesAuthentificationMethod(RaspberryDeviceBean.AUTH_PUBLIC_KEY_WITH_PASSWORD)) {
                // need permission to read private key file
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    LOGGER.error("Skipping widget update process because permission to read private key file is not granted. Showing alternate view.");
                    OverclockingWidgetView.initNoPermissionView(context, appWidgetId);
                    return;
                }
            }
            // Construct the RemoteViews object
            final RemoteViews views = OverclockingWidgetView.initDefaultView(context, appWidgetId, deviceBean, showTemp, showArm, showLoad, showMemory);
            if (isNetworkAvailable(context)) {
                if (!triggeredByAlarm || !(onlyOnWlan && !isWiFiAvailable(context))) {
                    LOGGER.debug("Starting async update task for Widget[ID={}]...", appWidgetId);
                    OverclockingWidgetView.startRefreshing(views, context, appWidgetId);
                    // query in AsyncTask
                    new WidgetUpdateTask(context, views, showArm, showTemp, showMemory, showLoad, useFahrenheit, appWidgetId).execute(deviceBean);
                } else {
                    LOGGER.debug("Skipping update - no WiFi connected.");
                }
            } else {
                LOGGER.debug("No network for update of Widget[ID={}] - resetting widget view.", appWidgetId);
                OverclockingWidgetView.stopRefreshing(views, context, appWidgetId);
            }
        } else {
            LOGGER.warn("No device with id={} present in database!", deviceId);
        }
    }

    private static boolean isNetworkAvailable(Context context) {
        LOGGER.debug("Checking if network is available");
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            LOGGER.debug("Network is available and connected.");
            return true;
        }
        return false;
    }

    private static boolean isWiFiAvailable(Context context) {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiInfo != null && wifiInfo.isConnected();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (this.deviceDb == null) {
            this.deviceDb = new DeviceDbHelper(context);
        }
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetId, deviceDb, false);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        // handle hiding/showing of information based on available screen size here...
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            OverclockingWidgetConfigureActivity.deleteDevicePref(context, appWidgetIds[i]);
            OverclockingWidgetConfigureActivity.removeAlarm(context, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), OverclockingWidget.class.getName());
        int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(thisAppWidget);

        for (int appWidgetId : appWidgetIds) {
            // Restoring scheduled alarm after reboot for every widget
            OverclockingWidgetConfigureActivity.settingScheduledAlarm(context, appWidgetId);
        }
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LoggingHelper.initLogging(context);
        String action = intent.getAction();
        LOGGER.debug("Receiving Intent: action={}", action);
        if (action.equals(ACTION_WIDGET_UPDATE_ONE_MANUAL)) {
            int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                super.onReceive(context, intent);
            } else {
                if (this.deviceDb == null) {
                    this.deviceDb = new DeviceDbHelper(context);
                }
                updateAppWidget(context, widgetId, this.deviceDb, false);
            }
        } else {
            super.onReceive(context, intent);
        }
    }


}


