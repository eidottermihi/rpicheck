package de.eidottermihi.rpicheck.beans;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import org.ocpsoft.prettytime.PrettyTime;

public class UptimeBean implements Serializable {
	private static final long serialVersionUID = 2L;

	private long secondsRunning;

	public UptimeBean(double uptimeFull) {
		this.secondsRunning = (long) uptimeFull;
	}

	public long getSecondsRunning() {
		return secondsRunning;
	}

	public void setSecondsRunning(long secondsRunning) {
		this.secondsRunning = secondsRunning;
	}

	public String getRunningPretty() {
		// current time - (secondsRunning * 1000) => start time
		PrettyTime pretty = new PrettyTime();
		return pretty.format(new Date(Calendar.getInstance().getTimeInMillis()
				- (secondsRunning * 1000)));
	}

}
