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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.eidottermihi.raspicheck.R;
import de.eidottermihi.rpicheck.activity.MainActivity;
import de.eidottermihi.rpicheck.db.RaspberryDeviceBean;

/**
 * @author Michael
 */
public class OverclockingWidgetView {

    private static final Logger LOGGER = LoggerFactory.getLogger(OverclockingWidgetView.class);
    private static final String ACTION_WIDGET_UPDATE_ONE_MANUAL = "updateOneWidgetManual";
    private static final String URI_SCHEME = "raspicheck";


    public static RemoteViews initDefaultView(Context context, int appWidgetId, RaspberryDeviceBean deviceBean, boolean showTemp, boolean showArm, boolean showLoad, boolean showMemory) {
        LOGGER.debug("Initiating default view for Widget[ID={}].", appWidgetId);
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.overclocking_widget);
        views.setOnClickPendingIntent(R.id.buttonRefresh, getSelfPendingIntent(context, appWidgetId, ACTION_WIDGET_UPDATE_ONE_MANUAL));
        PendingIntent activityIntent = getActivityIntent(context);
        views.setOnClickPendingIntent(R.id.linLayoutName, activityIntent);
        views.setOnClickPendingIntent(R.id.linLayoutTemp, activityIntent);
        views.setOnClickPendingIntent(R.id.linLayoutArm, activityIntent);
        views.setOnClickPendingIntent(R.id.linLayoutLoad, activityIntent);
        views.setOnClickPendingIntent(R.id.linLayoutMem, activityIntent);
        views.setTextViewText(R.id.textDeviceValue, deviceBean.getName());
        views.setTextViewText(R.id.textDeviceUserHost, String.format("%s@%s", deviceBean.getUser(), deviceBean.getHost()));
        views.setViewVisibility(R.id.linLayoutTemp, showTemp ? View.VISIBLE : View.GONE);
        views.setViewVisibility(R.id.linLayoutArm, showArm ? View.VISIBLE : View.GONE);
        views.setViewVisibility(R.id.linLayoutLoad, showLoad ? View.VISIBLE : View.GONE);
        views.setViewVisibility(R.id.linLayoutMem, showMemory ? View.VISIBLE : View.GONE);
        views.setProgressBar(R.id.progressBarArmValue, 100, 0, false);
        views.setProgressBar(R.id.progressBarLoad, 100, 0, false);
        views.setProgressBar(R.id.progressBarMemory, 100, 0, false);
        views.setProgressBar(R.id.progressBarTempValue, 100, 0, false);
        appWidgetManager.updateAppWidget(appWidgetId, views);
        return views;
    }

    public static void startRefreshing(RemoteViews views, Context context, int appWidgetId) {
        LOGGER.debug("Showing refresh view for Widget[ID={}].", appWidgetId);
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        views.setViewVisibility(R.id.textStatusValue, View.GONE);
        views.setViewVisibility(R.id.buttonRefresh, View.GONE);
        views.setViewVisibility(R.id.refreshProgressBar, View.VISIBLE);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static RemoteViews initNoPermissionView(Context context, int appWidgetId) {
        LOGGER.debug("Showing no permission view for Widget[ID={}].", appWidgetId);
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.overclocking_widget_no_read_permission);
        appWidgetManager.updateAppWidget(appWidgetId, views);
        return views;
    }

    public static void stopRefreshing(RemoteViews views, Context context, int appWidgetId) {
        LOGGER.debug("Stopping refresh view for Widget[ID={}].", appWidgetId);
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        views.setViewVisibility(R.id.buttonRefresh, View.VISIBLE);
        views.setViewVisibility(R.id.refreshProgressBar, View.GONE);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static RemoteViews initRemovedView(Context context, int appWidgetId) {
        LOGGER.debug("Showing removed view for Widget[ID={}].", appWidgetId);
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final RemoteViews removedView = new RemoteViews(context.getPackageName(), R.layout.overclocking_widget);
        appWidgetManager.updateAppWidget(appWidgetId, removedView);
        return removedView;
    }

    private static PendingIntent getSelfPendingIntent(Context context, int appWidgetId, String action) {
        final Uri data = getPendingIntentUri(appWidgetId);
        return getSelfPendingIntent(context, appWidgetId, data, action);
    }

    private static PendingIntent getSelfPendingIntent(Context context, int appWidgetId, Uri uri, String action) {
        final Intent intent = new Intent(context, OverclockingWidget.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(uri);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Uri getPendingIntentUri(int appWidgetId) {
        return Uri.withAppendedPath(Uri.parse(URI_SCHEME + "://widget/id/"), String.valueOf(appWidgetId));
    }

    private static PendingIntent getActivityIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        return pendingIntent;
    }
}
