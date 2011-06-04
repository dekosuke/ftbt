package cx.ath.dekosuke.ftbt;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

//設定項目外のデータを取得するマネージャ
public class StateMan {
	private final static String PREFNAME = "ftbtpref";
	private final static String SORTKEY = "catalogSort";

	// 現在のソート状態の取得
	// 0->引数なし 1-4 -> sort=1-4
	public static int getSortParam(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREFNAME,
				Context.MODE_PRIVATE);
		int sorttype = pref.getInt(SORTKEY, 0);
		return sorttype;
	}

	public static void setSortParam(Context context, int i) {
		SharedPreferences pref = context.getSharedPreferences(PREFNAME,
				Context.MODE_PRIVATE);
		Editor e = pref.edit();
		e.putInt(SORTKEY, i);
		e.commit();
	}
	
	public static int getFontSizeSetting(Context context){
		int fontSizeSetting = 0 ;
		try {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(context);
			fontSizeSetting = Integer.parseInt(preferences.getString("fontSizeSetting","0"));
		} catch (Exception e) {
			FLog.d("message", e);
			return 0;
		}
		return fontSizeSetting;
	}
	
	public double getMainFontSize(Context context){
		int fontSizeSetting = getFontSizeSetting(context);
		return 16.0d+fontSizeSetting;
	}
	
	public double getDescFontSize(Context context){
		int fontSizeSetting = getFontSizeSetting(context);
		return 14.0d+fontSizeSetting;

	}
}
