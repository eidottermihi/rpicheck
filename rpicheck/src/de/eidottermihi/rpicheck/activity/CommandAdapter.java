package de.eidottermihi.rpicheck.activity;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.eidottermihi.rpicheck.R;
import de.eidottermihi.rpicheck.activity.helper.CursorHelper;
import de.eidottermihi.rpicheck.db.CommandBean;

/**
 * Cursor adapter for Commands.
 */
public class CommandAdapter extends CursorAdapter {

	private LayoutInflater inflater;

	public CommandAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView name = (TextView) view.findViewById(R.id.commandRowName);
		TextView command = (TextView) view.findViewById(R.id.commandRowCommand);
		CommandBean bean = CursorHelper.readCommand(cursor);
		name.setText(bean.getName());
		command.setText(bean.getCommand());
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = inflater.inflate(R.layout.command_row, parent, false);
		return v;
	}

}
