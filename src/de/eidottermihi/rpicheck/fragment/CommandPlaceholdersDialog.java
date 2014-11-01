package de.eidottermihi.rpicheck.fragment;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import de.eidottermihi.rpicheck.R;

public class CommandPlaceholdersDialog extends DialogFragment {

	ArrayList<String> placeholders;
	String command;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(
				getActivity());
		this.placeholders = this.getArguments().getStringArrayList(
				"placeholders");
		this.command = this.getArguments().getString("cmd");

		builder.setTitle(getString(R.string.run_cmd_dialog_title, this.command));

		// fetching the theme-dependent icon
		TypedValue icon = new TypedValue();
		if (getActivity().getTheme().resolveAttribute(R.attr.ic_dialog_run,
				icon, true)) {
			builder.setIcon(icon.resourceId);
		}

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// just closing the dialog
			}
		});
		final LayoutInflater inflater = getActivity().getLayoutInflater();
		final View view = inflater.inflate(R.layout.dialog_command_run, null);
		builder.setView(view);
		return builder.create();
	}

}
