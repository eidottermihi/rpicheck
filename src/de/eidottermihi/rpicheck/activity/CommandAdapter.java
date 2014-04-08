package de.eidottermihi.rpicheck.activity;

import de.eidottermihi.rpicheck.R;
import de.eidottermihi.rpicheck.db.CommandBean;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

// TODO not working
public class CommandAdapter extends BaseAdapter {

	private Cursor commandCursor;
	private Context context;
	private LayoutInflater inflater;
	private OnClickListener buttonOnClickListener;

	private DeviceDbHelper deviceDbHelper;

	public CommandAdapter(Cursor commandCursor, Context context,
			OnClickListener buttonOnClickListener) {
		this.commandCursor = commandCursor;
		this.context = context;
		this.inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.buttonOnClickListener = buttonOnClickListener;
		this.deviceDbHelper = new DeviceDbHelper(this.context);
	}

	@Override
	public int getCount() {
		return commandCursor.getColumnCount();
	}

	@Override
	public Object getItem(int position) {
		boolean moveToPosition = commandCursor.moveToPosition(position);
		if (moveToPosition) {
			CommandBean bean = new CommandBean();
			bean.setId(commandCursor.getLong(0));
			bean.setName(commandCursor.getString(1));
			bean.setCommand(commandCursor.getString(2));
			bean.setShowOutput(commandCursor.getInt(3) == 1 ? true : false);
			return bean;
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		boolean moveToPosition = commandCursor.moveToPosition(position);
		if (moveToPosition) {
			return commandCursor.getLong(0);
		}
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (convertView == null) {
			v = inflater.inflate(R.layout.command_row, null);
		}
		TextView name = (TextView) v.findViewById(R.id.commandRowName);
		long commandId = this.getItemId(position);
		final CommandBean command = deviceDbHelper.readCommand(commandId);
		name.setText(command.getName());
		Button queryButton = (Button) v
				.findViewById(R.id.commandRowQueryButton);
		queryButton.setOnClickListener(this.buttonOnClickListener);
		return v;
	}

}
