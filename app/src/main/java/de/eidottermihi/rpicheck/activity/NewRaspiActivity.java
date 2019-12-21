/**
 * MIT License
 *
 * Copyright (c) 2019  RasPi Check Contributors
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
package de.eidottermihi.rpicheck.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.eidottermihi.raspicheck.R;
import de.eidottermihi.rpicheck.activity.helper.Validation;
import io.freefair.android.injection.annotation.InjectView;
import io.freefair.android.injection.annotation.XmlMenu;
import io.freefair.android.injection.app.InjectionAppCompatActivity;

@XmlMenu(R.menu.activity_raspi_new)
public class NewRaspiActivity extends InjectionAppCompatActivity {
    public static final String PI_HOST = "PI_HOST";
    public static final String PI_NAME = "PI_NAME";
    public static final String PI_USER = "PI_USER";
    public static final String PI_DESC = "PI_DESC";
    public static final String PI_BUNDLE = "PI_BUNDLE";
    public static final int REQUEST_SAVE = 0;
    private static final Logger LOGGER = LoggerFactory.getLogger(NewRaspiActivity.class);

    // assigning view elements to fields
    @InjectView(R.id.edit_raspi_name_editText)
    private EditText editTextName;
    @InjectView(R.id.edit_raspi_host_editText)
    private EditText editTextHost;
    @InjectView(R.id.edit_raspi_user_editText)
    private EditText editTextUser;
    @InjectView(R.id.edit_raspi_desc_editText)
    private EditText editTextDescription;


    private Validation validator = new Validation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raspi_new);
        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Show information text
        final View text = findViewById(R.id.new_raspi_text);
        text.setVisibility(View.VISIBLE);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.menu_continue:
                continueToAuthMethodActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void continueToAuthMethodActivity() {
        boolean validationSuccessful = validator.validatePiCoreData(this, editTextName, editTextHost, editTextUser);
        if (validationSuccessful) {
            // getting credentials from textfields
            final String name = editTextName.getText().toString().trim();
            final String host = editTextHost.getText().toString().trim();
            final String user = editTextUser.getText().toString().trim();
            final String description = editTextDescription.getText().toString().trim();
            // continue to auth activity
            final Intent intent = new Intent(NewRaspiActivity.this, NewRaspiAuthActivity.class);
            final Bundle bundle = new Bundle();
            bundle.putString(PI_NAME, name);
            bundle.putString(PI_HOST, host);
            bundle.putString(PI_USER, user);
            bundle.putString(PI_DESC, description);
            intent.putExtra(PI_BUNDLE, bundle);
            this.startActivityForResult(intent, REQUEST_SAVE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SAVE && resultCode == RESULT_OK) {
            // user saved the new raspi
            setResult(RESULT_OK);
            finish();
        }
    }

}
