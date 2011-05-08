package cx.ath.dekosuke.ftbt;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.net.URL;

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

	public FutabaBBSMenuParser(String urlStr) {
		this.urlStr = urlStr;
		BBSs = new ArrayList<FutabaBBSContent>();
		network_ok = true;
		cache_ok = true;
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
				network_ok = true;
			} catch (Exception e) { // ネットワークつながってないときとか
				Log.d("ftbt", "failed to get catalog html");
				network_ok = false;
				if (SDCard.cacheExist(FutabaCrypt.createDigest(urlStr))) {
					Log.d("ftbt", "getting html from cache");
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
				if(bbs.name.equals("二次元裏")){
					if(bbs.url.contains("may")){
						bbs.name="二次元裏(may)";
						//imgを手動追加
						FutabaBBSContent img_bbs = new FutabaBBSContent();
						img_bbs.name = "二次元裏(img)";
						img_bbs.url = "http://img.2chan.net/b/";
						BBSs.add(img_bbs);
					}else if(bbs.url.contains("dec")){
						bbs.name="二次元裏(dec)";						
					}else if(bbs.url.contains("jun")){
						bbs.name="二次元裏(jun)";						
					}
				}
				BBSs.add(bbs);
			}
		} catch (Exception e) {
			Log.i("ftbt", "failure in FutabaBBSMenuParser", e);
		}
		// return list;
	}

	public ArrayList<FutabaBBSContent> getBBSs() {
		return BBSs;
	}
}
