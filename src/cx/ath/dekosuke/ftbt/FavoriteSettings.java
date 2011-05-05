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
import java.io.IOException;
import android.util.Log;

//お気に入り板リストの設定
public class FavoriteSettings {
	private static final String OPT_FAVORITES = "favorites";

	public static ArrayList<FutabaBBSContents> getFavorites(Context context) 
        throws IOException {
    	ArrayList<FutabaBBSContents> bbss = new ArrayList<FutabaBBSContents>();
        try{
	    	if( SDCard.existSeriarized(OPT_FAVORITES)){
                bbss = (ArrayList<FutabaBBSContents>) SDCard.getSerialized(OPT_FAVORITES).readObject();
            }
        }catch(Exception e){
            Log.i("ftbt", "message", e);
        }
		return bbss;
	}

	public static void setFavorites(Context context,
			ArrayList<FutabaBBSContents> futabaBBSs) throws IOException { // PASSWORD用ゲッタの定義
        /*
		String serializeStr = "";
		Iterator iterator = futabaBBSs.iterator();
		// あまり効率のよさそうではない直列化
		while (iterator.hasNext()) {
			serializeStr += iterator.next().toString();
			if (iterator.hasNext()) {
				serializeStr += " ";
			}
		}
        Log.d("ftbt", "write serializeStr=|"+serializeStr+"|");
        SDCard.saveString(OPT_FAVORITES, serializeStr, true);
        */
        SDCard.setSerialized(OPT_FAVORITES, futabaBBSs);
	}
}
