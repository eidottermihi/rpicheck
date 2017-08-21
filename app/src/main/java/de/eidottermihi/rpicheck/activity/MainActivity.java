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
package de.eidottermihi.rpicheck.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SpinnerAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.eidottermihi.raspicheck.BuildConfig;
import de.eidottermihi.raspicheck.R;
import de.eidottermihi.rpicheck.activity.helper.Constants;
import de.eidottermihi.rpicheck.activity.helper.FormatHelper;
import de.eidottermihi.rpicheck.activity.helper.LoggingHelper;
import de.eidottermihi.rpicheck.adapter.DeviceSpinnerAdapter;
import de.eidottermihi.rpicheck.beans.QueryBean;
import de.eidottermihi.rpicheck.beans.ShutdownResult;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;
import de.eidottermihi.rpicheck.db.RaspberryDeviceBean;
import de.eidottermihi.rpicheck.fragment.PassphraseDialog;
import de.eidottermihi.rpicheck.fragment.PassphraseDialog.PassphraseDialogListener;
import de.eidottermihi.rpicheck.fragment.QueryErrorMessagesDialog;
import de.eidottermihi.rpicheck.fragment.QueryExceptionDialog;
import de.eidottermihi.rpicheck.fragment.RebootDialogFragment;
import de.eidottermihi.rpicheck.fragment.RebootDialogFragment.ShutdownDialogListener;
import de.eidottermihi.rpicheck.ssh.LoadAveragePeriod;
import de.eidottermihi.rpicheck.ssh.beans.DiskUsageBean;
import de.eidottermihi.rpicheck.ssh.beans.Exported;
import de.eidottermihi.rpicheck.ssh.beans.NetworkInterfaceInformation;
import de.eidottermihi.rpicheck.ssh.beans.ProcessBean;
import de.eidottermihi.rpicheck.ssh.beans.WlanBean;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;
import io.freefair.android.injection.annotation.InjectView;
import io.freefair.android.injection.annotation.XmlLayout;
import io.freefair.android.injection.app.InjectionAppCompatActivity;
import sheetrock.panda.changelog.ChangeLog;

