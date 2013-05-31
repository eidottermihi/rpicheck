package de.eidottermihi.rpicheck.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import de.eidottermihi.raspitools.RaspiQueryException;
import de.eidottermihi.raspitools.beans.DiskUsageBean;
import de.eidottermihi.raspitools.beans.MemoryBean;
import de.eidottermihi.raspitools.beans.NetworkInterfaceInformation;
import de.eidottermihi.raspitools.beans.ProcessBean;
import de.eidottermihi.raspitools.beans.VcgencmdBean;

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
