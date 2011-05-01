package cx.ath.dekosuke.ftbt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
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
	//fling用テンポラリ
	private MotionEvent e_temp;
	//x方向移動成分
	private float mx = 0f;

	public ImageCatalogSingleView(Context context) {
		this(context, null, 0);
	}

	public ImageCatalogSingleView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ImageCatalogSingleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		matrix = new Matrix();
		matrix.setScale(1, 1);
		setClickable(true); //これないとマルチタッチ効かないけど、あると親クラスのクリックイベントを奪う・・
		setOnTouchListener(this);
	}

	public boolean onTouch(View v, MotionEvent event) {
		//Toast.makeText(getContext(), "touch detected", Toast.LENGTH_SHORT).show();
		ImageView view = (ImageView) v;
		ImageCatalog activity = (ImageCatalog)getContext();
		/*
		if( activity.gallery.onTouchEvent(event) ){
			return true;
		}*/
		//activity.gallery.onFling(null, null, 1000f, 0f);
		//Log.d("ftbt", "fling "+activity.gallery.onFling(null, null, 100f, 100f) );
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
			//Log.d("ftbt", "fling");
			//activity.gallery.onScroll(e_temp, event, 100f, 100f); //これは動いた
			switch (mode) {
			case DRAG:
				matrix.set(moveMatrix);
				//activity.gallery.onScroll(e_temp, event, - (event.getX() - point.x), 0f); //これは動いた
				/*
				matrix.postTranslate(mxt, 0f);
				mx+=mxt;
				Log.d("ftbt", "mxt="+mxt);
				if(mxt<-200){
					activity.gallery.onScroll(e_temp, event, - (mxt+200), 0f);
					matrix.postTranslate(200-mxt, 0f);
					mx+=-200-mxt;
				}
				*/
				moveX(event);
				matrix.postTranslate(0f, event.getY() - point.y);
				view.setImageMatrix(matrix);
				break;
			case ZOOM:
				if (false && mode == ZOOM) { //ちょっとdisable
					float currentLength = getLength(event);
					middle = getMiddle(event, middle);
					if (true) {
						matrix.set(moveMatrix);
						float scale = filter(matrix,currentLength / initLength);
						Log.d("ftbt", "matrix update"+matrix.toString());
						matrix.postScale(scale, scale, middle.x, middle.y);
						view.setImageMatrix(matrix);
					}
					break;
				}
				break;
			}
		}
		return false;
	}
	
	//x軸方向の移動。画像移動matrix->一定以上はみ出たらgalleryのonscroll
	private void moveX(MotionEvent event){
		// 画面サイズの取得
		WindowManager wm = ((WindowManager) getContext()
				.getSystemService(Context.WINDOW_SERVICE));
		Display display = wm.getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();		
		float dx=event.getX() - point.x;
		Log.d("ftbt", "width="+width+" mx="+mx+" dx="+dx);
		float mxt = mx+dx;
		ImageCatalog activity = (ImageCatalog)getContext();
		if(mxt > (width/2)){
			matrix.postTranslate(width/2-mx, 0f);		
			activity.gallery.onScroll(e_temp, event, +(mx-width/2), 0f);
			mx=width/2;
		}else if(mxt < -(width/2)){
			matrix.postTranslate(-width/2-mx, 0f);			
			activity.gallery.onScroll(e_temp, event, +(mx+width/2), 0f);
			mx=-width/2;			
		}else{
			matrix.postTranslate(dx, 0f);
			mx+=dx;			
		}
	}
	
	//拡大縮小制限用
	private float filter(Matrix m, float s){
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
