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

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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

    private DeviceDbHelper deviceDb;

    static void updateAppWidget(Context context, final AppWidgetManager appWidgetManager,
                                final int appWidgetId, DeviceDbHelper deviceDb) {
        Long deviceId = OverclockingWidgetConfigureActivity.loadDeviceId(context, appWidgetId);
        if (deviceId != null) {
            RaspberryDeviceBean deviceBean = deviceDb.read(deviceId);
            LOGGER.debug("Updating widgetId {} for device {}", appWidgetId, deviceBean.getName());
            // Construct the RemoteViews object
            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.overclocking_widget);
            views.setTextViewText(R.id.textDeviceValue, deviceBean.getName());
            views.setTextViewText(R.id.textDeviceUserHost, String.format("%s@%s", deviceBean.getUser(), deviceBean.getHost()));
            final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                LOGGER.debug("Network available - querying device...");
                views.setTextViewText(R.id.textStatusValue, "refreshing...");
                views.setViewVisibility(R.id.relLayoutData, View.GONE);
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
                        views.setViewVisibility(R.id.relLayoutData, View.VISIBLE);
                        if ("online".equals(status)) {
                            String temp = stringStringMap.get("temp");
                            if (temp != null) {
                                views.setTextViewText(R.id.textTempValue, temp + "°C");
                                double tempDouble = Double.valueOf(temp);
                                if (tempDouble > 90.0) {
                                    tempDouble = 90.0;
                                }
                                views.setProgressBar(R.id.progressBarTempValue, 90, (int) (tempDouble), false);
                            }
                            String armFreq = stringStringMap.get("armFreq");
                            if (armFreq != null) {
                                double armFreqDouble = Double.valueOf(armFreq) / 1000 / 1000;
                                views.setTextViewText(R.id.textArmValue, armFreqDouble + " MHz");
                                double min = 500.0;
                                double max = 1200.0;
                                double value = armFreqDouble;
                                if (value > max) {
                                    value = max;
                                } else if (value < min) {
                                    value = min;
                                }
                                int progressValue = (int) (((value - min) / (max - min)) * 100);
                                views.setProgressBar(R.id.progressBarArmValue, 100, progressValue, false);
                            }
                            String loadAvg = stringStringMap.get("loadAvg");
                            if (loadAvg != null) {
                                views.setTextViewText(R.id.textLoadValue, loadAvg + " %");
                                double loadAvgDouble = Double.parseDouble(loadAvg);
                                if (loadAvgDouble > 1.0) {
                                    loadAvgDouble = 1.0;
                                }
                                int progressValue = (int) (loadAvgDouble * 100);
                                views.setProgressBar(R.id.progressBarLoad, 100, progressValue, false);
                            }
                        } else {
                            views.setViewVisibility(R.id.textTempValue, View.GONE);
                            views.setViewVisibility(R.id.progressBarTempValue, View.GONE);
                            views.setViewVisibility(R.id.textArmValue, View.GONE);
                            views.setViewVisibility(R.id.progressBarArmValue, View.GONE);
                            views.setViewVisibility(R.id.textLoadValue, View.GONE);
                            views.setViewVisibility(R.id.progressBarLoad, View.GONE);
                        }
                        // Instruct the widget manager to update the widget
                        LOGGER.debug("Query performed - updating widget data.");
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    }
                }.execute(deviceBean);
            } else {
                LOGGER.debug("No network available - skipping widget update process.");
            }
        }
    }

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
}


