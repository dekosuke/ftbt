package cx.ath.dekosuke.ftbt;

import java.util.ArrayList;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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

public class ftbt extends Activity implements Runnable {

	ProgressDialog waitDialog;
	Thread thread;
	boolean initialStart=true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialStart=false;
		setWait();
	}
	

	@Override
	protected void onResume() {
		super.onResume();
		if(!initialStart){
			setWait();
		}
	}

	public void setWait() {
		FLog.d("ftbt-cachecheck start");
		waitDialog = new ProgressDialog(this);
		waitDialog.setMessage("キャッシュの整理中...");
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

	// メイン
	public void loading() {
		// あんまり期待できないSDカード入ってるか判定
		/*
		 * if(!SDCard.isMountedExSD()){ Toast.makeText(this,
		 * getString(R.string.app_name
		 * )+"を利用するためには、SDカードが必要です。\nSDカードを装備してから再起動してください", Toast.LENGTH_LONG);
		 * return; }
		 */

		// キャッシュを削除する(重い)
		// この処理マルチスレッドにするために別activityに
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
			FLog.d("cachesize=" + cacheSize);
			SDCard.limitCache(cacheSize);
			waitDialog.dismiss();

			Intent intent = new Intent();
			intent.setClassName(getPackageName(), getClass().getPackage()
					.getName() + ".ftbt_main");
			startActivity(intent);
		} catch (Exception e) {
			FLog.d("message", e);
		}

	}
}
