package cx.ath.dekosuke.ftbt;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
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
import android.view.View;
import cx.ath.dekosuke.ftbt.R.id;

//スレッド表示アクティビティ
public class fthread extends Activity implements Runnable {

	public ArrayList<FutabaStatus> statuses = null; // レス一覧
	private FutabaAdapter adapter = null;
	public String threadURL = null;

	private ProgressDialog waitDialog;
	private Thread thread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		threadURL = (String) intent.getSerializableExtra("baseUrl")
				+ (String) intent.getSerializableExtra("threadNum");
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
			statuses = parser.getStatuses();
			Log.d("ftbt", "parse end");

			setContentView(R.layout.futaba_thread);

			ListView listView = (ListView) findViewById(id.threadlistview);
			// アダプターを設定します
			adapter = new FutabaAdapter(this, R.layout.futaba_thread_row,
					statuses);
			listView.setAdapter(adapter);

		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}
		waitDialog.dismiss();
	}

	// スレッドに存在するすべての画像のURLを配列にして返す
	public ArrayList<String> getImageURLs() {
		Iterator iterator = statuses.iterator();
		int i = 0;
		ArrayList list = new ArrayList<String>();
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
		setWait();
	}

}
