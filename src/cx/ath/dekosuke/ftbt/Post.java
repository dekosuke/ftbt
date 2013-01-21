package cx.ath.dekosuke.ftbt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import cx.ath.dekosuke.ftbt.R.id;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

//multipart postのため
//設定 buildpath->configure buildpath->libraryで個別追加した
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

public class Post extends Activity implements Runnable {
	public String urlStr;
	public int threadNum;
	public String threadURL;
	public String postText;

	ProgressDialog waitDialog;
	Thread thread;

	// multipart 画像添付回り
	final int REQUEST_IMAGEPICK_CONSTANT = 0x100200;
	Uri imageContent = null;

	// スレ建てか返信かどうか
	boolean newthread = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 無操作で暗くなるのを防ぐ
		if (getResources().getBoolean(R.bool.avoidsleep)) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		try {
			FLog.d("start Post activity");
			Intent intent = getIntent();
			// urlStr = (String) intent.getSerializableExtra("urlStr");
			String baseURL = (String) intent.getSerializableExtra("baseURL");
			threadNum = (Integer) intent.getSerializableExtra("threadNum");
			postText = (String) intent.getSerializableExtra("postText");
			if (threadNum == 0) {
				newthread = true;
			}
			if (newthread) {
				threadURL = baseURL;
			} else {
				threadURL = baseURL + threadNum;
			}
			urlStr = baseURL + "futaba.php?guid=on";

			setContentView(R.layout.post);
			TextView titleText = (TextView) findViewById(id.titletext);
			if (newthread) {
				titleText.setText("スレッドを建てる");
			}
			EditText comment_v = (EditText) findViewById(id.comment);
			// comment_v.setFocusable(true);
			// comment_v.requestFocus(View.FOCUS_DOWN);
			if (postText != null) {
				comment_v.setText(postText);
				comment_v.setSelection(postText.length()); // テキストの最後にfocus当てる。
			}
			// comment_v.setSelected(false);

			String deleteKey = "";
			try {
				SharedPreferences preferences = PreferenceManager
						.getDefaultSharedPreferences(this);
				deleteKey = preferences.getString(
						getString(R.string.deletekey), "");
			} catch (Exception e) {
				FLog.d("message", e);
			}
			if (!deleteKey.equals("")) {
				TextView deleteKey_v = (TextView) findViewById(id.deletekey);
				deleteKey_v.setText(deleteKey);

			}
			FLog.d("deletekey=" + deleteKey);

			Button postbutton = (Button) findViewById(id.postbutton);
			postbutton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					onClickPostButton(v);
				}
			});
			Button imgchoosebutton = (Button) findViewById(id.imgchoosebutton);
			imgchoosebutton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					onClickImageChooseButton(v);
				}
			});
			/*
			 * TableLayout tablelayout = (TableLayout)
			 * findViewById(id.tableLayout1); tablelayout.setColumnCollapsed(0,
			 * true); tablelayout.setColumnCollapsed(1, true);
			 * tablelayout.setColumnCollapsed(2, true);
			 * tablelayout.setColumnCollapsed(3, true);
			 * tablelayout.setColumnCollapsed(4, true);
			 */

			FutabaCookieManager.PrintCookie();

		} catch (Exception e) {
			FLog.d("message", e);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		//CookieSyncManager.getInstance().startSync();
	}

	@Override
	protected void onPause() {
		super.onPause();
		//CookieSyncManager.getInstance().stopSync();
	}

	public void setWait() {
		waitDialog = new ProgressDialog(this);
		waitDialog.setMessage(this.getString(R.string.loading));
		waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// waitDialog.setCancelable(true);
		waitDialog.show();

		thread = new Thread(this);
		thread.start();
	}

	public void run() {
		try { // 細かい時間を置いて、ダイアログを確実に表示させる
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// スレッドの割り込み処理を行った場合に発生、catchの実装は割愛
		}
		handler.sendEmptyMessage(0);
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			// HandlerクラスではActivityを継承してないため
			// 別の親クラスのメソッドにて処理を行うようにした。
			try {
				loading();
			} catch (Exception e) {
				FLog.d("message", e);
			}
		}
	};

	public void onClickPostButton(View v) {

		// 確認
		AlertDialog.Builder dlg;
		dlg = new AlertDialog.Builder(Post.this);
		dlg.setTitle("投稿の確認");
		dlg.setCancelable(true);
		dlg.setMessage("投稿してよいですか？");
		dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Catalog.this.finish();
				// Catalog.this.deleteThreads();
				Post.this.setWait();
			}
		});
		dlg.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Catalog.cancel();
				// Catalog.this.deleteThreads(false);
			}
		});
		dlg.show();

		// setWait();
	}

	public void onClickImageChooseButton(View v) {
		// 画像選択ボタン
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, REQUEST_IMAGEPICK_CONSTANT);
	}

	// 画像を選択した直後に呼ばれる
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK
				&& requestCode == REQUEST_IMAGEPICK_CONSTANT) {
			Toast.makeText(this, "画像" + data.getData(), Toast.LENGTH_SHORT)
					.show();
			/*
			 * try{ InputStream is =
			 * getContentResolver().openInputStream(data.getData()); Bitmap bm =
			 * BitmapFactory.decodeStream(is); is.close(); }catch(Exception e){
			 * 
			 * }
			 */
			imageContent = data.getData();
			Button imgchoosebutton = (Button) findViewById(id.imgchoosebutton);
			imgchoosebutton.setVisibility(View.GONE); // 画像選択ボタンを消す
			TextView imgchoosenotify = (TextView) findViewById(id.imgchoosenotify);
			imgchoosenotify.setText("選択:" + imageContent.toString()); // 選択したファイル名を表示
			imgchoosenotify.setTextColor(Color.RED);
		}
	}

	private void loading() {

		// HTTPリクエストを作成する
		// futaba.php?guid=on POST
		// <b>題　　名</b></td><td><input type=text name=sub size="35"
		// <input type=submit value="返信する" onClick="ptfk(48912)">
		// <textarea name=com cols="48" rows="4" id="ftxa"></textarea>
		// <b>削除キー</b></td><td><input type=password name=pwd size=8 maxlength=8
		// value="">
		/*
		 * http://localhost/futaba.php?mode=regist&MAX_FILE_SIZE=512000&
		 * pthb=&pthc=&pthd=&flvr=&scsz=&js=off&
		 * resto=48912&name=name&email=email
		 * &sub=title&com=text&textonly=on&pwd=1111
		 */
		// viewからデータ取得
		TextView name_v = (TextView) findViewById(id.name);
		String name = name_v.getText().toString();
		TextView email_v = (TextView) findViewById(id.email);
		String email = email_v.getText().toString();
		TextView comment_v = (TextView) findViewById(id.comment);
		String comment = comment_v.getText().toString();
		TextView deletekey_v = (TextView) findViewById(id.deletekey);
		String deletekey = deletekey_v.getText().toString();

		/*
		 * Post activity = (Post) getContext(); String threadNum =
		 * activity.threadNum; String urlStr = activity.urlStr;
		 */

		FLog.d("threadNum=" + threadNum);
		FLog.d("urlStr=" + urlStr);
		FLog.d("comment=" + comment);
		FLog.d("deletekey=" + deletekey);
		FLog.d("email=" + email);
		FLog.d("name=" + name);
		FLog.d("threadURL=" + threadURL);

		if (false) {
			return;
		}

		// というわけでリクエストの作成
		// スレッドに一度アクセスしてcookieセット－＞書き込みの２度アクセス
		try {
			FLog.d("start post!");

			DefaultHttpClient httpClient;
			httpClient = new DefaultHttpClient();
			//httpClient.setHeader( "Connection", "Keep-Alive" );
			FutabaCookieManager.loadCookie(httpClient);
			httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
					CookiePolicy.BROWSER_COMPATIBILITY);
			httpClient.getParams()
					.setParameter("http.connection.timeout", 5000);
			httpClient.getParams().setParameter("http.socket.timeout", 3000);

			String posttime = "";
			List<Cookie> cookies = httpClient.getCookieStore()
					.getCookies();
			for (int i = 0; i < cookies.size(); i++) {
				if(cookies.get(i).getName().equals("posttime")){
					posttime = cookies.get(i).getValue();
					FLog.d("posttime="+posttime);
				}
			}

			HttpPost httppost = new HttpPost(urlStr);
			// 添付画像
			if (false) {
				// multipartにはライブラリ追加が必要そう
				// http://www.softwarepassion.com/android-series-get-post-and-multipart-post-requests/
				// http://yakinikunotare.boo.jp/orebase/index.php?Android%A1%CA%B3%AB%C8%AF%A1%CB%2F%A5%CD%A5%C3%A5%C8%A5%EF%A1%BC%A5%AF%A4%F2%BB%C8%A4%C3%A4%C6%A5%D5%A5%A1%A5%A4%A5%EB%A4%F2%A5%A2%A5%C3%A5%D7%A5%ED%A1%BC%A5%C9%A4%B9%A4%EB
				// MultipartEntity reqEntity = new
				// MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				// reqEntity.addPart("myFile", bin);
				// FileBody fileBody = new FileBody(file);
				// entity.addPart("upfile", fileBody);
			}
			/*
			 * List<NameValuePair> nameValuePair = new
			 * ArrayList<NameValuePair>(7); nameValuePair.add(new
			 * BasicNameValuePair("email", email)); nameValuePair.add(new
			 * BasicNameValuePair("name", name)); nameValuePair.add(new
			 * BasicNameValuePair("mode", "regist")); nameValuePair.add(new
			 * BasicNameValuePair("resto", threadNum)); nameValuePair.add(new
			 * BasicNameValuePair("com", comment)); nameValuePair.add(new
			 * BasicNameValuePair("sub", "")); //題名 nameValuePair.add(new
			 * BasicNameValuePair("pwd", deletekey));
			 */
			MultipartEntity entity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);
			Charset sjisCharset = Charset.forName("Shift_JIS");
			entity.addPart("email", new StringBody(email, sjisCharset));
			entity.addPart("name", new StringBody(name, sjisCharset));
			entity.addPart("mode", new StringBody("regist"));
			entity.addPart("MAX_FILE_SIZE", new StringBody("512000"));
			entity.addPart("baseform", new StringBody(""));
			entity.addPart("pthb", new StringBody(posttime));
			entity.addPart("pthc", new StringBody(posttime));
			entity.addPart("pthd", new StringBody(posttime));
			entity.addPart("flvr", new StringBody(""));
			entity.addPart("scsz", new StringBody(""));
			//entity.addPart("textonly", new StringBody("on"));
			//entity.addPart("hash", new StringBody("1357485774338496-0050adb1005979e5402e80655b02a76d"));
			entity.addPart("js", new StringBody("on"));
			if (!newthread) {
				entity.addPart("resto", new StringBody("" + threadNum));
			}
			entity.addPart("com", new StringBody(comment, sjisCharset));
			entity.addPart("sub", new StringBody("")); //題名
			entity.addPart("pwd", new StringBody(deletekey));
			if (imageContent != null) { // content:// -> file://
				FLog.d("imageContent=" + imageContent);
				FLog.d("getPath=" + imageContent.getPath());
				// if(true) return;
				Cursor c = getContentResolver().query(imageContent, null, null,
						null, null);
				c.moveToFirst();
				String filename = c.getString(c
						.getColumnIndex(MediaStore.MediaColumns.DATA));
				FLog.d("filename=" + filename);
				FileBody fileBody = new FileBody(new File(filename));// new
																		// File(imageContent.getPath()));
				entity.addPart("upfile", fileBody);
			}
			httppost.setEntity(entity);
			// httppost.setEntity(new UrlEncodedFormEntity(nameValuePair,
			// "Shift-JIS"));
			httppost.addHeader("referer", threadURL+".htm");
			//httppost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0)");

			HttpResponse response = httpClient.execute(httppost);
			//FutabaCookieManager.saveCookie(httpClient);
			ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
			response.getEntity().writeTo(byteArrayOutputStream2);
			String retData2 = byteArrayOutputStream2.toString("Shift-JIS");
			// SDCard.saveBin("retdata2", retData2.getBytes("Shift-JIS"),
			// false);
			Log.v("ftbt", retData2);
			FLog.d("2nd access end");

			PostParser parser = new PostParser();
			String contents = parser.parse(this, retData2);
			if (!contents.equals("")) {
				Toast.makeText(this, contents, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "投稿しました", Toast.LENGTH_LONG).show();
			}
			setOnReturn();
		} catch (Exception e) {
			FLog.d("message", e);
		}
		waitDialog.dismiss();

		// スレッドに戻る
		finish();

	}

	// 戻ったときのインテントにパラメータ渡す(再読み込みするか判定のため)
	public void setOnReturn() {
		Intent ret_i = new Intent();
		ret_i.putExtra("posted", "true");
		setResult(RESULT_OK, ret_i);
	}
}
