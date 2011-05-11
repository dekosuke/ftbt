package cx.ath.dekosuke.ftbt;

import java.util.HashMap;
import android.graphics.Bitmap;
import android.util.Log;
import java.util.Iterator;

import java.io.File;
import java.net.URL;

public class ImageCache {
	
	public static Bitmap getImage(String url) {
		String urlHash = FutabaCrypt.createDigest(url);
		// 本当はキーを画像名ではなくスレッド名含むURLにすべき
		try {
			if (SDCard.cacheExist(urlHash)) {
				return SDCard.loadBitmapCache(urlHash);
			}
		} catch (Exception e) {
		FLog.d("message", e);
		}
		return null;
	}

	public static boolean setImage(String url) {
		String urlHash = FutabaCrypt.createDigest(url);
		try {
			SDCard.saveFromURL(urlHash, new URL(url), true);
			return true;
		} catch (Exception e) {
		FLog.d("message", e);
		}
		return false;
	}

	public static String saveImage(String url) {
		String urlHash = FutabaCrypt.createDigest(url);
		File file = new File(url);
		if (SDCard.cacheExist(urlHash)) {
			try {
				return SDCard.copyCacheToFile(urlHash, file.getName());
			} catch (Exception e) {
			FLog.d("message", e);
			}
		} else {
			// キャッシュがない(通常あまりないはずだが・・)
			//再読み込みしてください、みたいな例外を投げましょう
			/*
			 * setImage(url); //再帰 saveImage(url);
			 */
		}
		return "";
	}

	public static void GC() {
		// currently do nothing
	}
}
