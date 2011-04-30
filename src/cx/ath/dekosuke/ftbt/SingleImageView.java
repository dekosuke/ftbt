package cx.ath.dekosuke.ftbt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;


class SingleImageView extends ImageView implements OnTouchListener {

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

	public SingleImageView(Context context) {
		this(context, null, 0);
	}

	public SingleImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SingleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		matrix = new Matrix();
		matrix.setScale(1, 1);
		//setClickable(true); //これないとマルチタッチ効かないけど、あると親クラスのクリックイベントを奪う・・
		setOnTouchListener(this);
	}

	public boolean onTouch(View v, MotionEvent event) {
		Toast.makeText(getContext(), "touch detected", Toast.LENGTH_SHORT).show();
		ImageView view = (ImageView) v;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.d("ftbt", "mode=DRAG");
			mode = DRAG;
			point.set(event.getX(), event.getY());
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
			switch (mode) {
			case DRAG:
				matrix.set(moveMatrix);
				matrix.postTranslate(event.getX() - point.x, event.getY() - point.y);
				view.setImageMatrix(matrix);
				break;
			case ZOOM:
				if (mode == ZOOM) {
					float currentLength = getLength(event);
					middle = getMiddle(event, middle);
					if (true) {
						matrix.set(moveMatrix);
						float scale = filter(matrix,currentLength / initLength);
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
	
	//拡大縮小制限用
	private float filter(Matrix m, float s){
		return s;
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
