package cx.ath.dekosuke.ftbt;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

import java.util.ArrayList;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import android.view.MotionEvent;
import android.os.AsyncTask;

//BufferedStreamのエラー問題対応
import java.io.ByteArrayOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.OutputStream;

//画面サイズ取得のため
import android.view.WindowManager;
import android.content.Context;
import android.view.Display;

//Progress Dialog出すため
import android.app.ProgressDialog;
import java.lang.Thread;

import cx.ath.dekosuke.ftbt.FutabaCatalogAdapter.ImageGetTask;
import android.os.Handler;
import android.os.Message;

//画像カタログ
//指定された画像の登録および、隣の画像への移動
//画像の円リストは別のデータ構造で。
public class SingleImage extends Activity implements Runnable {

	// 画像を読み込む際にAsyncTaskを使うが、
	// 新しいAsyncTaskが来たら古いAsyncTaskは諦めて終了する。
	// ここに登録されてないIDのタスクはキャンセル
	static int LastTaskID = -1;
	static Object lock = new Object();

	// ProgressDialog関連
	public ProgressDialog waitDialog;
	private Thread thread;

	Toast toast;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		toast = Toast.makeText(this, "[]", Toast.LENGTH_SHORT);
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
			} catch (Exception e) {
				Log.d("ftbt", "message", e);
			}
		}
	};

	private void loading() {

		Log.d("ftbt", "imageCatalog.onCreate start");
		Intent intent = getIntent();
		Log.d("ftbt", "hoge");
		ArrayList<String> imgURLs = (ArrayList<String>) intent
				.getSerializableExtra("imgURLs");

		Log.d("ftbt", "hoge1");
		String myImageURL = (String) intent.getSerializableExtra("myImgURL");
		Log.d("ftbt", "hoge2");

		// ここでIntentによる追加情報からCircleListを構築する
		CircleList.clear();

		// これスタティックにするのはどうかという感じがする
		for (int i = 0; i < imgURLs.size(); i++) {
			String imgURL = imgURLs.get(i);
			CircleList.add(imgURL);
			if (imgURL.equals(myImageURL)) {
				CircleList.moveToLast();
			}
		}
		// CircleList.add(myImageURL);
		// CircleList.move(1);

		SingleImageView view = new SingleImageView(this);
		view.setCurrentImage();
		final int FP = ViewGroup.LayoutParams.FILL_PARENT;
		view.setLayoutParams( new LinearLayout.LayoutParams(FP, FP) );
		view.setClickable(true);
		view.setFocusable(true);
		view.setFocusableInTouchMode(true);
		view.setScaleType(ScaleType.MATRIX);
		//view.setOnTouchListener(new FlickTouchListener());
		setContentView(view);
	}
}

// 円状のリスト。カタログに載っているファイルのリスト。
class CircleList {
	private static ArrayList<String> list = new ArrayList<String>();
	private static int pointer = -1; // 基本的に-1になるときは0件のときのみ。

	public static void add(String str) {
		list.add(str);
		if (pointer == -1) {
			pointer = 0;
		}
	}

	public static String get() {
		return list.get(pointer);
	}

	public static void set(int i) {
		pointer = i;
	}

	public static void move(int i) {
		pointer += i;
		pointer = (pointer + list.size()) % list.size();
	}

	public static void moveToZero() {
		pointer = 0;
	}

	public static void moveToLast() {
		pointer = list.size() - 1;
	}

	public static int pos() {
		return pointer;
	}

	public static int size() {
		return list.size();
	}

	public static void clear() {
		list = new ArrayList<String>();
		pointer = -1;
	}
}
