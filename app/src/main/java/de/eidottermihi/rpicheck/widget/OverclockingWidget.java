/**
 * Copyright (C) 2016  RasPi Check Contributors
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

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RemoteViews;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.eidottermihi.raspicheck.R;
import de.eidottermihi.rpicheck.activity.SettingsActivity;
import de.eidottermihi.rpicheck.activity.helper.FormatHelper;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;
import de.eidottermihi.rpicheck.db.RaspberryDeviceBean;
import de.eidottermihi.rpicheck.ssh.IQueryService;
import de.eidottermihi.rpicheck.ssh.LoadAveragePeriod;
import de.eidottermihi.rpicheck.ssh.beans.RaspiMemoryBean;
import de.eidottermihi.rpicheck.ssh.beans.VcgencmdBean;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQuery;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link OverclockingWidgetConfigureActivity OverclockingWidgetConfigureActivity}
 */
public class OverclockingWidget extends AppWidgetProvider {

    public static final String ACTION_WIDGET_UPDATE_ONE = "updateOneWidget";
    public static final String STATUS = "status";
    public static final String STATUS_ONLINE = "online";
    public static final String STATUS_OFFLINE = "offline";
    public static final String KEY_TEMP = "temp";
    public static final String KEY_ARM_FREQ = "armFreq";
    public static final String KEY_LOAD_AVG = "loadAvg";
    public static final String KEY_MEM_USED = "memUsed";
    public static final String KEY_MEM_TOTAL = "memTotal";
    public static final String KEY_MEM_USED_PERCENT = "memUsedPercent";
    private static final Logger LOGGER = LoggerFactory.getLogger(OverclockingWidget.class);
    private static final String URI_SCHEME = "raspicheck";
    private static final String ACTION_WIDGET_UPDATE_ONE_MANUAL = "updateOneWidgetManual";

    private DeviceDbHelper deviceDb;

    private static void connect(IQueryService raspiQuery, RaspberryDeviceBean deviceBean) throws RaspiQueryException {
        if (deviceBean.usesAuthentificationMethod(RaspberryDeviceBean.AUTH_PASSWORD)) {
            raspiQuery.connect(deviceBean.getPass());
        } else if (deviceBean.usesAuthentificationMethod(RaspberryDeviceBean.AUTH_PUBLIC_KEY)) {
            raspiQuery.connectWithPubKeyAuth(deviceBean.getKeyfilePath());
        } else if (deviceBean.usesAuthentificationMethod(RaspberryDeviceBean.AUTH_PUBLIC_KEY_WITH_PASSWORD)) {
            raspiQuery.connectWithPubKeyAuthAndPassphrase(deviceBean.getKeyfilePath(), deviceBean.getKeyfilePass());
        } else {
            LOGGER.error("Unknown authentification combination!");
        }
    }

