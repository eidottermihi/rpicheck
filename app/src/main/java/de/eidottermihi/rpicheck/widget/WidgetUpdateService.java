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

import android.app.IntentService;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.eidottermihi.rpicheck.activity.helper.LoggingHelper;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

/**
 * @author eidottermihi
 */
public class WidgetUpdateService extends IntentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WidgetUpdateService.class);


    public WidgetUpdateService() {
        super("WidgetUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LoggingHelper.initLogging(getApplicationContext());
        int appWidgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID,
                INVALID_APPWIDGET_ID);
        if (appWidgetId != INVALID_APPWIDGET_ID) {
            LOGGER.debug("Received alarm intent for update of Widget[ID={}].", appWidgetId);
            OverclockingWidget.updateAppWidget(getApplicationContext(), appWidgetId, new DeviceDbHelper(getApplicationContext()), true);
        }
    }
}
