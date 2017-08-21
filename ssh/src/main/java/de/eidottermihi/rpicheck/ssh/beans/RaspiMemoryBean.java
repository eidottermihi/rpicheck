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
    private float percentageUsed;
    private String errorMessage;

    public RaspiMemoryBean(long totalMemory, long totalUsed) {
        this.totalMemory = MemoryBean.from(Memory.KB, totalMemory);
        this.totalUsed = MemoryBean.from(Memory.KB, totalUsed);
        this.totalFree = MemoryBean.from(Memory.KB, totalMemory - totalUsed);
        this.percentageUsed = ((float) totalUsed) / ((float) totalMemory);
    }

    public RaspiMemoryBean(String string) {
        this.errorMessage = string;
    }

    public MemoryBean getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(MemoryBean totalMemory) {
        this.totalMemory = totalMemory;
    }

    public MemoryBean getTotalUsed() {
        return totalUsed;
    }

    public void setTotalUsed(MemoryBean totalUsed) {
        this.totalUsed = totalUsed;
    }

    public MemoryBean getTotalFree() {
        return totalFree;
    }

    public void setTotalFree(MemoryBean totalFree) {
        this.totalFree = totalFree;
    }

    public float getPercentageUsed() {
        return percentageUsed;
    }

    public void setPercentageUsed(float percentageUsed) {
        this.percentageUsed = percentageUsed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
