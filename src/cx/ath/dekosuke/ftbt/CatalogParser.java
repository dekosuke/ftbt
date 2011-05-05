package cx.ath.dekosuke.ftbt;

import java.io.IOException;
import java.io.StringReader;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.ArrayList;

import android.util.Log;
import android.os.AsyncTask;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.content.Context;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.NameValuePair;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;

public class CatalogParser {

	private String urlStr;
	private String title;
	private String titleImgURL;
	public boolean network_ok;
	public boolean cache_ok;

	private ArrayList<FutabaThreadContent> fthreads;

	public CatalogParser(String urlStr) {
		this.urlStr = urlStr;
		title = "(title)";
		fthreads = new ArrayList<FutabaThreadContent>();
		network_ok = true;
		cache_ok = true;
	}

	// メモ:ふたばのスレッドはhtml-body-2つめのformのなかにある
	// スレッドの形式:
	public void parse(Context context) {
		try {
			// 正規表現でパーズ範囲を絞り込む
			Pattern honbunPattern = Pattern.compile("<table.+?>.+?</table>",
					Pattern.DOTALL);
			Pattern resPattern = Pattern.compile("<td.*?>(.+?)</td>",
					Pattern.DOTALL);
			Pattern textPattern = Pattern.compile("<small.*?>(.+?)</small>.+?<font[^>]+>(.+?)</font>",
					Pattern.DOTALL);
			Pattern imgPattern = Pattern.compile(
					"<img.*?src=(?:\"|')(.+?)(?:\"|')", Pattern.DOTALL);
			Pattern threadNumPattern = // numといいつつres/部分（スレによる）も入ってる
			Pattern.compile("<a.*?href=(?:\"|')([a-zA-Z0-9/]+[.]htm)(?:\"|')",
					Pattern.DOTALL);
			Pattern tagPattern = Pattern.compile("<.+?>", Pattern.DOTALL);

			CookieManager.getInstance().setAcceptCookie(true);
			CookieManager.getInstance().removeExpiredCookie();

			// HttpClientの準備
			DefaultHttpClient httpClient;
			httpClient = new DefaultHttpClient();
			FutabaCookieManager.loadCookie(httpClient); //クッキーのロード
			httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
					CookiePolicy.BROWSER_COMPATIBILITY);
			httpClient.getParams()
					.setParameter("http.connection.timeout", 5000);
			httpClient.getParams().setParameter("http.socket.timeout", 3000);
			HttpPost httppost = new HttpPost(urlStr);
			List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(3);
			nameValuePair.add(new BasicNameValuePair("mode", "catset"));
			nameValuePair.add(new BasicNameValuePair("cx", "10"));
			nameValuePair.add(new BasicNameValuePair("cy", "5"));
			nameValuePair.add(new BasicNameValuePair("cl", "50"));

			urlStr = urlStr + "?mode=cat"; // カタログです
			String data = null;
			// cookie取得->カタログ取得と2度HTTPアクセスしている
			try {
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePair));
				HttpResponse response = httpClient.execute(httppost);
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				response.getEntity().writeTo(byteArrayOutputStream);
				HttpGet httpget = new HttpGet(urlStr);
				HttpResponse httpResponse = null;
				httpResponse = httpClient.execute(httpget);
				FutabaCookieManager.saveCookie(httpClient); //クッキー保存
				int status = httpResponse.getStatusLine().getStatusCode();
				if (HttpStatus.SC_OK == status) {
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					httpResponse.getEntity().writeTo(outputStream);
					SDCard.saveBin(FutabaCrypt.createDigest(urlStr),
							outputStream.toByteArray(), true); // キャッシュに保存
					data = outputStream.toString("SHIFT-JIS");
					// parse(outputStream.toString());
				} else {
					Log.d("ftbt", "NON-OK Status" + status);
					throw new Exception("HTTP BAD RESULT");
				}
				network_ok = true;
			} catch (Exception e) { // カタログ取得に失敗、キャッシュから
				Log.d("ftbt", "message", e);
				network_ok = false;
				if (SDCard.cacheExist(FutabaCrypt.createDigest(urlStr))) {
					Log.d("ftbt",
							"getting html from cache"
									+ FutabaCrypt.createDigest(urlStr));
					data = SDCard.loadTextCache(FutabaCrypt
							.createDigest(urlStr));
				} else {
					Log.d("ftbt", "cache " + FutabaCrypt.createDigest(urlStr)
							+ "not found");
					cache_ok = false;
				}
			}

			Matcher mc = honbunPattern.matcher(data);
			mc.find();
			mc.find(); // 2つ目
			String honbun = mc.group(0);
			// Log.d( "ftbt", honbun );
			Matcher mcRes = resPattern.matcher(honbun);
			while (mcRes.find()) {
				Matcher mcText = textPattern.matcher(mcRes.group(1));
				mcText.find();
				FutabaThreadContent thread = new FutabaThreadContent();
				String text = mcText.group(1);
				String resNum = mcText.group(2);
				text = tagPattern.matcher(text).replaceAll(""); // タグ除去
				thread.text=text;
				thread.resNum=resNum;
				Matcher mcThreadNum = threadNumPattern.matcher(mcRes.group(1));
				mcThreadNum.find();
				String threadNum = mcThreadNum.group(1);
				thread.threadNum = threadNum;
				Matcher mcImg = imgPattern.matcher(mcRes.group(1));
				if (mcImg.find()) {
					thread.imgURL = mcImg.group(1);
				}
				// Log.d( "ftbt", text );
				fthreads.add(thread);
			}
		} catch (Exception e) {
			Log.i("ftbt", "parser error", e);
			throw new RuntimeException(e);
		}
		// return list;
	}

	public ArrayList<FutabaThreadContent> getThreads() {
		return fthreads;
	}
}
