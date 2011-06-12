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

//注目キーワードリストの設定
public class FocusedSettings {
	private static final String OPT_KEYWORDS = "keywords";

	public static ArrayList<String> get(Context context) 
        throws IOException {
    	ArrayList<String> keywords = new ArrayList<String>();
        try{
	    	if( SDCard.existSeriarized(OPT_KEYWORDS)){
                keywords = (ArrayList<String>) SDCard.getSerialized(OPT_KEYWORDS).readObject();
            }
        }catch(Exception e){
           FLog.d("message", e);
        }
		return keywords;
	}

	public static void set(Context context,
			ArrayList<String> futabaBBSs) throws IOException {
        SDCard.setSerialized(OPT_KEYWORDS, futabaBBSs);
	}
}
