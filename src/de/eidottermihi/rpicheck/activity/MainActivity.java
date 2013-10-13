package de.eidottermihi.rpicheck.activity;

import java.io.File;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sheetrock.panda.changelog.ChangeLog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;

import de.eidottermihi.rpicheck.R;
import de.eidottermihi.rpicheck.activity.helper.Helper;
import de.eidottermihi.rpicheck.activity.helper.LoggingHelper;
import de.eidottermihi.rpicheck.beans.DiskUsageBean;
import de.eidottermihi.rpicheck.beans.NetworkInterfaceInformation;
import de.eidottermihi.rpicheck.beans.ProcessBean;
import de.eidottermihi.rpicheck.beans.QueryBean;
import de.eidottermihi.rpicheck.beans.RaspiMemoryBean;
import de.eidottermihi.rpicheck.beans.ShutdownResult;
import de.eidottermihi.rpicheck.beans.UptimeBean;
import de.eidottermihi.rpicheck.beans.VcgencmdBean;
import de.eidottermihi.rpicheck.beans.WlanBean;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;
import de.eidottermihi.rpicheck.db.RaspberryDeviceBean;
import de.eidottermihi.rpicheck.fragment.PassphraseDialog;
import de.eidottermihi.rpicheck.fragment.QueryErrorMessagesDialog;
import de.eidottermihi.rpicheck.fragment.QueryExceptionDialog;
import de.eidottermihi.rpicheck.fragment.RebootDialogFragment;
import de.eidottermihi.rpicheck.fragment.PassphraseDialog.PassphraseDialogListener;
import de.eidottermihi.rpicheck.fragment.RebootDialogFragment.ShutdownDialogListener;
import de.eidottermihi.rpicheck.ssh.RaspiQuery;
import de.eidottermihi.rpicheck.ssh.RaspiQueryException;

