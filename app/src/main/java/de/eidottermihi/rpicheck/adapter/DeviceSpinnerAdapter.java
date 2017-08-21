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
package de.eidottermihi.rpicheck.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.eidottermihi.raspicheck.R;
import de.eidottermihi.rpicheck.activity.helper.CursorHelper;
import de.eidottermihi.rpicheck.db.RaspberryDeviceBean;

/**
 * Adapter for cursor-backed Device Spinners.
 */
public class DeviceSpinnerAdapter extends ResourceCursorAdapter {
    private final LayoutInflater inflater;

    /**
     * @param context the Context
     * @param c full device cursor
     * @param alwaysWithUserHost true, if user@host should always be displayed (not only in dropdown view)
     */
    public DeviceSpinnerAdapter(Context context, Cursor c, boolean alwaysWithUserHost) {
        super(context, alwaysWithUserHost ? R.layout.device_row_dropdown : R.layout.device_row,
                c, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        this.inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        RaspberryDeviceBean device = CursorHelper.readDevice(cursor);
        TextView name = (TextView) view.findViewById(R.id.deviceRowName);
        name.setText(device.getName());
        TextView userHost = (TextView) view.findViewById(R.id.deviceRowUserHost);
        if (userHost != null) {
            userHost.setText(String.format("%s@%s", device.getUser(), device.getHost()));
        }
    }

    @Override
    public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.device_row_dropdown, parent, false);
    }


}
