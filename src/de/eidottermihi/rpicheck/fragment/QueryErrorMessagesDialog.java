/**
 * 
 */
package de.eidottermihi.rpicheck.fragment;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.webkit.WebView;
import de.eidottermihi.rpicheck.R;

/**
 * Dialog for showing error messages that occured during the query.
 * 
 * @author Michael
 * 
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
