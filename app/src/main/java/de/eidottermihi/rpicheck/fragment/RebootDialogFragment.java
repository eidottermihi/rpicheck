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

import de.eidottermihi.raspicheck.R;

public class RebootDialogFragment extends DialogFragment {

    public interface ShutdownDialogListener {
        public void onHaltClick(DialogInterface dialog);

        public void onRebootClick(DialogInterface dialog);
    }

    private ShutdownDialogListener mShutdownDialogListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mShutdownDialogListener = (ShutdownDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ShutdownDialogListener.");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.shutdown_type_title);

        //fetching the theme-dependent icon
        TypedValue icon = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(
                R.attr.ic_dialog_shutdown, icon, true)) {
            builder.setIcon(icon.resourceId);
        }

        builder.setItems(R.array.shutdown_options,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // reboot
                                mShutdownDialogListener.onRebootClick(dialog);
                                break;
                            case 1:
                                // halt
                                mShutdownDialogListener.onHaltClick(dialog);
                            default:
                                break;
                        }

                    }
                });
        return builder.create();
    }
}
