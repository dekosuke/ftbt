package cx.ath.dekosuke.ftbt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.webkit.CookieSyncManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

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
import android.preference.PreferenceManager;

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
	private int sortType = 0;
	// private ListView listView;
	public String BBSName = ""; // 板名
	private ListView listView;
	public ArrayList<String> focusWords = new ArrayList<String>();

	// 履歴モードか通常モードか
	public String mode;

	int position = 0; // 現在位置(リロード時復帰用)

	boolean onCreateEnd = false;
	HistoryManager man = new HistoryManager();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FLog.d("catalog start");

		CookieSyncManager.createInstance(this);
		CookieSyncManager.getInstance().startSync();

		// 無操作で暗くなるのを防ぐ
		if (getResources().getBoolean(R.bool.avoidsleep)) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		setWait();

	}

	@Override
	protected void onResume() {
		super.onResume();
		CookieSyncManager.getInstance().stopSync();
		try {
			FLog.d("Catalog::onResume");
			if (adapter != null) {
				adapter.notifyDataSetChanged();
			}
			if (onCreateEnd && mode.equals("history")) { // 履歴カター＞スレ－＞履歴カタで履歴の並びが変わっている可能性あり
				if (adapter != null) {
					setWait();
				}
			}
		} catch (Exception e) {
			FLog.d("message", e);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		System.gc();
		CookieSyncManager.getInstance().sync();
	}

	public void setWait() {
		if (waitDialog != null) {
			waitDialog.dismiss();
		}
		waitDialog = new ProgressDialog(this);
		waitDialog.setMessage(this.getString(R.string.loading));
		waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// waitDialog.setCancelable(true);
		waitDialog.show();

		try {
			sortType = StateMan.getSortParam(this);
		} catch (Exception e) {
			FLog.d("message", e);
		}

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
				FLog.d("message", e);
			}
		}
	};

	final Handler handler2 = new Handler();

	private void loading() {
		Intent intent = getIntent();
		baseUrl = (String) intent.getSerializableExtra("baseUrl");
		BBSName = (String) intent.getSerializableExtra("BBSName");
		mode = (String) intent.getSerializableExtra("mode");
		catalogURL = baseUrl + "futaba.php";
		setContentView(R.layout.futaba_catalog);
		listView = (ListView) findViewById(id.cataloglistview);
		
		//listView.setSmoothScrollbarEnabled(true);
		try {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(this);
			boolean fastScrollEnabled = preferences.getBoolean(
					"fastscrollenable", false);
			listView.setFastScrollEnabled(fastScrollEnabled);
		}catch(Exception e){
			FLog.d("message", e);
		}
		
		adapter = new CatalogAdapter(this, R.layout.futaba_catalog_row,
				(ArrayList<FutabaThreadContent>) fthreads.clone());
		// 通常モード・履歴モードの片方でしか使わないボタンを消す
		if (!mode.equals("history")) { // 通常モード
			Button historyDeleteButton = (Button) findViewById(id.delete_btn);
			historyDeleteButton.setVisibility(View.GONE);
		} else { // 履歴モード
			// 通常モードのときのボタンを非表示に
			Button reloadButton = (Button) findViewById(id.reload_btn);
			reloadButton.setVisibility(View.GONE);
			Button historyButton = (Button) findViewById(id.history_btn);
			historyButton.setVisibility(View.GONE);
			Button sortTypeButton = (Button) findViewById(id.sorttype_btn);
			sortTypeButton.setVisibility(View.GONE);
		}

		// EditTextでenter押したときのイベント取る
		// http://android-vl0c0lv.blogspot.com/2009/08/edittext.html
		// このくらい簡単な処理書くのに行数多すぎるだろjk...
		EditText searchWord = (EditText) findViewById(R.id.searchinput);
		searchWord.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				FLog.d("onKey" + event);
				// ここではEditTextに改行が入らないようにしている。
				if (event.getAction() == KeyEvent.ACTION_DOWN
						&& keyCode == KeyEvent.KEYCODE_ENTER) {
					return true;
				}
				// Enterを離したときに検索処理を実行
				if (event.getAction() == KeyEvent.ACTION_UP
						&& keyCode == KeyEvent.KEYCODE_ENTER) {
					EditText word = (EditText) findViewById(R.id.searchinput);
					Catalog activity = (Catalog) v.getContext();
					activity.onClickSearchButton(v);
					return false;
				}
				return false;
			}
		});

		/*
		 * ( LinearLayout footer = new LinearLayout(this);
		 * footer.setLayoutParams(createParam(0, 40));
		 * listView.addFooterView(footer);
		 */
		Button btn = new Button(this);
		btn.setText("hoge\fuga");
		btn.setVisibility(View.INVISIBLE);
		// listView.addFooterView(btn);
		listView.setAdapter(adapter);

		LinearLayout searchBar = (LinearLayout) findViewById(id.search_bar);
		searchBar.setVisibility(View.GONE);

		new Thread(new FutabaCatalogContentGetter()).start();
		// FutabaThreadContentGetter getterThread = new
		// FutabaThreadContentGetter();
		// getterThread.start();
	}

	private LinearLayout.LayoutParams createParam(int w, int h) {
		return new LinearLayout.LayoutParams(w, h);
	}

	private class FutabaCatalogContentGetter extends Thread {
		@Override
		public void run() {
			try {
				fthreads = new ArrayList<FutabaThreadContent>();

				parser = new CatalogParser();

				String title_text = "";
				String toast_text = "";

				if (!mode.equals("history")) { // 通常
					String catalogHtml = "";
					Boolean network_ok = true;
					Boolean cache_ok = true;
					try {
						catalogHtml = CatalogHtmlReader.Read(catalogURL,
								adapter.getContext(), sortType);
						network_ok = true;
					} catch (UnknownHostException e) { // ネット繋がってない(これ以外も色々あり)
						FLog.d("hoge");

						// FLog.d("message", e);
						network_ok = false;
						if (SDCard.cacheExist(FutabaCrypt
								.createDigest(catalogURL))) {
							FLog.d("getting html from cache"
									+ FutabaCrypt.createDigest(catalogURL));
							catalogHtml = SDCard.loadTextCache(FutabaCrypt
									.createDigest(catalogURL));
						} else {
							FLog.d("cache "
									+ FutabaCrypt.createDigest(catalogURL)
									+ "not found");
							cache_ok = false;
						}
					} catch (Exception e) { // その他エラー
						network_ok = false;
						if (SDCard.cacheExist(FutabaCrypt
								.createDigest(catalogURL))) {
							FLog.d("getting html from cache"
									+ FutabaCrypt.createDigest(catalogURL));
							catalogHtml = SDCard.loadTextCache(FutabaCrypt
									.createDigest(catalogURL));
						} else {
							FLog.d("cache "
									+ FutabaCrypt.createDigest(catalogURL)
									+ "not found");
							cache_ok = false;
						}

						FLog.d("message", e);
					}
					if (!network_ok) {
						if (cache_ok) {
							toast_text = "ネットワークに繋がっていません。代わりに前回読み込み時のキャッシュを使用します";
						} else {
							toast_text = "ネットワークに繋がっていません";
						}
					}

					if (network_ok || cache_ok) {
						FLog.d("network=" + network_ok + " cache=" + cache_ok
								+ " length=" + catalogHtml.length());
						if (catalogHtml.length() > 50) { // あまりに小さいと失敗っぽいので弾く・・
							parser.parse(catalogHtml);
							fthreads = parser.getThreads();
						} else {
							toast_text = "データの取得に失敗しました";
						}
					}

					// BBS名足す
					for (int i = 0; i < fthreads.size(); ++i) {
						fthreads.get(i).BBSName = BBSName;
					}

					// どちらにしろ必要に
					man.Load();

					title_text = BBSName + "(カタログ) - "
							+ getString(R.string.app_name);

				} else { // 履歴モード。複数板混在なので注意
					man.Load();

					fthreads = man.getThreadsArray();

					title_text = "履歴 - " + getString(R.string.app_name);
				}

				/*
				 * if (position != 0) { listView.setSelection(Math.min(position,
				 * listView.getCount())); }
				 */
				focusWords = FocusedSettings.get(adapter.getContext());
				adapter.items.clear();
				if (!mode.equals("history") && focusWords.size() > 0) { // 通常
					adapter.items.add(FutabaThreadContent.createMenu1());
					Iterator<FutabaThreadContent> iterator = fthreads
							.iterator();
					while (iterator.hasNext()) {
						FutabaThreadContent athread = iterator.next();
						if (StringUtil.focusWordMatched(athread.text,
								focusWords)) {
							adapter.items.add(athread);
						}
					}
					adapter.items.add(FutabaThreadContent.createMenu2());
					iterator = fthreads.iterator();
					while (iterator.hasNext()) {
						FutabaThreadContent athread = iterator.next();
						if (!StringUtil.focusWordMatched(athread.text,
								focusWords)) {
							adapter.items.add(athread);
						}
					}
				} else { // 履歴モード or キーワード登録0
					Iterator<FutabaThreadContent> iterator = fthreads
							.iterator();
					while (iterator.hasNext()) {
						FutabaThreadContent athread = iterator.next();
						adapter.items.add(athread);
					}
				}

				final String title_text_f = title_text;
				final String toast_text_f = toast_text;

				// 描画に関わる処理はここに集約(メインスレッド実行)
				handler2.post(new Runnable() {
					public void run() {
						if (!toast_text_f.equals("")) {
							Toast.makeText(adapter.getContext(), toast_text_f,
									Toast.LENGTH_SHORT).show();
						}
						setTitle(title_text_f);
						waitDialog.dismiss();
						adapter.notifyDataSetChanged();
						listView.invalidateViews();
					}
				});
				onCreateEnd = true;
			} catch (Exception e) {
				FLog.i("message", e);
			}
		}
	}

	public void onClickReloadBtn(View v) {
		FLog.d("catalog onclick-reload");
		ListView listView = (ListView) findViewById(id.cataloglistview);
		// Button reloadButton = (Button) findViewById(R.id.reload_btn);
		// reloadButton.setPressed(true);//setgetResources().getDrawable(R.drawable.ic_popup_sync));
		position = listView.getFirstVisiblePosition();
		// 現在位置（リロードで復帰）
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

	private int delete_option = 0;
	final int DELETE_CHECKED = 0;
	final int DELETE_NONCHECKED = 1;
	final int DELETE_ALL = 2;

	// 履歴削除ボタン
	public void onClickDeleteBtn(View v) {
		if (adapter.items.size() != fthreads.size()) {
			Toast.makeText(this, "検索絞り込み中は、スレッドを削除できません", Toast.LENGTH_LONG)
					.show();
			return;
		}
		delete_option = 0; //必要(これないと、チェックなしすれ削除→チェックありすれ削除とかで困る）
		final CharSequence[] items = { "チェック有りのスレ", "チェック無しのスレ", "すべてのスレ" };
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
			// http://stackoverflow.com/questions/257514/android-access-child-views-from-a-listview
			// 見えてる場所しか消せないぽいよ？・・->とても頑張ればいける
			if (delete_option == DELETE_ALL) {
				adapter.items.clear();
			} else {
				if (delete_option == DELETE_CHECKED) {
					for (int i = adapter.items.size() - 1; i >= 0; --i) {
						// チェックされたアイテム（画面外はチェック消される）を削除
						if (adapter.items.get(i).isChecked) {
							adapter.items.remove(i);
						} else {
							// チェック状態戻す
							adapter.items.get(i).isChecked = false;
						}
					}
				} else { // チェックされていないアイテム（画面外含む）を削除
					for (int i = adapter.items.size() - 1; i >= 0; --i) {
						if (!adapter.items.get(i).isChecked) {
							adapter.items.remove(i);
						} else {
							// チェック状態戻す
							adapter.items.get(i).isChecked = false;
						}
					}
				}
			}
			HistoryManager man = new HistoryManager();
			man.set(adapter.items);
			man.Save();
			fthreads = (ArrayList<FutabaThreadContent>) adapter.items.clone();
			// adapter.items = man.getThreadsArray();
			// DesireHDで動かしてて分かったけど
			// adapter.itemの参照アドレスが変わる->notifyDataSetChanged で落ちる
			adapter.notifyDataSetChanged();
			listView.invalidateViews();
		} catch (Exception e) {
			FLog.i("message", e);
		}
	}

	public void onClick(View v) {
		FLog.d("catalog onclick");
		// v.reload();
	}

	@Override
	public void onDestroy() {
		FLog.d("Catalog::onDestoy(), System.gc will be called");
		System.gc(); // GC促す
		super.onDestroy();
	}

	// メニュー
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_catalog, menu);
		return true;
	}

	public void onClickPostBtn(View v) {
		// Toast.makeText(this, "投稿ボタンが押されました", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent();
		intent.putExtra("baseURL", baseUrl);
		intent.putExtra("threadNum", 0);
		intent.setClassName(getPackageName(), getClass().getPackage().getName()
				+ ".Post");
		startActivity(intent);
	}

	public void onClickSortTypeBtn(View v) {
		// Toast.makeText(this, "ソート選択ボタンが押されました", Toast.LENGTH_SHORT).show();
		final String[] strs = { "カタログ", "新順", "古順", "多順", "少順" };
		AlertDialog.Builder dlg;
		final Catalog catalog = this;
		dlg = new AlertDialog.Builder(this);
		dlg.setTitle("ソート方法の選択");
		// dlg.setMessage("クリップボードにコピーするテキストを選択してください");
		dlg.setCancelable(true);
		dlg.setSingleChoiceItems(strs, sortType,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						// button.setText(String.format("%sが選択されました。",items[item]));
						sortType = item; // この実装こういうものなんですかね・・・
					}
				});
		dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				FLog.d("sortType=" + sortType);
				if (sortType >= 0 && sortType < strs.length) {
					StateMan.setSortParam(catalog, sortType);
					catalog.onClickReloadBtn(null);
				}
			}
		});
		dlg.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		dlg.show();

	}

	// メニューをクリック
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.post:
			onClickPostBtn(null);
			return true;
		case R.id.share:
			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, baseUrl);
			try {
				startActivityForResult(intent, 0);
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(this, "client not found", Toast.LENGTH_SHORT)
						.show();
			}
			return true;
		case R.id.search:
			LinearLayout searchBar = (LinearLayout) findViewById(id.search_bar);
			if (searchBar.getVisibility() == View.GONE) {
				searchBar.setVisibility(View.VISIBLE);
			} else {
				searchBar.setVisibility(View.GONE);
			}
			return true;
		case R.id.settings:
			intent = new Intent();
			intent.setClassName(getPackageName(), getClass().getPackage()
					.getName() + ".PrefSetting");
			startActivity(intent);
			return true;
		case R.id.about:
			Uri uri = Uri.parse(getString(R.string.helpurl));
			intent = new Intent(Intent.ACTION_VIEW, uri);
			intent.setClassName("com.android.browser",
					"com.android.browser.BrowserActivity");
			try {
				startActivity(intent);
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(this, "ブラウザが見つかりません", Toast.LENGTH_SHORT).show();
			}
			return true;
		case R.id.focusword:
			intent = new Intent();
			intent.setClassName(getPackageName(), getClass().getPackage()
					.getName() + ".FocusEdit");
			startActivity(intent);
			return true;
		}
		return false;
	}

	public void onClickSearchButton(View v) {
		// Toast.makeText(this, "検索ボタンが押されました", Toast.LENGTH_SHORT).show();
		FLog.d("fthreads size=" + fthreads.size());
		if (true) {
			EditText searchEdit = (EditText) findViewById(id.searchinput);
			String searchText = searchEdit.getText().toString(); // これでいいんだろうか
			String[] query = StringUtil.queryNormalize(searchText);
			adapter.items.clear();

			try {
				ArrayList<String> focusWords = FocusedSettings.get(this);
				adapter.items.clear();
				if (!mode.equals("history") && focusWords.size() > 0) { // 通常
					adapter.items.add(FutabaThreadContent.createMenu1());
					Iterator<FutabaThreadContent> iterator = fthreads
							.iterator();
					while (iterator.hasNext()) {
						FutabaThreadContent athread = iterator.next();
						if (StringUtil.isQueryMatch(athread.text, query)
								&& StringUtil.focusWordMatched(athread.text,
										focusWords)) {
							adapter.items.add(athread);
						}
					}
					adapter.items.add(FutabaThreadContent.createMenu2());
					iterator = fthreads.iterator();
					while (iterator.hasNext()) {
						FutabaThreadContent athread = iterator.next();
						if (StringUtil.isQueryMatch(athread.text, query)
								&& !StringUtil.focusWordMatched(athread.text,
										focusWords)) {
							adapter.items.add(athread);
						}
					}
				} else { // 履歴モード or キーワード登録0
					Iterator<FutabaThreadContent> iterator = fthreads
							.iterator();
					while (iterator.hasNext()) {
						FutabaThreadContent athread = iterator.next();
						if (StringUtil.isQueryMatch(athread.text, query)) {
							adapter.items.add(athread);
						}
					}
				}
			} catch (Exception e) {
				FLog.d("message", e);
			}

			int itemnum = adapter.items.size();
			if (!mode.equals("history") && focusWords.size() > 0) { // 通常
				itemnum -= 2; // メニューぶん減らす
			}
			adapter.notifyDataSetChanged(); // 再描画命令
			LinearLayout searchBar = (LinearLayout) findViewById(id.search_bar);
			// searchBar.setVisibility(View.GONE);
			String toastText = "全" + fthreads.size() + "スレッド中、" + itemnum
					+ "スレッドを表示します";
			if (fthreads.size() == itemnum) {
				toastText = "すべてのスレッドを表示します";
			} else if (itemnum == 0) {
				toastText = "該当するスレッドは見つかりませんでした";
			}
			Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();

			// ソフトウェアキーボードかくす
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

		}
	}

	public void onClickSearchHideButton(View v) {
		LinearLayout searchBar = (LinearLayout) findViewById(id.search_bar);
		searchBar.setVisibility(View.GONE);
		// ソフトウェアキーボードかくす
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	public boolean onSearchRequested() {
		// Toast.makeText(this, "検索ボタンが呼ばれました", Toast.LENGTH_SHORT).show();
		LinearLayout searchBar = (LinearLayout) findViewById(id.search_bar);
		if (searchBar.getVisibility() == View.GONE) {
			searchBar.setVisibility(View.VISIBLE);
		} else {
			searchBar.setVisibility(View.GONE);
			// ソフトウェアキーボードかくす
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

		}
		return false;

	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		FLog.d("dispatchKeyEvent:" + event.getAction());
		// if(true){ return super.dispatchKeyEvent(event); }

		int action = event.getAction();
		int keyCode = event.getKeyCode();
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			if (action == KeyEvent.ACTION_UP) {
				FLog.d("vdown" + listView.getFirstVisiblePosition());

				// 一ページ分下スクロール int topPosition =
				// listView.getFirstVisiblePosition();
				int topPositionY = listView.getChildAt(0).getTop();
				WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
				Display disp = wm.getDefaultDisplay();
				int height = disp.getHeight();
				listView.setSelectionFromTop(
						listView.getFirstVisiblePosition(), topPositionY
								- (height - 200));

				// int height_temp =
				// listView.setScrollIndicators(up, down)topPositionY;
				// while(height_temp<)
			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			if (action == KeyEvent.ACTION_UP) {
				// 一ページ分上スクロール
				int topPositionY = listView.getChildAt(0).getTop();
				WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
				Display disp = wm.getDefaultDisplay();
				int height = disp.getHeight();
				listView.setSelectionFromTop(
						listView.getFirstVisiblePosition(), topPositionY
								+ (height - 200));
			}
			return true;
		}
		return super.dispatchKeyEvent(event);
		//return false;
	}

	@Override
	public void onStop() {
		FLog.d("ThumbGrid::onStop()");

		super.onStop();
		System.gc(); // GC呼ぶよ
	}
}
