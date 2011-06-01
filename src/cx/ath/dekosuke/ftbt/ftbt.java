package cx.ath.dekosuke.ftbt;

import java.util.ArrayList;

import android.app.LocalActivityManager;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

import cx.ath.dekosuke.ftbt.R.id;

//タブ式トップページ

public class ftbt extends TabActivity implements Runnable {

	public ArrayList<FutabaBBSContent> favoriteBBSs = new ArrayList<FutabaBBSContent>();

	ProgressDialog waitDialog;
	Thread thread;

	TabHost tabs;
	
	final int ON_SETTING=10000;
	
	public void myStart(){
		// TabHostのインスタンスを取得
		tabs = getTabHost();

		// 2.1で落ちる問題対策のためのダミータブ
		TabSpec tab00 = tabs.newTabSpec("TabSheet0");
		View v1 = new MyView(this, "読み込み中・・・");
		tab00.setIndicator(v1);
		Intent intent = new Intent().setClassName(getPackageName(), getClass()
				.getPackage().getName() + ".DummyTab");
		tab00.setContent(intent);
		tabs.addTab(tab00);

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		myStart();
		setWait();

	}

	public void setWait() {
		waitDialog = new ProgressDialog(this);
		waitDialog.setMessage("キャッシュを整理しています。\n(前回起動から開いたページ数に応じて時間がかかります)");
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
			FLog.d("handle msg" + msg);
			try {
				loading();
			} catch (Exception e) {
				FLog.d("message", e);
			}
		}
	};

	public void loading() {

		// ユーザの指定したディレクトリ設定を読み込む
		try {
			SDCard.setCacheDir(this);
			SDCard.setSaveDir(this);
		} catch (Exception e) {
			FLog.d("message", e);
		}

		if (!SDCard.isSDCardMounted()) {
			Toast.makeText(this, "SDカードが挿入されていません", Toast.LENGTH_LONG).show();
			//return;
		}
		
		try {
			String saveDir = SDCard.getSaveDir();
			if (saveDir == null) {
				throw new Exception("bad userdir");
			}
		} catch (Exception e) {
			FLog.d("message", e);
			if (waitDialog != null) {
				waitDialog.dismiss();
			}
			Toast.makeText(
					this,
					"保存先指定ディレクトリ\""
							+ SDCard.saveDir
							+ "\"が存在しないか、もしくは書き込みできないディレクトリです。\nメニューから再設定をお願いします。"
							+ "設定がわからない場合、"
							+ Environment.getExternalStorageDirectory()
							+ "を推奨します。", Toast.LENGTH_LONG).show();
			return;
		}

		// キャッシュを削除する(重い)
		try {
			// ダイアログ出すの大変なのでToastに
			// Toast.makeText(this, "キャッシュを整理しています・・・",
			// Toast.LENGTH_LONG).show();
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(this);
			int cacheSize = Integer.parseInt(preferences.getString(
					getString(R.string.cachesize), "5"));

			FLog.d("cachesize=" + cacheSize);
			SDCard.limitCache(cacheSize);

		} catch (Exception e) {
			FLog.d("message", e);
		}

		FLog.d("after cachecheck");

		try {
			waitDialog.dismiss();
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		/*
		 * // レイアウトを設定 -> これあると2.1で落ちるよ(2.2だとok)
		 * LayoutInflater.from(this).inflate(R.layout.tabmain,
		 * tabs.getTabContentView(), true);
		 */
		Intent intent;

		try {
			// ダミータブ消す
			tabs.getTabWidget().getChildAt(0).setVisibility(View.GONE);
			//tabs.clearAllTabs();
			
			int tabNum = tabs.getTabWidget().getChildCount();

			// お気に入りスレッドリスト
			favoriteBBSs = new ArrayList<FutabaBBSContent>();
			FLog.d("favbbs=" + favoriteBBSs);
			favoriteBBSs = FavoriteSettings.getFavorites(this);
			// タブシートの設定
			intent = new Intent().setClassName(getPackageName(), getClass()
					.getPackage().getName() + ".FutabaBBSMenu");
			intent.putExtra("mode", "all");
			//intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			TabSpec tab01 = tabs.newTabSpec("TabSheet1");
			View v1 = new MyView(this, "すべて");
			tab01.setIndicator(v1);
			tab01.setContent(intent);
			tabs.addTab(tab01);

			intent = new Intent().setClassName(getPackageName(), getClass()
					.getPackage().getName() + ".FutabaBBSMenu");
			intent.putExtra("mode", "fav");
			//intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			TabSpec tab02 = tabs.newTabSpec("TabSheet2");
			// tab02.setIndicator("お気に入り");
			View v2 = new MyView(this, "お気に入り");
			tab02.setIndicator(v2);
			tab02.setContent(intent);
			tabs.addTab(tab02);
			// 初期表示のタブ設定
			tabs.setCurrentTab(1);

			setTitle("BBS一覧 - " + getString(R.string.app_name));
		} catch (Exception e) {
			FLog.d("message", e);
		}
		FLog.d("ftbt start");
	}

	public void addFavoriteBBSs(FutabaBBSContent bbs) {
		FLog.d("favoriteBBSs=" + favoriteBBSs.toString());
		try {
			if (favoriteBBSs.indexOf(bbs) == -1) {
				FLog.d("add " + bbs.toString());
				favoriteBBSs.add(bbs);
				FavoriteSettings.setFavorites(this, favoriteBBSs); // xmlに保存
				// adapter.addList(bbs);
				// adapter.notifyDataSetChanged();
			} else {
				FLog.d("thread already exist in favlist");
			}
		} catch (Exception e) {
			FLog.d("message", e);
		}
	}

	public void removeFavoriteBBSs(FutabaBBSContent bbs) {
		try {
			favoriteBBSs.remove(bbs);
			FavoriteSettings.setFavorites(this, favoriteBBSs); // xmlに保存
			FLog.d("remove " + bbs.toString());
		} catch (Exception e) {
			FLog.d("message", e);
		}
	}

	// メニュー
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_bbsmenu, menu);
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
			startActivityForResult(intent, ON_SETTING);
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

	private class MyView extends FrameLayout {
		private LayoutInflater inflater;

		public MyView(Context context) {
			super(context);
			inflater = LayoutInflater.from(context);
		}

		public MyView(Context context, String title) {
			this(context);

			try {
				View v = inflater.inflate(R.layout.tabwidget, null);

				// テキスト
				TextView tv = (TextView) v.findViewById(R.id.text);
				tv.setText(title);

				addView(v);
			} catch (Exception e) {
				FLog.d("message", e);
			}
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		FLog.d("onhoge");
		if(requestCode == ON_SETTING){ //プリファレンス変更後はキャッシュ変更→お気に入り初期化がされている可能性があるので再起動
			//tabs.getTabWidget().getChildAt(0).setVisibility(View.VISIBLE);
			/*
			LocalActivityManager manager = getLocalActivityManager();
			manager.destroyActivity("TabSheet1", true);
			manager.destroyActivity("TabSheet2", true);
			Intent intent;
			// タブシートの設定
			intent = new Intent().setClassName(getPackageName(), getClass()
					.getPackage().getName() + ".FutabaBBSMenu");
			intent.putExtra("mode", "all");
			manager.startActivity("TabSheet1", intent);
			intent = new Intent().setClassName(getPackageName(), getClass()
					.getPackage().getName() + ".FutabaBBSMenu");
			intent.putExtra("mode", "fav");
			manager.startActivity("TabSheet2", intent);
			*/
			//tabs.getTabWidget().getChildAt(1).notifyAll();
			//tabs.getTabWidget().getChildAt(2).notifyAll();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		FLog.d("ftbt onresume");
	}
}
