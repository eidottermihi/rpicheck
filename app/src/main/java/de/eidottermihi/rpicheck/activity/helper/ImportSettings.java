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


import de.eidottermihi.rpicheck.activity.AbstractFileChoosingActivity;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;

import static de.eidottermihi.rpicheck.activity.SettingsActivity.KEY_PREF_TEMPERATURE_SCALE;
import static de.eidottermihi.rpicheck.activity.SettingsActivity.KEY_PREF_QUERY_HIDE_ROOT_PROCESSES;
import static de.eidottermihi.rpicheck.activity.SettingsActivity.KEY_PREF_FREQUENCY_UNIT;
import static de.eidottermihi.rpicheck.activity.SettingsActivity.KEY_PREF_DEBUG_LOGGING;
import static de.eidottermihi.rpicheck.activity.SettingsActivity.KEY_PREF_QUERY_SHOW_SYSTEM_TIME;

public class ImportSettings extends AbstractFileChoosingActivity {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportSettings.class);
    private String filePath;
    private Context mBaseContext;
    private JSONObject settingsJson;
    private Boolean errorHappened = false;

    //Couldn't come up with any better names
    public Boolean ImportAll(Context baseContext) {
        LOGGER.debug("ExportAll sucessfully called");
        this.mBaseContext = baseContext;

        /*Calling the function for choosing the folder here causes an
         "java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String android.content.Context.getPackageName()' on a null object reference"
         error. I have no clue how to fix this so for now I'll just hardcode the download directory.

          Ok so it turns out when I call this method, the mBase object (whatever that does) gets set to null (or not set at all),
          which causes all of these problems when ContextWrapper.java tries to
          return mBase.getPackageName()
          I know that now but still have no clue how to fix this.

          Ok so passing the base context of SettingsActivity.java fixing the context problems,
          but now I get 'java.lang.NullPointerException: Attempt to invoke virtual method 'android.app.ActivityThread$ApplicationThread android.app.ActivityThread.getApplicationThread()' on a null object reference'
          which I don't think I can fix. At least SharedPreferences works with the mBaseContext workaround.
         */
         //startFileChooser(mBaseContext);
        this.filePath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))+"/rpicheck_export.json";
        doImport();
        //Unsure if code returns here if the filepicker works, may have to move this to onActivityResult.
        if (!errorHappened) {
            return true;
        }else{
            return false;
        }
    }

    private void doImport() {
        LOGGER.debug("Import successfully called with file: {}", filePath);
        DeviceDbHelper deviceDb;
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader(filePath)) {

            JSONArray fullJsonArray = (JSONArray) jsonParser.parse(reader);

            this.settingsJson = (JSONObject) fullJsonArray.get(0);
            JSONArray deviceJson = (JSONArray) fullJsonArray.get(1);
            LOGGER.debug(settingsJson.toString());
            LOGGER.debug(deviceJson.toString());
            deviceDb = new DeviceDbHelper(mBaseContext);

            //Validate the data before wiping the DB
            //ToDo add validation for the settings and finish the device validation
            for (int i = 0; i < deviceJson.size(); i++) {
                JSONObject dObj = (JSONObject) deviceJson.get(i);
                String name = Strings.nullToEmpty((String) dObj.get("name"));
                String host = Strings.nullToEmpty((String) dObj.get("host"));
                String user = Strings.nullToEmpty((String) dObj.get("user"));
                String pass = Strings.nullToEmpty((String) dObj.get("pass"));
                if (dObj.get("ssh_port") != null  && dObj.get("ssh_port") instanceof Number) {
                    Integer ssh_port = (int) (long) dObj.get("ssh_port");
                }else {
                    this.errorHappened = true;
                    return;
                }
                String description = Strings.nullToEmpty((String) dObj.get("description"));
                String sudo_passwd = Strings.nullToEmpty((String) dObj.get("sudo_passwd"));
                String auth_method = Strings.nullToEmpty((String) dObj.get("auth_method"));
                String keyfile_path = Strings.nullToEmpty((String) dObj.get("keyfile_path"));
                String keyfile_pass = Strings.nullToEmpty((String) dObj.get("keyfile_pass"));


                if (name.isEmpty() || host.isEmpty() || user.isEmpty() || auth_method.isEmpty()) {

                }


            }
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mBaseContext);
            SharedPreferences.Editor prefEdit = prefs.edit();
            prefEdit.putString(KEY_PREF_TEMPERATURE_SCALE, (String) settingsJson.get("temp_scale"));
            prefEdit.putBoolean(KEY_PREF_QUERY_HIDE_ROOT_PROCESSES, (Boolean) settingsJson.get("hide_root"));
            prefEdit.putString(KEY_PREF_FREQUENCY_UNIT, (String) settingsJson.get("freq_unit"));
            prefEdit.putBoolean(KEY_PREF_DEBUG_LOGGING, (Boolean) settingsJson.get("debug_log"));
            prefEdit.putBoolean(KEY_PREF_QUERY_SHOW_SYSTEM_TIME, (Boolean) settingsJson.get("sys_time"));
            prefEdit.apply();

            deviceDb.wipeAllData();
            for (int i = 0; i < deviceJson.size(); i++) {
                JSONObject dObj = (JSONObject) deviceJson.get(i);
                LOGGER.debug(dObj.toString());
                String name = (String) dObj.get("name");
                String host = (String) dObj.get("host");
                String user = (String) dObj.get("user");
                String pass = (String) dObj.get("pass");
                Integer ssh_port = (int) (long) dObj.get("ssh_port");
                String description = (String) dObj.get("description");
                String sudo_passwd = (String) dObj.get("sudo_passwd");
                String auth_method = (String) dObj.get("auth_method");
                String keyfile_path = (String) dObj.get("keyfile_path");
                String keyfile_pass = (String) dObj.get("keyfile_pass");
                deviceDb.create(name, host, user, pass, ssh_port, description,
                        sudo_passwd, auth_method, keyfile_path, keyfile_pass);
            }

        } catch(FileNotFoundException e){
            this.errorHappened = true;
            e.printStackTrace();
        } catch(IOException e){
            this.errorHappened = true;
            e.printStackTrace();
        } catch(ParseException e){
            this.errorHappened = true;
            e.printStackTrace();
        } catch(Exception e){
            this.errorHappened = true;
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LOGGER.debug("onActivityResult called");
        if (requestCode == REQUEST_CODE_LOAD_FILE && resultCode == Activity.RESULT_OK) {
            final String filePath = data.getData().getPath();
            LOGGER.debug("Selected file: {}", filePath);
            this.filePath = filePath;
            doImport();
        }
    }
}