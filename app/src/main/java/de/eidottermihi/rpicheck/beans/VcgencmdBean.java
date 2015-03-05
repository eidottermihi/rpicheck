/**
 * 
 */
package de.eidottermihi.rpicheck.beans;

import java.io.Serializable;

/**
 * Bean capsuling information that is obtained via vcgencmd.
 * 
 * @author Michael
 * 
 */
public class VcgencmdBean implements Serializable {
	private static final long serialVersionUID = 8436937766205200037L;

	/**
	 * CPU temperature in Celsius.
	 */
	private double cpuTemperature;

	/**
	 * CORE frequency in Hz.
	 */
	private long coreFrequency;

	/**
	 * ARM frequency in Hz.
	 */
	private long armFrequency;

	/**
	 * Volts of CORE.
	 */
	private double coreVolts;

	/**
	 * Version of vcgencmd.
	 */
	private String version;

	public double getCpuTemperature() {
		return cpuTemperature;
	}

	public void setCpuTemperature(double cpuTemperature) {
		this.cpuTemperature = cpuTemperature;
	}

	public long getCoreFrequency() {
		return coreFrequency;
	}

	public void setCoreFrequency(long coreFrequency) {
		this.coreFrequency = coreFrequency;
	}

	public long getArmFrequency() {
		return armFrequency;
	}

	public void setArmFrequency(long armFrequency) {
		this.armFrequency = armFrequency;
	}

	public double getCoreVolts() {
		return coreVolts;
	}

	public void setCoreVolts(double coreVolts) {
		this.coreVolts = coreVolts;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
