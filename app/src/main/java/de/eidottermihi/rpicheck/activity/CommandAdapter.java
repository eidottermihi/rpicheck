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
