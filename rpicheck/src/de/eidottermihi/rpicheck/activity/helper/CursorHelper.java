package de.eidottermihi.rpicheck.activity.helper;

import android.database.Cursor;
import de.eidottermihi.rpicheck.db.CommandBean;

public class CursorHelper {

	public static CommandBean readCommand(Cursor c) {
		CommandBean command = new CommandBean();
		command.setId(c.getLong(0));
		command.setName(c.getString(1));
		command.setCommand(c.getString(2));
		command.setShowOutput(c.getInt(3) == 1 ? true : false);
		return command;
	}

}
