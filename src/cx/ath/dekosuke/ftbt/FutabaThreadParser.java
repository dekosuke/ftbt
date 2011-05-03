package cx.ath.dekosuke.ftbt;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.net.URL;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class FutabaThreadParser {

	private static final String FORMTAG = "form";
	private static final String BLOCKQUOTETAG = "qlockquote";
	private static final String TABLETAG = "table";
	private static final String IMGTAG = "img";
	private static final String SRCTAG = "src";

	private String urlStr;
	private String title;
	private String titleImgURL;

	public boolean network_ok;
	public boolean cache_ok;

	private ArrayList<FutabaStatus> statuses;

	private static Pattern tagPattern = Pattern
			.compile("<.+?>", Pattern.DOTALL);

	public FutabaThreadParser(String urlStr) {
		this.urlStr = urlStr;
		title = "(title)";
		statuses = new ArrayList<FutabaStatus>();
	}

	// メモ:ふたばのスレッドはhtml-body-2つめのformのなかにある
	// TODO:mailtoのパーズ
	// スレッドの形式:
	public void parse() {
		try {
			// 正規表現でパーズ範囲を絞り込む
			Pattern honbunPattern = Pattern.compile("<form.*?>.+?</form>",
					Pattern.DOTALL);
			Pattern resPattern = Pattern.compile("<table.*?>(.+?)</table>",
					Pattern.DOTALL);
			Pattern textPattern = Pattern.compile(
					"<input[^>]+><font[^>]+><b>(.*?)</b></font>"+
					".*?<font[^>]+><b>(.*?) ?</b></font>"+
					"(.*?) (No.[0-9]+).+?<blockquote.*?>(.+?)</blockquote>", Pattern.DOTALL);
			Pattern imgPattern = Pattern.compile(
					"<a.*?target.*?href=\"(.+?)\"", Pattern.DOTALL);
			Pattern thumbPattern = Pattern.compile(
					"<img.*?src=\"(.+?)\".+?width=([0-9]+).+?height=([0-9]+)",
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
				network_ok = false;
				Log.d("ftbt", "failed to get catalog html");
				if (SDCard.cacheExist(FutabaCrypt.createDigest(urlStr))) {
					Log.d("ftbt", "getting html from cache");
					allData = SDCard.loadTextCache(FutabaCrypt
							.createDigest(urlStr));
					cache_ok = true;
				}else{
					cache_ok = false;					
				}
			}
			// parser.setInput(new StringReader(new String(data, "UTF-8")));
			Log.d("ftbt", allData);
			Matcher mc = honbunPattern.matcher(allData);
			mc.find();
			mc.find(); // 2つ目
			String honbun = mc.group(0);
			// ここで画像(img)とテキスト(blockquote)のマッチング
			FutabaStatus statusTop = new FutabaStatus();
			Matcher mcImg = thumbPattern.matcher(honbun);
			mcImg.find();
			statusTop.imgURL = mcImg.group(1);
			// Log.d("ftbt", "parse w="+mcImg.group(2)+"h="+mcImg.group(3) );
			statusTop.width = Integer.parseInt(mcImg.group(2));
			statusTop.height = Integer.parseInt(mcImg.group(3));
			Matcher mcBigImg = imgPattern.matcher(honbun);
			mcBigImg.find();
			statusTop.bigImgURL = mcBigImg.group(1);
			Matcher mcText = textPattern.matcher(honbun);
			mcText.find();
			statusTop.title=mcText.group(1);
			statusTop.name =mcText.group(2);
			statusTop.datestr =mcText.group(3);
			statusTop.idstr =mcText.group(4);
			String text = mcText.group(5);
			text = normalize(text);
			statusTop.text = text;
			statuses.add(statusTop);

			// Log.d( "ftbt", honbun );
			Matcher mcRes = resPattern.matcher(honbun);
			while (mcRes.find()) {
				mcText = textPattern.matcher(mcRes.group(1));
				// Log.d( "ftbt", mcRes.group(1) );
				mcText.find();
				FutabaStatus status = new FutabaStatus();
				status.title=mcText.group(1);
				status.name =normalize(mcText.group(2)); //メールアドレスが入っていることあり
				status.datestr =mcText.group(3);
				status.idstr =mcText.group(4);
				text = mcText.group(5);
				text = normalize(text);
				status.text = text;
				mcImg = thumbPattern.matcher(mcRes.group(1));
				if (mcImg.find()) {
					status.imgURL = mcImg.group(1);
					status.width = Integer.parseInt(mcImg.group(2));
					status.height = Integer.parseInt(mcImg.group(3));
					// Log.d("ftbt",
					// "parse w="+mcImg.group(2)+"h="+mcImg.group(3) );
				}
				mcBigImg = imgPattern.matcher(mcRes.group(1));
				if (mcBigImg.find()) {
					status.bigImgURL = mcBigImg.group(1);
				}
				// Log.d( "ftbt", text );
				statuses.add(status);
			}
		} catch (Exception e) {
			Log.i("ftbt", "failure in FutabaThreadParser", e);
		}
		// return list;
	}

	private String normalize(String text) {
		text = text.replaceAll("<br>", "\n");
		text = tagPattern.matcher(text).replaceAll(""); // タグ除去
		text = text.replaceAll("&gt;", ">");
		return text;
	}

	// 返信ひとつげっと
	public void addStatus(XmlPullParser parser) {
		String text = "";
		String imgURL = "";
		try {
			int eventType = parser.next();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String name = null;
				switch (eventType) {
				case XmlPullParser.START_TAG:
					name = parser.getName();
					if (name.equalsIgnoreCase(BLOCKQUOTETAG)) {
						text = parser.getText(); // リプライ文
					} else if (name.equalsIgnoreCase(IMGTAG)) {
						imgURL = getAttributeByName(parser, SRCTAG);
					}
					break;
				case XmlPullParser.END_TAG:
					name = parser.getName();
					if (name.equalsIgnoreCase(TABLETAG)) {
						// スレッド追加
						FutabaStatus status = new FutabaStatus();
						status.text = text;
						status.imgURL = imgURL;
						statuses.add(status);
						return;
					}
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getAttributeByName(XmlPullParser parser, String nameArg) {
		for (int i = 0; i < parser.getAttributeCount(); ++i) {
			String name = parser.getAttributeName(i);
			if (nameArg.equalsIgnoreCase(name)) {
				return parser.getAttributeValue(i);
			}
		}
		return "";
	}

	public ArrayList<FutabaStatus> getStatuses() {
		return statuses;
	}
}
