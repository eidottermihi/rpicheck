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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import de.eidottermihi.raspicheck.R;
import de.eidottermihi.rpicheck.adapter.DeviceSpinnerAdapter;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;
import de.fhconfig.android.library.ui.injection.InjectionActionBarActivity;

/**
 * Abstract configuration activity for common parts (device chooser).
 *
 * @author Michael
 */
public abstract class AbstractWidgetConfigurationActivity extends InjectionActionBarActivity {

    protected int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    protected DeviceDbHelper deviceDbHelper;
    protected Spinner piSpinner;

    private int spinnerId;

    public AbstractWidgetConfigurationActivity(int spinnerId){
        super();
        this.spinnerId = spinnerId;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        this.piSpinner = (Spinner) findViewById(this.spinnerId);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        this.getSupportActionBar().setTitle(getString(R.string.widget_configure_title));
        deviceDbHelper = new DeviceDbHelper(this);
        final int deviceCount = initDeviceSpinner();
        if (deviceCount == 0) {
            // show Toast to add a device first
            Toast.makeText(this, getString(R.string.widget_add_no_device), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    /**
     * @return Pi count
     */
    private int initDeviceSpinner() {
        // Device Spinner
        final DeviceSpinnerAdapter deviceSpinnerAdapter = new DeviceSpinnerAdapter(AbstractWidgetConfigurationActivity.this, deviceDbHelper.getFullDeviceCursor(), true);
        piSpinner.setAdapter(deviceSpinnerAdapter);
        return deviceSpinnerAdapter.getCount();
    }
}
