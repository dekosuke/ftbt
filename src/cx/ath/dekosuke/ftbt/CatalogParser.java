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

	private ArrayList<FutabaThreadContent> fthreads;

	public CatalogParser() {
		fthreads = new ArrayList<FutabaThreadContent>();
	}

	// メモ:ふたばのスレッドはhtml-body-2つめのformのなかにある
	// スレッドの形式:
	public void parse(String catalogHtml) {
		try {
			// 正規表現でパーズ範囲を絞り込む
			Pattern honbunPattern = Pattern.compile("<table.+?>.+?</table>",
					Pattern.DOTALL);
			Pattern resPattern = Pattern.compile("<td.*?>(.+?)</td>",
					Pattern.DOTALL);
			Pattern textPattern = Pattern.compile("<small.*?>(.+?)</small>.*?<font[^>]+>" +
					".*?([0-9]+).*?</font>",
					Pattern.DOTALL);
			Pattern imgPattern = Pattern.compile(
					"<img.*?src=(?:\"|')(.+?)(?:\"|')", Pattern.DOTALL);
			Pattern threadNumPattern = 
			Pattern.compile("<a.*?href=(?:\"|')res[/]([0-9]+)[.]htm(?:\"|')",
					Pattern.DOTALL);
			Pattern tagPattern = Pattern.compile("<.+?>", Pattern.DOTALL);

			Matcher mc = honbunPattern.matcher(catalogHtml);
			mc.find();
			mc.find(); // 2つ目
			String honbun = mc.group(0);
			//FLog.d(honbun );
			Matcher mcRes = resPattern.matcher(honbun);
			while (mcRes.find()) {
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
				thread.threadNum = Integer.parseInt(threadNum);
				Matcher mcImg = imgPattern.matcher(mcRes.group(1));
				if (mcImg.find()) {
					thread.imgURL = mcImg.group(1);
				}
				//FLog.d(text );
				fthreads.add(thread);
			}
		} catch (Exception e) {
		FLog.d("parser error", e);
			throw new RuntimeException(e);
		}
		// return list;
	}

	public ArrayList<FutabaThreadContent> getThreads() {
		return fthreads;
	}
}
