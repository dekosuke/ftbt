package cx.ath.dekosuke.ftbt;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import java.io.IOException;
import java.util.List;

public class FutabaCookieManager {
	static List<Cookie> data = null;

	public static void saveCookie(DefaultHttpClient httpClient) {
		data = httpClient.getCookieStore().getCookies();
		FLog.d("saving cookies"+data.size());
		for (int i = 0; i < data.size(); i++) {
			FLog.d("saving cookie: " + data.get(i).toString());
		}
	}

	public static void loadCookie(DefaultHttpClient httpClient) {

		if (data != null) {
			FLog.d("adding cookies"+data.size());
			for (int i = 0; i < data.size(); i++) {
				FLog.d("cookie: " + data.get(i).toString());
				httpClient.getCookieStore().addCookie(data.get(i));
			}
		}
	}

	// デバッグ用
	public static void PrintCookie() {
		if (data == null || data.isEmpty()) {
			FLog.d("Cookie(in FutabaCookieManager) None");
		} else {
			FLog.d("Show Cookies(in FutabaCookieManager)");
			for (int i = 0; i < data.size(); i++) {
				FLog.d("1 " + data.get(i).toString());
			}
		}
	}

	// GetでスレッドなどのHTML取得
	// http://terurou.hateblo.jp/entry/20110702/1309541200
	public static String GetWithCookie(final String url) throws IOException {
		HttpGet request = new HttpGet(url);
		DefaultHttpClient httpClient = new DefaultHttpClient();
		loadCookie(httpClient); //ここでクッキー設定
		try {
			String result = httpClient.execute(request,
					new ResponseHandler<String>() {
						@Override
						public String handleResponse(HttpResponse response)
								throws IOException {

							// response.getStatusLine().getStatusCode()でレスポンスコードを判定する。
							// 正常に通信できた場合、HttpStatus.SC_OK（HTTP 200）となる。
							switch (response.getStatusLine().getStatusCode()) {
							case HttpStatus.SC_OK:
								//正常終了List<Cookie> cookies = httpClient.getCookieStore().getCookies();

								return EntityUtils.toString(
										response.getEntity(), "SHIFT-JIS");

							case HttpStatus.SC_NOT_FOUND:
								throw new IOException("data not found"+url); // FIXME

							default:
								throw new IOException("default io exceptionZ"); // FIXME
							}

						}
					});

			// logcatにレスポンスを表示
			FLog.d("GetWithCookie:"+result);
			//cookie保存
			saveCookie(httpClient);
			return result;
		} finally {
			// ここではfinallyでshutdown()しているが、HttpClientを使い回す場合は、
			// 適切なところで行うこと。当然だがshutdown()したインスタンスは通信できなくなる。
			httpClient.getConnectionManager().shutdown();
		}
	}
}
