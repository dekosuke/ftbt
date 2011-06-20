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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import cx.ath.dekosuke.ftbt.FutabaThreadAdapter.ImageGetTask;
import cx.ath.dekosuke.ftbt.R.id;

//スレッド表示アクティビティ
public class FutabaThread extends Activity implements Runnable {

	public ArrayList<FutabaStatus> statuses = null; // レス一覧
	public FutabaThreadAdapter adapter = null;
	public String threadURL = null;
	public String baseURL = null;
	public String BBSName = null;
	public int threadNum;
	public Toast toast;

	private ProgressDialog waitDialog;
	private Thread thread;

	private ListView listView;

	// 現在位置(リロード時復帰用)
	int position = 0;
	int positionY = 0;

	// 前回との更新を色分け表示するため
	int currentSize = 0;
	int prevSize = 0;

	// 画像カタログから戻ってきたときにどの画像から戻ってきたか判定用
	final int TO_IMAGECATALOG = 0;
	final int TO_POST = 1;

	private int itemLongClick_chosen = 0; // ここに変数置くの可能ならやめたい・・

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 無操作で暗くなるのを防ぐ
		if (getResources().getBoolean(R.bool.avoidsleep)) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		Intent intent = getIntent();
		String superFutabaLinkURL = intent.getDataString();
		if (superFutabaLinkURL != null) { // スーパーふたばリンク（urlからのリンク)
			FLog.d("superFutabaLink:" + superFutabaLinkURL);
			Pattern baseURLpattern = Pattern.compile(
					"(http://[^.]+[.]2chan[.]net/[^/]+/)res/([0-9]+)",
					Pattern.DOTALL);
			Matcher threadm = baseURLpattern.matcher(superFutabaLinkURL);
			if (threadm.find()) {
				baseURL = threadm.group(1);
				threadNum = Integer.parseInt(threadm.group(2));
				BBSName = "ふたばリンク"; // ここは本当は板名ほしい・・
				threadURL = baseURL + "res/" + threadNum + ".htm";
				FLog.d("threadURL=" + threadURL);
			} else {
				Toast.makeText(this, "URLの解読エラー", Toast.LENGTH_LONG).show();
			}

		} else { // 通常フロー

			baseURL = (String) intent.getSerializableExtra("baseUrl");
			threadNum = Integer.parseInt((String) intent
					.getSerializableExtra("threadNum"));
			BBSName = (String) intent.getSerializableExtra("BBSName");

			try {
				HistoryManager man = new HistoryManager();
				man.Load();
				FutabaThreadContent thread;
				thread = man.get(threadNum);
				FLog.d("seeAt="+thread.seeAt);
				position = thread.seeAt;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				FLog.d("message",e);
			}

			threadURL = baseURL + "res/" + threadNum + ".htm";
		}
		// FLog.d("threadurl=" + threadURL);

		statuses = new ArrayList<FutabaStatus>();

		setContentView(R.layout.futaba_thread);

		listView = (ListView) findViewById(id.threadlistview);
		
		// アダプターを設定します
		adapter = new FutabaThreadAdapter(this, R.layout.futaba_thread_row,
				statuses);
		listView.setAdapter(adapter);

		// 長クリック－＞テキスト共有
		listView.setOnItemLongClickListener(new FutabaThreadOnLongClickListener());

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
		
		setWait();
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

	final Handler handler2 = new Handler();
	final Handler handler3 = new Handler();

