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
		// Matrix matrix = new Matrix();
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

	public static Bitmap ResizeCutToSquare(Bitmap bmp) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		int size = Math.min(width, height);
		// Matrix matrix = new Matrix();
		int pixels[] = new int[size * size];
		int stride = Math.max(width, height);
		bmp.getPixels(pixels, 0, size, Math.max(0, (width - height) / 2),
				Math.max(0, (height - width) / 2), size, size);

		Bitmap ret = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		ret.setPixels(pixels, 0, size, 0, 0, size, size);
		/*
		 * if (width > height) { ret.setPixels(pixels, 0, width, 0, (width -
		 * height) / 2, width, height); } else { ret.setPixels(pixels, 0, width,
		 * (height - width) / 2, 0, width, height); }
		 */
		return ret;
	}

	public static Bitmap ResizeCenter(Bitmap bmp, int size) {
		Bitmap temp = ResizeCutToSquare(bmp);
		return Bitmap.createScaledBitmap(temp, size, size, true);
	}

}
