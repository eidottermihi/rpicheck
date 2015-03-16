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

    private static final Logger LOGGER = LoggerFactory.getLogger(OverclockingWidget.class);
    private static final String URI_SCHEME = "raspicheck";
    private static final String ACTION_WIDGET_UPDATE_ONE = "updateOneWidget";

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
            RaspberryDeviceBean deviceBean = deviceDb.read(deviceId);
            LOGGER.debug("Updating Widget[ID={}] for device {} - update interval: {} mins", appWidgetId, deviceBean.getName(), updateInterval);
            // Construct the RemoteViews object
            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.overclocking_widget);
            views.setOnClickPendingIntent(R.id.buttonRefresh, getSelfPendingIntent(context, appWidgetId));
            views.setTextViewText(R.id.textDeviceValue, deviceBean.getName());
            views.setTextViewText(R.id.textDeviceUserHost, String.format("%s@%s", deviceBean.getUser(), deviceBean.getHost()));
            final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                LOGGER.debug("Network available - querying device...");
                views.setTextViewText(R.id.textStatusValue, context.getString(R.string.widget_refreshing));
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
                            result.put("status", "online");
                            final VcgencmdBean vcgencmdBean = query.queryVcgencmd();
                            if (vcgencmdBean != null) {
                                result.put("temp", vcgencmdBean.getCpuTemperature() + "");
                                result.put("armFreq", vcgencmdBean.getArmFrequency() + "");
                            }
                            double loadAverage = query.queryLoadAverage(LoadAveragePeriod.FIVE_MINUTES);
                            result.put("loadAvg", loadAverage + "");
                            query.disconnect();
                        } catch (RaspiQueryException e) {
                            result.put("status", "offline");
                        }
                        return result;
                    }

                    @Override
                    protected void onPostExecute(Map<String, String> stringStringMap) {
                        super.onPostExecute(stringStringMap);
                        String status = stringStringMap.get("status");
                        views.setTextViewText(R.id.textStatusValue, status + " - " + new SimpleDateFormat("HH:mm").format(new Date()));
                        if ("online".equals(status)) {
                            String temp = stringStringMap.get("temp");
                            if (temp != null) {
                                views.setTextViewText(R.id.textTempValue, temp + "°C");
                                double tempValue = Double.parseDouble(temp);
                                updateProgressbar(views, R.id.progressBarTempValue, 0, 90, tempValue);
                            }
                            String armFreq = stringStringMap.get("armFreq");
                            if (armFreq != null) {
                                double armFreqDouble = Double.valueOf(armFreq) / 1000 / 1000;
                                views.setTextViewText(R.id.textArmValue, armFreqDouble + " MHz");
                                updateProgressbar(views, R.id.progressBarArmValue, 500, 1200, armFreqDouble);
                            }
                            String loadAvg = stringStringMap.get("loadAvg");
                            if (loadAvg != null) {
                                double loadAvgDouble = Double.parseDouble(loadAvg);
                                int progressValue = (int) (loadAvgDouble * 100);
                                views.setTextViewText(R.id.textLoadValue, progressValue + " %");
                                updateProgressbar(views, R.id.progressBarLoad, 0, 100, progressValue);
                            }
                        } else {
                            LOGGER.debug("Query failed, showing device as offline.");
                            views.setViewVisibility(R.id.textTempValue, View.GONE);
                            views.setViewVisibility(R.id.progressBarTempValue, View.GONE);
                            views.setViewVisibility(R.id.textArmValue, View.GONE);
                            views.setViewVisibility(R.id.progressBarArmValue, View.GONE);
                            views.setViewVisibility(R.id.textLoadValue, View.GONE);
                            views.setViewVisibility(R.id.progressBarLoad, View.GONE);
                        }
                        // Instruct the widget manager to update the widget
                        LOGGER.debug("Updating widget[ID={}] view.", appWidgetId);
                        appWidgetManager.updateAppWidget(appWidgetId, views);
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

    private static PendingIntent getSelfPendingIntent(Context context, int appWidgetId) {
        Intent i = new Intent(context, OverclockingWidget.class);
        i.setAction(ACTION_WIDGET_UPDATE_ONE);
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        Uri data = Uri.withAppendedPath(
                Uri.parse(URI_SCHEME + "://widget/id/")
                , String.valueOf(appWidgetId));
        i.setData(data);
        return PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
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
        LOGGER.debug("onReveice: action={}", action);
        if (action.equals(ACTION_WIDGET_UPDATE_ONE)) {
            int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            LOGGER.debug("AppWidgetID: {}", widgetId);
            if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                super.onReceive(context, intent);
            } else {
                this.onUpdate(context, AppWidgetManager.getInstance(context), new int[]{widgetId});
            }
        } else {
            super.onReceive(context, intent);
        }
    }
}


