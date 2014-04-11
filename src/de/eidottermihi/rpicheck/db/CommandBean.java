package de.eidottermihi.rpicheck.db;

import java.io.Serializable;

/**
 * @author Michael
 * 
 */
public class CommandBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	private String name;
	private String command;
	private boolean showOutput;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public boolean isShowOutput() {
		return showOutput;
	}

	public void setShowOutput(boolean showOutput) {
		this.showOutput = showOutput;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

}
