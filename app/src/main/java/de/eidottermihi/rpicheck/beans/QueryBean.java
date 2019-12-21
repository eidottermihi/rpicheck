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
package de.eidottermihi.rpicheck.beans;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import de.eidottermihi.rpicheck.ssh.beans.Exported;
import de.eidottermihi.rpicheck.ssh.beans.DiskUsageBean;
import de.eidottermihi.rpicheck.ssh.beans.MemoryBean;
import de.eidottermihi.rpicheck.ssh.beans.NetworkInterfaceInformation;
import de.eidottermihi.rpicheck.ssh.beans.ProcessBean;
import de.eidottermihi.rpicheck.ssh.beans.RaspiMemoryBean;
import de.eidottermihi.rpicheck.ssh.beans.VcgencmdBean;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;

public class QueryBean implements Serializable {
    private static final long serialVersionUID = 4550202922460044379L;

    private VcgencmdBean vcgencmdInfo;
    private List<NetworkInterfaceInformation> networkInfo;
    private Date lastUpdate;
    private String startup;
    private String avgLoad;
    private RaspiMemoryBean memoryBean;
    private String serialNo;
    private List<DiskUsageBean> disks;
    private String distri;
    private List<ProcessBean> processes;
    private String systemtime;
    private RaspiQueryException exception;
    private List<String> errorMessages;

    @Exported("vcgencmd")
    public VcgencmdBean getVcgencmdInfo() {
        return vcgencmdInfo;
    }

    public void setVcgencmdInfo(VcgencmdBean vcgencmdInfo) {
        this.vcgencmdInfo = vcgencmdInfo;
    }

    @Exported("network")
    public List<NetworkInterfaceInformation> getNetworkInfo() {
        return networkInfo;
    }

    public void setNetworkInfo(List<NetworkInterfaceInformation> networkInfo) {
        this.networkInfo = networkInfo;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Exported("startup")
    public String getStartup() {
        return startup;
    }

    public void setStartup(String startup) {
        this.startup = startup;
    }

    @Exported("averageLoad")
    public String getAvgLoad() {
        return avgLoad;
    }

    public void setAvgLoad(String avgLoad) {
        this.avgLoad = avgLoad;
    }

    @Exported("serial")
    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    @Exported("disks")
    public List<DiskUsageBean> getDisks() {
        return disks;
    }

    public void setDisks(List<DiskUsageBean> disks) {
        this.disks = disks;
    }

    @Exported("distribution")
    public String getDistri() {
        return distri;
    }

    public void setDistri(String distri) {
        this.distri = distri;
    }

    @Exported("processes")
    public List<ProcessBean> getProcesses() {
        return processes;
    }

    public void setProcesses(List<ProcessBean> processes) {
        this.processes = processes;
    }

    public RaspiQueryException getException() {
        return exception;
    }

    public void setException(RaspiQueryException exception) {
        this.exception = exception;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    @Exported("systemtime")
    public String getSystemtime() {
        return systemtime;
    }

    public void setSystemtime(String systemtime) {
        this.systemtime = systemtime;
    }

    @Override
    public String toString() {
        return "QueryBean{" +
                "vcgencmdInfo=" + vcgencmdInfo +
                ", networkInfo=" + networkInfo +
                ", lastUpdate=" + lastUpdate +
                ", startup='" + startup + '\'' +
                ", avgLoad='" + avgLoad + '\'' +
                ", serialNo='" + serialNo + '\'' +
                ", disks=" + disks +
                ", distri='" + distri + '\'' +
                ", processes=" + processes +
                ", systemtime='" + systemtime + '\'' +
                ", memory='" + memoryBean + '\'' +
                ", exception=" + exception +
                ", errorMessages=" + errorMessages +
                '}';
    }

    @Exported("memory")
    public RaspiMemoryBean getMemoryBean(){
        return this.memoryBean;
    }

    public void setMemoryBean(RaspiMemoryBean memoryBean) {
        this.memoryBean = memoryBean;
    }
}
