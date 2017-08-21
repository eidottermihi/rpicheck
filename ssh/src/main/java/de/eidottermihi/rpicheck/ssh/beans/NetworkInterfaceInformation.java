/**
 * Copyright (C) 2017  RasPi Check Contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
/**
 *
 */
package de.eidottermihi.rpicheck.ssh.beans;

import java.io.Serializable;

/**
 * Bean capsuling information of a network interface.
 *
 * @author Michael
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

    @Exported
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

    @Exported
    public String getIpAdress() {
        return ipAdress;
    }

    public void setIpAdress(String ipAdress) {
        this.ipAdress = ipAdress;
    }

    @Exported("wlan")
    public WlanBean getWlanInfo() {
        return wlanInfo;
    }

    public void setWlanInfo(WlanBean wlanInfo) {
        this.wlanInfo = wlanInfo;
    }

}
