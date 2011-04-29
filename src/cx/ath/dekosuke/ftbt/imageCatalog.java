package cx.ath.dekosuke.ftbt;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.content.Intent;
import android.util.Log;

import android.widget.Toast;
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
import android.os.Handler;
import android.os.Message;

//画像カタログ
//指定された画像の登録および、隣の画像への移動
//画像の円リストは別のデータ構造で。
public class imageCatalog extends Activity implements Runnable {

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
		try{ //細かい時間を置いて、ダイアログを確実に表示させる
			Thread.sleep(100);
		}catch(InterruptedException e){
			 //スレッドの割り込み処理を行った場合に発生、catchの実装は割愛
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

		imageCatalogView view = new imageCatalogView(this);
		view.setOnTouchListener(new FlickTouchListener());
		setContentView(view);
	}

	private float lastTouchX;
	private float currentX;
	private float lastTouchY;
	private float currentY;

	private class FlickTouchListener implements View.OnTouchListener {
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN:
				lastTouchX = event.getX();
				lastTouchY = event.getY();
				break;

			case MotionEvent.ACTION_UP:
				currentX = event.getX();
				currentY = event.getY();
				float moveX = currentX - lastTouchX;
				float moveY = currentY - lastTouchY;
				if (moveY > 5.0f && moveY * moveY > moveX * moveX) {
					// 画像を保存する
					String imgFile = CircleList.get();
					File file = new File(imgFile);
					try {
						SDCard.saveFromURL(file.getName(), new URL(imgFile),
								false);
						toast.cancel();
						toast = Toast.makeText(v.getContext(),
								"画像" + file.getName() + "を保存しました",
								Toast.LENGTH_SHORT);
						toast.show();
					} catch (Exception e) {
						Log.i("ftbt", "message", e);
					}
				} else if (moveX > 5.0f) {
					// 前に戻る動作
					CircleList.move(-1);
					Log.d("ftbt", "motion prev " + CircleList.pos());
					((imageCatalogView) v).doDraw();
				} else if (moveX < -5.0f) {
					// 次に移動する動作
					CircleList.move(1);
					Log.d("ftbt", "motion next " + CircleList.pos());
					((imageCatalogView) v).doDraw();
				}
				break;

			case MotionEvent.ACTION_CANCEL:
				currentX = event.getX();
				if (lastTouchX < currentX) {
					// 前に戻る動作
				}
				if (lastTouchX > currentX) {
					// 次に移動する動作
				}
				break;
			}
			return true;
		}
	}

}

class imageCatalogView extends SurfaceView implements SurfaceHolder.Callback {

	public imageCatalogView(Context context) {
		super(context);

		getHolder().addCallback(this);
	}

	public void doDraw() {
		SurfaceHolder holder = getHolder();
		// SurfaceViewが作成された時の処理（初期画面の描画等）を記述
		Canvas canvas = holder.lockCanvas();

		// この間にグラフィック描画のコードを記述する。
		String imgFile = CircleList.get();
		setTag(imgFile);

		Context context = getContext();
		WindowManager wm = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE));
		Display display = wm.getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();

		try {
			canvas.drawColor(0, PorterDuff.Mode.CLEAR);
			Bitmap bmp = ImageCache.getImage(imgFile);
			if (bmp == null) { // キャッシュない
				// ImageCache.asyncSetImage(imgFile, imgFile);
				ImageGetTask task = new ImageGetTask(this);
				task.execute(imgFile);
			} else {
				Log.d("ftbt", "draw image");
				float s_x = Math.max(1.0f, (float) bmp.getWidth()
						/ (float) width);
				float s_y = Math.max(1.0f, (float) bmp.getHeight()
						/ (float) height);
				float scale = Math.max(s_x, s_y);
				int new_x = (int) (bmp.getWidth() / scale);
				int new_y = (int) (bmp.getHeight() / scale);
				bmp = Bitmap.createScaledBitmap(bmp, new_x, new_y, true);

				Paint p = new Paint();
				canvas.drawBitmap(bmp, 0, 0, p);

				imageCatalog activity = (imageCatalog)context;
				activity.waitDialog.dismiss();
			}
		} catch (Exception e) {
			// Log.i("ftbt", "message", new Throwable());
			Log.i("ftbt", "message", e);
		}

		// この間にグラフィック描画のコードを記述する。

		holder.unlockCanvasAndPost(canvas);
	}

	// 画像取得用スレッド
	class ImageGetTask extends AsyncTask<String, Void, Bitmap> {
		private imageCatalogView image;
		private String tag;
		private int id;

		public ImageGetTask(imageCatalogView _image) {
			image = _image;
			tag = image.getTag().toString();
			// ID登録
			synchronized (imageCatalog.lock) {
				imageCatalog.LastTaskID += 1;
				id = imageCatalog.LastTaskID;
			}
			Log.d("ftbt", "thread id" + id + " created.");
		}

		@Override
		protected Bitmap doInBackground(String... urls) {

			Bitmap bm = null;
			try {
				Log.d("ftbt", "getting" + urls[0]);
				bm = ImageCache.getImage(urls[0]);
				if (bm == null) { // does not exist on cache
					ImageCache.setImage(urls[0]);
					bm = ImageCache.getImage(urls[0]);
				}
			} catch (Exception e) {
				Log.i("ftbt", "message", e);
			}
			return bm;
		}

		// メインスレッドで実行する処理
		@Override
		protected void onPostExecute(Bitmap result) {
			// Tagが同じものが確認して、同じであれば画像を設定する
			if (image != null && tag != null & tag.equals(image.getTag())) {
				// image.setImageBitmap(result);
				try {
					image.doDraw(); // 再描画
				} catch (Exception e) {
					Log.i("ftbt", "message", e);
				}
			}
			Log.d("ftbt", "thread " + id + "end.");
		}

		@Override
		protected void onCancelled() {
			Log.d("ftbt", "スレッドキャンセル id=" + id);
		}

		private Bitmap MyDecodeStream(InputStream in) {
			final int IO_BUFFER_SIZE = 4 * 1024;
			Bitmap bitmap = null;
			BufferedOutputStream out = null;
			try {

				in = new BufferedInputStream(in, IO_BUFFER_SIZE);

				final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
				out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
				byte[] b = new byte[IO_BUFFER_SIZE];
				int read;
				while ((read = in.read(b)) != -1) {
					out.write(b, 0, read);
				}
				// streamCopy(in, out);
				out.flush();

				final byte[] data = dataStream.toByteArray();
				bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

			} catch (Exception e) {
				Log.i("ftbt", "message", e);
			}
			return bitmap;
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		doDraw();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		doDraw();

	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

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
