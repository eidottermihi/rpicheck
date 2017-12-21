package de.eidottermihi.rpicheck.activity.helper;

import android.support.annotation.Nullable;

import com.nononsenseapps.filepicker.AbstractFilePickerActivity;
import com.nononsenseapps.filepicker.AbstractFilePickerFragment;
import com.nononsenseapps.filepicker.FilePickerFragment;

import java.io.File;

/**
 * Extends {@link AbstractFilePickerActivity} to show hidden folders (like .ssh).
 *
 * @author eidottermihi
 */
public class RaspiFilePickerActivity extends AbstractFilePickerActivity<File> {

    public RaspiFilePickerActivity() {
        super();
    }

    @Override
    protected AbstractFilePickerFragment<File> getFragment(@Nullable String startPath, int mode, boolean allowMultiple, boolean allowCreateDir, boolean allowExistingFile, boolean singleClick) {
        final FilePickerFragment filePickerFragment = new FilePickerFragment();
        filePickerFragment.setArgs(startPath, mode, allowMultiple, allowCreateDir, allowExistingFile, singleClick);
        filePickerFragment.showHiddenItems(true);
        return filePickerFragment;
    }


}
