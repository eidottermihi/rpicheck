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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.eidottermihi.raspicheck.BuildConfig;
import de.eidottermihi.rpicheck.activity.AbstractFileChoosingActivity;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;
import de.eidottermihi.rpicheck.db.RaspberryDeviceBean;

import static de.eidottermihi.rpicheck.activity.SettingsActivity.KEY_PREF_TEMPERATURE_SCALE;
import static de.eidottermihi.rpicheck.activity.SettingsActivity.KEY_PREF_QUERY_HIDE_ROOT_PROCESSES;
import static de.eidottermihi.rpicheck.activity.SettingsActivity.KEY_PREF_FREQUENCY_UNIT;
import static de.eidottermihi.rpicheck.activity.SettingsActivity.KEY_PREF_DEBUG_LOGGING;
import static de.eidottermihi.rpicheck.activity.SettingsActivity.KEY_PREF_QUERY_SHOW_SYSTEM_TIME;

public class ExportSettings extends AbstractFileChoosingActivity {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportSettings.class);
    private String filePath;
    private String fileName;
    private Context mBaseContext;

    //Couldn't come up with any better names
    public void ExportAll(Context baseContext) {
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
         //startDirChooser(mBaseContext);
        this.filePath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        doExport();
    }

    private void doExport() {
        //I briefly skimmed over how andOTP handles backups, which solidified my decision to use JSON and maybe add encryption at a later date.
        LOGGER.debug("Export successfully called with filePath: {}", filePath);
        //This first exports all settings of the app and then every device into a json file, which includes passwords but no keyfiles
        DeviceDbHelper deviceDb;
        RaspberryDeviceBean deviceBean;
        JSONArray fullJSON = new JSONArray();
        Integer i = 1;

        //This causes the same java.lang.NullPointerException as above if
        //'this' instead of 'mBaseContext' is supplied
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mBaseContext);
        String temp_scale = prefs.getString(KEY_PREF_TEMPERATURE_SCALE, null);
        Boolean hide_root = (prefs.getBoolean(KEY_PREF_QUERY_HIDE_ROOT_PROCESSES, false));
        String freq_unit = prefs.getString(KEY_PREF_FREQUENCY_UNIT, null);
        Boolean debug_log = (prefs.getBoolean(KEY_PREF_DEBUG_LOGGING, false));
        Boolean sys_time = (prefs.getBoolean(KEY_PREF_QUERY_SHOW_SYSTEM_TIME, false));
        //LOGGER.debug("temp_scale:"+temp_scale+"|hide_root:"+String.valueOf(hide_root)+ "|freq_unit:"+
        //        freq_unit+"|debug_log:"+String.valueOf(debug_log)+"|sys_time:"+String.valueOf(sys_time));

        JSONObject userSettings = new JSONObject();
        userSettings.put("temp_scale", temp_scale);
        userSettings.put("hide_root", hide_root);
        userSettings.put("freq_unit", freq_unit);
        userSettings.put("debug_log", debug_log);
        userSettings.put("sys_time", sys_time);
        //Adding the version code just in case something changes in the future and
        //it has to be imported differently (or throw out an "too old" error).
        userSettings.put("version_code", BuildConfig.VERSION_CODE);
        LOGGER.debug(userSettings.toJSONString());

        JSONArray devices = new JSONArray();
        deviceDb = new DeviceDbHelper(mBaseContext);
        while(true) {
            deviceBean = deviceDb.read(i);
            if (deviceBean != null) {
                JSONObject device = new JSONObject();
                device.put("_id", deviceBean.getId());
                device.put("name", deviceBean.getName());
                device.put("description", deviceBean.getDescription());
                device.put("host", deviceBean.getHost());
                device.put("user", deviceBean.getUser());
                device.put("passwd", deviceBean.getPass());
                device.put("sudo_passwd", deviceBean.getSudoPass());
                device.put("ssh_port", deviceBean.getPort());
                //getCreatedAt() and getModifiedAt() returns an Java Date, which JSON does not like.
                //Originally intended to save it as a string, but because I can't
                //import it through DeviceDbHelper I'll leave it out.
                //device.put("created_at", String.valueOf(deviceBean.getCreatedAt()));
                //device.put("modified_at", String.valueOf(deviceBean.getModifiedAt()));
                device.put("serial", deviceBean.getSerial());
                device.put("auth_method", deviceBean.getAuthMethod());
                device.put("keyfile_path", deviceBean.getKeyfilePath());
                device.put("keyfile_pass", deviceBean.getKeyfilePass());
                LOGGER.debug(device.toJSONString());
                devices.add(device);
            } else {
                break;
            }
            i++;
        }

        fullJSON.add(userSettings);
        fullJSON.add(devices);

        //Write file
        this.fileName = "rpicheck_export.json";
        File exportFile = new File(filePath, fileName);

        try (FileWriter writer = new FileWriter(exportFile, false)) {
            writer.write(fullJSON.toJSONString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LOGGER.debug("onActivityResult called");
        if (requestCode == REQUEST_CODE_LOAD_FILE && resultCode == Activity.RESULT_OK) {
            final String filePath = data.getData().getPath();
            LOGGER.debug("Selected path: {}", filePath);
            this.filePath = filePath;
            doExport();
        }
    }
}