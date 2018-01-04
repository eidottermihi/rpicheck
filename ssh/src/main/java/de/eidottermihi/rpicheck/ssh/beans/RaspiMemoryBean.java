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

public class RaspiMemoryBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private MemoryBean totalMemory;
    private MemoryBean totalUsed;
    private MemoryBean totalFree;
    private float memoryPercentageUsed;
    private MemoryBean swapMemory;
    private MemoryBean swapUsed;
    private MemoryBean swapFree;
    private float swapPercentageUsed;
    private String errorMessage;

    public RaspiMemoryBean(long memoryTotal, long memoryUsed, long swapTotal, long swapUsed) {
        this.totalMemory = MemoryBean.from(Memory.KB, memoryTotal);
        this.totalUsed = MemoryBean.from(Memory.KB, memoryUsed);
        this.totalFree = MemoryBean.from(Memory.KB, memoryTotal - memoryUsed);
        this.memoryPercentageUsed = ((float) memoryUsed) / ((float) memoryTotal);
        this.swapMemory = MemoryBean.from(Memory.KB, swapTotal);
        this.swapUsed = MemoryBean.from(Memory.KB, swapUsed);
        this.swapFree = MemoryBean.from(Memory.KB, swapTotal - swapUsed);
        this.swapPercentageUsed = ((float) swapUsed / (float) swapTotal);
    }

    public RaspiMemoryBean(String string) {
        this.errorMessage = string;
    }

    @Exported("totalMemory")
    public MemoryBean getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(MemoryBean totalMemory) {
        this.totalMemory = totalMemory;
    }

    @Exported("usedMemory")
    public MemoryBean getTotalUsed() {
        return totalUsed;
    }

    public void setTotalUsed(MemoryBean totalUsed) {
        this.totalUsed = totalUsed;
    }

    @Exported("freeMemory")
    public MemoryBean getTotalFree() {
        return totalFree;
    }

    public void setTotalFree(MemoryBean totalFree) {
        this.totalFree = totalFree;
    }

    @Exported("memoryPercentageUsed")
    public float getMemoryPercentageUsed() {
        return memoryPercentageUsed;
    }

    public void setMemoryPercentageUsed(float memoryPercentageUsed) {
        this.memoryPercentageUsed = memoryPercentageUsed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Exported("totalSwap")
    public MemoryBean getSwapMemory() {
        return swapMemory;
    }

    public void setSwapMemory(MemoryBean swapMemory) {
        this.swapMemory = swapMemory;
    }

    @Exported("usedSwap")
    public MemoryBean getSwapUsed() {
        return swapUsed;
    }

    public void setSwapUsed(MemoryBean swapUsed) {
        this.swapUsed = swapUsed;
    }

    @Exported("freeSwap")
    public MemoryBean getSwapFree() {
        return swapFree;
    }

    public void setSwapFree(MemoryBean swapFree) {
        this.swapFree = swapFree;
    }

    @Exported("swapPercentageUsed")
    public float getSwapPercentageUsed() {
        return swapPercentageUsed;
    }

    public void setSwapPercentageUsed(float swapPercentageUsed) {
        this.swapPercentageUsed = swapPercentageUsed;
    }
}
