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

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.eidottermihi.raspicheck.R;
import de.eidottermihi.rpicheck.activity.helper.CursorHelper;
import de.eidottermihi.rpicheck.db.CommandBean;

/**
 * Cursor adapter for Commands.
 */
public class CommandAdapter extends CursorAdapter {

    private LayoutInflater inflater;

    public CommandAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        this.inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final TextView name = (TextView) view.findViewById(R.id.commandRowName);
        final TextView command = (TextView) view.findViewById(R.id.commandRowCommand);
        final CommandBean bean = CursorHelper.readCommand(cursor);
        name.setText(bean.getName());
        command.setText(bean.getCommand());
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View v = inflater.inflate(R.layout.command_row, parent, false);
        return v;
    }

}
