/**
 * 
 */
package de.eidottermihi.rpicheck.beans;

import java.io.Serializable;

/**
 * Bean capsuling information of a network interface.
 * 
 * @author Michael
 * 
 */
public class NetworkInterfaceInformation implements Serializable {

	private static final long serialVersionUID = 702088882506060933L;

	/**
	 * The name of the interface (eth0, wlan0, ...).
	 */
	private String name;

	/**
	 * If an interface has no carrier, it is not up and running (no ip adress
	 * and so on..)
	 */
	private boolean hasCarrier;

	/**
	 * Optional: the ip adress.
	 */
	private String ipAdress;

	/**
	 * Optional: Link and signal quality if a wifi interface.
	 */
	private WlanBean wlanInfo;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isHasCarrier() {
		return hasCarrier;
	}

	public void setHasCarrier(boolean hasCarrier) {
		this.hasCarrier = hasCarrier;
	}

	public String getIpAdress() {
		return ipAdress;
	}

	public void setIpAdress(String ipAdress) {
		this.ipAdress = ipAdress;
	}

	public WlanBean getWlanInfo() {
		return wlanInfo;
	}

	public void setWlanInfo(WlanBean wlanInfo) {
		this.wlanInfo = wlanInfo;
	}

}
