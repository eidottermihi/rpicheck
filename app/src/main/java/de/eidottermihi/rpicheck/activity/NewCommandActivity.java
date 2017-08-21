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

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.eidottermihi.raspicheck.R;
import de.eidottermihi.rpicheck.activity.helper.Validation;
import de.eidottermihi.rpicheck.db.CommandBean;
import de.eidottermihi.rpicheck.db.DeviceDbHelper;
import io.freefair.android.injection.annotation.InjectView;
import io.freefair.android.injection.annotation.XmlLayout;
import io.freefair.android.injection.annotation.XmlMenu;
import io.freefair.android.injection.app.InjectionAppCompatActivity;

@XmlLayout(R.layout.activity_command_new)
@XmlMenu(R.menu.activity_command_new)
public class NewCommandActivity extends InjectionAppCompatActivity {
    private static final Logger LOGGER = LoggerFactory.getLogger(NewCommandActivity.class);

    // Requestcode for new command
    public static final int REQUEST_NEW = 0;
    // Requestcode for edit command
    public static final int REQUEST_EDIT = 1;
    // Key for CommandBean when edit is requested
    public static final String CMD_KEY_EDIT = "cmdId";

    @InjectView(R.id.new_cmd_name_editText)
    private EditText nameEditText;
    @InjectView(R.id.new_cmd_command_editText)
    private EditText commandEditText;

    DeviceDbHelper db;

    Validation validation = new Validation();

    Long cmdId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        db = new DeviceDbHelper(this);

        if (getIntent().getExtras() != null && getIntent().getExtras().getLong(CMD_KEY_EDIT, -1L) != -1) {
            // edit existing command!
            cmdId = getIntent().getExtras().getLong(CMD_KEY_EDIT);
            getSupportActionBar().setTitle(getString(R.string.activity_title_edit_command));
            new AsyncTask<Void, Void, CommandBean>() {
                @Override
                protected CommandBean doInBackground(Void... params) {
                    return db.readCommand(cmdId);
                }

                @Override
                protected void onPostExecute(CommandBean commandBean) {
                    nameEditText.setText(commandBean.getName());
                    commandEditText.setText(commandBean.getCommand());
                }
            }.execute();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                LOGGER.info("Cancelling new/edit command activity.");
                this.setResult(RESULT_CANCELED);
                this.finish();
                return true;
            case R.id.menu_save:
                saveCommand();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveCommand() {
        if (validation.validateNewCmdData(this, commandEditText)) {
            String name = nameEditText.getText().toString();
            String cmd = commandEditText.getText().toString().trim();
            if (Strings.isNullOrEmpty(name)) {
                name = cmd;
            }
            final CommandBean bean = new CommandBean();
            bean.setName(name);
            bean.setCommand(cmd);
            bean.setShowOutput(true);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    if (cmdId == null) {
                        db.create(bean);
                    } else {
                        bean.setId(cmdId);
                        db.updateCommand(bean);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void r) {
                    NewCommandActivity.this.setResult(RESULT_OK);
                    NewCommandActivity.this.finish();
                }
            }.execute();
        }
    }

}
