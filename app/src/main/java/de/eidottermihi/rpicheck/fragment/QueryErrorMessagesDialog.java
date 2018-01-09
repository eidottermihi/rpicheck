/**
 * MIT License
 *
 * Copyright (c) 2018  RasPi Check Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
