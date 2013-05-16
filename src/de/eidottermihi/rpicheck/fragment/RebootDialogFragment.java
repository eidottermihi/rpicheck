package de.eidottermihi.rpicheck.fragment;

import de.eidottermihi.rpicheck.R;
import de.eidottermihi.rpicheck.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class RebootDialogFragment extends DialogFragment {

	public interface RebootDialogListener {
		public void onDialogPositiveClick(DialogFragment dialog);

		public void onDialogNegativeClick(DialogFragment dialog);
	}

	private RebootDialogListener mRebootDialogListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mRebootDialogListener = (RebootDialogListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement RebootDialogListener.");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.question_reboot);
		builder.setNegativeButton(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mRebootDialogListener
						.onDialogNegativeClick(RebootDialogFragment.this);
			}
		});
		builder.setPositiveButton(R.string.reboot, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mRebootDialogListener
						.onDialogPositiveClick(RebootDialogFragment.this);
			}
		});
		return builder.create();
	}

}
