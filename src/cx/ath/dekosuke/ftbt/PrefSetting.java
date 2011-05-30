package cx.ath.dekosuke.ftbt;

import android.os.Bundle;

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
			DirectorySelectDialogPreference cachedir = (DirectorySelectDialogPreference) this
					.findPreference(getString(R.string.cachedirsummary));
			cachedir.setSummary(R.string.cachedirsummary);
			// リスナーを設定する
			cachedir.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference preference,
						Object newValue) {
					return CacheDirChange(preference, newValue);
				}
			});
			DirectorySelectDialogPreference savedir = (DirectorySelectDialogPreference) this
					.findPreference(getString(R.string.savedirsummary));
			cachedir.setSummary(R.string.savedirsummary);
			// リスナーを設定する
			cachedir.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference preference,
						Object newValue) {
					return SaveDirChange(preference, newValue);
				}
			});
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

	private boolean CacheDirChange(Preference preference, Object newValue) {
		String input = newValue.toString();
		try {
			if (input != null ) {
				preference.setSummary(R.string.cachedirsummary);
				SDCard.setCacheDir(this);
				return true;
			} else {
			}
		} catch (Exception e) {

		}
		// ディレクトリの作成ができるか確認(作成できない場合、警告を出しデフォルトに戻す)
		return false;
	}

	private boolean SaveDirChange(Preference preference, Object newValue) {
		String input = newValue.toString();
		try {
			if (input != null) {
				preference.setSummary(R.string.savedirsummary);
				SDCard.setSaveDir(this);
				return true;
			} else {
			}
		} catch (Exception e) {

		}
		// ディレクトリの作成ができるか確認(作成できない場合、警告を出しデフォルトに戻す)
		return false;
	}
	
	private String getDirFooter(){
		return "(推奨は"+SDCard.getBaseDir()+"です)";
	}
	
}