@XmlLayout(R.layout.activity_main)
public class MainActivity extends InjectionAppCompatActivity implements
        ActionBar.OnNavigationListener,
        ShutdownDialogListener, PassphraseDialogListener, AsyncQueryDataUpdate,
        AsyncShutdownUpdate {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainActivity.class);

    private static final String CURRENT_DEVICE = "currentDevice";
    private static final String ALL_DEVICES = "allDevices";

    private static final String KEY_PREF_REFRESH_BY_ACTION_COUNT = "refreshCountByAction";
    private static final int REQUEST_PERMISSION_READ_FOR_QUERY_PULL = 1;
    private static final int REQUEST_PERMISSION_READ_FOR_QUERY_NO_PULL = 2;
    private static final int REQUEST_PERMISSION_READ_FOR_HALT = 3;
    private static final int REQUEST_PERMISSION_READ_FOR_REBOOT = 4;
    private static boolean isOnBackground;

    @InjectView(R.id.commandButton)
    private Button commandButton;
    @InjectView(R.id.armFreqText)
    private TextView armFreqText;
    @InjectView(R.id.coreFreqText)
    private TextView coreFreqText;
    @InjectView(R.id.coreVoltText)
    private TextView coreVoltText;
    @InjectView(R.id.coreTempText)
    private TextView coreTempText;
    @InjectView(R.id.firmwareText)
    private TextView firmwareText;
    @InjectView(R.id.lastUpdateText)
    private TextView lastUpdateText;
    @InjectView(R.id.uptimeText)
    private TextView uptimeText;
    @InjectView(R.id.averageLoadText)
    private TextView averageLoadText;
    @InjectView(R.id.totalMemoryText)
    private TextView totalMemoryText;
    @InjectView(R.id.freeMemoryText)
    private TextView freeMemoryText;
    @InjectView(R.id.cpuSerialText)
    private TextView serialNoText;
    @InjectView(R.id.distriText)
    private TextView distriText;
    @InjectView(R.id.diskTable)
    private TableLayout diskTable;
    @InjectView(R.id.processTable)
    private TableLayout processTable;
    @InjectView(R.id.networkTable)
    private TableLayout networkTable;
    @InjectView(R.id.swipeRefreshLayout)
    private SwipeRefreshLayout swipeRefreshLayout;

    private SharedPreferences sharedPrefs;
    private DeviceDbHelper deviceDb;
    private RaspberryDeviceBean currentDevice;
    private SparseArray<RaspberryDeviceBean> allDevices;
    private Cursor deviceCursor;
    private SpinnerAdapter spinadapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // assigning Shared Preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        LoggingHelper.initLogging(this);

        // assigning refreshable root scrollview
        initSwipeRefreshLayout();

        // Changelog
        final ChangeLog changeLog = new ChangeLog(this);
        if (changeLog.firstRun()) {
            changeLog.getLogDialog().show();
        }

        // init device database
        deviceDb = new DeviceDbHelper(this);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                deviceCursor = deviceDb.getFullDeviceCursor();
                return null;
            }

            @Override
            protected void onPostExecute(Void r) {
                if (deviceCursor.getCount() == 0) {
                    Intent newRaspiIntent = new Intent(MainActivity.this, NewRaspiActivity.class);
                    startActivityForResult(newRaspiIntent, NewRaspiActivity.REQUEST_SAVE);
                } else {
                    // init spinner
                    initSpinner();
                }
            }
        }.execute();
    }

    private void initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LOGGER.trace("Query initiated by PullToRefresh.");
                doQuery(true);
            }
        });
        TypedValue accentColor = new TypedValue();
        TypedValue primaryColor = new TypedValue();
        if (getTheme().resolveAttribute(R.attr.colorAccent, accentColor, true) &&
                getTheme().resolveAttribute(R.attr.colorPrimary, primaryColor, true)) {
            swipeRefreshLayout.setColorSchemeResources(accentColor.resourceId, primaryColor.resourceId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOnBackground = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isOnBackground = true;
    }

    /**
     * Reset the view.
     */
    private void resetView() {
        coreTempText.setText("");
        armFreqText.setText("");
        coreFreqText.setText("");
        coreVoltText.setText("");
        firmwareText.setText("");
        uptimeText.setText("");
        averageLoadText.setText("");
        totalMemoryText.setText("");
        freeMemoryText.setText("");
        distriText.setText("");
        serialNoText.setText("");
        lastUpdateText.setText("");
        // tables
        updateDiskTable(null);
        updateNetworkTable(null);
        updateProcessTable(null);
        // remove share action
        this.supportInvalidateOptionsMenu();
    }

    private void updateQueryDataInView(QueryBean result) {
        final String tempScale = sharedPrefs.getString(SettingsActivity.KEY_PREF_TEMPERATURE_SCALE, getString(R.string.pref_temperature_scala_default));
        coreTempText.setText(FormatHelper.formatTemperature(currentDevice.getLastQueryData().getVcgencmdInfo().getCpuTemperature(), tempScale));
        final String freqScale = sharedPrefs.getString(SettingsActivity.KEY_PREF_FREQUENCY_UNIT, getString(R.string.pref_frequency_unit_default));
        armFreqText.setText(FormatHelper.formatFrequency(currentDevice.getLastQueryData().getVcgencmdInfo().getArmFrequency(), freqScale));
        coreFreqText.setText(FormatHelper.formatFrequency(currentDevice.getLastQueryData().getVcgencmdInfo().getCoreFrequency(), freqScale));
        coreVoltText.setText(FormatHelper.formatDecimal(currentDevice.getLastQueryData().getVcgencmdInfo().getCoreVolts()));
        firmwareText.setText(result.getVcgencmdInfo().getVersion());
        lastUpdateText.setText(SimpleDateFormat.getDateTimeInstance().format(result.getLastUpdate()));
        // uptime and average load may contain errors
        if (result.getAvgLoad() != null) {
            averageLoadText.setText(result.getAvgLoad());
        }
        if (result.getStartup() != null) {
            uptimeText.setText(result.getStartup());
        }
        if (result.getFreeMem() != null) {
            freeMemoryText.setText(result.getFreeMem().humanReadableByteCount(false));
        }
        if (result.getTotalMem() != null) {
            totalMemoryText.setText(result.getTotalMem().humanReadableByteCount(false));
        }
        serialNoText.setText(result.getSerialNo());
        distriText.setText(result.getDistri());
        // update tables
        updateNetworkTable(result);
        updateDiskTable(result);
        updateProcessTable(result);
        this.handleQueryError(result.getErrorMessages());
        this.supportInvalidateOptionsMenu();
    }

    /**
     * Shows a dialog containing the ErrorMessages.
     *
     * @param errorMessages the messages
     */
    private void handleQueryError(List<String> errorMessages) {
        final ArrayList<String> messages = new ArrayList<>(errorMessages);
        if (errorMessages.size() > 0 && !isOnBackground) {
            LOGGER.debug("Showing query error messages.");
            Bundle args = new Bundle();
            args.putStringArrayList(QueryErrorMessagesDialog.KEY_ERROR_MESSAGES, messages);
            final QueryErrorMessagesDialog messageDialog = new QueryErrorMessagesDialog();
            messageDialog.setArguments(args);
            messageDialog.show(getSupportFragmentManager(), "QueryErrorMessagesDialog");
        }
    }

    private void updateNetworkTable(QueryBean result) {
        // remove rows except header
        networkTable.removeViews(1, networkTable.getChildCount() - 1);
        if (result != null && result.getNetworkInfo() != null) {
            for (NetworkInterfaceInformation interfaceInformation : result.getNetworkInfo()) {
                networkTable.addView(createNetworkRow(interfaceInformation));
            }

        }
    }

    private View createNetworkRow(NetworkInterfaceInformation interfaceInformation) {
        final TableRow tempRow = new TableRow(this);
        tempRow.addView(createTextView(interfaceInformation.getName()));
        CharSequence statusText;
        if (interfaceInformation.isHasCarrier()) {
            statusText = getText(R.string.network_status_up);
        } else {
            statusText = getText(R.string.network_status_down);
        }
        tempRow.addView(createTextView(statusText.toString()));
        if (interfaceInformation.getIpAdress() != null) {
            tempRow.addView(createTextView(interfaceInformation.getIpAdress()));
        } else {
            tempRow.addView(createTextView(" - "));
        }
        if (interfaceInformation.getWlanInfo() != null) {
            final WlanBean wlan = interfaceInformation.getWlanInfo();
            tempRow.addView(createTextView(FormatHelper.formatPercentage(wlan.getSignalLevel())));
            tempRow.addView(createTextView(FormatHelper.formatPercentage(wlan.getLinkQuality())));
        } else {
            tempRow.addView(createTextView(" - "));
            tempRow.addView(createTextView(" - "));
        }
        return tempRow;
    }

    private void updateProcessTable(QueryBean result) {
        // remove current rows except header row
        processTable.removeViews(1, processTable.getChildCount() - 1);
        if (result != null && result.getProcesses() != null) {
            for (ProcessBean processBean : result.getProcesses()) {
                processTable.addView(createProcessRow(processBean));
            }
        }
    }

    private View createProcessRow(ProcessBean processBean) {
        final TableRow tempRow = new TableRow(this);
        tempRow.addView(createTextView(processBean.getpId() + ""));
        tempRow.addView(createTextView(processBean.getTty()));
        tempRow.addView(createTextView(processBean.getCpuTime()));
        tempRow.addView(createTextView(processBean.getCommand()));
        return tempRow;
    }

    private View createDiskRow(DiskUsageBean disk) {
        final TableRow tempRow = new TableRow(this);
        tempRow.addView(createTextView(disk.getFileSystem()));
        tempRow.addView(createTextView(disk.getSize()));
        tempRow.addView(createTextView(disk.getAvailable()));
        tempRow.addView(createTextView(disk.getUsedPercent()));
        tempRow.addView(createTextView(disk.getMountedOn()));
        return tempRow;
    }

    private View createTextView(String text) {
        final TextView tempText = new TextView(this);
        tempText.setText(text);
        float pix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        tempText.setPadding((int) (pix), (int) (pix), 0, 0);
        return tempText;
    }

    private void updateDiskTable(QueryBean result) {
        // remove current rows except header row
        diskTable.removeViews(1, diskTable.getChildCount() - 1);
        if (result != null && result.getDisks() != null) {
            for (DiskUsageBean diskUsageBean : result.getDisks()) {
                // add row to table
                diskTable.addView(createDiskRow(diskUsageBean));
            }
        }
    }

    private void initSpinner() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                deviceCursor = deviceDb.getFullDeviceCursor();
                LOGGER.debug("Device cursor rows: " + deviceCursor.getCount());
                return null;
            }

            @Override
            protected void onPostExecute(Void r) {
                // only show spinner if theres already a device to show
                if (deviceCursor.getCount() > 0) {
                    // make adapter
                    //spinadapter = new SimpleCursorAdapter(MainActivity.this,
                    //        android.R.layout.simple_spinner_dropdown_item,
                    //        deviceCursor, new String[]{"name", "_id"},
                    //        new int[]{android.R.id.text1});
                    spinadapter = new DeviceSpinnerAdapter(MainActivity.this, deviceCursor, true);
                    //spinadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                    getSupportActionBar().setListNavigationCallbacks(spinadapter, MainActivity.this);
                    getSupportActionBar().setDisplayShowTitleEnabled(false);
                    commandButton.setVisibility(View.VISIBLE);
                } else {
                    getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                    getSupportActionBar().setDisplayShowTitleEnabled(true);
                    currentDevice = null;
                    // disable edit/restart/delete action menu items
                    supportInvalidateOptionsMenu();
                    commandButton.setVisibility(View.GONE);

                }
            }
        }.execute();
    }

    /**
     * Shows a dialog with a detailed error message.
     *
     * @param exception
     */
    private void handleQueryException(final RaspiQueryException exception) {
        final String errorMessage = this.mapExceptionToErrorMessage(exception);
        // only show dialog when app is not in background
        if (!isOnBackground) {
            LOGGER.debug("Query caused exception. Showing dialog.");
            // build dialog
            Bundle dialogArgs = new Bundle();
            dialogArgs.putString(QueryExceptionDialog.MESSAGE_KEY, errorMessage);
            QueryExceptionDialog dialogFragment = new QueryExceptionDialog();
            dialogFragment.setArguments(dialogArgs);
            dialogFragment.show(getSupportFragmentManager(), "QueryExceptionDialog");
        }
    }

    private String mapExceptionToErrorMessage(RaspiQueryException exception) {
        String message = null;
        switch (exception.getReasonCode()) {
            case RaspiQueryException.REASON_CONNECTION_FAILED:
                message = getString(R.string.connection_failed);
                break;
            case RaspiQueryException.REASON_AUTHENTIFICATION_FAILED:
                message = getString(R.string.authentication_failed);
                break;
            case RaspiQueryException.REASON_TRANSPORT_EXCEPTION:
                message = getString(R.string.transport_exception);
                break;
            case RaspiQueryException.REASON_IO_EXCEPTION:
                message = getString(R.string.unexpected_exception);
                break;
            case RaspiQueryException.REASON_VCGENCMD_NOT_FOUND:
                message = getString(R.string.exception_vcgencmd);
                break;
            default:
                message = getString(R.string.weird_exception);
                break;
        }
        return message;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        // set delete, edit and reboot visible if there is a current device
        boolean currDevice = currentDevice != null;
        menu.findItem(R.id.menu_delete).setVisible(currDevice);
        menu.findItem(R.id.menu_edit_raspi).setVisible(currDevice);
        menu.findItem(R.id.menu_reboot).setVisible(currDevice);
        boolean showingQueryData = currDevice && currentDevice.getLastQueryData() != null;
        menu.findItem(R.id.menu_share).setVisible(showingQueryData);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                this.startActivity(settingsIntent);
                break;
            case R.id.menu_new_raspi:
                Intent newRaspiIntent = new Intent(MainActivity.this, NewRaspiActivity.class);
                this.startActivityForResult(newRaspiIntent, NewRaspiActivity.REQUEST_SAVE);
                break;
            case R.id.menu_delete:
                this.deleteCurrentDevice();
                break;
            case R.id.menu_edit_raspi:
                final Bundle extras = new Bundle();
                extras.putInt(Constants.EXTRA_DEVICE_ID, currentDevice.getId());
                Intent editRaspiIntent = new Intent(MainActivity.this, EditRaspiActivity.class);
                editRaspiIntent.putExtras(extras);
                this.startActivityForResult(editRaspiIntent, EditRaspiActivity.REQUEST_EDIT);
                break;
            case R.id.menu_reboot:
                this.showRebootDialog();
                break;
            case R.id.menu_refresh:
                swipeRefreshLayout.setRefreshing(true);
                this.doQuery(false);
                break;
            case R.id.menu_share:
                this.shareQueryData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareQueryData() {
        LOGGER.debug("Create sharing intent for current query data.");
        final Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                "RasPi Check Status '" + currentDevice.getName() + "'");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, buildShareText(currentDevice));
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    private String buildShareText(@NonNull RaspberryDeviceBean device) {
        return getString(R.string.share_body, device.getName(), device.getHost(), new SimpleDateFormat().format(device.getLastQueryData().getLastUpdate()),
                buildShareTextBody(device.getLastQueryData()), BuildConfig.VERSION_NAME);
    }

    private String buildShareTextBody(@NonNull QueryBean lastQueryData) {
        Map<String, String> informations = new LinkedHashMap<>();
        visitExportedMethods(lastQueryData, "", informations);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> infoEntry : informations.entrySet()) {
            sb.append(infoEntry.getKey()).append("=").append(infoEntry.getValue()).append("\n");
        }
        return sb.toString();
    }

    private void visitExportedMethods(Object object, String keyPrefix, Map<String, String> informationMap) {
        for (Method method : object.getClass().getMethods()) {
            if (method.isAnnotationPresent(Exported.class) && method.getParameterTypes().length == 0) {
                String key = method.getAnnotation(Exported.class).value();
                if ("".equals(key)) {
                    key = method.getName();
                    if (key.startsWith("get")) {
                        key = key.substring(3).toLowerCase();
                    }
                }
                try {
                    final Object value = method.invoke(object);
                    if (value != null) {
                        if (value instanceof Boolean || value instanceof String || value instanceof Double || value instanceof Long || value instanceof Integer) {
                            informationMap.put(keyPrefix + key, value.toString());
                        } else if (value instanceof Collection) {
                            Collection collection = (List) value;
                            int counter = 0;
                            for (Object val : collection) {
                                visitExportedMethods(val, String.format("%s[%s].", key, counter++), informationMap);
                            }

                        } else {
                            visitExportedMethods(value, key + ".", informationMap);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn("Couldn't invoke exported method.", e);
                }
            }
        }
    }

    /**
     * Shows 3 toasts if refresh is happening via action button and not pull to refresh
     */
    private void showPullToRefreshHint() {
        int count = sharedPrefs.getInt(KEY_PREF_REFRESH_BY_ACTION_COUNT, 0);
        if (count < 3) {
            Toast.makeText(this, getString(R.string.hint_pulltorefresh), Toast.LENGTH_LONG).show();
            sharedPrefs.edit().putInt(KEY_PREF_REFRESH_BY_ACTION_COUNT, ++count).apply();
        }
    }

    private void showRebootDialog() {
        LOGGER.trace("Showing reboot dialog.");
        DialogFragment rebootDialog = new RebootDialogFragment();
        rebootDialog.show(getSupportFragmentManager(), "reboot");
    }

    /**
     * @return true if network is available
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


    private void doRebootOrHalt(String type) {
        LOGGER.info("Doing {} on {}...", type, currentDevice.getName());
        if (isNetworkAvailable()) {
            // get connection settings from shared preferences
            final String host = currentDevice.getHost();
            final String user = currentDevice.getUser();
            final String port = currentDevice.getPort() + "";
            final String sudoPass = currentDevice.getSudoPass();
            if (currentDevice.usesAuthentificationMethod(RaspberryDeviceBean.AUTH_PASSWORD)) {
                final String pass = currentDevice.getPass();
                if (pass != null) {
                    new SSHShutdownTask(this).execute(host, user, pass, port, sudoPass, type, null, null);
                } else {
                    Toast.makeText(this, R.string.no_password_specified, Toast.LENGTH_LONG).show();
                }
            } else if (currentDevice.usesAuthentificationMethod(RaspberryDeviceBean.AUTH_PUBLIC_KEY) || currentDevice.usesAuthentificationMethod(RaspberryDeviceBean.AUTH_PUBLIC_KEY_WITH_PASSWORD)) {
                // keyfile must be present and readable
                final String keyfilePath = currentDevice.getKeyfilePath();
                if (keyfilePath != null) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        LOGGER.debug("Requesting permission to read private key file from storage...");
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                type.equals(Constants.TYPE_HALT) ? REQUEST_PERMISSION_READ_FOR_HALT : REQUEST_PERMISSION_READ_FOR_REBOOT);
                        return;
                    } else {
                        final File privateKey = new File(keyfilePath);
                        if (privateKey.exists()) {
                            new SSHShutdownTask(this).execute(host, user, null, port, sudoPass, type, keyfilePath, null);
                        } else {
                            Toast.makeText(this, "Cannot find keyfile at location: " + keyfilePath, Toast.LENGTH_LONG);
                        }
                    }
                } else {
                    Toast.makeText(this, "No keyfile specified!", Toast.LENGTH_LONG);
                }
                if (currentDevice.usesAuthentificationMethod(RaspberryDeviceBean.AUTH_PUBLIC_KEY_WITH_PASSWORD)) {
                    if (!Strings.isNullOrEmpty(currentDevice.getKeyfilePass())) {
                        final String passphrase = currentDevice.getKeyfilePass();
                        new SSHShutdownTask(this).execute(host, user, null, port, sudoPass, type, keyfilePath, passphrase);
                    } else {
                        final String dialogType = type.equals(Constants.TYPE_REBOOT) ? PassphraseDialog.SSH_SHUTDOWN : PassphraseDialog.SSH_HALT;
                        final DialogFragment passphraseDialog = new PassphraseDialog();
                        final Bundle args = new Bundle();
                        args.putString(PassphraseDialog.KEY_TYPE, dialogType);
                        passphraseDialog.setArguments(args);
                        passphraseDialog.setCancelable(false);
                        passphraseDialog.show(getSupportFragmentManager(), "passphrase");
                    }
                }
            }
        } else {
            // no network available
            Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteCurrentDevice() {
        LOGGER.info("Deleting pi {}.", currentDevice.getName());
        deviceDb.delete(currentDevice.getId());
        if (allDevices != null) {
            allDevices.delete(currentDevice.getId());
        }
        initSpinner();
    }


    private void doQuery(boolean initByPullToRefresh) {
        if (currentDevice == null) {
            // no device available, show hint for user
            Toast.makeText(this, R.string.no_device_available, Toast.LENGTH_LONG).show();
            // stop refresh animation from pull-to-refresh
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        if (isNetworkAvailable()) {
            // get connection settings from shared preferences
            String host = currentDevice.getHost();
            String user = currentDevice.getUser();
            String port = currentDevice.getPort() + "";
            String pass = null;
            // reading process preference
            final Boolean hideRoot = sharedPrefs.getBoolean(SettingsActivity.KEY_PREF_QUERY_HIDE_ROOT_PROCESSES, true);
            String keyPath = null;
            String keyPass = null;
            boolean canConnect = false;
            // check authentification method
            if (currentDevice.usesAuthentificationMethod(RaspberryDeviceBean.AUTH_PASSWORD)) {
                pass = currentDevice.getPass();
                if (pass != null) {
                    canConnect = true;
                } else {
                    Toast.makeText(this, R.string.no_password_specified, Toast.LENGTH_LONG).show();
                }
            } else if (currentDevice.usesAuthentificationMethod(RaspberryDeviceBean.AUTH_PUBLIC_KEY) ||
                    currentDevice.usesAuthentificationMethod(RaspberryDeviceBean.AUTH_PUBLIC_KEY_WITH_PASSWORD)) {
                // keyfile must be present and readable
                final String keyfilePath = currentDevice.getKeyfilePath();
                if (keyfilePath != null) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        LOGGER.debug("Requesting permission to read private key file from storage...");
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                initByPullToRefresh ? REQUEST_PERMISSION_READ_FOR_QUERY_PULL : REQUEST_PERMISSION_READ_FOR_QUERY_NO_PULL);
                        return;
                    } else {
                        final File privateKey = new File(keyfilePath);
                        if (privateKey.exists()) {
                            keyPath = keyfilePath;
                            canConnect = true;
                        } else {
                            Toast.makeText(this, "Cannot find keyfile at location: " + keyfilePath, Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(this, "No keyfile specified!", Toast.LENGTH_LONG).show();
                }
                if (currentDevice.usesAuthentificationMethod(RaspberryDeviceBean.AUTH_PUBLIC_KEY_WITH_PASSWORD)) {
                    // keypass must be present
                    final String keyfilePass = currentDevice.getKeyfilePass();
                    if (keyfilePass != null) {
                        canConnect = true;
                        keyPass = keyfilePass;
                    } else {
                        final DialogFragment newFragment = new PassphraseDialog();
                        final Bundle args = new Bundle();
                        args.putString(PassphraseDialog.KEY_TYPE, PassphraseDialog.SSH_QUERY);
                        newFragment.setArguments(args);
                        newFragment.setCancelable(false);
                        newFragment.show(getSupportFragmentManager(), "passphrase");
                        canConnect = false;
                    }
                }
            }
            if (canConnect) {
                if (!initByPullToRefresh) {
                    // show hint that user can use pull-to-refresh
                    showPullToRefreshHint();
                }
                // execute query in background
                new SSHQueryTask(this, getLoadAveragePreference()).execute(host, user, pass, port, hideRoot.toString(), keyPath, keyPass);
            }
        } else {
            // no network available
            Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
            // stop refresh animation from pull-to-refresh
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_READ_FOR_QUERY_PULL:
                if (isPermissionGranted(grantResults)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doQuery(true);
                        }
                    }, 200);
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(this, R.string.permission_private_key_error, Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_PERMISSION_READ_FOR_QUERY_NO_PULL:
                if (isPermissionGranted(grantResults)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doQuery(false);
                        }
                    }, 200);
                } else {
                    Toast.makeText(this, R.string.permission_private_key_error, Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_PERMISSION_READ_FOR_HALT:
                if (isPermissionGranted(grantResults)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doRebootOrHalt(Constants.TYPE_HALT);
                        }
                    }, 200);
                } else {
                    Toast.makeText(this, R.string.permission_private_key_error, Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_PERMISSION_READ_FOR_REBOOT:
                if (isPermissionGranted(grantResults)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doRebootOrHalt(Constants.TYPE_REBOOT);
                        }
                    }, 200);
                } else {
                    Toast.makeText(this, R.string.permission_private_key_error, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private boolean isPermissionGranted(int[] grantResults) {
        return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    private LoadAveragePeriod getLoadAveragePreference() {
        final String loadAvgPrefString = sharedPrefs.getString("pref_load_avg", "FIVE_MINUTES");
        LoadAveragePeriod period;
        switch (loadAvgPrefString) {
            case "ONE_MINUTE":
                period = LoadAveragePeriod.ONE_MINUTE;
                break;
            case "FIVE_MINUTES":
                period = LoadAveragePeriod.FIVE_MINUTES;
                break;
            case "FIFTEEN_MINUTES":
                period = LoadAveragePeriod.FIFTEEN_MINUTES;
                break;
            default:
                period = LoadAveragePeriod.FIVE_MINUTES;
                break;
        }
        LOGGER.debug("Load average preference: {}", period);
        return period;
    }

    @Override
    public boolean onNavigationItemSelected(final int itemPosition, long itemId) {
        LOGGER.debug("Spinner item selected: pos=" + itemPosition + ", id="
                + itemId);
        new AsyncTask<Long, Void, RaspberryDeviceBean>() {
            @Override
            protected RaspberryDeviceBean doInBackground(Long... params) {
                // get device with id
                return deviceDb.read(params[0]);
            }

            @Override
            protected void onPostExecute(RaspberryDeviceBean read) {
                if (currentDevice == null) {
                    currentDevice = read;
                } else {
                    // set current device only when device has changed (query
                    // data get
                    // lost otherwise)
                    if (read.getId() != currentDevice.getId()) {
                        LOGGER.debug("Switch from device id {} to device id {}.", currentDevice.getId(), read.getId());
                        currentDevice = read;
                        // switched to other device
                        // check if last query data for new device is present
                        boolean lastQueryPresent = false;
                        if (allDevices != null) {
                            RaspberryDeviceBean deviceBean = allDevices.get(currentDevice.getId());
                            if (deviceBean != null) {
                                if (deviceBean.getLastQueryData() != null && deviceBean.getLastQueryData().getException() == null) {
                                    currentDevice.setLastQueryData(deviceBean.getLastQueryData());
                                    updateQueryDataInView(currentDevice.getLastQueryData());
                                    lastQueryPresent = true;
                                }
                            }
                        }
                        if (!lastQueryPresent) {
                            resetView();
                        }
                    } else {
                        // device was maybe updated
                        if (currentDevice.getLastQueryData() != null) {
                            final QueryBean data = currentDevice.getLastQueryData();
                            currentDevice = read;
                            currentDevice.setLastQueryData(data);
                        } else {
                            currentDevice = read;
                        }
                    }
                }
                if (currentDevice != null) {
                    currentDevice.setSpinnerPosition(itemPosition);
                }
                // refresh options menu
                supportInvalidateOptionsMenu();
                // if current device == null (if only device was deleted), start new raspi activity
                if (currentDevice == null) {
                    Toast.makeText(MainActivity.this, R.string.please_add_a_raspberry_pi, Toast.LENGTH_LONG).show();
                    Intent newRaspiIntent = new Intent(MainActivity.this, NewRaspiActivity.class);
                    startActivity(newRaspiIntent);
                }
            }
        }.execute(itemId);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // saving query data of current device
        if (currentDevice != null) {
            LOGGER.debug("Saving instance state (current device)");
            outState.putSerializable(CURRENT_DEVICE, currentDevice);
            if (allDevices == null) {
                LOGGER.debug("Saving new instance of all devices.");
                allDevices = new SparseArray<RaspberryDeviceBean>();
                allDevices.put(currentDevice.getId(), currentDevice);
            } else {
                LOGGER.debug("Adding current device to all devices.");
                allDevices.put(currentDevice.getId(), currentDevice);
            }
        }
        if (allDevices != null) {
            outState.putSparseParcelableArray(ALL_DEVICES, allDevices);
        }
        outState.putString("bug:fix", "no empty outstate");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getSerializable(CURRENT_DEVICE) != null) {
            LOGGER.debug("Restoring device..");
            currentDevice = (RaspberryDeviceBean) savedInstanceState.getSerializable(CURRENT_DEVICE);
            // restoring tables
            LOGGER.debug("Setting spinner to show last Pi.");
            this.getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            this.getSupportActionBar().setSelectedNavigationItem(currentDevice.getSpinnerPosition());
            if (currentDevice.getLastQueryData() != null
                    && currentDevice.getLastQueryData().getException() == null) {
                LOGGER.debug("Restoring query data..");
                this.updateQueryDataInView(currentDevice.getLastQueryData());
            } else {
                LOGGER.debug("No last query data present.");
                this.resetView();
            }
        }
        if (savedInstanceState.getSparseParcelableArray(ALL_DEVICES) != null) {
            LOGGER.debug("Restoring all devices.");
            allDevices = savedInstanceState
                    .getSparseParcelableArray(ALL_DEVICES);
        }
    }

    @Override
    public void onHaltClick(DialogInterface dialog) {
        LOGGER.trace("ShutdownDialog: Halt chosen.");
        this.doRebootOrHalt(Constants.TYPE_HALT);
    }

    @Override
    public void onRebootClick(DialogInterface dialog) {
        LOGGER.trace("ShutdownDialog: Reboot chosen.");
        this.doRebootOrHalt(Constants.TYPE_REBOOT);
    }

    @Override
    public void onPassphraseOKClick(DialogFragment dialog, String passphrase,
                                    boolean savePassphrase, String type) {
        if (savePassphrase) {
            // save passphrase in db
            LOGGER.debug("Saving passphrase for device {}.", currentDevice.getName());
            currentDevice.setKeyfilePass(passphrase);
            new Thread() {
                @Override
                public void run() {
                    deviceDb.update(currentDevice);
                }
            }.start();
        }
        if (type.equals(PassphraseDialog.SSH_QUERY)) {
            // connect
            final Boolean hideRoot = sharedPrefs.getBoolean(SettingsActivity.KEY_PREF_QUERY_HIDE_ROOT_PROCESSES, true);
            new SSHQueryTask(this, getLoadAveragePreference()).execute(currentDevice.getHost(), currentDevice.getUser(), null,
                    currentDevice.getPort() + "", hideRoot.toString(),
                    currentDevice.getKeyfilePath(), passphrase);
        } else if (type.equals(PassphraseDialog.SSH_SHUTDOWN)) {
            new SSHShutdownTask(this).execute(currentDevice.getHost(),
                    currentDevice.getUser(), null,
                    currentDevice.getPort() + "", currentDevice.getSudoPass(),
                    Constants.TYPE_REBOOT, currentDevice.getKeyfilePath(),
                    passphrase);
        } else if (type.equals(PassphraseDialog.SSH_HALT)) {
            new SSHShutdownTask(this).execute(currentDevice.getHost(),
                    currentDevice.getUser(), null,
                    currentDevice.getPort() + "", currentDevice.getSudoPass(),
                    Constants.TYPE_HALT, currentDevice.getKeyfilePath(),
                    passphrase);
        }
    }

    @Override
    public void onPassphraseCancelClick() {
        // update and reset pullToRefresh
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        switch (requestCode) {
            case NewRaspiActivity.REQUEST_SAVE:
                initSpinner();
                break;
            case EditRaspiActivity.REQUEST_EDIT:
                initSpinner();
                break;
            default:
                break;
        }
    }

    /**
     * Gets called when Command Button is clicked. Starts activity for custom
     * Commands.
     *
     * @param view
     */
    public void onCommandButtonClick(View view) {
        switch (view.getId()) {
            case R.id.commandButton:
                Bundle currPi = new Bundle();
                currPi.putSerializable("pi", currentDevice);
                Intent commandIntent = new Intent(MainActivity.this, CustomCommandActivity.class);
                commandIntent.putExtras(currPi);
                this.startActivity(commandIntent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deviceDb != null) {
            deviceDb.close();
        }
    }

    @Override
    public void onQueryFinished(QueryBean result) {
        currentDevice.setLastQueryData(result);
        // update and reset pullToRefresh
        swipeRefreshLayout.setRefreshing(false);
        if (result.getException() == null) {
            // update view data
            this.updateQueryDataInView(result);
            // update entry in allDevices-Map
            if (allDevices != null) {
                allDevices.put(this.currentDevice.getId(), this.currentDevice);
            } else {
                allDevices = new SparseArray<>();
                allDevices.put(this.currentDevice.getId(), this.currentDevice);
            }
        } else {
            this.handleQueryException(result.getException());
        }

    }

    @Override
    public void onQueryProgress(int progress) {
        //progressBar.setProgress(progress);
    }

    @Override
    public void onShutdownFinished(ShutdownResult result) {
        if (result.getType().equals(Constants.TYPE_REBOOT)) {
            if (result.getExcpetion() == null) {
                Toast.makeText(this, R.string.reboot_success, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.reboot_fail, Toast.LENGTH_LONG).show();
            }
        } else if (result.getType().equals(Constants.TYPE_HALT)) {
            if (result.getExcpetion() == null) {
                Toast.makeText(this, R.string.halt_success, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.halt_fail, Toast.LENGTH_LONG).show();
            }
        }

    }
}
