package cx.ath.dekosuke.ftbt;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

//http://blog.flaxia.net/2011/03/27/android%E3%81%A7%E3%83%87%E3%82%A3%E3%83%AC%E3%82%AF%E3%83%88%E3%83%AA%E9%81%B8%E6%8A%9E%E3%83%97%E3%83%AA%E3%83%95%E3%82%A1%E3%83%AC%E3%83%B3%E3%82%B9%E3%82%92%E4%BD%BF%E3%81%86/
public class DirectorySelectDialog extends Activity implements
		DialogInterface.OnClickListener {
	private Context mContext;
	private ArrayList<File> mDirectoryList;
	private onDirectoryListDialogListener mListenner;

	public DirectorySelectDialog(Context context) {
		mContext = context;
		mDirectoryList = new ArrayList<File>();
	}

	public void onClick(DialogInterface dialog, int which) {
		if ((null != mDirectoryList) && (null != mListenner)) {
			File file = mDirectoryList.get(which);
			show(file.getAbsolutePath(), file.getPath());
		}
	}

	public void show(final String path, String title) {
		try {
			File[] mDirectories = new File(path).listFiles();
			if (null == mDirectories && null != mListenner) {
				mListenner.onClickFileList(null);
			} else {
				mDirectoryList.clear();
				ArrayList<String> viewList = new ArrayList<String>();
				for (File file : mDirectories) {
					if (file.isDirectory()) {
						viewList.add(file.getName() + "/");
						mDirectoryList.add(file);
					}
				}

				// ダイアログ表示
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						mContext);
				alertDialogBuilder.setTitle(title);
				alertDialogBuilder.setItems(viewList.toArray(new String[0]),
						this);
				// 自身のContextではgetStringが失敗する
				alertDialogBuilder.setPositiveButton(
						mContext.getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								System.out.println(path);
								mListenner.onClickFileList(path);
							}
						});
				// 自身のContextではgetStringが失敗する
				alertDialogBuilder.setNegativeButton(
						mContext.getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								mListenner.onClickFileList(null);
							}
						});
				alertDialogBuilder.show();
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	public void setOnFileListDialogListener(
			onDirectoryListDialogListener listener) {
		mListenner = listener;
	}

	public interface onDirectoryListDialogListener {
		public void onClickFileList(String path);
	}
}
