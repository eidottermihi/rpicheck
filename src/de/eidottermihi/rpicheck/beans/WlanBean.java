/**
 * 
 */
package de.eidottermihi.rpicheck.beans;

import java.io.Serializable;

/**
 * Bean capsuling data of a wlan connection (link quality, ...).
 * 
 * @author Michael
 * 
 */
public class WlanBean implements Serializable {
	private static final long serialVersionUID = 4533512302697302122L;

	private Integer linkQuality;
	private Integer signalLevel;

	public Integer getLinkQuality() {
		return linkQuality;
	}

	public void setLinkQuality(Integer linkQuality) {
		this.linkQuality = linkQuality;
	}

	public Integer getSignalLevel() {
		return signalLevel;
	}

	public void setSignalLevel(Integer signalLevel) {
		this.signalLevel = signalLevel;
	}

}
