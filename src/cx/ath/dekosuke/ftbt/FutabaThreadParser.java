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

	private ArrayList<FutabaStatus> statuses;

	private static Pattern tagPattern = Pattern
			.compile("<.+?>", Pattern.DOTALL);

	public FutabaThreadParser() {
		title = "(title)";
		statuses = new ArrayList<FutabaStatus>();
	}

	// メモ:ふたばのスレッドはhtml-body-2つめのformのなかにある
	// TODO:mailtoのパーズ
	// スレッドの形式:
	// / anonymous = true は いもげとか
	public void parse(String allData, boolean anonymous) {
		try {
			// 正規表現でパーズ範囲を絞り込む
			Pattern honbunPattern = Pattern.compile("<form.*?>.+?</form>",
					Pattern.DOTALL);
			Pattern resPattern = Pattern.compile("<table.*?>(.+?)</table>",
					Pattern.DOTALL);
			Pattern textAttrPattern = Pattern
					.compile("<input[^>]+><font[^>]+><b>(.*?)</b></font>"
							+ ".*?<font[^>]+><b>(.*?) ?</b></font>"
							+ "(.*?) (No.[0-9]+).+?<blockquote", Pattern.DOTALL);
			Pattern imgTextAttrPattern = Pattern.compile(
					"<input[^>]+>([^<]*?)<a", Pattern.DOTALL);
			Pattern textPattern = Pattern.compile(
					"<blockquote.*?>(.+?)</blockquote>", Pattern.DOTALL);
			Pattern imgPattern = Pattern.compile(
					"<a.*?target.*?href=(?:\"|\')(.+?)(?:\"|\')",
					Pattern.DOTALL);
			Pattern thumbPattern = Pattern
					.compile(
							"<img.*?src=(?:\"|\')(.+?)(?:\"|\').+?width=([0-9]+).+?height=([0-9]+)",
							Pattern.DOTALL);

			// parser.setInput(new StringReader(new String(data, "UTF-8")));
			Matcher mc = honbunPattern.matcher(allData);
			mc.find();
			mc.find(); // 2つ目
			String honbun = mc.group(0);
			// ここで画像(img)とテキスト(blockquote)のマッチング
			FutabaStatus statusTop = new FutabaStatus();
			Matcher mcImg = thumbPattern.matcher(honbun);
			if (mcImg.find()) {
				statusTop.imgURL = mcImg.group(1);
				statusTop.width = Integer.parseInt(mcImg.group(2));
				statusTop.height = Integer.parseInt(mcImg.group(3));
			}
			// Log.d("ftbt", "parse w="+mcImg.group(2)+"h="+mcImg.group(3) );
			Matcher mcBigImg = imgPattern.matcher(honbun);
			if (mcBigImg.find()) {
				statusTop.bigImgURL = mcBigImg.group(1);
			}
			Matcher mcText = textPattern.matcher(honbun);
			mcText.find();
			if (!anonymous) {
				Matcher mcTextAttr = textAttrPattern.matcher(honbun);
				if (mcTextAttr.find()) {
					statusTop.title = mcTextAttr.group(1);
					statusTop.name = normalize(mcTextAttr.group(2)); // メールアドレスが入っていることあり
					statusTop.datestr = mcTextAttr.group(3);
					statusTop.idstr = mcTextAttr.group(4);
				}
			} else {
				Matcher mcTextAttr = imgTextAttrPattern.matcher(honbun);
				if (mcTextAttr.find()) {
					statusTop.datestr = mcTextAttr.group(1);
				}
			}
			String text = mcText.group(1);
			statusTop.text = text;
			statuses.add(statusTop);

			// Log.d( "ftbt", honbun );
			Matcher mcRes = resPattern.matcher(honbun);
			while (mcRes.find()) {
				FutabaStatus status = new FutabaStatus();
				mcText = textPattern.matcher(mcRes.group(1));
				// Log.d( "ftbt", mcRes.group(1) );
				mcText.find();
				if (!anonymous) {
					Matcher mcTextAttr = textAttrPattern.matcher(mcRes.group(1));
					if (mcTextAttr.find()) {
						status.title = mcTextAttr.group(1);
						status.name = normalize(mcTextAttr.group(2)); // メールアドレスが入っていることあり
						status.datestr = mcTextAttr.group(3);
						status.idstr = mcTextAttr.group(4);
					}
				} else {
					Matcher mcTextAttr = imgTextAttrPattern.matcher(mcRes.group(1));
					if (mcTextAttr.find()) {
						status.datestr = mcTextAttr.group(1);
					}
				}

				text = mcText.group(1);
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
	
	public FutabaThreadContent buildThreadInfo(){
		/*
		public String userName;
		public String title;
		public String text;
		public String mailTo;
		public int id;
		public String imgURL;
		public String threadNum;
		public String baseUrl;//historyのときはこれが入っている。内容的にはFutabaThreadに渡すintentと同じ
		public String resNum;
		public String BBSName; //どの板か->過去ログ表示のときに使う
		public long lastAccessed; //最終アクセス時間(UnixTime)
		public boolean isChecked=false;		
		
						Matcher mcText = textPattern.matcher(mcRes.group(1));
				mcText.find();
				FutabaThreadContent thread = new FutabaThreadContent();
				String text = mcText.group(1);
				String resNum = mcText.group(2);
				//text = tagPattern.matcher(text).replaceAll(""); // タグ除去
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
		*/
		if(statuses.size()==0){ return null; }
		FutabaThreadContent thread = new FutabaThreadContent();
		thread.userName = statuses.get(0).userName;
		thread.title = statuses.get(0).title;
		thread.text  = statuses.get(0).text;
		thread.resNum = ""+(statuses.size()-1);
		thread.threadNum = "res/" + statuses.get(0).idstr;
		//カタログ画像が抜けてしまうけど仕方ないね
		return thread;
	}

	public ArrayList<FutabaStatus> getStatuses() {
		return statuses;
	}
	
	//タイトルを取得する(最大num文字)
	public String getTitle(int num){
		if(statuses.size()>0){
			String text = statuses.get(0).title;
			text = tagPattern.matcher(text).replaceAll(""); // タグ除去
			return text.substring(0, Math.min(text.length(), num));
		}
		return "(no title)";
	}
}
