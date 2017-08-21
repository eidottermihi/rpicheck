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
package de.eidottermihi.rpicheck.activity.helper;

import android.database.Cursor;

import java.util.Date;

import de.eidottermihi.rpicheck.db.CommandBean;
import de.eidottermihi.rpicheck.db.RaspberryDeviceBean;

public class CursorHelper {

    public static CommandBean readCommand(Cursor c) {
        final CommandBean command = new CommandBean();
        command.setId(c.getLong(0));
        command.setName(c.getString(1));
        command.setCommand(c.getString(2));
        command.setShowOutput(c.getInt(3) == 1 ? true : false);
        return command;
    }

    public static RaspberryDeviceBean readDevice(Cursor cursor) {
        final RaspberryDeviceBean device = new RaspberryDeviceBean();
        device.setId(cursor.getInt(0));
        device.setName(cursor.getString(1));
        device.setDescription(cursor.getString(2));
        device.setHost(cursor.getString(3));
        device.setUser(cursor.getString(4));
        device.setPass(cursor.getString(5));
        device.setSudoPass(cursor.getString(6));
        device.setPort(cursor.getInt(7));
        device.setCreatedAt(new Date(cursor.getLong(8)));
        device.setModifiedAt(new Date(cursor.getLong(9)));
        device.setSerial(cursor.getString(10));
        device.setAuthMethod(cursor.getString(11));
        device.setKeyfilePath(cursor.getString(12));
        device.setKeyfilePass(cursor.getString(13));
        return device;
    }

}
