package de.eidottermihi.rpicheck.beans;

public enum Memory {
	B("Byte", "B", 1), KB("KiloByte", "KB", 1000), MB("MegaByte", "MB",
			1000 * 1000), GB("GigaByte", "GB", 1000 * 1000 * 1000), TB(
			"TeraByte", "TB", 1000 * 1000 * 1000 * 1000);

	private String longName;
	private String shortName;
	private long scale;

	private Memory(String name, String shortName, long scale) {
		this.longName = name;
		this.shortName = shortName;
		this.scale = scale;
	}

	public String getLongName() {
		return longName;
	}

	public String getShortName() {
		return shortName;
	}

	public long getScale() {
		return scale;
	}

}
