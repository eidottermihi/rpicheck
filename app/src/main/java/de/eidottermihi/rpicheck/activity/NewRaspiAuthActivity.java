/**
 * Copyright (C) 2015  RasPi Check Contributors
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
package de.eidottermihi.rpicheck.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import de.eidottermihi.raspicheck.R;
import de.eidottermihi.rpicheck.activity.helper.Validation;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;
import de.fhconfig.android.library.injection.annotation.XmlLayout;
import de.fhconfig.android.library.injection.annotation.XmlMenu;
import de.fhconfig.android.library.injection.annotation.XmlView;
import de.fhconfig.android.library.ui.FloatLabelLayout;
import de.fhconfig.android.library.ui.injection.InjectionActionBarActivity;

@XmlLayout(R.layout.activity_raspi_new_auth)
@XmlMenu(R.menu.activity_raspi_new_auth)
public class NewRaspiAuthActivity extends InjectionActionBarActivity implements OnItemSelectedListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(NewRaspiAuthActivity.class);

    public static final String AUTH_PASSWORD = "password";
    public static final String AUTH_PUBLIC_KEY = "keys";
    public static final String AUTH_PUBLIC_KEY_WITH_PASSWORD = "keysWithPassword";
    public static final String[] SPINNER_AUTH_METHODS = {AUTH_PASSWORD, AUTH_PUBLIC_KEY, AUTH_PUBLIC_KEY_WITH_PASSWORD};

    public static final int REQUEST_LOAD = 0;

    private Validation validator = new Validation();

    private DeviceDbHelper deviceDb;

    // assigning view elements to fields
    @XmlView(R.id.spinnerAuthMethod)
    private Spinner spinnerAuth;
    @XmlView(R.id.ssh_password_layout)
    private FloatLabelLayout relLaySshPass;
    @XmlView(R.id.rel_key)
    private RelativeLayout relLayKeyfile;
    @XmlView(R.id.key_password_layout)
    private FloatLabelLayout keyPasswordLayout;
    @XmlView(R.id.ssh_password_edit_text)
    private EditText editTextSshPass;
    @XmlView(R.id.key_password_edit_text)
    private EditText editTextKeyfilePass;
    @XmlView(R.id.buttonKeyfile)
    private Button buttonKeyfile;
    @XmlView(R.id.edit_raspi_ssh_port_editText)
    private EditText editTextSshPort;
    @XmlView(R.id.edit_raspi_sudoPass_editText)
    private EditText editTextSudoPw;
    @XmlView(R.id.checkboxAsk)
    private CheckBox checkboxAskPassphrase;


    private String keyfilePath;

    private String host;
    private String desc;
    private String user;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raspi_new_auth);
        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // show default option for auth method = ssh password
        this.switchAuthMethodsInView(SPINNER_AUTH_METHODS[0]);
        // init auth spinner
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.auth_methods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerAuth.setAdapter(adapter);
        spinnerAuth.setOnItemSelectedListener(this);

        // init sql db
        deviceDb = new DeviceDbHelper(this);

        // get data from previous screen (name/host/user...), already validated
        final Bundle piData = this.getIntent().getExtras().getBundle(NewRaspiActivity.PI_BUNDLE);
        host = piData.getString(NewRaspiActivity.PI_HOST);
        name = piData.getString(NewRaspiActivity.PI_NAME);
        user = piData.getString(NewRaspiActivity.PI_USER);
        desc = piData.getString(NewRaspiActivity.PI_DESC);
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
            case R.id.menu_save:
                saveRaspi();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onButtonClick(View view) {
        switch (view.getId()) {
            case R.id.buttonKeyfile:
                openKeyfile();
                break;
            default:
                break;
        }
    }

    public void onCheckboxClick(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        switch (view.getId()) {
            case R.id.checkboxAsk:
                switchCheckbox(checked);
                break;
            default:
                break;
        }
    }

    private void switchCheckbox(boolean checked) {
        LOGGER.debug("Always ask for passphrase: {}", checked);
        if (checked) {
            // don't show textfield for passphrase
            keyPasswordLayout.setVisibility(View.GONE);
        } else {
            // show textfield for passphrase
            keyPasswordLayout.setVisibility(View.VISIBLE);
        }
    }

    private void openKeyfile() {
        final Intent intent = new Intent(getBaseContext(), FileDialog.class);
        intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory().getPath());

        // can user select directories or not
        intent.putExtra(FileDialog.CAN_SELECT_DIR, false);
        // user can only open existing files
        intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);

        // alternatively you can set file filter
        // intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "png" });
        this.startActivityForResult(intent, REQUEST_LOAD);
    }

    private void saveRaspi() {
        // get auth method
        final String selectedAuthMethod = SPINNER_AUTH_METHODS[spinnerAuth.getSelectedItemPosition()];
        final String sudoPass = editTextSudoPw.getText().toString().trim();
        final String sshPort = editTextSshPort.getText().toString().trim();
        boolean portOk = true;
        // validate ssh port (range 1 to 65535)
        if (!validator.validatePort(editTextSshPort)) {
            portOk = false;
        }
        if (portOk) {
            boolean saveSuccessful = false;
            if (selectedAuthMethod.equals(SPINNER_AUTH_METHODS[0])) {
                // ssh password (cannot be empty)
                if (validator.checkNonOptionalTextField(editTextSshPass, getString(R.string.validation_msg_password))) {
                    final String sshPass = editTextSshPass.getText().toString().trim();
                    addRaspiToDb(name, host, user, selectedAuthMethod, sshPort, desc, sudoPass, sshPass, null, null);
                    saveSuccessful = true;
                }
            } else if (selectedAuthMethod.equals(SPINNER_AUTH_METHODS[1])) {
                // keyfile must be selected
                if (keyfilePath != null && new File(keyfilePath).exists()) {
                    addRaspiToDb(name, host, user, selectedAuthMethod, sshPort, desc, sudoPass, null, null, keyfilePath);
                    saveSuccessful = true;
                } else {
                    buttonKeyfile.setError(getString(R.string.validation_msg_keyfile));
                }
            } else if (selectedAuthMethod.equals(SPINNER_AUTH_METHODS[2])) {
                // keyfile must be selected
                if (keyfilePath != null && new File(keyfilePath).exists()) {
                    if (checkboxAskPassphrase.isChecked()) {
                        addRaspiToDb(name, host, user, selectedAuthMethod, sshPort, desc, sudoPass, null, null, keyfilePath);
                        saveSuccessful = true;
                    } else {
                        // password must be set
                        if (validator.checkNonOptionalTextField(editTextKeyfilePass, getString(R.string.validation_msg_key_passphrase))) {
                            final String keyfilePass = editTextKeyfilePass.getText().toString().trim();
                            addRaspiToDb(name, host, user, selectedAuthMethod, sshPort, desc, sudoPass, null, keyfilePass, keyfilePath);
                            saveSuccessful = true;
                        }
                    }
                } else {
                    buttonKeyfile.setError(getString(R.string.validation_msg_keyfile));
                }
            }
            if (saveSuccessful) {
                Toast.makeText(this, R.string.new_pi_created, Toast.LENGTH_SHORT).show();
                // finish
                this.setResult(RESULT_OK);
                this.finish();
            }
        }
    }

    private void addRaspiToDb(final String name, final String host, final String user,
                              final String authMethod, String sshPort, final String description,
                              String sudoPass, final String sshPass, final String keyPass, final String keyPath) {
        // if sshPort is empty, use default port (22)
        if (Strings.isNullOrEmpty(sshPort)) {
            sshPort = getText(R.string.default_ssh_port).toString();
        }
        if (Strings.isNullOrEmpty(sudoPass)) {
            sudoPass = "";
        }
        final String port = sshPort, pass = sudoPass;
        new Thread() {
            @Override
            public void run() {
                deviceDb.create(name, host, user, sshPass, Integer.parseInt(port),
                        description, pass, authMethod, keyPath, keyPass);
            }
        }.start();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos,
                               long id) {
        final String selectedAuthMethod = SPINNER_AUTH_METHODS[pos];
        LOGGER.debug("Auth method selected: {}", selectedAuthMethod);
        this.switchAuthMethodsInView(selectedAuthMethod);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {

    }

    private void switchAuthMethodsInView(String method) {
        if (method.equals(SPINNER_AUTH_METHODS[0])) {
            // show only ssh password
            relLaySshPass.setVisibility(View.VISIBLE);
            relLayKeyfile.setVisibility(View.GONE);
        } else if (method.equals(SPINNER_AUTH_METHODS[1])) {
            // show key file button (no passphrase)
            relLaySshPass.setVisibility(View.GONE);
            relLayKeyfile.setVisibility(View.VISIBLE);
            checkboxAskPassphrase.setVisibility(View.GONE);
            editTextKeyfilePass.setVisibility(View.GONE);
        } else {
            // show key file button and passphrase field
            relLaySshPass.setVisibility(View.GONE);
            relLayKeyfile.setVisibility(View.VISIBLE);
            checkboxAskPassphrase.setVisibility(View.VISIBLE);
            editTextKeyfilePass.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_LOAD) {
                final String filePath = data
                        .getStringExtra(FileDialog.RESULT_PATH);
                LOGGER.debug("Path of selected keyfile: {}", filePath);
                this.keyfilePath = filePath;
                // set text to filename, not full path
                String fileName = getFilenameFromPath(filePath);
                buttonKeyfile.setText(fileName);
                buttonKeyfile.setError(null);
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            LOGGER.warn("No keyfile selected...");
        }
    }

    public static String getFilenameFromPath(String filePath) {
        final File f = new File(filePath);
        return f.getName();
    }

}
