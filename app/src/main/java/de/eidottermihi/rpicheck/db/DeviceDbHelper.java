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
package de.eidottermihi.rpicheck.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

import de.eidottermihi.rpicheck.activity.NewRaspiAuthActivity;
import de.eidottermihi.rpicheck.activity.helper.CursorHelper;

public class DeviceDbHelper extends SQLiteOpenHelper {
    /**
     * Current database version.
     */
    private static final int DATABASE_VERSION = 9;
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DeviceDbHelper.class);
    private static final String DATABASE_NAME = "RASPIQUERY";
    private static final String DEVICES_TABLE_NAME = "DEVICES";
    private static final String QUERIES_TABLE_NAME = "QUERIES";
    private static final String COMMANDS_TABLE_NAME = "COMMANDS";
    private static final String COLUMN_ID = BaseColumns._ID;
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_HOST = "host";
    private static final String COLUMN_USER = "user";
    private static final String COLUMN_PASSWD = "passwd";
    private static final String COLUMN_SSHPORT = "ssh_port";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_MODIFIED_AT = "modified_at";
    private static final String COLUMN_SUDOPW = "sudo_passwd";
    private static final String COLUMN_AUTH_METHOD = "auth_method";
    private static final String COLUMN_KEYFILE_PATH = "keyfile_path";
    private static final String COLUMN_KEYFILE_PASS = "keyfile_pass";
    private static final String COLUMN_SERIAL = "serial";
    private static final String DEVICE_TABLE_CREATE = "CREATE TABLE "
            + DEVICES_TABLE_NAME + " (" + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + COLUMN_NAME
            + " TEXT ," + COLUMN_DESCRIPTION + " TEXT, " + COLUMN_HOST
            + " TEXT, " + COLUMN_USER + " TEXT," + COLUMN_PASSWD + " TEXT, "
            + COLUMN_SUDOPW + " TEXT, " + COLUMN_SSHPORT + " INTEGER, "
            + COLUMN_CREATED_AT + " INTEGER, " + COLUMN_MODIFIED_AT
            + " INTEGER, " + COLUMN_SERIAL + " TEXT, " + COLUMN_AUTH_METHOD
            + " TEXT NOT NULL, " + COLUMN_KEYFILE_PATH + " TEXT, "
            + COLUMN_KEYFILE_PASS + " TEXT)";
    private static final String COLUMN_QUERY_TIME = "time";
    private static final String COLUMN_QUERY_STATUS = "status";
    private static final String COLUMN_QUERY_DEVICE_ID = "device_id";
    private static final String COLUMN_QUERY_CORE_TEMP = "core_temp";
    private static final String COLUMN_QUERY_CORE_FREQ = "core_freq";
    private static final String COLUMN_QUERY_CORE_VOLT = "core_volt";
    private static final String COLUMN_QUERY_ARM_FREQ = "arm_freq";
    private static final String COLUMN_QUERY_STARTUP_TIME = "startup_time";
    private static final String COLUMN_QUERY_IP_ADDR = "ip";
    private static final String COLUMN_QUERY_UPTIME_FULL = "uptime_full";
    private static final String COLUMN_QUERY_UPTIME_IDLE = "uptime_idle";
    private static final String COLUMN_QUERY_MEM_TOTAL = "mem_total";
    private static final String COLUMN_QUERY_MEM_FREE = "mem_free";
    private static final String COLUMN_QUERY_DISTRIBUTION = "distribution";
    private static final String QUERY_TABLE_CREATE = "CREATE TABLE "
            + QUERIES_TABLE_NAME + " (" + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
            + COLUMN_QUERY_DEVICE_ID + " INTEGER NOT NULL, "
            + COLUMN_QUERY_TIME + " INTEGER, " + COLUMN_QUERY_STATUS
            + " TEXT, " + COLUMN_QUERY_CORE_TEMP + " REAL, "
            + COLUMN_QUERY_CORE_FREQ + " INTEGER, " + COLUMN_QUERY_CORE_VOLT
            + " REAL, " + COLUMN_QUERY_ARM_FREQ + " INTEGER, "
            + COLUMN_QUERY_STARTUP_TIME + " INTEGER, " + COLUMN_QUERY_IP_ADDR
            + " TEXT, " + COLUMN_QUERY_UPTIME_FULL + " INTEGER, "
            + COLUMN_QUERY_UPTIME_IDLE + " INTEGER, " + COLUMN_QUERY_MEM_TOTAL
            + " INTEGER, " + COLUMN_QUERY_MEM_FREE + " INTEGER, "
            + COLUMN_QUERY_DISTRIBUTION + " TEXT)";
    private static final String COLUMN_CMD_COMMAND = "command";
    private static final String COLUMN_CMD_FLAGOUTPUT = "flag_output";
    private static final String COLUMN_CMD_NAME = "name";
    private static final String COMMAND_TABLE_CREATE = "CREATE TABLE "
            + COMMANDS_TABLE_NAME + " (" + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + COLUMN_CMD_NAME
            + " TEXT NOT NULL, " + COLUMN_CMD_COMMAND + " TEXT NOT NULL, "
            + COLUMN_CMD_FLAGOUTPUT + " INTEGER NOT NULL)";

    public DeviceDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LOGGER.info("Executing first-time database setup.");
        db.execSQL(DEVICE_TABLE_CREATE);
        db.execSQL(QUERY_TABLE_CREATE);
        db.execSQL(COMMAND_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LOGGER.info("Upgrading database from version " + oldVersion + " to "
                + newVersion);
        boolean migrationAvailable = false;
        if (oldVersion == 6 && newVersion == 7) {
            upgradeV6ToV7(db);
            migrationAvailable = true;
        }
        if (oldVersion == 6 && newVersion == 8) {
            upgradeV6ToV7(db);
            upgradeV7ToV8(db);
            migrationAvailable = true;
        }
        if (oldVersion == 7 && newVersion == 8) {
            upgradeV7ToV8(db);
            migrationAvailable = true;
        }
        if (oldVersion == 6 && newVersion == 9) {
            upgradeV6ToV7(db);
            upgradeV7ToV8(db);
            upgradeV8ToV9(db);
            migrationAvailable = true;
        }
        if (oldVersion == 7 && newVersion == 9) {
            upgradeV7ToV8(db);
            upgradeV8ToV9(db);
            migrationAvailable = true;
        }
        if (oldVersion == 8 && newVersion == 9) {
            upgradeV8ToV9(db);
            migrationAvailable = true;
        }
        if (!migrationAvailable) {
            // dropping all tables (data will be lost *sad* )
            LOGGER.warn("No migration for database upgrade from version {} to version {} available. Setting up whole new database, all current data will be lost, sorry!");
            db.execSQL("DROP TABLE " + DEVICES_TABLE_NAME);
            db.execSQL("DROP TABLE " + QUERIES_TABLE_NAME);
            // run initial setup
            this.onCreate(db);
        }
    }

    private void upgradeV8ToV9(SQLiteDatabase db) {
        LOGGER.info("Upgrading database from version 8 to version 9: adding commands table.");
        db.execSQL(COMMAND_TABLE_CREATE);
    }

    private void upgradeV7ToV8(SQLiteDatabase db) {
        LOGGER.info("Upgrading database from version 7 to version 8: adding auth method, key file path and key file passphrase to device table.");
        db.execSQL("ALTER TABLE " + DEVICES_TABLE_NAME + " ADD COLUMN "
                + COLUMN_AUTH_METHOD + " TEXT NOT NULL DEFAULT '"
                + RaspberryDeviceBean.AUTH_PASSWORD + "'");
        db.execSQL("ALTER TABLE " + DEVICES_TABLE_NAME + " ADD COLUMN "
                + COLUMN_KEYFILE_PATH + " TEXT");
        db.execSQL("ALTER TABLE " + DEVICES_TABLE_NAME + " ADD COLUMN "
                + COLUMN_KEYFILE_PASS + " TEXT");
    }

    private void upgradeV6ToV7(SQLiteDatabase db) {
        LOGGER.debug("Upgrading database from version 6 to version 7: adding sudo password column to device table.");
        // adding sudo pw field in device table
        db.execSQL("ALTER TABLE " + DEVICES_TABLE_NAME + " ADD COLUMN "
                + COLUMN_SUDOPW + " TEXT");
    }

    /**
     * @return a full cursor for device table
     */
    public Cursor getFullDeviceCursor() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.query(DEVICES_TABLE_NAME, null, null, null, null, null, null);
    }

    /**
     * @return a full cursor for commands table
     */
    public Cursor getFullCommandCursor() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db
                .query(COMMANDS_TABLE_NAME, null, null, null, null, null, null);
    }

    /**
     * Creates a new device in the device table.
     *
     * @param name        name of device
     * @param host        hostname
     * @param user        username (ssh)
     * @param pass        password (ssh)
     * @param sshPort     SSH port
     * @param description device description
     * @return a {@link RaspberryDeviceBean}
     */
    public RaspberryDeviceBean create(String name, String host, String user,
                                      String pass, int sshPort, String description, String sudoPass,
                                      String authMethod, String keyFilePath, String keyFilePass) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // _id AUTOINCREMENT
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_HOST, host);
        values.put(COLUMN_USER, user);
        values.put(COLUMN_SSHPORT, sshPort);
        values.put(COLUMN_SUDOPW, sudoPass);
        values.put(COLUMN_AUTH_METHOD, authMethod);
        if (authMethod.equals(RaspberryDeviceBean.AUTH_PASSWORD)) {
            // insert only ssh password
            values.put(COLUMN_PASSWD, pass);
        } else if (authMethod
                .equals(RaspberryDeviceBean.AUTH_PUBLIC_KEY)) {
            // insert only location of keyfile
            values.put(COLUMN_KEYFILE_PATH, keyFilePath);
        } else if (authMethod
                .equals(RaspberryDeviceBean.AUTH_PUBLIC_KEY_WITH_PASSWORD)) {
            // insert keyfile path and password
            values.put(COLUMN_KEYFILE_PATH, keyFilePath);
            values.put(COLUMN_KEYFILE_PASS, keyFilePass);
        }

        // created: current timestamp
        Long timestamp = Calendar.getInstance().getTimeInMillis();
        values.put(COLUMN_CREATED_AT, timestamp);
        values.put(COLUMN_MODIFIED_AT, timestamp);
        long id = db.insert(DEVICES_TABLE_NAME, null, values);
        LOGGER.debug("Inserted new device[id={}].", id);
        return read(id);
    }

    /**
     * Gets a device from the device table.
     *
     * @param id ID of the device
     * @return a {@link RaspberryDeviceBean}
     */
    public RaspberryDeviceBean read(long id) {
        LOGGER.trace("Reading device with id = " + id);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(DEVICES_TABLE_NAME, new String[]{COLUMN_ID, COLUMN_NAME,
                        COLUMN_DESCRIPTION, COLUMN_HOST, COLUMN_USER, COLUMN_PASSWD,
                        COLUMN_SUDOPW, COLUMN_SSHPORT, COLUMN_CREATED_AT, COLUMN_MODIFIED_AT,
                        COLUMN_SERIAL, COLUMN_AUTH_METHOD, COLUMN_KEYFILE_PATH,
                        COLUMN_KEYFILE_PASS},
                COLUMN_ID + "=" + id, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            final RaspberryDeviceBean bean = CursorHelper.readDevice(cursor);
            cursor.close();
            db.close();
            return bean;
        } else {
            cursor.close();
            db.close();
            LOGGER.warn("Device with id = {} is not in db.", id);
            return null;
        }
    }

    /**
     * Deletes a device and all device query data of this device.
     *
     * @param id the ID of the Device
     */
    public void delete(long id) {
        final String idString = id + "";
        SQLiteDatabase db = this.getWritableDatabase();
        int queryRows = db.delete(QUERIES_TABLE_NAME, COLUMN_QUERY_DEVICE_ID
                + " = ?", new String[]{idString});
        int deviceRows = db.delete(DEVICES_TABLE_NAME, COLUMN_ID + " = ?",
                new String[]{idString});
        LOGGER.info("Delete device with id=" + idString + ": " + deviceRows
                + "device row(s) deleted, " + queryRows
                + " query data row(s) deleted.");
        db.close();
    }

    /**
     * Updates a device.
     *
     * @param device the device to update
     * @return the updated device
     */
    public RaspberryDeviceBean update(RaspberryDeviceBean device) {
        LOGGER.trace("Updating device with id=" + device.getId() + "...");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, device.getName());
        values.put(COLUMN_HOST, device.getHost());
        values.put(COLUMN_USER, device.getUser());
        values.put(COLUMN_PASSWD, device.getPass());
        values.put(COLUMN_SSHPORT, device.getPort());
        values.put(COLUMN_DESCRIPTION, device.getDescription());
        values.put(COLUMN_SERIAL, device.getSerial());
        values.put(COLUMN_SUDOPW, device.getSudoPass());
        values.put(COLUMN_AUTH_METHOD, device.getAuthMethod());
        values.put(COLUMN_KEYFILE_PATH, device.getKeyfilePath());
        values.put(COLUMN_KEYFILE_PASS, device.getKeyfilePass());

        // modified: current timestamp
        Long timestamp = Calendar.getInstance().getTimeInMillis();
        values.put(COLUMN_MODIFIED_AT, timestamp);
        int rowsUpdate = db.update(DEVICES_TABLE_NAME, values, COLUMN_ID
                + " = ?", new String[]{device.getId() + ""});
        db.close();
        LOGGER.trace(rowsUpdate + " row afflicted from update.");
        return read(device.getId());
    }

    public CommandBean create(CommandBean command) {
        LOGGER.info("Writing new command[name={}] to database.",
                command.getName());
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // _id AUTOINCREMENT
        values.put(COLUMN_CMD_NAME, command.getName());
        values.put(COLUMN_CMD_COMMAND, command.getCommand());
        values.put(COLUMN_CMD_FLAGOUTPUT, command.isShowOutput());
        long id = db.insert(COMMANDS_TABLE_NAME, null, values);
        db.close();
        return readCommand(id);
    }

    public CommandBean readCommand(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        CommandBean bean = null;
        Cursor cursor = db.query(COMMANDS_TABLE_NAME, new String[]{COLUMN_ID,
                        COLUMN_CMD_NAME, COLUMN_CMD_COMMAND, COLUMN_CMD_FLAGOUTPUT},
                COLUMN_ID + "=" + id, null, null, null, COLUMN_ID);
        if (cursor.moveToFirst()) {
            bean = CursorHelper.readCommand(cursor);
        } else {
            LOGGER.info("No command with id={} found.", id);
        }
        cursor.close();
        db.close();
        return bean;
    }

    public CommandBean updateCommand(CommandBean command) {
        LOGGER.debug("Updating command id {}.", command.getId());
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CMD_NAME, command.getName());
        values.put(COLUMN_CMD_COMMAND, command.getCommand());
        values.put(COLUMN_CMD_FLAGOUTPUT, command.isShowOutput());
        int rowsUpdate = db.update(COMMANDS_TABLE_NAME, values, COLUMN_ID
                + " = ?", new String[]{command.getId() + ""});
        db.close();
        LOGGER.debug(rowsUpdate + " row afflicted from update.");
        return readCommand(command.getId());
    }

    public boolean deleteCommand(long id) {
        final String idString = id + "";
        SQLiteDatabase db = this.getWritableDatabase();
        int deviceRows = db.delete(COMMANDS_TABLE_NAME, COLUMN_ID + " = ?",
                new String[]{idString});
        LOGGER.info("Deleted command with id=" + idString + ": " + deviceRows
                + "command row(s) deleted.");
        db.close();
        return deviceRows > 0;

    }

    /**
     * Deletes all device data and query data from the database.
     */
    public void wipeAllData() {
        LOGGER.info("Wiping all device information....");
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsQueries = db.delete(QUERIES_TABLE_NAME, "1", null);
        LOGGER.info("Deleted " + rowsQueries
                + " device query data from database.");
        int rowsDevices = db.delete(DEVICES_TABLE_NAME, "1", null);
        LOGGER.info("Deleted " + rowsDevices + " devices from database.");
        int commands = db.delete(COMMANDS_TABLE_NAME, null, null);
        LOGGER.info("Deleted " + commands + " commands from database.");
        LOGGER.info("Wipe successful.");
        db.close();
    }

}