	private void loading() {
		LinearLayout searchBar = (LinearLayout) findViewById(id.search_bar);
		searchBar.setVisibility(View.GONE);

		FutabaThreadContentGetter getterThread = new FutabaThreadContentGetter();
		getterThread.start();
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
				FLog.d("image" + status.bigImgURL);
				list.add(status.bigImgURL);
			}
			i++;
		}
		return list;
	}

	// スレッドに存在するすべての画像のURLを配列にして返す
	public ArrayList<String> getThumbURLs() {
		Iterator iterator = statuses.iterator();
		int i = 0;
		ArrayList<String> list = new ArrayList<String>();
		// ループ
		while (iterator.hasNext()) {
			FutabaStatus status = (FutabaStatus) iterator.next();
			if (status.imgURL != null) {
				FLog.d("thumbimage" + status.imgURL);
				list.add(status.imgURL);
			}
			i++;
		}
		return list;
	}

	public void onClickReloadBtn(View v) {
		try {
			FLog.d("fthread onclick-reload");
			position = listView.getFirstVisiblePosition();
			positionY = listView.getChildAt(0).getTop();
			; // 現在位置（リロードで復帰）
			FLog.d("position=" + position);
			setWait();
		} catch (Exception e) {
			FLog.d("message", e);
		}
	}

	public void onClickGoTopBtn(View v) {
		// 最後へ
		listView.setSelection(0);
	}

	public void onClickGridViewBtn(View v) {
		FLog.d("intent calling gridview activity");
		Intent intent = new Intent();
		// Log.d ( "ftbt", threadNum );
		ArrayList<String> thumbURLs = getThumbURLs();
		if (thumbURLs.size() == 0) { // 画像がないよ
			return;
		}
		// ↓positionを一番近い場所にしたいのなら、今のpositionの画像が何番目か知る必要あり
		int pos = listView.getFirstVisiblePosition();
		int imagepos = 0;
		for (int i = 0; i <= pos; ++i) { // 現在位置までの画像を数えて行ってる
			FutabaStatus status = (FutabaStatus) adapter.items.get(i);
			if (status.imgURL != null && !status.imgURL.equals("")) {
				imagepos++;
			}
		}
		imagepos = Math.max(0, imagepos - 1);// これやらないと半分隠れてるときとかに不自然
		// Math.min(listView.getFirstVisiblePosition(), thumbURLs.size()-1));
		intent.putExtra("position", imagepos);
		intent.putExtra("imgURLs", getImageURLs());
		intent.putExtra("thumbURLs", thumbURLs);
		intent.setClassName(getPackageName(), getClass().getPackage().getName()
				+ ".ThumbGrid");
		startActivity(intent);
		// http://android.roof-balcony.com/intent/intent/
		/*
		 * activity.startActivityForResult(intent, activity.TO_IMAGECATALOG);
		 */
	}

	public void onClickGoBottomBtn(View v) {
		// 最後へ
		listView.setSelection(listView.getCount() - 1);
		// listView.setSelectionFromTop(position, y)
		// listView.getScrollY()
	}

	// 一画面ぶんスクロール。未完成
	// http://d.hatena.ne.jp/gae+eyo/20100729/1280393179
	// を読めばできそう
	public void onClickGoBtn(View v) {
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		// smoothScrollByOffsetはAndroid3からですねはい
		// listView.smoothScrollByOffset(display.getHeight());
	}

	public void onClickPostBtn(View v) {
		// Toast.makeText(this, "投稿ボタンが押されました", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent();
		// Log.d ( "ftbt", threadNum );
		intent.putExtra("baseURL", baseURL);
		intent.putExtra("threadNum", threadNum);
		intent.setClassName(getPackageName(), getClass().getPackage().getName()
				+ ".Post");
		startActivityForResult(intent, TO_POST);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_thread, menu);
		return true;
	}

	public void registerShiori(int position) {
		try {
			if (position == 0) {
				Toast.makeText(this, "スレッドの最初では栞を登録できません", Toast.LENGTH_SHORT)
						.show();
			} else {
				FutabaStatus item = (FutabaStatus) adapter.items.get(position);
				if (FutabaStatus.isBlank(item)) {
					// 新着のための空白部分の場合、次のレスに
					position += 1;
					item = (FutabaStatus) adapter.items.get(position);
				}
				// スレッドファイルに書き込み
				HistoryManager man = new HistoryManager();
				man.Load();
				FutabaThreadContent thread = new FutabaThreadContent();
				thread.pointAt = item.id;
				thread.threadNum = threadNum;
				FLog.d(thread.toString());
				man.updateThread(thread);
				man.Save();
				View view = listView.getChildAt(0);
				adapter.shioriPosition = item.id;
				adapter.setShioriStatus(view);
				adapter.notifyDataSetChanged();// 再描画
				listView.invalidate();

				Toast.makeText(this, "栞を登録しました。", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			FLog.d("message", e);
		}
	}

	public void removeShiori(int position) {
		try {
			// FutabaStatus item = (FutabaStatus) items.get(position);
			// スレッドファイルに書き込み
			HistoryManager man = new HistoryManager();
			man.Load();
			FutabaThreadContent thread = new FutabaThreadContent();
			thread.pointAt = -1;
			thread.threadNum = threadNum;
			FLog.d(thread.toString());
			man.updateThreadRemoveShiori(thread);
			man.Save();
			View view = listView.getChildAt(0);
			adapter.shioriPosition = 0;
			// adapter.setShioriStatus(view);
			adapter.notifyDataSetChanged();// 再描画
			listView.invalidate();

			Toast.makeText(this, "栞を削除しました。", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			FLog.d("message", e);
		}
	}

	public void searchShiori() {
		try {
			HistoryManager man = new HistoryManager();
			man.Load();
			FutabaThreadContent thread = man.get(threadNum);
			/*
			 * int position = thread.pointAt; if (position != 0) {
			 * listView.setSelection(Math.min(position, adapter.items.size() -
			 * 1)); Toast.makeText(this, "栞に移動しました", Toast.LENGTH_SHORT).show();
			 * } else { Toast.makeText(this, "栞が登録されていません",
			 * Toast.LENGTH_SHORT).show(); }
			 */
			int position = thread.pointAt;
			if (position == 0) {
				Toast.makeText(this, "栞が登録されていません", Toast.LENGTH_SHORT).show();
			} else {
				int i = 0;
				for (i = 0; i < adapter.items.size(); ++i) {
					FutabaStatus status = (FutabaStatus) adapter.items.get(i);
					if (status.id == position) {
						listView.setSelection(i);
						Toast.makeText(this, "栞に移動しました", Toast.LENGTH_SHORT)
								.show();
						break;
					}

				}
				if (i == adapter.items.size()) {
					Toast.makeText(this, "栞の位置が見つかりませんでした", Toast.LENGTH_SHORT)
							.show();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			FLog.d("message", e);
			Toast.makeText(this, "栞が見つかりませんでした", Toast.LENGTH_SHORT).show();
		}
	}

	// メニューをクリック
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.search:
			LinearLayout searchBar = (LinearLayout) findViewById(id.search_bar);
			if (searchBar.getVisibility() == View.GONE) {
				searchBar.setVisibility(View.VISIBLE);
			} else {
				searchBar.setVisibility(View.GONE);
			}
			return true;
		case R.id.download:
			// スレ全体を保存
			saveAll();
			return true;
		case R.id.post:
			onClickPostBtn(null);
			return true;
		case R.id.possave:
			// 栞をはさむ
		{
			int position = listView.getFirstVisiblePosition();
			registerShiori(position);
		}
			// 現在位置の取得
			return true;
		case R.id.posload:
			// 栞を読み込む(位置移動)
			searchShiori();
			return true;
		case R.id.share:
			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, threadURL);
			try {
				startActivityForResult(intent, 0);
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(this, "client not found", Toast.LENGTH_SHORT)
						.show();
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
		}
		return false;
	}

	// スレ全体を保存
	public void saveAll() {

		if (waitDialog != null) {
			waitDialog.dismiss();
		}
		waitDialog = new ProgressDialog(this);
		waitDialog.setMessage(this.getString(R.string.loading));
		waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// waitDialog.setCancelable(true);
		waitDialog.show();

		// 画像を順番に保存
		ArrayList<String> imgURLs = getImageURLs();
		// imgといいつつhtmlも足す
		imgURLs.add(threadURL);
		FutabaThreadAllImageRetriever getterThread = new FutabaThreadAllImageRetriever();
		getterThread.setImageURLs(imgURLs);
		getterThread.start();
	}

	// 全画像取得用スレッド
	private class FutabaThreadAllImageRetriever extends Thread {
		private ArrayList<String> imgURLs;

		public void setImageURLs(ArrayList<String> imgURLs) {
			this.imgURLs = imgURLs;
		}

		@Override
		public void run() {
			// 保存ディレクトリ作成
			// SDCard.createThreadDir(thread)
			int saveItemNum = 0;
			for (int i = 0; i < imgURLs.size(); ++i) {
				if (!waitDialog.isShowing()) { // キャンセルされた
					return;
				}
				try {
					final String imgURL = imgURLs.get(i);
					String threadName = "";
					if (false && statuses.size() != 0) {
						// スレ名表示に問題があるのでここ断念
						FutabaStatus status = statuses.get(0); // スレの最初のコメント
						Date date = new Date();
						SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMDD");
						threadName = "" + BBSName + "_" + sdf1.format(date)
								+ "_スレ" + threadNum;
					} else { // 今は常にここに来る
						threadName = "" + BBSName + "_スレ" + threadNum;
					}
					FLog.d("trying to save" + imgURL);
					File file = new File(imgURL);
					if (!imgURL.contains("htm")
							&& SDCard.savedImageToThreadExist(file.getName(),
									threadName)) { // すでにファイルある
						continue;
					}
					File saved_file = ImageCache.saveImageToThread(imgURL,
							threadName);
					if (saved_file == null) { // キャッシュにファイルがない
						ImageCache.setImage(imgURL); // 画像をネットから取ってくる(ここが重い)
						saved_file = ImageCache.saveImageToThread(imgURL,
								threadName);
					}
					if (saved_file != null) {
						saveItemNum += 1;
					}
					final File saved_file_f = saved_file;
					// 描画に関わる処理はここに集約(メインスレッド実行)
					handler3.post(new Runnable() {
						public void run() {
							// waitDialog.show();
							if (saved_file_f != null) {

								waitDialog.setMessage("ファイル\n" + saved_file_f
										+ "\nに保存しました");

								// ギャラリーに反映されるように登録
								// http://www.adakoda.com/adakoda/2010/08/android-34.html
								String mimeType = StringUtil
										.getMIMEType(saved_file_f.getName());

								FLog.d("name=" + saved_file_f.getName());
								FLog.d("mime=" + mimeType);

								// ContentResolver を使用する場合
								ContentResolver contentResolver = getContentResolver();
								ContentValues values = new ContentValues(7);
								values.put(Images.Media.TITLE,
										saved_file_f.getName());
								values.put(Images.Media.DISPLAY_NAME,
										saved_file_f.getName());
								values.put(Images.Media.DATE_TAKEN,
										System.currentTimeMillis());
								values.put(Images.Media.MIME_TYPE, mimeType);
								values.put(Images.Media.ORIENTATION, 0);
								values.put(Images.Media.DATA,
										saved_file_f.getPath());
								values.put(Images.Media.SIZE,
										saved_file_f.length());
								contentResolver.insert(
										Images.Media.EXTERNAL_CONTENT_URI,
										values);
							} else {
								if (toast != null) {
									toast.cancel();
								}
								toast.makeText(adapter.getContext(),
										"画像" + imgURL + "の取得に失敗しました",
										Toast.LENGTH_SHORT).show();

							}
						}
					});

				} catch (Exception e) {
					FLog.d("message", e);
				}
			}
			final int saveItemNum_f = saveItemNum;
			handler3.post(new Runnable() {
				public void run() {
					waitDialog.dismiss();
					if (toast != null) {
						toast.cancel();
					}
					toast.makeText(adapter.getContext(),
							"" + saveItemNum_f + "個のファイルを新規に保存しました",
							Toast.LENGTH_SHORT).show();
				}
			});

			System.gc();
		}

	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		try {
			// FLog.d("return intent imgURL="+imgURL);
			FLog.d("after activity " + requestCode);
			if (requestCode == TO_IMAGECATALOG) {
				String imgURL = (String) intent.getSerializableExtra("imgURL");
				for (int i = 0; i < statuses.size(); ++i) {
					// FLog.d("image"+i+"="+statuses.get(i).bigImgURL);
					if (imgURL.equals(statuses.get(i).bigImgURL)) {
						// FLog.d("hit="+i);
						listView.setSelection(Math.min(i, listView.getCount()));
						break;
					}
				}
			} else if (requestCode == TO_POST) {
				String posted = "";
				try {
					posted = (String) intent.getSerializableExtra("posted");
				} catch (Exception e) {
				}
				if (!posted.equals("")) {
					// 再読み込み
					this.onClickReloadBtn(null);
				}
			} else {
				FLog.d("unknown result code");
			}
		} catch (Exception e) {
			FLog.d("message", e);
		}
	}

	private boolean isAnonymousThread(String url) {
		return url.contains("img.2chan.net/b")
				|| url.contains("up.2chan.net/e");
	}

	private class FutabaThreadContentGetter extends Thread {

		@Override
		public void run() {

			try {
				// 匿名のBBS
				boolean anonymous = false;
				// ここでマジックナンバーが・・
				if (isAnonymousThread(baseURL)) {
					anonymous = true;
				}

				currentSize = prevSize = 0;

				// Toastに表示するtext
				String toast_text = "";

				Boolean network_ok = true;
				Boolean cache_ok = true;

				// キャッシュのHTMLをパーズするパーザ
				FutabaThreadParser cacheParser = new FutabaThreadParser();
				// webから取得したデータをパーズするパーザ
				FutabaThreadParser webParser = new FutabaThreadParser();
				if (SDCard.cacheExist(FutabaCrypt.createDigest(threadURL))) {
					String cacheThreadHtml = SDCard.loadTextCache(FutabaCrypt
							.createDigest(threadURL));
					cacheParser.parse(cacheThreadHtml, anonymous);
					FLog.d("cache  ok  " + threadURL);

				} else {
					cache_ok = false;
					FLog.d("cache fail " + threadURL);
				}

				String threadHtml = "";
				try {
					SDCard.saveFromURL(FutabaCrypt.createDigest(threadURL),
							new URL(threadURL), true); // キャッシュに保存
					// これが取得できれば最新のデータ
					String webThreadHtml = SDCard.loadTextCache(FutabaCrypt
							.createDigest(threadURL));
					webParser.parse(webThreadHtml, anonymous);
					// FLog.d(threadHtml);
					if (cache_ok) {
						// ↓コメント削除があるからこれは仮定できない
						/*
						 * if (webParser.getStatuses().size() < cacheParser
						 * .getStatuses().size()) { // スレが短くなってる -
						 * データが途中で転送切れたとかの類? throw new Exception(
						 * "network disconnected before finish"); }
						 */

						try {
							// 取得に成功した場合、履歴データの件数とかを更新する
							HistoryManager man = new HistoryManager();
							FutabaThreadContent thread = new FutabaThreadContent();
							thread.threadNum = threadNum;
							thread.resNum = ""
									+ Math.max(0, webParser.getStatuses()
											.size() - 1);
							man.Load();
							man.updateThread(thread);
							man.Save();
						} catch (Exception e) {
							FLog.d("message", e);
						}

					}

					network_ok = true;
				} catch (IOException e) {
					network_ok = false;
					// ホスト見つからない(ネットワークない) とか
					// レスポンスコードが2XX以外とか(スレ落ちなど)
					String cause = "ネットワークに繋がっていません";
					if (e.toString().contains("Incorrect response code ")) {
						cause = "スレッドが存在しません";
					}

					if (cache_ok) {
						toast_text = cause + "。前回読み込み時のキャッシュを使用します";
					} else {
						toast_text = cause;
					}

				} catch (Exception e) { // ネットワークつながってないときとか
					network_ok = false;
					FLog.d("message", e);
					if (cache_ok) {
						toast_text = "ネットワークに繋がっていません。代わりに前回読み込み時のキャッシュを使用します";
					} else {
						toast_text = "ネットワークに繋がっていません";
					}

				}

				HistoryManager man = new HistoryManager();
				man.Load();
				if ((!network_ok) && (!cache_ok)) { // データ取得もキャッシュもないスレッドは消す
					FutabaThreadContent thread = new FutabaThreadContent();
					thread.threadNum = threadNum;
					FLog.d("del thread" + thread.toString());
					man.removeThread(thread);
					man.Save();
				}

				// しおりの場所を調べる
				try {
					FutabaThreadContent thisThread = man.get(threadNum);
					adapter.shioriPosition = thisThread.pointAt;
				} catch (Exception e) {
					FLog.d("message", e);
				}

				ArrayList<FutabaStatus> statuses = new ArrayList<FutabaStatus>();
				if (network_ok) {
					statuses = webParser.getStatuses();
					int num = webParser.getStatuses().size();
					currentSize = num;
					if (cache_ok) {
						num -= cacheParser.getStatuses().size();
						prevSize = cacheParser.getStatuses().size();
						if (num < 0) {
							FLog.d("currentSize=" + currentSize + " prevSize="
									+ prevSize);
							toast_text = "新着:" + Math.max(num, 0) + "件";
						} else {
							toast_text = "新着レス:" + num + "件";
						}
					} else {
						toast_text = "レス" + Math.max(num - 1, 0) + "件";
						currentSize = prevSize = num;
					}
				} else if (cache_ok) {
					statuses = cacheParser.getStatuses();
				}
				FLog.d("parse end" + statuses.size());
				FutabaThreadParser parser = webParser;
				if (!network_ok) {
					parser = cacheParser;
				}
				final String title = parser.getTitle(20) + " - "
						+ getString(R.string.app_name);
				final String toast_text_f = toast_text;

				final ArrayList<FutabaStatus> statuses_ref = statuses;
				final int prevSize_ref = prevSize;
				// 描画に関わる処理はここに集約(メインスレッド実行)
				handler2.post(new Runnable() {
					public void run() {
						adapter.items.clear();
						for (int i = 0; i < statuses_ref.size(); ++i) {
							// FLog.d(statuses_ref.get(i).toString());
							if (i != 0 && i == prevSize_ref) {
								adapter.items.add(FutabaStatus.createBlank());
							}
							adapter.items.add(statuses_ref.get(i));
							// 最後にスレ落ち時間予告をはさむ
							if (i == statuses_ref.size() - 1
									&& !statuses_ref.get(0).endTime.equals("")) {
								adapter.items.add(FutabaStatus
										.createEndTime(statuses_ref.get(0).endTime));
							}
						}

						Toast.makeText(adapter.getContext(), toast_text_f,
								Toast.LENGTH_SHORT).show();
						setTitle(title);
						if (waitDialog != null) {
							waitDialog.dismiss();
						}
						adapter.notifyDataSetChanged();
						listView.invalidate();
						if (position != 0) {
							listView.setSelectionFromTop(
									Math.min(position, listView.getCount()),
									positionY);
						}
					}
				});

			} catch (Exception e) {
				FLog.d("message", e);
			}
		}
	}

	// 画像を保存する
	public void saveImage(String imgURL) {
		// 見せなくていいや...
		if (waitDialog != null) {
			waitDialog.dismiss();
		}
		waitDialog = new ProgressDialog(this);
		waitDialog.setMessage(this.getString(R.string.loading));
		waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); //
		waitDialog.setCancelable(true);
		waitDialog.show();

		FutabaThreadImageRetriever getterThread = new FutabaThreadImageRetriever();
		getterThread.setImageURL(imgURL);
		getterThread.start();
	}

	private class FutabaThreadImageRetriever extends Thread {
		private String imgURL;

		public void setImageURL(String imgURL) {
			this.imgURL = imgURL;
		}

		@Override
		public void run() {

			try {
				// 画像を保存する
				String imgFile = imgURL;
				FLog.d("trying to save" + imgURL);
				File file = new File(imgFile);
				File saved_file = ImageCache.saveImage(imgFile);
				if (saved_file == null) { // キャッシュにファイルがない
					ImageCache.setImage(imgFile); // 画像をネットから取ってくる(ここが重い)
					saved_file = ImageCache.saveImage(imgFile);
				}
				final File saved_file_f = saved_file;
				// 描画に関わる処理はここに集約(メインスレッド実行)
				handler3.post(new Runnable() {
					public void run() {
						if (saved_file_f != null) {

							Toast.makeText(adapter.getContext(),
									saved_file_f.getAbsolutePath() + "に保存しました",
									Toast.LENGTH_SHORT).show();

							// ギャラリーに反映されるように登録
							// http://www.adakoda.com/adakoda/2010/08/android-34.html
							String mimeType = StringUtil
									.getMIMEType(saved_file_f.getName());

							FLog.d("name=" + saved_file_f.getName());
							FLog.d("mime=" + mimeType);

							// ContentResolver を使用する場合
							ContentResolver contentResolver = getContentResolver();
							ContentValues values = new ContentValues(7);
							values.put(Images.Media.TITLE,
									saved_file_f.getName());
							values.put(Images.Media.DISPLAY_NAME,
									saved_file_f.getName());
							values.put(Images.Media.DATE_TAKEN,
									System.currentTimeMillis());
							values.put(Images.Media.MIME_TYPE, mimeType);
							values.put(Images.Media.ORIENTATION, 0);
							values.put(Images.Media.DATA,
									saved_file_f.getPath());
							values.put(Images.Media.SIZE, saved_file_f.length());
							contentResolver.insert(
									Images.Media.EXTERNAL_CONTENT_URI, values);
						} else {
							Toast.makeText(adapter.getContext(),
									"画像の取得に失敗しました", Toast.LENGTH_SHORT).show();

						}
					}
				});

			} catch (Exception e) {
				FLog.d("message", e);
			}
			waitDialog.dismiss();

		}
	}

	public void onClickSearchButton(View v) {
		// Toast.makeText(this, "検索ボタンが押されました", Toast.LENGTH_SHORT).show();

		if (true) {
			EditText searchEdit = (EditText) findViewById(id.searchinput);
			String searchText = searchEdit.getText().toString(); // これでいいんだろうか
			String[] query = StringUtil.queryNormalize(searchText);
			adapter.items.clear();
			// 検索テキストから絞込み
			for (int i = listView.getFirstVisiblePosition() + 1; i < statuses
					.size(); ++i) {
				String text = statuses.get(i).text;
				// Toast.makeText(this, "text=" + text,
				// Toast.LENGTH_SHORT).show();
				if (StringUtil.isQueryMatch(text, query)) { // みつかった
					listView.setSelection(i);
				}
			}
			/*
			 * adapter.notifyDataSetChanged(); // 再描画命令 LinearLayout searchBar =
			 * (LinearLayout) findViewById(id.search_bar); //
			 * searchBar.setVisibility(View.GONE); String toastText = "全" +
			 * fthreads.size() + "スレッド中、" + adapter.items.size() + "スレッドを表示します";
			 * if (fthreads.size() == adapter.items.size()) { toastText =
			 * "すべてのスレッドを表示します"; } else if (adapter.items.size() == 0) {
			 * toastText = "該当するスレッドは見つかりませんでした"; } Toast.makeText(this,
			 * toastText, Toast.LENGTH_SHORT).show();
			 */

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
		if (false) {
			LinearLayout searchBar = (LinearLayout) findViewById(id.search_bar);
			if (searchBar.getVisibility() == View.GONE) {
				searchBar.setVisibility(View.VISIBLE);
			} else {
				searchBar.setVisibility(View.GONE);
			}
		}
		return false;

	}

	@Override
	public void onPause() {
		super.onPause();
		int seeAt = listView.getFirstVisiblePosition();
		try {
			// スレッドファイルに書き込み
			HistoryManager man = new HistoryManager();
			man.Load();
			FutabaThreadContent thread = new FutabaThreadContent();
			thread.seeAt = seeAt;
			thread.threadNum = threadNum;
			FLog.d("seeAt="+seeAt);
			man.updateThread(thread);
			man.Save();
		} catch (Exception e) {
			FLog.d("message", e);
		}
	}
	
	/*
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
	*/
}
