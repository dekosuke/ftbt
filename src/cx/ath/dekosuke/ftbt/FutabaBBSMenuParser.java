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
			Matcher mcBBS = BBSPattern.matcher(allData);
			while (mcBBS.find()) {
				FutabaBBSContent bbs = new FutabaBBSContent();
				bbs.url = mcBBS.group(1);
				bbs.name = mcBBS.group(2);
				if (bbs.name.equals("二次元裏")) {
					if (bbs.url.contains("may")) {
						bbs.name = "二次元裏(may)";
						// imgを手動追加
						FutabaBBSContent img_bbs = new FutabaBBSContent();
						img_bbs.name = "二次元裏(img)";
						img_bbs.url = "http://img.2chan.net/b/";
						BBSs.add(img_bbs);
					} else if (bbs.url.contains("dec")) {
						bbs.name = "二次元裏(dec)";
						// datを手動追加
						FutabaBBSContent dat_bbs = new FutabaBBSContent();
						dat_bbs.name = "二次元裏(dat)";
						dat_bbs.url = "http://dat.2chan.net/b/";
						BBSs.add(dat_bbs);
					} else if (bbs.url.contains("jun")) {
						bbs.name = "二次元裏(jun)";
					}
				}
				// 自作PCの直前に特殊掲示板３つ
				if (bbs.name.equals("自作PC") && display_censored) {
					FutabaBBSContent other_bbs = new FutabaBBSContent();
					other_bbs.name = "二次元グロ";
					other_bbs.url = "http://cgi.2chan.net/o/";
					BBSs.add(other_bbs);
					other_bbs = new FutabaBBSContent();
					other_bbs.name = "二次元グロ裏";
					other_bbs.url = "http://jun.2chan.net/51/";
					BBSs.add(other_bbs);
					other_bbs = new FutabaBBSContent();
					other_bbs.name = "えろげ";
					other_bbs.url = "http://zip.2chan.net/5/";
					BBSs.add(other_bbs);
				}
				if (bbs.name.equals("二次元ID") ) {
					// てすとjunを手動追加
					FutabaBBSContent dat_bbs = new FutabaBBSContent();
					dat_bbs.name = "てすとjun";
					dat_bbs.url = "http://www.2chan.net/30/";
					BBSs.add(dat_bbs);
				}
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
