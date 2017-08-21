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
package de.eidottermihi.rpicheck.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.eidottermihi.raspicheck.R;
import de.eidottermihi.rpicheck.activity.helper.Constants;
import de.eidottermihi.rpicheck.activity.helper.Validation;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;
import de.eidottermihi.rpicheck.db.RaspberryDeviceBean;
import io.freefair.android.injection.annotation.InjectView;
import io.freefair.android.injection.annotation.XmlLayout;
import io.freefair.android.injection.annotation.XmlMenu;

@XmlLayout(R.layout.activity_raspi_edit)
@XmlMenu(R.menu.activity_raspi_edit)
public class EditRaspiActivity extends AbstractFileChoosingActivity implements OnItemSelectedListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(EditRaspiActivity.class);

    public static final int REQUEST_EDIT = 10;

    // assigning view elements to fields
    @InjectView(R.id.edit_raspi_name_editText)
    private EditText editTextName;
    @InjectView(R.id.edit_raspi_host_editText)
    private EditText editTextHost;
    @InjectView(R.id.edit_raspi_user_editText)
    private EditText editTextUser;
    @InjectView(R.id.ssh_password_edit_text)
    private EditText editTextPass;
    @InjectView(R.id.edit_raspi_ssh_port_editText)
    private EditText editTextSshPortOpt;
    @InjectView(R.id.edit_raspi_desc_editText)
    private EditText editTextDescription;
    @InjectView(R.id.edit_raspi_sudoPass_editText)
    private EditText editTextSudoPass;

    @InjectView(R.id.spinnerAuthMethod)
    private Spinner spinnerAuth;
    @InjectView(R.id.ssh_password_layout)
    private TextInputLayout sshPasswordLayout;
    @InjectView(R.id.rel_key)
    private RelativeLayout relLayKeyfile;
    @InjectView(R.id.key_password_layout)
    private TextInputLayout keyPasswordLayout;
    @InjectView(R.id.key_password_edit_text)
    private EditText keyPasswordEditText;
    @InjectView(R.id.buttonKeyfile)
    private Button buttonKeyfile;
    @InjectView(R.id.checkboxAsk)
    private CheckBox checkboxAskPassphrase;


    private DeviceDbHelper deviceDb;
    private RaspberryDeviceBean deviceBean;

    private Validation validator = new Validation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raspi_edit);
        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // init sql db
        deviceDb = new DeviceDbHelper(this);

        // read device information
        int deviceId = this.getIntent().getExtras().getInt(Constants.EXTRA_DEVICE_ID);
        deviceBean = deviceDb.read(deviceId);

        // init auth spinner
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.auth_methods, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerAuth.setAdapter(adapter);
        spinnerAuth.setOnItemSelectedListener(this);
        if (deviceBean.getAuthMethod().equals(RaspberryDeviceBean.AUTH_PASSWORD)) {
            spinnerAuth.setSelection(0);
        } else if (deviceBean.getAuthMethod().equals(RaspberryDeviceBean.AUTH_PUBLIC_KEY)) {
            spinnerAuth.setSelection(1);
        } else {
            spinnerAuth.setSelection(2);
        }
        // fill fields according to data from device bean
        fillFromBean();
    }

    private void fillFromBean() {
        // fill fields first that will always be shown
        editTextName.setText(deviceBean.getName());
        editTextHost.setText(deviceBean.getHost());
        editTextUser.setText(deviceBean.getUser());
        editTextSshPortOpt.setText(deviceBean.getPort() + "");
        editTextDescription.setText(deviceBean.getDescription());
        editTextSudoPass.setText(deviceBean.getSudoPass());

        // switch auth method
        final String method = deviceBean.getAuthMethod();
        switchAuthMethodsInView(method);
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
                updateRaspi();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onButtonClick(View view) {
        switch (view.getId()) {
            case R.id.buttonKeyfile:
                startFileChooser();
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
            // remove passphrase from textfield
            keyPasswordEditText.setText("");
        } else {
            // show textfield for passphrase
            keyPasswordLayout.setVisibility(View.VISIBLE);
            keyPasswordEditText.setVisibility(View.VISIBLE);
        }
    }

    private void updateRaspi() {
        final int authMethod = spinnerAuth.getSelectedItemPosition();
        boolean validationSuccessful = validator.validatePiEditData(this, authMethod, editTextName, editTextHost, editTextUser,
                editTextPass, editTextSshPortOpt, editTextSudoPass,
                keyPasswordEditText, buttonKeyfile,
                checkboxAskPassphrase.isChecked(), deviceBean.getKeyfilePath());
        if (validationSuccessful) {
            // getting credentials from textfields
            final String name = editTextName.getText().toString().trim();
            final String host = editTextHost.getText().toString().trim();
            final String user = editTextUser.getText().toString().trim();
            final String sshPort = editTextSshPortOpt.getText().toString().trim();
            final String sudoPass = editTextSudoPass.getText().toString().trim();
            final String description = editTextDescription.getText().toString().trim();
            if (authMethod == 0) {
                final String pass = editTextPass.getText().toString().trim();
                updateRaspiInDb(name, host, user, pass, sshPort, description, sudoPass,
                        RaspberryDeviceBean.SPINNER_AUTH_METHODS[authMethod],
                        null, null);
            } else if (authMethod == 1) {
                final String keyfilePath = deviceBean.getKeyfilePath();
                updateRaspiInDb(name, host, user, null, sshPort, description, sudoPass,
                        RaspberryDeviceBean.SPINNER_AUTH_METHODS[authMethod],
                        keyfilePath, null);
            } else if (authMethod == 2) {
                final String keyfilePath = deviceBean.getKeyfilePath();
                if (checkboxAskPassphrase.isChecked()) {
                    updateRaspiInDb(name, host, user, null, sshPort, description, sudoPass,
                            RaspberryDeviceBean.SPINNER_AUTH_METHODS[authMethod],
                            keyfilePath, null);
                } else {
                    final String keyfilePass = keyPasswordEditText.getText().toString().trim();
                    updateRaspiInDb(name, host, user, null, sshPort, description,
                            sudoPass, RaspberryDeviceBean.SPINNER_AUTH_METHODS[authMethod],
                            keyfilePath, keyfilePass);
                }
            }
            Toast.makeText(this, R.string.update_successful, Toast.LENGTH_SHORT).show();
            // back to main
            this.setResult(RESULT_OK);
            this.finish();
        }
    }

    private void updateRaspiInDb(String name, String host, String user,
                                 String pass, String sshPort, String description, String sudoPass,
                                 String authMethod, String keyfilePath, String keyfilePass) {
        // if sudoPass is null use empty pass
        if (Strings.isNullOrEmpty(sudoPass)) {
            sudoPass = "";
        }
        deviceBean.setName(name);
        deviceBean.setHost(host);
        deviceBean.setUser(user);
        deviceBean.setPass(pass);
        deviceBean.setPort(Integer.parseInt(sshPort));
        deviceBean.setDescription(description);
        deviceBean.setSudoPass(sudoPass);
        deviceBean.setAuthMethod(authMethod);
        deviceBean.setKeyfilePath(keyfilePath);
        deviceBean.setKeyfilePass(keyfilePass);
        new Thread() {
            @Override
            public void run() {
                deviceDb.update(deviceBean);
            }
        }.start();
    }

    private void switchAuthMethodsInView(String method) {
        if (method.equals(RaspberryDeviceBean.AUTH_PASSWORD)) {
            // show only ssh password
            sshPasswordLayout.setVisibility(View.VISIBLE);
            editTextPass.setText(deviceBean.getPass());
            relLayKeyfile.setVisibility(View.GONE);
        } else if (method.equals(RaspberryDeviceBean.AUTH_PUBLIC_KEY)) {
            // show key file button (no passphrase)
            sshPasswordLayout.setVisibility(View.GONE);
            relLayKeyfile.setVisibility(View.VISIBLE);
            initButtonKeyfile();
            checkboxAskPassphrase.setVisibility(View.GONE);
            keyPasswordLayout.setVisibility(View.GONE);
        } else {
            // show key file button and passphrase field
            sshPasswordLayout.setVisibility(View.GONE);
            relLayKeyfile.setVisibility(View.VISIBLE);
            initButtonKeyfile();
            checkboxAskPassphrase.setVisibility(View.VISIBLE);
            if (deviceBean.getKeyfilePass() != null) {
                keyPasswordLayout.setVisibility(View.VISIBLE);
                checkboxAskPassphrase.setChecked(false);
                keyPasswordEditText.setText(deviceBean.getKeyfilePass());
            } else if (!Strings.isNullOrEmpty(keyPasswordEditText.getText()
                    .toString())) {
                keyPasswordLayout.setVisibility(View.VISIBLE);
                checkboxAskPassphrase.setChecked(false);
            } else {
                keyPasswordLayout.setVisibility(View.GONE);
                checkboxAskPassphrase.setChecked(true);
            }
        }
    }

    private void initButtonKeyfile() {
        if (deviceBean.getKeyfilePath() != null) {
            buttonKeyfile.setText(getFilenameFromPath(deviceBean.getKeyfilePath()));
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
                               long arg3) {
        final String selectedAuthMethod = RaspberryDeviceBean.SPINNER_AUTH_METHODS[pos];
        LOGGER.debug("Auth method selected: {}", selectedAuthMethod);
        this.switchAuthMethodsInView(selectedAuthMethod);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_LOAD_FILE && resultCode == Activity.RESULT_OK) {
            final String filePath = data.getData().getPath();
            LOGGER.debug("Path of selected keyfile: {}", filePath);
            deviceBean.setKeyfilePath(filePath);
            // set text to filename, not full path
            final String fileName = getFilenameFromPath(filePath);
            buttonKeyfile.setText(fileName);
        }
    }
}