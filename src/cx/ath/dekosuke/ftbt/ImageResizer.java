package cx.ath.dekosuke.ftbt;

import android.graphics.Bitmap;
import java.lang.Math;
import android.util.Log;
import android.graphics.Matrix;

public class ImageResizer {
	public static Bitmap ResizeWideToSquare(Bitmap bmp) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		int size = Math.max(width, height);
		Matrix matrix = new Matrix();
		int pixels[] = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);

		Bitmap ret = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		if (width > height) {
			ret.setPixels(pixels, 0, width, 0, (width - height) / 2, width,
					height);
		} else {
			ret.setPixels(pixels, 0, width, (height - width) / 2, 0, width,
					height);
		}
		return ret;
	}
}
