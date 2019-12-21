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
    @InjectView(R.id.command_timeout_editText)
    private EditText timeoutEditText;

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
                    timeoutEditText.setText(commandBean.getTimeout() + "");
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
        if (validation.validateNewCmdData(this, commandEditText, timeoutEditText)) {
            String name = nameEditText.getText().toString();
            String cmd = commandEditText.getText().toString().trim();
            Integer timeout = Integer.parseInt(timeoutEditText.getText().toString().trim());
            if (Strings.isNullOrEmpty(name)) {
                name = cmd;
            }
            final CommandBean bean = new CommandBean();
            bean.setName(name);
            bean.setCommand(cmd);
            bean.setShowOutput(true);
            bean.setTimeout(timeout);
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
