package cx.ath.dekosuke.ftbt;


import java.io.File;
 
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

import cx.ath.dekosuke.ftbt.DirectorySelectDialog.onDirectoryListDialogListener;
 
public class DirectorySelectDialogPreference extends DialogPreference implements
        DirectorySelectDialog.onDirectoryListDialogListener {
 
    public DirectorySelectDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
 
    @Override
    protected void onBindView(View view) {
        SharedPreferences pref = getSharedPreferences();
        //String summary = R.string.cachedirsummary;//SDCard.getBaseDir();
        //setSummary(summary);
        super.onBindView(view);
    }
 
    @Override
    protected void onClick() {
        File externalStorage = Environment.getExternalStorageDirectory();//Environment.getExternalStorageDirectory();
        DirectorySelectDialog dlg = new DirectorySelectDialog(getContext());
        dlg.setOnFileListDialogListener((onDirectoryListDialogListener) this);
        dlg.show(externalStorage.getAbsolutePath(), externalStorage.getPath());
    }
 
    public void onClickFileList(String path) {
        if (null != path) {
            SharedPreferences.Editor editor = getEditor();
            editor.putString(getKey(), path);
            editor.commit();
            notifyChanged();
        }
    }
}