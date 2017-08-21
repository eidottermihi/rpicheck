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

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.RemoteViews;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.eidottermihi.raspicheck.R;
import de.eidottermihi.rpicheck.activity.helper.FormatHelper;
import de.eidottermihi.rpicheck.db.RaspberryDeviceBean;
import de.eidottermihi.rpicheck.ssh.IQueryService;
import de.eidottermihi.rpicheck.ssh.LoadAveragePeriod;
import de.eidottermihi.rpicheck.ssh.beans.RaspiMemoryBean;
import de.eidottermihi.rpicheck.ssh.beans.VcgencmdBean;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQuery;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;

/**
 * @author Michael
 */
public class WidgetUpdateTask extends AsyncTask<RaspberryDeviceBean, Void, Map<String, String>> {

    public static final String STATUS = "status";
    public static final String STATUS_ONLINE = "online";
    public static final String STATUS_OFFLINE = "offline";
    public static final String KEY_TEMP = "temp";
    public static final String KEY_ARM_FREQ = "armFreq";
    public static final String KEY_LOAD_AVG = "loadAvg";
    public static final String KEY_MEM_USED = "memUsed";
    public static final String KEY_MEM_TOTAL = "memTotal";
    public static final String KEY_MEM_USED_PERCENT = "memUsedPercent";

    private static final Logger LOGGER = LoggerFactory.getLogger(WidgetUpdateTask.class);

    private Context context;
    private RemoteViews widgetView;
    private boolean showArm;
    private boolean showTemp;
    private boolean showMemory;
    private boolean showLoad;
    private boolean useFahrenheit;
    private int appWidgetId;

    public WidgetUpdateTask(Context context, RemoteViews widgetView, boolean showArm, boolean showTemp, boolean showMemory, boolean showLoad, boolean useFahrenheit, int appWidgetId) {
        this.context = context;
        this.widgetView = widgetView;
        this.showArm = showArm;
        this.showTemp = showTemp;
        this.showMemory = showMemory;
        this.showLoad = showLoad;
        this.useFahrenheit = useFahrenheit;
        this.appWidgetId = appWidgetId;
    }

    private void connect(IQueryService raspiQuery, RaspberryDeviceBean deviceBean) throws RaspiQueryException {
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
                LOGGER.debug("Error closing the ssh client.", e);
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(Map<String, String> stringStringMap) {
        String status = stringStringMap.get(STATUS);
        widgetView.setTextViewText(R.id.textStatusValue, status + " - " + SimpleDateFormat.getTimeInstance(DateFormat.SHORT).format(new Date()));
        widgetView.setViewVisibility(R.id.textStatusValue, View.VISIBLE);
        widgetView.setViewVisibility(R.id.buttonRefresh, View.VISIBLE);
        widgetView.setViewVisibility(R.id.refreshProgressBar, View.GONE);
        if (STATUS_ONLINE.equals(status)) {
            String temp = stringStringMap.get(KEY_TEMP);
            if (temp != null) {
                double tempValueInCelsius = Double.parseDouble(temp);
                String tempString = FormatHelper.formatTemperature(tempValueInCelsius, (useFahrenheit ? FormatHelper.SCALE_FAHRENHEIT : FormatHelper.SCALE_CELSIUS));
                double tempValue = (useFahrenheit) ? FormatHelper.celsiusToFahrenheit(tempValueInCelsius) : tempValueInCelsius;
                widgetView.setTextViewText(R.id.textTempValue, tempString);
                double minVal = (useFahrenheit) ? FormatHelper.celsiusToFahrenheit(0.0) : 0.0;
                double maxVal = (useFahrenheit) ? FormatHelper.celsiusToFahrenheit(90.0) : 90.0;
                updateProgressbar(widgetView, R.id.progressBarTempValue, minVal, maxVal, tempValue);
            }
            String armFreq = stringStringMap.get(KEY_ARM_FREQ);
            if (armFreq != null) {
                long armFreqHz = Long.valueOf(armFreq);
                double armFreqDoubleMhz = Double.valueOf(armFreq) / 1000 / 1000;
                widgetView.setTextViewText(R.id.textArmValue, FormatHelper.formatFrequency(armFreqHz, FormatHelper.SCALE_MHZ));
                updateProgressbar(widgetView, R.id.progressBarArmValue, 500, 1200, armFreqDoubleMhz);
            }
            String loadAvg = stringStringMap.get(KEY_LOAD_AVG);
            if (loadAvg != null) {
                double loadAvgDouble = Double.parseDouble(loadAvg);
                int progressValue = (int) (loadAvgDouble * 100);
                widgetView.setTextViewText(R.id.textLoadValue, FormatHelper.formatPercentage(progressValue));
                updateProgressbar(widgetView, R.id.progressBarLoad, 0, 100, progressValue);
            }
            String memUsedPercent = stringStringMap.get(KEY_MEM_USED_PERCENT);
            if (memUsedPercent != null) {
                updateMemory(memUsedPercent, widgetView);
            }
        } else {
            LOGGER.debug("Query failed, showing device as offline.");
        }
        // Instruct the widget manager to update the widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        LOGGER.debug("Updating widget[ID={}] view after AsyncTask finished.", appWidgetId);
        appWidgetManager.updateAppWidget(appWidgetId, widgetView);
    }

    private void updateMemory(String memUsedPercent, RemoteViews views) {
        int memUsedPercentInt = (int) (Double.parseDouble(memUsedPercent) * 100);
        views.setTextViewText(R.id.textMemoryValue, FormatHelper.formatPercentage(memUsedPercentInt));
        updateProgressbar(views, R.id.progressBarMemory, 0, 100, memUsedPercentInt);
    }

    private void updateProgressbar(RemoteViews views, int progressBarId, double min, double max, double value) {
        if (value > max) {
            value = max;
        } else if (value < min) {
            value = min;
        }
        double scaledValue = ((value - min) / (max - min)) * 100;
        LOGGER.debug("Updating progressbar[id={}]: scaledValue = {}", progressBarId, scaledValue);
        views.setProgressBar(progressBarId, 100, (int) scaledValue, false);
    }
}
