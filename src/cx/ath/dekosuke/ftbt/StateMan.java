package cx.ath.dekosuke.ftbt;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

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
}
