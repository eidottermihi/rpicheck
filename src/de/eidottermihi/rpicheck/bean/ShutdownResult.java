package de.eidottermihi.rpicheck.bean;

/**
 * Capsules the result of a Reboot/Halt-ASyncTask.
 * 
 * @author Michael
 * 
 */
public class ShutdownResult {
	private String type;
	private Throwable excpetion;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Throwable getExcpetion() {
		return excpetion;
	}

	public void setExcpetion(Throwable excpetion) {
		this.excpetion = excpetion;
	}

}
