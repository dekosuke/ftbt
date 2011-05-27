package cx.ath.dekosuke.ftbt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
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

	private int itemLongClick_chosen = 0; // ここに変数置くの可能ならやめたい・・

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		baseURL = (String) intent.getSerializableExtra("baseUrl");
		threadNum = Integer.parseInt((String) intent
				.getSerializableExtra("threadNum"));
		BBSName = (String) intent
				.getSerializableExtra("BBSName");
		threadURL = baseURL + "res/" + threadNum + ".htm";
		FLog.d("threadurl=" + threadURL);

		statuses = new ArrayList<FutabaStatus>();

		setContentView(R.layout.futaba_thread);

		listView = (ListView) findViewById(id.threadlistview);
		// アダプターを設定します
		adapter = new FutabaThreadAdapter(this, R.layout.futaba_thread_row,
				statuses);
		listView.setAdapter(adapter);

		// 長クリック－＞テキスト共有
		listView.setOnItemLongClickListener(new FutabaThreadOnLongClickListener());

		setWait();
	}

	public void setWait() {
		if (waitDialog != null) {
			waitDialog.dismiss();
		}
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

	final Handler handler2 = new Handler();
	final Handler handler3 = new Handler();

	private void loading() {
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
		startActivity(intent);
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
		case R.id.tweet:
			String thread_title = statuses.get(0).title;
			thread_title = FutabaThreadParser.removeTag(thread_title); // HTMLタグ除去
			thread_title = "見てる:"
					+ thread_title.substring(0,
							Math.min(30, thread_title.length()));
			if (thread_title.length() == 30) {
				thread_title = thread_title + "...";
			}
			String status_encoded = thread_title + " " + threadURL; // URIエンコードされた、ツイートしたい文章
			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			// intent.putExtra(Intent.EXTRA_SUBJECT , );
			intent.putExtra(Intent.EXTRA_TEXT, status_encoded
					+ getString(R.string.hashtagstr));
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
		waitDialog.setMessage("ネットワーク接続中...");
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
				if(!waitDialog.isShowing()){ //キャンセルされた
					return;
				}
				try {
					final String imgURL = imgURLs.get(i);
					final String threadName = BBSName + "_スレ" + threadNum;
					FLog.d("trying to save" + imgURL);
					File file = new File(imgURL);
					if(SDCard.savedImageToThreadExist(file.getName(), threadName)){ //すでにファイルある
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
							//waitDialog.show();
							if (saved_file_f != null) {

								waitDialog.setMessage("ファイル\n"+saved_file_f+"\nに保存しました");

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
								if(toast!=null){
									toast.cancel();
								}
								toast.makeText(adapter.getContext(),
										"画像"+imgURL+"の取得に失敗しました", Toast.LENGTH_SHORT)
										.show();

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
					if(toast!=null){
						toast.cancel();
					}
					toast.makeText(adapter.getContext(), ""+saveItemNum_f+"個のファイルを新規に保存しました", Toast.LENGTH_SHORT ).show();
				}
			});
			
			System.gc();
		}

	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		try {
			String imgURL = (String) intent.getSerializableExtra("imgURL");
			// FLog.d("return intent imgURL="+imgURL);
			if (requestCode == TO_IMAGECATALOG) {
				for (int i = 0; i < statuses.size(); ++i) {
					// FLog.d("image"+i+"="+statuses.get(i).bigImgURL);
					if (imgURL.equals(statuses.get(i).bigImgURL)) {
						// FLog.d("hit="+i);
						listView.setSelection(Math.min(i, listView.getCount()));
						break;
					}
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
							thread.resNum = "" + webParser.getStatuses().size();
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
						waitDialog.dismiss();
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
		if (waitDialog != null) {
			waitDialog.dismiss();
		}
		waitDialog = new ProgressDialog(this);
		waitDialog.setMessage("ネットワーク接続中...");
		waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// waitDialog.setCancelable(true);
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
						waitDialog.dismiss();
					}
				});

			} catch (Exception e) {
				FLog.d("message", e);
			}

		}
	}
}
