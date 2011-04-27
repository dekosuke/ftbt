package cx.ath.dekosuke.ftbt;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.net.URL;

import android.util.Log;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

//板一覧を取得するクラス
public class FutabaBBSMenuParser {

	private ArrayList<FutabaBBS> BBSs;
	private String urlStr;

	public FutabaBBSMenuParser(String urlStr) {
		this.urlStr = urlStr;
		BBSs = new ArrayList<FutabaBBS>();
	}

	// スレッドの形式:
	public void parse() {
		try {
			// 正規表現でパーズ範囲を絞り込む
			Pattern BBSPattern = Pattern.compile(
					"<a href=\"(http://[^>]+?/)futaba.html?\"[^>]+>(.+?)</a",
					Pattern.DOTALL);

			String allData = "";
			try {
				// byte[] data = HttpClient.getByteArrayFromURL(urlStr);
				// allData = new String(data, "Shift-JIS");
				SDCard.saveFromURL(FutabaCrypt.createDigest(urlStr), new URL(
						urlStr), true); // キャッシュに保存
				allData = SDCard
						.loadTextCache(FutabaCrypt.createDigest(urlStr));
			} catch (Exception e) { // ネットワークつながってないときとか
				Log.d("ftbt", "failed to get catalog html");
				if (SDCard.cacheExist(FutabaCrypt.createDigest(urlStr))) {
					Log.d("ftbt", "getting html from cache");
					allData = SDCard.loadTextCache(FutabaCrypt
							.createDigest(urlStr));
				}
			}
			Matcher mcBBS = BBSPattern.matcher(allData);
			while (mcBBS.find()) {
				FutabaBBS bbs = new FutabaBBS();
				bbs.url = mcBBS.group(1);
				bbs.name = mcBBS.group(2);
				BBSs.add(bbs);
				Log.d("ftbt", "url=" + bbs.url);
			}
		} catch (Exception e) {
			Log.i("ftbt", "failure in FutabaBBSMenuParser", e);
		}
		// return list;
	}

	public ArrayList<FutabaBBS> getBBSs() {
		return BBSs;
	}
}
