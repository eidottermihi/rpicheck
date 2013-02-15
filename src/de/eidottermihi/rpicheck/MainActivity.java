package de.eidottermihi.rpicheck;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.eidottermihi.raspitools.RaspiQuery;
import de.eidottermihi.raspitools.RaspiQueryException;
import de.eidottermihi.raspitools.beans.DiskUsageBean;
import de.eidottermihi.raspitools.beans.RaspiMemoryBean;
import de.eidottermihi.raspitools.beans.UptimeBean;

// TODO reboot command
// TODO wipe all raspis action from preferences (optional)
public class MainActivity extends SherlockFragmentActivity implements
		ActionBar.OnNavigationListener {
	protected static final String EXTRA_DEVICE_ID = "device_id";

	public static final String KEY_PREFERENCES_SHOWN = "key_prefs_preferences_shown";

	private static final String LOG_TAG = "MAIN";

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
	private TextView ipAddrText;
	private TextView distriText;
	private TableLayout diskTable;
	private ProgressBar progressBar;

	private SharedPreferences sharedPrefs;

	private QueryBean queryData;

	private DeviceDbHelper deviceDb;

	private RaspberryDeviceBean currentDevice;

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

	private MenuItem refreshItem;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// assigning Shared Preferences
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
		lastUpdateText = (TextView) findViewById(R.id.lastUpdateText);
		uptimeText = (TextView) findViewById(R.id.startedText);
		averageLoadText = (TextView) findViewById(R.id.loadText);
		totalMemoryText = (TextView) findViewById(R.id.totalMemText);
		freeMemoryText = (TextView) findViewById(R.id.freeMemText);
		serialNoText = (TextView) findViewById(R.id.cpuSerialText);
		ipAddrText = (TextView) findViewById(R.id.ipAddrText);
		distriText = (TextView) findViewById(R.id.distriText);
		diskTable = (TableLayout) findViewById(R.id.diskTable);

		// init device database
		deviceDb = new DeviceDbHelper(this);
		// init device cursor
		deviceCursor = deviceDb.getFullDeviceCursor();

		// init spinner
		initSpinner();
	}

	private void updateQueryDataInView() {
		String tempScale = sharedPrefs.getString(
				SettingsActivity.KEY_PREF_TEMPERATURE_SCALE,
				getString(R.string.pref_temperature_scala_default));
		coreTempText.setText(queryData.getTempCore().toString() + " "
				+ tempScale);
		armFreqText.setText(queryData.getFreqArm().toString());
		coreFreqText.setText(queryData.getFreqCore().toString());
		coreVoltText.setText(queryData.getVolts().toString());
		lastUpdateText.setText(SimpleDateFormat.getDateTimeInstance().format(
				queryData.getLastUpdate()));
		uptimeText.setText(queryData.getStartup());
		averageLoadText.setText(queryData.getAvgLoad().toString());
		totalMemoryText.setText(queryData.getTotalMem().humanReadableByteCount(false));
		freeMemoryText.setText(queryData.getFreeMem().humanReadableByteCount(false));
		serialNoText.setText(queryData.getSerialNo());
		ipAddrText.setText(queryData.getIpAddr());
		distriText.setText(queryData.getDistri());
		updateDiskTable(queryData.getDisks());
	}

	private void updateDiskTable(List<DiskUsageBean> disks) {
		// remove current rows except header row
		diskTable.removeViews(1, diskTable.getChildCount() - 1);
		for (de.eidottermihi.raspitools.beans.DiskUsageBean diskUsageBean : disks) {
			// new Textviews (code looks ugly, refactor sometimes..)
			TextView tempText = new TextView(this);
			tempText.setText(diskUsageBean.getFileSystem());
			tempText.setFreezesText(true);
			TextView tempSizeText = new TextView(this);
			tempSizeText.setText(diskUsageBean.getSize());
			tempSizeText.setFreezesText(true);
			TextView tempAvailText = new TextView(this);
			tempAvailText.setText(diskUsageBean.getAvailable());
			tempAvailText.setFreezesText(true);
			TextView tempUsedText = new TextView(this);
			tempUsedText.setText(diskUsageBean.getUsedPercent());
			tempUsedText.setFreezesText(true);
			TextView tempMountedOnText = new TextView(this);
			tempMountedOnText.setText(diskUsageBean.getMountedOn());
			tempMountedOnText.setFreezesText(true);
			// new TableRow
			TableRow tempRow = new TableRow(this);
			tempRow.addView(tempText);
			tempRow.addView(tempSizeText);
			tempRow.addView(tempAvailText);
			tempRow.addView(tempUsedText);
			tempRow.addView(tempMountedOnText);
			// add row to table
			diskTable.addView(tempRow);
		}
	}

	private void initSpinner() {
		Log.d(LOG_TAG, "Cursor rows: " + deviceCursor.getCount());
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
		// update refresh indicator
		refreshItem.setActionView(null);
		if (queryData.getStatus() == QueryStatus.OK) {
			// update view data
			this.updateQueryDataInView();
		} else if (queryData.getStatus() == QueryStatus.AuthenticationFailure) {
			Toast.makeText(this, R.string.authentication_failed,
					Toast.LENGTH_LONG).show();
		} else if (queryData.getStatus() == QueryStatus.ConnectionFailure) {
			Toast.makeText(this, R.string.connection_failed, Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		// assign refresh button to field
		refreshItem = menu.findItem(R.id.menu_refresh);
		// set delete and edit visible if there is a current device
		if (currentDevice != null) {
			menu.findItem(R.id.menu_delete).setVisible(true);
			menu.findItem(R.id.menu_edit_raspi).setVisible(true);
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
			this.doQuery(item);
			break;
		case R.id.menu_new_raspi:
			this.startActivity(newRaspiIntent);
			break;
		case R.id.menu_delete:
			this.deleteCurrentDevice();
			this.initSpinner();
			break;
		case R.id.menu_edit_raspi:
			Bundle extras = new Bundle();
			extras.putInt(EXTRA_DEVICE_ID, currentDevice.getId());
			editRaspiIntent.putExtras(extras);
			this.startActivity(editRaspiIntent);
			break;
		// case R.id.menu_reboot:
		// this.doReboot();
		// break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void deleteCurrentDevice() {
		Log.i(LOG_TAG,
				"User wants to delete device with id = "
						+ currentDevice.getId() + ".");
		deviceDb.delete(currentDevice.getId());
	}

	private void doQuery(MenuItem item) {
		if (currentDevice == null) {
			// no device available, show hint for user
			Toast.makeText(this, R.string.no_device_available,
					Toast.LENGTH_LONG).show();
			return;
		}
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			// show progressbar
			progressBar.setVisibility(View.VISIBLE);
			// animate refresh button
			item.setActionView(R.layout.action_button_refresh);
			// get connection settings from shared preferences
			String host = currentDevice.getHost();
			String user = currentDevice.getUser();
			String pass = currentDevice.getPass();
			String port = currentDevice.getPort() + "";
			// reading temperature preference
			String tempPref = sharedPrefs.getString(
					SettingsActivity.KEY_PREF_TEMPERATURE_SCALE, "C");
			// get connection from current device in dropdown
			if (host == null) {
				Toast.makeText(this, R.string.no_hostname_specified,
						Toast.LENGTH_LONG);
			} else if (user == null) {
				Toast.makeText(this, R.string.no_username_specified,
						Toast.LENGTH_LONG);
			} else if (pass == null) {
				Toast.makeText(this, R.string.no_password_specified,
						Toast.LENGTH_LONG);
			} else {
				// execute query
				new SSHQueryTask().execute(host, user, pass, port, tempPref);
			}
		} else {
			Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT)
					.show();
		}
	}

	private class SSHQueryTask extends AsyncTask<String, Integer, QueryBean> {

		@Override
		protected QueryBean doInBackground(String... params) {
			// create and do query
			raspiQuery = new RaspiQuery((String) params[0], (String) params[1],
					(String) params[2], Integer.parseInt(params[3]));
			QueryBean bean = new QueryBean();
			try {
				publishProgress(5);
				raspiQuery.connect();
				publishProgress(10);
				Double tempCore = raspiQuery
						.queryCpuTemp(params[4].equals("C") ? RaspiQuery.TEMP_CELSIUS
								: RaspiQuery.TEMP_FAHRENHEIT);
				publishProgress(20);
				Double armFreq = raspiQuery.queryFreq(RaspiQuery.FREQ_ARM,
						RaspiQuery.FREQUENCY_MHZ);
				Double coreFreq = raspiQuery.queryFreq(RaspiQuery.FREQ_CORE,
						RaspiQuery.FREQUENCY_MHZ);
				publishProgress(30);
				Double volts = raspiQuery.queryVolts();
				publishProgress(40);
				UptimeBean uptime = raspiQuery.queryUptime();
				publishProgress(50);
				String avgLoad = uptime.getAverageLoad();
				String prettyUptime = uptime.getRunningPretty();
				publishProgress(60);
				RaspiMemoryBean memory = raspiQuery.queryMemoryInformation();
				publishProgress(70);
				String serialNo = raspiQuery.queryCpuSerial();
				publishProgress(80);
				String ipAddr = raspiQuery.queryEth0IpAddr();
				publishProgress(90);
				bean.setDisks(raspiQuery.queryDiskUsage());
				publishProgress(95);
				bean.setDistri(raspiQuery.queryDistributionName());
				raspiQuery.disconnect();
				publishProgress(100);
				bean.setTempCore(tempCore);
				bean.setFreqArm(armFreq);
				bean.setFreqCore(coreFreq);
				bean.setVolts(volts);
				bean.setLastUpdate(Calendar.getInstance().getTime());
				bean.setStartup(prettyUptime);
				bean.setAvgLoad(avgLoad);
				bean.setFreeMem(memory.getTotalFree());
				bean.setTotalMem(memory.getTotalMemory());
				bean.setSerialNo(serialNo);
				bean.setIpAddr(ipAddr);
				bean.setStatus(QueryStatus.OK);
				return bean;
			} catch (RaspiQueryException e) {
				Log.e(LOG_TAG, e.getMessage());
				return this.handleException(e);
			} catch (IOException e) {
				Log.e(LOG_TAG, e.getMessage());
			}
			return null;
		}

		private QueryBean handleException(RaspiQueryException e) {
			QueryBean queryBean = new QueryBean();
			switch (e.getReasonCode()) {
			case RaspiQueryException.REASON_CONNECTION_FAILED:
				queryBean.setStatus(QueryStatus.ConnectionFailure);
				break;
			case RaspiQueryException.REASON_AUTHENTIFICATION_FAILED:
				queryBean.setStatus(QueryStatus.AuthenticationFailure);
				break;
			case RaspiQueryException.REASON_TRANSPORT_EXCEPTION:
				queryBean.setStatus(QueryStatus.ConnectionFailure);
			}
			return queryBean;
		}

		@Override
		protected void onPostExecute(QueryBean result) {
			// update query data
			queryData = result;
			// inform handler
			mHandler.post(mUpdateResults);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			Integer totalProgress = values[0];
			progressBar.setProgress(totalProgress);
			super.onProgressUpdate(values);
		}

	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		Log.d(LOG_TAG, "DropdownItemSelected: pos=" + itemPosition + ", id="
				+ itemId);
		// get device with id
		RaspberryDeviceBean read = deviceDb.read(itemId);
		this.currentDevice = read;
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

}
