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
import android.content.SharedPreferences;

import android.app.ProgressDialog;
import java.lang.Thread;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import cx.ath.dekosuke.ftbt.R.id;

public class FutabaBBSMenu extends Activity implements Runnable {
	private ProgressDialog waitDialog;
	private Thread thread;

	public FutabaBBSMenuAdapter adapter = null;
	public boolean initial_loading = true;
	String mode;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			Intent intent = getIntent();
			mode = (String) intent.getSerializableExtra("mode");

			setWait();
			// loading();
		} catch (Exception e) {
			FLog.d("message", e);
		}
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
				FLog.d("message", e);
			}
		}
	};

	private void loading() {

		try {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(this);
			boolean dispCensored = preferences.getBoolean(
					getString(R.string.dispcensored), false);
			

			setContentView(R.layout.bbsmenu);
			Intent intent = getIntent();

			ArrayList<FutabaBBSContent> BBSs = new ArrayList<FutabaBBSContent>();
			if (mode == null || mode.equals("all")) {
				FutabaBBSMenuParser parser = new FutabaBBSMenuParser(
						"http://www.4chan.org/bbsmenu.html");
				parser.setDisplayCensored(dispCensored);
				parser.parse();
				if (!parser.network_ok) {
					if (parser.cache_ok) {
						Toast.makeText(this,
								"ネットワークに繋がっていません。代わりに前回読み込み時のキャッシュを使用します",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(this, "ネットワークに繋がっていません",
								Toast.LENGTH_SHORT).show();
					}
				}
				BBSs = parser.getBBSs();
				if (initial_loading) {
					initial_loading = false;
					ftbt parent_activity = (ftbt) this.getParent();
					ArrayList<FutabaBBSContent> BBSs_faved = parent_activity.favoriteBBSs;
					HashSet<String> BBS_urls = new HashSet<String>();
					for (int i = 0; i < BBSs_faved.size(); i++) {
						BBS_urls.add(BBSs_faved.get(i).url);
					}
					for (int i = 0; i < BBSs.size(); i++) {
						if (BBS_urls.contains(BBSs.get(i).url)) {
							BBSs.get(i).faved = true;
						}
					}
				}
			} else { // fav
				ftbt parent_activity = (ftbt) this.getParent();
				BBSs = parent_activity.favoriteBBSs;
			}
			adapter = new FutabaBBSMenuAdapter(this, R.layout.futaba_bbs_row,
					BBSs);
			// アイテムを追加します
			ListView listView = (ListView) findViewById(id.listview);
			// アダプターを設定します
			listView.setAdapter(adapter);

			FLog.d("start");
		} catch (Exception e) {
			FLog.d("message", e);
		}
		waitDialog.dismiss();
	}

	// 設定画面に遷移
	public void transSetting(FutabaBBSContent item) {
		Intent intent = new Intent();
		/*
		 * intent.setClassName(getPackageName(),
		 * getClass().getPackage().getName()+".catalog");
		 */
		FLog.d(item.url);
		intent.putExtra("baseUrl", item.url);
		intent.putExtra("BBSName", item.name);
		intent.putExtra("mode", "normal");
		intent.setClassName(getPackageName(), getClass().getPackage().getName()
				+ ".Catalog");
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			if (mode.equals("fav")) {
				ftbt parent_activity = (ftbt) this.getParent();
				if (adapter != null) {
					adapter.items = parent_activity.favoriteBBSs;
					adapter.notifyDataSetChanged();
				}
			}
		} catch (Exception e) {
			FLog.d("message", e);
		}
		FLog.d("BBSMenu onResume");
	}
}
