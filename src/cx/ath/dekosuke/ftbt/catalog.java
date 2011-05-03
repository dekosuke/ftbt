package cx.ath.dekosuke.ftbt;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.content.Intent;
import android.webkit.CookieSyncManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import android.content.Intent;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

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

import android.widget.Button;

import cx.ath.dekosuke.ftbt.R.id;

//板カタログ表示アクティビティ
public class catalog extends Activity implements OnClickListener, Runnable {

	private ArrayList<FutabaThread> fthreads = null;
	private FutabaCatalogParser parser;
	private FutabaCatalogAdapter adapter = null;
	public String baseUrl = "";
	private String catalogURL;
	private ProgressDialog waitDialog;
	private Thread thread;
	private Button buttonReload;
	private ListView listView;

	int position = 0; //現在位置(リロード時復帰用)

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d("ftbt", "catalog start");

		CookieSyncManager.createInstance(this);
		CookieSyncManager.getInstance().startSync();
		
		setWait();

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

				// プログレスダイアログ終了
			} catch (Exception e) {
				Log.d("ftbt", "message", e);
			}
		}
	};

	private void loading() {
		try {
			Intent intent = getIntent();
			baseUrl = (String) intent.getSerializableExtra("baseUrl");
			catalogURL = baseUrl + "futaba.php";
			buttonReload = new Button(this);
			buttonReload.setText("Reload");
			buttonReload.setOnClickListener(this);
			parser = new FutabaCatalogParser(catalogURL);
			parser.parse(getApplicationContext());
			Log.d("ftbt", " " + parser.network_ok + " " + parser.cache_ok);
			if (!parser.network_ok) {
				if(parser.cache_ok){
					Toast.makeText(this,
						"ネットワークに繋がっていません。代わりに前回読み込み時のキャッシュを使用します",
						Toast.LENGTH_LONG).show();
				}else{
					Toast.makeText(this,
							"ネットワークに繋がっていません",
							Toast.LENGTH_LONG).show();
				}
			}
			fthreads = parser.getThreads();

			setContentView(R.layout.futaba_catalog);

			listView = (ListView) findViewById(id.cataloglistview);
			// アダプターを設定します
			adapter = new FutabaCatalogAdapter(this,
					R.layout.futaba_catalog_row, fthreads);
			listView.setAdapter(adapter);
			if(position!=0){
				listView.setSelection(position);
			}

			waitDialog.dismiss();
		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}
	}

	public void onClickReloadBtn(View v) {
		Log.d("ftbt", "catalog onclick-reload");
		position = adapter.currentPosition;
		setWait();
	}

	public void onClick(View v) {
		Log.d("ftbt", "catalog onclick");
		// v.reload();
	}

}
