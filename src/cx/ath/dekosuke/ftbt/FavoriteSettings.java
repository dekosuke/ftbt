package cx.ath.dekosuke.ftbt;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

//お気に入りスレッドの設定
public class FavoriteSettings extends PreferenceActivity { // PreferenceActivityの継承
	private static final String OPT_FAVORITES = "favorites";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.favoritesettings);
	}

	public static ArrayList<FutabaBBS> getFavorites(Context context) { // ID用ゲッタの定義
		String serializeStr = PreferenceManager.getDefaultSharedPreferences(
				context).getString(OPT_FAVORITES, "");
		String[] bbss_array = serializeStr.split(" ");
		ArrayList<FutabaBBS> bbss = new ArrayList<FutabaBBS>();
		for (int i =  0; i < bbss_array.length; i++) {
			bbss.add(new FutabaBBS(bbss_array[i]));
		}
		return bbss;
	}

	public static void setFavorites(Context context,
			ArrayList<FutabaBBS> futabaBBSs) { // PASSWORD用ゲッタの定義
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor ed = sp.edit();
		String serializeStr = "";
		Iterator iterator = futabaBBSs.iterator();
		// あまり効率のよさそうではない直列化
		while (iterator.hasNext()) {
			serializeStr += iterator.next().toString();
			if (iterator.hasNext()) {
				serializeStr += " ";
			}
		}
		ed.putString("account", serializeStr); // ここにシリアライズした中身を入れる
		ed.commit();
	}
}