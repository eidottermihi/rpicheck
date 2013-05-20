package de.eidottermihi.rpicheck.activity.helper;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.widget.EditText;
import de.eidottermihi.rpicheck.R;

/**
 * A helper class for validating user input.
 * 
 * @author Michael
 * 
 */
public class Validation {

	/**
	 * Validates if user input is valid.
	 * 
	 * @param name
	 * @param host
	 * @param user
	 * @param password
	 * @param port
	 * @param sudoPass
	 * @return true, if input is valid
	 */
	public boolean validatePiData(Context context, EditText name,
			EditText host, EditText user, EditText password, EditText port,
			EditText sudoPass) {
		boolean dataValid = true;
		if (!checkNonOptionalTextField(name,
				context.getString(R.string.validation_msg_name))) {
			dataValid = false;
		}
		if (!checkNonOptionalTextField(host,
				context.getString(R.string.validation_msg_host))) {
			dataValid = false;
		}
		if (!checkNonOptionalTextField(user,
				context.getString(R.string.validation_msg_user))) {
			dataValid = false;
		}
		if (!checkNonOptionalTextField(password,
				context.getString(R.string.validation_msg_password))) {
			dataValid = false;
		}
		final String piPortString = port.getText().toString().trim();
		if (!StringUtils.isBlank(piPortString)) {
			boolean portValid = true;
			try {
				int portNr = Integer.parseInt(piPortString);
				if (portNr < 1 || portNr > 65535) {
					portValid = false;
				}
			} catch (NumberFormatException e) {
				portValid = false;
			}
			if (!portValid) {
				port.setError(context.getString(R.string.validation_msg_port));
			}
		}
		return dataValid;
	}

	/**
	 * Checks if a Textfield is not blank.
	 * 
	 * @param textfield
	 *            the EditText to check
	 * @param errorMessage
	 *            the errorMessage to set if validation fails
	 * @return true, if valid, else false
	 */
	private boolean checkNonOptionalTextField(EditText textfield,
			String errorMessage) {
		// get text
		final String text = textfield.getText().toString().trim();
		if (text == null || StringUtils.isBlank(text)) {
			textfield.setError(errorMessage);
			return false;
		}
		return true;
	}
}
