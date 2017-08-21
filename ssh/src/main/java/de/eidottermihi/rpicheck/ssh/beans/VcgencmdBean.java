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
package de.eidottermihi.rpicheck.ssh.beans;

import java.io.Serializable;

/**
 * Bean capsuling information that is obtained via vcgencmd.
 *
 * @author Michael
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

    @Exported
    public double getCpuTemperature() {
        return cpuTemperature;
    }

    public void setCpuTemperature(double cpuTemperature) {
        this.cpuTemperature = cpuTemperature;
    }

    @Exported
    public long getCoreFrequency() {
        return coreFrequency;
    }

    public void setCoreFrequency(long coreFrequency) {
        this.coreFrequency = coreFrequency;
    }

    @Exported
    public long getArmFrequency() {
        return armFrequency;
    }

    public void setArmFrequency(long armFrequency) {
        this.armFrequency = armFrequency;
    }

    @Exported
    public double getCoreVolts() {
        return coreVolts;
    }

    public void setCoreVolts(double coreVolts) {
        this.coreVolts = coreVolts;
    }

    @Exported
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