public class MainActivity extends SherlockFragmentActivity implements
		ActionBar.OnNavigationListener, OnRefreshListener<ScrollView>,
		ShutdownDialogListener, PassphraseDialogListener {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MainActivity.class);

	protected static final String EXTRA_DEVICE_ID = "device_id";

	private static final String CURRENT_DEVICE = "currentDevice";
	private static final String ALL_DEVICES = "allDevices";

	private static final String TYPE_REBOOT = "reboot";
	private static final String TYPE_HALT = "halt";

	private final Helper helper = new Helper();

	private Intent settingsIntent;
	private Intent newRaspiIntent;
	private Intent editRaspiIntent;
	private RaspiQuery raspiQuery;

	private TextView coreTempText;
	private TextView armFreqText;
	private TextView coreFreqText;
	private TextView coreVoltText;
	private TextView lastUpdateText;
	private TextView uptimeText;
	private TextView averageLoadText;
	private TextView totalMemoryText;
	private TextView freeMemoryText;
	private TextView serialNoText;
	private TextView distriText;
	private TextView firmwareText;
	private TableLayout diskTable;
	private TableLayout processTable;
	private TableLayout networkTable;
	private ProgressBar progressBar;
	private PullToRefreshScrollView refreshableScrollView;

	private SharedPreferences sharedPrefs;

	private ShutdownResult shutdownResult;

	private DeviceDbHelper deviceDb;

	private RaspberryDeviceBean currentDevice;
	private SparseArray<RaspberryDeviceBean> allDevices;

	private Cursor deviceCursor;

	// Need handler for callbacks to the UI thread
	final Handler mHandler = new Handler();

	// Create runnable for posting
	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			// gets called from AsyncTask when data was queried
			updateResultsInUi();
		}
	};

	// Runnable for reboot result
	final Runnable mRebootResult = new Runnable() {
		public void run() {
			// gets called from AsyncTask when reboot command was sent
			afterShutdown();
		}

	};

	private MenuItem refreshItem;

	static {
		Security.insertProviderAt(
				new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// LOGGER.debug("onCreate()....");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// assigning Shared Preferences
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		// init intents
		settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
		newRaspiIntent = new Intent(MainActivity.this, NewRaspiActivity.class);
		editRaspiIntent = new Intent(MainActivity.this, EditRaspiActivity.class);

		// assigning progressbar
		progressBar = (ProgressBar) findViewById(R.id.progressBar1);

		// assigning textviews to fields
		armFreqText = (TextView) findViewById(R.id.armFreqText);
		coreFreqText = (TextView) findViewById(R.id.coreFreqText);
		coreVoltText = (TextView) findViewById(R.id.coreVoltText);
		coreTempText = (TextView) findViewById(R.id.coreTempText);
		firmwareText = (TextView) findViewById(R.id.firmwareText);
		lastUpdateText = (TextView) findViewById(R.id.lastUpdateText);
		uptimeText = (TextView) findViewById(R.id.startedText);
		averageLoadText = (TextView) findViewById(R.id.loadText);
		totalMemoryText = (TextView) findViewById(R.id.totalMemText);
		freeMemoryText = (TextView) findViewById(R.id.freeMemText);
		serialNoText = (TextView) findViewById(R.id.cpuSerialText);
		distriText = (TextView) findViewById(R.id.distriText);
		diskTable = (TableLayout) findViewById(R.id.diskTable);
		processTable = (TableLayout) findViewById(R.id.processTable);
		networkTable = (TableLayout) findViewById(R.id.networkTable);

		// assigning refreshable root scrollview
		refreshableScrollView = (PullToRefreshScrollView) findViewById(R.id.scrollView1);
		refreshableScrollView.setOnRefreshListener(this);

		// init device database
		deviceDb = new DeviceDbHelper(this);
		// init spinner
		initSpinner();

		boolean debugLogging = sharedPrefs.getBoolean(
				SettingsActivity.KEY_PREF_DEBUG_LOGGING, false);
		if (debugLogging) {
			LoggingHelper.changeLogger(true);
		}

		// changelog
		final ChangeLog cl = new ChangeLog(this);
		if (cl.firstRun())
			cl.getLogDialog().show();
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
		updateDiskTable();
		updateNetworkTable();
		updateProcessTable();
	}

	private void updateQueryDataInView() {
		final List<String> errorMessages = currentDevice.getLastQueryData()
				.getErrorMessages();
		final String tempScale = sharedPrefs.getString(
				SettingsActivity.KEY_PREF_TEMPERATURE_SCALE,
				getString(R.string.pref_temperature_scala_default));
		coreTempText.setText(helper.formatTemperature(currentDevice
				.getLastQueryData().getVcgencmdInfo().getCpuTemperature(),
				tempScale));
		final String freqScale = sharedPrefs.getString(
				SettingsActivity.KEY_PREF_FREQUENCY_UNIT,
				getString(R.string.pref_frequency_unit_default));
		armFreqText.setText(helper.formatFrequency(currentDevice
				.getLastQueryData().getVcgencmdInfo().getArmFrequency(),
				freqScale));
		coreFreqText.setText(helper.formatFrequency(currentDevice
				.getLastQueryData().getVcgencmdInfo().getCoreFrequency(),
				freqScale));
		coreVoltText.setText(helper.formatDecimal(currentDevice
				.getLastQueryData().getVcgencmdInfo().getCoreVolts()));
		firmwareText.setText(currentDevice.getLastQueryData().getVcgencmdInfo()
				.getVersion());
		lastUpdateText.setText(SimpleDateFormat.getDateTimeInstance().format(
				currentDevice.getLastQueryData().getLastUpdate()));
		// uptime and average load may contain errors
		if (currentDevice.getLastQueryData().getAvgLoad() != null) {
			averageLoadText.setText(currentDevice.getLastQueryData()
					.getAvgLoad().toString());
		}
		if (currentDevice.getLastQueryData().getStartup() != null) {
			uptimeText.setText(currentDevice.getLastQueryData().getStartup());
		}
		if (currentDevice.getLastQueryData().getFreeMem() != null) {
			freeMemoryText.setText(currentDevice.getLastQueryData()
					.getFreeMem().humanReadableByteCount(false));
		}
		if (currentDevice.getLastQueryData().getTotalMem() != null) {
			totalMemoryText.setText(currentDevice.getLastQueryData()
					.getTotalMem().humanReadableByteCount(false));
		}
		serialNoText.setText(currentDevice.getLastQueryData().getSerialNo());
		distriText.setText(currentDevice.getLastQueryData().getDistri());
		// update tables
		updateNetworkTable();
		updateDiskTable();
		updateProcessTable();
		this.handleQueryError(errorMessages);
	}

	/**
	 * Shows a dialog containing the ErrorMessages.
	 * 
	 * @param errorMessages
	 *            the messages
	 */
	private void handleQueryError(List<String> errorMessages) {
		final ArrayList<String> messages = new ArrayList<String>(errorMessages);
		if (errorMessages.size() > 0) {
			LOGGER.trace("Showing query error messages.");
			Bundle args = new Bundle();
			args.putStringArrayList(
					QueryErrorMessagesDialog.KEY_ERROR_MESSAGES, messages);
			final QueryErrorMessagesDialog messageDialog = new QueryErrorMessagesDialog();
			messageDialog.setArguments(args);
			messageDialog.show(getSupportFragmentManager(),
					"QueryErrorMessagesDialog");
		}
	}

	private void updateNetworkTable() {
		// remove rows except header
		networkTable.removeViews(1, networkTable.getChildCount() - 1);
		if (currentDevice != null && currentDevice.getLastQueryData() != null) {
			for (NetworkInterfaceInformation interfaceInformation : currentDevice
					.getLastQueryData().getNetworkInfo()) {
				networkTable.addView(createNetworkRow(interfaceInformation));
			}

		}
	}

	private View createNetworkRow(
			NetworkInterfaceInformation interfaceInformation) {
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
			tempRow.addView(createTextView(
					helper.formatPercentage(wlan.getSignalLevel()), 3));
			tempRow.addView(createTextView(
					helper.formatPercentage(wlan.getLinkQuality()), 3));
		} else {
			tempRow.addView(createTextView(" - ", 3));
			tempRow.addView(createTextView(" - ", 3));
		}
		return tempRow;
	}

	private void updateProcessTable() {
		// remove current rows except header row
		processTable.removeViews(1, processTable.getChildCount() - 1);
		if (currentDevice != null && currentDevice.getLastQueryData() != null) {
			for (ProcessBean processBean : currentDevice.getLastQueryData()
					.getProcesses()) {
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
		return createTextView(text, null);
	}

	private View createTextView(String text, Integer paddingLeft) {
		final TextView tempText = new TextView(this);
		tempText.setText(text);
		if (paddingLeft != null) {
			float pix = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
					paddingLeft.intValue(), getResources().getDisplayMetrics());
			tempText.setPadding((int) (pix), 0, 0, 0);
		}
		return tempText;
	}

	private void updateDiskTable() {
		// remove current rows except header row
		diskTable.removeViews(1, diskTable.getChildCount() - 1);
		if (currentDevice != null && currentDevice.getLastQueryData() != null) {
			for (DiskUsageBean diskUsageBean : currentDevice.getLastQueryData()
					.getDisks()) {
				// add row to table
				diskTable.addView(createDiskRow(diskUsageBean));
			}
		}
	}

	private void initSpinner() {
		deviceCursor = deviceDb.getFullDeviceCursor();
		LOGGER.trace("Cursor rows: " + deviceCursor.getCount());
		// only show spinner if theres already a device to show
		if (deviceCursor.getCount() > 0) {
			// make adapter
			@SuppressWarnings("deprecation")
			SimpleCursorAdapter spinadapter = new SimpleCursorAdapter(this,
					R.layout.sherlock_spinner_dropdown_item, deviceCursor,
					new String[] { "name", "_id" },
					new int[] { android.R.id.text1 });
			spinadapter
					.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
			this.getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_LIST);
			this.getSupportActionBar().setListNavigationCallbacks(spinadapter,
					this);
			this.getSupportActionBar().setDisplayShowTitleEnabled(false);
		} else {
			// no device, start activity to create a new
			Toast.makeText(this, R.string.please_add_a_raspberry_pi,
					Toast.LENGTH_LONG).show();
			this.startActivity(newRaspiIntent);
		}
	}

	private void updateResultsInUi() {
		// hide and reset progress bar
		progressBar.setVisibility(View.GONE);
		progressBar.setProgress(0);
		// update and reset pullToRefresh
		refreshableScrollView.onRefreshComplete();
		refreshableScrollView.setMode(Mode.PULL_FROM_START);
		// update refresh indicator
		refreshItem.setActionView(null);
		if (currentDevice.getLastQueryData().getException() == null) {
			// update view data
			this.updateQueryDataInView();
			// update entry in allDevices-Map
			if (allDevices != null) {
				allDevices.put(this.currentDevice.getId(), this.currentDevice);
			} else {
				allDevices = new SparseArray<RaspberryDeviceBean>();
				allDevices.put(this.currentDevice.getId(), this.currentDevice);
			}
		} else {
			this.handleQueryException(currentDevice.getLastQueryData()
					.getException());
		}
	}

	/**
	 * Shows a dialog with a detailed error message.
	 * 
	 * @param exception
	 */
	private void handleQueryException(RaspiQueryException exception) {
		LOGGER.trace("Query caused exception. Showing dialog.");
		// build dialog
		final String errorMessage = this.mapExceptionToErrorMessage(exception);
		Bundle dialogArgs = new Bundle();
		dialogArgs.putString(QueryExceptionDialog.MESSAGE_KEY, errorMessage);
		QueryExceptionDialog dialogFragment = new QueryExceptionDialog();
		dialogFragment.setArguments(dialogArgs);
		dialogFragment
				.show(getSupportFragmentManager(), "QueryExceptionDialog");
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

	/**
	 * Just show Toast.
	 */
	private void afterShutdown() {
		if (shutdownResult.getType().equals(TYPE_REBOOT)) {
			if (shutdownResult.getExcpetion() == null) {
				Toast.makeText(this, R.string.reboot_success, Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(this, R.string.reboot_fail, Toast.LENGTH_LONG)
						.show();
			}
		} else if (shutdownResult.getType().equals(TYPE_HALT)) {
			if (shutdownResult.getExcpetion() == null) {
				Toast.makeText(this, R.string.halt_success, Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(this, R.string.halt_fail, Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		// assign refresh button to field
		refreshItem = menu.findItem(R.id.menu_refresh);
		// set delete, edit and reboot visible if there is a current device
		if (currentDevice != null) {
			LOGGER.trace("Enabling menu buttons.");
			menu.findItem(R.id.menu_delete).setVisible(true);
			menu.findItem(R.id.menu_edit_raspi).setVisible(true);
			menu.findItem(R.id.menu_reboot).setVisible(true);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			this.startActivity(settingsIntent);
			break;
		case R.id.menu_refresh:
			// do query
			refreshItem = item;
			this.doQuery(false);
			break;
		case R.id.menu_new_raspi:
			this.startActivity(newRaspiIntent);
			break;
		case R.id.menu_delete:
			this.deleteCurrentDevice();
			this.initSpinner();
			break;
		case R.id.menu_edit_raspi:
			final Bundle extras = new Bundle();
			extras.putInt(EXTRA_DEVICE_ID, currentDevice.getId());
			editRaspiIntent.putExtras(extras);
			this.startActivity(editRaspiIntent);
			break;
		case R.id.menu_reboot:
			this.showRebootDialog();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showRebootDialog() {
		LOGGER.trace("Showing reboot dialog.");
		DialogFragment rebootDialog = new RebootDialogFragment();
		rebootDialog.show(getSupportFragmentManager(), "reboot");
	}

	private void doRebootOrHalt(String type) {
		LOGGER.info("Doing {} on {}...", type, currentDevice.getName());
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			// get connection settings from shared preferences
			final String host = currentDevice.getHost();
			final String user = currentDevice.getUser();
			final String port = currentDevice.getPort() + "";
			final String sudoPass = currentDevice.getSudoPass();
			if (currentDevice.getAuthMethod().equals(
					NewRaspiAuthActivity.SPINNER_AUTH_METHODS[0])) {
				// ssh password
				final String pass = currentDevice.getPass();
				new SSHShutdownTask().execute(host, user, pass, port, sudoPass,
						type, null, null);
			} else if (currentDevice.getAuthMethod().equals(
					NewRaspiAuthActivity.SPINNER_AUTH_METHODS[1])) {
				// keyfile
				final String keyfilePath = currentDevice.getKeyfilePath();
				if (keyfilePath != null) {
					final File privateKey = new File(keyfilePath);
					if (privateKey.exists()) {
						new SSHShutdownTask().execute(host, user, null, port,
								sudoPass, type, keyfilePath, null);
					} else {
						Toast.makeText(this,
								"Cannot find keyfile at location: "
										+ keyfilePath, Toast.LENGTH_LONG);
					}
				} else {
					Toast.makeText(this, "No keyfile specified!",
							Toast.LENGTH_LONG);
				}
			} else if (currentDevice.getAuthMethod().equals(
					NewRaspiAuthActivity.SPINNER_AUTH_METHODS[2])) {
				// keyfile and passphrase
				final String keyfilePath = currentDevice.getKeyfilePath();
				if (keyfilePath != null) {
					final File privateKey = new File(keyfilePath);
					if (privateKey.exists()) {
						if (!StringUtils
								.isBlank(currentDevice.getKeyfilePass())) {
							final String passphrase = currentDevice
									.getKeyfilePass();
							new SSHShutdownTask().execute(host, user, null,
									port, sudoPass, type, keyfilePath,
									passphrase);
						} else {
							final String dialogType = type.equals(TYPE_REBOOT) ? PassphraseDialog.SSH_SHUTDOWN
									: PassphraseDialog.SSH_HALT;
							final DialogFragment passphraseDialog = new PassphraseDialog();
							final Bundle args = new Bundle();
							args.putString(PassphraseDialog.KEY_TYPE,
									dialogType);
							passphraseDialog.setArguments(args);
							passphraseDialog.show(getSupportFragmentManager(),
									"passphrase");
						}
					} else {
						Toast.makeText(this,
								"Cannot find keyfile at location: "
										+ keyfilePath, Toast.LENGTH_LONG);
					}
				} else {
					Toast.makeText(this, "No keyfile specified!",
							Toast.LENGTH_LONG);
				}
			}
		} else {
			Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void deleteCurrentDevice() {
		LOGGER.info("Deleting pi {}.", currentDevice.getName());
		deviceDb.delete(currentDevice.getId());
		if (allDevices != null) {
			allDevices.delete(currentDevice.getId());
		}
	}

	private void doQuery(boolean initByPullToRefresh) {
		if (currentDevice == null) {
			// no device available, show hint for user
			Toast.makeText(this, R.string.no_device_available,
					Toast.LENGTH_LONG).show();
			// stop refresh animation from pull-to-refresh
			refreshableScrollView.onRefreshComplete();
			return;
		}
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			// show progressbar
			progressBar.setVisibility(View.VISIBLE);
			// animate refresh button
			refreshItem.setActionView(R.layout.action_button_refresh);
			// get connection settings from shared preferences
			String host = currentDevice.getHost();
			String user = currentDevice.getUser();
			String port = currentDevice.getPort() + "";
			String pass = null;
			// reading process preference
			final Boolean hideRoot = Boolean.valueOf(sharedPrefs.getBoolean(
					SettingsActivity.KEY_PREF_QUERY_HIDE_ROOT_PROCESSES, true));
			String keyPath = null;
			String keyPass = null;
			boolean canConnect = false;
			// check authentification method
			final String authMethod = currentDevice.getAuthMethod();
			if (authMethod.equals(NewRaspiAuthActivity.SPINNER_AUTH_METHODS[0])) {
				// only ssh password
				pass = currentDevice.getPass();
				if (pass != null) {
					canConnect = true;
				} else {
					Toast.makeText(this, R.string.no_password_specified,
							Toast.LENGTH_LONG);
				}
			} else if (authMethod
					.equals(NewRaspiAuthActivity.SPINNER_AUTH_METHODS[1])) {
				// keyfile must be present
				final String keyfilePath = currentDevice.getKeyfilePath();
				if (keyfilePath != null) {
					final File privateKey = new File(keyfilePath);
					if (privateKey.exists()) {
						keyPath = keyfilePath;
						canConnect = true;
					} else {
						Toast.makeText(this,
								"Cannot find keyfile at location: "
										+ keyfilePath, Toast.LENGTH_LONG);
					}
				} else {
					Toast.makeText(this, "No keyfile specified!",
							Toast.LENGTH_LONG);
				}
			} else if (authMethod
					.equals(NewRaspiAuthActivity.SPINNER_AUTH_METHODS[2])) {
				// keyfile and keypass must be present
				final String keyfilePath = currentDevice.getKeyfilePath();
				if (keyfilePath != null) {
					final File privateKey = new File(keyfilePath);
					if (privateKey.exists()) {
						keyPath = keyfilePath;
						final String keyfilePass = currentDevice
								.getKeyfilePass();
						if (keyfilePass != null) {
							canConnect = true;
							keyPass = keyfilePass;
						} else {
							final DialogFragment newFragment = new PassphraseDialog();
							final Bundle args = new Bundle();
							args.putString(PassphraseDialog.KEY_TYPE,
									PassphraseDialog.SSH_QUERY);
							newFragment.setArguments(args);
							newFragment.show(getSupportFragmentManager(),
									"passphrase");
							canConnect = false;
						}
					} else {
						Toast.makeText(this,
								"Cannot find keyfile at location: "
										+ keyfilePath, Toast.LENGTH_LONG);
					}
				} else {
					Toast.makeText(this, "No keyfile specified!",
							Toast.LENGTH_LONG);
				}
			}
			if (host == null) {
				Toast.makeText(this, R.string.no_hostname_specified,
						Toast.LENGTH_LONG);
				canConnect = false;
			} else if (user == null) {
				Toast.makeText(this, R.string.no_username_specified,
						Toast.LENGTH_LONG);
				canConnect = false;
			}
			if (canConnect) {
				// disable pullToRefresh (if refresh initiated by action bar)
				if (!initByPullToRefresh) {
					refreshableScrollView.setMode(Mode.DISABLED);
				}
				// execute query
				new SSHQueryTask().execute(host, user, pass, port,
						hideRoot.toString(), keyPath, keyPass);
			}
		} else {
			Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT)
					.show();
			// stop refresh animation from pull-to-refresh
			refreshableScrollView.onRefreshComplete();
		}
	}

	private class SSHShutdownTask extends
			AsyncTask<String, Integer, ShutdownResult> {

		/**
		 * Send the reboot command. Returns true, when no Exception was raised.
		 */
		@Override
		protected ShutdownResult doInBackground(String... params) {
			raspiQuery = new RaspiQuery((String) params[0], (String) params[1],
					Integer.parseInt(params[3]));
			final String pass = params[2];
			final String sudoPass = params[4];
			final String type = params[5];
			final String keyfile = params[6];
			final String keypass = params[7];
			ShutdownResult result = new ShutdownResult();
			result.setType(type);
			try {
				if (keyfile != null) {
					File f = new File(keyfile);
					if (keypass == null) {
						// connect with private key only
						raspiQuery.connectWithPubKeyAuth(f.getPath());
					} else {
						// connect with key and passphrase
						raspiQuery.connectWithPubKeyAuthAndPassphrase(
								f.getPath(), keypass);
					}
				} else {
					raspiQuery.connect(pass);
				}
				raspiQuery.connect(pass);
				if (type.equals(TYPE_REBOOT)) {
					raspiQuery.sendRebootSignal(sudoPass);
				} else if (type.equals(TYPE_HALT)) {
					raspiQuery.sendHaltSignal(sudoPass);
				}
				raspiQuery.disconnect();
				return result;
			} catch (RaspiQueryException e) {
				LOGGER.error(e.getMessage());
				result.setExcpetion(e);
				return result;
			}
		}

		@Override
		protected void onPostExecute(ShutdownResult result) {
			shutdownResult = result;
			// inform handler
			mHandler.post(mRebootResult);
		}

	}

	private class SSHQueryTask extends AsyncTask<String, Integer, QueryBean> {

		@Override
		protected QueryBean doInBackground(String... params) {
			// create and do query
			raspiQuery = new RaspiQuery((String) params[0], (String) params[1],
					Integer.parseInt(params[3]));
			final String pass = params[2];
			boolean hideRootProcesses = Boolean.parseBoolean(params[4]);
			final String privateKeyPath = params[5];
			final String privateKeyPass = params[6];
			QueryBean bean = new QueryBean();
			final long msStart = new Date().getTime();
			bean.setErrorMessages(new ArrayList<String>());
			try {
				publishProgress(5);
				if (privateKeyPath != null) {
					File f = new File(privateKeyPath);
					if (privateKeyPass == null) {
						// connect with private key only
						raspiQuery.connectWithPubKeyAuth(f.getPath());
					} else {
						// connect with key and passphrase
						raspiQuery.connectWithPubKeyAuthAndPassphrase(
								f.getPath(), privateKeyPass);
					}
				} else {
					raspiQuery.connect(pass);
				}
				publishProgress(20);
				final VcgencmdBean vcgencmdBean = raspiQuery.queryVcgencmd();
				publishProgress(50);
				UptimeBean uptime = raspiQuery.queryUptime();
				publishProgress(60);
				RaspiMemoryBean memory = raspiQuery.queryMemoryInformation();
				publishProgress(70);
				String serialNo = raspiQuery.queryCpuSerial();
				publishProgress(72);
				List<ProcessBean> processes = raspiQuery
						.queryProcesses(!hideRootProcesses);
				publishProgress(80);
				final List<NetworkInterfaceInformation> networkInformation = raspiQuery
						.queryNetworkInformation();
				publishProgress(90);
				bean.setDisks(raspiQuery.queryDiskUsage());
				publishProgress(95);
				bean.setDistri(raspiQuery.queryDistributionName());
				raspiQuery.disconnect();
				publishProgress(100);
				bean.setVcgencmdInfo(vcgencmdBean);
				bean.setLastUpdate(Calendar.getInstance().getTime());
				// uptimeBean may contain messages
				if (uptime.getErrorMessage() != null) {
					bean.getErrorMessages().add(uptime.getErrorMessage());
				} else {
					bean.setStartup(uptime.getRunningPretty());
					bean.setAvgLoad(uptime.getAverageLoad());
				}
				if (memory.getErrorMessage() != null) {
					bean.getErrorMessages().add(memory.getErrorMessage());
				} else {
					bean.setFreeMem(memory.getTotalFree());
					bean.setTotalMem(memory.getTotalMemory());
				}
				bean.setSerialNo(serialNo);
				bean.setNetworkInfo(networkInformation);
				bean.setProcesses(processes);
				for (String error : bean.getErrorMessages()) {
					LOGGER.error(error);
				}
			} catch (RaspiQueryException e) {
				LOGGER.error("Query failed: " + e.getMessage());
				bean.setException(e);
			}
			final long msFinish = new Date().getTime();
			final long durationInMs = msFinish - msStart;
			LOGGER.debug("Query time: {} ms.", durationInMs);
			return bean;
		}

		@Override
		protected void onPostExecute(QueryBean result) {
			// update query data
			currentDevice.setLastQueryData(result);
			// inform handler
			mHandler.post(mUpdateResults);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			final Integer totalProgress = values[0];
			progressBar.setProgress(totalProgress);
			super.onProgressUpdate(values);
		}

	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		LOGGER.debug("Spinner item selected: pos=" + itemPosition + ", id="
				+ itemId);
		// get device with id
		RaspberryDeviceBean read = deviceDb.read(itemId);
		if (currentDevice == null) {
			this.currentDevice = read;
		} else {
			// set current device only when device has changed (query data get
			// lost otherwise)
			if (read.getId() != this.currentDevice.getId()) {
				LOGGER.debug("Switch from device id {} to device id {}.",
						this.currentDevice.getId(), read.getId());
				this.currentDevice = read;
				// switched to other device
				// check if last query data for new device is present
				boolean lastQueryPresent = false;
				if (allDevices != null) {
					RaspberryDeviceBean deviceBean = allDevices
							.get(this.currentDevice.getId());
					if (deviceBean != null) {
						if (deviceBean.getLastQueryData() != null) {
							this.currentDevice.setLastQueryData(deviceBean
									.getLastQueryData());
							this.updateQueryDataInView();
							lastQueryPresent = true;
						}
					}
				}
				if (!lastQueryPresent) {
					this.resetView();
				}
			}
		}
		if (currentDevice != null) {
			this.currentDevice.setSpinnerPosition(itemPosition);
		}
		// refresh options menu
		this.supportInvalidateOptionsMenu();
		// if current device == null (if only device was deleted), start new
		// raspi activity
		if (currentDevice == null) {
			Toast.makeText(this, R.string.please_add_a_raspberry_pi,
					Toast.LENGTH_LONG).show();
			this.startActivity(newRaspiIntent);
		}
		return true;
	}

	@Override
	public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
		LOGGER.trace("Query initiated by PullToRefresh.");
		this.doQuery(true);
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
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState.getSerializable(CURRENT_DEVICE) != null) {
			LOGGER.debug("Restoring device..");
			currentDevice = (RaspberryDeviceBean) savedInstanceState
					.getSerializable(CURRENT_DEVICE);
			// restoring tables
			LOGGER.debug("Setting spinner to show last Pi.");
			this.getSupportActionBar().setSelectedNavigationItem(
					currentDevice.getSpinnerPosition());
			if (currentDevice.getLastQueryData() != null) {
				LOGGER.debug("Restoring query data..");
				this.updateQueryDataInView();
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
		this.doRebootOrHalt(TYPE_HALT);
	}

	@Override
	public void onRebootClick(DialogInterface dialog) {
		LOGGER.trace("ShutdownDialog: Reboot chosen.");
		this.doRebootOrHalt(TYPE_REBOOT);
	}

	@Override
	public void onPassphraseOKClick(DialogFragment dialog, String passphrase,
			boolean savePassphrase, String type) {
		if (savePassphrase) {
			// save passphrase in db
			LOGGER.debug("Saving passphrase for device {}.",
					currentDevice.getName());
			currentDevice.setKeyfilePass(passphrase);
			deviceDb.update(currentDevice);
		}
		if (type.equals(PassphraseDialog.SSH_QUERY)) {
			// connect
			final Boolean hideRoot = Boolean.valueOf(sharedPrefs.getBoolean(
					SettingsActivity.KEY_PREF_QUERY_HIDE_ROOT_PROCESSES, true));
			new SSHQueryTask().execute(currentDevice.getHost(),
					currentDevice.getUser(), null,
					currentDevice.getPort() + "", hideRoot.toString(),
					currentDevice.getKeyfilePath(), passphrase);
		} else if (type.equals(PassphraseDialog.SSH_SHUTDOWN)) {
			// new SSHShutdownTask().execute(params)
		} else if (type.equals(PassphraseDialog.SSH_HALT)) {

		}
	}

	@Override
	public void onPassphraseCancelClick() {
		// hide and reset progress bar
		progressBar.setVisibility(View.GONE);
		progressBar.setProgress(0);
		// update and reset pullToRefresh
		refreshableScrollView.onRefreshComplete();
		refreshableScrollView.setMode(Mode.PULL_FROM_START);
		// update refresh indicator
		refreshItem.setActionView(null);
	}

}
