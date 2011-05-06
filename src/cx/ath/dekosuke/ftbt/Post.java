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
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
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
	public String threadNum;
	public String threadURL;

	ProgressDialog waitDialog;
	Thread thread;

	//multipart 画像添付回り
	final int REQUEST_IMAGEPICK_CONSTANT = 0x100200;
	Uri imageContent = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		try {
			Log.d("ftbt", "start Post activity");
			Intent intent = getIntent();
			// urlStr = (String) intent.getSerializableExtra("urlStr");
			String baseURL = (String) intent.getSerializableExtra("baseURL");
			threadNum = (String) intent.getSerializableExtra("threadNum");
			threadURL = baseURL + threadNum;
			urlStr = baseURL + "futaba.php";
			String[] temp = threadNum.split("[/]");
			threadNum = temp[temp.length - 1];
			temp = threadNum.split("[.]");
			threadNum = temp[0];

			setContentView(R.layout.post);
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
			TableLayout tablelayout = (TableLayout) findViewById(id.tableLayout1);
			tablelayout.setColumnCollapsed(0, true);
			tablelayout.setColumnCollapsed(1, true);
			tablelayout.setColumnCollapsed(2, true);
			tablelayout.setColumnCollapsed(3, true);
			tablelayout.setColumnCollapsed(4, true);
			*/

			// cookie関連
			CookieSyncManager.createInstance(this);
			CookieSyncManager.getInstance().startSync();

			FutabaCookieManager.PrintCookie();

		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		CookieSyncManager.getInstance().stopSync();
	}

	@Override
	protected void onPause() {
		super.onPause();
		CookieSyncManager.getInstance().sync();
	}

	public void setWait() {
		waitDialog = new ProgressDialog(this);
		waitDialog.setMessage("ネットワーク接続中...");
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
				Log.d("ftbt", "message", e);
			}
		}
	};

	public void onClickPostButton(View v) {
		setWait();
	}

	
	public void onClickImageChooseButton(View v) {
		//画像選択ボタン
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, REQUEST_IMAGEPICK_CONSTANT);
	}
	
	//画像を選択した直後に呼ばれる
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGEPICK_CONSTANT) {
	    	Toast.makeText(this, "画像"+data.getData(), Toast.LENGTH_SHORT).show();
	    	/*
	        try{
	            InputStream is = getContentResolver().openInputStream(data.getData());
	            Bitmap bm = BitmapFactory.decodeStream(is);
	            is.close();
	        }catch(Exception e){
	            
	        }
	        */
	    	imageContent = data.getData();
			Button imgchoosebutton = (Button) findViewById(id.imgchoosebutton);
			imgchoosebutton.setVisibility(View.GONE); //画像選択ボタンを消す
			TextView imgchoosenotify = (TextView) findViewById(id.imgchoosenotify);
			imgchoosenotify.setText("選択:"+imageContent.toString()); //選択したファイル名を表示
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

		Log.d("ftbt", "threadNum=" + threadNum);
		Log.d("ftbt", "urlStr=" + urlStr);
		Log.d("ftbt", "comment=" + comment);
		Log.d("ftbt", "deletekey=" + deletekey);
		Log.d("ftbt", "email=" + email);
		Log.d("ftbt", "name=" + name);
		Log.d("ftbt", "threadURL=" + threadURL);

		if (false) {
			return;
		}

		// というわけでリクエストの作成
		// スレッドに一度アクセスしてcookieセット－＞書き込みの２度アクセス
		try {
			Log.d("ftbt", "start post!");

			DefaultHttpClient httpClient;
			httpClient = new DefaultHttpClient();
			// httpClient.setHeader( "Connection", "Keep-Alive" );
			FutabaCookieManager.loadCookie(httpClient);
			httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
					CookiePolicy.BROWSER_COMPATIBILITY);
			httpClient.getParams()
					.setParameter("http.connection.timeout", 5000);
			httpClient.getParams().setParameter("http.socket.timeout", 3000);

			/*
			 * HttpGet httpGet = new HttpGet(urlStr); HttpResponse httpResponse
			 * = null; httpResponse = httpClient.execute(httpGet);
			 * ByteArrayOutputStream byteArrayOutputStream = new
			 * ByteArrayOutputStream();
			 * httpResponse.getEntity().writeTo(byteArrayOutputStream); String
			 * retData = byteArrayOutputStream.toString("Shift-JIS");
			 * Log.d("ftbt", retData); Log.d("ftbt", "1st access end");
			 * 
			 * try { // 操作間隔を置く Thread.sleep(3000); } catch (Exception e) {
			 * Log.i("ftbt", "message", e); }
			 */

			try {
				// クッキー内容の取得
				{
					List<Cookie> cookies = httpClient.getCookieStore()
							.getCookies();
					if (cookies.isEmpty()) {
						Log.d("ftbt", "Cookie None");
					} else {
						for (int i = 0; i < cookies.size(); i++) {
							Log.d("ftbt", "" + cookies.get(i).toString());
						}
					}
				}
			} catch (Exception e) {
				Log.i("ftbt", "message", e);
			}

			HttpPost httppost = new HttpPost(urlStr);
			//添付画像
			if(false)
			{
				//multipartにはライブラリ追加が必要そう
				//http://www.softwarepassion.com/android-series-get-post-and-multipart-post-requests/
				//http://yakinikunotare.boo.jp/orebase/index.php?Android%A1%CA%B3%AB%C8%AF%A1%CB%2F%A5%CD%A5%C3%A5%C8%A5%EF%A1%BC%A5%AF%A4%F2%BB%C8%A4%C3%A4%C6%A5%D5%A5%A1%A5%A4%A5%EB%A4%F2%A5%A2%A5%C3%A5%D7%A5%ED%A1%BC%A5%C9%A4%B9%A4%EB
				//MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);  
				//reqEntity.addPart("myFile", bin);
				//FileBody fileBody = new FileBody(file);
				//entity.addPart("upfile", fileBody);
			}
			/*
			List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(7);
			nameValuePair.add(new BasicNameValuePair("email", email));
			nameValuePair.add(new BasicNameValuePair("name", name));
			nameValuePair.add(new BasicNameValuePair("mode", "regist"));
			nameValuePair.add(new BasicNameValuePair("resto", threadNum));
			nameValuePair.add(new BasicNameValuePair("com", comment));
			nameValuePair.add(new BasicNameValuePair("sub", "")); //題名
			nameValuePair.add(new BasicNameValuePair("pwd", deletekey));
			*/
			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			Charset sjisCharset = Charset.forName("Shift_JIS");
			entity.addPart("email", new StringBody(email, sjisCharset));
			entity.addPart("name", new StringBody(name, sjisCharset));
			entity.addPart("mode", new StringBody("regist"));
			entity.addPart("resto", new StringBody(threadNum));
			entity.addPart("com", new StringBody(comment, sjisCharset));
			entity.addPart("sub", new StringBody(""));
			entity.addPart("pwd", new StringBody(deletekey));
			if(imageContent!=null){ // content:// -> file://
				Log.d("ftbt", "imageContent="+imageContent);
				Log.d("ftbt", "getPath="+imageContent.getPath());
				//if(true) return;
				Cursor c = getContentResolver().query(imageContent, null, null, null, null);
				c.moveToFirst();	
				String filename = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA)); 
				Log.d("ftbt", "filename="+filename);
				FileBody fileBody = new FileBody(new File(filename));//new File(imageContent.getPath()));
				entity.addPart("upfile", fileBody);
			}
			httppost.setEntity(entity);
			//httppost.setEntity(new UrlEncodedFormEntity(nameValuePair, "Shift-JIS"));
			httppost.addHeader("referer", threadURL);
			HttpResponse response = httpClient.execute(httppost);
			FutabaCookieManager.saveCookie(httpClient);
			ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
			response.getEntity().writeTo(byteArrayOutputStream2);
			String retData2 = byteArrayOutputStream2.toString("Shift-JIS");
			// SDCard.saveBin("retdata2", retData2.getBytes("Shift-JIS"),
			// false);
			Log.v("ftbt", retData2);
			Log.d("ftbt", "2nd access end");

			PostParser parser = new PostParser();
			String contents = parser.parse(this, retData2);
			if (!contents.equals("")) {
				Toast.makeText(this, contents, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "投稿しました", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}
		waitDialog.dismiss();

		// スレッドに戻る
		finish();

	}
}
