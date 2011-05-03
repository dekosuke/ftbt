package cx.ath.dekosuke.ftbt;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;
import java.lang.Math;

class ImageCatalogSingleView extends ImageView implements OnTouchListener,
		Runnable {

	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	private Matrix matrix = new Matrix();
	private int mode = NONE;

	/** ズーム時の座標 */
	private PointF middle = new PointF();
	/** ドラッグ用マトリックス */
	private Matrix moveMatrix = new Matrix();
	/** Zoom開始時の二点間距離 */
	private float initLength = 1;
	// fling用テンポラリ
	private MotionEvent prevEvent;
	// x方向移動成分
	private float mx = 0f;

	// 画面サイズ
	private int width = 100;
	private int height = 100;
	// 読み込んだ画像
	private Bitmap bm = null;
	// bx, byはbitmapのサイズそのものでなく、matrixのscale後のサイズ
	private float bx;
	private float by;

	// ProgressDialog関連
	public ProgressDialog waitDialog;
	private Thread thread;

	// マルチタッチのための２点座標
	private boolean p1_pressed = false;
	private boolean p2_pressed = false;
	private PointF point = null;
	private PointF p1 = new PointF();
	private PointF p2 = new PointF();
	private int point_side = 1;

	public ImageCatalogSingleView(Context context) {
		this(context, null, 0);
	}

	public ImageCatalogSingleView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ImageCatalogSingleView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		matrix = new Matrix();
		matrix.setScale(1, 1);
		bx = by = 0;
		setClickable(true); // これないとマルチタッチ効かないけど、あると親クラスのクリックイベントを奪う・・
		setOnTouchListener(this);

		// 画面サイズの取得
		WindowManager wm = ((WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE));
		Display display = wm.getDefaultDisplay();
		width = display.getWidth();
		height = display.getHeight();

		// 拡大縮小可能に
		this.setScaleType(ScaleType.MATRIX);
		// 画像取得
		setImage();
	}

	public boolean onTouch(View v, MotionEvent event) {
		try {
			// Toast.makeText(getContext(), "touch detected",
			// Toast.LENGTH_SHORT).show();
			ImageView view = (ImageView) v;
			ImageCatalog activity = (ImageCatalog) getContext();
			/*
			 * if( activity.gallery.onTouchEvent(event) ){ return true; }
			 */
			// activity.gallery.onFling(null, null, 1000f, 0f);
			// Log.d("ftbt", "fling "+activity.gallery.onFling(null, null, 100f,
			// 100f) );
			Log.d("ftbt", event.toString());
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_1_DOWN:
				Log.d("ftbt", "mode=DRAG");
				mode = DRAG;
				p1.set(event.getX(), event.getY());
				moveMatrix.set(matrix);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_2_UP:
				point = null;
				Log.d("ftbt", "mode=NONE");
				mode = NONE;
				break;
			case MotionEvent.ACTION_POINTER_2_DOWN:
				p2.set(event.getX(), event.getY());
				initLength = getLength(event);
				if (true) {
					Log.d("ftbt", "mode=ZOOM");
					moveMatrix.set(matrix);
					mode = ZOOM;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				/*
				if (p1 != null) {
					Log.d("ftbt", "move p1=" + p1.x + " p1=" + p1.y);
				}
				if (p2 != null) {
					Log.d("ftbt", "move p2=" + p2.x + " p2=" + p2.y);
				}
				*/
				//Log.d("ftbt", "mode=" + mode);
				float ex = event.getX();
				float ey = event.getY();
				float d1_sq = (p1.x - ex) * (p1.x - ex) + (p1.y - ey)
						* (p1.y - ey);
				float d2_sq = (p2.x - ex) * (p2.x - ex) + (p2.y - ey)
						* (p2.y - ey);
				int near_side;
				if (d1_sq < d2_sq) { // near2d1
					if (point == null) {
						point = new PointF(ex, ey);
						point_side = 1;
					}
					p1 = new PointF(ex, ey);
					near_side = 1;
				} else {
					if (point == null) {
						point = new PointF(ex, ey);
						point_side = 2;
					}
					p2 = new PointF(ex, ey);
					near_side = 2;
				}
				//Log.d("ftbt", "ex=" + event.getX() + " ry=" + event.getY()
				//		+ " px=" + point.x + " py=" + point.y);
				float d2 = (ex - point.x) * (ex - point.x) + (ey - point.y)
						* (ey - point.y);
				if (d2 > 50 * 50 && (point_side != near_side)) { // ポインタ入れ替わり対策
					// 参考 http://cuaoar.jp/2010/05/flash-player-101-1.html
					break;
				}
				if (d2 > 150 * 150 && mode==ZOOM) { // 誤作動対策
					break;
				}
				//Log.d("ftbt", "move ex=" + event.getX() + " ey=" + event.getY());
				// activity.gallery.onScroll(e_temp, event, 100f, 100f);
				// //これは動いた

				switch (mode) {
				case DRAG:
					matrix.set(moveMatrix);
					// matrix.postTranslate(ex - point.x, ey - point.y);
					move(1.0f*(ex - point.x), 1.3f*(ey - point.y));
					view.setImageMatrix(matrix);
					break;
				case ZOOM:
					if (mode == ZOOM) { // ちょっとdisable
						float currentLength = getLength(event);
						middle = getMiddle(event, middle);
						if (true) {
							matrix.set(moveMatrix);
							float scale = filter(matrix, currentLength
									/ initLength);
							// matrix.postScale(scale, scale, middle.x,
							// middle.y);
							zoomImage((float)Math.pow(scale, 1.3f), middle.x, middle.y);
							view.setImageMatrix(matrix);
						}
						break;
					}
					break;
				}
			}
		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}
		return false;
	}

	// x軸方向の移動。画像移動matrix->一定以上はみ出たらgalleryのonscroll
	private void move(float dx, float dy) {
		// matrix.postTranslate(dx, 0f);
		float[] values = new float[9];
		matrix.getValues(values);
		values[Matrix.MTRANS_X] += dx;
		values[Matrix.MTRANS_Y] += dy;
		matrix.setValues(values);
	}

	// 画像の拡大縮小
	public void zoomImage(float scale, float mx, float my) {
		matrix.set(moveMatrix);
		Log.d("ftbt", "matrix update" + matrix.toString());

		// 現在のスケール取得
		float[] values = new float[9];
		matrix.getValues(values);
		float currentScale = values[Matrix.MSCALE_X];
		float postScale = currentScale * scale;

		bx = bm.getWidth() * postScale;
		by = bm.getHeight() * postScale;

		matrix.postScale(scale, scale, mx, my);
		/*
		 * Rect r = new Rect(); this.getGlobalVisibleRect(r); if ((r.left) *
		 * (r.left) > 1f) { //左が空いているなら左寄せ //デフォルトで左寄せ拡大 //mx = (mx - prex_bx/2)
		 * * () }else{ //右が開いているなら右寄せ float currentX = values[Matrix.MTRANS_X];
		 * float nextX = (bx - width); values[Matrix.MTRANS_X] = -nextX;
		 * //mx+=nextX-currentX; }
		 */
		float dbx = bm.getWidth() * (postScale - currentScale);
		// 中央寄せ
		/*
		 * values[Matrix.MTRANS_X] = -dbx / 2; mx += dbx / 2;
		 */

		// matrix.setValues(values);
		// matrix.postTranslate(-dbx / 2, 0f);
		mx = 0;
		setImageMatrix(matrix);
	}

	// ダブルタップ時に呼ばれる
	public boolean onDoubleTap(MotionEvent e) {
		Log.i("ftbt", "double tap");
		return false;
	}

	public void zoomImageToWindow() {
		// 画面サイズの取得
		WindowManager wm = ((WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE));
		Display display = wm.getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();

		if (bm.getWidth() < width && bm.getHeight() < height) {
			// matrix.reset();
			bx = bm.getWidth();
			by = bm.getHeight();
		} else {
			float scale = Math.min((float) width / (float) bm.getWidth(),
					(float) height / (float) bm.getHeight());
			Log.d("ftbt", "scale=" + scale);
			matrix.setScale(scale, scale);
			bx = bm.getWidth() * scale;
			by = bm.getHeight() * scale;
		}

		// 中央にセット
		matrix.preTranslate(-(bx - width) / 2, -(by - height) / 2);
	}

	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		this.bm = bm;

		// 画面サイズの取得
		WindowManager wm = ((WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE));
		Display display = wm.getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();

		// フィット+中央配置
		matrix.set(moveMatrix);
		zoomImageToWindow();
		Log.d("ftbt", "bx=" + bx + " by=" + by);
		setImageMatrix(matrix);
	}

	// 拡大縮小制限用
	private float filter(Matrix m, float s) {
		return (float) Math.max(s, 0.5);
	}

	private float getLength(MotionEvent e) {
		float xx = e.getX(1) - e.getX(0);
		float yy = e.getY(1) - e.getY(0);
		return FloatMath.sqrt(xx * xx + yy * yy);
	}

	private PointF getMiddle(MotionEvent e, PointF p) {
		float x = e.getX(0) + e.getX(1);
		float y = e.getY(0) + e.getY(1);
		p.set(x / 2, y / 2);
		return p;
	}

	// 画像をオンライン取得
	public void setImage() {
		waitDialog = new ProgressDialog(getContext());
		waitDialog.setMessage("ロード中...");
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
		String imgFile = CircleList.get();
		setTag(imgFile);
		Bitmap bmp = ImageCache.getImage(imgFile);
		if (bmp == null) { // キャッシュない
			// ImageCache.asyncSetImage(imgFile, imgFile);
			ImageGetTask task = new ImageGetTask(this);
			task.execute(imgFile);
		} else {
			setImageBitmap(bmp);
			dismissWaidDialog();
		}
	}

	void dismissWaidDialog() {
		waitDialog.dismiss();
	}

	// 画像を読み込む際にAsyncTaskを使うが、
	// 新しいAsyncTaskが来たら古いAsyncTaskは諦めて終了する。
	// ここに登録されてないIDのタスクはキャンセル
	static int LastTaskID = -1;
	static Object lock = new Object();

	// 画像取得用スレッド
	class ImageGetTask extends AsyncTask<String, Void, Bitmap> {
		private ImageCatalogSingleView image;
		private String tag;
		private int id;

		public ImageGetTask(ImageCatalogSingleView _image) {
			image = _image;
			tag = image.getTag().toString();
			// ID登録
			synchronized (ImageCatalogSingleView.lock) {
				ImageCatalogSingleView.LastTaskID += 1;
				id = ImageCatalogSingleView.LastTaskID;
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
					boolean network_result = ImageCache.setImage(urls[0]);
					if(!network_result){ //画像をhttpで取ってくるのに失敗
						Toast.makeText(getContext(), "画像の取得に失敗しました。\nネットワークがつながっていないか、" +
								"画像ファイルが存在しない可能性があります", Toast.LENGTH_SHORT).show();
					}
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
			dismissWaidDialog();
			// Tagが同じものが確認して、同じであれば画像を設定する
			if (image != null && tag != null & tag.equals(image.getTag())) {
				// image.setImageBitmap(result);
				try {
					image.setImageBitmap(result);
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
	}
}
