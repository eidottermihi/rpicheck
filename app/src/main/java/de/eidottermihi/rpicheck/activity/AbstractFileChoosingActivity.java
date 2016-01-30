package de.eidottermihi.rpicheck.activity;

import android.content.Intent;
import android.os.Environment;

import com.nononsenseapps.filepicker.FilePickerActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import io.freefair.android.injection.app.InjectionAppCompatActivity;


/**
 * Superclass for activites that start a FilePicker activity.
 *
 * @author Michael
 */
public abstract class AbstractFileChoosingActivity extends InjectionAppCompatActivity {

    public static final int REQUEST_CODE_LOAD_FILE = 0;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFileChoosingActivity.class);

    /**
     * Sends an Intent to FilePickerActivity.
     */
    public final void startFileChooser() {
        LOGGER.debug("Sending Intent to open FilePicker Activity.");
        final Intent i = new Intent(this, FilePickerActivity.class);
        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

        startActivityForResult(i, REQUEST_CODE_LOAD_FILE);
    }


    public final String getFilenameFromPath(String filePath) {
        return new File(filePath).getName();
    }
}
