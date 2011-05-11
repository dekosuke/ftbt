package cx.ath.dekosuke.ftbt;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

import java.util.List;

public class FutabaCookieManager {
	static List<Cookie> data = null;

	public static void saveCookie(DefaultHttpClient httpClient){
		data = httpClient.getCookieStore().getCookies();
	}
	
	public static void loadCookie(DefaultHttpClient httpClient){
		if(data != null){
		FLog.d("adding cookies");
			for (int i = 0; i < data.size(); i++) {
			FLog.d("cookie: " + data.get(i).toString());
				httpClient.getCookieStore().addCookie(data.get(i));
			}
		}
	}
	
	//デバッグ用
	public static void PrintCookie(){
		if (data==null || data.isEmpty()) {
		FLog.d("Cookie(in FutabaCookieManager) None");
		} else {
		FLog.d("Show Cookies(in FutabaCookieManager)");
			for (int i = 0; i < data.size(); i++) {
			FLog.d("1 " + data.get(i).toString());
			}
		}
	}
}
