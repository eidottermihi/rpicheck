package de.eidottermihi.rpicheck.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.eidottermihi.rpicheck.R;
import de.eidottermihi.rpicheck.db.CommandBean;

public class CommandPlaceholdersDialog extends DialogFragment {

	public static final String ARG_PLACEHOLDERS = "placeholders";
	public static final String ARG_COMMAND = "cmd";
	public static final String ARG_PASSPHRASE = "passphrase";

	ArrayList<String> placeholders;
	CommandBean command;
	String keyPass;

	PlaceholdersDialogListener activityListener;

	public interface PlaceholdersDialogListener {
		/**
		 * Gets called when the user entered the replacements for the
		 * placeholders.
		 * 
		 * @param dialog
		 *            the Dialog
		 * @param command
		 *            the command with replaced values
		 */
		public void onPlaceholdersOKClick(DialogFragment dialog,
				CommandBean command, String keyPassphrase);

		public void onPlaceholdersCancelClick();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			activityListener = (PlaceholdersDialogListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement PlaceholdersDialogListener.");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(
				getActivity());
		this.placeholders = this.getArguments().getStringArrayList(
				ARG_PLACEHOLDERS);
		this.command = (CommandBean) this.getArguments().getSerializable(
				ARG_COMMAND);
		this.keyPass = this.getArguments().getString(ARG_PASSPHRASE);

		builder.setTitle("Placeholders");

		// fetching the theme-dependent icon
		TypedValue icon = new TypedValue();
		if (getActivity().getTheme().resolveAttribute(R.attr.ic_dialog_run,
				icon, true)) {
			builder.setIcon(icon.resourceId);
		}

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				activityListener.onPlaceholdersOKClick(
						CommandPlaceholdersDialog.this, command, keyPass);
			}
		});
		final LayoutInflater inflater = getActivity().getLayoutInflater();
		final View view = inflater.inflate(R.layout.dialog_placeholders, null);
		LinearLayout linLayout = (LinearLayout) view
				.findViewById(R.id.placeholderLayout);
		for (String string : placeholders) {
			TextView v = new TextView(getActivity());
			v.setText(string);
			linLayout.addView(v);
		}
		builder.setView(view);
		return builder.create();
	}

}
