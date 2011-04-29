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

	private FutabaTopAdapter adapter = null;
	String mode;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		mode = (String) intent.getSerializableExtra("mode");
		
		//キャッシュを削除する(重いので明示+確認すべし)
		SDCard.limitCache(100);

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

		ArrayList<FutabaBBS> BBSs = new ArrayList<FutabaBBS>();
		if(mode.equals("all")){
			FutabaBBSMenuParser parser = new FutabaBBSMenuParser(
					"http://www.2chan.net/bbsmenu.html");
			parser.parse();
			if(!parser.network_ok && parser.cache_ok){
				Toast.makeText(this, "ネットワークに繋がっていません。代わりに前回読み込み時のキャッシュを使用します。", Toast.LENGTH_LONG).show();
			}
	
			BBSs = parser.getBBSs();
		}else{ //fav
			BBSs = (ArrayList<FutabaBBS>) intent.getSerializableExtra("favoriteBBSs");
		}
		adapter = new FutabaTopAdapter(this, R.layout.futaba_bbs_row, BBSs);
		// アイテムを追加します
		ListView listView = (ListView) findViewById(id.listview);
		// アダプターを設定します
		listView.setAdapter(adapter);

		Log.d("ftbt", "start");

		waitDialog.dismiss();
	}

	// 設定画面に遷移
	public void transSetting(FutabaBBS item) {
		Intent intent = new Intent();
		/*
		 * intent.setClassName(getPackageName(),
		 * getClass().getPackage().getName()+".catalog");
		 */
		Log.d("ftbt", item.url);
		intent.putExtra("baseUrl", item.url);
		intent.setClassName(getPackageName(),
		// getClass().getPackage().getName()+".fthread");
				getClass().getPackage().getName() + ".catalog");
		startActivity(intent);
	}
}
