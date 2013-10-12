package de.eidottermihi.rpicheck.beans;

import java.io.Serializable;

public class MemoryBean implements Serializable {
	private static final long serialVersionUID = -6431484424293280487L;

	private long bytes;

	private MemoryBean(long bytes) {
		this.setBytes(bytes);
	}

	public static MemoryBean from(Memory scale, long data) {
		final MemoryBean memoryBean = new MemoryBean(0);
		if (scale == Memory.B) {
			memoryBean.setBytes(data * Memory.B.getScale());
		} else if (scale == Memory.KB) {
			memoryBean.setBytes(data * Memory.KB.getScale());
		} else if (scale == Memory.MB) {
			memoryBean.setBytes(data * Memory.MB.getScale());
		} else if (scale == Memory.GB) {
			memoryBean.setBytes(data * Memory.GB.getScale());
		} else if (scale == Memory.TB) {
			memoryBean.setBytes(data * Memory.GB.getScale());
		}
		return memoryBean;
	}

	public String humanReadableByteCount(boolean si) {
		int unit = si ? 1000 : 1024;
		if (getBytes() < unit)
			return getBytes() + " B";
		int exp = (int) (Math.log(getBytes()) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1)
				+ (si ? "" : "i");
		return String.format("%.1f %sB", getBytes() / Math.pow(unit, exp), pre);
	}

	public long getBytes() {
		return bytes;
	}

	public void setBytes(long bytes) {
		this.bytes = bytes;
	}

}
