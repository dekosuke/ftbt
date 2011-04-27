package cx.ath.dekosuke.ftbt;

import java.util.HashMap;
import android.graphics.Bitmap;
import android.util.Log;
import java.util.Iterator;

import java.io.File;
import java.net.URL;

public class ImageCache {
	private static HashMap<String, Bitmap> cache = new HashMap<String, Bitmap>();
	private final static int SIZE_SUM_MAX = 1000 * 1000 * 20;
	private static int sizeSum = 0;

	public static Bitmap getImage(String url) {
		String urlHash = FutabaCrypt.createDigest(url);
		// 本当はキーを画像名ではなくスレッド名含むURLにすべき
		try {
			if (SDCard.cacheExist(urlHash)) {
				return SDCard.loadBitmapCache(urlHash);
			}
		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}
		return null;
	}

	public static void setImage(String url) {
		String urlHash = FutabaCrypt.createDigest(url);
		try {
			SDCard.saveFromURL(urlHash, new URL(url), true);
		} catch (Exception e) {
			Log.i("ftbt", "message", e);
		}
	}

	public static void GC() {
		// currently do nothing
	}
}
