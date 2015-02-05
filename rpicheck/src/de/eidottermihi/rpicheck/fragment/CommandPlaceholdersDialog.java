package de.eidottermihi.rpicheck.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Strings;

import de.eidottermihi.rpicheck.R;
import de.eidottermihi.rpicheck.db.CommandBean;

public class CommandPlaceholdersDialog extends DialogFragment {

	public static final String ARG_PLACEHOLDERS = "placeholders";
	public static final String ARG_COMMAND = "cmd";
	public static final String ARG_PASSPHRASE = "passphrase";

	private ArrayList<String> placeholders;
	private CommandBean command;
	private String keyPass;
	private Map<String, EditText> placeholderInputs;

	private PlaceholdersDialogListener activityListener;

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

		builder.setTitle(R.string.title_placeholders);

		// fetching the theme-dependent icon
		TypedValue icon = new TypedValue();
		if (getActivity().getTheme().resolveAttribute(R.attr.ic_dialog_run,
				icon, true)) {
			builder.setIcon(icon.resourceId);
		}

		builder.setPositiveButton(R.string.placeholders_button_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		final LayoutInflater inflater = getActivity().getLayoutInflater();
		final View view = inflater.inflate(R.layout.dialog_placeholders, null);
		LinearLayout linLayout = (LinearLayout) view
				.findViewById(R.id.placeholderLayout);
		placeholderInputs = new HashMap<String, EditText>();
		for (String string : placeholders) {
			final View row = inflater.inflate(R.layout.command_placeholder_row,
					null);
			final TextView placeholderName = (TextView) row
					.findViewById(R.id.placeholderName);
			placeholderName.setText(string);
			final EditText placeholderText = (EditText) row
					.findViewById(R.id.placeholderText);
			linLayout.addView(row);
			placeholderInputs.put(string, placeholderText);
		}
		builder.setView(view);
		return builder.create();
	}

	@Override
	public void onStart() {
		super.onStart(); // super.onStart() is where dialog.show() is actually
							// called on the underlying dialog, so we have to do
							// it after this point
		AlertDialog d = (AlertDialog) getDialog();
		if (d != null) {
			Button positiveButton = (Button) d
					.getButton(Dialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// do replacements
					String cmd = command.getCommand();
					for (Entry<String, EditText> entry : placeholderInputs
							.entrySet()) {
						String placeholder = entry.getKey();
						String replacement = entry.getValue().getText()
								.toString();
						if (!Strings.isNullOrEmpty(replacement)) {
							cmd = cmd.replace(placeholder, replacement);
						}
					}
					command.setCommand(cmd);
					dismiss();
					activityListener.onPlaceholdersOKClick(
							CommandPlaceholdersDialog.this, command, keyPass);
				}
			});
		}
	}

}
