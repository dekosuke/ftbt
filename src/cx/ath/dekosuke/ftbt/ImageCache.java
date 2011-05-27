package cx.ath.dekosuke.ftbt;

import java.util.HashMap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import java.util.Iterator;

import java.io.ByteArrayOutputStream;
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

	// Bitmap→バイトデータ
	private static byte[] bmp2data(Bitmap src, Bitmap.CompressFormat format,
			int quality) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		src.compress(format, quality, os);
		return os.toByteArray();
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

	public static boolean setImageFromBitmap(String url, Bitmap bmp) {
		String urlHash = FutabaCrypt.createDigest(url);
		byte[] bytes = bmp2data(bmp, Bitmap.CompressFormat.PNG, 100);
		try {
			// SDCard.saveFromURL(urlHash, new URL(url), true);
			SDCard.saveBin(urlHash, bytes, true);
			return true;
		} catch (Exception e) {
			FLog.d("message", e);
		}
		return false;
	}

	public static File saveImage(String url) {
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
			// 再読み込みしてください、みたいな例外を投げましょう
			/*
			 * setImage(url); //再帰 saveImage(url);
			 */
		}
		return null;
	}
	
	public static File saveImageToThread(String url, String threadName) {
		String urlHash = FutabaCrypt.createDigest(url);
		File file = new File(url);
		if (SDCard.cacheExist(urlHash)) {
			try {
				return SDCard.copyCacheToThreadFile(urlHash, file.getName(), threadName);
			} catch (Exception e) {
				FLog.d("message", e);
			}
		} else {
			// キャッシュがない(通常あまりないはずだが・・)
			// 再読み込みしてください、みたいな例外を投げましょう
			/*
			 * setImage(url); //再帰 saveImage(url);
			 */
		}
		return null;
	}

	public static void GC() {
		// currently do nothing
	}
}
