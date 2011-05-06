package cx.ath.dekosuke.ftbt;

import java.util.ArrayList;

import android.app.LocalActivityManager;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
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
		// レイアウトを設定
		LayoutInflater.from(this).inflate(R.layout.tabmain,
				tabs.getTabContentView(), true);
		Intent intent;

		// キャッシュを削除する(重い)
		try {
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(this);
			int cacheSize = Integer.parseInt(preferences.getString(
					getString(R.string.cachesize), "5"));
			Log.d("ftbt", "cachesize=" + cacheSize);
			ProgressDialog waitDialog = new ProgressDialog(this);
			waitDialog.setMessage("キャッシュの整理中・・・");
			waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			// waitDialoProgressDialogg.setCancelable(true);
			waitDialog.show();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			SDCard.limitCache(30);
			waitDialog.dismiss();
		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}

		try {
			// お気に入りスレッドリスト
			favoriteBBSs = new ArrayList<FutabaBBSContent>();
			Log.d("ftbt", "favbbs="+favoriteBBSs);
			favoriteBBSs = FavoriteSettings.getFavorites(this);
			// タブシートの設定
			intent = new Intent().setClassName(getPackageName(), getClass()
					.getPackage().getName() + ".FutabaBBSMenu");
			intent.putExtra("mode", "all");
			TabSpec tab01 = tabs.newTabSpec("TabSheet1");
			tab01.setIndicator("すべて");
			tab01.setContent(intent);
			tabs.addTab(tab01);

			intent = new Intent().setClassName(getPackageName(), getClass()
					.getPackage().getName() + ".FutabaBBSMenu");
			intent.putExtra("mode", "fav");
			tab02 = tabs.newTabSpec("TabSheet2");
			tab02.setIndicator("お気に入り");
			tab02.setContent(intent);
			tabs.addTab(tab02);
			// 初期表示のタブ設定
			tabs.setCurrentTab(0);
		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}
	}

	public void addFavoriteBBSs(FutabaBBSContent bbs) {
		Log.d("ftbt", "favoriteBBSs=" + favoriteBBSs.toString());
		try {
			if (favoriteBBSs.indexOf(bbs) == -1) {
				Log.d("ftbt", "add " + bbs.toString());
				favoriteBBSs.add(bbs);
				FavoriteSettings.setFavorites(this, favoriteBBSs); // xmlに保存
				//adapter.addList(bbs);
				//adapter.notifyDataSetChanged();
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
			Toast.makeText(this, "about", Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}

}
