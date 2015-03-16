package de.eidottermihi.raspicheck;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Spinner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.eidottermihi.rpicheck.adapter.DeviceSpinnerAdapter;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;
import de.larsgrefer.android.library.injection.annotation.XmlLayout;
import de.larsgrefer.android.library.injection.annotation.XmlMenu;
import de.larsgrefer.android.library.injection.annotation.XmlView;
import de.larsgrefer.android.library.ui.InjectionActionBarActivity;


/**
 * The configuration screen for the {@link OverclockingWidget OverclockingWidget} AppWidget.
 */
@XmlLayout(R.layout.overclocking_widget_configure)
@XmlMenu(R.menu.activity_overclocking_widget_configure)
public class OverclockingWidgetConfigureActivity extends InjectionActionBarActivity {

    private static final Logger LOGGER = LoggerFactory.getLogger(OverclockingWidgetConfigureActivity.class);
    private static final String PREFS_NAME = "de.eidottermihi.raspicheck.OverclockingWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    @XmlView(R.id.widgetPiSpinner)
    Spinner widgetPiSpinner;
    private DeviceDbHelper deviceDbHelper;

    public OverclockingWidgetConfigureActivity() {
        super();
    }

    /**
     * @param context
     * @param appWidgetId
     * @param deviceId    ID of the chosen device
     */
    static void saveChosenDevicePref(Context context, int appWidgetId, Long deviceId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putLong(PREF_PREFIX_KEY + appWidgetId, deviceId);
        prefs.commit();
    }

    static Long loadDeviceId(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Long deviceId = prefs.getLong(PREF_PREFIX_KEY + appWidgetId, 0);
        if (deviceId != 0) {
            return deviceId;
        } else {
            return null;
        }
    }

    static void deleteDevicePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.commit();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

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

        deviceDbHelper = new DeviceDbHelper(this);
        initSpinner();
    }

    private void initSpinner() {
        widgetPiSpinner.setAdapter(new DeviceSpinnerAdapter(OverclockingWidgetConfigureActivity.this, deviceDbHelper.getFullDeviceCursor(), true));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                final Context context = OverclockingWidgetConfigureActivity.this;

                // When the button is clicked, store the string locally
                //String widgetText = mAppWidgetText.getText().toString();
                long selectedItemId = widgetPiSpinner.getSelectedItemId();
                LOGGER.info("Selected Device - Item ID = {}", selectedItemId);
                // save Device ID in prefs
                saveChosenDevicePref(context, mAppWidgetId, selectedItemId);

                // It is the responsibility of the configuration activity to update the app widget
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                OverclockingWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId, deviceDbHelper);

                // Make sure we pass back the original appWidgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}