    static void updateAppWidget(Context context, final AppWidgetManager appWidgetManager,
                                final int appWidgetId, DeviceDbHelper deviceDb, boolean initByAlarm) {
        LOGGER.debug("Updating Widget[ID={}]. initByAlarm = {}", appWidgetId, initByAlarm);
        Long deviceId = OverclockingWidgetConfigureActivity.loadDeviceId(context, appWidgetId);
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
                final RemoteViews removedView = new RemoteViews(context.getPackageName(), R.layout.overclocking_widget_no_device);
                appWidgetManager.updateAppWidget(appWidgetId, removedView);
                return;
            }
            if (deviceBean.usesAuthentificationMethod(RaspberryDeviceBean.AUTH_PUBLIC_KEY) || deviceBean.usesAuthentificationMethod(RaspberryDeviceBean.AUTH_PUBLIC_KEY_WITH_PASSWORD)) {
                // need permission to read private key file
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    LOGGER.error("Skipping widget update process because permission to read private key file is not granted. Showing alternate view.");
                    final RemoteViews removedView = new RemoteViews(context.getPackageName(), R.layout.overclocking_widget_no_read_permission);
                    appWidgetManager.updateAppWidget(appWidgetId, removedView);
                    return;
                }
            }
            // Construct the RemoteViews object
            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.overclocking_widget);
            views.setOnClickPendingIntent(R.id.buttonRefresh, getSelfPendingIntent(context, appWidgetId, ACTION_WIDGET_UPDATE_ONE_MANUAL));
            views.setTextViewText(R.id.textDeviceValue, deviceBean.getName());
            views.setTextViewText(R.id.textDeviceUserHost, String.format("%s@%s", deviceBean.getUser(), deviceBean.getHost()));
            views.setViewVisibility(R.id.linLayoutTemp, showTemp ? View.VISIBLE : View.GONE);
            views.setViewVisibility(R.id.linLayoutArm, showArm ? View.VISIBLE : View.GONE);
            views.setViewVisibility(R.id.linLayoutLoad, showLoad ? View.VISIBLE : View.GONE);
            views.setViewVisibility(R.id.linLayoutMem, showMemory ? View.VISIBLE : View.GONE);
            appWidgetManager.updateAppWidget(appWidgetId, views);
            if (shouldDoQuery(context, initByAlarm, onlyOnWlan)) {
                LOGGER.debug("Querying for Widget[ID={}]...", appWidgetId);
                views.setViewVisibility(R.id.textStatusValue, View.GONE);
                views.setViewVisibility(R.id.buttonRefresh, View.GONE);
                views.setViewVisibility(R.id.refreshProgressBar, View.VISIBLE);
                appWidgetManager.updateAppWidget(appWidgetId, views);
                // query in AsyncTask
                new AsyncTask<RaspberryDeviceBean, Void, Map<String, String>>() {
                    @Override
                    protected Map<String, String> doInBackground(RaspberryDeviceBean... params) {
                        final Map<String, String> result = new HashMap<>();
                        RaspberryDeviceBean deviceBean = params[0];
                        IQueryService query = new RaspiQuery(deviceBean.getHost(), deviceBean.getUser(), deviceBean.getPort());
                        try {
                            connect(query, deviceBean);
                            result.put(STATUS, STATUS_ONLINE);
                            if (showArm || showTemp) {
                                final VcgencmdBean vcgencmdBean = query.queryVcgencmd();
                                if (vcgencmdBean != null) {
                                    result.put(KEY_TEMP, vcgencmdBean.getCpuTemperature() + "");
                                    result.put(KEY_ARM_FREQ, vcgencmdBean.getArmFrequency() + "");
                                }
                            }
                            if (showMemory) {
                                final RaspiMemoryBean memoryBean = query.queryMemoryInformation();
                                if (memoryBean != null && memoryBean.getErrorMessage() == null) {
                                    result.put(KEY_MEM_USED, memoryBean.getTotalUsed().humanReadableByteCount(false));
                                    result.put(KEY_MEM_TOTAL, memoryBean.getTotalMemory().humanReadableByteCount(false));
                                    result.put(KEY_MEM_USED_PERCENT, String.valueOf(memoryBean.getPercentageUsed()));
                                }
                            }
                            if (showLoad) {
                                double loadAverage = query.queryLoadAverage(LoadAveragePeriod.FIVE_MINUTES);
                                result.put(KEY_LOAD_AVG, String.valueOf(loadAverage));
                            }
                        } catch (RaspiQueryException e) {
                            LOGGER.info("Widget update failed, setting device status offline.", e);
                            result.put(STATUS, STATUS_OFFLINE);
                        } finally {
                            try {
                                query.disconnect();
                            } catch (RaspiQueryException e) {
                                LOGGER.debug("Error occ closing the ssh client.", e);
                            }
                        }
                        return result;
                    }

                    @Override
                    protected void onPostExecute(Map<String, String> stringStringMap) {
                        super.onPostExecute(stringStringMap);
                        String status = stringStringMap.get(STATUS);
                        views.setTextViewText(R.id.textStatusValue, status + " - " + new SimpleDateFormat("HH:mm").format(new Date()));
                        views.setViewVisibility(R.id.textStatusValue, View.VISIBLE);
                        views.setViewVisibility(R.id.buttonRefresh, View.VISIBLE);
                        views.setViewVisibility(R.id.refreshProgressBar, View.GONE);
                        if (STATUS_ONLINE.equals(status)) {
                            String temp = stringStringMap.get(KEY_TEMP);
                            if (temp != null) {
                                double tempValueInCelsius = Double.parseDouble(temp);
                                String tempString = FormatHelper.formatTemperature(tempValueInCelsius, (useFahrenheit ? FormatHelper.SCALE_FAHRENHEIT : FormatHelper.SCALE_CELSIUS));
                                double tempValue = (useFahrenheit) ? FormatHelper.celsiusToFahrenheit(tempValueInCelsius) : tempValueInCelsius;
                                views.setTextViewText(R.id.textTempValue, tempString);
                                double minVal = (useFahrenheit) ? FormatHelper.celsiusToFahrenheit(0.0) : 0.0;
                                double maxVal = (useFahrenheit) ? FormatHelper.celsiusToFahrenheit(90.0) : 90.0;
                                updateProgressbar(views, R.id.progressBarTempValue, minVal, maxVal, tempValue);
                            }
                            String armFreq = stringStringMap.get(KEY_ARM_FREQ);
                            if (armFreq != null) {
                                long armFreqHz = Long.valueOf(armFreq);
                                double armFreqDoubleMhz = Double.valueOf(armFreq) / 1000 / 1000;
                                views.setTextViewText(R.id.textArmValue, FormatHelper.formatFrequency(armFreqHz, FormatHelper.SCALE_MHZ));
                                updateProgressbar(views, R.id.progressBarArmValue, 500, 1200, armFreqDoubleMhz);
                            }
                            String loadAvg = stringStringMap.get(KEY_LOAD_AVG);
                            if (loadAvg != null) {
                                double loadAvgDouble = Double.parseDouble(loadAvg);
                                int progressValue = (int) (loadAvgDouble * 100);
                                views.setTextViewText(R.id.textLoadValue, FormatHelper.formatPercentage(progressValue));
                                updateProgressbar(views, R.id.progressBarLoad, 0, 100, progressValue);
                            }
                            String memUsedPercent = stringStringMap.get(KEY_MEM_USED_PERCENT);
                            if (memUsedPercent != null) {
                                updateMemory(memUsedPercent, views);
                            }
                        } else {
                            LOGGER.debug("Query failed, showing device as offline.");
                        }
                        // Instruct the widget manager to update the widget
                        LOGGER.debug("Updating widget[ID={}] view.", appWidgetId);
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    }

                    private void updateMemory(String memUsedPercent, RemoteViews views) {
                        int memUsedPercentInt = (int) (Double.parseDouble(memUsedPercent) * 100);
                        views.setTextViewText(R.id.textMemoryValue, FormatHelper.formatPercentage(memUsedPercentInt));
                        updateProgressbar(views, R.id.progressBarMemory, 0, 100, memUsedPercentInt);
                    }

                    private void updateProgressbar(RemoteViews views, int progressBarId, double min, double max, double value) {
                        LOGGER.debug("min: {}  max: {} value: {}", min, max, value);
                        if (value > max) {
                            value = max;
                        } else if (value < min) {
                            value = min;
                        }
                        double scaledValue = ((value - min) / (max - min)) * 100;
                        LOGGER.debug("Updating progressbar: scaledValue = {}", scaledValue);
                        views.setProgressBar(progressBarId, 100, (int) scaledValue, false);
                    }
                }.execute(deviceBean);
            } else {
                LOGGER.debug("Can't update widget[id={}] - resetting widget view.", appWidgetId);
                views.setViewVisibility(R.id.buttonRefresh, View.VISIBLE);
                views.setViewVisibility(R.id.refreshProgressBar, View.GONE);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
    }

    private static boolean shouldDoQuery(Context context, boolean initByAlarm, boolean onlyOnWlan) {
        LOGGER.debug("Checking if update should be performed: initByAlarm={} onlyOnWlan={}", initByAlarm, onlyOnWlan);
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        final NetworkInfo wlanInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean doQuery = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            LOGGER.debug("Network is available and connected.");
            if (initByAlarm) {
                if (onlyOnWlan) {
                    if (wlanInfo != null && wlanInfo.isConnected()) {
                        LOGGER.debug("WiFi is connected");
                        doQuery = true;
                    }
                } else {
                    doQuery = true;
                }
            } else {
                doQuery = true;
            }
        } else {
            LOGGER.debug("No network available - skipping widget update process.");
        }
        LOGGER.debug("Initiate update? {}", doQuery);
        return doQuery;
    }

    protected static PendingIntent getSelfPendingIntent(Context context, int appWidgetId, String action) {
        final Uri data = getPendingIntentUri(appWidgetId);
        return getSelfPendingIntent(context, appWidgetId, data, action);
    }

    protected static PendingIntent getSelfPendingIntent(Context context, int appWidgetId, Uri uri, String action) {
        final Intent intent = new Intent(context, OverclockingWidget.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(uri);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    protected static Uri getPendingIntentUri(int appWidgetId) {
        return Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/"), String.valueOf(appWidgetId));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (this.deviceDb == null) {
            this.deviceDb = new DeviceDbHelper(context);
        }
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i], deviceDb, false);
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
            removeAlarm(context, appWidgetIds[i]);
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
        String action = intent.getAction();
        LOGGER.debug("Receiving Intent: action={}", action);
        if (action.equals(ACTION_WIDGET_UPDATE_ONE_MANUAL) || action.equals(ACTION_WIDGET_UPDATE_ONE)) {
            int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                super.onReceive(context, intent);
            } else {
                if (this.deviceDb == null) {
                    this.deviceDb = new DeviceDbHelper(context);
                }
                updateAppWidget(context, AppWidgetManager.getInstance(context), widgetId, this.deviceDb, action.equals(ACTION_WIDGET_UPDATE_ONE) ? true : false);
            }
        } else {
            super.onReceive(context, intent);
        }
    }

    private void removeAlarm(Context c, int appWidgetId) {
        LOGGER.debug("Removing alarm for Widget[ID={}].", appWidgetId);
        final Uri intentUri = getPendingIntentUri(appWidgetId);
        final PendingIntent pendingIntent = getSelfPendingIntent(c, appWidgetId, intentUri, ACTION_WIDGET_UPDATE_ONE);
        final AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}


