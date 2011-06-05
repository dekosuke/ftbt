package cx.ath.dekosuke.ftbt;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cx.ath.dekosuke.ftbt.R.id;

public class ThumbGrid extends Activity implements Runnable {

	private ProgressDialog waitDialog;
	private Thread thread;

	public ArrayList<String> thumbURLs = new ArrayList<String>();
	public ArrayList<String> imgURLs = new ArrayList<String>();
	public int startPos = 0;
	private int width;
	private int height;

	// private CatalogAdapter adapter = null;
	public String baseUrl = "";
	private String catalogURL;
	private String BBSName = ""; // 板名
	
	private GridView grid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 無操作で暗くなるのを防ぐ
		if (getResources().getBoolean(R.bool.avoidsleep)) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		
		try {
			setTitle("サムネイル一覧 - " + getString(R.string.app_name));
			Toast.makeText(this, "サムネイル一覧を読み込み中です", Toast.LENGTH_SHORT).show();

			System.gc(); // 重いのでGC呼んでおくよ(こんなところで呼んでいいのかわからないけど)

			FLog.d("ThumbGrid.onCreate start");
			Intent intent = getIntent();
			thumbURLs = (ArrayList<String>) intent
					.getSerializableExtra("thumbURLs");
			imgURLs = (ArrayList<String>) intent
					.getSerializableExtra("imgURLs");
			startPos = (Integer) intent.getSerializableExtra("position");

			// 画面サイズの取得
			WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			width = display.getWidth();
			height = display.getHeight();
			
		} catch (Exception e) {
			FLog.d("message", e);
		}
		setWait();

	}

	public void setWait() {
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

				// プログレスダイアログ終了
			} catch (Exception e) {
				FLog.d("message", e);
			}
		}
	};

	final Handler handler2 = new Handler();

	private final int FP = ViewGroup.LayoutParams.FILL_PARENT;
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

	private void loading() {

		/*
		 * LinearLayout linearLayout = new LinearLayout(this);
		 * linearLayout.setOrientation(LinearLayout.HORIZONTAL);
		 * setContentView(linearLayout);
		 */
		setContentView(R.layout.thumbgrid);

		TextView imagenum = (TextView) findViewById(id.imagenum);
		imagenum.setText("画像:" + thumbURLs.size() + "枚  ");

		grid = (GridView) findViewById(id.gridview);
		/*
		 * GridView grid = new GridView(this); linearLayout.addView(grid,
		 * createParam(WC, FP));
		 */

		if(width<height){
			grid.setNumColumns(3);
		}else{
			grid.setNumColumns(5);			
		}
		//grid.setVerticalSpacing(10);
		// grid.setStretchMode(GridView.STRETCH_SPACING);
		ThumbGridAdapter adapter = new ThumbGridAdapter(this,
				R.layout.thumbgridelement, thumbURLs);
		grid.setAdapter(adapter);		
		grid.setSmoothScrollbarEnabled(true);
		grid.setVerticalScrollBarEnabled(true);
		grid.setSelection(startPos);
		grid.setDrawingCacheEnabled(true);
		//grid.setDrawingCacheBackgroundColor(Color.BLACK);

		waitDialog.dismiss();
	}
	
	//回転のときに呼ばれる
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		// 画面サイズの取得
		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		width = display.getWidth();
		height = display.getHeight();
		
		if(width<height){
			grid.setNumColumns(3);
		}else{
			grid.setNumColumns(5);			
		}
		System.gc();
	}
	
	private LinearLayout.LayoutParams createParam(int w, int h) {
		return new LinearLayout.LayoutParams(w, h);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_gridview, menu);
		return true;
	}

	// メニューをクリック
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.post:
			// onClickPostBtn(null);
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

	@Override
	public void onPause() {
		FLog.d("ThumbGrid::onPause()");

		super.onPause();
		System.gc(); // GC呼ぶよ
	}

	@Override
	public void onStop() {
		FLog.d("ThumbGrid::onStop()");

		super.onStop();
		System.gc(); // GC呼ぶよ
	}

	@Override
	public void onDestroy() {
		FLog.d("ThumbGrid::onDestroy()");

		super.onDestroy();
		System.gc(); // GC呼ぶよ
	}
}
