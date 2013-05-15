package de.eidottermihi.rpicheck;

public enum QueryStatus {
	OK("Everything ok."), ConnectionFailure("Connection failed."), AuthenticationFailure(
			"Authentication failed."), VCGENCMD("vcgencmd was not found.");

	private String text;

	private QueryStatus(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
