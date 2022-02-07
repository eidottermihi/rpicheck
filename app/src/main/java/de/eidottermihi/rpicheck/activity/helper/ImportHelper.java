/**
 * MIT License
 *
 * Copyright (c) 2022  RasPi Check Contributors
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
package de.eidottermihi.rpicheck.activity.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.google.common.base.Strings;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;


import de.eidottermihi.raspicheck.R;
import de.eidottermihi.rpicheck.activity.AbstractFileChoosingActivity;
import de.eidottermihi.rpicheck.db.CommandBean;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;
import de.eidottermihi.rpicheck.db.RaspberryDeviceBean;

import static de.eidottermihi.rpicheck.activity.SettingsActivity.KEY_PREF_TEMPERATURE_SCALE;
import static de.eidottermihi.rpicheck.activity.SettingsActivity.KEY_PREF_QUERY_HIDE_ROOT_PROCESSES;
import static de.eidottermihi.rpicheck.activity.SettingsActivity.KEY_PREF_FREQUENCY_UNIT;
import static de.eidottermihi.rpicheck.activity.SettingsActivity.KEY_PREF_DEBUG_LOGGING;
import static de.eidottermihi.rpicheck.activity.SettingsActivity.KEY_PREF_QUERY_SHOW_SYSTEM_TIME;

public class ImportHelper extends AbstractFileChoosingActivity {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportHelper.class);
    private String filePath;
    private Context mBaseContext;
    private String errorString = null;

    //Couldn't come up with any better names for the methods
    public String ImportAll(Context baseContext) {
        this.mBaseContext = baseContext;

        /*Calling the function for choosing the folder here causes an
         "java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String android.content.Context.getPackageName()' on a null object reference"
         error. I have no clue how to fix this so for now I'll just hardcode the download directory.

          Ok so it turns out when I call this method, the mBase object (whatever that does) gets set to null (or not set at all),
          which causes all of these problems when ContextWrapper.java tries to
          return mBase.getPackageName()
          I know that now but still have no clue how to fix this.

          Ok so passing the base context of SettingsActivity.java fixing the context problems, but now I get
          'java.lang.NullPointerException: Attempt to invoke virtual method 'android.app.ActivityThread$ApplicationThread android.app.ActivityThread.getApplicationThread()' on a null object reference'
          which I don't think I can fix. At least SharedPreferences works with the mBaseContext workaround.
         */
        //startFileChooser();
        this.filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +"/rpicheck_export.json";
        doImport();
        //Unsure if code returns here if the filepicker works, may have to move this to onActivityResult.
        //Maybe it doesn't even return to SettingsActivity? That would be annoying.
        return errorString;
    }

    private void doImport() {
        LOGGER.info("Import called with file: {}", filePath);
        DeviceDbHelper deviceDb;
        JSONObject settingsJson;
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader(filePath)) {

            JSONArray fullJsonArray = (JSONArray) jsonParser.parse(reader);

            settingsJson = (JSONObject) fullJsonArray.get(0);
            JSONArray deviceJson = (JSONArray) fullJsonArray.get(1);
            JSONArray commandJson = (JSONArray) fullJsonArray.get(2);
            deviceDb = new DeviceDbHelper(mBaseContext);

            //Validate the data before wiping the DB
            for (int i = 0; i < deviceJson.size(); i++) {
                LOGGER.debug("Validating device {}", i);
                JSONObject dObj = (JSONObject) deviceJson.get(i);
                String name = Strings.nullToEmpty((String) dObj.get("name"));
                String host = Strings.nullToEmpty((String) dObj.get("host"));
                String user = Strings.nullToEmpty((String) dObj.get("user"));
                String pass = Strings.nullToEmpty((String) dObj.get("pass"));
                //Check if the port isn't null and is actually a number before assigning to prevent an error.
                int ssh_port;
                if (dObj.get("ssh_port") != null && dObj.get("ssh_port") instanceof Number) {
                    //noinspection ConstantConditions
                    ssh_port = (int) (long) dObj.get("ssh_port");
                } else {
                    LOGGER.error("SSH-Port null or NaN in device {}", i);
                    this.errorString = mBaseContext.getResources().getString(R.string.import_err_null_nan_port);
                    return;
                }
                /*Description and sudo-passwd can be empty, no need to check for that
                String description = Strings.nullToEmpty((String) dObj.get("description"));
                String sudo_passwd = Strings.nullToEmpty((String) dObj.get("sudo_passwd"));*/
                String auth_method = Strings.nullToEmpty((String) dObj.get("auth_method"));
                String keyfile_path = Strings.nullToEmpty((String) dObj.get("keyfile_path"));
                String keyfile_pass = Strings.nullToEmpty((String) dObj.get("keyfile_pass"));

                //Check if required fields are not empty
                if (name.isEmpty()) {
                    LOGGER.error("Name is empty in device {}", i);
                    this.errorString = mBaseContext.getResources().getString(R.string.import_err_null_device_name);
                    return;
                }
                if (host.isEmpty()) {
                    LOGGER.error("Hostname/IP is empty in device {}", i);
                    this.errorString = mBaseContext.getResources().getString(R.string.import_err_null_ip);
                    return;
                }
                if (user.isEmpty()) {
                    LOGGER.error("SSH username is empty in device {}", i);
                    this.errorString = mBaseContext.getResources().getString(R.string.import_err_null_ssh_user);
                    return;
                }
                if (auth_method.isEmpty()) {
                    LOGGER.error("Auth method is empty in device {}", i);
                    this.errorString = mBaseContext.getResources().getString(R.string.import_err_null_auth_method);
                    return;
                }

                //Check if the port is in the valid range
                if (ssh_port < 1 || ssh_port > 65535) {
                    LOGGER.error("SSH-Port \"{}\" in device {} is invalid", ssh_port, i);
                    this.errorString = mBaseContext.getResources().getString(R.string.import_err_invalid_port);
                    return;
                }

                //Check if the required fields for the respective auth method are not empty
                switch (auth_method) {
                    case RaspberryDeviceBean.AUTH_PASSWORD:
                        if (pass.isEmpty()) {
                            LOGGER.error("SSH-Password is empty in device {}", i);
                            this.errorString = mBaseContext.getResources().getString(R.string.import_err_null_ssh_pass);
                            return;
                        }
                        break;
                    case RaspberryDeviceBean.AUTH_PUBLIC_KEY:
                        //ToDo Maybe just default it to some system-path (like Downloads or Documents)
                        // with a warning if it's missing/empty as it's not essential?
                        if (keyfile_path.isEmpty()) {
                            LOGGER.error("Keyfile path is empty in device {}", i);
                            this.errorString = mBaseContext.getResources().getString(R.string.import_err_null_key_path);
                            return;
                        }
                        break;
                    case RaspberryDeviceBean.AUTH_PUBLIC_KEY_WITH_PASSWORD:
                        if (keyfile_path.isEmpty()) {
                            LOGGER.error("Keyfile path is empty in device {}", i);
                            this.errorString = mBaseContext.getResources().getString(R.string.import_err_null_key_path);
                            return;
                        }
                        if (keyfile_pass.isEmpty()) {
                            LOGGER.error("Keyfile password is empty in device {}", i);
                            this.errorString = mBaseContext.getResources().getString(R.string.import_err_null_key_pass);
                            return;
                        }
                        break;
                    default:
                        LOGGER.error("auth_method does not match one of the valid settings in device {}", i);
                        this.errorString = mBaseContext.getResources().getString(R.string.import_err_invalid_auth_method);
                        return;
                }
            }

            //Validate if commands conform to the DB
            for (int i = 0; i < commandJson.size(); i++) {
                LOGGER.debug("Validating command {}", i);
                JSONObject commandJSON = (JSONObject) commandJson.get(i);
                //_id can be hardcoded because it is never needed for the creation of a command.
                if (Strings.nullToEmpty((String) commandJSON.get("name")).isEmpty()) {
                    LOGGER.error("Name is empty in command {}", i);
                    this.errorString = mBaseContext.getResources().getString(R.string.import_err_null_command_name);
                    return;
                }

                if (Strings.nullToEmpty((String) commandJSON.get("command")).isEmpty()) {
                    LOGGER.error("Command is empty in command {}", i);
                    this.errorString = mBaseContext.getResources().getString(R.string.import_err_null_command);
                    return;
                }

                if (commandJSON.get("flag_output") == null || !(commandJSON.get("flag_output") instanceof Boolean)) {
                    LOGGER.error("flag_output is null or not boolean in command {}", i);
                    this.errorString = mBaseContext.getResources().getString(R.string.import_err_null_nabool_flag_output);
                    return;
                }

                if (commandJSON.get("timeout") == null || !(commandJSON.get("timeout") instanceof Number)) {
                    LOGGER.error("Timeout is null or NaN in command {}", i);
                    this.errorString = mBaseContext.getResources().getString(R.string.import_err_null_nan_timeout);
                    return;
                }
            }

                //Check if the SharedPreferences have the right type, the right values and aren't null
                if (settingsJson.get("temp_scale") != null) {
                    //noinspection ConstantConditions
                    if (!((String) settingsJson.get("temp_scale")).matches(
                            mBaseContext.getResources().getStringArray(R.array.temp_scales_values)[0]+"|"+
                                    mBaseContext.getResources().getStringArray(R.array.temp_scales_values)[1])) {
                        LOGGER.error("Temperature scale is not \""+
                                mBaseContext.getResources().getStringArray(R.array.temp_scales_values)[0]
                                +"\" or \""+
                                mBaseContext.getResources().getStringArray(R.array.temp_scales_values)[1]
                                +"\"");
                        this.errorString = mBaseContext.getResources().getString(R.string.import_err_invalid_temp_scale);
                        return;
                    }
                } else {
                    LOGGER.error("Temperature scale is null");
                    this.errorString = mBaseContext.getResources().getString(R.string.import_err_null_temp_scale);
                    return;
                }

                if (settingsJson.get("hide_root") == null) {
                    LOGGER.error("Hide root processes is null");
                    this.errorString = mBaseContext.getResources().getString(R.string.import_err_null_hide_root);
                    return;
                }

                if (settingsJson.get("freq_unit") != null) {
                    //noinspection ConstantConditions
                    if (!((String) settingsJson.get("freq_unit")).matches(
                            mBaseContext.getResources().getStringArray(R.array.pref_frequency_unit_values)[0]+"|"+
                                    mBaseContext.getResources().getStringArray(R.array.pref_frequency_unit_values)[1]+"|"+
                                    mBaseContext.getResources().getStringArray(R.array.pref_frequency_unit_values)[2])) {
                        LOGGER.error("Frequency units are not \""+
                                mBaseContext.getResources().getStringArray(R.array.pref_frequency_unit_values)[0]+
                                "\" or \""+mBaseContext.getResources().getStringArray(R.array.pref_frequency_unit_values)[1]+
                                "\" or \""+mBaseContext.getResources().getStringArray(R.array.pref_frequency_unit_values)[2]+"\"");
                        this.errorString = mBaseContext.getResources().getString(R.string.import_err_invalid_freq_unit);
                        return;
                    }
                } else {
                    LOGGER.error("Frequency unit is null");
                    this.errorString = mBaseContext.getResources().getString(R.string.import_err_null_freq_unit);
                    return;
                }

            if (settingsJson.get("debug_log") == null) {
                LOGGER.error("Debug logging is null");
                this.errorString = mBaseContext.getResources().getString(R.string.import_err_null_debug_log);
                return;
            }
            if (settingsJson.get("sys_time") == null) {
                LOGGER.error("Show system time is null");
                this.errorString = mBaseContext.getResources().getString(R.string.import_err_null_sys_time);
                return;
            }


            //Everything is valid and nothing is missing, time to apply it!

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mBaseContext);
            SharedPreferences.Editor prefEdit = prefs.edit();

            prefEdit.putString(KEY_PREF_TEMPERATURE_SCALE, (String) settingsJson.get("temp_scale"));
            //noinspection ConstantConditions
            prefEdit.putBoolean(KEY_PREF_QUERY_HIDE_ROOT_PROCESSES, (Boolean) settingsJson.get("hide_root"));
            prefEdit.putString(KEY_PREF_FREQUENCY_UNIT, (String) settingsJson.get("freq_unit"));
            //noinspection ConstantConditions
            prefEdit.putBoolean(KEY_PREF_DEBUG_LOGGING, (Boolean) settingsJson.get("debug_log"));
            //noinspection ConstantConditions
            prefEdit.putBoolean(KEY_PREF_QUERY_SHOW_SYSTEM_TIME, (Boolean) settingsJson.get("sys_time"));

            prefEdit.apply();

            //ToDo Maybe make wiping all data optional via a dialog asking the user?
            //  Shouldn't need any change except for an if around the wipeAllData call.
            //Note: wipeAllData does not reset autoincrement fields back to 1.
            deviceDb.wipeAllData();

            for (int i = 0; i < deviceJson.size(); i++) {
                JSONObject dObj = (JSONObject) deviceJson.get(i);

                String name = (String) dObj.get("name");
                String host = (String) dObj.get("host");
                String user = (String) dObj.get("user");
                String pass = (String) dObj.get("pass");
                //noinspection ConstantConditions
                int ssh_port = (int) (long) dObj.get("ssh_port");
                String description = (String) dObj.get("description");
                String sudo_passwd = (String) dObj.get("sudo_passwd");
                String auth_method = (String) dObj.get("auth_method");
                String keyfile_path = (String) dObj.get("keyfile_path");
                String keyfile_pass = (String) dObj.get("keyfile_pass");

                deviceDb.create(name, host, user, pass, ssh_port, description,
                        sudo_passwd, auth_method, keyfile_path, keyfile_pass);
            }

            for (int i = 0; i < commandJson.size(); i++) {
                JSONObject commandJSON = (JSONObject) commandJson.get(i);
                final CommandBean cmdBean = new CommandBean();
                cmdBean.setId(1);
                cmdBean.setName(Strings.nullToEmpty((String) commandJSON.get("name")));
                cmdBean.setCommand(Strings.nullToEmpty((String) commandJSON.get("command")));
                // noinspection ConstantConditions
                cmdBean.setShowOutput((boolean) commandJSON.get("flag_output"));
                //noinspection ConstantConditions
                cmdBean.setTimeout((int) (long) commandJSON.get("timeout"));

                deviceDb.create(cmdBean);
            }

        } catch(FileNotFoundException e){
            this.errorString = mBaseContext.getResources().getString(R.string.err_file_not_found);
            LOGGER.error(e.toString());
            //Unsure if I really want to write the entire stack trace into the debug log,
            //but why not? Debug logging has to be explicitly enabled anyway.
            LOGGER.debug(Arrays.toString(e.getStackTrace()));
        } catch(IOException e){
            this.errorString = mBaseContext.getResources().getString(R.string.err_ioexception);
            LOGGER.error(e.toString());
            LOGGER.debug(Arrays.toString(e.getStackTrace()));
        } catch(ParseException e){
            this.errorString = mBaseContext.getResources().getString(R.string.import_err_json_parse);
            LOGGER.error(e.getMessage() != null ? e.getMessage() : e.toString());
            LOGGER.debug(Arrays.toString(e.getStackTrace()));
        } catch(Exception e){
            this.errorString = mBaseContext.getResources().getString(R.string.err_exception);
            LOGGER.error(e.toString());
            LOGGER.debug(Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_LOAD_FILE && resultCode == Activity.RESULT_OK) {
            final String filePath = data.getData().getPath();
            LOGGER.debug("Selected file: {}", filePath);
            this.filePath = filePath;
            doImport();
        }
    }
}