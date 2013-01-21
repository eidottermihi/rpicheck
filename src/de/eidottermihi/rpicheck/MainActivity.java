package de.eidottermihi.rpicheck;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.eidottermihi.raspitools.RaspiQuery;
import de.eidottermihi.raspitools.RaspiQueryException;

public class MainActivity extends Activity {
	public static final String KEY_PREFERENCES_SHOWN = "key_prefs_preferences_shown";

	private static final String LOG_TAG = "MAIN";

	private Intent settingsIntent;
	private RaspiQuery raspiQuery;
	private TextView cpuFreqTextView;
	private TextView cpuTempTextView;
	private ProgressBar progressBar;
	private Button checkButton;

	private SharedPreferences sharedPrefs;

	private QueryBean queryData;

	// Need handler for callbacks to the UI thread
	final Handler mHandler = new Handler();

	// Create runnable for posting
	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			updateResultsInUi();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// assigning view elements to fields
		cpuFreqTextView = (TextView) findViewById(R.id.cpuFreqText);
		cpuTempTextView = (TextView) findViewById(R.id.cpuTempText);
		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		checkButton = (Button) findViewById(R.id.button1);

		// assigning Shared Preferences
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		// init settings intent
		settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);

		// check if preferences were already visited once for initial setup
		checkPreferencesVisited();
	}

	private void checkPreferencesVisited() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean preferencesShown = prefs.getBoolean(KEY_PREFERENCES_SHOWN,
				false);
		if (!preferencesShown) {
			// launch settings activity
			this.startActivity(settingsIntent);
		}
	}

	protected void updateResultsInUi() {
		// update view
		progressBar.setVisibility(ProgressBar.INVISIBLE);
		if (queryData.getStatus() == QueryStatus.OK) {
			cpuFreqTextView.setText(queryData.getCpuFrequency());
			cpuTempTextView.setText(queryData.getCpuTemperature());
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
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void onCheck(View view) {
		switch (view.getId()) {
		case R.id.button1:
			doQuery();
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			this.startActivity(settingsIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@SuppressWarnings("unchecked")
	private void doQuery() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			// show progress bar
			progressBar.setVisibility(ProgressBar.VISIBLE);
			// get connection settings from shared preferences
			String host = sharedPrefs.getString(
					SettingsActivity.KEY_PREF_HOSTNAME, null);
			String user = sharedPrefs.getString(
					SettingsActivity.KEY_PREF_USERNAME, null);
			String pass = sharedPrefs.getString(
					SettingsActivity.KEY_PREF_PASSWORD, null);
			if (host == null) {
				Toast.makeText(this, R.string.no_hostname_specified, Toast.LENGTH_LONG);
			} else if (user == null) {
				Toast.makeText(this, R.string.no_username_specified, Toast.LENGTH_LONG);
			} else if (pass == null) {
				Toast.makeText(this, R.string.no_password_specified, Toast.LENGTH_LONG);
			} else {
				// execute query
				new SSHQueryTask().execute(host, user, pass);
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
					(String) params[2]);
			QueryBean bean = new QueryBean();
			try {
				raspiQuery.connect();
				String cpuTemp = raspiQuery.queryCpuTemp();
				String cpuFreq = raspiQuery.queryCpuFreq();
				raspiQuery.disconnect();
				bean.setCpuTemperature(cpuTemp);
				bean.setCpuFrequency(cpuFreq);
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
			default:
				break;
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

	}

}
