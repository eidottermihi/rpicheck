package de.eidottermihi.rpicheck.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import de.eidottermihi.raspicheck.R;

/**
 * A dialog which shows a error message.
 * 
 * @author Michael
 * 
 */
public class QueryExceptionDialog extends DialogFragment {
	public static final String MESSAGE_KEY = "message";

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.exception_dialog_title);
		builder.setMessage(getArguments().getString(MESSAGE_KEY));
		builder.setPositiveButton("Ok", null);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		return builder.create();
	}

}
