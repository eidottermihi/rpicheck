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
        command.setTimeout(c.getInt(4));
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
