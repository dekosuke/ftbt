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

public class ftbt extends TabActivity {

	public ArrayList<FutabaBBSContent> favoriteBBSs = new ArrayList<FutabaBBSContent>();

	private TabSpec tab02;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// TabHostのインスタンスを取得
		TabHost tabs = getTabHost();
		/*
		// レイアウトを設定 -> これあると2.1で落ちるよ(2.2だとok)
		LayoutInflater.from(this).inflate(R.layout.tabmain,
				tabs.getTabContentView(), true);
				*/
		Intent intent;

		// キャッシュを削除する(重い)
		try {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(this);
			int cacheSize = Integer.parseInt(preferences.getString(
					getString(R.string.cachesize), "5"));
			ProgressDialog waitDialog = new ProgressDialog(this);
			waitDialog.setMessage("キャッシュの整理中・・・");
			waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			// waitDialoProgressDialogg.setCancelable(true);
			waitDialog.show();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			Log.d("ftbt", "cachesize=" + cacheSize);
			SDCard.limitCache(cacheSize);
			waitDialog.dismiss();
		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}

		try {
			// お気に入りスレッドリスト
			favoriteBBSs = new ArrayList<FutabaBBSContent>();
			Log.d("ftbt", "favbbs=" + favoriteBBSs);
			favoriteBBSs = FavoriteSettings.getFavorites(this);
			// タブシートの設定
			intent = new Intent().setClassName(getPackageName(), getClass()
					.getPackage().getName() + ".FutabaBBSMenu");
			intent.putExtra("mode", "all");
			TabSpec tab01 = tabs.newTabSpec("TabSheet1");
			View v1 = new MyView(this, "すべて");
			tab01.setIndicator(v1);
			tab01.setContent(intent);
			tabs.addTab(tab01);

			intent = new Intent().setClassName(getPackageName(), getClass()
					.getPackage().getName() + ".FutabaBBSMenu");
			intent.putExtra("mode", "fav");
			tab02 = tabs.newTabSpec("TabSheet2");
			// tab02.setIndicator("お気に入り");
			View v2 = new MyView(this, "お気に入り");
			tab02.setIndicator(v2);
			tab02.setContent(intent);
			tabs.addTab(tab02);
			// 初期表示のタブ設定
			tabs.setCurrentTab(0);
		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}
		Log.d("ftbt", "ftbt start");
	}

	public void addFavoriteBBSs(FutabaBBSContent bbs) {
		Log.d("ftbt", "favoriteBBSs=" + favoriteBBSs.toString());
		try {
			if (favoriteBBSs.indexOf(bbs) == -1) {
				Log.d("ftbt", "add " + bbs.toString());
				favoriteBBSs.add(bbs);
				FavoriteSettings.setFavorites(this, favoriteBBSs); // xmlに保存
				// adapter.addList(bbs);
				// adapter.notifyDataSetChanged();
			} else {
				Log.d("ftbt", "thread already exist in favlist");
			}
		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}
	}

	public void removeFavoriteBBSs(FutabaBBSContent bbs) {
		try {
			favoriteBBSs.remove(bbs);
			FavoriteSettings.setFavorites(this, favoriteBBSs); // xmlに保存
			Log.d("ftbt", "remove " + bbs.toString());
		} catch (Exception e) {
			Log.i("ftbt", "message", e);
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
			startActivity(intent);
			return true;
		case R.id.about:
			Uri uri = Uri.parse(getString(R.string.helpurl));
			intent = new Intent(Intent.ACTION_VIEW, uri);
			intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
			try {
				startActivity(intent);
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(this, "ブラウザが見つかりません", Toast.LENGTH_SHORT)
						.show();
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

			try{
			View v = inflater.inflate(R.layout.tabwidget, null);

			// テキスト
			TextView tv = (TextView) v.findViewById(R.id.text);
			tv.setText(title);

			addView(v);
			}catch(Exception e){
				Log.d("ftbt", "message", e);
			}
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Log.d("ftbt", "ftbt onresume");
	}
}
