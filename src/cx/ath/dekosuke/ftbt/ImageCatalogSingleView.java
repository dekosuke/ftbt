package cx.ath.dekosuke.ftbt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.AsyncTask;
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

class ImageCatalogSingleView extends ImageView implements OnTouchListener {

	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	private Matrix matrix = new Matrix();
	private int mode = NONE;
	/** 画像移動用の位置 */
	private PointF point = new PointF();
	/** ズーム時の座標 */
	private PointF middle = new PointF();
	/** ドラッグ用マトリックス */
	private Matrix moveMatrix = new Matrix();
	/** Zoom開始時の二点間距離 */
	private float initLength = 1;
	// fling用テンポラリ
	private MotionEvent e_temp;
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
	}

	public boolean onTouch(View v, MotionEvent event) {
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
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.d("ftbt", "mode=DRAG");
			mode = DRAG;
			point.set(event.getX(), event.getY());
			e_temp = event;
			moveMatrix.set(matrix);
			break;
		case MotionEvent.ACTION_POINTER_2_UP:
		case MotionEvent.ACTION_UP:
			Log.d("ftbt", "mode=NONE");
			mode = NONE;
			break;
		case MotionEvent.ACTION_POINTER_2_DOWN:
			initLength = getLength(event);
			if (true) {
				Log.d("ftbt", "mode=ZOOM");
				moveMatrix.set(matrix);
				mode = ZOOM;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			Log.d("ftbt", "move ex=" + event.getX() + " ey=" + event.getY());
			// activity.gallery.onScroll(e_temp, event, 100f, 100f); //これは動いた
			// 持ち上がり時にひどい値が来るのでスキップ
			if (prevEvent == null) {
				prevEvent = event;
				break;
			}
			if ((Math.abs(event.getX() - prevEvent.getX()) + Math.abs(event
					.getY() - prevEvent.getY())) > 50.0f) {
				point.x = event.getX();
				point.y = event.getY();
				prevEvent = event;
				break;
			}
			switch (mode) {
			case DRAG:
				matrix.set(moveMatrix);
				// activity.gallery.onScroll(e_temp, event, - (event.getX() -
				// point.x), 0f); //これは動いた
				/*
				 * matrix.postTranslate(mxt, 0f); mx+=mxt; Log.d("ftbt",
				 * "mxt="+mxt); if(mxt<-200){ activity.gallery.onScroll(e_temp,
				 * event, - (mxt+200), 0f); matrix.postTranslate(200-mxt, 0f);
				 * mx+=-200-mxt; }
				 */
				moveX(event);
				matrix.postTranslate(0f, event.getY() - point.y);
				view.setImageMatrix(matrix);
				break;
			case ZOOM:
				if (mode == ZOOM) { // ちょっとdisable
					float currentLength = getLength(event);
					middle = getMiddle(event, middle);
					if (true) {
						matrix.set(moveMatrix);
						float scale = filter(matrix, currentLength / initLength);
						// matrix.postScale(scale, scale, middle.x, middle.y);
						zoomImage((float) Math.max(scale, 0.8), middle.x,
								middle.y);
						view.setImageMatrix(matrix);
					}
					break;
				}
				break;
			}
		}
		return false;
	}

	// x軸方向の移動。画像移動matrix->一定以上はみ出たらgalleryのonscroll
	private void moveX(MotionEvent event) {
		// 画面サイズの取得
		WindowManager wm = ((WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE));
		Display display = wm.getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		float dx = event.getX() - point.x;
		Log.d("ftbt", "bx=" + bx + " width=" + width + " mx=" + mx + " dx="
				+ dx);
		Log.d("ftbt", "matrix=" + matrix.toString());
		ImageCatalog activity = (ImageCatalog) getContext();
		Rect r = new Rect();
		this.getGlobalVisibleRect(r);
		Log.d("ftbt", "rect=" + r.toString());
		if (bx > width && Math.abs(dx) > 1f) {
			// スクロール可能なサイズ。このサイズ以上スクロールした場合はgalleryにスクロールを渡す
			float dwx = (bx - width) / 2;
			// mx成分取得
			float[] values = new float[9];
			matrix.getValues(values);
			float mx = values[Matrix.MTRANS_X] + dwx;
			float mxt = mx + dx;
			if (mxt >= dwx) {
				Log.d("ftbt", "case1");
				// matrix.postTranslate(dwx-mx, 0f);
				values[Matrix.MTRANS_X] = 0f;
				matrix.setValues(values);
				// activity.gallery.onScroll(e_temp, event, +(mx-dwx), 0f);
				// activity.gallery.onScroll(e_temp, event, -dx, 0f);
				activity.gallery.onScroll(e_temp, event, -10f, 0f);
				mx = dwx;
			} else if (mxt <= -dwx) {
				Log.d("ftbt", "case2");
				// matrix.postTranslate(-dwx-mx, 0f);
				values[Matrix.MTRANS_X] = -(bx - width);
				matrix.setValues(values);
				// activity.gallery.onScroll(e_temp, event, +(mx+dwx), 0f);
				activity.gallery.onScroll(e_temp, event, 10f, 0f);
				mx = -dwx;
			} else if ((r.left) * (r.left) > 1f) { // viewの間にある
				Log.d("ftbt", "case3");
				if ((-dx) > r.left) {
					activity.gallery.onScroll(e_temp, event, r.left, 0f);
					matrix.postTranslate(dx+r.left, 0f);
				} else {
					activity.gallery.onScroll(e_temp, event, -dx, 0f);
				}
			} else if ((r.right - width) * (r.right - width) > 1f) {
				Log.d("ftbt", "case4");
				if (dx > width - r.right) {
					activity.gallery.onScroll(e_temp, event,
							-(width - r.right), 0f);
					matrix.postTranslate(dx-(width-r.right), 0f);
				} else {
					activity.gallery.onScroll(e_temp, event, -dx, 0f);
				}
			} else {
				Log.d("ftbt", "case5");
				matrix.postTranslate(dx, 0f);
				// mx+=dx;
			}
		} else { // 画面内に表示できてる
			activity.gallery.onScroll(e_temp, event, -dx, 0f);
		}
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2,
			float velocityX, float velocityY){
		ImageCatalog activity = (ImageCatalog) getContext();
		return activity.gallery.onFling(e1, e2, velocityX, velocityY);
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

	public void zoomImageToWindow() {
		// 画面サイズの取得
		WindowManager wm = ((WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE));
		Display display = wm.getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();

		if (bm.getWidth() < width && bm.getHeight() < height) {
			return;
		} else {
			float scale = Math.min((float) width / (float) bm.getWidth(),
					(float) height / (float) bm.getHeight());
			Log.d("ftbt", "scale=" + scale);
			matrix.setScale(scale, scale);
			bx = bm.getWidth() * scale;
			by = bm.getHeight() * scale;
		}
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
		matrix.postTranslate(-(bx - width) / 2, -(by - height) / 2);
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

}
