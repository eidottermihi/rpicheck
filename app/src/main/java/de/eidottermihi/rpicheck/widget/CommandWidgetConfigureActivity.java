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

import android.appwidget.AppWidgetManager;
import android.os.Bundle;

import de.eidottermihi.raspicheck.R;
import de.fhconfig.android.library.injection.annotation.XmlLayout;
import de.fhconfig.android.library.injection.annotation.XmlMenu;


/**
 * The configuration screen for the {@link CommandWidget CommandWidget} AppWidget.
 */
@XmlLayout(R.layout.command_widget_configure)
@XmlMenu(R.menu.activity_command_widget_configure)
public class CommandWidgetConfigureActivity extends AbstractWidgetConfigurationActivity {

    private static final String PREFS_NAME = "de.eidottermihi.rpicheck.widget.CommandWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public CommandWidgetConfigureActivity() {
        super(R.id.piSpinner);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }
}



