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
package de.eidottermihi.raspicheck;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.eidottermihi.rpicheck.activity.NewRaspiAuthActivity;
import de.eidottermihi.rpicheck.activity.helper.FormatHelper;
import de.eidottermihi.rpicheck.beans.RaspiMemoryBean;
import de.eidottermihi.rpicheck.beans.VcgencmdBean;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;
import de.eidottermihi.rpicheck.db.RaspberryDeviceBean;
import de.eidottermihi.rpicheck.ssh.IQueryService;
import de.eidottermihi.rpicheck.ssh.LoadAveragePeriod;
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
    /**
     * Place to store URIs which are used for update via AlarmManager
     */
    private static HashMap<Integer, Uri> uris = new HashMap<Integer, Uri>();

    private DeviceDbHelper deviceDb;

    private static void connect(IQueryService raspiQuery, RaspberryDeviceBean deviceBean) throws RaspiQueryException {
        if (deviceBean.getAuthMethod().equals(NewRaspiAuthActivity.AUTH_PASSWORD)) {
            raspiQuery.connect(deviceBean.getPass());
        } else if (deviceBean.getAuthMethod().equals(NewRaspiAuthActivity.AUTH_PUBLIC_KEY)) {
            raspiQuery.connectWithPubKeyAuth(deviceBean.getKeyfilePath());
        } else if (deviceBean.getAuthMethod().equals(NewRaspiAuthActivity.AUTH_PUBLIC_KEY_WITH_PASSWORD)) {
            raspiQuery.connectWithPubKeyAuthAndPassphrase(deviceBean.getKeyfilePath(), deviceBean.getKeyfilePass());
        } else {
            LOGGER.error("Unknown authentification combination!");
        }
    }

    static void updateAppWidget(Context context, final AppWidgetManager appWidgetManager,
                                final int appWidgetId, DeviceDbHelper deviceDb) {
        Long deviceId = OverclockingWidgetConfigureActivity.loadDeviceId(context, appWidgetId);
        if (deviceId != null) {
            // get update interval
            Integer updateInterval = OverclockingWidgetConfigureActivity.loadUpdateInterval(context, appWidgetId);
            final boolean showTemp = OverclockingWidgetConfigureActivity.loadShowStatus(context, OverclockingWidgetConfigureActivity.PREF_SHOW_TEMP_SUFFIX, appWidgetId);
            final boolean showArm = OverclockingWidgetConfigureActivity.loadShowStatus(context, OverclockingWidgetConfigureActivity.PREF_SHOW_ARM_SUFFIX, appWidgetId);
            final boolean showLoad = OverclockingWidgetConfigureActivity.loadShowStatus(context, OverclockingWidgetConfigureActivity.PREF_SHOW_LOAD_SUFFIX, appWidgetId);
            final boolean showMemory = OverclockingWidgetConfigureActivity.loadShowStatus(context, OverclockingWidgetConfigureActivity.PREF_SHOW_MEMORY_SUFFIX, appWidgetId);
            RaspberryDeviceBean deviceBean = deviceDb.read(deviceId);
            if (deviceBean == null) {
                // device has been deleted
                LOGGER.debug("Device has been deleted, showing alternate view with message.");
                final RemoteViews removedView = new RemoteViews(context.getPackageName(), R.layout.overclocking_widget_no_device);
                appWidgetManager.updateAppWidget(appWidgetId, removedView);
                return;
            }
            LOGGER.debug("Updating Widget[ID={}] for device {} - update interval: {} mins", appWidgetId, deviceBean.getName(), updateInterval);
            // Construct the RemoteViews object
            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.overclocking_widget);
            views.setOnClickPendingIntent(R.id.buttonRefresh, getSelfPendingIntent(context, appWidgetId, ACTION_WIDGET_UPDATE_ONE_MANUAL));
            views.setTextViewText(R.id.textDeviceValue, deviceBean.getName());
            views.setTextViewText(R.id.textDeviceUserHost, String.format("%s@%s", deviceBean.getUser(), deviceBean.getHost()));
            views.setViewVisibility(R.id.linLayoutTemp, showTemp ? View.VISIBLE : View.GONE);
            views.setViewVisibility(R.id.linLayoutArm, showArm ? View.VISIBLE : View.GONE);
            views.setViewVisibility(R.id.linLayoutLoad, showLoad ? View.VISIBLE : View.GONE);
            views.setViewVisibility(R.id.linLayoutMem, showLoad ? View.VISIBLE : View.GONE);
            final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                LOGGER.debug("Network available - querying device...");
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
                            query.disconnect();
                        } catch (RaspiQueryException e) {
                            result.put(STATUS, STATUS_OFFLINE);
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
                                double tempValue = Double.parseDouble(temp);
                                views.setTextViewText(R.id.textTempValue, FormatHelper.formatTemperature(tempValue, FormatHelper.SCALE_CELSIUS));
                                updateProgressbar(views, R.id.progressBarTempValue, 0, 90, tempValue);
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
                LOGGER.debug("No network available - skipping widget update process.");
            }
        }
    }

    protected static PendingIntent getSelfPendingIntent(Context context, int appWidgetId, String action) {
        Uri data = getPendingIntentUri(appWidgetId);
        return getSelfPendingIntent(context, appWidgetId, data, action);
    }

    protected static PendingIntent getSelfPendingIntent(Context context, int appWidgetId, Uri uri, String action) {
        final Intent intent = new Intent(context, OverclockingWidget.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(uri);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getConfigurationPendingIntent(Context context, int appWidgetId){
        final Intent intent = new Intent(context, OverclockingWidgetConfigureActivity.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(getPendingIntentUri(appWidgetId));
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    protected static Uri getPendingIntentUri(int appWidgetId) {
        return Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/"), String.valueOf(appWidgetId));
    }

    public static void addUri(int appWidgetId, Uri uri) {
        uris.put(appWidgetId, uri);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        deviceDb = new DeviceDbHelper(context);
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i], deviceDb);
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
        // Enter relevant functionality for when the first widget is created
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
                this.onUpdate(context, AppWidgetManager.getInstance(context), new int[]{widgetId});
            }
        } else {
            super.onReceive(context, intent);
        }
    }

    private void removeAlarm(Context c, int appWidgetId) {
        Uri removedUri = uris.remove(appWidgetId);
        if (removedUri != null) {
            LOGGER.debug("Removing alarm for Widget[ID={}].", appWidgetId);
            PendingIntent pendingIntent = getSelfPendingIntent(c, appWidgetId, removedUri, ACTION_WIDGET_UPDATE_ONE);
            AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
        } else {
            LOGGER.debug("No alarm intent uri for Widget[ID={}] in Map.", appWidgetId);
        }
    }
}


