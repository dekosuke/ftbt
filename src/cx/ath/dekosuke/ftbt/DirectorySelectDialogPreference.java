package cx.ath.dekosuke.ftbt;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import cx.ath.dekosuke.ftbt.DirectorySelectDialog.onDirectoryListDialogListener;

public class DirectorySelectDialogPreference extends DialogPreference implements
		DirectorySelectDialog.onDirectoryListDialogListener {

	DirectorySelectDialog dlg;
	onDirectoryListDialogListener listener;

	public DirectorySelectDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onBindView(View view) {
		SharedPreferences pref = getSharedPreferences();
		// String summary = R.string.cachedirsummary;//SDCard.getBaseDir();
		// setSummary(summary);
		super.onBindView(view);
	}

	@Override
	protected void onClick() {
		File externalStorage = new File("/");// Environment.getExternalStorageDirectory();
		dlg = new DirectorySelectDialog(getContext());
		dlg.setOnFileListDialogListener((onDirectoryListDialogListener) this);
		dlg.show(externalStorage.getAbsolutePath(), externalStorage.getPath());
	}

	public void addListener(onDirectoryListDialogListener listener) {
		this.listener = listener;
		// dlg.setOnFileListDialogListener((onDirectoryListDialogListener)
		// listener);
	}

	public void changePreference(String path) {

		SharedPreferences.Editor editor = getEditor();
		editor.putString(getKey(), path);
		FLog.d("getkey=" + getKey() + " path=" + path);
		editor.commit();
		notifyChanged();
		Toast.makeText(getContext(), "ディレクトリ\"" + path + "\"に設定しました",
				Toast.LENGTH_SHORT).show();
		if (this.listener != null) {
			this.listener.onClickFileList(path);
		}

	}

	public void onClickFileList(String path) {
		if (null != path) {
			if (!SDCard.isUsableDirectory(new File(path))) {
				Toast.makeText(getContext(),
						"ディレクトリ\"" + path + "\"は書き込み権限がありません。\n再選択をお願いします。",
						Toast.LENGTH_LONG).show();
				FLog.d("unable to write");
				return;
			} else {
				final String path_f = path;
				// 確認ダイアログ
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getContext());
				builder.setMessage(
						"本当にディレクトリを変更してよろしいですか？\n"
								+ "選択したディレクトリは「ふたばと」専用の（他のアプリケーションと競合しない）ディレクトリですか？\n"
								+ "また、ディレクトリの変更は非推奨です。とくに理由がない場合は設定変更しないことをお勧めします")
						.setCancelable(true)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										changePreference(path_f);
									}
								})
						.setNegativeButton("キャンセル",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
				builder.create().show();
			}
		}
	}
}