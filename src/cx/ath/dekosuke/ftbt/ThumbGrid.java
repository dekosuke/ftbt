package cx.ath.dekosuke.ftbt;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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

	// private CatalogAdapter adapter = null;
	public String baseUrl = "";
	private String catalogURL;
	private String BBSName = ""; // 板名

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle("サムネイル一覧");

		FLog.d("ThumbGrid.onCreate start");
		Intent intent = getIntent();
		thumbURLs = (ArrayList<String>) intent
		.getSerializableExtra("thumbURLs");
		imgURLs = (ArrayList<String>) intent
		.getSerializableExtra("imgURLs");

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
		LinearLayout linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.HORIZONTAL);
		setContentView(linearLayout);
		*/
		setContentView(R.layout.thumbgrid);

		GridView grid = (GridView) findViewById(id.gridview);
		/*
		GridView grid = new GridView(this);
		linearLayout.addView(grid, createParam(WC, FP));
		*/

		grid.setNumColumns(3);
		grid.setVerticalSpacing(10);
		// grid.setStretchMode(GridView.STRETCH_SPACING);
		ThumbGridAdapter adapter = new ThumbGridAdapter(this,
				R.layout.thumbgridelement, thumbURLs);
		grid.setAdapter(adapter);

		waitDialog.dismiss();
	}

	private LinearLayout.LayoutParams createParam(int w, int h) {
		return new LinearLayout.LayoutParams(w, h);
	}

	/*
	 * ArrayList<String> urls = new ArrayList<String>(); ArrayAdapter<String>
	 * arrayAdapter = new ArrayAdapter<String>(this, R.layout.thumbgridelement,
	 * urls);
	 */
}
