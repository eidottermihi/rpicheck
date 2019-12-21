/**
 * MIT License
 *
 * Copyright (c) 2019  RasPi Check Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
