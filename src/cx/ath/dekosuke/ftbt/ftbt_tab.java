package cx.ath.dekosuke.ftbt;

import android.app.Activity;
import android.os.Bundle;

//adding listview
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import android.util.Log;

//using Intent
import android.content.Intent;

import android.app.ProgressDialog;
import java.lang.Thread;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;

import cx.ath.dekosuke.ftbt.R.id;

public class ftbt_tab extends Activity implements Runnable {
	private ProgressDialog waitDialog;
	private Thread thread;

	private FutabaBBSMenuAdapter adapter = null;
	String mode;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		mode = (String) intent.getSerializableExtra("mode");

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
		setContentView(R.layout.main);

		Intent intent = getIntent();

		ArrayList<FutabaBBSContents> BBSs = new ArrayList<FutabaBBSContents>();
		if (mode.equals("all")) {
			FutabaBBSMenuParser parser = new FutabaBBSMenuParser(
					"http://www.2chan.net/bbsmenu.html");
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
			BBSs = parser.getBBSs();
			Log.d("ftbt", "BBSs_a" + BBSs.toString());
		} else { // fav
			ftbt parent_activity = (ftbt) this.getParent();
			Log.d("ftbt", "read hoge");
			BBSs = parent_activity.favoriteBBSs;
			Log.d("ftbt", "BBSs_f" + BBSs.toString());
		}
		adapter = new FutabaBBSMenuAdapter(this, R.layout.futaba_bbs_row, BBSs);
		// アイテムを追加します
		ListView listView = (ListView) findViewById(id.listview);
		// アダプターを設定します
		listView.setAdapter(adapter);

		Log.d("ftbt", "start");

		waitDialog.dismiss();
	}

	// 設定画面に遷移
	public void transSetting(FutabaBBSContents item) {
		Intent intent = new Intent();
		/*
		 * intent.setClassName(getPackageName(),
		 * getClass().getPackage().getName()+".catalog");
		 */
		Log.d("ftbt", item.url);
		intent.putExtra("baseUrl", item.url);
		intent.putExtra("mode", "normal");
		intent.setClassName(getPackageName(),
				getClass().getPackage().getName() + ".Catalog");
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			if (mode.equals("fav")) {
				ftbt parent_activity = (ftbt) this.getParent();
				if(adapter!=null){
					adapter.items = parent_activity.favoriteBBSs;
					adapter.notifyDataSetChanged();
				}
				Log.d("ftbt", "read hoge2");
			}
		} catch (Exception e) {
			Log.d("ftbt", "message", e);
		}
	}
}
