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
/**
 *
 */
package de.eidottermihi.rpicheck.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.webkit.WebView;

import java.util.List;

import de.eidottermihi.raspicheck.R;

/**
 * Dialog for showing error messages that occured during the query.
 *
 * @author Michael
 */
public class QueryErrorMessagesDialog extends DialogFragment {

    public static final String KEY_ERROR_MESSAGES = "errorMessages";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        List<String> messages = this.getArguments().getStringArrayList(
                KEY_ERROR_MESSAGES);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.errormessages_dialog_title);
        WebView wv = new WebView(getActivity());
        wv.loadDataWithBaseURL(null, this.createErrorMessages(messages),
                "text/html", "UTF-8", null);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            Compability.setViewLayerTypeSoftware(wv);
        }
        wv.setBackgroundColor(0);
        builder.setView(wv);
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", null);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        return builder.create();
    }

    private String createErrorMessages(List<String> messages) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<ul style='color: white;'>");
        for (String string : messages) {
            sb.append("<li>").append(string).append("</li>");
        }
        sb.append("</ul>");
        return sb.toString();
    }

    @SuppressLint("NewApi")
    private static class Compability {
        static void setViewLayerTypeSoftware(View v) {
            v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }
}
