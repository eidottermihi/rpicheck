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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.Spinner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.eidottermihi.raspicheck.R;
import de.eidottermihi.rpicheck.adapter.CommandAdapter;
import de.fhconfig.android.library.injection.annotation.XmlLayout;
import de.fhconfig.android.library.injection.annotation.XmlMenu;
import de.fhconfig.android.library.injection.annotation.XmlView;


/**
 * The configuration screen for the {@link CommandWidget CommandWidget} AppWidget.
 */
@XmlLayout(R.layout.command_widget_configure)
@XmlMenu(R.menu.activity_command_widget_configure)
public class CommandWidgetConfigureActivity extends AbstractWidgetConfigurationActivity {

    private static final String PREFS_NAME = "de.eidottermihi.rpicheck.widget.CommandWidget";
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandWidgetConfigureActivity.class);
    private static final String KEY_DEVICE_ID = "_pi_id";
    private static final String KEY_COMMAND_ID = "_cmd_id";
    private static final String KEY_RUN_IN_BACKGROUND = "_background";
    private static final String KEY_INITIALIZED = "_initialized";

    @XmlView(R.id.cmd_widget_conf_cmdSpinner)
    private Spinner cmdSpinner;
    @XmlView(R.id.cmd_widget_conf_cb_background)
    private CheckBox runInBackgroundCheckBox;

    private Cursor commandCursor;

    public CommandWidgetConfigureActivity() {
        super(R.id.cmd_widget_conf_piSpinner);
    }

    protected static Long loadCommandId(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        final long id = prefs.getLong(buildFullPrefKey(KEY_COMMAND_ID, appWidgetId), 0L);
        if (id == 0L) {
            return null;
        }
        return id;
    }

    protected static Long loadDeviceId(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        final long id = prefs.getLong(buildFullPrefKey(KEY_DEVICE_ID, appWidgetId), 0L);
        if (id == 0L) {
            return null;
        }
        return id;
    }

    protected static Boolean loadRunInBackground(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getBoolean(buildFullPrefKey(KEY_RUN_IN_BACKGROUND, appWidgetId), false);
    }

    protected static void deleteWidget(Context context, int appWidgetId) {
        LOGGER.debug("Deleting data for Command Widget[ID={}]", appWidgetId);
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(buildFullPrefKey(KEY_DEVICE_ID, appWidgetId))
                .remove(buildFullPrefKey(KEY_COMMAND_ID, appWidgetId))
                .remove(buildFullPrefKey(KEY_RUN_IN_BACKGROUND, appWidgetId))
                .remove(buildFullPrefKey(KEY_INITIALIZED, appWidgetId))
                .apply();
    }

    public static boolean isInitialized(Context context, int appWidgetId) {
        return context.getSharedPreferences(PREFS_NAME, 0).getBoolean(buildFullPrefKey(KEY_INITIALIZED, appWidgetId), false);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        initCommandSpinner();
    }

    @Override
    public String getSharedPreferencesName() {
        return PREFS_NAME;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                saveCommandWidget();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveCommandWidget() {
        final long deviceId = piSpinner.getSelectedItemId();
        final long commandId = cmdSpinner.getSelectedItemId();
        LOGGER.debug("Saving new Command-Widget for Pi[ID={}] and Command[ID={}].", deviceId, commandId);
        final SharedPreferences.Editor prefs = getSharedPreferences(getSharedPreferencesName(), 0).edit();
        prefs.putLong(buildFullPrefKey(KEY_DEVICE_ID, mAppWidgetId), deviceId);
        prefs.putLong(buildFullPrefKey(KEY_COMMAND_ID, mAppWidgetId), commandId);
        prefs.putBoolean(buildFullPrefKey(KEY_RUN_IN_BACKGROUND, mAppWidgetId), runInBackgroundCheckBox.isChecked());
        prefs.putBoolean(buildFullPrefKey(KEY_INITIALIZED, mAppWidgetId), true);
        prefs.apply();

        // It is the responsibility of the configuration activity to update the app widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(CommandWidgetConfigureActivity.this);
        CommandWidget.updateAppWidget(CommandWidgetConfigureActivity.this, appWidgetManager, mAppWidgetId, deviceDbHelper);

        // Make sure we pass back the original appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    private void initCommandSpinner() {
        // db operation in background thread
        new AsyncTask<Void, Void, Cursor>() {

            @Override
            protected Cursor doInBackground(Void... voids) {
                return deviceDbHelper.getFullCommandCursor();
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                final CommandAdapter commandAdapter = new CommandAdapter(CommandWidgetConfigureActivity.this, cursor, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
                cmdSpinner.setAdapter(commandAdapter);
                if (commandAdapter.getCount() == 0) {
                    finishWithToast("You need to add a custom command first.");
                    return;
                }
                super.onPostExecute(cursor);
            }
        }.execute();
    }
}



