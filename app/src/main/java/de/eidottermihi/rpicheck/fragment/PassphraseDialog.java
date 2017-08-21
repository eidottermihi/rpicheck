/**
 * Copyright (C) 2017  RasPi Check Contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package de.eidottermihi.rpicheck.fragment;

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
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.common.base.Strings;

import de.eidottermihi.raspicheck.R;

public class PassphraseDialog extends DialogFragment {

    public interface PassphraseDialogListener {
        /**
         * Gets called when the user entered his passphrase for the private key
         * authentification.
         *
         * @param dialog         the Dialog
         * @param passphrase     the passphrase entered
         * @param savePassphrase if the passphrase should be saved
         */
        public void onPassphraseOKClick(DialogFragment dialog,
                                        String passphrase, boolean savePassphrase, String type);

        public void onPassphraseCancelClick();
    }

    public static final String SSH_QUERY = "query";
    public static final String SSH_SHUTDOWN = "shutdown";
    public static final String SSH_HALT = "halt";
    public static final String KEY_TYPE = "type";

    private PassphraseDialogListener mPassphraseDialogListener;

    private String type;

    private EditText editTextPassphrase;
    private CheckBox checkSavePassphrase;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mPassphraseDialogListener = (PassphraseDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PassphraseDialogListener.");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity());
        this.type = this.getArguments().getString(KEY_TYPE);
        builder.setTitle(R.string.dialog_passphrase_needed);

        // fetching the theme-dependent icon
        TypedValue icon = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(
                R.attr.ic_dialog_passphrase, icon, true)) {
            builder.setIcon(icon.resourceId);
        }

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_passphrase, null);
        builder.setView(view);
        editTextPassphrase = (EditText) view
                .findViewById(R.id.editTextPassphrase);
        checkSavePassphrase = (CheckBox) view
                .findViewById(R.id.checkBoxSavePassphrase);
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing here
                    }
                });
        builder.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPassphraseDialogListener.onPassphraseCancelClick();
                    }
                });
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
                    // validate passphrase not empty
                    if (!Strings.isNullOrEmpty(editTextPassphrase.getText()
                            .toString())) {
                        final String passphrase = editTextPassphrase.getText()
                                .toString().trim();
                        final boolean savePassphrase = checkSavePassphrase
                                .isChecked();
                        dismiss();
                        mPassphraseDialogListener.onPassphraseOKClick(
                                PassphraseDialog.this, passphrase,
                                savePassphrase, type);
                    } else {
                        editTextPassphrase
                                .setError(getString(R.string.validation_msg_key_passphrase));
                    }
                }
            });
        }
    }
}
