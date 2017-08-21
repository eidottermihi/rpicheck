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
package de.eidottermihi.rpicheck.beans;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import de.eidottermihi.rpicheck.ssh.beans.Exported;
import de.eidottermihi.rpicheck.ssh.beans.DiskUsageBean;
import de.eidottermihi.rpicheck.ssh.beans.MemoryBean;
import de.eidottermihi.rpicheck.ssh.beans.NetworkInterfaceInformation;
import de.eidottermihi.rpicheck.ssh.beans.ProcessBean;
import de.eidottermihi.rpicheck.ssh.beans.VcgencmdBean;
import de.eidottermihi.rpicheck.ssh.impl.RaspiQueryException;

public class QueryBean implements Serializable {
    private static final long serialVersionUID = 4550202922460044379L;

    private VcgencmdBean vcgencmdInfo;
    private List<NetworkInterfaceInformation> networkInfo;
    private Date lastUpdate;
    private String startup;
    private String avgLoad;
    private MemoryBean totalMem;
    private MemoryBean freeMem;
    private String serialNo;
    private List<DiskUsageBean> disks;
    private String distri;
    private List<ProcessBean> processes;
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

    @Override
    public String toString() {
        return "QueryBean [vcgencmdInfo=" + vcgencmdInfo + ", networkInfo="
                + networkInfo + ", lastUpdate=" + lastUpdate + ", startup="
                + startup + ", avgLoad=" + avgLoad + ", totalMem=" + totalMem
                + ", freeMem=" + freeMem + ", serialNo=" + serialNo
                + ", disks=" + disks + ", distri=" + distri + ", processes="
                + processes + ", exception=" + exception + ", errorMessages="
                + errorMessages + "]";
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

    @Exported("totalMemory")
    public MemoryBean getTotalMem() {
        return totalMem;
    }

    public void setTotalMem(MemoryBean totalMem) {
        this.totalMem = totalMem;
    }

    @Exported("freeMemory")
    public MemoryBean getFreeMem() {
        return freeMem;
    }

    public void setFreeMem(MemoryBean freeMem) {
        this.freeMem = freeMem;
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

}
