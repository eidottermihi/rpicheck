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
import android.database.Cursor;
import android.os.Environment;
import android.preference.PreferenceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import de.eidottermihi.raspicheck.BuildConfig;
import de.eidottermihi.raspicheck.R;
import de.eidottermihi.rpicheck.activity.AbstractFileChoosingActivity;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;
import de.eidottermihi.rpicheck.db.RaspberryDeviceBean;

import static de.eidottermihi.rpicheck.activity.SettingsActivity.KEY_PREF_TEMPERATURE_SCALE;
import static de.eidottermihi.rpicheck.activity.SettingsActivity.KEY_PREF_QUERY_HIDE_ROOT_PROCESSES;
import static de.eidottermihi.rpicheck.activity.SettingsActivity.KEY_PREF_FREQUENCY_UNIT;
import static de.eidottermihi.rpicheck.activity.SettingsActivity.KEY_PREF_DEBUG_LOGGING;
import static de.eidottermihi.rpicheck.activity.SettingsActivity.KEY_PREF_QUERY_SHOW_SYSTEM_TIME;

@SuppressWarnings("unchecked")
public class ExportSettings extends AbstractFileChoosingActivity {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportSettings.class);
    private String filePath;
    private Context mBaseContext;
    private String errorString = null;

    //Couldn't come up with any better names
    public String ExportAll(Context baseContext) {
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

        return errorString;
    }

    private void doExport() {
        LOGGER.info("Export called with filePath: {}", filePath);
        //I briefly skimmed over how andOTP handles backups, which solidified my decision to use
        // JSON and maybe add encryption at a later date because of the passwords.
        DeviceDbHelper deviceDb;
        RaspberryDeviceBean deviceBean;
        JSONArray fullJSON = new JSONArray();
        //This first exports all SharedPreferences of the app, then every device and then all commands
        // into a json file, which includes passwords but no keyfiles, only their path.

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mBaseContext);
        String temp_scale = prefs.getString(KEY_PREF_TEMPERATURE_SCALE, null);
        Boolean hide_root = (prefs.getBoolean(KEY_PREF_QUERY_HIDE_ROOT_PROCESSES, false));
        String freq_unit = prefs.getString(KEY_PREF_FREQUENCY_UNIT, null);
        Boolean debug_log = (prefs.getBoolean(KEY_PREF_DEBUG_LOGGING, false));
        Boolean sys_time = (prefs.getBoolean(KEY_PREF_QUERY_SHOW_SYSTEM_TIME, false));
        JSONObject userSettings = new JSONObject();
        userSettings.put("temp_scale", temp_scale);
        userSettings.put("hide_root", hide_root);
        userSettings.put("freq_unit", freq_unit);
        userSettings.put("debug_log", debug_log);
        userSettings.put("sys_time", sys_time);
        /*
        Was considering using the KEY variables as the keys for the JSON, but any change would
        have made the JSON unusable so I'll stick with hardcoded names. Code stays here just in case.
        Maybe make the keys "global" variables?
        userSettings.put(KEY_PREF_TEMPERATURE_SCALE, temp_scale);
        userSettings.put(KEY_PREF_QUERY_HIDE_ROOT_PROCESSES, hide_root);
        userSettings.put(KEY_PREF_FREQUENCY_UNIT, freq_unit);
        userSettings.put(KEY_PREF_DEBUG_LOGGING, debug_log);
        userSettings.put(KEY_PREF_QUERY_SHOW_SYSTEM_TIME, sys_time);
         */
        //Adding the version code just in case something changes in the future and
        //it has to be imported differently (or throw out an "too old" error).
        userSettings.put("version_code", BuildConfig.VERSION_CODE);


        JSONArray devices = new JSONArray();
        deviceDb = new DeviceDbHelper(mBaseContext);
        Cursor deviceCursor = deviceDb.getFullDeviceCursor();
        boolean cursorEnded = deviceCursor.moveToFirst();
        while (cursorEnded) {
            deviceBean = deviceDb.read(deviceCursor.getLong(0));

            JSONObject device = new JSONObject();
            device.put("_id", deviceBean.getId());
            device.put("name", deviceBean.getName());
            device.put("description", deviceBean.getDescription());
            device.put("host", deviceBean.getHost());
            device.put("user", deviceBean.getUser());
            device.put("passwd", deviceBean.getPass());
            device.put("sudo_passwd", deviceBean.getSudoPass());
            device.put("ssh_port", deviceBean.getPort());
            /*getCreatedAt() and getModifiedAt() returns an Java Date, which JSON does not like.
              Originally intended to save it as a string, but because I can't
              import it through DeviceDbHelper I'll leave it out.
              device.put("created_at", String.valueOf(deviceBean.getCreatedAt()));
              device.put("modified_at", String.valueOf(deviceBean.getModifiedAt()));*/
            device.put("serial", deviceBean.getSerial());
            device.put("auth_method", deviceBean.getAuthMethod());
            device.put("keyfile_path", deviceBean.getKeyfilePath());
            device.put("keyfile_pass", deviceBean.getKeyfilePass());
            devices.add(device);

            cursorEnded = deviceCursor.moveToNext();
        }
        deviceCursor.close();

        JSONArray commands = new JSONArray();
        Cursor commandCursor = deviceDb.getFullCommandCursor();
        cursorEnded = commandCursor.moveToFirst();
        while (cursorEnded) {
            JSONObject command = new JSONObject();
            //_id is not needed as it's an autoincrement field in the DB
            //command.put("_id", commandCursor.getInt(0));
            command.put("name", commandCursor.getString(1));
            command.put("command", commandCursor.getString(2));
            //noinspection SimplifiableConditionalExpression
            command.put("flag_output", commandCursor.getInt(3) == 1 ? true : false);
            command.put("timeout", commandCursor.getInt(4));
            commands.add(command);

            cursorEnded = commandCursor.moveToNext();
        }
        commandCursor.close();

        fullJSON.add(userSettings);
        fullJSON.add(devices);
        fullJSON.add(commands);

        //Write file
        String fileName = "rpicheck_export.json";
        int fileNumber = 1;
        File exportFile = new File(filePath, fileName);
        //Append a number to the name instead of overwriting old exports.
        // Kinda brute-force but even at 1.000.000 files it takes just takes 20 seconds (emulator).
        while(exportFile.exists()) {
            fileName = "rpicheck_export("+ fileNumber +").json";
            exportFile = new File(filePath, fileName);
            fileNumber += 1;
        }

        try (FileWriter writer = new FileWriter(exportFile, false)) {
            writer.write(fullJSON.toJSONString());
            writer.flush();
            LOGGER.info("File \"{}\" successfully saved at \"{}\"", fileName, filePath);
        } catch (IOException e) {
            this.errorString = mBaseContext.getResources().getString(R.string.err_ioexception);
            LOGGER.error(e.toString());
            LOGGER.debug(Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_LOAD_FILE && resultCode == Activity.RESULT_OK) {
            final String filePath = data.getData().getPath();
            LOGGER.debug("Selected path: {}", filePath);
            this.filePath = filePath;
            doExport();
        }
    }
}