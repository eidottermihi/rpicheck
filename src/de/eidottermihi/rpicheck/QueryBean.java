package de.eidottermihi.rpicheck;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import de.eidottermihi.raspitools.beans.DiskUsageBean;
import de.eidottermihi.raspitools.beans.MemoryBean;
import de.eidottermihi.raspitools.beans.NetworkInterfaceInformation;
import de.eidottermihi.raspitools.beans.ProcessBean;
import de.eidottermihi.raspitools.beans.VcgencmdBean;

public class QueryBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private VcgencmdBean vcgencmdInfo;
	private List<NetworkInterfaceInformation> networkInfo;
	private Date lastUpdate;
	private QueryStatus status;
	private String startup;
	private String avgLoad;
	private MemoryBean totalMem;
	private MemoryBean freeMem;
	private String serialNo;
	private List<DiskUsageBean> disks;
	private String distri;
	private List<ProcessBean> processes;

	public VcgencmdBean getVcgencmdInfo() {
		return vcgencmdInfo;
	}

	public void setVcgencmdInfo(VcgencmdBean vcgencmdInfo) {
		this.vcgencmdInfo = vcgencmdInfo;
	}

	public List<NetworkInterfaceInformation> getNetworkInfo() {
		return networkInfo;
	}

	public void setNetworkInfo(List<NetworkInterfaceInformation> networkInfo) {
		this.networkInfo = networkInfo;
	}

	public QueryStatus getStatus() {
		return status;
	}

	public void setStatus(QueryStatus status) {
		this.status = status;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String getStartup() {
		return startup;
	}

	public void setStartup(String startup) {
		this.startup = startup;
	}

	public String getAvgLoad() {
		return avgLoad;
	}

	public void setAvgLoad(String avgLoad) {
		this.avgLoad = avgLoad;
	}

	public MemoryBean getTotalMem() {
		return totalMem;
	}

	public void setTotalMem(MemoryBean totalMem) {
		this.totalMem = totalMem;
	}

	public MemoryBean getFreeMem() {
		return freeMem;
	}

	public void setFreeMem(MemoryBean freeMem) {
		this.freeMem = freeMem;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public List<DiskUsageBean> getDisks() {
		return disks;
	}

	public void setDisks(List<DiskUsageBean> disks) {
		this.disks = disks;
	}

	public String getDistri() {
		return distri;
	}

	public void setDistri(String distri) {
		this.distri = distri;
	}

	public List<ProcessBean> getProcesses() {
		return processes;
	}

	public void setProcesses(List<ProcessBean> processes) {
		this.processes = processes;
	}

}
