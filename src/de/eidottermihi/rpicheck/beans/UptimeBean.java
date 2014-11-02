package de.eidottermihi.rpicheck.beans;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.ocpsoft.prettytime.PrettyTime;

public class UptimeBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final DecimalFormat df = new DecimalFormat("#.### %");
	private static final DecimalFormatSymbols symbols = new DecimalFormatSymbols(
			Locale.ENGLISH);
	private long secondsRunning;
	private String averageLoad;
	private String errorMessage;

	public UptimeBean(String errorMsg){
		this.errorMessage = errorMsg;
		this.secondsRunning = 0;
		this.averageLoad = "n/a";
	}
	
	public UptimeBean(double uptimeFull, double uptimeIdle) {
		df.setDecimalFormatSymbols(symbols);
		// calculating load
		double averageLoad = (1 - (uptimeIdle / uptimeFull));
		// precision = 4
		this.averageLoad = df.format(averageLoad);
		this.secondsRunning = (long) uptimeFull;
	}

	public long getSecondsRunning() {
		return secondsRunning;
	}

	public void setSecondsRunning(long secondsRunning) {
		this.secondsRunning = secondsRunning;
	}

	public String getAverageLoad() {
		return averageLoad;
	}

	public void setAverageLoad(String averageLoad) {
		this.averageLoad = averageLoad;
	}

	public String getRunningPretty() {
		// current time - (secondsRunning * 1000) => start time
		PrettyTime pretty = new PrettyTime();
		return pretty.format(new Date(Calendar.getInstance().getTimeInMillis()
				- (secondsRunning * 1000)));
	}

	/**
	 * If bean contains Error-Message, no data should be retrieved from it.
	 * 
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
