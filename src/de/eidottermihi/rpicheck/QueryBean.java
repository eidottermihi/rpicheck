package de.eidottermihi.rpicheck;

public class QueryBean {
	private String cpuTemperature;
	private String cpuFrequency;
	private QueryStatus status;

	public String getCpuTemperature() {
		return cpuTemperature;
	}

	public void setCpuTemperature(String cpuTemperature) {
		this.cpuTemperature = cpuTemperature;
	}

	public String getCpuFrequency() {
		return cpuFrequency;
	}

	public void setCpuFrequency(String cpuFrequency) {
		this.cpuFrequency = cpuFrequency;
	}	
	
	public QueryStatus getStatus() {
		return status;
	}

	public void setStatus(QueryStatus status) {
		this.status = status;
	}	

}
