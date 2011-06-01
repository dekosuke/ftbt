package cx.ath.dekosuke.ftbt;

import cx.ath.dekosuke.ftbt.DirectorySelectDialog.onDirectoryListDialogListener;
import android.os.Bundle;

import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;
import android.preference.Preference.OnPreferenceChangeListener;

public class PrefSetting extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefsetting);

		try {
			EditTextPreference etp = (EditTextPreference) this
					.findPreference(getString(R.string.cachesize));
			// リスナーを設定する
			etp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference preference,
						Object newValue) {
					return CacheSizeChange(preference, newValue);
				}
			});
			EditTextPreference etp_hist = (EditTextPreference) this
					.findPreference(getString(R.string.historynum));
			// リスナーを設定する
			etp_hist.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference preference,
						Object newValue) {
					return HistorySizeChange(preference, newValue);
				}
			});
			EditTextPreference etp_delkey = (EditTextPreference) this
					.findPreference(getString(R.string.deletekey));
			// リスナーを設定する
			etp_delkey
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
						public boolean onPreferenceChange(
								Preference preference, Object newValue) {
							return DeleteKeyChange(preference, newValue);
						}
					});
			// リスナーを設定する
			/*
			 * cachedir.setOnFileListDialogListener(new
			 * onDirectoryListDialogListener() { public void
			 * onClickFileList(String path){ return CacheDirChange(cachedir,
			 * path); } });
			 */
			final DirectorySelectDialogPreference savedir = (DirectorySelectDialogPreference) this
					.findPreference("saveDir");
			// savedir.setSummary(R.string.savedirsummary);
			String saveSummaryFooter = "";
			if (SDCard.saveDir != null) {
				saveSummaryFooter += "。\n現在の設定:\"" + SDCard.saveDir + "\"";
			}
			savedir.setSummary(getString(R.string.savedirsummary)
					+ saveSummaryFooter);
			savedir.addListener(new onDirectoryListDialogListener() {
				public void onClickFileList(String path) {
					// SDCard.setSaveDir(activity);
					/*
					 * String saveSummaryFooter = ""; if(SDCard.saveDir !=
					 * null){
					 * saveSummaryFooter+="。\n現在の設定:\""+SDCard.saveDir+"\""; }
					 * savedir.setSummary(getString(R.string.savedirsummary)+
					 * saveSummaryFooter);
					 */
					savedir.setSummary(path);
				}
			});
			// リスナーを設定する
			/*
			 * savedir.setOnPreferenceChangeListener(new
			 * OnPreferenceChangeListener() { public boolean
			 * onPreferenceChange(Preference preference, Object newValue) {
			 * return SaveDirChange(preference, newValue); } });
			 */
			FLog.d("hoge");
			CheckBoxPreference innerCache = (CheckBoxPreference) this
					.findPreference("innerCache");
			innerCache.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				public boolean onPreferenceChange(
						Preference preference, Object newValue) {
					return InnerCacheChange(preference, newValue);
				}
			});
			//innerCache.setSummary("内部メモリをキャッシュに使用します。変更は次回起動時から有効になります\n（注：お気に入り・履歴もキャッシュにあるので消えます）");
		} catch (Exception e) {
			FLog.d("message", e);
		}
	}

	private boolean CacheSizeChange(Preference preference, Object newValue) {
		String input = newValue.toString();
		try {
			if (input != null && Integer.parseInt(input) > 0
					&& Integer.parseInt(input) > 4
					&& Integer.parseInt(input) <= 1000) {
				preference.setSummary(input);
				return true;
			} else {
				// nullまたは100以下はエラー
			}
		} catch (Exception e) {

		}
		Toast.makeText(this, "キャッシュサイズは5MBから1000MBにしてください", Toast.LENGTH_LONG)
				.show();
		return false;
	}

	private boolean HistorySizeChange(Preference preference, Object newValue) {
		String input = newValue.toString();
		try {
			if (input != null && Integer.parseInt(input) >= 0
					&& Integer.parseInt(input) <= 100) {
				preference.setSummary(input);
				return true;
			} else {
			}
		} catch (Exception e) {

		}
		Toast.makeText(this, "記憶する履歴は100件以下にしてください", Toast.LENGTH_LONG).show();
		return false;
	}

	private boolean DeleteKeyChange(Preference preference, Object newValue) {
		String input = newValue.toString();
		try {
			if (input != null && input.length() <= 8) {
				preference.setSummary(input);
				return true;
			} else {
			}
		} catch (Exception e) {

		}
		Toast.makeText(this, "パスワードは8文字以下にしてください", Toast.LENGTH_LONG).show();
		return false;
	}

	private boolean InnerCacheChange(Preference preference, Object newValue) {
		String input = newValue.toString();
		FLog.d("newvalue=" + newValue);
		try {
			if (input != null) {
				FLog.d("input=" + input);
				//SDCard.setCacheDir(this);
				//この時点ではまだキャッシュディレクトリ更新されてない
				SDCard.copyCacheSetting(this);
				return true;
			} else {
			}
		} catch (Exception e) {

		}
		return false;
	}

	private boolean SaveDirChange(Preference preference, Object newValue) {
		String input = newValue.toString();
		try {
			if (input != null) {
				// preference.setSummary(R.string.savedirsummary);
				SDCard.setSaveDir(this);
				return true;
			} else {
			}
		} catch (Exception e) {

		}
		// ディレクトリの作成ができるか確認(作成できない場合、警告を出しデフォルトに戻す)
		return false;
	}

	private String getDirFooter() {
		return "(推奨は" + SDCard.getBaseDir() + "です)";
	}

	@Override
	public void onDestroy() {
		FLog.d("PrefSetting::onDestroy()");

		super.onDestroy();

		// ユーザの指定したディレクトリ設定を読み込む
		try {
			SDCard.setCacheDir(this);
			SDCard.setSaveDir(this); // String saveDir = SDCard.getSaveDir(); }
		} catch (Exception e) {
			FLog.d("message", e);
		}
	}
}
