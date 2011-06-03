package cx.ath.dekosuke.ftbt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.CookieManager;

//カタログのHTML取得が死ぬほど長いのでとりあえず一つのファイルに
//長い理由は２回アクセスしているからだったり
public class CatalogHtmlReader {

	public static String Read(String urlStr, Context context, int sortType)
			throws Exception {

		CookieManager.getInstance().setAcceptCookie(true);
		CookieManager.getInstance().removeExpiredCookie();

		int catalogX = 10;
		int catalogY = 5;
		String threadStrNum = "50";
		try {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(context);
			int temp = Integer.parseInt(preferences.getString(
					"catalogThreadNum", "50"));
			catalogX = temp / 5;
			threadStrNum = preferences.getString(
					"threadStrNum", "50");
		} catch (Exception e) {
			FLog.d("message", e);
		}

		// HttpClientの準備
		DefaultHttpClient httpClient;
		httpClient = new DefaultHttpClient();
		FutabaCookieManager.loadCookie(httpClient); // クッキーのロード
		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.BROWSER_COMPATIBILITY);
		httpClient.getParams().setParameter("http.connection.timeout", 5000);
		httpClient.getParams().setParameter("http.socket.timeout", 3000);
		HttpPost httppost = new HttpPost(urlStr);
		List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(4);
		nameValuePair.add(new BasicNameValuePair("mode", "catset"));
		nameValuePair
				.add(new BasicNameValuePair("cx", String.valueOf(catalogX)));
		nameValuePair
				.add(new BasicNameValuePair("cy", String.valueOf(catalogY)));
		nameValuePair.add(new BasicNameValuePair("cl", threadStrNum));

		String urlStrAppend = urlStr + "?mode=cat"; // カタログです
		if (sortType > 0 && sortType < 5) {
			urlStrAppend += "&sort=" + sortType;
		}
		String data = null;
		// cookie取得->カタログ取得と2度HTTPアクセスしている
		httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
		HttpResponse response = httpClient.execute(httppost);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		response.getEntity().writeTo(byteArrayOutputStream);
		HttpGet httpget = new HttpGet(urlStrAppend);
		HttpResponse httpResponse = null;
		httpResponse = httpClient.execute(httpget);
		FutabaCookieManager.saveCookie(httpClient); // クッキー保存
		int status = httpResponse.getStatusLine().getStatusCode();
		if (HttpStatus.SC_OK == status) {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			httpResponse.getEntity().writeTo(outputStream);
			SDCard.saveBin(FutabaCrypt.createDigest(urlStr),
					outputStream.toByteArray(), true); // キャッシュに保存
			data = outputStream.toString("SHIFT-JIS");
			// parse(outputStream.toString());
		} else {
			FLog.d("NON-OK Status" + status);
			throw new Exception("HTTP BAD RESULT");
		}
		return data;
	}
}
