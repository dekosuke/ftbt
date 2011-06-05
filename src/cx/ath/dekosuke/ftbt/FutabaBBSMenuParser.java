package cx.ath.dekosuke.ftbt;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.net.URL;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

//板一覧を取得するクラス
public class FutabaBBSMenuParser {

	private ArrayList<FutabaBBSContent> BBSs;
	private String urlStr;
	public boolean network_ok;
	public boolean cache_ok;
	public boolean display_censored = false;

	public FutabaBBSMenuParser(String urlStr) {
		this.urlStr = urlStr;
		BBSs = new ArrayList<FutabaBBSContent>();
		network_ok = true;
		cache_ok = true;
	}

	public void setDisplayCensored(boolean bool) {
		display_censored = bool;
	}

	// スレッドの形式:
	//<li><a href="http://boards.4chan.org/int/" class="boardlink" title="International">International</a></li>
	//http://boards.4chan.org/i/
	public void parse() {
		try {
			// 正規表現でパーズ範囲を絞り込む
			Pattern BBSPattern = Pattern.compile(
					"<a href=\"http://boards.4chan.org/([^/])+/[^>]+?title=\"([^\"]+)\"",
					Pattern.DOTALL);

			String allData = "";
			try {
				// byte[] data = HttpClient.getByteArrayFromURL(urlStr);
				// allData = new String(data, "Shift-JIS");
				SDCard.saveFromURL(FutabaCrypt.createDigest(urlStr), new URL(
						urlStr), true); // キャッシュに保存
				allData = SDCard
						.loadTextCache(FutabaCrypt.createDigest(urlStr));
				network_ok = true;
			} catch (Exception e) { // ネットワークつながってないときとか
				FLog.d("failed to get catalog html");
				network_ok = false;
				if (SDCard.cacheExist(FutabaCrypt.createDigest(urlStr))) {
					FLog.d("getting html from cache");
					allData = SDCard.loadTextCache(FutabaCrypt
							.createDigest(urlStr));
				} else { // キャッシュもない
					cache_ok = false;
				}
			}
			FLog.d("AllData:"+allData);
			Matcher mcBBS = BBSPattern.matcher(allData);
			while (mcBBS.find()) {
				FutabaBBSContent bbs = new FutabaBBSContent();
				bbs.url = "http://boards.4chan.org/"+mcBBS.group(1)+"/";
				bbs.name = mcBBS.group(2);
				FLog.d("matched BBS:"+bbs.toString());
				BBSs.add(bbs);
			}
		} catch (Exception e) {
			FLog.d("failure in FutabaBBSMenuParser", e);
		}
		// return list;
	}

	public ArrayList<FutabaBBSContent> getBBSs() {
		return BBSs;
	}
}
