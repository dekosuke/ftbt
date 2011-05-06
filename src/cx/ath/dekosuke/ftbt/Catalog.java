package cx.ath.dekosuke.ftbt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.webkit.CookieSyncManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
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
import java.net.UnknownHostException;

import android.os.Handler;
import android.os.Message;

import android.widget.Button;

import cx.ath.dekosuke.ftbt.R.id;

//板カタログ表示アクティビティ
public class Catalog extends Activity implements OnClickListener, Runnable {

	private ArrayList<FutabaThreadContent> fthreads = new ArrayList<FutabaThreadContent>();
	private CatalogParser parser;
	private CatalogAdapter adapter = null;
	public String baseUrl = "";
	private String catalogURL;
	private ProgressDialog waitDialog;
	private Thread thread;
	private Button buttonReload;
	//private ListView listView;
	private String BBSName = ""; // 板名

	// 履歴モードか通常モードか
	public String mode;

	int position = 0; // 現在位置(リロード時復帰用)

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
		try {
			Log.d("ftbt", "Catalog::onResume");
			if(adapter!=null){
				adapter.notifyDataSetChanged();
				ListView listView = (ListView) findViewById(id.cataloglistview);
				listView.invalidate();
			}
		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}
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
			BBSName = (String) intent.getSerializableExtra("BBSName");
			mode = (String) intent.getSerializableExtra("mode");
			catalogURL = baseUrl + "futaba.php";
			buttonReload = new Button(this);
			buttonReload.setText("Reload");
			buttonReload.setOnClickListener(this);
			parser = new CatalogParser();

			setContentView(R.layout.futaba_catalog);
			ListView listView = (ListView) findViewById(id.cataloglistview);

			if (!mode.equals("history")) { // 通常
				String catalogHtml = "";
				Boolean network_ok = true;
				Boolean cache_ok = true;
				try {
					catalogHtml = CatalogHtmlReader.Read(catalogURL);
					network_ok = true;
				} catch (UnknownHostException e) { // ネット繋がってない

					//Log.d("ftbt", "message", e);
					network_ok = false;
					if (SDCard.cacheExist(FutabaCrypt.createDigest(catalogURL))) {
						Log.d("ftbt",
								"getting html from cache"
										+ FutabaCrypt.createDigest(catalogURL));
						catalogHtml = SDCard.loadTextCache(FutabaCrypt
								.createDigest(catalogURL));
					} else {
						Log.d("ftbt",
								"cache " + FutabaCrypt.createDigest(catalogURL)
										+ "not found");
						cache_ok = false;
					}
				} catch (Exception e) { // その他エラー
					network_ok = cache_ok = false;
					Log.d("ftbt", "message", e);
				}
				if (!network_ok) {
					if (cache_ok) {
						Toast.makeText(this,
								"ネットワークに繋がっていません。代わりに前回読み込み時のキャッシュを使用します",
								Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(this, "ネットワークに繋がっていません",
								Toast.LENGTH_LONG).show();
					}
				}

				if (network_ok || cache_ok) {
					parser.parse(catalogHtml);
					fthreads = parser.getThreads();
				}

				// BBS名足す
				for (int i = 0; i < fthreads.size(); ++i) {
					fthreads.get(i).BBSName = BBSName;
				}

				Button historyDeleteButton = (Button) findViewById(id.delete_btn);
				historyDeleteButton.setVisibility(View.GONE);
				TextView nonclickableblank = (TextView) findViewById(id.nonclickableblank);
				nonclickableblank.setVisibility(View.GONE);
			} else { // 履歴モード。複数板混在なので注意
				HistoryManager man = new HistoryManager();
				man.Load();
				fthreads = man.getThreadsArray();
				
				//通常モードのときのボタンを非表示に
				Button reloadButton = (Button) findViewById(id.reload_btn);
				reloadButton.setVisibility(View.GONE);
				Button historyButton = (Button) findViewById(id.history_btn);
				historyButton.setVisibility(View.GONE);
			}

			// アダプターを設定します
			adapter = new CatalogAdapter(this, R.layout.futaba_catalog_row,
					fthreads);
			listView.setAdapter(adapter);
			if (position != 0) {
				listView.setSelection(Math.min(position, listView.getCount()));
			}

			waitDialog.dismiss();
		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}
	}

	public void onClickReloadBtn(View v) {
		Log.d("ftbt", "catalog onclick-reload");
		ListView listView = (ListView) findViewById(id.cataloglistview);
		position = listView.getFirstVisiblePosition();
		; // 現在位置（リロードで復帰）
		setWait();
	}

	// 履歴モードに
	public void onClickHistoryBtn(View v) {
		Intent intent = new Intent();
		intent.putExtra("baseUrl", baseUrl);
		// 履歴モードは全板共通だが、どこから着たのか保持のため一応持ってる
		intent.putExtra("BBSName", BBSName);
		intent.putExtra("mode", "history");
		intent.setClassName(getPackageName(), getClass().getPackage().getName()
				+ ".Catalog");
		startActivity(intent);
	}

	int delete_option = 0;
	final int DELETE_CHECKED = 0;
	final int DELETE_NONCHECKED = 1;

	// 履歴削除ボタン
	public void onClickDeleteBtn(View v) {
		final CharSequence[] items = { "チェック有りのスレ", "チェック無しのスレ" };
		AlertDialog.Builder dlg;
		dlg = new AlertDialog.Builder(Catalog.this);
		dlg.setTitle("スレッド履歴の削除");
		dlg.setCancelable(true);
		dlg.setSingleChoiceItems(items, 0,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						// button.setText(String.format("%sが選択されました。",items[item]));
						Catalog.this.delete_option = item;
					}
				});
		dlg.setPositiveButton("削除する", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Catalog.this.finish();
				Catalog.this.deleteThreads();
			}
		});
		dlg.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Catalog.cancel();
				// Catalog.this.deleteThreads(false);
			}
		});
		dlg.show();
	}

	// チェックされたスレッド、もしくはチェックされていないスレッドを削除する
	public void deleteThreads() {
		try {
			ListView listView = (ListView) findViewById(id.cataloglistview);
			Log.d("ftbt", "delete threads with option " + delete_option);
			if (delete_option == DELETE_CHECKED) {
				for (int i = listView.getCount() - 1; i >= 0; --i) {
					View view = listView.getChildAt(i);
					CheckBox checkbox = (CheckBox) view
							.findViewById(R.id.checkbox);
					if (checkbox.isChecked()) {
						Log.d("ftbt", "delete element at " + i);
						adapter.items.remove(i);
					}
				}
			} else if (delete_option == DELETE_NONCHECKED) {
				for (int i = listView.getCount() - 1; i >= 0; --i) {
					View view = listView.getChildAt(i);
					CheckBox checkbox = (CheckBox) view
							.findViewById(R.id.checkbox);
					if (!checkbox.isChecked()) {
						Log.d("ftbt", "delete element at " + i);
						adapter.items.remove(i);
					}
				}
			}
			HistoryManager man = new HistoryManager();
			man.set(adapter.items);
			man.Save();
			// adapter.items = man.getThreadsArray();
			// DesireHDで動かしてて分かったけど
			// adapter.itemの参照アドレスが変わる->notifyDataSetChanged で落ちる
			adapter.notifyDataSetChanged();
			listView.invalidateViews();
		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}
	}

	public void onClick(View v) {
		Log.d("ftbt", "catalog onclick");
		// v.reload();
	}

	// メニュー
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_catalog, menu);
		return true;
	}

	// メニューをクリック
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
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

}
