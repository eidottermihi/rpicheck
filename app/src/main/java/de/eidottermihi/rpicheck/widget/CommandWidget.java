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
package de.eidottermihi.rpicheck.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import de.eidottermihi.raspicheck.R;
import de.eidottermihi.rpicheck.activity.CustomCommandActivity;
import de.eidottermihi.rpicheck.db.CommandBean;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;
import de.eidottermihi.rpicheck.db.RaspberryDeviceBean;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link CommandWidgetConfigureActivity CommandWidgetConfigureActivity}
 */
public class CommandWidget extends AppWidgetProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandWidget.class);
    private static final String URI_SCHEME = "raspicheck-cmd";
    private DeviceDbHelper deviceDbHelper;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, DeviceDbHelper deviceDbHelper) {
        final boolean isInitialized = CommandWidgetConfigureActivity.isInitialized(context, appWidgetId);
        if (isInitialized) {
            final Long deviceId = CommandWidgetConfigureActivity.loadDeviceId(context, appWidgetId);
            final Long commandId = CommandWidgetConfigureActivity.loadCommandId(context, appWidgetId);
            final boolean runInBackground = CommandWidgetConfigureActivity.loadRunInBackground(context, appWidgetId);
            LOGGER.debug("Updating CommandWidget[ID={}] for Pi[ID={}] and Command[ID={}].", appWidgetId, deviceId, commandId);
            final RaspberryDeviceBean deviceBean = deviceDbHelper.read(deviceId);
            final CommandBean commandBean = deviceDbHelper.readCommand(commandId);
            final RemoteViews views = updateWidgetView(context, appWidgetId, deviceBean, commandBean, runInBackground);
            // update widget view
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private static RemoteViews updateWidgetView(Context context, int appWidgetId, @Nullable RaspberryDeviceBean deviceBean, @Nullable CommandBean commandBean, boolean runInBackground) {
        RemoteViews views;
        if (deviceBean != null && commandBean != null) {
            views = new RemoteViews(context.getPackageName(), R.layout.command_widget);
            views.setTextViewText(R.id.cmd_widget_title_text, deviceBean.getName());
            views.setCharSequence(R.id.cmd_widget_button_run_cmd, "setText", commandBean.getName());
            final Intent cmdIntent = new Intent();
            cmdIntent.setClass(context, CustomCommandActivity.class);
            cmdIntent.putExtra(CustomCommandActivity.EXTRA_COMMAND_ID, commandBean.getId());
            cmdIntent.putExtra(CustomCommandActivity.EXTRA_DEVICE_BEAN, (java.io.Serializable) deviceBean);
            cmdIntent.setAction(CustomCommandActivity.ACTION_RUN_COMMAND);
            cmdIntent.setData(getPendingIntentUri(appWidgetId));
            cmdIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            final PendingIntent p = PendingIntent.getActivity(context, 0, cmdIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.cmd_widget_button_run_cmd, p);
        } else {
            views = new RemoteViews(context.getPackageName(), R.layout.command_widget_no_device_command);
        }
        return views;
    }

    private static Uri getPendingIntentUri(int appWidgetId) {
        return Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/"), String.valueOf(appWidgetId));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (deviceDbHelper == null) {
            this.deviceDbHelper = new DeviceDbHelper(context);
        }
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i], this.deviceDbHelper);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            CommandWidgetConfigureActivity.deleteWidget(context, appWidgetIds[i]);
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


