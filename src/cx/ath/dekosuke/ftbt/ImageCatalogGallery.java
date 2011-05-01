package cx.ath.dekosuke.ftbt;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Gallery;

public class ImageCatalogGallery extends Gallery implements OnTouchListener {

	public ImageCatalogGallery(Context context) {
		super(context);
		//super(context);
		// TODO Auto-generated constructor stub

		setClickable(true);
		setOnTouchListener(this);
	
	}
	
	public ImageCatalogGallery(Context context, AttributeSet attrSet) {
		super(context, attrSet);
	}

	public boolean onTouch(View v, MotionEvent event) {
		//FutabaImageCatalogAdapter adapter = (FutabaImageCatalogAdapter)getAdapter();
		//adapter.get
		return super.onTouchEvent(event);
	}
	
	public boolean onFling(MotionEvent e1, MotionEvent e2,
							float velocityX, float velocityY){
		return false;
		//return super.onFling(e1, e2, velocityX, velocityY);
	}
}
