package cx.ath.dekosuke.ftbt;

import android.app.Activity;
import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.content.Intent;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.DefaultedHttpContext;
import org.apache.http.protocol.HttpContext;

import android.app.ProgressDialog;
import java.lang.Thread;
import android.os.Handler;
import android.os.Message;

import android.widget.ListView;
import android.widget.Toast;
import android.view.View;
import cx.ath.dekosuke.ftbt.R.id;

//スレッド表示アクティビティ
public class fthread extends Activity implements Runnable {

	public ArrayList<FutabaStatus> statuses = null; // レス一覧
	private FutabaThreadAdapter adapter = null;
	public String threadURL = null;
	public String baseURL = null;
	public String threadNum = null;

	private ProgressDialog waitDialog;
	private Thread thread;

	private ListView listView;

	int position = 0; // 現在位置(リロード時復帰用)
	
	//画像カタログから戻ってきたときにどの画像から戻ってきたか判定用
	final int TO_IMAGECATALOG=0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		baseURL = (String) intent.getSerializableExtra("baseUrl");
		threadNum = (String) intent.getSerializableExtra("threadNum");
		threadURL = baseURL + threadNum;
		setWait();
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

	private void loading() {
		try {
			statuses = new ArrayList<FutabaStatus>();
			FutabaThreadParser parser = new FutabaThreadParser(threadURL);
			parser.parse();
			if (!parser.network_ok) {
				if (parser.cache_ok) {
					Toast.makeText(this,
							"ネットワークに繋がっていません。代わりに前回読み込み時のキャッシュを使用します",
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(this, "ネットワークに繋がっていません", Toast.LENGTH_LONG)
							.show();
				}
			}
			statuses = parser.getStatuses();
			Log.d("ftbt", "parse end");

			setContentView(R.layout.futaba_thread);

			listView = (ListView) findViewById(id.threadlistview);
			// アダプターを設定します
			adapter = new FutabaThreadAdapter(this, R.layout.futaba_thread_row,
					statuses);
			listView.setAdapter(adapter);
			if (position != 0) {
				listView.setSelection(Math.min(position, listView.getCount()));
			}

		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}
		waitDialog.dismiss();
	}

	// スレッドに存在するすべての画像のURLを配列にして返す
	public ArrayList<String> getImageURLs() {
		Iterator iterator = statuses.iterator();
		int i = 0;
		ArrayList<String> list = new ArrayList<String>();
		// ループ
		while (iterator.hasNext()) {
			FutabaStatus status = (FutabaStatus) iterator.next();
			if (status.bigImgURL != null) {
				Log.d("ftbt", "image" + status.bigImgURL);
				list.add(status.bigImgURL);
			}
			i++;
		}
		return list;
	}

	public void onClickReloadBtn(View v) {
		Log.d("ftbt", "fthread onclick-reload");
		position = listView.getFirstVisiblePosition();
		; // 現在位置（リロードで復帰）
		Log.d("ftbt", "position=" + position);
		setWait();
	}

	public void onClickPostBtn(View v) {
		// Toast.makeText(this, "投稿ボタンが押されました", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent();
		// Log.d ( "ftbt", threadNum );
		intent.putExtra("baseURL", baseURL);
		intent.putExtra("threadNum", threadNum);
		intent.setClassName(getPackageName(), getClass().getPackage().getName()
				+ ".Post");
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	//メニューをクリック
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.tweet:
			Toast.makeText(this, "簡易ブラウザ画面に移動します", Toast.LENGTH_SHORT).show();
			String thread_title = statuses.get(0).text;
			thread_title = thread_title.substring(0, Math.min(50, thread_title.length()));
			//threadURL
			String hashTag = " #fromandroid";
			String status_encoded = "てすてす"; //URIエンコードされた、ツイートしたい文章
			//Uri uri = Uri.parse("http://twitter.com/?status="+status_encoded);
			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, status_encoded);
			try{
			  startActivityForResult(intent, 0);
			}
			catch (android.content.ActivityNotFoundException ex) {
			  Toast.makeText(this, "client not found", Toast.LENGTH_LONG).show();
			}
			return true;
		case R.id.settings:
			intent = new Intent();
			intent.setClassName(getPackageName(), getClass().getPackage()
					.getName() + ".PrefSetting");
			startActivity(intent);
			return true;
		case R.id.about:
			Toast.makeText(this, "about", Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		try{
			String imgURL = (String) intent.getSerializableExtra("imgURL");
			//Log.d("ftbt", "return intent imgURL="+imgURL);
			if(requestCode==TO_IMAGECATALOG){
				for(int i=0;i<statuses.size();++i){
					//Log.d("ftbt", "image"+i+"="+statuses.get(i).bigImgURL);
					if(imgURL.equals(statuses.get(i).bigImgURL)){
						//Log.d("ftbt", "hit="+i);
						listView.setSelection(Math.min(i, listView.getCount()));
						break;
					}
				}
			}else{
				Log.d("ftbt", "unknown result code");
			}
		}catch(Exception e){
			Log.i("ftbt", "message", e);
		}
	}
}
