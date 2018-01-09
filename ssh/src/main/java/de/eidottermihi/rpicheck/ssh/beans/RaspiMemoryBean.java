/**
 * MIT License
 *
 * Copyright (c) 2018  RasPi Check Contributors
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
