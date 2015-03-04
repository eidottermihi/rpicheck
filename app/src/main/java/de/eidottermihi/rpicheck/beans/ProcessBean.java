package de.eidottermihi.rpicheck.beans;

import java.io.Serializable;

/**
 * Bean for storing process information.
 * 
 * @author Michael
 * 
 */
public class ProcessBean implements Serializable {
	private static final long serialVersionUID = 3421726611868018755L;
	private int pId;
	private String tty;
	private String cpuTime;
	private String command;

	public ProcessBean(int pId, String tty, String cpuTime, String command) {
		this.pId = pId;
		this.tty = tty;
		this.cpuTime = cpuTime;
		this.command = command;
	}

	public int getpId() {
		return pId;
	}

	public void setpId(int pId) {
		this.pId = pId;
	}

	public String getTty() {
		return tty;
	}

	public void setTty(String tty) {
		this.tty = tty;
	}

	public String getCpuTime() {
		return cpuTime;
	}

	public void setCpuTime(String cpuTime) {
		this.cpuTime = cpuTime;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	@Override
	public String toString() {
		return "ProcessBean [pId=" + pId + ", tty=" + tty + ", cpuTime="
				+ cpuTime + ", command=" + command + "]";
	}

}
