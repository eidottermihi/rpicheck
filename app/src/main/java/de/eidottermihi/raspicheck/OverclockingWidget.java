package de.eidottermihi.raspicheck;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import de.eidottermihi.rpicheck.beans.VcgencmdBean;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;
import de.eidottermihi.rpicheck.db.RaspberryDeviceBean;
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
            // Construct the RemoteViews object
            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.overclocking_widget);
            views.setTextViewText(R.id.textDeviceValue, String.format("%s - %s@%s", deviceBean.getName(), deviceBean.getUser(), deviceBean.getHost()));

            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                LOGGER.debug("Updating widget - ID {}", appWidgetId);
                new AsyncTask<RaspberryDeviceBean, Void, Map<String, String>>() {
                    @Override
                    protected Map<String, String> doInBackground(RaspberryDeviceBean... params) {
                        final Map<String, String> result = new HashMap<>();
                        RaspberryDeviceBean deviceBean = params[0];
                        RaspiQuery query = new RaspiQuery(deviceBean.getHost(), deviceBean.getUser(), deviceBean.getPort());
                        try {
                            query.connect(deviceBean.getPass());
                            result.put("status", "online");
                            VcgencmdBean vcgencmdBean = query.queryVcgencmd();
                            result.put("temp", vcgencmdBean.getCpuTemperature() + "");
                        } catch (RaspiQueryException e) {
                            result.put("status", "offline");
                        }
                        return result;
                    }

                    @Override
                    protected void onPostExecute(Map<String, String> stringStringMap) {
                        super.onPostExecute(stringStringMap);
                        views.setTextViewText(R.id.textStatusValue, stringStringMap.get("status"));
                        views.setTextViewText(R.id.textTempValue, stringStringMap.get("temp"));
                        // Instruct the widget manager to update the widget
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    }
                }.execute(deviceBean);
            }

        }

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        deviceDb = new DeviceDbHelper(context);
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            LOGGER.debug("onUpdate - appWidgetID = {}", i);
            updateAppWidget(context, appWidgetManager, appWidgetIds[i], deviceDb);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            LOGGER.debug("onUpdate - appWidgetID = {}", i);
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


