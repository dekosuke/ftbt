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
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;
import java.lang.Math;
import android.view.GestureDetector;

class ImageCatalogSingleView extends ImageView implements Runnable {

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
	private PointF point = null;
	private PointF p1 = new PointF();
	private PointF p2 = new PointF();
	private int point_side = 1;

	private boolean rotated = false;

	// ダブルクリックのための(ry
	private GestureDetector gestureDetector;

	public ImageCatalogSingleView(Context context) {
		this(context, null, 0);
	}

	public ImageCatalogSingleView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	// 画面サイズを取得するための小細工・・・
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		width = w; // これで幅が取得できる
		height = h; // これで高さが取得できる
	}
	
	//画像への参照をなくしてメモリを解放させる
	public void clearImage(){
		try{
		super.setImageBitmap(null);
		if(bm!=null){
			int size = bm.getWidth() * bm.getHeight();
			bm = null;
			if(size>1000000){ //巨大な画像ならGC呼ぶよ
				Log.d("ftbt", "calling GC");
				System.gc();
			}
		}
		}catch(Exception e){
			Log.d("ftbt", "message", e);
		}
	}

	public ImageCatalogSingleView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		try {
			matrix = new Matrix();
			matrix.setScale(1, 1);
			bx = by = 0;
			setEnabled(true);
			setClickable(true); // これないとマルチタッチ効かないけど、あると親クラスのクリックイベントを奪う・・
			// setOnTouchListener(this);

			// 拡大縮小可能に
			this.setScaleType(ScaleType.MATRIX);
			// 画像取得
			setImage();
			// ダブルタップ
			this.gestureDetector = new GestureDetector(context,
					simpleOnGestureListener);
		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		onTouch(event);
		gestureDetector.onTouchEvent(event);
		return true;
		// trueにしないとdoubletap取れない
		// http://mokkouyou.blog114.fc2.com/blog-entry-49.html
	}

	public boolean onTouch(MotionEvent event) {
		try {
			// Toast.makeText(getContext(), "touch detected",
			// Toast.LENGTH_SHORT).show();
			ImageCatalog activity = (ImageCatalog) getContext();
			/*
			 * if( activity.gallery.onTouchEvent(event) ){ return true; }
			 */
			// activity.gallery.onFling(null, null, 1000f, 0f);
			// Log.d("ftbt", "fling "+activity.gallery.onFling(null, null, 100f,
			// 100f) );
			// Log.d("ftbt", event.toString());
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_1_DOWN:
				// Log.d("ftbt", "mode=DRAG");
				mode = DRAG;
				p1.set(event.getX(), event.getY());
				moveMatrix.set(matrix);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_2_UP:
				point = null;
				// Log.d("ftbt", "mode=NONE");
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
				float d2 = (ex - point.x) * (ex - point.x) + (ey - point.y)
						* (ey - point.y);
				if (d2 > 50 * 50 && (point_side != near_side)) { // ポインタ入れ替わり対策
					// 参考 http://cuaoar.jp/2010/05/flash-player-101-1.html
					break;
				}
				if (d2 > 150 * 150 && mode == ZOOM) { // 誤作動対策
					break;
				}
				// Log.d("ftbt", "move ex=" + event.getX() + " ey=" +
				// event.getY());
				// activity.gallery.onScroll(e_temp, event, 100f, 100f);
				// //これは動いた

				switch (mode) {
				case DRAG:
					matrix.set(moveMatrix);
					// matrix.postTranslate(ex - point.x, ey - point.y);
					move(1.0f * (ex - point.x), 1.3f * (ey - point.y));
					setImageMatrix(matrix);
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
							zoomImage((float) Math.pow(scale, 1.3f), middle.x,
									middle.y);
							setImageMatrix(matrix);
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

	// 画像の平行移動
	private void move(float dx, float dy) {
		// matrix.postTranslate(dx, 0f);
		float[] values = new float[9];
		matrix.getValues(values);
		values[Matrix.MTRANS_X] += dx;
		values[Matrix.MTRANS_Y] += dy;
		matrix.setValues(values);
	}

	// 画像の回転
	public void rotateImage() {
		Log.d("ftbt", "rotate called");
		/*
		 * float[] values = new float[9]; matrix.getValues(values);
		 * values[Matrix] += dx; values[Matrix.MTRANS_Y] += dy;
		 * matrix.setValues(values);
		 */
		matrix.postRotate(90f, width / 2, height / 2);
		setImageMatrix(matrix);
		rotated = !rotated;
	}

	// 画像の拡大縮小
	public void zoomImage(float scale, float mx, float my) {
		// matrix.set(moveMatrix);

		// 現在のスケール取得
		float[] values = new float[9];
		matrix.getValues(values);
		float currentScale = 1f;
		if (!rotated) {
			currentScale = Math.abs(values[Matrix.MSCALE_X]);
		} else {
			currentScale = Math.abs(values[Matrix.MSKEW_X]);
		}
		float postScale = currentScale * scale;

		Log.d("ftbt", "current=" + currentScale + " scale=" + scale);

		// 画面に収まるサイズ
		float minScale = 1f;
		if (!rotated) {
			minScale = Math.min((float) width / (float) bm.getWidth(),
					(float) height / (float) bm.getHeight());
		} else {
			minScale = Math.min((float) height / (float) bm.getWidth(),
					(float) width / (float) bm.getHeight());
		}
		minScale = Math.min(minScale, 1f);
		Log.d("ftbt", "minscale=" + minScale);
		if (postScale < minScale) {
			scale = minScale / currentScale;
		}
		matrix.postScale(scale, scale, mx, my);
		/*
		 * if(!rotated){ matrix.postScale(scale, scale, mx, my); }else{
		 * matrix.postScale(scale, scale, my, mx); }
		 */

		bx = bm.getWidth() * postScale;
		by = bm.getHeight() * postScale;

		setImageMatrix(matrix);
	}

	// ダブルタップ時に呼ばれる
	public boolean onDoubleTap(MotionEvent e) {
		Log.i("ftbt", "double tap");
		return false;
	}

	public void zoomImageToWindow() {
		if (bm.getWidth() < width && bm.getHeight() < height) {
			// matrix.reset();
			matrix.setScale(1f, 1f);
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

		// 画像をView中央にセット
		float[] values = new float[9];
		matrix.getValues(values);
		values[Matrix.MTRANS_X] = (width - bx) / 2;
		values[Matrix.MTRANS_Y] = (height - by) / 2;
		matrix.setValues(values);
	}

	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		this.bm = bm;

		// フィット+中央配置
		rotated = false;
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
		try{
			String imgFile = CircleList.get();
			setTag(imgFile);
			Bitmap bmp = ImageCache.getImage(imgFile);
			if (bmp == null) { // キャッシュない
				// ImageCache.asyncSetImage(imgFile, imgFile);
				ImageGetTask task = new ImageGetTask(this);
				task.execute(imgFile);
			} else {
				setImageBitmap(bmp);
			}
		}catch(Exception e){
			Log.d("ftbt", "message", e);
		}
		dismissWaidDialog();
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
	class ImageGetTask extends AsyncTask<String, Void, String> {
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
		protected String doInBackground(String... urls) {

			try {
				Log.d("ftbt", "getting" + urls[0]);
				ImageCache.getImage(urls[0]);
				if (ImageCache.getImage(urls[0]) == null) { // does not exist on cache
					boolean network_result = ImageCache.setImage(urls[0]);
					if (!network_result) { // 画像をhttpで取ってくるのに失敗
						Toast.makeText(
								getContext(),
								"画像の取得に失敗しました。\nネットワークがつながっていないか、"
										+ "画像ファイルが存在しない可能性があります",
								Toast.LENGTH_SHORT).show();
						return "";
					}
					return urls[0];
				}
			} catch (Exception e) {
				Log.i("ftbt", "message", e);
			}
			return "";
		}

		// メインスレッドで実行する処理
		@Override
		protected void onPostExecute(String url) {
			dismissWaidDialog();
			// Tagが同じものが確認して、同じであれば画像を設定する
			if (!url.equals("") && tag != null & tag.equals(image.getTag())) {
				// image.setImageBitmap(result);
				try {
					Bitmap bmp = ImageCache.getImage(url);
					image.setImageBitmap(bmp);
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

	private final SimpleOnGestureListener simpleOnGestureListener = new SimpleOnGestureListener() {

		@Override
		public boolean onDoubleTap(MotionEvent event) {
			Log.i("ftbt", "onDoubleTap");
			return super.onDoubleTap(event);
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent event) {
			Log.i("ftbt", "onDoubleTapEvent");
			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_2_UP:
				middle = getMiddle(event, middle);
				if (true) {
					matrix.set(moveMatrix);
					zoomImage(1.3f, middle.x, middle.y);
					setImageMatrix(matrix);
				}
				break;
			}
			return super.onDoubleTapEvent(event);
		}

		@Override
		public boolean onDown(MotionEvent event) {
			Log.i("ftbt", "onDown");
			return super.onDown(event);
		}

		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2,
				float velocityX, float velocityY) {
			Log.i("ftbt", "onFling");
			return super.onFling(event1, event2, velocityX, velocityY);
		}

		@Override
		public void onLongPress(MotionEvent event) {
			Log.i("ftbt", "onLongPress");
			super.onLongPress(event);
		}

		@Override
		public boolean onScroll(MotionEvent event1, MotionEvent event2,
				float distanceX, float distanceY) {
			Log.i("ftbt", "onScroll");
			return super.onScroll(event1, event2, distanceX, distanceY);
		}

		@Override
		public void onShowPress(MotionEvent event) {
			Log.i("ftbt", "onShowPress");
			super.onShowPress(event);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent event) {
			Log.i("ftbt", "onSingleTapConfirmed");
			return super.onSingleTapConfirmed(event);
		}

		@Override
		public boolean onSingleTapUp(MotionEvent event) {
			Log.i("ftbt", "onSingleTapUp");
			return super.onSingleTapUp(event);
		}

	};
}
