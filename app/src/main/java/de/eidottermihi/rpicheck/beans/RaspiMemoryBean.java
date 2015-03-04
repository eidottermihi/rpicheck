package de.eidottermihi.rpicheck.beans;

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
		this.percentageUsed = totalUsed / totalMemory;
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
