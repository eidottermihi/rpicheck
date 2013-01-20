package de.eidottermihi.rpicheck;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.eidottermihi.raspitools.RaspiQuery;
import de.eidottermihi.raspitools.RaspiQueryException;

public class MainActivity extends Activity implements TextWatcher {
	private static final String LOG_TAG = "MAIN";

	private RaspiQuery raspiQuery;
	private TextView hostnameTextView;
	private TextView usernameTextView;
	private TextView passwordTextView;
	private TextView cpuFreqTextView;
	private TextView cpuTempTextView;
	private ProgressBar progressBar;
	private Button checkButton;

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
		hostnameTextView = (TextView) findViewById(R.id.hostnameText);
		usernameTextView = (TextView) findViewById(R.id.usernameText);
		passwordTextView = (TextView) findViewById(R.id.passwordText);
		cpuFreqTextView = (TextView) findViewById(R.id.cpuFreqText);
		cpuTempTextView = (TextView) findViewById(R.id.cpuTempText);
		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		checkButton = (Button) findViewById(R.id.button1);

		// assigning TextWatcher to text fields
		hostnameTextView.addTextChangedListener(this);
		usernameTextView.addTextChangedListener(this);
		passwordTextView.addTextChangedListener(this);
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

	@SuppressWarnings("unchecked")
	private void doQuery() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			// show progress bar
			progressBar.setVisibility(ProgressBar.VISIBLE);
			new SSHQueryTask().execute(hostnameTextView.getText().toString(),
					usernameTextView.getText().toString(), passwordTextView
							.getText().toString());
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

	public void afterTextChanged(Editable s) {
		// activate Check button if all credentials are not blank
		if (StringUtils.isNotBlank(hostnameTextView.getText())
				&& StringUtils.isNotBlank(usernameTextView.getText())
				&& StringUtils.isNotBlank(passwordTextView.getText())) {
			checkButton.setEnabled(true);
		} else {
			checkButton.setEnabled(false);
		}
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

}
