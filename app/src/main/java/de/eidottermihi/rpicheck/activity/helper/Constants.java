package de.eidottermihi.rpicheck.activity.helper;

public final class Constants {
	/**
	 * Directory in which app data will be stored.
	 */
	public static final String SD_LOCATION = "/data/de.eidottermihi.rpicheck/";

	/**
	 * ID for DeviceBean in Action Extras Bundle.
	 */
	public static final String EXTRA_DEVICE_ID = "device_id";

	/**
	 * Constant indicating ShutdownTask should do a reboot.
	 */
	public static final String TYPE_REBOOT = "reboot";

	/**
	 * Constant indicating ShutdownTask should do a halt.
	 */
	public static final String TYPE_HALT = "halt";
}
